task bwamem_to_bam {
	String sample_id
	File FASTQ
	File FASTQ2
	String reference

	command {
		bwa mem -M -t 8 -j -R '@RG\tID:A\tLB:testlib\tPU:FCB05VTABXX\tSM:${sample_id}\tPL:ILLUMINA' ${reference} \
		 ${FASTQ} ${FASTQ2} | samtools view -1 - > ${sample_id}.bam 
	}   

	runtime { 
		cpu : 8
	} 

	output { 
		File out = "${sample_id}.bam"
	}
}

#task indexBam {
#	File bam
#	String sample_id
#	command {
#		samtools index ${bam} ${sample_id}.bam.bai
#	}
#	runtime {
#		cpu : 1
#	}
#	output {
#		File out = "${sample_id}.bam.bai"
#	}
#}


task sortBam {
	File bam
	String sample_id
	command {
		samtools sort -o ${sample_id}.sorted.bam ${bam}
	}
	runtime {
		cpu : 1
	}
	output {
		File out = "${sample_id}.sorted.bam"
	}
}

task moveSortedBam {
	File sortedBam
	String dir
	command {
		mkdir -p ${dir} && mv ${sortedBam} ${dir}
	}
	runtime {
		cpu : 1
	}
}

workflow bwasub {
	File inputSamplesFile
	String outputDir
	Array[Array[String]] inputSamples = read_tsv(inputSamplesFile)
	String reference
	scatter (sample in inputSamples){
		call bwamem_to_bam {
        		input:
			        sample_id = sample[0],
			        FASTQ = sample[1],
			        FASTQ2 = sample[2],
			        reference = reference,
		}
		call sortBam {
			input: 
				bam = bwamem_to_bam.out,
				sample_id = sample[0]
		}
		#Done after merge!
		#call indexBam {
		#	input:
		#		bam = sortBam.out,
		#		sample_id = sample[0]
		#}
		call moveSortedBam {
			input:
				sortedBam = sortBam.out,
				dir = outputDir
		}
    	}
	output {
		Array[File] bams = sortBam.out
	}
}

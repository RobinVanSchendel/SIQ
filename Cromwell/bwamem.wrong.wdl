
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

workflow bwasub {
	File inputSamplesFile
	Array[Array[String]] inputSamples = read_tsv(inputSamplesFile)
	String reference
	scatter (sample in inputSamples){
		call sortBam {
			input: 
				bam = bwamem_to_bam.out,
				sample_id = sample[0]
		}
    	}
}

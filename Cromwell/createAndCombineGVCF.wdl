task addOrReplaceReadGroups {
	String inF
	String sample
	command {
		/home/rvanschendel/gatk-4.0.8.1/gatk AddOrReplaceReadGroups -I ${inF} -O ${sample}.g.sorted.bam -LB 0 -PL illumina -PU 1 -SM ${sample}
		samtools index ${sample}.g.sorted.bam
	}
	output {
		File outputFile = "${sample}.g.sorted.bam"
	}
}

task haploTypeCaller {
	String genome
	String inF
	command {
		/home/rvanschendel/gatk-4.0.8.1/gatk HaplotypeCaller -R ${genome} -I ${inF} -O ${inF}.g.vcf -ERC GVCF
	}
	output {
		File outputFile = "${inF}.g.vcf"
	}
}

task findGVCF {
	String dir
	command {
		find ${dir} -name "*.g.vcf"
	}
	output {
		Array[String] out = read_lines (stdout())
	}
}
task combineGVCF {
	Array[String] files
	String reference
	String outputFile
	command {
		/home/rvanschendel/gatk-4.0.8.1/gatk CombineGVCFs -R ${reference} --variant ${sep=" --variant " files} --output ${outputFile}
	}
	output {
		File out = "${outputFile}"
	}
}
task filterSNPS {
	File in
	String reference
	String outputFile
	String parameters = ' -select "QD < 2.0 || MQ < 40.0 || FS > 60.0 || SOR > 3.0 || MQRankSum < -12.5 || ReadPosRankSum < -8.0" -select-type SNP'
	command {
		/home/rvanschendel/gatk-4.0.8.1/gatk SelectVariants -R ${reference} --variant ${in} --output ${outputFile} ${parameters}
	}
	output {
		File out = "${outputFile}"
	}
	
	
}

workflow gVCFCombine {
	String reference
	String outputFile
	String outputFileFilter
	String files
	Array[String] samples = read_lines(files)
	scatter (sample in samples){
		String sampleName = basename(sample, ".sorted.bam")
		call addOrReplaceReadGroups{
			input:
				inF = sample,
				sample = sampleName
		}
		call haploTypeCaller{
			input:
				inF = addOrReplaceReadGroups.outputFile,
				genome = reference,
		}
	}
	call combineGVCF {
		input:
			files = haploTypeCaller.outputFile,
			reference = reference,
			outputFile = outputFile
	}
	call filterSNPS {
		input:
			reference = reference,
			in = combineGVCF.out,
			outputFile = outputFileFilter
	}
}

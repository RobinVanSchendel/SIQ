task genotypeGVCF {
	String file
	String reference
	String outputFile
	command {
		/home/rvanschendel/gatk-4.0.8.1/gatk GenotypeGVCFs \
			-R ${reference}\
			--variant ${file}\
			--output ${outputFile}
	}
	output {
		File out = "${outputFile}"
	}
}

workflow gVCFCombine {
	String file
	String reference
	String outputFile
	call genotypeGVCF {
		input:
			file = file,
			reference = reference,
			outputFile = outputFile
	}
}

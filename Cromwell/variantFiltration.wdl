task variantFiltration {
	String inputFile
	String reference
	String outputFile
	command {
		/home/rvanschendel/gatk-4.0.8.1/gatk SelectVariants -R ${reference} --variant ${inputFile} --output ${outputFile} \
		-select "QD < 2.0 || MQ < 40.0 || FS > 60.0 || SOR > 3.0 || MQRankSum < -12.5 || ReadPosRankSum < -8.0" \
		-select-type SNP \
		--invert-select
	}
	output {
		File out = "${outputFile}"
	}
}

workflow gVCFCombine {
	String reference
	String outputFile
	String file

	call variantFiltration {
		input:
			inputFile = file,
			reference = reference,
			outputFile = outputFile
	}
}

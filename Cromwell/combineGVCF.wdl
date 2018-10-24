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

workflow gVCFCombine {
	String dir
	String reference
	String outputFile
	call findGVCF {
		input:
			dir = dir
	}
	call combineGVCF {
		input:
			files = findGVCF.out,
			reference = reference,
			outputFile = outputFile
	}
}

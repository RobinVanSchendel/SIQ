task findGVCF {
        String dir
        command {
                find ${dir} -name "*.g.vcf"
        }
        output {
                Array[String] out = read_lines (stdout())
        }
}


workflow merge {
	String dir
        String reference
        File outputFile
}

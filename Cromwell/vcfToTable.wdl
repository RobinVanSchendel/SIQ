workflow mainFilterSNPS {
	File in
	String reference
	String outputFile
	call vcfToTable {
                input:
                        file = in,
                        reference = reference,
                        outputFile = outputFile
        }
}
task vcfToTable {
        File file
        String reference
        String outputFile
        String parameters = ' -F CHROM -F POS -F ID -F QUAL -F AC -F GT -F HOM-REF -F HOM-VAR -F HET -F NO-CALL -F TYPE -F NSAMPLES -F NCALLED -F VAR -F SM -GF'
        command {
                /home/rvanschendel/gatk-4.0.8.1/gatk VariantsToTable -R ${reference} -V ${file} --output ${outputFile} ${parameters}
        }
        output {
                File out = "${outputFile}"
        }
}

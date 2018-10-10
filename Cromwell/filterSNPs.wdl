workflow mainFilterSNPS {
	File in
	String reference
	String outputFile
	call genotypeGVCF {
                input:
                        file = in,
                        reference = reference,
                        outputFile = outputFile
        }

	call filterSNPS {
		input: 
		in = genotypeGVCF.out,
		reference = reference,
		outputFile = outputFile
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


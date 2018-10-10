task concatFiles {
	Array[File] inFiles
	String outFile
	command {
		cat ${sep=" " inFiles} > ${outFile}
	}
	output {
		File out = "${outFile}"
	}
}
task concatArrays {
    Array[File] inA
    Array[File] inB
    Array[File] inC
    command {
        cat ${write_lines(inA)}
        cat ${write_lines(inB)}
        cat ${write_lines(inC)}
    }
    output {
        Array[File] out = read_lines(stdout())
    }
}
task PEAR {
	String r1
	String r2
	String out
	command {
		/exports/humgen/rvanschendel/PEAR/bin/pear-0.9.6-bin-64 -f ${r1} -r ${r2} -o ${out} -j 4
	}
	output {
		File assembled = "${out}.assembled.fastq"
		File discarded = "${out}.discarded.fastq"
		File unassF = "${out}.unassembled.forward.fastq"
		File unassR = "${out}.unassembled.reverse.fastq"
	}
	runtime {
		cpu : 4
	}
}
task Analyzer {
	String inFile
	String subject
	String left
	String right
	String param = " -m 2 -c -e 0.05 "
	String outputFile
	String leftPrimer
	String rightPrimer
	String minPassedPrimer
	String additionalSearch
	String alias
	command {
		java -jar /exports/humgen/rvanschendel/Project_Hartwig/Tijsterman_Analyzer_FASTQ_1.6.jar ${param} \
		-infile ${inFile} -subject ${subject} -left ${left} -right ${right} -o ${outputFile} -leftPrimer ${leftPrimer} -rightPrimer ${rightPrimer} \
		-minPassedPrimer ${minPassedPrimer} -additionalSearch "${additionalSearch}" -alias "${alias}"
	}
	runtime {
		cpu : 1
		memory : 15		
	}
	output {
		File out = "${outputFile}"
	}
}
workflow NGS_PCR {
	File samples
	Array[Array[String]] sampleArray = read_tsv(samples)
	String outputFile

	scatter (sample in sampleArray){
		call PEAR {
			input: 
				r1 = sample[0],
				r2 = sample[1],
				out = sample[2]
		}
		call Analyzer as AnalyzerAss{
			input:
				inFile = PEAR.assembled,

				subject = sample[4],
				left = sample[5],
				right = sample[6],
				leftPrimer = sample[7],
				rightPrimer = sample[8],
				outputFile = sample[2],
				alias = sample[3],
				additionalSearch = sample[9],
				minPassedPrimer = sample[10]
		}
		call Analyzer as AnalyzerF{
                        input:
                                inFile = PEAR.unassF,

                                subject = sample[4],
                                left = sample[5],
                                right = sample[6],
                                leftPrimer = sample[7],
                                rightPrimer = sample[8],
                                outputFile = sample[2],
                                alias = sample[3],
                                additionalSearch = sample[9],
                                minPassedPrimer = sample[10]
                }
		call Analyzer as AnalyzerR{
                        input:
                                inFile = PEAR.unassR,

                                subject = sample[4],
                                left = sample[5],
                                right = sample[6],
                                leftPrimer = sample[7],
                                rightPrimer = sample[8],
                                outputFile = sample[2],
                                alias = sample[3],
                                additionalSearch = sample[9],
                                minPassedPrimer = sample[10]
                }


	}
	call concatArrays{
		input:
			inA = AnalyzerAss.out,
			inB = AnalyzerF.out,
			inC = AnalyzerR.out
	}
	call concatFiles{
		input:
			inFiles = concatArrays.out,
			outFile = outputFile
	}
    }

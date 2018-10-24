task findBams {
	String sampleName
	String dir
	command {
		find ${dir} -name "${sampleName}*.sorted.bam"
	}
	output {
		Array[String] out = read_lines (stdout())
	}
}
task mergeBams {
	Array[String] bams
	String sampleName
	String dir
	command {
		samtools merge ${sampleName}.sorted.bam ${sep=" " bams}
	}
	runtime {
		cpu: 1
	}
	output {
		File out = "${sampleName}.sorted.bam"
	}
}
task indexBam {
        File bam
        String sampleId
        command {
                samtools index ${bam} ${sampleId}.bam.bai
        }
        runtime {
                cpu : 1
        }
        output {
                File out = "${sampleId}.bam.bai"
        }
}
task moveFile {
        File file
        String dir
        command {
		#let's move instead of copy
                mkdir -p ${dir} && mv ${file} ${dir}
        }
        runtime {
                cpu : 1
        }
}


workflow merge {
	File inputRealSamplesFile
	String dir
	String outputDir
	Array[File] bams
	Array[Array[String]] inputRealSamples = read_tsv(inputRealSamplesFile)
	scatter (realSample in inputRealSamples){
		call findBams {
			input:
				sampleName = realSample[0],
				dir = dir
		}
		call mergeBams {
			input:
				bams = findBams.out,
				sampleName = realSample[0],
				dir = dir
		}
		call indexBam {
			input:
				bam = mergeBams.out,
				sampleId = realSample[0]
		}
		call moveFile as copyBam {
                        input:
                                file = mergeBams.out,
                                dir = outputDir
                }
		call moveFile as copyIndex {
			input:
				file = indexBam.out,
				dir = outputDir
		}
	}
}

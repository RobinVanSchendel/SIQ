task bwamem_to_bam {
        String sample_id
        File FASTQ
        File FASTQ2
        String reference

        command {
                bwa mem -M -t 8 -j -R '@RG\tID:A\tLB:testlib\tPU:FCB05VTABXX\tSM:${sample_id}\tPL:ILLUMINA' ${reference} \
                 ${FASTQ} ${FASTQ2} | samtools view -1 - > ${sample_id}.bam
        }

        runtime {
                cpu : 8
        }

        output {
                File out = "${sample_id}.bam"
        }
}
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

task moveFile {
        File file
        String dir
        command {
                mkdir -p ${dir} && mv ${file} ${dir}
        }
        runtime {
                cpu : 1
        }
}

task findBams {
        String sampleName
        String dir
	#dummy variable to trick cromwell into halting the second scatter
	Array[File] sortBams
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
                samtools index ${bam} ${sampleId}.sorted.bam.bai
        }
        runtime {
                cpu : 1
        }
        output {
                File out = "${sampleId}.sorted.bam.bai"
        }
}


workflow main_workflow {
	#for mapper
        File inputSamplesFile
        String outputDir
        String reference
	Array[Array[String]] inputSamples = read_tsv(inputSamplesFile)

	scatter (sample in inputSamples){
                call bwamem_to_bam {
                        input:
                                sample_id = sample[0],
                                FASTQ = sample[1],
                                FASTQ2 = sample[2],
                                reference = reference,
                }
                call sortBam {
                        input:
                                bam = bwamem_to_bam.out,
                                sample_id = sample[0]
                }
                call moveFile as moveSortedBam {
                        input:
                                file = sortBam.out,
                                dir = outputDir
                }
        }

	#for merge
	File inputRealSamplesFile
	String outputDirMerge

        Array[String] inputRealSamples = read_lines(inputRealSamplesFile)

        scatter (realSample in inputRealSamples){
                call findBams {
                        input:
                                sampleName = realSample,
                                dir = outputDir,
				sortBams = sortBam.out
				#sortBams is a dummy variable to make sure execution of this scatter waits for the previous mapping call
                }
                call mergeBams {
                        input:
                                bams = findBams.out,
                                sampleName = realSample,
                }
                call indexBam {
                        input:
                                bam = mergeBams.out,
                                sampleId = realSample
                }
                call moveFile as copyBam {
                        input:
                                file = mergeBams.out,
                                dir = outputDirMerge
                }
                call moveFile as copyIndex {
                        input:
                                file = indexBam.out,
                                dir = outputDirMerge
                }
        }


}

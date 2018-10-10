task createPindelFile {
  Array[String] files
  String outFile
  command {
  	for file in ${sep=' ' files} ; do
			baseFile=$(basename "$file");
			echo -e "$file\t250\t$baseFile";
		done > ${outFile}
	}
	output {
		File out = "${outFile}"
	}
}
task Pindel {
	String genome
	File inputFile
	String outputFile
	String chr
	Int x = 5
	Float u = 0.02
	Float e = 0.01
	Int B = 2000000
	Int w = 1
	Int NM = 0
	Int T = 8
	
	command {
		/exports/humgen/rvanschendel/Pindel/src/pindel -f ${genome} -i ${inputFile} -o ${outputFile} -T 8 -B ${B} \
		 -w ${w} -x ${x} -c ${chr} --NM ${NM} -u ${u} -e ${e}
	}
	output {
		File TD = "${outputFile}_TD" 
		File D = "${outputFile}_D"
		File SI = "${outputFile}_SI"
		File INV = "${outputFile}_INV"
	}
	runtime {
		cpu : 8
	}
}


task mergeFiles {
	Array[File] TD
	Array[File] D
	Array[File] SI
	Array[File] INV
	String outputDir
	String outputFile
	
	command {
		mkdir -p ${outputDir}
		fileTD="${outputDir}/${outputFile}_TD"
		fileD="${outputDir}/${outputFile}_D"
		fileSI="${outputDir}/${outputFile}_SI"
		fileINV="${outputDir}/${outputFile}_INV"
		cat ${sep=' ' TD} > $fileTD
		cat ${sep=' ' D} > $fileD
		cat ${sep=' ' INV} > $fileINV
		cat ${sep=' ' SI} > $fileSI
	}
	output{
		String outputFileName = "${outputDir}/${outputFile}"
	}
	
}
task analyzePindel {
	String inputFile
	String outputFile
	command {
		java -jar /exports/humgen/rvanschendel/Project_Hartwig/Pindel_1.0.jar -i ${inputFile} -o ${outputFile}
	}
	runtime {
		memory : 15
	}
}

workflow RunPindel {
	File inputRealSamplesFile
	Array[String] inputRealSamples = read_lines(inputRealSamplesFile)
	String out = "temp.txt"
	call createPindelFile {
		input:
			files = inputRealSamples,
			outFile = out
	}
	#Array[String] chromosomes = ["CHROMOSOME_I","CHROMOSOME_II","CHROMOSOME_III","CHROMOSOME_IV","CHROMOSOME_V","CHROMOSOME_X","CHROMOSOME_MtDNA"]
	Array[String] chromosomes = ["CHROMOSOME_I:2000-50000","CHROMOSOME_II:2000-50000","CHROMOSOME_III:2000-50000","CHROMOSOME_IV:2000-50000","CHROMOSOME_V:2000-50000","CHROMOSOME_X:2000-50000","CHROMOSOME_MtDNA:2000-5000"]
	##Pindel variables
	Int x = 5
	Float u = 0.02
	Float e = 0.01
	String genome = "/exports/humgen/rvanschendel/Genomes/c_elegans.WS235.genomic.fa"
	String outputFile
	String outputDir
	String outputFileAnalysis
 
	scatter (chr in chromosomes) {
		call Pindel {
			input:
			genome = genome,
			inputFile = createPindelFile.out,
			outputFile = outputFile,
			chr = chr
			
		}
	}
	call mergeFiles {
		input:
			TD = Pindel.TD,
			SI = Pindel.SI,
			INV = Pindel.INV,
			D = Pindel.D,
			outputDir = outputDir,
			outputFile = outputFile
	}
	call analyzePindel {
		input:
			inputFile = mergeFiles.outputFileName,
			outputFile = outputFileAnalysis
	}
}

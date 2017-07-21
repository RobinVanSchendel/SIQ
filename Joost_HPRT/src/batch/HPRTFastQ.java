package batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import utils.CompareSequence;

public class HPRTFastQ {

	public static void main(String[] args) {
		String fileName = "HPRTTableFASTQ.txt";
		SequenceController sq = new SequenceController();
		//for IS color parts
		HashMap<String, String> colorMap = new HashMap<String,String>();
		colorMap.put("px458_Cas9-GFP", "brown");
		colorMap.put("mmHPRT_sequence_Fasta", "purple");
		colorMap.put("Flank insert", "orange");
		colorMap.put("Flank insert rc", "red");
		colorMap.put("Tandem duplication", "green");
		colorMap.put("Tandem duplication2", "darkgreen");
		boolean collapse = true;
		String name = "HPRT_FASTQ_output";
		double maxError = 0.05;
		
		
		if(collapse){
			name +="_collapse";
		}
		name +=".txt";
		File output = new File(name);
		sq.setOutputFile(output);
		sq.setCollapseEvents(collapse);
		
		//sq.setPrintOnlyISParts();
		sq.setColorMap(colorMap);
		File file = new File(fileName);
		
		if(sq.getOutputFile() != null){
			sq.writeln("Type\t"+CompareSequence.getOneLineHeader());
		}
		else{
			System.out.println("Type\t"+CompareSequence.getOneLineHeader());
		}
		
		Scanner s = null;
		final long startTime = System.nanoTime();
		try {
			s = new Scanner(file);
			boolean first = true;
			int fileColumn = -1;
			int subjectColumn = -1;
			int lFColumn = -1;
			int rFColumn = -1;
			int typeColumn = -1;
			while(s.hasNextLine()){
				String line = s.nextLine();
				String[] parts = line.split("\t");
				if(first){
					int i = 0;
					for(String str: parts){
						if(str.equals("Files")){
							fileColumn = i;
						}
						if(str.equals("Subject")){
							subjectColumn = i;
						}
						if(str.equals("LeftFlank")){
							lFColumn = i;
						}
						if(str.equals("RightFlank")){
							rFColumn = i;
						}
						if(str.equals("Type")){
							typeColumn = i;
						}
						i++;
					}
					first = false;
				}
				else{
					String files = parts[fileColumn];
					String subject = parts[subjectColumn];
					String leftFlank = parts[lFColumn];
					String rightFlank = parts[rFColumn];
					String type = parts[typeColumn];
					sq.readFilesFASTQMultiThreaded(files, subject, leftFlank, rightFlank, type, new File("Z:\\Joost\\Files\\Manuscripts\\Schimmel_etal_2017\\Robin\\px458_HPRT.txt"), true, maxError);
				}
			}
			s.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final long duration = (System.nanoTime() - startTime);
		long seconds = TimeUnit.NANOSECONDS.toSeconds(duration);
		System.out.println("This script took "+seconds+" seconds");
	}

}

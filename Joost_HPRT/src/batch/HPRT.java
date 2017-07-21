package batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import utils.CompareSequence;

public class HPRT {

	public static void main(String[] args) {
		String fileName = "HPRTTable.txt";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmdd-kkmmss");
		Date date = new Date();

	      // display time and date using toString()
	    String output = sdf.format(date)+"_output.txt";
		if(args.length>1){
			fileName = args[1];
		}
		if(args.length>2){
			output = args[2];
		}
		File outputFile = new File(output);
		SequenceController sq = new SequenceController();
		//for IS color parts
		HashMap<String, String> colorMap = new HashMap<String,String>();
		colorMap.put("px458_Cas9-GFP", "brown");
		colorMap.put("mmHPRT_sequence_Fasta", "purple");
		colorMap.put("Flank insert", "orange");
		colorMap.put("Flank insert rc", "red");
		colorMap.put("Tandem duplication", "green");
		colorMap.put("Tandem duplication2", "darkgreen");
		
		//sq.setPrintOnlyISParts();
		sq.setColorMap(colorMap);
		File file = new File(fileName);
		if(!isTableOK(file)){
			System.exit(0);
		}
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outputFile);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		writer.println("Type\t"+CompareSequence.getOneLineHeader());
		//System.out.println("Type\t"+CompareSequence.getOneLineHeader());
		Scanner s = null;
		try {
			s = new Scanner(file);
			boolean first = true;
			int fileColumn = -1;
			int subjectColumn = -1;
			int lFColumn = -1;
			int rFColumn = -1;
			int typeColumn = -1;
			int addSearchColumn = -1;
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
						if(str.equals("AdditionalSearch")){
							addSearchColumn = i;
						}
						i++;
					}
					first = false;
				}
				else{
					String files = parts[fileColumn];
					String subject = parts[subjectColumn];
					String leftFlank = null;
					if(lFColumn>-1){
						leftFlank = parts[lFColumn];
					}
					String rightFlank = null;
					if(rFColumn>-1){
						rightFlank = parts[rFColumn];
					}
					String type = null;
					if(typeColumn >-1){
						 type = parts[typeColumn];
					}
					File search = null;
					if(addSearchColumn>-1){
						search = new File(parts[addSearchColumn]);
					}
					sq.readFiles(files, subject, leftFlank, rightFlank, type, search, writer);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.close();
		System.out.println("Output can be found in "+outputFile.getAbsolutePath());
	}

	private static boolean isTableOK(File file) {
		if(!file.exists()){
			System.err.println("File "+file.getAbsolutePath()+" could not be found");
			System.err.println("Specify a file with a sequence table or make sure HPRTTable.txt is present");
			return false;
		}

		boolean error = false;
		try {
			Scanner s = new Scanner(file);
			boolean first = true;
			int fileColumn = -1;
			int subjectColumn = -1;
			int addSearchColumn = -1;
			while(s.hasNextLine()){
				String line = s.nextLine();
				String[] parts = line.split("\t");
				if(parts.length<2){
					System.err.println("Table should be a tab separated file containing at least the directory with sequence files and a subject file");
					error = true;
				}
				if(first){
					boolean files = false;
					int i = 0;
					for(String str: parts){
						if(str.equals("Files")){
							fileColumn = i;
						}
						if(str.equals("Subject")){
							subjectColumn = i;
						}
						if(str.equals("AdditionalSearch")){
							addSearchColumn = i;
						}
						i++;
					}
					first = false;
					if(fileColumn == -1){
						System.err.println("Column Files could not be found");
						error = true;
					}
					if(subjectColumn == -1){
						System.err.println("Column Files could not be found");
						error = true;
					}
				}
				else{
					File tempFile = new File(parts[fileColumn]);
					if(!tempFile.exists()){
						System.err.println(tempFile.getAbsolutePath()+" does not exist");
						error = true;
					}
					if(tempFile.exists() && !tempFile.isDirectory()){
						System.err.println(tempFile.getAbsolutePath()+" is not a directory");
						error = true;
					}
					tempFile = new File(parts[subjectColumn]);
					if(!tempFile.exists()){
						System.err.println(tempFile.getAbsolutePath()+" does not exist");
						error = true;
					}
					if(tempFile.exists() && !tempFile.isFile()){
						System.err.println(tempFile.getAbsolutePath()+" is not a file");
						error = true;
					}
					if(addSearchColumn>-1 && parts[addSearchColumn]!= null){
						tempFile = new File(parts[addSearchColumn]);
						if(!tempFile.exists()){
							System.err.println(tempFile.getAbsolutePath()+" does not exist");
							error = true;
						}
						if(tempFile.exists() && !tempFile.isFile()){
							System.err.println(tempFile.getAbsolutePath()+" is not a file");
							error = true;
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return !error;
	}

}

package hprt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class HPRT {

	public static void main(String[] args) {
		String dirName = "Z:\\Joost\\Files\\Manuscripts\\Schimmel_etal_2016\\Robin";
		String fileName = "HPRTTable.txt";
		SequenceController sq = new SequenceController();
		File dir = new File(dirName);
		File file = new File(fileName);
		
		Scanner s = null;
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
					sq.readFiles(files, subject, leftFlank, rightFlank, type);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

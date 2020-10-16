package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class SimplifyTSVTable {

	public static void main(String[] args) {
		File fileIn = null;
		if(args.length==1) {
			String file = args[0];
			fileIn = new File(file);
		}
		else {
			String file = "Z:\\Datasets - NGS, UV_TMP, MMP\\Targeted Sequencing\\SIQPlotter";
			fileIn = new File(file);
		}
		ArrayList<File> list = new ArrayList<File>();
		if(!fileIn.exists()) {
			System.err.println("File "+fileIn.getAbsolutePath()+" does noet exist or is a directory");
			System.exit(0);
		}
		if(fileIn.isDirectory()) {
			for(File tempFile: fileIn.listFiles()) {
				if(!tempFile.getName().contains("reduced") && !tempFile.getName().contains("stats")) {
					list.add(tempFile);
					System.out.println(tempFile.getName());
				}
			}
		}
		else if(fileIn.isFile()) {
			list.add(fileIn);
		}
		//System.exit(0);
		
		
		for(File f: list) {
			String outputFile = f.getAbsolutePath()+"_reduced.txt";
			if(f.getAbsolutePath().endsWith(".txt")) {
				outputFile = f.getAbsolutePath().replace(".txt", "_reducedCols.txt");
			}
			File outputF = new File(outputFile);
			long sizeBytes = f.length();
			long sizeKB = sizeBytes/1024;
			long sizeMB = sizeKB/1024;
			
			try {
				Scanner s = new Scanner(f);
				int line = 0;
				String[] keepColumns = {"countEvents", "fraction", "Alias", "Subject","delSize", "insSize"
						,"Type","homology","homologyLength","delStart","delEnd"
						,"delRelativeStart","delRelativeEnd","delRelativeStartRight","delRelativeEndRight","delRelativeStartTD",
						"delRelativeEndTD","getHomologyColor"
						};
				
				ArrayList<Integer> colKeepHM = null;
				FileWriter writer = new FileWriter(outputF);
				BufferedWriter bw = new BufferedWriter(writer);
				
				while(s.hasNextLine()) {
					String curLine = s.nextLine();
					if(line==0) {
						String[] cols = curLine.split("\t");
						for(String col: cols) {
							//System.out.println(col);
							colKeepHM = createKeepColumns(cols,keepColumns);
						}
						//System.exit(0);
					}
					String[] cols = curLine.split("\t");
					StringBuffer output = new StringBuffer(); 
					for(int colNr=0;colNr<cols.length;colNr++) {
						if(colKeepHM.contains(colNr)) {
							if(output.length()>0) {
								output.append("\t");
							}
							output.append(cols[colNr]);
						}
					}
					output.append("\n");
					bw.write(output.toString());
					line++;
				}
				s.close();
				bw.close();
				long sizeBytesOut = outputF.length();
				long sizeKBytesOut = sizeBytesOut/1024;
				long sizeMBytesOut = sizeKBytesOut/1024;
				System.out.println(f.getAbsolutePath()+ " size: "+sizeMB+"Mb");
				System.out.println(outputF.getAbsolutePath()+" reduced to: "+sizeMBytesOut+"Mb");
				
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static ArrayList<Integer> createKeepColumns(String[] cols, String[] keepColumns) {
		ArrayList<Integer> colKeepHM = new ArrayList<Integer>();
		int colCounter = 0;
		for(String col: cols) {
			for(String kC: keepColumns) {
				if(col.contentEquals(kC)) {
					colKeepHM.add(colCounter);
				}
			}
			colCounter++;
		}
		return colKeepHM;
	}

}

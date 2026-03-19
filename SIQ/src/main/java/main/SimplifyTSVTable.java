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
		boolean addGene = false;
		if(args.length==1) {
			String file = args[0];
			fileIn = new File(file);
		}
		else {
			//String file = "Z:\\Datasets - NGS, UV_TMP, MMP\\Targeted Sequencing\\Hartwig\\LUMC-001-104-Marco_CRISPR_screen\\Raw\\Processed\\";
			//String file = "Z:\\Datasets - NGS, UV_TMP, MMP\\Targeted Sequencing\\Hartwig\\LUMC-001-104-Marco_CRISPR_screen\\Raw\\Processed_Again\\";
			//String file = "C:\\Temp\\MBdata\\MBcontrols_fixed.txt";
			//String file = "E:\\Joost_Repair_Seq\\outSlurm";
			//String file = "Z:\\Datasets - NGS, UV_TMP, MMP\\Joost_Repair_Seq_Screen_Liu_Adamson\\dsbs\\fullFiles\\compressed";
			String file = "Z:\\Datasets - NGS, UV_TMP, MMP\\Targeted Sequencing\\Hartwig\\GenomeScan105973Marco\\Analysis\\analyzed";
			fileIn = new File(file);
		}
		System.out.println(fileIn);
		System.out.println(fileIn.isDirectory());
		ArrayList<File> list = new ArrayList<File>();
		if(!fileIn.exists()) {
			System.err.println("File "+fileIn.getAbsolutePath()+" does noet exist or is a directory");
			System.exit(0);
		}
		if(fileIn.isDirectory()) {
			for(File tempFile: fileIn.listFiles()) {
				if(tempFile.getName().contains("compressed") && !tempFile.getName().contains("reduced")) {
					list.add(tempFile);
				}
				else if(!tempFile.getName().contains("reduced")) {
					list.add(tempFile);
				}
				/*
				if(!tempFile.getName().contains("reduced") && !tempFile.getName().contains("stats")) {
					if(tempFile.getName().contains(".txt")) {
						list.add(tempFile);
						System.out.println(tempFile.getName());
					}
				}
				*/
			}
		}
		else if(fileIn.isFile()) {
			list.add(fileIn);
		}
		//System.exit(0);
		
		
		for(File f: list) {
			System.out.println(f.getName());
			//System.exit(0);
			String outputFile = f.getAbsolutePath()+"_reduced.txt";
			if(f.getAbsolutePath().endsWith(".txt")) {
				outputFile = f.getAbsolutePath().replace(".txt", "_reducedCols.txt");
			}
			File outputF = new File(outputFile);
			if(outputF.exists()) {
				System.out.println("Skipping "+f.getAbsolutePath());
				continue;
			}
			long sizeBytes = f.length();
			long sizeKB = sizeBytes/1024;
			long sizeMB = sizeKB/1024;
			
			try {
				Scanner s = new Scanner(f);
				int line = 0;
				String[] keepColumns = {"countEvents", "fraction", "Barcode","Clone", "Gene" ,"Alias", "Subject","delSize", "insSize"
						,"Type","homology","homologyLength","delStart","delEnd"
						,"delRelativeStart","delRelativeEnd","delRelativeStartRight","delRelativeEndRight","delRelativeStartTD",
						"delRelativeEndTD","getHomologyColor", "SNVMutation","del", "insertion","File","Raw","isFirstHit"
						};
				
				ArrayList<Integer> colKeepHM = null;
				int barcodeColumn = -1;
				
				FileWriter writer = new FileWriter(outputF);
				BufferedWriter bw = new BufferedWriter(writer);
				
				while(s.hasNextLine()) {
					String curLine = s.nextLine();
					if(line==0) {
						String[] cols = curLine.split("\t");
						int count = 0;
						for(String col: cols) {
							System.out.println(col);
							colKeepHM = createKeepColumns(cols,keepColumns);
							if(addGene) {
								if(col.contentEquals("Barcode")) {
									barcodeColumn = count;
								}
							}
							count++;
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
					if(addGene) {
						if(line==0) {
							output.append("\tGene");
						}
						else {
							String gene = getGene(cols[barcodeColumn]);
							output.append("\t"+gene);
						}
					}
					try {
						//column 0 has to be the countEvents
						if(line!=0) {
							Integer.parseInt(cols[0]);
						}
						output.append("\n");
						bw.write(output.toString());
					}
					//ignore the ones that cannot be parsed
					catch(Exception e) {
						System.err.println("problem with line "+line+" "+curLine);
					}

					line++;
					if(line%1000000==0) {
						System.out.println("Already processed "+line+" lines");
					}
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

	private static String getGene(String string) {
		int index = string.indexOf("_");
		if(index>0) {
			return string.substring(0, index);
		}
		return string;
		/*
		String firstPart = string.split("_")[0];
		String[] parts = string.split("-");
		String lastPart = parts[parts.length-1];
		String ret = firstPart+"_"+lastPart;
		return ret;
		*/
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

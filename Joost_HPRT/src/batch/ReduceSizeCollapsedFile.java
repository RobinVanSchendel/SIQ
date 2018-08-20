package batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

public class ReduceSizeCollapsedFile {

	public static void main(String[] args) throws FileNotFoundException {
		
		int minimumCounts = 5;
		int[] startPositions = null;//{81,216};
		int[] endPositions = null;//{326,552};
		//int[] startPositions = {81,216};
		//int[] endPositions = {326,552};
		
		//replace fileNames
		File names= new File("HPRT_FASTQ_output_filenames.txt");
		HashMap<String, String> namesMap = ReadInFileNamesChanges(names);
		Vector<String> notFoundNames = new Vector<String>();
		HashMap<String, String> hm = new HashMap<String, String>();
		HashMap<String, Integer> hmCounts = new HashMap<String, Integer>();
		
		boolean replaceNames = true;
		
		//File f = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\Joost\\01292018_projectnumber103311\\HPRT_FASTQ_output_WITHASSEMBLY_collapse.txt");
		//File f = new File("E:\\Project_Joost_HPRT\\HPRT_FASTQ_output_collapse_on_key.txt");
		File f = new File("E:\\Project_Joost_HPRT\\20180228_HPRT_FASTQ_output_collapse_search_mmHPRT_and_plasmid.txt");
		File output = new File(f.getAbsolutePath()+"_reduced.txt");
		Scanner s  = new Scanner(f);
		boolean first = true;
		boolean reduceByKey = true; //TURN THIS OFF WHEN YOU WANT TO SEPARATE BY POSITION
		
		PrintWriter pw = new PrintWriter(output);
		
		int matchStartColumn = -1;
		int matchEndColumn = -1;
		int fileColumn = -1;
		int leftFlankColumn = -1;
		int delColumn = -1;
		int rightFlankColumn = -1;
		int countColumn = -1;
		int insertColumn = -1;
		int lines = 0;
		int totalLines = 0;
		
		
		while(s.hasNextLine()) {
			String line = s.nextLine();
			String[] parts = line.split("\t");
			if(first) {
				int index = 0;
				for(String part: parts) {
					if(part.equals("matchStart")) {
						matchStartColumn = index;
					}
					else if(part.equals("matchEnd")) {
						matchEndColumn = index;
					}
					else if(part.equals("File")) {
						fileColumn = index;
					}
					else if(part.equals("delStart")) {
						leftFlankColumn = index;
					}
					else if(part.equals("del")) {
						delColumn = index;
					}
					else if(part.equals("delEnd")) {
						rightFlankColumn = index;
					}
					else if(part.equals("insertion")) {
						insertColumn = index;
					}
					else if(part.equals("#Counts")) {
						countColumn = index;
					}
					index++;
				}
				pw.println(line);
				first = false;
			}
			else {
				totalLines++;
				try {
					if(Integer.parseInt(parts[0])>=minimumCounts) {
						if(replaceNames) {
							String replacement = parts[fileColumn];
							if(namesMap.containsKey(replacement)) {
								parts[fileColumn] = namesMap.get(replacement);
							}
							else {
								if(!notFoundNames.contains(parts[fileColumn])) {
									notFoundNames.add(parts[fileColumn]);
								}
							}
						}
						if(reduceByKey) {
							String key = extractKey(parts, fileColumn, leftFlankColumn, delColumn, rightFlankColumn, insertColumn);
							int count = Integer.parseInt(parts[countColumn]);
							if(hm.containsKey(key)) {
								hmCounts.put(key, hmCounts.get(key)+count);
							}
							else {
								hm.put(key, String.join("\t", parts));
								hmCounts.put(key, count);
							}
						}
						else {
							if(startPositions!= null && endPositions!= null) {
								if(containsNumber(parts[matchStartColumn], startPositions) &&
										containsNumber(parts[matchEndColumn], endPositions)) {
									pw.println(String.join("\t", parts));
									lines++;
								}
							}
							else{
								pw.println(String.join("\t", parts));
								lines++;
							}
						}
					}
				}
				catch(Exception e) {
					System.out.println("whatever...");
					//e.printStackTrace();
				}
				if(totalLines %100000 == 0) {
					System.out.println("Already processed "+totalLines+" lines");
				}
			}
		}
		//now print the hash if needed
		if(reduceByKey) {
			for(String key: hm.keySet()) {
				String line = hm.get(key);
				String[] parts = line.split("\t");
				parts[0] = hmCounts.get(key)+"";
				pw.println(String.join("\t", parts));
				lines++;
			}
		}
		s.close();
		pw.close();
		System.out.println("Total lines printed: "+lines+" out of "+totalLines);
		if(notFoundNames.size()>0) {
			System.out.println("Could not replace:");
			Collections.sort(notFoundNames);
			for(String missing: notFoundNames) {
				System.out.println(missing);
			}
		}
	}

	
	private static String extractKey(String[] parts, int fileColumn, int leftFlankColumn, int delColumn,
			int rightFlankColumn, int insertColumn) {
		String s = "_";
		return parts[fileColumn]+s+parts[leftFlankColumn]+s+parts[delColumn]+s+parts[rightFlankColumn]+s+parts[insertColumn];
	}


	private static boolean containsNumber(String string, int[] positions) {
		int number = Integer.parseInt(string);
		
		for(int i: positions) {
			if(i==number) {
				return true;
			}
		}
		return false;
	}
	private static HashMap<String, String> ReadInFileNamesChanges(File names) throws FileNotFoundException {
		HashMap<String, String> hm = new HashMap<String, String>();
		Scanner s = new Scanner(names);
		while(s.hasNextLine()) {
			String line = s.nextLine();
			String[] parts = line.split("\t");
			if(parts.length>1) {
				hm.put(parts[0], parts[1]);
			}
		}
		s.close();
		return hm;
	}

}

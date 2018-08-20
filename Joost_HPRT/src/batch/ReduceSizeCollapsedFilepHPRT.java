package batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

import utils.SetAndPosition;
import utils.UtilsReduce;

public class ReduceSizeCollapsedFilepHPRT {

	public static void main(String[] args) throws FileNotFoundException {
		
		String setNameFile = "HPRT_sets.txt";
		String fileNamesFile = "HPRT_Samples.txt";
		
		final int minimumCounts = 2;
		final int maxPosDeviation = 15;
		final int minSizeInward = 30;
		
		
		File file = new File("E:/Project_Hartwig/20180815_HPRT_all_1_3.txt");
		HashMap<String, Integer> index = UtilsReduce.getHeader(file);
		System.out.println("retrieved header");
		
		System.out.println("retrieving column File");
		
		ArrayList<String> fileNames = UtilsReduce.ReadInFileNames(new File(fileNamesFile));
		System.out.println(String.join("\n",fileNames));
		HashMap<String, String> namesMap = UtilsReduce.assignReplacementNamesTillLast(fileNames);
		
		System.out.println("reading in ReadInFileNamesChanges");
		System.out.println("read ReadInFileNamesChanges");
		System.out.println("reading in countPositions");
		ArrayList<SetAndPosition> pos = UtilsReduce.readInFilePositions(new File(setNameFile));
		ArrayList<SetAndPosition> posInward = UtilsReduce.createInwardPositions(pos, minSizeInward);
		System.out.println("read countPositions");
		//System.exit(0);
		
		System.out.println("replacements:");
		for(String key: namesMap.keySet()) {
			System.out.println(key+"\t"+namesMap.get(key));
		}
		HashMap<String, Integer> removeColumns = new HashMap<String, Integer>();
		removeColumns.put("Raw",-1);
		removeColumns.put("Schematic", -1);
		removeColumns.put("Name", -1);
		removeColumns.put("CutType", -1);
		//fill the removecolumns
		for(String key: removeColumns.keySet()) {
			if(index.containsKey(key)) {
				removeColumns.put(key,index.get(key));
			}
			else {
				System.err.println("Cannot remove column "+key+" as it does not exist");
				System.exit(0);
			}
		}
		
		
		Vector<String> notFoundNames = new Vector<String>();
		HashMap<String, String> hm = new HashMap<String, String>();
		HashMap<String, Integer> hmCounts = new HashMap<String, Integer>();
		HashMap<String, Integer> hmMaxCounts = new HashMap<String, Integer>();
		
		boolean replaceNames = true;
		boolean addKey = true;
		
		//File f = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\Joost\\01292018_projectnumber103311\\HPRT_FASTQ_output_WITHASSEMBLY_collapse.txt");
		//File f = new File("E:\\Project_Joost_HPRT\\HPRT_FASTQ_output_collapse_on_key.txt");
		File output = new File(file.getAbsolutePath()+"_reduced.txt");
		Scanner s  = new Scanner(file);
		boolean first = true;
		boolean reduceByKey = true; //TURN THIS OFF WHEN YOU WANT TO SEPARATE BY POSITION
		
		PrintWriter pw = new PrintWriter(output);
		
		int lines = 0;
		int totalLines = 0;
		
		
		while(s.hasNextLine()) {
			String line = s.nextLine();
			String[] parts = line.split("\t");
			if(first) {
				if(addKey) {
					line = "Key\t"+line;
				}
				pw.println(line);
				first = false;
			}
			else if(!parts[index.get("countEvents")].equals("countEvents")) {
				totalLines++;
				try {
					if(!removeColumns.isEmpty()) {
						for(String key: removeColumns.keySet()) {
							if(removeColumns.get(key)>-1) {
								parts[removeColumns.get(key)] = "";
							}
						}
					}
					if(totalLines %10000 == 0) {
						System.out.println("Already processed "+totalLines+" lines");
					}
					if(!UtilsReduce.positionIsOk(parts,index, pos, "matchStart", "matchEnd", "File", maxPosDeviation, null)) {
						//System.out.println(String.join("\t", parts));
						continue;
					}
					//the sequence should at least continue a few bases inside the expected location
					if(!UtilsReduce.positionIsOkInward(parts,index, posInward, "delStart", "delEnd", "File",0, null)) {
						//System.out.println("not ok "+String.join("\t", parts));
						/*
						for(SetAndPosition sp: posInward) {
							if(parts[index.get("File")].equals(sp.getName())) {
								System.out.println(sp);
							}
						}
						*/
						continue;
					}
					//for reduceByKey I have to check later if the counts are ok
					if(reduceByKey || Integer.parseInt(parts[index.get("countEvents")])>=minimumCounts) {
						if(replaceNames) {
							String replacement = parts[index.get("File")];
							if(namesMap.containsKey(replacement)) {
								parts[index.get("File")] = namesMap.get(replacement);
							}
							else {
								if(!notFoundNames.contains(parts[index.get("File")])) {
									notFoundNames.add(parts[index.get("File")]);
								}
							}
						}
						if(reduceByKey) {
							//Key = 
							String key = UtilsReduce.extractKey(parts, index.get("File"), index.get("Subject"), index.get("delStart"), index.get("delEnd"), index.get("insertion"));
							int count = Integer.parseInt(parts[index.get("countEvents")]);
							if(hm.containsKey(key)) {
								hmCounts.put(key, hmCounts.get(key)+count);
								//but also check if that count is the highest
								if(hmMaxCounts.get(key)<count) {
									//replace
									hm.put(key, String.join("\t", parts));
									//update
									hmMaxCounts.put(key,count);
								}
							}
							else {
								hm.put(key, String.join("\t", parts));
								hmCounts.put(key, count);
								hmMaxCounts.put(key,count);
							}
						}
						/*
						else {
							if(startPositions!= null && endPositions!= null) {
								if(containsNumber(parts[index.get("matchStart")], startPositions) &&
										containsNumber(parts[index.get("matchEnd")], endPositions)) {
									pw.println(String.join("\t", parts));
									lines++;
								}
							}
							else{
								pw.println(String.join("\t", parts));
								lines++;
							}
						}
						*/
					}
				}
				catch(Exception e) {
					System.out.println("whatever...");
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
		//now print the hash if needed
		if(reduceByKey) {
			for(String key: hm.keySet()) {
				String line = hm.get(key);
				String[] parts = line.split("\t");
				//do we need to print this one?
				if(hmCounts.get(key)>minimumCounts) {
					parts[0] = hmCounts.get(key)+"";
					if(addKey) {
						pw.print(key+"\t");
					}
					pw.println(String.join("\t", parts));
				}
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
}

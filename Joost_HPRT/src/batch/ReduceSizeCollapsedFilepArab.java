package batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

import utils.CompareSequence;
import utils.SetAndPosition;
import utils.UtilsReduce;

public class ReduceSizeCollapsedFilepArab {

	public static void main(String[] args) throws FileNotFoundException {
		
		String setNameFile = "arab_sets.txt";
		
		int minimumCounts = 2;
		int maxPosDeviation = 15;
		final int minSizeInward = 30;
		
		File file = new File("E:/Project_Hartwig/20180731_arab_all.txt");
		HashMap<String, Integer> index = UtilsReduce.getHeader(file);
		
		ArrayList<String> fileNames = UtilsReduce.retrieveColumn(file, "File", true);
		System.out.println(String.join("\n",fileNames));
		HashMap<String, String> namesMap = UtilsReduce.assignReplacementNames(fileNames);
		
		ArrayList<SetAndPosition> pos = UtilsReduce.readInFilePositions(new File(setNameFile));
		ArrayList<SetAndPosition> posInward = UtilsReduce.createInwardPositions(pos, minSizeInward);
		//System.exit(0);
		
		System.out.println("replacements:");
		for(String key: namesMap.keySet()) {
			System.out.println(key+"\t"+namesMap.get(key));
		}
		HashMap<String, Integer> removeColumns = new HashMap<String, Integer>();
		//removeColumns.put("Raw",-1);
		removeColumns.put("Schematic", -1);
		//removeColumns.put("Name", -1);
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
		int totalIncorrectPos = 0;
		int totalIncorrectPosInward = 0;
		int totalCorrect = 0;
		boolean printFirstError = true;
		
		
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
				if(totalLines %10000 == 0) {
					System.out.println("Already processed "+totalLines+" lines");
				}
				try {
					if(!removeColumns.isEmpty()) {
						for(String key: removeColumns.keySet()) {
							if(removeColumns.get(key)>-1) {
								parts[removeColumns.get(key)] = "";
							}
						}
					}
					int count = Integer.parseInt(parts[index.get("countEvents")]);
					if(!UtilsReduce.positionIsOk(parts,index, pos, "matchStart", "matchEnd", "File", maxPosDeviation, null)) {
						/*
						if(count>10000) {
							if(printFirstError) {
								System.out.println("Counts\t"+CompareSequence.getOneLineHeader());
								printFirstError = false;
							}
							System.out.println(String.join("\t", parts));
						}
						*/
						totalIncorrectPos+=count;
						continue;
					}
					if(!UtilsReduce.positionIsOkInward(parts,index, posInward, "delStart", "delEnd", "File",0, null)) {
						//System.out.println("not ok "+String.join("\t", parts));
						/*
						for(SetAndPosition sp: posInward) {
							if(parts[index.get("File")].equals(sp.getName())) {
								System.out.println(sp);
							}
						}
						*/
						totalIncorrectPosInward+=count;
						continue;
					}
					totalCorrect+=count;
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
							if(hm.containsKey(key)) {
								hmCounts.put(key, hmCounts.get(key)+count);
							}
							else {
								hm.put(key, String.join("\t", parts));
								hmCounts.put(key, count);
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
		System.out.println("Total lines printed: "+lines+" out of "+totalLines+" to "+output.getAbsolutePath());
		System.out.println("Total correct "+totalCorrect+" wrong pos: "+totalIncorrectPos+ " wrong pos inward "+totalIncorrectPosInward);
		if(notFoundNames.size()>0) {
			System.out.println("Could not replace:");
			Collections.sort(notFoundNames);
			for(String missing: notFoundNames) {
				System.out.println(missing);
			}
		}
	}
}

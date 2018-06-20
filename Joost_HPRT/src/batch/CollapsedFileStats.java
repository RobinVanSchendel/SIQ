package batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

public class CollapsedFileStats {

	public static void main(String[] args) throws FileNotFoundException {
		
		//int[] startPositions = null;//{81,216};
		//int[] endPositions = null;//{326,552};
		int[] startPositions = {81,216};
		int[] endPositions = {326,552};
		
		File f = new File("HPRT_FASTQ_output_collapse.txt");
		Scanner s  = new Scanner(f);
		boolean first = true;
		HashMap<String, Integer> hm = new HashMap<String, Integer>();
		
		int matchStartColumn = -1;
		int matchEndColumn = -1;
		int fileColumn = -1;
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
					if(part.equals("matchEnd")) {
						matchEndColumn = index;
					}
					if(part.equals("File")){
						fileColumn = index;
					}
					index++;
				}
				first = false;
			}
			else {
				totalLines++;
				String file = parts[fileColumn];
				int count = Integer.parseInt(parts[0]);
				if(hm.containsKey(file)) {
					hm.put(file, hm.get(file)+count);
				}
				else {
					hm.put(file,count);
				}
				if(startPositions!= null && endPositions!= null) {
						if(containsNumber(parts[matchStartColumn], startPositions) &&
								containsNumber(parts[matchEndColumn], endPositions)) {
							String okKey = file+"_primersOk";
							if(hm.containsKey(okKey)) {
								hm.put(okKey, hm.get(okKey)+count);
							}
							else {
								hm.put(okKey,count);
							}
						}
				}
				if(totalLines %100000 == 0) {
					System.out.println("processed "+totalLines+" lines");
				}
			}
		}
		s.close();
		for(String key: hm.keySet()){
			System.out.println(key+"\t"+hm.get(key));
		}
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

}

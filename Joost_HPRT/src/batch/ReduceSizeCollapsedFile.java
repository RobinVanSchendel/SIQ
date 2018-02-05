package batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

public class ReduceSizeCollapsedFile {

	public static void main(String[] args) throws FileNotFoundException {
		
		int minimumCounts = 5;
		int[] startPositions = null;//{81,216};
		int[] endPositions = null;//{326,552};
		//int[] startPositions = {81,216};
		//int[] endPositions = {326,552};
		
		File f = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\Joost\\01292018_projectnumber103311\\HPRT_FASTQ_output_WITHASSEMBLY_collapse.txt");
		File output = new File(f.getAbsolutePath()+"_reduced.txt");
		Scanner s  = new Scanner(f);
		boolean first = true;
		PrintWriter pw = new PrintWriter(output);
		
		int matchStartColumn = -1;
		int matchEndColumn = -1;
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
					index++;
				}
				pw.println(line);
				first = false;
			}
			else {
				totalLines++;				
				if(Integer.parseInt(parts[0])>=minimumCounts) {
					if(startPositions!= null && endPositions!= null) {
						if(containsNumber(parts[matchStartColumn], startPositions) &&
								containsNumber(parts[matchEndColumn], endPositions)) {
							pw.println(line);
							lines++;
						}
					}
					else{
						pw.println(line);
						lines++;
					}
				}
				if(totalLines %100000 == 0) {
					System.out.println("Already processed "+totalLines+" lines");
				}
			}
		}
		s.close();
		pw.close();
		System.out.println("Total lines printed: "+lines+" out of "+totalLines);
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

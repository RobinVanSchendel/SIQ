package dnaanalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class GetSNVMutations {

	public static void main(String[] args) throws FileNotFoundException {
		Scanner s = new Scanner(new File("test.txt"));
		boolean first = true;
		while(s.hasNextLine()) {
			String line = s.nextLine();
			if(first) {
				System.out.println(line+"\tSNV");
				first = false;
			}
			//System.out.println(line);
			String[] parts = line.split("\t");
			if(parts.length>=3 && parts[1].length()==1 && parts[2].length()==1) {
				String mutation = getMutation(parts[1], parts[2]);
				System.out.println(line+"\t"+mutation);
			}
		}
		s.close();
	}
	public static String getMutation(String ref, String alt){
		if((ref.equals("A") && alt.equals("G")) || (ref.equals("T") && alt.equals("C"))){
			return "AT->GC";
		}
		else if((ref.equals("G") && alt.equals("A")) || (ref.equals("C") && alt.equals("T"))){
			return "GC->AT";
		}
		else if((ref.equals("A") && alt.equals("C")) || (ref.equals("T") && alt.equals("G"))){
			return "AT->CG";
		}
		else if((ref.equals("G") && alt.equals("T")) || (ref.equals("C") && alt.equals("A"))){
			return "GC->TA";
		}
		else if((ref.equals("A") && alt.equals("T")) || (ref.equals("T") && alt.equals("A"))){
			return "AT->TA";
		}
		else if((ref.equals("G") && alt.equals("C")) || (ref.equals("C") && alt.equals("G"))){
			return "GC->CG";
		}
		return ref+"->"+alt;
	}
}

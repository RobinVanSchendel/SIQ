package dnaanalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class MainReverseComplementTable {
	public static void main(String[] args) {
		File f = new File("seq.txt");
		Scanner s;
		try {
			s = new Scanner(f);
			while(s.hasNextLine()) {
				String line = s.nextLine();
				System.out.println(line+"\t"+Utils.reverseComplement(line));
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

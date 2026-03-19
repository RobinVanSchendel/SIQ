package main;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class TriplePrimerCombinations {
	private ArrayList<TriplePrimer> al = new ArrayList<TriplePrimer>();
	public TriplePrimerCombinations(File file) {
		try {
			Scanner s = new Scanner(file);
			while(s.hasNextLine()) {
				String line = s.nextLine();
				String[] parts = line.split("\t");
				String name = parts[0];
				String R1Barcode  = parts[1];
				String R1Primer = parts[2];
				String R2Barcode  = parts[3];
				String R2Primer = parts[4];
				TriplePrimer tp = new TriplePrimer(name, R1Barcode, R1Primer, R2Barcode, R2Primer);
				al.add(tp);
			}
			s.close();
		}
		catch(Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public ArrayList<TriplePrimer> getAll(){
		return al;
	}
}

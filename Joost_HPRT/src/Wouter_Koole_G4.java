import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.JTextField;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojavax.bio.seq.RichSequence;

import utils.CompareSequence;

public class Wouter_Koole_G4 {

	public static void main(String[] args) {
		RichSequence subject = null;
		try {
				String dna = "gaacaagcttattaaccttggctgtacaatctgggaaagttccgacttcctgccatgttccacgagaagtatccatcttctcaacaatgtagtgaagaacatcagttcctccgttatcagttggaggcttccagttcaatgTTATTACCCTGGGCGCCGCATGCGTCGACCCgggaggcctgatttcaCCCCCCCCTCCCCCACCCCCTCCCtcgcAATTCATATGATCCCtacatccttccttatggatctcgtcaatcttgagtggtccttctggagttcctggtacatcaagaacagtaacattgcactgagcagtatcttttccatgctcattttcaacaatgattttgtaaactccagtatctccacgaacagcagagaagatgtgaattgctgaggatgttggtgtgttcgtaacatcagctcttgctcctgtatcgattgttgcatcgttggccttccatttagcaactggggctggctctccttcgaatgcgatatcgagcttgatgggtgttccagccttgatacggagatccaaaagtccggcgaggttaagttttggagccattcttcttggtttggcaacaacatttcctgttggatcagatggttttcctggcccagccttattgacagccttcacacggaactgataagtctctcctggagtcaaattatcagcagttgcctttgttgtctttccatcaacacgtgcacactcaacccagtctccaaacttgtccttcttctcaacgatgtaagcatcaattggtgcaccaccgtcgtttgctggtggcttccattcaagatcaacatgatccttatcccaat";
				subject = RichSequence.Tools.createRichSequence("G4e", DNATools.createDNA(dna));
		} catch (BioException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File in = new File("input.txt");
		
		Scanner input = null;
		try {
			input = new Scanner(in);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int index = 0;
		String left = "GCATGCGTCGACCCgggaggcctgatttca";
		String right = "CCCCCCCCTCCCCCACCCCCTCCCtcgcAATT";
		while(input.hasNextLine()){
			String line = input.nextLine();
			String[] parts = line.split("\t");
			String dna = parts[2]+parts[3]+parts[4];
			if(!dna.contains("flank")){
				try {
					//System.out.println(dna);
					RichSequence s = RichSequence.Tools.createRichSequence(""+index, DNATools.createDNA(dna));
					CompareSequence cs = new CompareSequence(subject, null, s, null, left, right, null, null);
					cs.setMinimumSizeWithoutLeftRight(10);
					cs.determineFlankPositions();
					System.out.println(cs.toStringOneLine());
				} catch (IllegalSymbolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			index++;
		}
		
	}

}

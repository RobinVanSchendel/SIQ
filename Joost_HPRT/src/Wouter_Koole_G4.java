import gui.GUI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.biojava.bio.*;
import org.biojava.bio.program.abi.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequence.IOTools;

import utils.CompareSequence;
import utils.Utils;

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
		while(input.hasNextLine()){
			String line = input.nextLine();
			String[] parts = line.split("\t");
			String dna = parts[2]+parts[3]+parts[4];
			if(!dna.contains("flank")){
				try {
					//System.out.println(dna);
					RichSequence s = RichSequence.Tools.createRichSequence(""+index, DNATools.createDNA(dna));
					CompareSequence cs = new CompareSequence(subject, null, s, null, "", "", null, null);
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

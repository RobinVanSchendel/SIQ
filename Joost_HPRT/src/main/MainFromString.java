package main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.biojavax.bio.seq.RichSequenceIterator;

import utils.CompareSequence;

public class MainFromString {

	public static void main(String[] args) {
		File subjectFile = new File("E:\\Project_Hartwig\\arab.fa");
		String currentStrain = "";
		//System.out.println("id\tcurrentStrain\ttype\tnumberOfOccurences\tleftFlank\tdeletion\trightFlank\tinsert\tdeletionSize\tinsertSize\tnetChange\thomology\thomologyLength");
		BufferedReader br;
		RichSequence subject = null;
		try {
			br = new BufferedReader(new FileReader(subjectFile));
			RichSequenceIterator si2 = IOTools.readFastaDNA(br, null);
			subject = si2.nextRichSequence();
		} catch (FileNotFoundException | NoSuchElementException | BioException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.out.println(CompareSequence.getOneLineHeader());
		String name = "test";
		String raw = "ttgctcgtatttcttcaggaactatctacagctcctcactctttccaaatcgcgcaccgcccggaagaattttgctgttgaactacaaaattccgggtctacaaacaccggaattctgtccaaggtaaaaaacagcaaacacttgtaacacatctttattcaaccaagtaaacctaagaactg"; 
				
		
		//CompareSequence cs = new CompareSequence(subject, null, raw, null, "", "", null, currentStrain, true, name);
		//cs.determineFlankPositions();
		//System.out.println(cs.toStringOneLine());

	}

}

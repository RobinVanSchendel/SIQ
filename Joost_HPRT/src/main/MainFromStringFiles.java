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

public class MainFromStringFiles {

	public static void main(String[] args) {
		File f = new File("C:\\GIT\\test_robin\\TM_allele\\transposons192.txt");
		File subjectFile = new File("C:\\Users\\rvanschendel\\Documents\\Project_Lig3\\Tc1_transposon_st192\\unc-22_st192_Tc.fa.txt");
		Scanner s = null;
		try{
			s = new Scanner(f);
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
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
		while(s.hasNextLine()){
			String line = s.nextLine();
			String[] parts = line.split("\t");
			if(line.indexOf("unc-22")>=0){
				currentStrain = line.trim().replace("\t", " ");
			}
			if(parts.length>3 && parts[0]!= null && parts[0].length()>0){
				//modify the ends accordingly
				int numberOfOccurences = Integer.parseInt(parts[1].trim());
				String leftFlank = parts[2].trim();
				String rightFlank = parts[4].trim();
				String query = leftFlank+parts[3]+rightFlank;
				CompareSequence cs = new CompareSequence(subject, null, query, null, "", "", null, currentStrain, true, parts[0]);
				cs.determineFlankPositions();
				for(int i = 0;i<numberOfOccurences;i++){
					System.out.println(cs.toStringOneLine());
				}
			}
		}

	}

}

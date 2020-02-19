package main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.jcvi.jillion.trace.chromat.Chromatogram;
import org.jcvi.jillion.trace.chromat.ChromatogramFactory;

import utils.CompareSequence;
import utils.KMERLocation;
import utils.Subject;

public class TestSingleSequence {

	public static void main(String[] args) {
		RichSequence seq = null;
		System.out.println(CompareSequence.getOneLineHeader());
		BufferedReader is = null;
		
		//FILL IN
		
		String refFile = "E:\\temp\\eGFP.txt";
		String seqS = "gacgtaaacggccacaagttcagcgtgtccggcgagggcgagggcgatgccacctacggcaagctgaccctgaagttcatctgcaccaccggcaagctgcccgtgccctggcccaccctcgtgaccaccctgaccacatgaagcagcacgacttcttcaagtcc";
		String left = "GAAGTTCATCTGCACCACCG";
		String right = "CTACGGCGTGCAGTGCTTCA";
		
		File ref = new File(refFile);
		try {
			//is = new BufferedReader(new FileReader("Z:\\Joost\\Files\\Manuscripts\\Schimmel_etal_2016\\Robin\\HPRT-FASTA-CR2.txt"));
			//is = new BufferedReader(new FileReader("Z:\\Joost\\Files\\Manuscripts\\Schimmel_etal_2016\\Robin\\HPRT-FASTA-CR1.txt"));
			//is = new BufferedReader(new FileReader("Z:\\Evelyne\\JavaSoftware\\XF1290.fa.txt"));
			//is = new BufferedReader(new FileReader("Z:\\Evelyne\\DNA\\Revertants sequencing\\XF1423_extended.fa.txt"));
			//is = new BufferedReader(new FileReader("Z:\\Tim\\G23 insertion\\XF1426.fa.txt"));
			//is = new BufferedReader(new FileReader("C:\\Users\\rvanschendel\\Documents\\Project_Primase\\polq-1_reversion\\XF1280_whole_unc-22_100bp_zone_for_polq.fa"));
			//is = new BufferedReader(new FileReader("E:\\Project_Hartwig\\HPRT-FASTA-CR1.txt"));
			is = new BufferedReader(new FileReader(refFile));
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(0);
		}
		//get a SequenceDB of all sequences in the file
		RichSequenceIterator si = IOTools.readFastaDNA(is, null);
		RichSequence hprtSeq = null;
		while(si.hasNext()){
			try {
				hprtSeq = si.nextRichSequence();
				//IOTools.writeFasta(System.out, hprtSeq, null);
			} catch (NoSuchElementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BioException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			KMERLocation kmerl = new KMERLocation(hprtSeq.seqString());
			Subject subjectObject = new Subject(hprtSeq, left, right);
			
			CompareSequence kmerWith = new CompareSequence(subjectObject,seqS,null, null, true, "", kmerl);
			//CompareSequence(RichSequence subject, String query, QualitySequence quals, String left, String right, String pamSite, String dir, boolean checkReverse, String queryName, KMERLocation kmerl) {
			//s.setAdditionalSearchString(additional);
			//kmerWith.setAndDetermineCorrectRange(0.05);
			//kmerWith.maskSequenceToHighQualityRemove();
			kmerWith.determineFlankPositions(false);
			String outputKMER = kmerWith.toStringOneLine();
			System.out.println(outputKMER);
			
			/*
			
			kmerless1 = new CompareSequence(hprtSeq, seq.seqString(),chromo.getQualitySequence(), left,right, null, null, true, seq.getName(), null);
			mkerless1.setAndDetermineCorrectRange(0.05);
			//s.setAdditionalSearchString(additional);
			//s.maskSequenceToHighQuality(left, right);
			kmerless1.maskSequenceToHighQualityRemove();
			kmerless1.determineFlankPositions();
			outputKMERless = kmerless1.toStringOneLine();
			System.out.println(outputKMERless);
			
			kmerWith = new CompareSequence(hprtSeq, seq.seqString(),chromo.getQualitySequence(), left,right, null, null, true, seq.getName(), kmerl);
			kmerWith.setAndDetermineCorrectRange(0.05);
			//s.setAdditionalSearchString(additional);
			//s.maskSequenceToHighQuality(left, right);
			kmerWith.maskSequenceToHighQualityRemove();
			kmerWith.determineFlankPositions();
			outputKMER = kmerWith.toStringOneLine();
			System.out.println(outputKMER);
			
			if(kmerWith.getRemarks().length()==0 && kmerless1.getRemarks().length()==0 && !outputKMERless.equals(outputKMER)) {
				System.err.println("KMER AND KMERLESS ARE NOT THE SAME (QUALTIY MASKED)");
				System.err.println(outputKMERless);
				System.err.println(outputKMER);
			}
			*/
	}
}

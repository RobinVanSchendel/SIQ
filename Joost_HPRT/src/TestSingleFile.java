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
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.jcvi.jillion.trace.chromat.Chromatogram;
import org.jcvi.jillion.trace.chromat.ChromatogramFactory;

import utils.CompareSequence;

public class TestSingleFile {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		RichSequence seq = null;
		Chromatogram chromo = null;
		Vector<Sequence> additional = new Vector<Sequence>();
		try {
			//File f = new File("C:\\Users\\rvanschendel\\Documents\\Project_Lig3\\Tc1_transposon_If230\\1039717\\rde_3_20_2527773-1039717.ab1");
			//File f = new File("C:\\Users\\rvanschendel\\Documents\\Project_Primase\\100bp_insertion_zone\\Sequencing revertants\\1039028\\XF1289_78_2_2518043-1039028.ab1");
			//File f = new File("C:\\Users\\rvanschendel\\Documents\\Project_Primase\\G23_56bp_zone\\1040510\\XF1335_53_2538975-1040510.ab1");
			//File f = new File("Z:\\Joost\\Files\\Manuscripts\\Schimmel_etal_2016\\Footprints Cas9-N863A\\Sequence files\\Lig4\\Cas9_N863A_Lig4nest_16_2501047-1037841.ab1");
			//File f = new File("Z:/Joost/Files/Manuscripts/Schimmel_etal_2016/Footprints Cas9-WT/Sequence files/CR1/POLQ-Ku80_dko/A03__T2_CR1_2534295-1040173.ab1");
			//File f = new File("Z:\\Joost\\Files\\Manuscripts\\Schimmel_etal_2016\\Footprints Cas9-WT\\Sequence files\\CR2\\WT\\HPRT_CR2_121_2482959-1036715.ab1");
			//File f = new File("Z:\\Joost\\Files\\Manuscripts\\Schimmel_etal_2016\\Footprints Cas9-N863A\\Sequence files\\Ku80\\C02_2506297-1038176.ab1");
			//File f = new File("C:\\Users\\rvanschendel\\Documents\\Project_Primase\\mrt-1 mrt-2\\mrt-2 dog-1 XF1399 (100bp_insert)\\XF1399_12_2566156-1042317.ab1");
			//File f = new File("Z:\\Evelyne\\DNA\\Revertants sequencing\\XF1423\\1-5 test\\6_2579328-1043179.ab1");
			//File f = new File("Z:\\Tim\\G23 insertion\\XF1426\\XF1426_7.ab1");
			//File f = new File("C:\\Users\\rvanschendel\\Documents\\Project_Primase\\polq-1_reversion\\Revertants\\1044472\\XF1474_91_A4_1_2597736-1044472.ab1");
			File f = new File("Z:\\Robin\\Project_Primase\\UV_TMP_hus-1-vs-N2\\unc-22_sequencing\\1046088\\33_51_4_2621330-1046088.ab1");
			
			

			
			//search additional 'TDNA'
			//File add = new File("Z:\\Joost\\Files\\Manuscripts\\Schimmel_etal_2016\\Robin\\px458_HPRT.txt");
			BufferedReader is2 = null;
			
			//is2 = new BufferedReader(new FileReader(add));
			/*
			SequenceIterator si2 = IOTools.readFastaDNA(is2, null);
			while(si2.hasNext()){
				try {
					additional.add(si2.nextSequence());
				} catch (NoSuchElementException | BioException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			*/
						
			

			chromo = ChromatogramFactory.create(f);
			try {
				seq = RichSequence.Tools.createRichSequence(f.getName(), DNATools.createDNA(chromo.getNucleotideSequence().toString()));
			} catch (IllegalSymbolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String name = f.getName();
			//seq = new SimpleSequence(symbols, name, name, Annotation.EMPTY_ANNOTATION);
			//IOTools.writeFasta(System.out, seq,null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader is = null;
		try {
			//is = new BufferedReader(new FileReader("Z:\\Joost\\Files\\Manuscripts\\Schimmel_etal_2016\\Robin\\HPRT-FASTA-CR2.txt"));
			//is = new BufferedReader(new FileReader("Z:\\Joost\\Files\\Manuscripts\\Schimmel_etal_2016\\Robin\\HPRT-FASTA-CR1.txt"));
			//is = new BufferedReader(new FileReader("Z:\\Evelyne\\JavaSoftware\\XF1290.fa.txt"));
			//is = new BufferedReader(new FileReader("Z:\\Evelyne\\DNA\\Revertants sequencing\\XF1423_extended.fa.txt"));
			//is = new BufferedReader(new FileReader("Z:\\Tim\\G23 insertion\\XF1426.fa.txt"));
			//is = new BufferedReader(new FileReader("C:\\Users\\rvanschendel\\Documents\\Project_Primase\\polq-1_reversion\\XF1280_whole_unc-22_100bp_zone_for_polq.fa"));
			is = new BufferedReader(new FileReader("Z:\\Robin\\Project_Primase\\UV_TMP_hus-1-vs-N2\\unc-22_sequencing\\unc-22.fa"));
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
		System.out.println(CompareSequence.getOneLineHeader());
		//CR1
		//String left = "TTTGTTTTGTATACCTAATCATTATG";
		//String right = "CCGAGGATTTGGAAAAAGTGTTTA";
		//CR2
		//String left = "TGAAAGACTTGCTCGAGATGTCA";
		//String right = "TGAAGGAGATGGGAGGCCATCACATT";
		//CR1 overhang
		//String left = "GATTTGTTTTGTATACCTAAT";
		//String right = "TTATGGACAGGTTAGTAAGACCTCGAT";
		
		//Primase low
		//String left = "GCATGCGTCGACCCgggaggcctgatttca";
		//String right = "CCCCCCCCTCCCCCACCCCCTCCCtcgcAATT";
		//G4XF1426
		//String left = "ctaagagcatccaaattcttggctcgtgtgaatggctagcctgg";
		//String right = "CCCCCCCCCCCCCCCCCCCCCCCGACTGCTTGCGGA";
		String left = "";
		String right = "";
		
		CompareSequence s = new CompareSequence(hprtSeq, null, seq,chromo.getQualitySequence(), left, right, null, null);
		s.setAdditionalSearchString(additional);
		s.determineFlankPositions();
		System.out.println(s.toStringOneLine());
		
		s = new CompareSequence(hprtSeq, null, seq,chromo.getQualitySequence(), left,right, null, null);
		s.setAndDetermineCorrectRange(0.05);
		s.setAdditionalSearchString(additional);
		//s.maskSequenceToHighQuality(left, right);
		s.maskSequenceToHighQualityRemove();
		s.determineFlankPositions();
		System.out.println(s.toStringOneLine());
	}

}

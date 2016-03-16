import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.biojava.bio.*;
import org.biojava.bio.program.abi.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.jcvi.jillion.trace.chromat.Chromatogram;
import org.jcvi.jillion.trace.chromat.ChromatogramFactory;

import Utils.CompareSequence;
import Utils.Utils;
import GUI.GUI;

public class Joost_HPRT {

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
		Sequence seq = null;
		try {
			File f = new File("C:\\Users\\rvanschendel\\Documents\\Project_Joost\\1036107\\HPRT_RF1_2_2471688-1036107.ab1");
			Chromatogram chromo = ChromatogramFactory.create(f);
			ABITrace trace = new ABITrace(f);
			SymbolList symbols = trace.getSequence();
			String name = f.getName();
			seq = new SimpleSequence(symbols, name, name, Annotation.EMPTY_ANNOTATION);
			//IOTools.writeFasta(System.out, seq,null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new GUI();
		/*
		File hprt = new File("HPRT.fa");
		BufferedReader is = null;
		try {
			is = new BufferedReader(new FileReader("HPRT.fa"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//get a SequenceDB of all sequences in the file
		SequenceIterator si = IOTools.readFastaDNA(is, null);
		Sequence hprtSeq = null;
		while(si.hasNext()){
			try {
				hprtSeq = si.nextSequence();
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
		//CompareSequence s = new CompareSequence(hprtSeq, seq);
		*/
	}

}

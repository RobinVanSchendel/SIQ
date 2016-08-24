package hprt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.jcvi.jillion.core.qual.QualitySequence;
import org.jcvi.jillion.core.residue.nt.NucleotideSequence;
import org.jcvi.jillion.trace.chromat.Chromatogram;
import org.jcvi.jillion.trace.chromat.ChromatogramFactory;

import utils.CompareSequence;

public class SequenceController {
	public void readFiles(String dir, String subjectFile, String leftFlank, String rightFlank, String type){
		BufferedReader is = null;
		Sequence subject = null;
		try {
			is = new BufferedReader(new FileReader(subjectFile));
			SequenceIterator si = IOTools.readFastaDNA(is, null);
			subject = si.nextSequence();
		} catch (FileNotFoundException | NoSuchElementException | BioException e1) {
			e1.printStackTrace();
		}
		
		File d = new File(dir);
		System.out.println(CompareSequence.getOneLineHeader());
		for(File cellType: d.listFiles()){
			if(cellType.isDirectory()){
				for(File seqs: cellType.listFiles()){
					try {
						Chromatogram chromo = ChromatogramFactory.create(seqs);
						NucleotideSequence seq = chromo.getNucleotideSequence();
						QualitySequence quals = chromo.getQualitySequence();
						Sequence query = null;
						try {
							query = DNATools.createDNASequence(seq.toString(), seqs.getName());
						} catch (IllegalSymbolException e) {
							e.printStackTrace();
						}
						CompareSequence cs = new CompareSequence(subject, null, query, quals, leftFlank, rightFlank, null, cellType.getName());
						cs.determineFlankPositions();
						System.out.println(cs.toStringOneLine());
						
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
}

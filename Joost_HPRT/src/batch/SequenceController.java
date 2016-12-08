package batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.jcvi.jillion.core.qual.QualitySequence;
import org.jcvi.jillion.core.residue.nt.NucleotideSequence;
import org.jcvi.jillion.trace.chromat.Chromatogram;
import org.jcvi.jillion.trace.chromat.ChromatogramFactory;

import utils.CompareSequence;
import utils.Utils;

public class SequenceController {
	private boolean printOnlyIsParts = false;
	private HashMap<String, String> colorMap;
	
	public void readFiles(String dir, String subjectFile, String leftFlank, String rightFlank, String type, File searchAdditional){
		BufferedReader is = null, is2 = null;
		RichSequence subject = null;
		Vector<Sequence> additional = new Vector<Sequence>();
		try {
			is = new BufferedReader(new FileReader(subjectFile));
			is2 = new BufferedReader(new FileReader(searchAdditional));
			RichSequenceIterator si = IOTools.readFastaDNA(is, null);
			SequenceIterator si2 = IOTools.readFastaDNA(is2, null);
			subject = si.nextRichSequence();
			
			while(si2.hasNext()){
				additional.add(si2.nextSequence());
			}
		} catch (FileNotFoundException | NoSuchElementException | BioException e1) {
			e1.printStackTrace();
		}
		
		File d = new File(dir);
		for(File cellType: d.listFiles()){
			if(cellType.isDirectory()){
				for(File seqs: cellType.listFiles()){
					if(seqs.isFile()){ 
						try {
							Chromatogram chromo = ChromatogramFactory.create(seqs);
							NucleotideSequence seq = chromo.getNucleotideSequence();
							QualitySequence quals = chromo.getQualitySequence();
							RichSequence query = null;
							try {
								//query = DNATools.createDNASequence(seq.toString(), seqs.getName());
								query = RichSequence.Tools.createRichSequence(seqs.getName(), DNATools.createDNA(seq.toString()));
							} catch (IllegalSymbolException e) {
								e.printStackTrace();
							}
							//mask
							CompareSequence cs = new CompareSequence(subject, null, query, quals, leftFlank, rightFlank, null, cellType.getName());
							cs.setAndDetermineCorrectRange(0.05);
							cs.maskSequenceToHighQualityRemove(leftFlank, rightFlank);
							cs.determineFlankPositions();
							cs.setAdditionalSearchString(additional);
							cs.setCutType(type);
							//only correctly found ones
							if(cs.getRemarks().length() == 0 || true){
								if(!printOnlyIsParts){
									System.out.println(type+"\t"+cs.toStringOneLine());
								}
								else{
									String[] ret = cs.printISParts(colorMap);
									if(ret != null){
										for(String s: ret){
											System.out.println(type+"\t"+seqs.getName()+"\t"+s);
										}
									}
								}
							}
							//no masking
							/*
							cs = new CompareSequence(subject, null, query, quals, leftFlank, rightFlank, null, cellType.getName());
							cs.determineFlankPositions();
							cs.setAdditionalSearchString(additional.seqString());
							//only correctly found ones
							if(cs.getRemarks().length() == 0){
								System.out.println(type+"\t"+cs.toStringOneLine());
							}
							*/
							
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					else{
						//System.err.println(seqs.getName()+" isDir "+seqs.isDirectory());
					}
				}
			}
		}
	}
	public ArrayList<CompareSequence> readFilesTryToMatch(String dir, String subjectFile, String leftFlank, String rightFlank, String type, File searchAdditional) {
		ArrayList<CompareSequence> al = new ArrayList<CompareSequence>();
		
		ArrayList<RichSequence> sequences = Utils.fillArrayListSequences(new File(subjectFile));
		
		BufferedReader is = null, is2 = null;
		Vector<Sequence> additional = new Vector<Sequence>();
		try {
			if(searchAdditional != null){
				is2 = new BufferedReader(new FileReader(searchAdditional));
				RichSequenceIterator si2 = RichSequence.IOTools.readFastaDNA(is2, null);
				while(si2.hasNext()){
					additional.add(si2.nextRichSequence());
				}
			}
		} catch (FileNotFoundException | NoSuchElementException | BioException e1) {
			e1.printStackTrace();
		}
		
		File d = new File(dir);
		for(File cellType: d.listFiles()){
			if(cellType.isDirectory()){
				for(File seqs: cellType.listFiles()){
					if(seqs.isFile() && seqs.getName().endsWith(".ab1")){
						RichSequence subject = Utils.matchNameSequence(sequences, seqs.getName());
						try {
							//System.out.println("accessing "+seqs.getName());
							Chromatogram chromo = ChromatogramFactory.create(seqs);
							NucleotideSequence seq = chromo.getNucleotideSequence();
							QualitySequence quals = chromo.getQualitySequence();
							RichSequence query = null;
							try {
								query = RichSequence.Tools.createRichSequence(seqs.getName(), DNATools.createDNA(seq.toString()));
							} catch (IllegalSymbolException e) {
								e.printStackTrace();
							}
							//mask
							if(subject == null){
								//System.out.println(seqs.getName()+" no subject found");
								continue;
							}
							CompareSequence cs = new CompareSequence(subject, null, query, quals, leftFlank, rightFlank, null, cellType.getName());
							cs.setAndDetermineCorrectRange(0.05);
							cs.maskSequenceToHighQualityRemove(leftFlank, rightFlank);
							cs.determineFlankPositions();
							cs.setAdditionalSearchString(additional);
							cs.setCutType(type);
							//only correctly found ones
							//and filter for events that are the same in ID and class
							if(cs.getRemarks().length() == 0 && cs.getType() != CompareSequence.Type.WT){
								//String id = cs.getIDPart()+"|"+cs.getUniqueClass();
								al.add(cs);
							}
							//no masking
							/*
							cs = new CompareSequence(subject, null, query, quals, leftFlank, rightFlank, null, cellType.getName());
							cs.determineFlankPositions();
							cs.setAdditionalSearchString(additional.seqString());
							//only correctly found ones
							if(cs.getRemarks().length() == 0){
								System.out.println(type+"\t"+cs.toStringOneLine());
							}
							*/
							
						} catch (IOException e1) {
							System.err.println(seqs.getName()+" has a problem");
							e1.printStackTrace();
						}
					}
				}
			}
		}
		return al;
	}
	public void setPrintOnlyISParts(){
		this.printOnlyIsParts = true;
	}
	public void setColorMap(HashMap<String, String> colorMap){
		this.colorMap = colorMap;
	}
}

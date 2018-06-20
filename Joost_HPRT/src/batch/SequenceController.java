package batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.jcvi.jillion.core.datastore.DataStoreException;
import org.jcvi.jillion.core.datastore.DataStoreProviderHint;
import org.jcvi.jillion.core.qual.PhredQuality;
import org.jcvi.jillion.core.qual.QualitySequence;
import org.jcvi.jillion.core.residue.nt.NucleotideSequence;
import org.jcvi.jillion.core.util.iter.StreamingIterator;
import org.jcvi.jillion.trace.chromat.Chromatogram;
import org.jcvi.jillion.trace.chromat.ChromatogramFactory;
import org.jcvi.jillion.trace.fastq.FastqDataStore;
import org.jcvi.jillion.trace.fastq.FastqFileDataStoreBuilder;
import org.jcvi.jillion.trace.fastq.FastqQualityCodec;
import org.jcvi.jillion.trace.fastq.FastqRecord;

import utils.CompareSequence;
import utils.MyOptions;
import utils.SequenceFileThread;
import utils.Utils;

public class SequenceController {
	private boolean printOnlyIsParts = false;
	private HashMap<String, String> colorMap;
	private File outputFile;
	private HashMap<String, Integer> countEvents = new HashMap<String, Integer>();
	private HashMap<String, String> actualEvents = new HashMap<String, String>();
	private boolean collapseEvents = false;
	private ArrayList<CompareSequence> sequences;
	private HashMap<String, ArrayList<CompareSequence>> hash = new HashMap<String, ArrayList<CompareSequence>>();
	private int[] startPositions;
	private int[] endPositions;
	private long minimalCount;
	private int nrCPUs = Runtime.getRuntime().availableProcessors();
	private boolean includeStartEnd = true;
	
	public void readFiles(String dir, String subjectFile, String leftFlank, String rightFlank, String type, File searchAdditional, PrintWriter writer){
		BufferedReader is = null, is2 = null;
		RichSequence subject = null;
		Vector<Sequence> additional = new Vector<Sequence>();
		HashMap<String, String> hmAdditional = new HashMap<String, String>();
		try {
			is = new BufferedReader(new FileReader(subjectFile));
			is2 = new BufferedReader(new FileReader(searchAdditional));
			RichSequenceIterator si = IOTools.readFastaDNA(is, null);
			SequenceIterator si2 = IOTools.readFastaDNA(is2, null);
			subject = si.nextRichSequence();
			
			while(si2.hasNext()){
				additional.add(si2.nextSequence());
			}
			for(Sequence s: additional) {
				hmAdditional.put(s.getName(), s.seqString().toString());
			}
		} catch (FileNotFoundException | NoSuchElementException | BioException e1) {
			e1.printStackTrace();
		}
		
		
		File d = new File(dir);
		Vector<File> ab1s = getAB1Files(d);
		System.out.println("Analyzing "+ab1s.size()+" ab1 files in "+d.getAbsolutePath());
		int i =0;
		int total = 0;
		//long start = System.nanoTime();
		for(File seqs: ab1s){
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
							CompareSequence cs = new CompareSequence(subject, null, query, quals, leftFlank, rightFlank, null, seqs.getParentFile().getName());
							cs.setAndDetermineCorrectRange(0.05);
							cs.maskSequenceToHighQualityRemove();
							cs.determineFlankPositions();
							cs.setAdditionalSearchString(hmAdditional);
							cs.setCutType(type);
							//only correctly found ones
							if(cs.getRemarks().length() == 0){
								i++;
								if(!printOnlyIsParts){
									writer.println(type+"\t"+cs.toStringOneLine());
								}
								else{
									String[] ret = cs.printISParts(colorMap);
									if(ret != null){
										for(String s: ret){
											writer.println(type+"\t"+seqs.getName()+"\t"+s);
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
						total++;
						if(total%25 == 0){
							System.out.println("Already analyzed "+total+" files");
							//long end = System.nanoTime();
							//System.out.println("that took " +TimeUnit.MILLISECONDS.convert(end-start, TimeUnit.NANOSECONDS)+" milliseconds");
						}
		}
		System.out.println("Printed "+i+" files to output");
	}
	public ArrayList<CompareSequence> readFilesTryToMatch(String dir, String subjectFile, String leftFlank, String rightFlank, String type, File searchAdditional) {
		ArrayList<CompareSequence> al = new ArrayList<CompareSequence>();
		
		ArrayList<RichSequence> sequences = Utils.fillArrayListSequences(new File(subjectFile));
		
		BufferedReader is = null, is2 = null;
		Vector<Sequence> additional = new Vector<Sequence>();
		HashMap<String, String> hmAdditional = new HashMap<String, String>();
		try {
			if(searchAdditional != null){
				is2 = new BufferedReader(new FileReader(searchAdditional));
				RichSequenceIterator si2 = RichSequence.IOTools.readFastaDNA(is2, null);
				while(si2.hasNext()){
					additional.add(si2.nextRichSequence());
				}
				for(Sequence s: additional) {
					hmAdditional.put(s.getName(), s.seqString().toString());
				}
			}
		} catch (FileNotFoundException | NoSuchElementException | BioException e1) {
			e1.printStackTrace();
		}
		
		File d = new File(dir);
		Vector<File> ab1s = getAB1Files(d);
		System.out.println("Found "+ab1s.size()+" ab1 files");
		for(File seqs: ab1s){
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
				CompareSequence cs = new CompareSequence(subject, null, query, quals, leftFlank, rightFlank, null, seqs.getParent());
				cs.setAndDetermineCorrectRange(0.05);
				cs.maskSequenceToHighQualityRemove();
				cs.determineFlankPositions();
				cs.setAdditionalSearchString(hmAdditional);
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
		return al;
	}
	public void readFilesFASTQ(String dir, String subjectFile, String leftFlank, String rightFlank, String type, File searchAdditional, boolean writeToOutput){
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
		Vector<File> ab1s = getFASTQFiles(d, null);
		System.out.println("Found "+ab1s.size()+" FASTQ files");
		PrintWriter writer = null;
		if(writeToOutput){
			try {
				writer = new PrintWriter(new FileOutputStream(outputFile,true));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		int fileNr = 1;
		for(File seqs: ab1s){
			System.out.println(fileNr+"/"+ab1s.size()+":"+seqs.getName());
						try {
							FastqDataStore datastore = new FastqFileDataStoreBuilder(seqs)
							   .qualityCodec(FastqQualityCodec.SANGER)				   
							   .hint(DataStoreProviderHint.ITERATION_ONLY).build();
							//FastqDataStore datastore = new FastqFileDataStoreBuilder(seqs)
							   //.qualityCodec(FastqQualityCodec.ILLUMINA)				   
							   //.hint(DataStoreProviderHint.ITERATION_ONLY);//.build();
							if(writeToOutput){
								System.out.println("num records = " + datastore.getNumberOfRecords());
							}
							//Chromatogram chromo = ChromatogramFactory.create(seqs);
							//NucleotideSequence seq = chromo.getNucleotideSequence();
							//QualitySequence quals = chromo.getQualitySequence();
							StreamingIterator<FastqRecord> iter = null;
							try {
								iter = datastore.iterator();
							} catch (DataStoreException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							int counter = 0;
							while(iter.hasNext()){
								FastqRecord fastqRecord = iter.next();
								QualitySequence quals = fastqRecord.getQualitySequence();
								RichSequence query = null;
								try {
									query = RichSequence.Tools.createRichSequence(fastqRecord.getId(),DNATools.createDNA(fastqRecord.getNucleotideSequence().toString()));
								} catch (IllegalSymbolException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								//mask
								
								//CompareSequence cs = new CompareSequence(subject, null, query, quals, leftFlank, rightFlank, null, seqs.getParentFile().getName());
								CompareSequence cs = new CompareSequence(subject, null, query, quals, leftFlank, rightFlank, null, seqs.getParentFile().getName());
								cs.setAndDetermineCorrectRange(0.05);
								cs.maskSequenceToHighQualityRemove();
								cs.determineFlankPositions();
								//cs.setAdditionalSearchString(additional);
								cs.setCutType(type);
								cs.setCurrentFile(seqs.getName());
								//only correctly found ones
								if(cs.getRemarks().length() == 0){
									if(!printOnlyIsParts){
										if(collapseEvents){
											String key = cs.getKey(includeStartEnd);
											if(countEvents.containsKey(key)){
												countEvents.put(key, countEvents.get(key)+1);
											}
											else{
												countEvents.put(key, 1);
												actualEvents.put(key, cs.toStringOneLine());
											}
										}
										else{
											if(writer != null){
												writer.println(type+"\t"+cs.toStringOneLine());
											}
											else{
												System.out.println(type+"\t"+cs.toStringOneLine());
											}
										}
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
								counter++;
								if(writer != null && counter%10000==0){
									System.out.println("Already processed "+counter+" reads");
									//iter.close();
									//break;
								}
							}
							
						} catch (IOException e1) {
							e1.printStackTrace();
						}
			if(writer != null){
				if(collapseEvents){
					System.out.println("Writing events "+actualEvents.size());
					for(String key: actualEvents.keySet()){
						writer.println(countEvents.get(key)+"\t"+actualEvents.get(key));
					}
					actualEvents.clear();
					countEvents.clear();
				}
			}
			fileNr++;
		}
		writer.close();
	}
	public void readFilesFASTQMultiThreaded(String dir, String subjectFile, String leftFlank, String rightFlank, String type, File searchAdditional, boolean writeToOutput, String containsString, MyOptions options){
		BufferedReader is = null, is2 = null;
		RichSequence subject = null;
		Vector<Sequence> additional = new Vector<Sequence>();
		HashMap<String, String> hmAdditional = new HashMap<String, String>();
		try {
			is = new BufferedReader(new FileReader(subjectFile));
			if(searchAdditional!= null){
				is2 = new BufferedReader(new FileReader(searchAdditional));
				SequenceIterator si2 = IOTools.readFastaDNA(is2, null);
				while(si2.hasNext()){
					additional.add(si2.nextSequence());
				}
				for(Sequence s: additional) {
					hmAdditional.put(s.getName(), s.seqString().toString());
				}
			}
			RichSequenceIterator si = IOTools.readFastaDNA(is, null);
			subject = si.nextRichSequence();
			
			
		} catch (FileNotFoundException | NoSuchElementException | BioException e1) {
			e1.printStackTrace();
		}
		
		File d = new File(dir);
		Vector<File> ab1s = getFASTQFiles(d, containsString);
		System.out.println("Found "+ab1s.size()+" FASTQ files");
		int fileNr = 1;
		Vector<Thread> v = new Vector<Thread>();
		Vector<Thread> running = new Vector<Thread>();
		Vector<Integer> toBeRemoved = new Vector<Integer>();
		Vector<File> files = new Vector<File>();
		String prepostfix ="";
		if(options.collapseEvents()){
			prepostfix = "_collapse_";
		}
		String postfix = options.getOutputPostfix();
		for(File seqs: ab1s){
			System.out.println(fileNr+"/"+ab1s.size()+":"+seqs.getName());
			File output = new File(seqs.getAbsolutePath()+prepostfix+postfix);
			files.add(output);
			SequenceFileThread sft = new SequenceFileThread(seqs, true, subject, leftFlank, rightFlank,output, options.collapseEvents(), options.getMaxError(), hmAdditional);
			if(startPositions != null && endPositions!= null) {
				sft.setStartEndPositions(startPositions, endPositions);
			}
			sft.setMinimalCount(options.getMinNumber());
			sft.setCollapseStartEnd(includeStartEnd);
			Thread newThread = new Thread(sft);
			v.add(newThread);
			//newThread.start();
			fileNr++;
		}
		//int max = Runtime.getRuntime().availableProcessors();
		long max = options.getThreads();
		int maxFiles = ab1s.size();
		System.out.println("Gonna start "+Math.min(max, maxFiles)+" cpus");
		//let's see if this works
		while(v.size()>0 || running.size()>0){
			for(int nr = running.size()-1;nr>=0;nr--){
				if(!running.get(nr).isAlive()){
					toBeRemoved.add(nr);
				}
			}
			for(int i: toBeRemoved){
				running.remove(i);
			}
			toBeRemoved.clear();
			if(running.size()<max && v.size()>0){
				Thread t = v.remove(0);
				running.add(t);
				t.start();
			}
			try {
				//1 second of sleep... ZzZ...
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		writeAllFilesToOutput(files);
	}
	private void writeAllFilesToOutput(Vector<File> files) {
		//create outputstream for output
		if(getOutputFile() != null){
			try {
				PrintWriter writer = new PrintWriter(getOutputFile(), "UTF-8");
				//add header, always
				if(this.collapseEvents){
					writer.println("#Counts\t"+CompareSequence.getOneLineHeader());
				}
				else{
					writer.println("Empty\t"+CompareSequence.getOneLineHeader());
				}
				for(File f: files){
					Scanner s = new Scanner(f);
					while(s.hasNextLine()){
						writer.println(s.nextLine());
					}
					s.close();
				}
				writer.close();
			}
			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private Vector<File> getFASTQFiles(File d, String containsString) {
		Vector<File> files = new Vector<File>();
		for(File f: d.listFiles()){
			if(f.isDirectory()){
				files.addAll(getAB1Files(f));
			}
			//for some reason .gz does not work yet
			else if(f.isFile() && f.getName().endsWith(".fastq") || f.getName().endsWith(".fastq.gz")){
				if(containsString != null) {
					if(f.getName().contains(containsString)) {
						files.add(f);
					}
				}
				else {
					files.add(f);
				}
			}
		}
		return files;
	}
	public static Vector<File> getAB1Files(File d) {
		Vector<File> files = new Vector<File>();
		for(File f: d.listFiles()){
			if(f.isDirectory()){
				files.addAll(getAB1Files(f));
			}
			else if(f.isFile() && f.getName().endsWith(".ab1")){
				files.add(f);
			}
		}
		return files;
	}
	public void setPrintOnlyISParts(){
		this.printOnlyIsParts = true;
	}
	public void setColorMap(HashMap<String, String> colorMap){
		this.colorMap = colorMap;
	}
	public void setOutputFile(File f){
		this.outputFile = f;
	}
	public File getOutputFile(){
		return this.outputFile;
	}
	public void writeln(String string) {
		if(getOutputFile() != null){
			try {
				PrintWriter writer = new PrintWriter(this.outputFile, "UTF-8");
				writer.println(string);
				writer.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void setCollapseEvents(boolean collapse){
		this.collapseEvents = collapse;
	}
	public ArrayList<CompareSequence> readFilesTryToMatch(
			File dir, RichSequence currentSequence, String leftFlank,
			String rightFlank, String type, String searchAdditional, boolean printNonCorrect, double quality) {
		
		Vector<Sequence> additional = new Vector<Sequence>();
		HashMap<String, String> hmAdditional = new HashMap<String, String>();
		try {
			if(searchAdditional != null){
				BufferedReader is2 = new BufferedReader(new FileReader(searchAdditional));
				RichSequenceIterator si2 = RichSequence.IOTools.readFastaDNA(is2, null);
				while(si2.hasNext()){
					additional.add(si2.nextRichSequence());
				}
				for(Sequence s: additional) {
					hmAdditional.put(s.getName(), s.seqString().toString());
				}
			}
		} catch (FileNotFoundException | NoSuchElementException | BioException e1) {
			e1.printStackTrace();
		}
		
		ArrayList<CompareSequence> al = new ArrayList<CompareSequence>();
		Vector<File> ab1s = getAB1Files(dir);
		//System.out.println("Found "+ab1s.size()+" ab1 files");
		for(File seqs: ab1s){
			RichSequence subject = currentSequence;
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
				CompareSequence cs = new CompareSequence(subject, null, query, quals, leftFlank, rightFlank, null, seqs.getParent());
				cs.setAndDetermineCorrectRange(quality);
				cs.maskSequenceToHighQualityRemove();
				cs.determineFlankPositions();
				cs.setAdditionalSearchString(hmAdditional);
				cs.setCutType(type);
				//only correctly found ones
				//and filter for events that are the same in ID and class
				if(printNonCorrect || (cs.getRemarks().length() == 0 && cs.getType() != CompareSequence.Type.WT)){
					al.add(cs);
				}
				
			} catch (IOException e1) {
				System.err.println(seqs.getName()+" has a problem");
				e1.printStackTrace();
			}
		}
		return al;
	}
	public void setSequences(ArrayList<CompareSequence> al) {
		this.sequences = al;
	}
	public void printXY() {
		putInHash();
		//iterate hash
		List<String> keys = new ArrayList<String>(hash.keySet());
		Collections.sort(keys);
		for(String key: keys){
			ArrayList<CompareSequence> temp = hash.get(key);
			ArrayList<Double> tempXs = getXs(temp.size());
			temp.sort((o1,o2) -> o1.getDelEnd()-o2.getDelEnd());
			StringBuffer sb = new StringBuffer();
			sb.append("X\tX");
			for(Double d: tempXs){
				sb.append('\t');
				sb.append(d);
			}
			System.out.println(sb);
			sb = new StringBuffer();
			sb.append(key+"\t"+temp.get(0).getSubjectComments());
			for(CompareSequence cs: temp){
				sb.append("\t");
				sb.append(cs.getRelativeDelEnd());
			}
			System.out.println(sb);
		}
	}
	private ArrayList<Double> getXs(int size){
		ArrayList<Double> list = new ArrayList<Double>();
		for(int i = 0;i<size;i++){
			list.add(i/(double)(size-1));
		}
		return list;
	}
	
	private void putInHash() {
		if(hash == null){
			hash = new HashMap<String, ArrayList<CompareSequence>>();
		}
		for(CompareSequence cs: this.sequences){
			String subject = cs.getSubject();
			ArrayList<CompareSequence> temp = hash.get(subject);
			if(temp != null){
				temp.add(cs);
			}
			else{
				temp = new ArrayList<CompareSequence>();
				temp.add(cs);
				hash.put(subject, temp);
			}
		}
	}
	public class CustomComparator {
	    public int compareTo(CompareSequence object1, CompareSequence object2) {
	        return object1.getDelStart() - object2.getDelStart();
	    }
	}
	public void setStartEndPositions(int[] startPositions, int[] endPositions) {
		this.startPositions = startPositions;
		this.endPositions = endPositions;
		
	}
	public void setMinimalCount(long minimalCount) {
		this.minimalCount = minimalCount;
	}
	public void setThreads(int cpu) {
		this.nrCPUs = cpu;
	}
	public void setIncludeStartEnd(boolean includeStartEnd) {
		this.includeStartEnd = includeStartEnd;
	}
	
}

package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojavax.bio.seq.RichSequence;
import org.jcvi.jillion.core.datastore.DataStoreException;
import org.jcvi.jillion.core.datastore.DataStoreProviderHint;
import org.jcvi.jillion.core.qual.PhredQuality;
import org.jcvi.jillion.core.qual.QualitySequence;
import org.jcvi.jillion.core.residue.nt.Nucleotide;
import org.jcvi.jillion.core.util.iter.StreamingIterator;
import org.jcvi.jillion.trace.fastq.FastqDataStore;
import org.jcvi.jillion.trace.fastq.FastqFileDataStoreBuilder;
import org.jcvi.jillion.trace.fastq.FastqFileReader;
import org.jcvi.jillion.trace.fastq.FastqFileReader.Results;
import org.jcvi.jillion.trace.fastq.FastqQualityCodec;
import org.jcvi.jillion.trace.fastq.FastqRecord;

public class SequenceFileThread implements Runnable {

	private File f, output, outputStats;
	private boolean writeToOutput;
	private RichSequence subject;
	private String leftFlank, rightFlank;
	private HashMap<String, Integer> countEvents = new HashMap<String, Integer>();
	private HashMap<String, String> actualEvents = new HashMap<String, String>();
	private HashMap<String, CompareSequence> csEvents = new HashMap<String, CompareSequence>();
	private HashMap<String, String> colorMap;
	private boolean collapse;
	private double maxError;
	private HashMap<String, String> hmAdditional;
	private long minimalCount;
	private boolean includeStartEnd;
	private long maxReads;
	private boolean printHeader = false;
	private long minPassedPrimer;
	private String leftPrimer, rightPrimer;
	private String alias = "";
	
	public SequenceFileThread(File f, boolean writeToOutput, RichSequence subject, String leftFlank, String rightFlank, File output, boolean collapse, double maxError, HashMap<String, String> additional){
		this.f = f;
		this.writeToOutput = writeToOutput;
		this.subject = subject;
		this.leftFlank = leftFlank;
		this.rightFlank = rightFlank;
		this.output = output;
		this.outputStats = new File(output.getAbsolutePath()+"stats.txt");
		this.collapse = collapse;
		this.maxError = maxError;
		this.hmAdditional = additional;
	}
	
	@Override
	public void run() {
		runReal();
	}
	public void runReal() {
		Thread.currentThread().setName(f.getName());
		boolean printOnlyIsParts = false;
		boolean collapseEvents = collapse;
		PrintWriter writer = null, writerStats = null;
		String type = "";
		if(writeToOutput){
			try {
				writer = new PrintWriter(new FileOutputStream(output,false));
				writerStats = new PrintWriter(new FileOutputStream(outputStats,false));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		int counter = 0;
		int wrong = 0;
		int correct = 0;
		int wrongPositionLeft = 0;
		int wrongPositionRight = 0;
		int badQual = 0;
		Utils utils = new Utils();
		long realStart = System.nanoTime();
		try {
			FastqDataStore datastore = new FastqFileDataStoreBuilder(f)
			   .qualityCodec(FastqQualityCodec.SANGER)				   
			   .hint(DataStoreProviderHint.ITERATION_ONLY).build();
			//FastqDataStore datastore = new FastqFileDataStoreBuilder(seqs)
			   //.qualityCodec(FastqQualityCodec.ILLUMINA)				   
			   //.hint(DataStoreProviderHint.ITERATION_ONLY);//.build();
			if(writeToOutput){
				//Very expensive call!!
				//System.out.println(f.getName()+"\tnum records = \t" + datastore.getNumberOfRecords());
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
			
			ArrayList<SetAndPosition> poss = createSetAndPosition();
			long start = System.nanoTime();
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
				boolean checkReverse = false;
				CompareSequence cs = new CompareSequence(subject, null, query, quals, leftFlank, rightFlank, null, f.getParentFile().getName(), checkReverse);
				cs.setAndDetermineCorrectRange(maxError);
				cs.maskSequenceToHighQualityRemove();
				
				if(cs.getRemarks().length()!=0) {
					badQual++;
				}
				//small speedup
				boolean leftCorrect = false;
				boolean rightCorrect = false;
				System.out.println(java.lang.Thread.activeCount());
				if(cs.getRemarks().length()==0) {
					cs.determineFlankPositions();
					//System.out.println("So far took :"+duration+" milliseconds");
					leftCorrect = cs.isCorrectPositionLeft(poss);
					rightCorrect = cs.isCorrectPositionRight(poss);
					if(!leftCorrect) {
						wrongPositionLeft++;
					}
					if(!rightCorrect) {
						wrongPositionRight++;
					}
					if(leftCorrect && rightCorrect) {
						cs.setAdditionalSearchString(hmAdditional);
						//cs.setCutType(type);
						cs.setCurrentFile(f.getName());
						cs.setCurrentAlias(alias);
					}
				}
				//only correctly found ones
				//System.out.println(cs.toStringOneLine());
				if(cs.getRemarks().length() == 0 && leftCorrect && rightCorrect){
					correct++;
					if(!printOnlyIsParts){
						if(collapseEvents){
							String key = cs.getKey(includeStartEnd);
							if(countEvents.containsKey(key)){
								countEvents.put(key, countEvents.get(key)+1);
								//while this works, it might be slow and/or incorrect!
								//the best would be to give the majority here
								//replaced not so great sequences with more accurate ones
								//to be able to filter better later
								//20180731, added match positions as now sometimes shorter events are selected
								//which causes problems with filters later
								if(cs.getNrNs()<csEvents.get(key).getNrNs() && cs.getMatchStart()<= csEvents.get(key).getMatchStart() && cs.getMatchEnd() >=csEvents.get(key).getMatchEnd()  ) {
									csEvents.put(key, cs);
								}
							}
							else{
								countEvents.put(key, 1);
								//save the object instead
								csEvents.put(key, cs);
								//actualEvents.put(key, cs.toStringOneLine());
							}
						}
						else{
							if(writer != null){
								writer.println(type+"\t"+cs.toStringOneLine());
								//System.out.println(type+"\t"+cs.toStringOneLine());
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
								System.out.println(type+"\t"+f.getName()+"\t"+s);
							}
						}
					}
				}
				else {
					//System.err.println(cs.getRemarks());
					wrong++;
				}
				//System.out.println(cs.toStringOneLine());
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
				if(writer != null && counter%1000==0){
					long end = System.nanoTime();
					long duration = TimeUnit.MILLISECONDS.convert((end-start), TimeUnit.NANOSECONDS);
					//System.out.println("So far took :"+duration+" milliseconds");
					start = end;
					System.out.println("Thread: "+Thread.currentThread().getName()+" processed "+counter+" reads, costed (milliseconds): "+duration+" correct: "+correct+" wrong: "+wrong+" wrongPositionL: "+wrongPositionLeft+" wrongPositionR: "+wrongPositionRight+ " correct fraction: "+(correct/(double)(correct+wrong)));
					//iter.close();
					//break;
				}
				if(counter>= this.maxReads) {
					break;
				}
			}
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if(writer != null){
			if(collapseEvents){
				//System.out.println("Writing events "+csEvents.size()+" to: "+output.getAbsolutePath());
				//NO NOT HERE
				//writer.println("countEvents\t"+CompareSequence.getOneLineHeader());
				int count = 0;
				if(printHeader) {
					writer.println("countEvents\t"+CompareSequence.getOneLineHeader());
				}
				for(String key: csEvents.keySet()){
					//only output if we saw it minimalCount times
					if(countEvents.get(key)>=minimalCount) {
						writer.println(countEvents.get(key)+"\t"+csEvents.get(key).toStringOneLine());
						count++;
					}
				}
				System.out.println("Written "+count+" events to: "+output.getAbsolutePath());
				actualEvents.clear();
				countEvents.clear();
			}
			writer.close();
		}
		if(writerStats!= null) {
			writerStats.println(f.getName()+"\treads\t"+counter);
			writerStats.println(f.getName()+"\tcorrect\t"+correct);
			writerStats.println(f.getName()+"\twrong\t"+wrong);
			writerStats.println(f.getName()+"\twrongLeft\t"+wrongPositionLeft);
			writerStats.println(f.getName()+"\twrongRight\t"+wrongPositionRight);
			writerStats.println(f.getName()+"\tbadQual\t"+badQual);
			writerStats.close();
			System.out.println("Written stats to: "+outputStats.getAbsolutePath());
		}
		long end = System.nanoTime();
		long duration = TimeUnit.MILLISECONDS.convert((end-realStart), TimeUnit.NANOSECONDS);
		System.out.println("duration "+duration);
		utils.printCacheStats();
	}

	private ArrayList<SetAndPosition> createSetAndPosition() {
		int leftPos = subject.seqString().indexOf(leftPrimer);
		int rightPos = subject.seqString().indexOf(Utils.reverseComplement(rightPrimer));
		if(leftPos == -1 || rightPos == -1) {
			System.err.println("Cannot find primers!");
			System.err.println(subject.seqString());
			System.err.println(leftPrimer);
			System.err.println(rightPrimer);
			System.err.println(leftPos);
			System.err.println(rightPos);
		}
		//now adjust the rightPosition
		rightPos += rightPrimer.length();
		SetAndPosition sp = new SetAndPosition(subject.getName(),leftPos, rightPos);
		long leftPlus = leftPos+leftPrimer.length();
		long rightPlus = rightPos-rightPrimer.length();
		SetAndPosition spone = new SetAndPosition(subject.getName(),leftPlus, rightPlus);
		leftPlus += minPassedPrimer;
		rightPlus -= minPassedPrimer;
		SetAndPosition sptwo = null;
		if(minPassedPrimer != 0) {
			sptwo = new SetAndPosition(subject.getName(), leftPlus, rightPlus);
		}
		ArrayList<SetAndPosition> al = new ArrayList<SetAndPosition>();
		al.add(sp);
		al.add(spone);
		al.add(sptwo);
		System.out.println(sp);
		System.out.println(spone);
		System.out.println(sptwo);
		return al;
	}

	public void setMinimalCount(long minimalCount) {
		this.minimalCount = minimalCount;
	}
	public void setCollapseStartEnd(boolean includeStartEnd) {
		this.includeStartEnd = includeStartEnd;
	}

	public void setMaximumReads(long maxReads) {
		this.maxReads = maxReads;
		
	}

	public void printHeader() {
		this.printHeader = true;
		
	}

	public void setLeftPrimer(String leftPrimer) {
		this.leftPrimer = leftPrimer;
	}

	public void setRightPrimer(String rightPrimer) {
		this.rightPrimer = rightPrimer;
	}

	public void setMinPassedPrimer(long minPassedPrimer) {
		this.minPassedPrimer = minPassedPrimer;
	}
	
	public void setAlias(String alias) {
		this.alias = alias;
	}
}

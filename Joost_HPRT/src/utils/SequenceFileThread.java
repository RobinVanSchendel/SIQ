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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
	private KMERLocation kmerl;
	private boolean allowJump;
	
	public SequenceFileThread(File f, boolean writeToOutput, RichSequence subject, String leftFlank, String rightFlank, File output, boolean collapse, double maxError, HashMap<String, String> additional){
		this.f = f;
		this.writeToOutput = writeToOutput;
		this.subject = subject;
		this.leftFlank = leftFlank;
		this.rightFlank = rightFlank;
		this.output = output;
		this.outputStats = new File(output.getAbsolutePath()+".stats.txt");
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
		long realStart = System.nanoTime();
		ArrayList<SetAndPosition> poss = createSetAndPosition();
		AtomicLong start = new AtomicLong(System.nanoTime());
		AtomicInteger counter = new AtomicInteger(0);
		AtomicInteger wrong = new AtomicInteger(0);
		AtomicInteger wrongPosition = new AtomicInteger(0);
		//AtomicInteger wrongPositionRight = new AtomicInteger(0);
		AtomicInteger badQual = new AtomicInteger(0);
		AtomicInteger correct = new AtomicInteger(0);
		HashMap<String, Integer> remarks = new HashMap<String, Integer>();
		
		try {
			FastqFileReader.forEach( f, FastqQualityCodec.SANGER, 
			        (id, fastqRecord) -> {
				QualitySequence quals = fastqRecord.getQualitySequence();
				/*
				RichSequence query = null;
				try {
					query = RichSequence.Tools.createRichSequence(fastqRecord.getId(),DNATools.createDNA(fastqRecord.getNucleotideSequence().toString()));
				} catch (IllegalSymbolException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				*/
				//mask
				
				//CompareSequence cs = new CompareSequence(subject, null, query, quals, leftFlank, rightFlank, null, seqs.getParentFile().getName());
				boolean checkReverse = false;
				CompareSequence cs = new CompareSequence(subject, fastqRecord.getNucleotideSequence().toString(), quals, leftFlank, rightFlank, null, f.getParentFile().getName(), checkReverse, id, kmerl);
				cs.setAndDetermineCorrectRange(maxError);
				cs.maskSequenceToHighQualityRemove();
				cs.setAllowJump(this.allowJump);
				
				if(cs.getRemarks().length()!=0) {
					badQual.getAndIncrement();
				}
				//small speedup
				boolean leftCorrect = false;
				boolean rightCorrect = false;
				if(cs.getRemarks().length()==0) {
					cs.determineFlankPositions();
					leftCorrect = cs.isCorrectPositionLeft(poss);
					rightCorrect = cs.isCorrectPositionRight(poss);
					if(leftCorrect && rightCorrect) {
						cs.setAdditionalSearchString(hmAdditional);
						//cs.setCutType(type);
						cs.setCurrentFile(f.getName());
						cs.setCurrentAlias(alias, f.getName());
					}
					else {
						wrongPosition.getAndIncrement();
						if(cs.getRemarks().length()>0) {
							String rs = cs.getRemarks();
							if(remarks.containsKey(rs)) {
								remarks.put(rs, remarks.get(rs)+1);
							}
							else {
								remarks.put(rs, 1);
							}
						}
					}
				}
				//only correctly found ones
				//System.out.println(cs.toStringOneLine());
				if(cs.getRemarks().length() == 0 && leftCorrect && rightCorrect){
					correct.getAndIncrement();
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
					wrong.getAndIncrement();
				}
				//System.out.println(cs.toStringOneLine());
				//no masking
				counter.getAndIncrement();
				if(counter.get()%10000==0){
					long end = System.nanoTime();
					long duration = TimeUnit.MILLISECONDS.convert((end-start.get()), TimeUnit.NANOSECONDS);
					//System.out.println("So far took :"+duration+" milliseconds");
					start.set(end);
					System.out.println("Thread: "+Thread.currentThread().getName()+" processed "+counter+" reads, costed (milliseconds): "+duration+" correct: "+correct+" wrong: "+wrong+" wrongPosition: "+wrongPosition+ " correct fraction: "+(correct.get()/(double)(correct.get()+wrong.get())));
					//iter.close();
					//break;
				}
				if(counter.get()>= this.maxReads) {
					throw new BreakException();
				}
			});
		} catch (IOException | RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BreakException e) {
			//this is just to escape the lambda expression
			//e.printStackTrace();
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
			writerStats.println(f.getName()+"\twrongPosition\t"+wrongPosition);
			writerStats.println(f.getName()+"\tbadQual\t"+badQual);
			for(String key: remarks.keySet()) {
				writerStats.println(f.getName()+"\t"+key+"\t"+remarks.get(key));
			}
			writerStats.close();
			System.out.println("Written stats to: "+outputStats.getAbsolutePath());
		}
		long end = System.nanoTime();
		long duration = TimeUnit.SECONDS.convert((end-realStart), TimeUnit.NANOSECONDS);
		System.out.println("duration "+duration);
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
			System.exit(1);
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
	public void setKMERLocation(KMERLocation k) {
		this.kmerl = k;
	}

	public void setAllowJump(boolean allowJump) {
		this.allowJump = allowJump;
	}
}

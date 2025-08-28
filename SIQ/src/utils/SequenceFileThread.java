package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.biojava.bio.BioException;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.jcvi.jillion.core.datastore.DataStoreProviderHint;
import org.jcvi.jillion.core.qual.QualitySequence;
import org.jcvi.jillion.core.util.iter.StreamingIterator;
import org.jcvi.jillion.trace.fastq.FastqDataStore;
import org.jcvi.jillion.trace.fastq.FastqFileDataStoreBuilder;
import org.jcvi.jillion.trace.fastq.FastqFileReader;
import org.jcvi.jillion.trace.fastq.FastqQualityCodec;
import org.jcvi.jillion.trace.fastq.FastqRecord;

import gui.NGS;
import gui.NGSTableModel;

public class SequenceFileThread extends Thread {

	private File f, output, outputStats, outputTopStats;
	private boolean writeToOutput;
	private Subject subject;
	private BarcodeCounter countEvents = new BarcodeCounter();
	private HashMap<String, CompareSequence> csEvents = new HashMap<String, CompareSequence>();
	private HashMap<String, String> colorMap;
	private boolean collapse = true;
	private double maxError;
	private HashMap<String, String> hmAdditional;
	private long minimalCount;
	private boolean includeStartEnd;
	private long maxReads;
	private boolean printHeader = true;
	private String alias = "";
	private boolean allowJump;
	private File statsOutput;
	private File singleFileF, singleFileR;
	private boolean checkReverseOverwrite = false;
	private NGS ngs;
	
	private HashMap<String, String> lookupDone = new HashMap<String, String>();
	private NGSTableModel tableModel;
	private String flashExec;
	private int cpus = 1;
	private Semaphore semaphore;
	private int tinsDistValue = -1;
	private boolean overwriteMerged = false;
	private boolean delinsFilter = true; //default true
	private boolean longReads = false; //default false
	
	public SequenceFileThread(File f, boolean writeToOutput, Subject subject, File output, File statsOutput, double maxError, HashMap<String, String> additional, File outputTopStats, boolean overwriteMerge, boolean delinsFilter, boolean longread){
		this.f = f;
		this.writeToOutput = writeToOutput;
		this.subject = subject;
		this.output = output;
		this.outputStats = statsOutput;
		this.outputTopStats = outputTopStats;
		this.maxError = maxError;
		this.hmAdditional = additional;
		this.overwriteMerged = overwriteMerge;
		this.delinsFilter = delinsFilter;
		this.longReads = longread;
	}
	private void setCheckReverseOverwrite() {
		checkReverseOverwrite = true;
	}
	
	@Override
	public void run() {
		try {
			semaphore.acquire();
			runReal();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			semaphore.release();
		}
		
	}
	public void runReal() {
		Thread.currentThread().setName(alias);
		boolean printOnlyIsParts = false;
		boolean collapseEvents = collapse;
		PrintWriter writer = null, writerStats = null, writerTopStats = null;
		String type = "";
		//reset NGSTable if needed
		if(this.tableModel!= null) {
			this.tableModel.setStatus(ngs, 0);
			this.tableModel.setTotal(ngs, 0);
			this.tableModel.setCorrect(ngs, 0);
			this.tableModel.setPercentage(ngs, 0);
		}
		//do this here again!
		subject.swapPrimersIfNeeded();
		
		
		//Check if you need to assemble anything
		if(!f.exists() || (overwriteMerged && ngs.getR2()!=null) ) {
			if(ngs!=null) {
				System.out.println("Assembling the file via R1 R2");
				tableModel.setStatus(ngs,-1);
				tableModel.setTextStatus(ngs,"Merging");
				assembleFile();
				tableModel.setStatus(ngs, 0);
				
			}
		}
		if(tableModel!=null) {
			tableModel.setTextStatus(ngs,"Counting reads");
		}
		
		if(writeToOutput){
			try {
				writer = new PrintWriter(new FileOutputStream(output,false));
				writerStats = new PrintWriter(new FileOutputStream(outputStats,false));
				writerTopStats = new PrintWriter(new FileOutputStream(outputTopStats,false));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		long realStart = System.nanoTime();
		//ArrayList<SetAndPosition> poss = createSetAndPosition();
		//if(poss == null) {
		//	setCheckReverseOverwrite();
		//}
		AtomicLong start = new AtomicLong(System.nanoTime());
		AtomicInteger counter = new AtomicInteger(0);
		AtomicInteger wrong = new AtomicInteger(0);
		AtomicInteger wrongPosition = new AtomicInteger(0);
		AtomicInteger wrongPositionL = new AtomicInteger(0);
		AtomicInteger wrongPositionR = new AtomicInteger(0);
		AtomicInteger correctPositionFR = new AtomicInteger(0);
		AtomicInteger correctPositionFRassembled = new AtomicInteger(0);
		AtomicInteger badQual = new AtomicInteger(0);
		AtomicInteger containsN = new AtomicInteger(0);
		AtomicInteger correct = new AtomicInteger(0);
		AtomicInteger cacheHit = new AtomicInteger(0);
		AtomicBoolean takeRc = new AtomicBoolean(false);
		AtomicInteger totalRawReadsCounter = new AtomicInteger(0);
		HashMap<String, Integer> remarks = new HashMap<String, Integer>();
		HashMap<String, Integer> commonSequences = new HashMap<String, Integer>();

		//This hash will contain the remarks of a rejected sequence
		HashMap<String, String> commonSequencesRemarks = new HashMap<String, String>();
		
		//for PacBio data
		//would be better to know that this is PacBio data
		if(subject.isLongRead()) {
		//if(!subject.hasPrimers()) {
			this.setCheckReverseOverwrite();
			//this might cause issues
			//indeed this might be PacBio specific
			//disable for now
			setAllowJump(true);
		}
		
		
		try {
			//first check the non-assembled reads
			//FastqFileReader reader = new FastqFileReader()
			if(singleFileF != null && singleFileR != null) {
				FastqDataStore datastoreF = new FastqFileDataStoreBuilder(singleFileF).qualityCodec(FastqQualityCodec.SANGER).hint(DataStoreProviderHint.ITERATION_ONLY).build();
				FastqDataStore datastoreR = new FastqFileDataStoreBuilder(singleFileR).qualityCodec(FastqQualityCodec.SANGER).hint(DataStoreProviderHint.ITERATION_ONLY).build();
				StreamingIterator<FastqRecord> itF = datastoreF.iterator();
				StreamingIterator<FastqRecord> itR = datastoreR.iterator();
				int count = 0;
				boolean reverseDecisionMade = false;
				while(itF.hasNext() && itR.hasNext()) {
					FastqRecord F = itF.next();
					FastqRecord R = itR.next();
					count++;
					totalRawReadsCounter.getAndIncrement();
					//Forward
					boolean checkReverse = false;
					if(!reverseDecisionMade) {
						checkReverse = true;
					}
					if(checkReverseOverwrite) {
						checkReverse = true;
					}
					CompareSequence cs = new CompareSequence(subject, F.getNucleotideSequence().toString(), F.getQualitySequence(), f.getParentFile().getName(), checkReverse, F.getId());
					if(reverseDecisionMade && takeRc.get()) {
						cs.reverseRead();
					}
					cs.setAndDetermineCorrectRange(maxError);
					cs.maskSequenceToHighQualityRemove();
					//only if masked
					if(cs.isMasked()) {
						cs.setAllowJump(this.allowJump);
						cs.determineFlankPositions(true);
						boolean leftCorrect = cs.isCorrectPositionLeft();
						//speedup
						if(leftCorrect) {
							boolean wasReversed = cs.isReversed();
							cs = new CompareSequence(subject, R.getNucleotideSequence().toString(), R.getQualitySequence(), f.getParentFile().getName(), checkReverse, R.getId());
							//bug, needs to be opposite orientation of forward read
							if(!wasReversed) {
								//System.out.println("reversing");
								cs.reverseRead();
							}
							cs.setAndDetermineCorrectRange(maxError);
							cs.maskSequenceToHighQualityRemove();
							cs.setAllowJump(this.allowJump);
							cs.determineFlankPositions(false);
							boolean rightCorrect = cs.isCorrectPositionRight();
							//System.out.println(rightCorrect);
							//System.out.println(R.getNucleotideSequence());
							if(leftCorrect && rightCorrect) {
								correctPositionFR.getAndIncrement();
								//System.out.println("correct "+Thread.activeCount());
								if(!reverseDecisionMade) {
									if(wasReversed) {
										takeRc.set(true);
									}
									reverseDecisionMade = true;
								}
							}
							else {
								//System.out.println("not correct "+leftCorrect+":"+rightCorrect+" - "+Thread.activeCount());
								//System.out.println(F.getNucleotideSequence());
								//System.out.println(R.getNucleotideSequence());
//								System.out.println(R.getQualitySequence().toArray());
								//System.out.println(cs.getRangesString());
								//for(long i=0;i<R.getQualitySequence().getLength();i++) {
									//System.out.println(R.getQualitySequence().get(i).getErrorProbability());
								//}
								//System.out.println(cs.toStringOneLine());
								//if(R.getId().contentEquals("A00379:191:H7FWTDSXY:3:1101:22905:7905 2:N:0:ATGGATAA+GGCGATAA")) {
								//	System.out.println("Found");
								//	System.out.println(cs.isCorrectPositionRight());
								//	System.exit(0);
								//}
								//System.out.println("===");
								//System.exit(0);
							}
						}
					}
					if(maxReads!= 0 && count>=maxReads) {
						break;
					}
					if(count%10000==0 && count>0){
						long end = System.nanoTime();
						long duration = TimeUnit.MILLISECONDS.convert((end-start.get()), TimeUnit.NANOSECONDS);
						//System.out.println("So far took :"+duration+" milliseconds");
						start.set(end);
						System.out.println("Thread: "+Thread.currentThread().getName()+" processed "+count+" reads, costed (milliseconds): "+duration+" correct: "+correctPositionFR+" correct fraction: "+(correctPositionFR.get()/(double)(count)));
					}
					if(this.isInterrupted()) {
						break;
					}
				}
				//System.out.println("CorrectPositions:\t"+correctPositionFR);
				itF.close();
				itR.close();
				datastoreF.close();
				datastoreR.close();
			}
			AtomicInteger totalReads = new AtomicInteger(0);
			if(this.tableModel!= null) {
				//might be a bit expensive, but it is convenient to have
				FastqFileReader.forEach( f, FastqQualityCodec.SANGER, 
				        (id, fastqRecord) -> {
				        	totalReads.getAndIncrement();
				        });
				//System.out.println("total sequences "+totalReads.get());
				if(this.maxReads>0) {
					int min = (int) Math.min(totalReads.get(), this.maxReads);
					totalReads.set(min);
				}
				tableModel.setTextStatus(ngs,"Analyzing reads");
			}
			System.out.println(Thread.currentThread().getName()+"realLeft:"+subject.getLeftPrimer());
			System.out.println(Thread.currentThread().getName()+"realRight:"+subject.getRightPrimer());
			AtomicBoolean reverseDecisionMade = new AtomicBoolean(false);
			FastqFileReader.forEach( f, FastqQualityCodec.SANGER, 
			        (id, fastqRecord) -> {
			    totalRawReadsCounter.getAndIncrement();
				QualitySequence quals = fastqRecord.getQualitySequence();
				String seq = fastqRecord.getNucleotideSequence().toString();
				
				boolean checkReverse = false;
				boolean flaggedAsWrong = false;
				
				if(!reverseDecisionMade.get()) {
					checkReverse = true;
					//check if these are PacBio reads
					//or use the checkbox from the GUI panel
					//not ideal solution as reads can also be mixed
					if(id.endsWith("ccs") || this.longReads) {
						subject.setLongRead(true);
						this.setCheckReverseOverwrite();
						setAllowJump(true);
					}
				}
				else {
					checkReverse = false;
				}
				//in some cases can be overwritten
				if(checkReverseOverwrite) {
					checkReverse = true;
				}
				CompareSequence cs = new CompareSequence(subject, seq, quals, f.getParentFile().getName(), checkReverse, id);
				cs.setDelinsFilter(this.delinsFilter);
				//set barcode directly
				if(id.contains(" BC:")) {
					int startBC = id.indexOf(" BC:")+4;
					String barcode = id.substring(startBC);
					cs.setBarcode(barcode);
				}
				
				//bug if checkReverseOverwrite TRUE we already checked if we needed to reverse
				if(reverseDecisionMade.get() && takeRc.get() && !checkReverseOverwrite) {
					cs.reverseRead();
				}
				cs.setAndDetermineCorrectRange(maxError);
				//if there are primers specified it makes sense to call this
				if(!subject.isLongRead() && subject.hasPrimers()) {
					cs.maskSequenceToHighQualityRemoveSingleRange();
				}
				//PacBio data for instance should keep the best sequence
				else {
					cs.maskSequenceToHighQualityRemove();
				}
				cs.setAllowJump(this.allowJump);
				
				if(!cs.getRemarks().isEmpty()) {
					badQual.getAndIncrement();
					flaggedAsWrong = true;
				}
				boolean hasN = cs.checkContainsN();
				if(hasN) {
					if(!flaggedAsWrong) {
						containsN.getAndIncrement();
					}
					flaggedAsWrong = true;
				}
				boolean leftCorrect = false;
				boolean rightCorrect = false;
				//at this point has to be true because of earlier check
				if(cs.isMasked()) {
					//check if exists in cache
					String lookupDoneKey = lookupDone.get(cs.getQuery());
					if(lookupDoneKey != null) {
						countEvents.putOrAdd(lookupDoneKey,cs.getBarcode(),1);
						correct.getAndIncrement();
						cacheHit.getAndIncrement();
						
						//for stats purposes!
						leftCorrect = true;
						rightCorrect = true;
					}
					else {
						//AtomicLong tempProcessFlank = new AtomicLong(System.nanoTime());
						//put here because that makes debugging easier
						cs.setCurrentAlias(alias, f.getName());
						cs.determineFlankPositions(true);
						//processFlank.set(System.nanoTime()-tempProcessFlank.get()+processFlank.get());
						if(subject.isLongRead() && subject.hasPrimers()) {
							leftCorrect = cs.getRaw().indexOf(subject.getLeftPrimerMatchPacBio()) >= 0; 
							rightCorrect = cs.getRaw().indexOf(subject.getRightPrimerMatchPacBio()) >= 0;
						}
						//Illumina
						else {
							leftCorrect = cs.isCorrectPositionLeft();
							rightCorrect = cs.isCorrectPositionRight();
						}
						if(leftCorrect && rightCorrect) {
							correctPositionFRassembled.getAndIncrement();
						}
						else {
							if(!leftCorrect && rightCorrect) {
								wrongPositionL.getAndIncrement();
							}
							if(!rightCorrect && leftCorrect) {
								wrongPositionR.getAndIncrement();
								//System.out.println(cs.toStringOneLine());
							}
							wrongPosition.getAndIncrement();
						}
						boolean isHDRevent = false;
						CompareSequence hdr = subject.getHDREvent(cs);
						//isHDR?
						if(hdr != null) {
							cs.resetRemarks();
							isHDRevent = true;
							//set the HDR name
							cs.setHDRName(hdr.getName());
							//actually we need to check this via the HDR sequence rather than like this...
							//leftCorrect = cs.getRaw().startsWith(subject.getLeftPrimer());
							//rightCorrect = cs.getRaw().endsWith(subject.getRightPrimer());
						}
						if(cs.getRemarks().isEmpty()) {
							if(leftCorrect && rightCorrect) {
								cs.setAdditionalSearchString(hmAdditional);
								cs.setCurrentFile(f);
								//now we now if we need to reverse or not
								if(!reverseDecisionMade.get()) {
									//set the takeRC always, because the single files might mess up the decision
									if(cs.isReversed()) {
										takeRc.set(true);
									}
									else {
										takeRc.set(false);
									}
									reverseDecisionMade.set(true);
								}
								
							}
							else {
								if(cs.getRemarks().length()>0) {
									String rs = cs.getRemarks();
									if(remarks.containsKey(rs)) {
										remarks.put(rs, remarks.get(rs)+1);
									}
									else {
										remarks.put(rs, 1);
									}
								}
								//trick to look at the wrong events
								//you have to disable the cache!!!
								//disable for now
								/*
								else {
									cs.setCurrentFile(f);
									cs.setCurrentAlias(alias+" "+leftCorrect+" "+rightCorrect, f.getName());
									leftCorrect = true;
									rightCorrect = true;
								}
								*/
							}
						}
						//only correctly found ones
						if(cs.getRemarks().isEmpty() && leftCorrect && rightCorrect){
							correct.getAndIncrement();
							String key = cs.getKey(includeStartEnd);
							//this is for the cache
							
							lookupDone.put(cs.getQuery(),key);
							//now really count the event
							Integer countEventsNr = countEvents.get(key, cs.getBarcode());
							if(countEventsNr!=null){
								countEvents.putOrAdd(key, cs.getBarcode(), 1);
								//while this works, it might be slow and/or incorrect!
								//the best would be to give the majority here
								//replaced not so great sequences with more accurate ones
								//to be able to filter better later
								//20180731, added match positions as now sometimes shorter events are selected
								//which causes problems with filters later
								if(cs.getNrXs()<csEvents.get(key).getNrXs() && cs.getMatchStart()<= csEvents.get(key).getMatchStart() && cs.getMatchEnd() >=csEvents.get(key).getMatchEnd()  ) {
									csEvents.put(key, cs);
								}
								//can also be done if matchPositions are smaller
								else if(cs.getNrXs()==csEvents.get(key).getNrXs() && (cs.getMatchStart()< csEvents.get(key).getMatchStart() || cs.getMatchEnd() >csEvents.get(key).getMatchEnd())) {
									csEvents.put(key, cs);
								}
							}
							else{
								countEvents.putOrAdd(key,cs.getBarcode(), 1);
								//save the object instead
								csEvents.put(key, cs);
							}
						}
					}
				}
				
				if(cs.getRemarks().isEmpty() && leftCorrect && rightCorrect){
					//System.out.println("CORRECT "+checkReverse);
					//System.out.println("correct\t"+cs.toStringOneLine("dum"));
					//if(cs.getBarcode().contentEquals("RVsg_mmPolq-2")) {
						//System.out.println("correct\t"+cs.toStringOneLine());
					//}
				}
				else {
					//System.out.println("incorrect "+checkReverse);
					//if(cs.getBarcode().contentEquals("RVsg_mmPolq-2")) {
					//System.out.println("incorrect\t"+cs.toStringOneLine("dum"));
					//System.out.println(leftCorrect+"\t"+rightCorrect);
					//}
					//System.out.println("incorrect\t"+cs.toStringOneLine("dum"));
					//System.out.println(subject.isHDREvent(cs));
				}
				if(!(cs.getRemarks().isEmpty() && leftCorrect && rightCorrect)){
					wrong.getAndIncrement();
					//collect these
					if(commonSequences.containsKey(cs.getRaw())) {
						commonSequences.put(cs.getRaw(),commonSequences.get(cs.getRaw())+1);
					}
					else {
						commonSequences.put(cs.getRaw(),1);
						//also save the remark!
						//we can also add the remark for the next sequences
						commonSequencesRemarks.put(cs.getRaw(), cs.getRemarks());
					}
				}
				//System.out.println(cs.toStringOneLine());
				//no masking
				counter.getAndIncrement();
				//has GUI
				//are slooooow, so update more often
				if(subject.isLongRead() && this.tableModel!=null) {
					long end = System.nanoTime();
					long duration = TimeUnit.MILLISECONDS.convert((end-start.get()), TimeUnit.NANOSECONDS);
					if(duration>1000) {
						float perc = counter.get()/(float)totalReads.get();
						this.tableModel.setStatus(ngs, perc);
						this.tableModel.setTotal(ngs, counter.get());
						this.tableModel.setCorrect(ngs, correct.get());
						this.tableModel.setPercentage(ngs, correct.get()/(float)counter.get());
						//reset timer
						start.set(end);
					}
				}
				if(counter.get()%10000==0){
					long end = System.nanoTime();
					long duration = TimeUnit.MILLISECONDS.convert((end-start.get()), TimeUnit.NANOSECONDS);
					start.set(end);
					System.out.println("Thread: "+Thread.currentThread().getName()+" processed "+counter+" reads, costed (milliseconds): "+duration+" correct: "+correct+" wrong: "+wrong+" wrongPosition: "+wrongPosition+ " correct fraction: "+(correct.get()/(float)counter.get())+" cacheHit: "+cacheHit);
					//should be set based on memory usage rather
					//TODO
					if(lookupDone.size()>10000) {
						System.out.println("Clearing cache");
						lookupDone.clear();
					}
					//update GUI stuff if applicable
					if(this.tableModel!= null) {
						float perc = counter.get()/(float)totalReads.get();
						this.tableModel.setStatus(ngs, perc);
						this.tableModel.setTotal(ngs, counter.get());
						this.tableModel.setCorrect(ngs, correct.get());
						this.tableModel.setPercentage(ngs, correct.get()/(float)counter.get());
					}
				}
				if(maxReads>0 && counter.get()>= this.maxReads) {
					System.out.println(counter.get()+" >= "+this.maxReads);
					//update the table model
					if(this.tableModel!= null) {
						float perc = counter.get()/(float)totalReads.get();
						this.tableModel.setStatus(ngs, perc);
						this.tableModel.setTotal(ngs, counter.get());
						this.tableModel.setCorrect(ngs, correct.get());
						this.tableModel.setPercentage(ngs, correct.get()/(float)counter.get());
						this.tableModel.setTextStatus(ngs,"Writing");
					}
					throw new BreakException();
				}
				//forced stop
				if(this.isInterrupted()) {
					System.out.println("INTERUPTED");
					throw new BreakException();
				}
			});
			//final update
			if(this.tableModel!= null) {
				float perc = counter.get()/(float)totalReads.get();
				this.tableModel.setStatus(ngs, perc);
				this.tableModel.setTotal(ngs, counter.get());
				this.tableModel.setCorrect(ngs, correct.get());
				this.tableModel.setPercentage(ngs, correct.get()/(float)counter.get());
				this.tableModel.setTextStatus(ngs,"Writing");
			}
			
			
		} catch (IOException | RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BreakException e) {
			//this is just to escape the lambda expression
			//e.printStackTrace();
		}
			
		if(writer != null){
			if(collapseEvents){
				//get the total
				HashMap<String, Integer> totals = new HashMap<String, Integer>();
				for(String key: csEvents.keySet()){
					ArrayList<String> barcodes = countEvents.getBarcodes(key);
					//System.out.println("Requesting "+key);
					for(String barcode: barcodes) {
						if(countEvents.get(key, barcode)>=minimalCount) {
							if(!totals.containsKey(barcode)) {
								totals.put(barcode,0);
							}
							totals.put(barcode,totals.get(barcode)+countEvents.get(key,barcode));
						}
					}
				}
				//System.out.println("Writing events "+csEvents.size()+" to: "+output.getAbsolutePath());
				//NO NOT HERE
				//writer.println("countEvents\t"+CompareSequence.getOneLineHeader());
				int count = 0;
				if(printHeader) {
					writer.println("countEvents\tfraction\t"+CompareSequence.getOneLineHeader());
				}
				for(String key: csEvents.keySet()){
					//only output if we saw it minimalCount times
					ArrayList<String> barcodes = countEvents.getBarcodes(key);
					CompareSequence cs = csEvents.get(key);
					cs.setTINSSearchDistance(this.tinsDistValue);
					String eventString = null;
					for(String barcode: barcodes) {
						if(countEvents.get(key, barcode)>=minimalCount) {
							//get the Event String
							if(eventString == null) {
								eventString = cs.toStringOneLine("<DUMMY>").replace("\n", "_");
							}
							double total = 0;
							if(totals.containsKey(barcode)) {
								total = totals.get(barcode);
							}
							double fraction = countEvents.get(key,barcode)/total;
							//overwrite barcode here
							String tempEventString = eventString.replaceFirst("<DUMMY>", barcode);
							tempEventString = tempEventString.replaceFirst("<DUMMY>", getGene(barcode));
							tempEventString = tempEventString.replaceAll("<DUMMY>", barcode);
							writer.println(countEvents.get(key,barcode)+"\t"+fraction+"\t"+tempEventString);
							count++;
						}
					}
				}
				//setDir, Alias
				String refString = subject.getRefString();
				CompareSequence ref = new CompareSequence(subject,refString,null, f.getParentFile().getName(), true, "wt_query");
				ref.setCurrentAlias(alias, f.getName());
				ref.setCurrentFile(f);
				ref.determineFlankPositions(false);
				String outputString = ref.toStringOneLine("dummy");
				writer.println(0+"\t"+0+"\t"+outputString);
				System.out.println("Written "+count+" events to: "+output.getAbsolutePath());
			}
			writer.close();
		}
		if(writerStats!= null) {
			double correctFraction = correct.get()/(double)totalRawReadsCounter.get();
			double correctFractionMerged = correct.get()/(double)counter.get();
			writerStats.println("Alias\tFile\tSubject\tType\tReads");
			writerStats.println(alias+"\t"+f.getName()+"\t"+subject.getSubjectName()+"\tTotalReads\t"+totalRawReadsCounter);
			writerStats.println(alias+"\t"+f.getName()+"\t"+subject.getSubjectName()+"\tMergedReads\t"+counter);
			writerStats.println(alias+"\t"+f.getName()+"\t"+subject.getSubjectName()+"\tMergedCorrect\t"+correct);
			writerStats.println(alias+"\t"+f.getName()+"\t"+subject.getSubjectName()+"\tCorrectFractionTotal\t"+correctFraction);
			writerStats.println(alias+"\t"+f.getName()+"\t"+subject.getSubjectName()+"\tCorrectFractionMerged\t"+correctFractionMerged);
			writerStats.println(alias+"\t"+f.getName()+"\t"+subject.getSubjectName()+"\tMergedButWrong\t"+wrong);
			writerStats.println(alias+"\t"+f.getName()+"\t"+subject.getSubjectName()+"\tMergedButWrongPositionTotal\t"+wrongPosition);
			writerStats.println(alias+"\t"+f.getName()+"\t"+subject.getSubjectName()+"\tMergedButWrongPositionL\t"+wrongPositionL);
			writerStats.println(alias+"\t"+f.getName()+"\t"+subject.getSubjectName()+"\tMergedButWrongPositionR\t"+wrongPositionR);
			if(singleFileF != null && singleFileR != null) {
				writerStats.println(alias+"\t"+f.getName()+"\t"+subject.getSubjectName()+"\tUnmergedCorrectPositionFR\t"+correctPositionFR);
			}
			writerStats.println(alias+"\t"+f.getName()+"\t"+subject.getSubjectName()+"\tMergedCorrectPositionFR\t"+correctPositionFRassembled);
			
			writerStats.println(alias+"\t"+f.getName()+"\t"+subject.getSubjectName()+"\tMergedBadQual\t"+badQual);
			writerStats.println(alias+"\t"+f.getName()+"\t"+subject.getSubjectName()+"\tMergedcontainsN\t"+containsN);
			
			
			for(String key: remarks.keySet()) {
				writerStats.println(alias+"\t"+f.getName()+"\t"+subject.getSubjectName()+"\t"+key+"\t"+remarks.get(key));
			}
			writerStats.close();
			System.out.println("Written stats to: "+outputStats.getAbsolutePath());
		}
		if(writerTopStats !=null) {
			Object[] a = commonSequences.entrySet().toArray();
			Arrays.sort(a, new Comparator() {
			    public int compare(Object o1, Object o2) {
			        return ((Map.Entry<String, Integer>) o2).getValue()
			                   .compareTo(((Map.Entry<String, Integer>) o1).getValue());
			    }
			});
			int max = Math.min(100,commonSequences.size());
			writerTopStats.println("File\tExactReadFound\tReadKeyFound\tReads\tfractionOfReads\tleftPrimerCorrect\trightPrimerCorrect\tSeq\t"+CompareSequence.getOneLineHeader());
			for (int i=0;i<max;i++) {
				Object e = a[i];
				String seq = ((Map.Entry<String, Integer>) e).getKey();
				int times = ((Map.Entry<String, Integer>) e).getValue();
				//only output reads that have been seen often enough
				if(times>=minimalCount) {
					double fractionTotal = times/(double)counter.get();
					boolean found = false;
					//check if this read is called
					for(String key: csEvents.keySet()){
						//only output if we saw it minimalCount times
						ArrayList<String> barcodes = countEvents.getBarcodes(key);
						for(String barcode: barcodes) {
							boolean breakLoop = false; 
							if(countEvents.get(key, barcode)>=minimalCount) {
								CompareSequence cs = csEvents.get(key);
								if(cs.getRaw().contentEquals(seq)) {
									found = true;
									breakLoop = true;
									if(breakLoop) {
										break;
									}
								}
							}
							if(breakLoop) {
								break;
							}
						}
					}
					CompareSequence cs = new CompareSequence(subject, seq, null, f.getParentFile().getName(), true, "top "+(i+1));
					cs.determineFlankPositions(true);
					cs.setCurrentFile(f);
					cs.setCurrentAlias(alias, f.getName());
					//fill in the original remark
					if(cs.getRemarks().length()==0) {
						cs.setRemarks(commonSequencesRemarks.get(seq));
					}
					writerTopStats.println(alias+"\t"+found+"\t"+csEvents.containsKey(cs.getKey(includeStartEnd))+"\t"+times+"\t"+fractionTotal+"\t"+cs.isCorrectPositionLeft()+"\t"+cs.isCorrectPositionLeft()+"\t"+seq+"\t"+cs.toStringOneLine(""));
				}
			}
			writerTopStats.close();
			System.out.println("Written writerTopStats to: "+outputTopStats.getAbsolutePath());
		}
		if(this.tableModel!= null) {
			tableModel.setTextStatus(ngs,"Done");
		}
		
		long end = System.nanoTime();
		long duration = TimeUnit.SECONDS.convert((end-realStart), TimeUnit.NANOSECONDS);
		System.out.println("Thread: "+Thread.currentThread().getName()+" duration "+duration+" seconds");
	}
	/*
	private ArrayList<SetAndPosition> createSetAndPosition() {
		if(leftPrimer == null || leftPrimer.length()==0) {
			return null;
		}
		if(rightPrimer == null || rightPrimer.length()==0) {
			return null;
		}
		int leftPos = subject.seqString().indexOf(leftPrimer);
		int rightPos = subject.seqString().indexOf(Utils.reverseComplement(rightPrimer));
		if(leftPos == -1 || rightPos == -1) {
			//swap
			String primer = leftPrimer;
			leftPrimer = rightPrimer;
			rightPrimer = primer;
			leftPos = subject.seqString().indexOf(leftPrimer);
			rightPos = subject.seqString().indexOf(Utils.reverseComplement(rightPrimer));
			System.out.println(leftPos);
			System.out.println(rightPos);
			if(leftPos == -1 || rightPos == -1) {
				System.err.println("Cannot find primers!");
				System.err.println(subject.seqString());
				System.err.println(leftPrimer);
				System.err.println(rightPrimer);
				System.err.println(leftPos);
				System.err.println(rightPos);
				System.exit(0);
			}
			//this.takeRC  = true;
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
	*/

	private String getGene(String barcode) {
		int index = barcode.lastIndexOf("-");
		if(index>0) {
			return barcode.substring(0, index);
		}
		return barcode;
	}
	public void setMinimalCount(long minimalCount) {
		this.minimalCount = minimalCount;
	}
	public void setCollapseStartEnd(boolean includeStartEnd) {
		this.includeStartEnd = includeStartEnd;
	}

	public void setMaximumReads(long maxReads) {
		System.out.println("Setting maxReads to "+maxReads);
		this.maxReads = maxReads;
	}

	public void printHeader() {
		this.printHeader = true;
		
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setAllowJump(boolean allowJump) {
		this.allowJump = allowJump;
	}

	public void setFileF(File singleFileF) {
		this.singleFileF = singleFileF;
	}

	public void setFileR(File singleFileR2) {
		this.singleFileR = singleFileR2;
	}
	public void setTableModel(NGSTableModel m) {
		this.tableModel = m;
		
	}
	public void setNGS(NGS n) {
		this.ngs = n;
		if(ngs!=null) {
			if(ngs.getR2()!=null) { 
				//could still be not present
				if(ngs.getUnassembledFFileDerived()!=null && ngs.getUnassembledRFileDerived()!=null) {
					this.setFileF(ngs.getUnassembledFFileDerived());
					this.setFileR(ngs.getUnassembledRFileDerived());
				}
			}
		}
	}
	private void assembleFile() {
		runFlash();
	}
	private void runFlash(){
		//test for blast
		try {
			File f = new File(flashExec);
			System.out.println("Flash file "+f.getAbsolutePath()+ " exists");
			System.out.println("Flash file "+f.canExecute()+ " <= can execute");
			ArrayList<String> commandParts = new ArrayList<String>();
			commandParts.add(flashExec);
			commandParts.add(ngs.getR1().getAbsolutePath());
			commandParts.add(ngs.getR2().getAbsolutePath());
			commandParts.add("-M");
			commandParts.add("5000");
			commandParts.add("-O");
			commandParts.add("-x");
			commandParts.add("0");
			commandParts.add("-z");
			commandParts.add("-t");
			commandParts.add(""+cpus);
			commandParts.add("-o");
			commandParts.add(ngs.getAssembledFileDerived().getName());
			
			//String execTotal = "'"+flashExec+ "'  \""+ngs.getR1()+"\" \""+ngs.getR2()+"\" -M 5000 -O -x 0 -z -t "+this.cpus+;
			//String execTotal = flashExec+ " "+ngs.getR1()+" "+ngs.getR2()+" -r 300 -M 5000 -O -z -t "+this.cpus+" -o "+ngs.getAssembledFileDerived().getName();
			System.out.println(String.join(" ", commandParts));
			//Process p = Runtime.getRuntime().exec(commandParts);
			Process p = new ProcessBuilder(commandParts).start();
			//Process p = Runtime.getRuntime().exec("ping");
			// any error message?get
            StreamGobbler errorGobbler = new 
                StreamGobbler(p.getErrorStream(), "ERROR", false);            
            
            // any output?
            StreamGobbler outputGobbler = new 
                StreamGobbler(p.getInputStream(), "OUTPUT", false);
                
            // kick them off
            errorGobbler.start();
            outputGobbler.start();
            int exitVal = p.waitFor();
            //flash outputs in a strange file
            String currentDir = System.getProperty("user.dir");
            File flashOutput = new File(currentDir+File.separator+ngs.getAssembledFileDerived().getName()+".extendedFrags.fastq.gz");
            File flashOutputunassF = new File(currentDir+File.separator+ngs.getAssembledFileDerived().getName()+".notCombined_1.fastq.gz");
            File flashOutputunassR = new File(currentDir+File.separator+ngs.getAssembledFileDerived().getName()+".notCombined_2.fastq.gz");
            
            System.out.println("File "+flashOutput.getAbsolutePath());
            if(flashOutput.exists()) {
            	if(ngs.getAssembledFileDerived().exists()) {
            		ngs.getAssembledFileDerived().delete();
            	}
            	flashOutput.renameTo(ngs.getAssembledFileDerived());
            }
            else {
            	System.err.println("Something went wrong with the assembly");
            }
            if(flashOutputunassF.exists()) {
            	if(ngs.getUnassembledFFileDerived().exists()) {
            		ngs.getUnassembledFFileDerived().delete();
            	}
            	flashOutputunassF.renameTo(ngs.getUnassembledFFileDerived());
            }
            else {
            	System.err.println("Something went wrong with the assembly "+flashOutputunassF.getName());
            	System.err.println(flashOutputunassF.getAbsolutePath());
            }
            if(flashOutputunassR.exists()) {
            	if(ngs.getUnassembledRFileDerived().exists()) {
            		ngs.getUnassembledRFileDerived().delete();
            	}
            	flashOutputunassR.renameTo(ngs.getUnassembledRFileDerived());
            }
            else {
            	System.err.println("Something went wrong with the assembly "+flashOutputunassR.getName());
            	System.err.println(flashOutputunassR.getAbsolutePath());
            }
            ArrayList<File> toDelete = new ArrayList<File>();
            File hist = new File(currentDir+File.separator+ngs.getAssembledFileDerived().getName()+".hist");
            File histIn = new File(currentDir+File.separator+ngs.getAssembledFileDerived().getName()+".hist.innie");
            File histOut = new File(currentDir+File.separator+ngs.getAssembledFileDerived().getName()+".hist.outie");
            File histogram = new File(currentDir+File.separator+ngs.getAssembledFileDerived().getName()+".histogram");
            File histogramIn = new File(currentDir+File.separator+ngs.getAssembledFileDerived().getName()+".histogram.innie");
            File histogramOut = new File(currentDir+File.separator+ngs.getAssembledFileDerived().getName()+".histogram.outie");
            
            //Remove this unmoved files
            toDelete.add(hist);
            toDelete.add(histIn);
            toDelete.add(histOut);
            toDelete.add(histogram);
            toDelete.add(histogramIn);
            toDelete.add(histogramOut);
            for(File deleteFile: toDelete) {
            	if(deleteFile.exists()) {
            		deleteFile.delete();
            	}
            }
            
            
            
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void setFlash(String flashExec, int cpus) {
		this.flashExec = flashExec;
		this.cpus = cpus;
		
	}
	public void setSemaphore(Semaphore mySemaphore) {
		this.semaphore = mySemaphore;
		
	}
	public void setTinsDistance(int tinsDistValue) {
		this.tinsDistValue = tinsDistValue;
	}
	public void setHDR(File hdr) {
		if(hdr!=null) {
			try {
				BufferedReader is = new BufferedReader(new FileReader(hdr));
				RichSequenceIterator si = IOTools.readFastaDNA(is, null);
				//add all HDR events here
				while(si.hasNext()) {
					subject.addHDR(si.nextRichSequence());
				}
				
			} catch (FileNotFoundException | NoSuchElementException | BioException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

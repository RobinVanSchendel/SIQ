package utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.biojavax.bio.seq.RichSequence;
import org.jcvi.jillion.core.datastore.DataStoreProviderHint;
import org.jcvi.jillion.core.qual.QualitySequence;
import org.jcvi.jillion.core.util.iter.StreamingIterator;
import org.jcvi.jillion.trace.fastq.FastqDataStore;
import org.jcvi.jillion.trace.fastq.FastqFileDataStoreBuilder;
import org.jcvi.jillion.trace.fastq.FastqFileReader;
import org.jcvi.jillion.trace.fastq.FastqQualityCodec;
import org.jcvi.jillion.trace.fastq.FastqRecord;

import dnaanalysis.Blast;
import dnaanalysis.Utils;
import gui.NGS;
import gui.NGSTableModel;

public class SequenceFileThread extends Thread {

	private File f, output, outputStats;
	private boolean writeToOutput;
	private Subject subject;
	private HashMap<String, Integer> countEvents = new HashMap<String, Integer>();
	private HashMap<String, String> actualEvents = new HashMap<String, String>();
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
	
	//private boolean takeRC = false;
	
	public SequenceFileThread(File f, boolean writeToOutput, Subject subject, File output, File statsOutput, double maxError, HashMap<String, String> additional){
		this.f = f;
		this.writeToOutput = writeToOutput;
		this.subject = subject;
		this.output = output;
		this.outputStats = statsOutput;
		this.maxError = maxError;
		this.hmAdditional = additional;
	}
	private void setCheckReverseOverwrite() {
		checkReverseOverwrite = true;
	}
	
	@Override
	public void run() {
		try {
			semaphore.acquire();
			runReal();
			semaphore.release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void runReal() {
		Thread.currentThread().setName(alias);
		boolean printOnlyIsParts = false;
		boolean collapseEvents = collapse;
		PrintWriter writer = null, writerStats = null;
		String type = "";
		//reset NGSTable if needed
		if(this.tableModel!= null) {
			this.tableModel.setStatus(ngs, 0);
			this.tableModel.setTotal(ngs, 0);
			this.tableModel.setCorrect(ngs, 0);
			this.tableModel.setPercentage(ngs, 0);
		}
		
		
		//Check if you need to assemble anything
		if(!f.exists()) {
			if(ngs!=null) {
				System.out.println("Assembling the file via R1 R2");
				tableModel.setStatus(ngs,-1);
				assembleFile();
				tableModel.setStatus(ngs, 0);
			}
		}
		
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
		
		try {
			//first check the non-assembled reads
			//FastqFileReader reader = new FastqFileReader()
			if(singleFileF != null && singleFileR != null) {
				FastqDataStore datastoreF = new FastqFileDataStoreBuilder(singleFileF).qualityCodec(FastqQualityCodec.SANGER).hint(DataStoreProviderHint.ITERATION_ONLY).build();
				FastqDataStore datastoreR = new FastqFileDataStoreBuilder(singleFileR).qualityCodec(FastqQualityCodec.SANGER).hint(DataStoreProviderHint.ITERATION_ONLY).build();
				StreamingIterator<FastqRecord> itF = datastoreF.iterator();
				StreamingIterator<FastqRecord> itR = datastoreR.iterator();
				int count = 0;
				boolean first = true;
				while(itF.hasNext() && itR.hasNext()) {
					FastqRecord F = itF.next();
					FastqRecord R = itR.next();
					count++;
					totalRawReadsCounter.getAndIncrement();
					//Forward
					boolean checkReverse = false;
					if(first) {
						checkReverse = true;
						first = false;
					}
					if(checkReverseOverwrite) {
						checkReverse = true;
					}
					CompareSequence cs = new CompareSequence(subject, F.getNucleotideSequence().toString(), F.getQualitySequence(), f.getParentFile().getName(), checkReverse, F.getId());
					if(first && cs.isReversed()) {
						takeRc.set(true);
						first = false;
					}
					else if(takeRc.get()) {
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
							cs = new CompareSequence(subject, R.getNucleotideSequence().toString(), R.getQualitySequence(), f.getParentFile().getName(), checkReverse, R.getId());
							//bug, needs to be opposite orientation of forward read
							if(!takeRc.get()) {
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
							}
							else {
								//System.out.println("not correct "+leftCorrect+":"+rightCorrect+" - "+Thread.activeCount());
							}
						}
					}
					if(count>=maxReads) {
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
				System.out.println("CorrectPositions:\t"+correctPositionFR);
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
			}
			FastqFileReader.forEach( f, FastqQualityCodec.SANGER, 
			        (id, fastqRecord) -> {
			    totalRawReadsCounter.getAndIncrement();
				QualitySequence quals = fastqRecord.getQualitySequence();
				
				String seq = fastqRecord.getNucleotideSequence().toString();
				boolean checkReverse = false;
				if(counter.get()==0) {
					checkReverse = true;
				}
				else {
					checkReverse = false;
				}
				//in some cases can be overwritten
				if(checkReverseOverwrite) {
					checkReverse = true;
				}
				CompareSequence cs = new CompareSequence(subject, seq, quals, f.getParentFile().getName(), checkReverse, id);
				if(counter.get()==0 && cs.isReversed()) {
					takeRc.set(true);
				}
				else if(takeRc.get()) {
					cs.reverseRead();
				}
				cs.setAndDetermineCorrectRange(maxError);
				cs.maskSequenceToHighQualityRemoveSingleRange();
				cs.setAllowJump(this.allowJump);
				
				if(!cs.getRemarks().isEmpty()) {
					badQual.getAndIncrement();
				}
				boolean hasN = cs.checkContainsN();
				if(hasN) {
					containsN.getAndIncrement();
				}
				boolean leftCorrect = false;
				boolean rightCorrect = false;
				//at this point has to be true because of earlier check
				if(cs.isMasked()) {
					//check if exists in cache
					String lookupDoneKey = lookupDone.get(cs.getQuery());
					if(lookupDoneKey != null) {
						//String key = lookupDone.get(cs.getQuery());
						countEvents.put(lookupDoneKey, countEvents.get(lookupDoneKey)+1);
						correct.getAndIncrement();
						cacheHit.getAndIncrement();
						
						//for stats purposes!
						leftCorrect = true;
						rightCorrect = true;
					}
					else {
						//AtomicLong tempProcessFlank = new AtomicLong(System.nanoTime());
						cs.determineFlankPositions(true);
						//processFlank.set(System.nanoTime()-tempProcessFlank.get()+processFlank.get());
						leftCorrect = cs.isCorrectPositionLeft();
						rightCorrect = cs.isCorrectPositionRight();
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
							//System.out.println(cs.toStringOneLine());
							
						}
						if(cs.getRemarks().isEmpty()) {
							if(leftCorrect && rightCorrect) {
								cs.setAdditionalSearchString(hmAdditional);
								cs.setCurrentFile(f);
								cs.setCurrentAlias(alias, f.getName());
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
							Integer countEventsNr = countEvents.get(key);
							if(countEventsNr!=null){
								countEvents.put(key, countEventsNr+1);
								//while this works, it might be slow and/or incorrect!
								//the best would be to give the majority here
								//replaced not so great sequences with more accurate ones
								//to be able to filter better later
								//20180731, added match positions as now sometimes shorter events are selected
								//which causes problems with filters later
								if(cs.getNrXs()<csEvents.get(key).getNrXs() && cs.getMatchStart()<= csEvents.get(key).getMatchStart() && cs.getMatchEnd() >=csEvents.get(key).getMatchEnd()  ) {
									csEvents.put(key, cs);
								}
							}
							else{
								countEvents.put(key, 1);
								//save the object instead
								csEvents.put(key, cs);
							}
						}
					}
				}
				
				if(!(cs.getRemarks().isEmpty() && leftCorrect && rightCorrect)){
					wrong.getAndIncrement();
				}
				//System.out.println(cs.toStringOneLine());
				//no masking
				counter.getAndIncrement();
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
				double total = 0;
				for(String key: csEvents.keySet()){
					if(countEvents.get(key)>=minimalCount) {
						total += countEvents.get(key);
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
					if(countEvents.get(key)>=minimalCount) {
						double fraction = countEvents.get(key)/total;
						CompareSequence cs = csEvents.get(key);
						cs.setTINSSearchDistance(this.tinsDistValue);
						writer.println(countEvents.get(key)+"\t"+fraction+"\t"+csEvents.get(key).toStringOneLine().replace("\n", "_"));
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
			double correctFraction = correct.get()/(double)totalRawReadsCounter.get();
			double correctFractionMerged = correct.get()/(double)counter.get();
			writerStats.println(f.getName()+"\tTotalReads\t"+totalRawReadsCounter);
			writerStats.println(f.getName()+"\tMergedReads\t"+counter);
			writerStats.println(f.getName()+"\tMergedCorrect\t"+correct);
			writerStats.println(f.getName()+"\tCorrectFractionTotal\t"+correctFraction);
			writerStats.println(f.getName()+"\tCorrectFractionMerged\t"+correctFractionMerged);
			writerStats.println(f.getName()+"\tMergedButWrong\t"+wrong);
			writerStats.println(f.getName()+"\tMergedButWrongPositionTotal\t"+wrongPosition);
			writerStats.println(f.getName()+"\tMergedButWrongPositionL\t"+wrongPositionL);
			writerStats.println(f.getName()+"\tMergedButWrongPositionR\t"+wrongPositionR);
			if(singleFileF != null && singleFileR != null) {
				writerStats.println(f.getName()+"\tUnmergedCorrectPositionFR\t"+correctPositionFR);
			}
			writerStats.println(f.getName()+"\tMergedCorrectPositionFR\t"+correctPositionFRassembled);
			
			writerStats.println(f.getName()+"\tMergedBadQual\t"+badQual);
			writerStats.println(f.getName()+"\tMergedcontainsN\t"+containsN);
			
			
			for(String key: remarks.keySet()) {
				writerStats.println(f.getName()+"\t"+key+"\t"+remarks.get(key));
			}
			writerStats.close();
			System.out.println("Written stats to: "+outputStats.getAbsolutePath());
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
	public static int countLinesNew(File f2) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(f2));
	    try {
	        byte[] c = new byte[1024];

	        int readChars = is.read(c);
	        if (readChars == -1) {
	            // bail out if nothing to read
	            return 0;
	        }

	        // make it easy for the optimizer to tune this loop
	        int count = 0;
	        while (readChars == 1024) {
	            for (int i=0; i<1024;) {
	                if (c[i++] == '\n') {
	                    ++count;
	                }
	            }
	            readChars = is.read(c);
	        }

	        // count remaining characters
	        while (readChars != -1) {
	            System.out.println(readChars);
	            for (int i=0; i<readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	            readChars = is.read(c);
	        }
	        //System.out.println("CountLines is: "+count);
	        return count == 0 ? 1 : count;
	    } finally {
	        is.close();
	    }
	}
	public void setNGS(NGS n) {
		this.ngs = n;
	}
	private void assembleFile() {
		runFlash();
	}
	private void runFlash(){
		//test for blast
		try {
			
			//String execTotal = exec +" -query query.fa -db "+db+" -word_size 18 -outfmt \"6 std qseq sseq\"";
			String execTotal = flashExec+ " \""+ngs.getR1()+"\" \""+ngs.getR2()+"\" -M 5000 -O -x 0 -z -t "+this.cpus+" -o "+ngs.getAssembledFileDerived().getName();
			//String execTotal = flashExec+ " "+ngs.getR1()+" "+ngs.getR2()+" -r 300 -M 5000 -O -z -t "+this.cpus+" -o "+ngs.getAssembledFileDerived().getName();
			System.out.println(execTotal);
			Process p = Runtime.getRuntime().exec(execTotal);
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
            System.out.println(flashOutput.exists());
            if(flashOutput.exists()) {
            	flashOutput.renameTo(ngs.getAssembledFileDerived());
            }
            else {
            	System.err.println("Something went wrong with the assembly");
            }
            if(flashOutputunassF.exists()) {
            	flashOutputunassF.renameTo(ngs.getUnassembledFFileDerived());
            }
            else {
            	System.err.println("Something went wrong with the assembly "+flashOutputunassF.getName());
            	System.err.println(flashOutputunassF.getAbsolutePath());
            }
            if(flashOutputunassR.exists()) {
            	flashOutputunassR.renameTo(ngs.getUnassembledRFileDerived());
            }
            else {
            	System.err.println("Something went wrong with the assembly "+flashOutputunassR.getName());
            	System.err.println(flashOutputunassR.getAbsolutePath());
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
}

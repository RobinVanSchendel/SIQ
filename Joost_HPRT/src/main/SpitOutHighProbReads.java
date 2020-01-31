package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.jcvi.jillion.core.qual.QualitySequence;
import org.jcvi.jillion.trace.fastq.FastqFileReader;
import org.jcvi.jillion.trace.fastq.FastqQualityCodec;
import org.jcvi.jillion.trace.fastq.FastqRecord;
import org.jcvi.jillion.trace.fastq.FastqWriter;
import org.jcvi.jillion.trace.fastq.FastqWriterBuilder;

import dnaanalysis.Utils;

public class SpitOutHighProbReads {
	public final static double maxError = 0.001;

	public static void main(String[] args) {
		
		
		File dir = new File("e:\\Project_Methyl\\");
		
		for(File f: dir.listFiles()) {
			File out = new File(f.getAbsolutePath()+".out");
			File outSeq = new File(f.getAbsolutePath()+".out.seqs");
			if(f.getName().endsWith("assembled.fastq")) {
				AtomicInteger counter = new AtomicInteger(0);
				AtomicInteger correct = new AtomicInteger(0);
				try {
					FastqWriter fqw = new FastqWriterBuilder(out).build();
					BufferedWriter bf = new BufferedWriter(new FileWriter(outSeq));
					boolean isBS = f.getName().contains("_bs_");
					if(isBS) {
						//continue;
					}
					System.out.println(f.getName());
					HashMap<String, Integer> hm = new HashMap<String,Integer>();
					FastqFileReader.forEach( f, FastqQualityCodec.SANGER, 
					    (id, fastqRecord) -> {
					    	QualitySequence quals = fastqRecord.getQualitySequence();
					    	if(quals.getMinQuality().get().getErrorProbability() <= maxError) {
					    		String seq = fastqRecord.getNucleotideSequence().toString();
					    		int start = hasHindIIIAtStart(fastqRecord, isBS);					    		
					    		int end = hasHindIIIAtEnd(fastqRecord, isBS);
					    		//one off because of the HindIII site
					    		if(end>0) {
					    			//end++;
					    		}
					    		if(start>0) {
					    			start +=5;
					    		}
					    		if(start>0 & end>0 && end> start) {
					    			//take the part between the HindIII sites
					    			seq = seq.substring(start, end);
					    			if(!hm.containsKey(seq)) {
					    				hm.put(seq,0);
					    			}
				    				hm.put(seq, hm.get(seq)+1);
					    		}
					    		//System.exit(0);
					    		//fqw.write(fastqRecord);
					    		correct.getAndIncrement();
					    	}
					    	counter.getAndIncrement();
					    	if(counter.get()%100000==0) {
					    		double percCorrect = 100*correct.get()/(double)counter.get();
					    		System.out.println("Already processed "+counter.get()+" records, correct: "+percCorrect+"%");
					    	}
					    });
					//fqw.close();
					for(String key: hm.keySet()) {
						bf.write(f.getName()+"\t"+key+"\t"+hm.get(key)+"\n");
					}
					bf.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (RuntimeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				double percCorrect = 100*correct.get()/(double)counter.get();
				System.out.println("File "+f.getName()+" correct: "+correct.get()+" total: "+counter.get()+" perc: "+percCorrect+"%" );
			}
		}

	}

	private static int hasHindIIIAtStart(FastqRecord fastqRecord, boolean isBS) {
		String seq = fastqRecord.getNucleotideSequence().toString();
		String start = seq.substring(0, 40);
		//System.out.println(start);
		int index = start.indexOf("AGCTT");
		if(index>0) {
			return index;
		}
		if(!isBS) {
			return index;
		}
		//so BS
		int index1 = start.indexOf("AGTTT");
		if(index1>0) {
			return index1;
		}
		int index2 = start.indexOf("AACTT");
		if(index2>0) {
			return index2;
		}
		int index3 = start.indexOf("AATTT");
		if(index3>0) {
			return index3;
		}
		return -1;
	}

	private static int hasHindIIIAtEnd(FastqRecord fastqRecord, boolean isBS) {
		int size = 40;
		String seq = fastqRecord.getNucleotideSequence().toString();
		String start = seq.substring(seq.length()-size);
		//start = Utils.reverseComplement(start);
		//System.out.println(start);
		int index = start.lastIndexOf("AAGCT");
		if(index>0) {
			//System.out.println(index);
			return seq.length()-size+index;
		}
		if(!isBS) {
			//System.out.println(index);
			return -1;
		}
		//so BS
		int index1 = start.lastIndexOf("AAGTT");
		if(index1>0) {
			return seq.length()-size+index1;
		}
		int index2 = start.lastIndexOf("AAACT");
		if(index2>0) {
			return seq.length()-size+index2;
		}
		int index3 = start.lastIndexOf("AAATT");
		if(index3>0) {
			return seq.length()-size+index3;
		}
		return -1;
	}

}

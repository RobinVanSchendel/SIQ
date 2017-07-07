package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojavax.bio.seq.RichSequence;
import org.jcvi.jillion.core.datastore.DataStoreException;
import org.jcvi.jillion.core.datastore.DataStoreProviderHint;
import org.jcvi.jillion.core.qual.QualitySequence;
import org.jcvi.jillion.core.util.iter.StreamingIterator;
import org.jcvi.jillion.trace.fastq.FastqDataStore;
import org.jcvi.jillion.trace.fastq.FastqFileDataStoreBuilder;
import org.jcvi.jillion.trace.fastq.FastqQualityCodec;
import org.jcvi.jillion.trace.fastq.FastqRecord;

public class SequenceFileThread implements Runnable {

	private File f, output;
	private boolean writeToOutput;
	private RichSequence subject;
	private String leftFlank, rightFlank;
	private HashMap<String, Integer> countEvents = new HashMap<String, Integer>();
	private HashMap<String, String> actualEvents = new HashMap<String, String>();
	private HashMap<String, String> colorMap;
	private boolean collapse;
	
	public SequenceFileThread(File f, boolean writeToOutput, RichSequence subject, String leftFlank, String rightFlank, File output, boolean collapse){
		this.f = f;
		this.writeToOutput = writeToOutput;
		this.subject = subject;
		this.leftFlank = leftFlank;
		this.rightFlank = rightFlank;
		this.output = output;
		this.collapse = collapse;
	}
	
	@Override
	public void run() {
		boolean printOnlyIsParts = false;
		boolean collapseEvents = collapse;
		PrintWriter writer = null;
		String type = "";
		if(writeToOutput){
			try {
				writer = new PrintWriter(new FileOutputStream(output,false));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		try {
			FastqDataStore datastore = new FastqFileDataStoreBuilder(f)
			   .qualityCodec(FastqQualityCodec.SANGER)				   
			   .hint(DataStoreProviderHint.ITERATION_ONLY).build();
			//FastqDataStore datastore = new FastqFileDataStoreBuilder(seqs)
			   //.qualityCodec(FastqQualityCodec.ILLUMINA)				   
			   //.hint(DataStoreProviderHint.ITERATION_ONLY);//.build();
			if(writeToOutput){
				System.out.println(f.getName()+"\tnum records = \t" + datastore.getNumberOfRecords());
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
				CompareSequence cs = new CompareSequence(subject, null, query, quals, leftFlank, rightFlank, null, f.getParentFile().getName());
				cs.setAndDetermineCorrectRange(0.05);
				cs.maskSequenceToHighQualityRemove(leftFlank, rightFlank);
				cs.determineFlankPositions();
				//cs.setAdditionalSearchString(additional);
				//cs.setCutType(type);
				cs.setCurrentFile(f.getName());
				//only correctly found ones
				if(cs.getRemarks().length() == 0){
					if(!printOnlyIsParts){
						if(collapseEvents){
							String key = cs.getKey();
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
								System.out.println(type+"\t"+f.getName()+"\t"+s);
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
				if(writer != null && counter%1000==0){
					System.out.println("Already processed "+counter+" reads");
					//iter.close();
					//break;
				}
			}
			
		} catch (IOException | DataStoreException e1) {
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
			writer.close();
		}
	}

}

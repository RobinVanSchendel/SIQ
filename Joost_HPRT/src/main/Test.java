package main;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jcvi.jillion.core.datastore.DataStoreException;
import org.jcvi.jillion.core.datastore.DataStoreProviderHint;
import org.jcvi.jillion.core.util.iter.StreamingIterator;
import org.jcvi.jillion.trace.fastq.FastqDataStore;
import org.jcvi.jillion.trace.fastq.FastqFileDataStoreBuilder;
import org.jcvi.jillion.trace.fastq.FastqFileReader;
import org.jcvi.jillion.trace.fastq.FastqQualityCodec;
import org.jcvi.jillion.trace.fastq.FastqRecord;

import utils.CompareSequence;
import utils.Utils;

public class Test {
	private static HashMap<String, CompareSequence> csEvents = new HashMap<String, CompareSequence>();
	
	public static void main(String[] args) {
		File f = new File("E:\\Project_Hartwig\\414_415_01_S1.assembled.fastq");
		int counter = 0;
		try {
			long start = System.nanoTime();
			AtomicInteger atomicInteger = new AtomicInteger(0);
			FastqFileReader.forEach(f, FastqQualityCodec.SANGER,
					(id, record) -> {
						double qual = record.getAvgQuality().getAsDouble();
						//record.getQualitySequence();
						//System.out.println(java.lang.Thread.activeCount());
						atomicInteger.getAndIncrement();
						csEvents.put("test", null);
			});
			long end = System.nanoTime();
			long duration = end - start;
			
			duration = TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS);
			System.out.println("took Lambda "+duration+" "+atomicInteger.get());
			start = System.nanoTime();
			FastqDataStore datastore = new FastqFileDataStoreBuilder(f)
                    .qualityCodec(FastqQualityCodec.SANGER)
                    .hint(DataStoreProviderHint.ITERATION_ONLY)
                    .build();
			StreamingIterator<FastqRecord> iter = null;
			try {
				iter = datastore.iterator();
			} catch (DataStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			iter.toStream().forEach(s -> {
				s.getAvgQuality();
				double qual = s.getAvgQuality().getAsDouble();
				//System.out.println(java.lang.Thread.activeCount());
			});
			iter.close();
			/*
			while(iter.hasNext()) {
				FastqRecord fq = iter.next();
				double qual = fq.getAvgQuality();
				System.out.println(java.lang.Thread.activeCount());
			}
			*/
			end = System.nanoTime();
			duration = end - start;
			duration = TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS);
			System.out.println("took Store "+duration);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

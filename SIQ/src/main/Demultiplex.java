package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;

import org.jcvi.jillion.core.datastore.DataStoreProviderHint;
import org.jcvi.jillion.core.util.iter.StreamingIterator;
import org.jcvi.jillion.trace.fastq.FastqDataStore;
import org.jcvi.jillion.trace.fastq.FastqFileDataStoreBuilder;
import org.jcvi.jillion.trace.fastq.FastqQualityCodec;
import org.jcvi.jillion.trace.fastq.FastqRecord;
import org.jcvi.jillion.trace.fastq.FastqWriter;
import org.jcvi.jillion.trace.fastq.FastqWriterBuilder;

public class Demultiplex {
	public static String unmatchedTriplet = "XXX";
	
	public static void main(String[] args) {
		
		
		File oligoInfo = new File("Joost_Parts_Original.txt");
		
		
		File dir = new File("E:\\temp\\Ramsden\\\\");
		
		
		ArrayList<PairedEnd> files = getPairedEnd(dir);
		try {
			for(PairedEnd pe: files) {
				System.out.println(pe.toString());
				HashMap<String, File> hmR1 = createHashMap("hashmap.txt", pe.getR1());
				HashMap<String, File> hmR2 = createHashMap("hashmap.txt", pe.getR2());
				HashMap<String, FastqWriter> hmWriterR1 = new HashMap<String, FastqWriter>();
				HashMap<String, FastqWriter> hmWriterR2 = new HashMap<String, FastqWriter>();
				
				for(String key: hmR1.keySet()) {
					System.out.println(key+"\t"+hmR1.get(key));
					hmWriterR1.put(key, new FastqWriterBuilder(hmR1.get(key)).build());
				}
				for(String key: hmR2.keySet()) {
					System.out.println(key+"\t"+hmR2.get(key));
					hmWriterR2.put(key, new FastqWriterBuilder(hmR2.get(key)).build());
				}
				
				FastqDataStore datastoreR1 = new FastqFileDataStoreBuilder(pe.getR1())
						.qualityCodec(FastqQualityCodec.SANGER)
	                    .hint(DataStoreProviderHint.ITERATION_ONLY)
	                    .build();
				FastqDataStore datastoreR2 = new FastqFileDataStoreBuilder(pe.getR2())
						.qualityCodec(FastqQualityCodec.SANGER)
	                    .hint(DataStoreProviderHint.ITERATION_ONLY)
	                    .build();
				
				StreamingIterator<FastqRecord> iterR1 = datastoreR1.iterator();
				StreamingIterator<FastqRecord> iterR2 = datastoreR2.iterator();
				
				int nr = 0;
				while(iterR1.hasNext()) {
					FastqRecord fqR1 = iterR1.next();
					FastqRecord fqR2 = iterR2.next();
					//switch records if needed
					if(fqR2.getNucleotideSequence().toString().contains("AGAACCAATGCATGCGGC")){
						FastqRecord fqRTemp = fqR2;
						fqR2 = fqR1;
						fqR1 = fqRTemp;
						//System.out.println("reversed");
					}
					else {
						//System.out.println("not reversed");
					}
					String nucl = fqR1.getNucleotideSequence().toString();
					String triplet = nucl.substring(6, 9);
					
					//System.out.println(nucl);
					//System.out.println(triplet);
					//System.exit(0);
					if(hmR1.containsKey(triplet)) {
						hmWriterR1.get(triplet).write(fqR1);
						hmWriterR2.get(triplet).write(fqR2);
					}
					//save to unmatched?
					else {
						hmWriterR1.get(unmatchedTriplet).write(fqR1);
						hmWriterR2.get(unmatchedTriplet).write(fqR2);
					}
					nr++;
					if(nr%100000==0) {
						System.out.println("Already processed "+nr+" records");
					}
				}
				for(String key: hmWriterR1.keySet()) {
					hmWriterR1.get(key).close();
					File f = hmR1.get(key);
					compressGZIP(f);
				}
				for(String key: hmWriterR2.keySet()) {
					hmWriterR2.get(key).close();
					File f = hmR2.get(key);
					compressGZIP(f);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static ArrayList<PairedEnd> getPairedEnd(File f) {
		ArrayList<PairedEnd> al = new ArrayList<PairedEnd>();
		if(f.isDirectory()) {
			for(File R1: f.listFiles()) {
				if(R1.getName().endsWith("R1_001.fastq.gz")) {
					String str = R1.getAbsolutePath().replace("R1_001.fastq.gz", "R2_001.fastq.gz");
					File R2 = new File(str);
					if(R2.exists()) {
						PairedEnd pe = new PairedEnd(R1, R2);
						al.add(pe);
					}
				}
				else if(R1.getName().endsWith("_1.fastq")) {
					String str = R1.getAbsolutePath().replace("_1.fastq", "_2.fastq");
					File R2 = new File(str);
					if(R2.exists()) {
						PairedEnd pe = new PairedEnd(R1, R2);
						al.add(pe);
					}
				}
			}
		}
		return al;
		
	}

	private static HashMap<String, File> createHashMap(String string, File file) {
		HashMap<String, File> hm = new HashMap<String, File>();
		try {
			Scanner s = new Scanner(new File(string));
			while(s.hasNextLine()) {
				String line = s.nextLine();
				File outFile = new File(file.getAbsolutePath()+"_"+line+".fastq");
				System.out.println(outFile.getAbsolutePath());
				//FastqWriter fw = new FastqWriterBuilder(outFile).build();
				hm.put(line, outFile);
			}
			//add an unmatched part
			hm.put(unmatchedTriplet, new File(file.getAbsolutePath()+"_"+unmatchedTriplet+".fastq"));
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hm;
	}

	private static ArrayList<File> getGZFiles(File f) {
		ArrayList<File> al = new ArrayList<File>();
		if(f.isDirectory()) {
			for(File file: f.listFiles()) {
				if(file.getName().endsWith(".fastq.gz")) {
					al.add(file);
				}
			}
		}
		return al;
	}
	public static void compressGZIP(File input) throws IOException {
		File output = new File(input.getAbsolutePath()+".gz");
        try (GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(output))){
            try (FileInputStream in = new FileInputStream(input)){
                byte[] buffer = new byte[1024];
                int len;
                while((len=in.read(buffer)) != -1){
                    out.write(buffer, 0, len);
                }
            }
        }
    }

}

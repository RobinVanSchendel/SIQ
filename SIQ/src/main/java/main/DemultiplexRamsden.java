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

public class DemultiplexRamsden {
	public static String unmatchedTriplet = "XXX";

	public static void main(String[] args) {
		
		// TODO Auto-generated method stub
		File dir = new File("E:\\temp\\Ramsden\\\\");
		ArrayList<PairedEnd> files = getPairedEnd(dir);
		try {
			for(PairedEnd pe: files) {
				TriplePrimerCombinations tpc = new TriplePrimerCombinations(new File("hashmap.txt"));
				HashMap<String, FastqWriter> hmWriterR1 = new HashMap<String, FastqWriter>();
				HashMap<String, FastqWriter> hmWriterR2 = new HashMap<String, FastqWriter>();
				
				
				for(TriplePrimer key: tpc.getAll()) {
					System.out.println(key.getCombiBarcode());
					hmWriterR1.put(key.getCombiBarcode(), new FastqWriterBuilder(new File(pe.getR1().getParent()+"\\"+key.getName()+"R1.fastq")).build());
					hmWriterR2.put(key.getCombiBarcode(), new FastqWriterBuilder(new File(pe.getR2().getParent()+"\\"+key.getName()+"R2.fastq")).build());
				}
				//System.exit(0);
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
				int count = 0;
				
				while(iterR1.hasNext() && iterR2.hasNext()) {
					FastqRecord fqR1 = iterR1.next();
					FastqRecord fqR2 = iterR2.next();
					//switch records if needed
					
					for(TriplePrimer tp: tpc.getAll()) {
						if(tp.matches(fqR1, fqR2)) {
							//System.out.println("match "+tp.getCombiBarcode());
							FastqRecord stripped1 = tp.stripBarcode1(fqR1);
							FastqRecord stripped2 = tp.stripBarcode2(fqR2);
							hmWriterR1.get(tp.getCombiBarcode()).write(stripped1);
							hmWriterR2.get(tp.getCombiBarcode()).write(stripped2);
							count++;
							break;
						}
						
					}
					
					nr++;
					if(nr%100000==0) {
						System.out.println("Already processed "+nr+" records");
						System.out.println("Correct "+count);
						//System.exit(0);
						//break;
					}
				
				}
				iterR1.close();
				iterR2.close();
				for(String key: hmWriterR1.keySet()) {
					hmWriterR1.get(key).close();
					//File f = hmR1.get(key);
					//compressGZIP(f);
				}
				for(String key: hmWriterR2.keySet()) {
					hmWriterR2.get(key).close();
					//File f = hmR2.get(key);
					//compressGZIP(f);
				}
				System.out.println("Done");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void addCountToHashMap(HashMap<String, Integer> countr, String R1, String R2) {
		for(String key: countr.keySet()) {
			String[] parts = key.split("_");
			if(R1.startsWith(parts[0]) && R2.startsWith(parts[1])) {
				countr.put(key, countr.get(key)+1);
				//System.out.println("found "+key);
				break;
			}
		}
		
	}

	private static HashMap<String, Integer> createHashMapCodes(String string) {
		HashMap<String, Integer> hm = new HashMap<String, Integer>();
		try {
			Scanner s = new Scanner(new File(string));
			while(s.hasNextLine()) {
				String line = s.nextLine();
				String[] parts = line.split("\t");
				hm.put(parts[1]+"_"+parts[2], 0);
			}
			//add an unmatched part
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hm;
	}

	private static void addToHash(HashMap<String, Integer> tripletCounter, String triplet) {
		if(!tripletCounter.containsKey(triplet)) {
			tripletCounter.put(triplet, 0);
		}
		tripletCounter.put(triplet, tripletCounter.get(triplet)+1);
		
	}

	private static ArrayList<PairedEnd> getPairedEnd(File f) {
		ArrayList<PairedEnd> al = new ArrayList<PairedEnd>();
		if(f.isDirectory()) {
			for(File R1: f.listFiles()) {
				//if(R1.getName().startsWith("SRR11053622_1")) {
					if(R1.getName().endsWith("R1_001.fastq.gz")) {
						String str = R1.getAbsolutePath().replace("R1_001.fastq.gz", "R2_001.fastq.gz");
						File R2 = new File(str);
						if(R2.exists()) {
							PairedEnd pe = new PairedEnd(R1, R2);
							al.add(pe);
						}
					}
					else if(R1.getName().endsWith("R1.fastq")) {
						String str = R1.getAbsolutePath().replace("R1.fastq", "R2.fastq");
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
				//}
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
				String[] parts = line.split("\t");
				File outFile = new File(file.getAbsolutePath()+"_"+parts[0]+".fastq");
				String codes = parts[1]+"_"+parts[2];
				System.out.println(outFile.getAbsolutePath());
				//FastqWriter fw = new FastqWriterBuilder(outFile).build();
				hm.put(codes, outFile);
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

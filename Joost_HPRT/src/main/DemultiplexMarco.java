package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

import org.jcvi.jillion.core.datastore.DataStoreProviderHint;
import org.jcvi.jillion.core.util.iter.StreamingIterator;
import org.jcvi.jillion.trace.fastq.FastqDataStore;
import org.jcvi.jillion.trace.fastq.FastqFileDataStoreBuilder;
import org.jcvi.jillion.trace.fastq.FastqFileReader;
import org.jcvi.jillion.trace.fastq.FastqQualityCodec;
import org.jcvi.jillion.trace.fastq.FastqRecord;
import org.jcvi.jillion.trace.fastq.FastqWriter;
import org.jcvi.jillion.trace.fastq.FastqWriterBuilder;

import dnaanalysis.Utils;

public class DemultiplexMarco {
	public static String unmatchedTriplet = "XXX";

	public static void main(String[] args) {
		
		// TODO Auto-generated method stub
		File dir = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Marco_Signature_Screen");
		File dirOut = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Marco_Signature_Screen\\javaDemultiplex");
		String endOfBarCodeString = "ggtgtttcgtccttt".toUpperCase();
		String startOfBarCodeString = "gcatagctcttaaac".toUpperCase();
		
		ArrayList<PairedEnd> files = getPairedEnd(dir);
		HashMap<String, String> barcodes = createHashMap("screenBarcodes.txt");
		try {
			for(PairedEnd pe: files) {
				if(!pe.getR1().getName().contains("Sample4")) {
					continue;
				}
				//System.out.println(pe.toString());
				//HashMap<String, File> hmR2 = createHashMap("hashmap.txt", pe.getR2());
				HashMap<String, FastqWriter> hmWriterR1 = new HashMap<String, FastqWriter>();
				HashMap<String, FastqWriter> hmWriterR2 = new HashMap<String, FastqWriter>();
				
				/*
				for(String key: hmR1.keySet()) {
					System.out.println(key+"\t"+hmR1.get(key));
					hmWriterR1.put(key, new FastqWriterBuilder(hmR1.get(key)).build());
				}
				for(String key: hmR2.keySet()) {
					System.out.println(key+"\t"+hmR2.get(key));
					hmWriterR2.put(key, new FastqWriterBuilder(hmR2.get(key)).build());
				}
				*/
				
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
				HashMap<String, Integer> barcodeHits = new HashMap<String,Integer>();
				while(iterR1.hasNext()) {
					FastqRecord fqR1 = iterR1.next();
					FastqRecord fqR2 = iterR2.next();
					String R1S = fqR1.getNucleotideSequence().toString();
					String R2S = fqR2.getNucleotideSequence().toString();
					
					//System.out.println(R1S.substring(0, endOfBarCode));
					int start = R2S.indexOf(startOfBarCodeString);
					int end = R2S.indexOf(endOfBarCodeString);
					if(start>=0 && end>=0 && end > start) {
						start += startOfBarCodeString.length();
						//System.out.println(R2S);
						String barcode = R2S.substring(start, end);
						String barcodeAlt = R2S.substring(start, end-1);
						//System.out.println(R2S.substring(start, end));
						if(barcodes.containsKey(barcode) || barcodes.containsKey(barcodeAlt)) {
							String foundBarcode = barcodes.get(barcode);
							if(foundBarcode == null) {
								//reset this when needed
								//foundBarcode = barcodes.get(barcodeAlt);
								foundBarcode = "library";
							}
							if(!barcodeHits.containsKey(foundBarcode)){
								barcodeHits.put(foundBarcode, 0);
							}
							//for(String key: barcodeHits.keySet()) {
							//	System.out.println("key ["+key+"] ["+barcodeHits.get(key)+"]");
							//}
							//System.out.println(barcodeHits.size() +  barcodeHits.get(foundBarcode));
							
							barcodeHits.put(foundBarcode, barcodeHits.get(foundBarcode)+1);
							
							//Write to file
							if(!hmWriterR1.containsKey(foundBarcode)) {
								String fileName = pe.getR1().getName().replace("R1_001.fastq.gz", "");
								hmWriterR1.put(foundBarcode, new FastqWriterBuilder(new File(dirOut.getAbsolutePath()+File.separatorChar+fileName+"_"+foundBarcode+"_R1.fastq")).build());
								hmWriterR2.put(foundBarcode, new FastqWriterBuilder(new File(dirOut.getAbsolutePath()+File.separatorChar+fileName+"_"+foundBarcode+"_R2.fastq")).build());
								//hmWriterR2.put(key.getCombiBarcode(), new FastqWriterBuilder(new File(pe.getR2().getParent()+"\\"+key.getName()+"R2.fastq")).build());
							}
							hmWriterR1.get(foundBarcode).write(fqR1);
							hmWriterR2.get(foundBarcode).write(fqR2);
						}
						else {
							if(!barcodeHits.containsKey(barcode)){
								barcodeHits.put(barcode, 0);
							}
							barcodeHits.put(barcode, barcodeHits.get(barcode)+1);
							//System.out.println(barcodes.containsKey(barcodeAlt));
						}
						
					}
					
					//switch records if needed
					/*
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
					*/
					nr++;
					if(nr%100000==0) {
						System.out.println("Already processed "+nr+" records");
					}
				}
				System.out.println("found "+barcodeHits.size()+" barcodes");
				for(String key: barcodeHits.keySet()) {
					System.out.println(pe.getR2().getName()+"\t"+key+"\t"+barcodeHits.get(key));
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

	private static HashMap<String, String> createHashMap(String string) {
		HashMap<String, String> hm = new HashMap<String, String>();
		System.out.println("Taking column 2 and 1 for barcode and ID");
		try {
			Scanner s = new Scanner(new File(string));
			while(s.hasNextLine()) {
				String line = s.nextLine();
				String[] parts = line.split("\t");
				String revCom = Utils.reverseComplement(parts[1]).toUpperCase();
				//FastqWriter fw = new FastqWriterBuilder(outFile).build();
				String id = parts[0].replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
				hm.put(revCom, id);
			}
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Added "+hm.size()+" barcodes");
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
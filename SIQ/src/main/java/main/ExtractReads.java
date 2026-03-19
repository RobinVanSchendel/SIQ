package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import dnaanalysis.Utils;

public class ExtractReads {

	public static void main(String[] args) {
		
		File dir = new File("E:\\temp\\GenomeScan104896\\");
		String dirOutStr = "E:\\temp\\GenomeScan104896\\";
		File dirOut = new File(dirOutStr);
		Path path = Paths.get(dirOutStr);
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<File> files = getFastq(dir);
		
		
		String newStart = "GCCAGATTTTTCCTCCTCTCCTGACTACTCCCAGTCATAGCTGTCCCTCTTCTCTTATGAAGATCCCTCGACCTGCAGATCGAT".toUpperCase();
		String oldStart = "GCCAGATTTTTCCTCCTCTCCTGACTACTCCCAGTCATAGCTGTCCCTCTTCTCTTATGgAGATCCCTCGACCTGCAGCTCGAG".toUpperCase();
		String newEnd = "GCTAGCAGTCAGCAACCATAGTCCCGCCCCTAACT".toUpperCase();
		String oldEnd = "TCAATTAGTCAGCAACCATAGTCCCGCCCCTAACT".toUpperCase();
				
		for(File f:files) {
			try {
				FastqDataStore datastoreR1 = new FastqFileDataStoreBuilder(f)
						.qualityCodec(FastqQualityCodec.SANGER)
				        .hint(DataStoreProviderHint.ITERATION_ONLY)
				        .build();
				
				File old_old = new File(dirOut.getAbsolutePath()+File.separatorChar+f.getName()+"_old_old_R1.fastq");
				File new_new = new File(dirOut.getAbsolutePath()+File.separatorChar+f.getName()+"_new_new_R1.fastq");
				File new_old = new File(dirOut.getAbsolutePath()+File.separatorChar+f.getName()+"_new_old_R1.fastq");
				File old_new = new File(dirOut.getAbsolutePath()+File.separatorChar+f.getName()+"_old_new_R1.fastq");
				File rest = new File(dirOut.getAbsolutePath()+File.separatorChar+f.getName()+"_rest_R1.fastq");
				FastqWriter old_old_w = new FastqWriterBuilder(old_old).build();
				FastqWriter new_new_w = new FastqWriterBuilder(new_new).build();
				FastqWriter old_new_w = new FastqWriterBuilder(old_new).build();
				FastqWriter new_old_w = new FastqWriterBuilder(new_old).build();
				FastqWriter rest_w = new FastqWriterBuilder(rest).build();
				
				StreamingIterator<FastqRecord> iterR1 = datastoreR1.iterator();
				
				while(iterR1.hasNext()) {
					FastqRecord fqR1 = iterR1.next();
					String seq = fqR1.getNucleotideSequence().toString();
					if(seq.contains(newStart) && seq.contains(newEnd)) {
						new_new_w.write(fqR1);
					}
					else if(seq.contains(newStart) && seq.contains(oldEnd)) {
						new_old_w.write(fqR1);
					}
					else if(seq.contains(oldStart) && seq.contains(oldEnd)) {
						old_old_w.write(fqR1);
					}
					else if(seq.contains(oldStart) && seq.contains(newEnd)) {
						old_new_w.write(fqR1);
					}
					else {
						rest_w.write(fqR1);
					}
				}
				
				iterR1.close();
				new_new_w.close();
				new_old_w.close();
				old_old_w.close();
				old_new_w.close();
				rest_w.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*
		try {
			for(PairedEnd pe: files) {
				if(!pe.getR1().getName().contains("567")) {
					//continue;
				}
				//System.out.println(pe.toString());
				//HashMap<String, File> hmR2 = createHashMap("hashmap.txt", pe.getR2());
				HashMap<String, FastqWriter> hmWriterR1 = new HashMap<String, FastqWriter>();
				HashMap<String, FastqWriter> hmWriterR2 = new HashMap<String, FastqWriter>();
				
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
				int hit = 0;
				int nonhit = 0;
				HashMap<String, Integer> barcodeHits = new HashMap<String,Integer>();
				while(iterR1.hasNext()) {
					FastqRecord fqR1 = iterR1.next();
					FastqRecord fqR2 = iterR2.next();
					//String R1S = fqR1.getNucleotideSequence().toString();
					String R2S = fqR2.getNucleotideSequence().toString();
					
					//System.out.println(R1S.substring(0, endOfBarCode));
					int start = R2S.indexOf(startOfBarCodeString);
					int end = R2S.indexOf(endOfBarCodeString);
					if(start>=0 && end>=0 && end > (start+startOfBarCodeString.length())) {
						start += startOfBarCodeString.length();
						//System.out.println(R2S);
						String barcode = R2S.substring(start, end);
						String barcodeAlt = R2S.substring(start, end-1);
						//System.out.println(barcode+" "+barcodes.containsKey(barcode));
						//System.out.println(barcodeAlt);
						//System.out.println(barcodes.get(barcode));
						//System.out.println(barcodes.get(barcodeAlt));
						//System.out.println();
						//System.out.println(R2S.substring(start, end));
						if(barcodes.containsKey(barcode) || barcodes.containsKey(barcodeAlt)) {
							String foundBarcode = barcodes.get(barcode);
							String correctBarcode = barcode;
							
							if(foundBarcode == null) {
								//reset this when needed
								foundBarcode = barcodes.get(barcodeAlt);
								correctBarcode = barcodeAlt;
								//foundBarcode = "library";
							}
							//overwrite barcode if required
							String barcodeLookup = foundBarcode;
							if(addBarcodeToFQ) {
								foundBarcode = "therecanonlybeone";
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
								fileName = fileName.replace("R1.fastq.gz", "");
								if(write) {
									hmWriterR1.put(foundBarcode, new FastqWriterBuilder(new File(dirOut.getAbsolutePath()+File.separatorChar+fileName+"_"+foundBarcode+"_R1.fastq")).build());
									hmWriterR2.put(foundBarcode, new FastqWriterBuilder(new File(dirOut.getAbsolutePath()+File.separatorChar+fileName+"_"+foundBarcode+"_R2.fastq")).build());
								}
								else {
									hmWriterR1.put(foundBarcode,null);
									hmWriterR2.put(foundBarcode,null);
								}
								//hmWriterR2.put(key.getCombiBarcode(), new FastqWriterBuilder(new File(pe.getR2().getParent()+"\\"+key.getName()+"R2.fastq")).build());
							}
							if(write) {
								if(addBarcodeToFQ) {
									FastqRecord fqTestR1 = fqR1.toBuilder().comment("BC:"+barcodeLookup).build();
									FastqRecord fqTestR2 = fqR2.toBuilder().comment("BC:"+barcodeLookup).build();
									hmWriterR1.get(foundBarcode).write(fqTestR1);
									hmWriterR2.get(foundBarcode).write(fqTestR2);
								}
								else {
									hmWriterR1.get(foundBarcode).write(fqR1);
									hmWriterR2.get(foundBarcode).write(fqR2);
								}
							}
							hit++;
						}
						else {
							if(countNonMatched) {
								if(!barcodeHits.containsKey(barcode)){
									barcodeHits.put(barcode, 0);
								}
								barcodeHits.put(barcode, barcodeHits.get(barcode)+1);
							}
							//System.out.println(barcode+" "+barcodeAlt);
							nonhit++;
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
		/*
					nr++;
					if(nr%100000==0) {
						System.out.println("Already processed "+nr+" records");
						//System.out.println("hit "+hit+" non-hit: "+nonhit);
						//System.out.println(hmWriterR1.size()+" different barcodes");
						//System.exit(0);
						//iterR1.close();
						//iterR2.close();
					}
				}
				//System.out.println("found "+barcodeHits.size()+" barcodes");
				System.out.println(pe.getR2().getName()+"\tWritten\t"+hmWriterR1.size()+"\tfiles");
				for(String key: barcodeHits.keySet()) {
					//System.out.println(pe.getR2().getName()+"\t"+key+"\t"+barcodeHits.get(key));
				}
				iterR1.close();
				iterR2.close();
				if(write) {
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
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

	private static ArrayList<File> getFastq(File dir) {
		ArrayList<File> al = new ArrayList<File>();
		if(dir.isDirectory()) {
			for(File R1: dir.listFiles()) {
				if(R1.getName().endsWith(".fastq")||R1.getName().endsWith(".fastq.gz")) {
					al.add(R1);
				}
			}
		}
		else {
			if(dir.getName().endsWith(".fastq")|| dir.getName().endsWith(".fastq.gz")) {
				al.add(dir);
			}
		}
		return al;
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
				else if(R1.getName().endsWith("R1.fastq.gz")){
					String str = R1.getAbsolutePath().replace("R1.fastq.gz", "R2.fastq.gz");
					File R2 = new File(str);
					String str3 = R1.getAbsolutePath().replace("R1.fastq.gz", "R3.fastq.gz");
					File R3 = new File(str3);
					if(R3.exists()) {
						PairedEnd pe = new PairedEnd(R1, R3);
						al.add(pe);
					}
					else if(R2.exists()) {
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
		int idColumn = 2;
		int sgColumn = 3;
		boolean takeRevCom = false;
		boolean removeAAAC = true;
		try {
			Scanner s = new Scanner(new File(string));
			while(s.hasNextLine()) {
				String line = s.nextLine();
				String[] parts = line.split("\t");
				String revCom = parts[sgColumn].toUpperCase();
				if(takeRevCom) {
					revCom = Utils.reverseComplement(revCom);
				}
				//FastqWriter fw = new FastqWriterBuilder(outFile).build();
				String id = parts[idColumn].replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
				
				if(removeAAAC) {
					revCom = revCom.substring(4);
				}
				//System.out.println("Adding ["+revCom+"] "+revCom.length());
				hm.put(revCom, id);
			}
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
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

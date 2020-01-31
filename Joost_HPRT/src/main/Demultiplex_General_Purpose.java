package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
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

public class Demultiplex_General_Purpose {
	public static String unmatchedTriplet = "XXX";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File dir = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\GUIDEseq_Arabidopsis\\");
		final String UNKNOWNKEY = "Undetermined";
		HashMap<String, File> hm = createHashMap("hashmapCodes.txt",dir, UNKNOWNKEY);
		 
		
		File stat = new File("stats.txt");
		FileWriter fw = null;
		try {
			fw = new FileWriter(stat);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for(String key: hm.keySet()) {
			System.out.println(key+" "+hm.get(key));
		}
		HashMap<String, FastqWriter> hmWriterR1 = createHashMapWriter("R1",hm);
		HashMap<String, FastqWriter> hmWriterR2 = createHashMapWriter("R2",hm);
		
		ArrayList<PairedEnd> files = getPairedEnd(dir);
		try {
			for(PairedEnd pe: files) {
				System.out.println(pe.toString());
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
				HashMap<String, Integer> codes = new HashMap<String, Integer>();
				int nrFound = 0;
				while(iterR1.hasNext()) {
					FastqRecord fqR1 = iterR1.next();
					FastqRecord fqR2 = iterR2.next();
					int index = fqR1.getId().lastIndexOf(":")+1;
					String barcode = fqR1.getId().substring(index);
					if(hmWriterR1.containsKey(barcode)) {
						hmWriterR1.get(barcode).write(fqR1);
						hmWriterR2.get(barcode).write(fqR2);
						nrFound++;
					}
					else {
						hmWriterR1.get(UNKNOWNKEY).write(fqR1);
						hmWriterR2.get(UNKNOWNKEY).write(fqR2);
					}
					if(codes.containsKey(barcode)) {
						codes.put(barcode, codes.get(barcode)+1);
					}
					else {
						codes.put(barcode, 1);
					}
					//System.out.println(fqR1.getComment());
					//System.exit(0);
					
					nr++;
					if(nr%100000==0) {
						System.out.println("Already processed "+nr+" records, matched: "+nrFound);
						//break;
					}
				}
				fw.write("##### Top barcodes are shown here ####\n");
				/*
				 Map<String, Integer> sorted = codes
					        .entrySet()
					        .stream()
					        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
					        .collect(
					            toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2,
					                LinkedHashMap::new));
					                */
				/*
				int count = 0;
				for(String key: sorted.keySet()) {
					System.out.println(key+ " "+sorted.get(key));
					if(hmWriterR1.containsKey(key)) {
						fw.write(key+ " "+sorted.get(key)+" <======= matched\n");
					}
					else {
						fw.write(key+ " "+sorted.get(key)+"\n");
					}
					count++;
					if(count>50) {
						break;
					}
				}
				fw.close();
				*/
				for(String key: hmWriterR1.keySet()) {
					hmWriterR1.get(key).close();
					hmWriterR2.get(key).close();
				}
				for(String key: hm.keySet()) {
					File f = hm.get(key);
					File R1 = new File(f.getAbsolutePath()+"_R1.fastq");
					File R2 = new File(f.getAbsolutePath()+"_R2.fastq");
					System.out.println(R1.getAbsolutePath());
					compressGZIP(R1);
					System.out.println(R2.getAbsolutePath());
					compressGZIP(R2);
				}
				datastoreR1.close();
				datastoreR2.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static HashMap<String, FastqWriter> createHashMapWriter(String string, HashMap<String, File> hm) {
		HashMap<String, FastqWriter> hmWriter = new HashMap<String, FastqWriter>();
		for(String code : hm.keySet()) {
			File f = hm.get(code);
			File file = new File(f.getAbsolutePath()+"_"+string+".fastq");
			try {
				hmWriter.put(code, new FastqWriterBuilder(file).build());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return hmWriter;
	}

	private static HashMap<String, File> createHashMap(String file, File dir, String unknownkey) {
		HashMap<String, File> hm = new HashMap<String, File>();
		try {
			Scanner s = new Scanner(new File(file));
			//skip header
			s.nextLine();
			while(s.hasNextLine()) {
				String line = s.nextLine();
				String[] parts = line.split("\t");
				String barcode = parts[0]+"+"+parts[1];
				String fileName = dir.getAbsolutePath()+File.separator+parts[2];
				if(hm.containsKey(barcode)) {
					System.err.println("barcode: "+barcode+" is not unique, please check your config file");
					System.exit(0);
				}
				hm.put(barcode, new File(fileName));
			}
			s.close();
			String fileName = dir.getAbsolutePath()+File.separator+unknownkey;
			hm.put(unknownkey, new File(fileName));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hm;
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
			}
		}
		return al;
		
	}
	/*
	private static HashMap<String, File> createHashMap(String string, String string2) {
		HashMap<String, File> hm = new HashMap<String, File>();
		try {
			Scanner s = new Scanner(new File(string));
			while(s.hasNextLine()) {
				String line = s.nextLine();
				
				System.out.println(outFile.getAbsolutePath());
				//FastqWriter fw = new FastqWriterBuilder(outFile).build();
				hm.put(line, outFile);
			}
			//add an unmatched part
			hm.put(unmatchedTriplet, new File(string2.getAbsolutePath()+"_"+unmatchedTriplet+".fastq"));
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hm;
	}
	*/

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

package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
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

public class DemultiplexMarloesJoostSubScreen {
	public static String unmatchedTriplet = "XXX";

	public static void main(String[] args) {
		File R1 = null;				
		File R2 = null;
		File barcodeFile = null;
		if(args.length==3) {
			String fileString = args[0];
			R1 = new File(fileString);
			R2 = new File(args[1]);
			barcodeFile = new File(args[2]);
			if(!R1.exists()) {
				System.err.println("file "+R1.getAbsolutePath()+" does not exist");
				System.exit(0);
			}
			if(!R2.exists()) {
				System.err.println("file "+R2.getAbsolutePath()+" does not exist");
				System.exit(0);
			}
			if(!barcodeFile.exists()) {
				System.err.println("barcodefile "+barcodeFile.getAbsolutePath()+" does not exist");
				System.exit(0);
			}
		}
		else {
			System.out.println("Run as java -jar xx.jar <fastqR1> <fastqR2> <barcodefile>");
			//R1 = new File("E:\\temp\\Repair_Seq\\SRR15164729_1.fastq.gz");
			//R2 = new File("E:\\temp\\Repair_Seq\\SRR15164729_2.fastq.gz");
			//barcodeFile = new File("E:\\temp\\Repair_Seq\\JoostLiu.txt");
			R1 = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Targeted Sequencing\\Hartwig\\GenomeScan106443ScreenTD\\raw\\106443-001-001_R1.fastq.gz");
			R2 = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Targeted Sequencing\\Hartwig\\GenomeScan106443ScreenTD\\raw\\106443-001-001_R2.fastq.gz");
			barcodeFile = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Targeted Sequencing\\Hartwig\\GenomeScan106443ScreenTD\\raw\\oPool_Custom_for_split.txt");
			//System.exit(0);
		}
		boolean error = false;
		if(!R1.exists()) {
			System.err.println("R1 file "+R1.getAbsolutePath()+" does not exist");
			error = true;
		}
		if(!R2.exists()) {
			System.err.println("R2 file "+R2.getAbsolutePath()+" does not exist");
			error = true;
		}
		if(!barcodeFile.exists()) {
			System.err.println("barcodeFile file "+barcodeFile.getAbsolutePath()+" does not exist");
			error = true;
		}
		//quit if there is an error
		if(error) {
			System.exit(0);
		}
		
		String endOfBarCodeString = "ggtgtt".toUpperCase();
		String startOfBarCodeString = "gcatagctcttaaac".toUpperCase();
		int startOfBarCodeStringPos = 35;
		int endOfBarCodeStringPos = 54;
		boolean write = true;
		boolean countNonMatched = true;
		boolean addBarcodeToFQ = true;
		boolean separateInBins = true;
		boolean compress = true; //default == true
		boolean allowOneMismatch = true;
		
		boolean outputR2 = false;
		
		//HashMap<String, String> barcodes = createHashMap("104596_barcodes_MB.txt");
		HashMap<String, String> barcodes = createHashMap(barcodeFile);
		HashMap<String, String> sgRNAToBins = createHashMapSGRNAs(barcodeFile);
		
		//iterate all barcodes to ensure they are all the same size
		int barcodeLength = -1;
		boolean first = true;
		boolean barcodeLengthsSame = true;
		for(String s: barcodes.keySet()) {
			if(first) {
				barcodeLength = s.length();
				System.out.println("The barcode length is "+barcodeLength);
				first = false;
			}
			if(!first) {
				if(barcodeLength != s.length()) {
					System.err.println("There are barcodes with different lengths "+s);
					barcodeLengthsSame = false;
				}
			}
		}
		
		try {
			HashMap<String, FastqWriter> hmWriterR1 = new HashMap<String, FastqWriter>();
			HashMap<String, FastqWriter> hmWriterR2 = new HashMap<String, FastqWriter>();
			ArrayList<File> fileList = new ArrayList<File>();
			
			FastqDataStore datastoreR1 = new FastqFileDataStoreBuilder(R1)
					.qualityCodec(FastqQualityCodec.SANGER)
                    .hint(DataStoreProviderHint.ITERATION_ONLY)
                    .build();
			FastqDataStore datastoreR2 = new FastqFileDataStoreBuilder(R2)
					.qualityCodec(FastqQualityCodec.SANGER)
                    .hint(DataStoreProviderHint.ITERATION_ONLY)
                    .build();
			
			StreamingIterator<FastqRecord> iterR1 = datastoreR1.iterator();
			StreamingIterator<FastqRecord> iterR2 = datastoreR2.iterator();
			
			int nr = 0;
			int hit = 0;
			int nonhit = 0;
			int notFoundSG = 0;
			int hitPosition = 0;
			int mismatchHit = 0;
			
			String outDirString = R1.getAbsolutePath().replace("_R1_001.fastq.gz", "");
			outDirString = outDirString.replace("_R1.fastq.gz", "");
			File outDir = new File(outDirString);
			if(!outDir.exists()) {
				outDir.mkdir();
			}
			
			HashMap<String, Integer> barcodeHits = new HashMap<String,Integer>();
			while(iterR1.hasNext()) {
				FastqRecord fqR1 = iterR1.next();
				FastqRecord fqR2 = iterR2.next();
				//String R1S = fqR1.getNucleotideSequence().toString();
				String R2S = fqR2.getNucleotideSequence().toString();
				String barcode = null;
				
				//System.out.println(R1S.substring(0, endOfBarCode));
				int start = R2S.indexOf(startOfBarCodeString);
				//search for the last index!
				int end = R2S.lastIndexOf(endOfBarCodeString);
				if(start>=0 && end>=0 && end > (start+startOfBarCodeString.length())) {
					start += startOfBarCodeString.length();
					barcode = R2S.substring(start, end-1);
					
					if(barcodes.containsKey(barcode)) {
						hit++;
					}
				}
				//try position
				else {
					barcode = R2S.substring(startOfBarCodeStringPos,endOfBarCodeStringPos);
					//System.out.println(barcode+"\t"+Utils.reverseComplement(barcode)+"\t"+barcodes.containsKey(barcode));
					if(barcodes.containsKey(barcode) ) {
						hitPosition++;
					}
					//TODO: see if we can use the closest barcode then, perhaps allow a distance of 1?
					else {
						//System.exit(0);
					}
				}
				//retrieve the barcode
				String foundBarcode = barcodes.get(barcode);
				//not directly found
				if(allowOneMismatch && foundBarcode == null) {
					//ensure the length is correct otherwise there is no reason to search
					if(barcodeLengthsSame && barcode.length() == barcodeLength) {
						//check if we can find it with one mismatch
						ArrayList<String> closest = getDistance(barcode,barcodes.keySet());
						//only if a single hit was found we need to get the distance
						if(closest.size()==1) {
							int distance = getDistance(barcode, closest.get(0));
							//single mismatch
							if(distance == 1) {
								barcode = closest.get(0);
								//now replace the foundBarcode
								foundBarcode = barcodes.get(barcode);
								mismatchHit++;
							}
						}
					}
				}
					
				if(foundBarcode != null ) {
					//overwrite barcode if required
					String barcodeLookup = foundBarcode;
					if(addBarcodeToFQ && separateInBins) {
						foundBarcode = sgRNAToBins.get(foundBarcode);
					}
					else if(addBarcodeToFQ) {
						foundBarcode = "therecanonlybeone";
					}
					if(!barcodeHits.containsKey(foundBarcode)){
						//barcodeHits.put(foundBarcode, 0);
					}
					//barcodeHits.put(foundBarcode, barcodeHits.get(foundBarcode)+1);
					
					//Write to file
					if(!hmWriterR1.containsKey(foundBarcode)) {
						String fileName = R1.getName().replace("_R1_001.fastq.gz", "");
						fileName = fileName.replace("R1.fastq.gz", "");
						if(write) {
							File R1temp = Paths.get(outDir.getAbsolutePath(),fileName+"_"+foundBarcode+"_R1.fastq").toFile(); 
							hmWriterR1.put(foundBarcode, new FastqWriterBuilder(R1temp).build());
							fileList.add(R1temp);
							if(outputR2) {
								File R2temp = Paths.get(outDir.getAbsolutePath(),fileName+"_"+foundBarcode+"_R2.fastq").toFile();
								hmWriterR2.put(foundBarcode, new FastqWriterBuilder(R2temp).build());
								fileList.add(R2temp);
							}
							
							
							
						}
						else {
							hmWriterR1.put(foundBarcode,null);
							hmWriterR2.put(foundBarcode,null);
						}
					}
					if(write) {
						if(addBarcodeToFQ) {
							FastqRecord fqTestR1 = fqR1.toBuilder().comment("BC:"+barcodeLookup).build();
							hmWriterR1.get(foundBarcode).write(fqTestR1);
							if(outputR2) {
								FastqRecord fqTestR2 = fqR2.toBuilder().comment("BC:"+barcodeLookup).build();
								hmWriterR2.get(foundBarcode).write(fqTestR2);
							}
							
						}
						else {
							hmWriterR1.get(foundBarcode).write(fqR1);
							hmWriterR2.get(foundBarcode).write(fqR2);
						}
					}
				}
				else {
					if(countNonMatched) {
						if(!barcodeHits.containsKey(barcode)){
							barcodeHits.put(barcode, 0);
						}
						barcodeHits.put(barcode, barcodeHits.get(barcode)+1);
					}
					//System.out.println("nonhit\t"+pe.getR1().getName()+"\t"+barcodeAlt+"\t"+Utils.reverseComplement(barcodeAlt));
					nonhit++;
				}
				
				nr++;
				if(nr%1000000==0) {
					System.out.println("Already processed "+nr+" records (mismatches) "+mismatchHit);
				}
			}
			System.out.println(R1.getName()+"\thit "+hit+" hit position "+hitPosition+" non-hit: "+nonhit+ " mismatchHit: "+mismatchHit+ " not-sgRNAsurround: "+notFoundSG);
			iterR1.close();
			iterR2.close();
			if(write) {
				for(String key: hmWriterR1.keySet()) {
					hmWriterR1.get(key).close();
				}
				for(String key: hmWriterR2.keySet()) {
					hmWriterR2.get(key).close();
				}
				//compress and delete
				if(compress) {
					for(File f: fileList) {
						System.out.println("Compressing "+f.getName());
						compressGZIP(f);
						f.delete();
					}
				}
			}
			System.out.println(R1.getName()+"\thit "+hit+" hit position "+hitPosition+" non-hit: "+nonhit+ " mismatchHit: "+mismatchHit+ " not-sgRNAsurround: "+notFoundSG);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static ArrayList<String> getDistance(String barcode, Set<String> keySet) {
		int maxDiff = 100;
		ArrayList<String> curBarcodes = new ArrayList<String>();
		for(String s: keySet) {
			int diff = getDistance(barcode, s);
			if(diff < maxDiff) {
				maxDiff = diff;
				curBarcodes.clear();
				curBarcodes.add(s);
			}
			else if(diff == maxDiff){
				curBarcodes.add(s);
			}
		}
		return curBarcodes;
	}

	private static int getDistance(String barcode, String s) {
		if(barcode == null || s == null || barcode.length()!=s.length()) {
			return Integer.MAX_VALUE;
		}
		int score = 0;
		for(int i=0;i<barcode.length();i++) {
			if(barcode.charAt(i) != s.charAt(i)) {
				score++;
			}
		}
		return score;
	}

	private static HashMap<String, String> createHashMapSGRNAs(File f) {
		HashMap<String, String> hm = new HashMap<String, String>();
		HashMap<String, Integer> binCount = new HashMap<String, Integer>();
		int binNameColumn = 7;
		int idColumn = 2;
		try {
			Scanner s = new Scanner(f);
			//skip the first line as it contains the header
			s.hasNextLine();
			
			while(s.hasNextLine()) {
				String line = s.nextLine();
				String[] parts = line.split("\t");
				String name = parts[idColumn];
				String bin = parts[binNameColumn];
				//System.out.println(name+"\t"+bin);
				hm.put(name, bin);
				//just count the number of barcodes in a bin
				Integer number = binCount.get(bin);
				if(number == null) {
					binCount.put(bin, 1);
				}
				else {
					binCount.put(bin, number+1);
				}
			}
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Added "+hm.size()+" barcodes");
		for(String key: binCount.keySet()){
			//System.out.println(key +"\t"+binCount.get(key));
		}
		return hm;
	}

	private static HashMap<String, String> createHashMap(File f) {
		HashMap<String, String> hm = new HashMap<String, String>();
		System.out.println("Taking column 2 and 3 for barcode and ID");
		int idColumn = 2;
		int sgColumn = 3;
		boolean takeRevCom = true;
		boolean removeAAAC = false;
		try {
			Scanner s = new Scanner(f);
			//skip first line as that contains the header
			s.nextLine();
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

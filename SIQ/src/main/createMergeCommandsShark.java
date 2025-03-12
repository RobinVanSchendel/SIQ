package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

import dnaanalysis.Utils;

import java.util.regex.Matcher;


public class createMergeCommandsShark {

	public static void main(String[] args) throws IOException {
		
		//File dir = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Targeted Sequencing\\Hartwig\\GenomeScan106217\\raw\\");
		//File dir = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Targeted Sequencing\\Hartwig\\GenomeScan104269\\Raw\\");
		//File barcodeSamples = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Targeted Sequencing\\Hartwig\\GenomeScan104269\\104269JavaSamples.txt");
		//File dir = new File("W:\\Targeted\\GenomeScan106356\\");
		//File barcodeSamples = new File("W:\\Targeted\\GenomeScan106356\\106356JavaSamples.txt");
		File dir = new File("W:\\Targeted\\GenomeScan107065\\");
		File barcodeSamples = new File("W:\\Targeted\\GenomeScan107065\\107065JavaSamples.txt");
		
		
		String linuxInputDir = "/exports/humgen/rvanschendel/TargetedSequencingGenomeScan107065/";
		String linuxOutputDir = "/exports/humgen/rvanschendel/TargetedSequencingGenomeScan107065/rawMerged/";
		String runPattern = "107065-001-[0-9]+_";
		//String runPattern = "104269-001-[0-9]+_";
		int barcodeLength = 17; //default: 17
		boolean rcSeq2 = false; //default: false
		
		boolean outputAsMoveCommand = false; //default true
		
		HashMap<String, String> barcodes = createSampleBarcodeHash(barcodeSamples, rcSeq2);
		
		//some checks
		if(linuxInputDir.contentEquals(linuxOutputDir)) {
			System.err.println("linuxInputDir and linuxOutputDir cannot be the same");
			System.exit(0);
		}
		if(!linuxInputDir.endsWith("/")) {
			System.err.println("linuxInputDir does not end with '/'");
			System.exit(0);
		}
		if(!linuxOutputDir.endsWith("/")) {
			System.err.println("linuxOutputDir does not end with '/'");
			System.exit(0);
		}
		
		ArrayList<File> files = new ArrayList<File>();
		File[] allFiles = dir.listFiles();
		Arrays.sort(allFiles);
		
		for(File f: allFiles) {
			if(f.getName().endsWith(".fastq.gz")) {
				//if(!f.getName().endsWith("R2.fastq.gz")) {
					files.add(f);
				//}
				//System.out.println(f.getName());
			}
		}
		
		//System.out.println("search for matches");
		
		Pattern pattern = Pattern.compile(runPattern);
		HashMap<String, String> used = new HashMap<String, String>();
		
		//find matches
		while(files.size()>0) {
			File f = files.remove(0);
			//System.out.println(f.getName());
			boolean isR1 = determineR1(f.getName());
			
			Matcher matcher = pattern.matcher(f.getName());
			boolean found = matcher.find();
			String match = matcher.group();
			//System.out.println(found);
			//System.out.println(match);
			int pos = f.getName().indexOf(match)+match.length();
			int end = pos + barcodeLength;
			String barcode = f.getName().substring(pos, end);
			//System.out.println(barcode);
			String finalId = barcodes.get(barcode);
			//try a single barcode match as well
			if(finalId == null) {
				barcode = barcode.substring(0, 8); //fixed to 8bp!
				finalId = barcodes.get(barcode);
			}
			if(finalId == null) {
				System.err.println("Cannot find barcode "+barcode +"\t"+f.getName());
				finalId = barcode;
			}
			else {
				used.put(barcode, finalId);
			}
			//find same files
			ArrayList<File> matches = findFiles(match, files, isR1);
			
			//remove those files from the list
			for(File foundFile: matches) {
				files.remove(foundFile);
			}
			matches.add(f);
			StringBuffer command = new StringBuffer();
			//add input files to cat
			if(outputAsMoveCommand) {
				if(matches.size() > 1) {
					System.err.println("Cannot make a move command for multiple files!");
					System.err.println(matches);
					System.exit(0);
				}
				command.append("mv ");
			}
			for(File matchFile: matches) {
				String tempFile = linuxInputDir+matchFile.getName();
				if(command.length()>0) {
					command.append(" ");
				}
				command.append("'");
				command.append(tempFile);
				command.append("'");
			}
			//tab
			command.append("\t");
			//create outputfile starting with the match
			String outputfile = matches.get(0).getName();
			int start = outputfile.indexOf(match);
			outputfile = linuxOutputDir+outputfile.substring(start);
			//so replace with the ID here
			outputfile = outputfile.replace(barcode, finalId);
			
			//also replace _L00[1-9]_ with _
			outputfile = outputfile.replaceAll("_L00[1-9]_", "_");
			
			command.append("'");
			command.append(outputfile);
			command.append("'");
			System.out.println(command);
		}
		
		boolean first = true;
		
		for(String key: barcodes.keySet()) {
			if(!used.containsKey(key)) {
				if(first) {
					System.err.println("Following IDs were NOT found:");
					first = false;
				}
				System.err.println(barcodes.get(key)+"\t"+key);
			}
		}
				
				

	}

	private static HashMap<String, String> createSampleBarcodeHash(File barcodeSamples, boolean rcSeq) {
		HashMap<String, String> hm = new HashMap<String, String>();
		try {
			Scanner s = new Scanner(barcodeSamples);
			while(s.hasNextLine()) {
				String line = s.nextLine();
				String[] parts = line.split("\t");
				if(parts.length!=3 && parts.length!=2 ) {
					System.err.println("There should be 2 or 3 columns in this file: <ID> <P7> <P5>");
					System.err.println(line);
					System.exit(0);
				}
				//now add the barcodes
				String barcode = null;
				if(parts.length == 3) {
					barcode = parts[1]+"-"+parts[2];
					//or take the rc if that is requested
					if(rcSeq) {
						barcode = parts[1]+"-"+Utils.reverseComplement(parts[2]);
					}
				}
				else if(parts.length == 2) {
					barcode = parts[1];
				}
				String sampleID = parts[0];
				hm.put(barcode,sampleID);
			}
			s.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hm;
	}

	private static boolean determineR1(String name) {
		return name.endsWith("R1.fastq.gz");
	}

	private static ArrayList<File> findFiles(String match, ArrayList<File> files, boolean isR1) {
		ArrayList<File> al = new ArrayList<File>();
		for(File f: files) {
			if(f.getName().contains(match)) {
				//is the R1/R2 status the same?
				if(isR1 == determineR1(f.getName())){
					al.add(f);
				}
			}
		}
		return al;
	}

}

package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.RichSequence.IOTools;

import data.Row;
import data.Table;
import data.TableReader;
import utils.CompareSequence;
import utils.SampleInfo;
import utils.UtilsReduce;

public class ReadInResults {

	public static void main(String[] args) {
		File rawFile = new File("E:/Project_Hartwig/20180815_HPRT_all_1_3.txt_reduced.txt");
		File sampleFile = new File("E:\\Project_Hartwig\\HPRT_file_sample_simplified.txt");
		File subjectDir = new File("E:\\Project_Hartwig\\");
		File addSearchDir = new File("Z:\\Joost\\Files\\Manuscripts\\Schimmel_etal_2017\\Robin\\px458_HPRT.txt");
		
		File outFile = new File(rawFile.getAbsolutePath()+".searchadditional.txt");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// read in Additional parts
		HashMap<String, String> hmAdditional = new HashMap<String, String>();
		BufferedReader is2 = null;
		try {
			is2 = new BufferedReader(new FileReader(addSearchDir));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		SequenceIterator si2 = IOTools.readFastaDNA(is2, null);
		while(si2.hasNext()){
			Sequence s = null;
			try {
				s = si2.nextSequence();
			} catch (NoSuchElementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BioException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			hmAdditional.put(s.getName(), s.seqString().toString());
		}
		Table t = TableReader.readTable(sampleFile,"\t");
		//gather subjects
		HashMap<String,SampleInfo> sis = new HashMap<String,SampleInfo>();
		//System.out.println(t.toString());
		for(Row r: t.getRows()) {
			String tempFile = r.getColumn("File").toString();
			String subject = r.getColumn("Subject").toString();
			String left = r.getColumn("Left").toString();
			String right = r.getColumn("Right").toString();
			SampleInfo si = new SampleInfo(tempFile, new File(subject), left, right);
			sis.put(si.getFile(), si);
		}
		
		HashMap<String, Integer> index = UtilsReduce.getHeader(rawFile);
		
		File subjectFile = null;
		RichSequence subject = null;
		long startTime = System.nanoTime();
		try {
			Scanner s = new Scanner(rawFile);
			boolean first = true;
			int count = 0;
			while(s.hasNextLine()) {
				String line = s.nextLine();
				if(first) {
					first = false;
					writer.println(line);
					continue;
				}
				else {
					String[] parts = line.split("\t");
					String tempFile = parts[index.get("File")];
					if(tempFile.equals("File")) {
						continue;
					}
					//System.out.println(tempFile);
					File subjectFileTemp = sis.get(tempFile).getSubject();
					//System.out.println(subjectDir.getAbsolutePath());
					//System.out.println(tempFile);
					//System.out.println(subjectFileTemp.getName());
					subjectFileTemp = new File(subjectDir, subjectFileTemp.getName());
					//System.out.println(subjectFile.getAbsolutePath()+":"+subjectFile.exists());
					//read in subject
					if(subjectFileTemp != subjectFile) {
						BufferedReader is = new BufferedReader(new FileReader(subjectFileTemp));
						RichSequenceIterator si = IOTools.readFastaDNA(is, null);
						try {
							subject = si.nextRichSequence();
						} catch (NoSuchElementException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (BioException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					/*
					CompareSequence cs = new CompareSequence(subject, null, parts[index.get("Raw")], null, sis.get(tempFile).getLeft(),sis.get(tempFile).getRight(),null, null, true,parts[index.get("Name")]);
					cs.setCurrentFile(parts[index.get("File")]);
					cs.setAdditionalSearchString(hmAdditional);
					cs.determineFlankPositions();
					String finalLine = parts[index.get("Key")]+"\t"+parts[index.get("countEvents")]+"\t" + cs.toStringOneLine();
					//for some reason I got empty rows occasionally
					if(finalLine!= null && finalLine.length()>0) {
						writer.println(finalLine);

					}
					*/
					//System.out.println(line);
					//System.out.println(cs.toStringOneLine());
					//System.exit(0);
					subjectFile = subjectFileTemp;
				}
				count++;
				if(count%100==0) {
					long endTime = System.nanoTime();
					long duration = (endTime - startTime)/1000000;
					System.out.println("Already processed "+count+" lines "+duration);
					//writer.flush();
					//break;
				}
			}
			s.close();
			writer.close();
			System.out.println(count+" lines written to "+outFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

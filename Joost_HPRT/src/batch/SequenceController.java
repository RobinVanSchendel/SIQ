package batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.jcvi.jillion.core.datastore.DataStoreException;
import org.jcvi.jillion.core.datastore.DataStoreProviderHint;
import org.jcvi.jillion.core.qual.PhredQuality;
import org.jcvi.jillion.core.qual.QualitySequence;
import org.jcvi.jillion.core.residue.nt.NucleotideSequence;
import org.jcvi.jillion.core.util.iter.StreamingIterator;
import org.jcvi.jillion.trace.chromat.Chromatogram;
import org.jcvi.jillion.trace.chromat.ChromatogramFactory;
import org.jcvi.jillion.trace.fastq.FastqDataStore;
import org.jcvi.jillion.trace.fastq.FastqFileDataStoreBuilder;
import org.jcvi.jillion.trace.fastq.FastqQualityCodec;
import org.jcvi.jillion.trace.fastq.FastqRecord;

import utils.CompareSequence;
import utils.KMERLocation;
import utils.MyError;
import utils.MyOptions;
import utils.SequenceFileThread;
import utils.Utils;

public class SequenceController {
	private boolean printOnlyIsParts = false;
	private HashMap<String, String> colorMap;
	private File outputFile;
	private HashMap<String, Integer> countEvents = new HashMap<String, Integer>();
	private HashMap<String, String> actualEvents = new HashMap<String, String>();
	private boolean collapseEvents = false;
	private ArrayList<CompareSequence> sequences;
	private HashMap<String, ArrayList<CompareSequence>> hash = new HashMap<String, ArrayList<CompareSequence>>();
	private boolean includeStartEnd = false;
	private boolean checkReverse = true;
	
	public void readFilesFASTQMultiThreaded (MyOptions options){
		readFilesFASTQMultiThreaded(options.getSingleFile(),options.getSubject(), options.getLeftFlank(), options.getRightFlank(), null, options.getSearchAdditional(), true, null, options);
		
	}
	public void readFilesFASTQMultiThreaded(String file, String subjectFile, String leftFlank, String rightFlank, String type, File searchAdditional, boolean writeToOutput, String containsString, MyOptions options){
		File f = new File(subjectFile);
		if(!f.exists()) {
			MyError.err("The reference input file does not exist: "+f.getAbsolutePath());
		}
		BufferedReader is = null, is2 = null;
		RichSequence subject = null;
		Vector<Sequence> additional = new Vector<Sequence>();
		HashMap<String, String> hmAdditional = new HashMap<String, String>();
		try {
			is = new BufferedReader(new FileReader(subjectFile));
			if(searchAdditional!= null && !searchAdditional.equals("")){
				is2 = new BufferedReader(new FileReader(searchAdditional));
				SequenceIterator si2 = IOTools.readFastaDNA(is2, null);
				while(si2.hasNext()){
					additional.add(si2.nextSequence());
				}
				for(Sequence s: additional) {
					hmAdditional.put(s.getName(), s.seqString().toString());
				}
			}
			RichSequenceIterator si = IOTools.readFastaDNA(is, null);
			subject = si.nextRichSequence();
			
			
		} catch (FileNotFoundException | NoSuchElementException | BioException e1) {
			e1.printStackTrace();
		}
		
		File d = new File(file);
		if(!d.exists()) {
			MyError.err("The directory does not exist: "+d.getAbsolutePath());
		}
		if(!d.exists()) {
			MyError.err("The directory "+d.getAbsolutePath()+" is not a directory!");
		}
		Vector<File> ab1s = getFASTQFiles(d, containsString);
		System.out.println("Found "+ab1s.size()+" FASTQ files");
		int fileNr = 1;
		Vector<Thread> v = new Vector<Thread>();
		Vector<Thread> running = new Vector<Thread>();
		Vector<Integer> toBeRemoved = new Vector<Integer>();
		SequenceFileThread sft = null;
		KMERLocation kmerl = new KMERLocation(subject.seqString());
		//kmerl = null;
		for(File seqs: ab1s){
			System.out.println(fileNr+"/"+ab1s.size()+":"+seqs.getName());
			File output = new File(options.getOutput());
			if(!options.overwrite() && output.exists()) {
				//rename
				System.err.println("File "+output.getAbsolutePath()+" already exists, skipping!");
				continue;
			}
			sft = new SequenceFileThread(seqs, true, subject, leftFlank, rightFlank,output, options.collapseEvents(), options.getMaxError(), hmAdditional);
			sft.setMinimalCount(options.getMinNumber());
			sft.setCollapseStartEnd(includeStartEnd);
			sft.setMaximumReads(options.getMaxReads());
			sft.setLeftPrimer(options.getLeftPrimer());
			sft.setRightPrimer(options.getRightPrimer());
			sft.setMinPassedPrimer(options.getMinPassedPrimer());
			sft.setAlias(options.getAlias());
			sft.setKMERLocation(kmerl);
			sft.setAllowJump(options.allowJump());
			if(options.getSingleFile()!= null) {
				sft.printHeader();
			}
			sft.setFileF(options.getSingleFileF());
			sft.setFileR(options.getSingleFileR());
			Thread newThread = new Thread(sft);
			v.add(newThread);
			//newThread.start();
			fileNr++;
		}
		if(v.size()==0) {
			System.exit(0);
		}
		if(ab1s.size() == 1) {
			sft.runReal();
			return;
		}
		//int max = Runtime.getRuntime().availableProcessors();
		long max = options.getThreads();
		int maxFiles = ab1s.size();
		System.out.println("Gonna start "+Math.min(max, maxFiles)+" cpus");
		//let's see if this works
		while(v.size()>0 || running.size()>0){
			for(int nr = running.size()-1;nr>=0;nr--){
				if(!running.get(nr).isAlive()){
					toBeRemoved.add(nr);
				}
			}
			for(int i: toBeRemoved){
				running.remove(i);
			}
			toBeRemoved.clear();
			if(running.size()<max && v.size()>0){
				Thread t = v.remove(0);
				running.add(t);
				t.start();
			}
			try {
				//1 second of sleep... ZzZ...
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private Vector<File> getFASTQFiles(File d, String containsString) {
		Vector<File> files = new Vector<File>();
		//hack to also allow single Files
		if(d.isFile()) {
			files.add(d);
			return files;
		}
		for(File f: d.listFiles()){
			//recursive
			if(f.isDirectory()){
				files.addAll(getFASTQFiles(f, containsString));
			}
			//for some reason .gz does not work yet
			else if(f.isFile() && f.getName().endsWith(".fastq") || f.getName().endsWith(".fastq.gz")){
				if(containsString != null) {
					if(f.getName().contains(containsString)) {
						files.add(f);
					}
				}
				else {
					files.add(f);
				}
			}
		}
		return files;
	}
	public static Vector<File> getAB1Files(File d) {
		Vector<File> files = new Vector<File>();
		for(File f: d.listFiles()){
			if(f.isDirectory()){
				files.addAll(getAB1Files(f));
			}
			else if(f.isFile() && f.getName().endsWith(".ab1")){
				files.add(f);
			}
		}
		return files;
	}
	public void setPrintOnlyISParts(){
		this.printOnlyIsParts = true;
	}
	public void setColorMap(HashMap<String, String> colorMap){
		this.colorMap = colorMap;
	}
	public void setOutputFile(File f){
		this.outputFile = f;
	}
	public File getOutputFile(){
		return this.outputFile;
	}
	public void writeln(String string) {
		if(getOutputFile() != null){
			try {
				PrintWriter writer = new PrintWriter(this.outputFile, "UTF-8");
				writer.println(string);
				writer.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void setCollapseEvents(boolean collapse){
		this.collapseEvents = collapse;
	}
	public ArrayList<CompareSequence> readFilesTryToMatch(
			File dir, RichSequence currentSequence, String leftFlank,
			String rightFlank, String type, String searchAdditional, boolean printNonCorrect, double quality) {
		
		Vector<Sequence> additional = new Vector<Sequence>();
		HashMap<String, String> hmAdditional = new HashMap<String, String>();
		try {
			if(searchAdditional != null){
				BufferedReader is2 = new BufferedReader(new FileReader(searchAdditional));
				RichSequenceIterator si2 = RichSequence.IOTools.readFastaDNA(is2, null);
				while(si2.hasNext()){
					additional.add(si2.nextRichSequence());
				}
				for(Sequence s: additional) {
					hmAdditional.put(s.getName(), s.seqString().toString());
				}
			}
		} catch (FileNotFoundException | NoSuchElementException | BioException e1) {
			e1.printStackTrace();
		}
		
		ArrayList<CompareSequence> al = new ArrayList<CompareSequence>();
		Vector<File> ab1s = getAB1Files(dir);
		//System.out.println("Found "+ab1s.size()+" ab1 files");
		//Introduce the KMER
		KMERLocation kmerl = new KMERLocation(currentSequence.seqString());
		//kmerl = null;
		for(File seqs: ab1s){
			RichSequence subject = currentSequence;
			try {
				//System.out.println("accessing "+seqs.getName());
				Chromatogram chromo = ChromatogramFactory.create(seqs);
				NucleotideSequence seq = chromo.getNucleotideSequence();
				QualitySequence quals = chromo.getQualitySequence();
				//mask
				CompareSequence cs = new CompareSequence(subject, seq.toString(), quals, leftFlank, rightFlank, null, seqs.getParent(), checkReverse, seqs.getName(), kmerl);
				cs.setAndDetermineCorrectRange(quality);
				cs.maskSequenceToHighQualityRemove();
				cs.determineFlankPositions(false);
				cs.setAdditionalSearchString(hmAdditional);
				cs.setCutType(type);
				//only correctly found ones
				//and filter for events that are the same in ID and class
				if(printNonCorrect || (cs.getRemarks().length() == 0 && cs.getType() != CompareSequence.Type.WT)){
					al.add(cs);
				}
				
			} catch (IOException e1) {
				System.err.println(seqs.getName()+" has a problem");
				e1.printStackTrace();
			}
		}
		return al;
	}
	public void setSequences(ArrayList<CompareSequence> al) {
		this.sequences = al;
	}
	public void printXY() {
		putInHash();
		//iterate hash
		List<String> keys = new ArrayList<String>(hash.keySet());
		Collections.sort(keys);
		for(String key: keys){
			ArrayList<CompareSequence> temp = hash.get(key);
			ArrayList<Double> tempXs = getXs(temp.size());
			temp.sort((o1,o2) -> o1.getDelEnd()-o2.getDelEnd());
			StringBuffer sb = new StringBuffer();
			sb.append("X\tX");
			for(Double d: tempXs){
				sb.append('\t');
				sb.append(d);
			}
			System.out.println(sb);
			sb = new StringBuffer();
			sb.append(key+"\t"+temp.get(0).getSubjectComments());
			for(CompareSequence cs: temp){
				sb.append("\t");
				sb.append(cs.getRelativeDelEnd());
			}
			System.out.println(sb);
		}
	}
	private ArrayList<Double> getXs(int size){
		ArrayList<Double> list = new ArrayList<Double>();
		for(int i = 0;i<size;i++){
			list.add(i/(double)(size-1));
		}
		return list;
	}
	
	private void putInHash() {
		if(hash == null){
			hash = new HashMap<String, ArrayList<CompareSequence>>();
		}
		for(CompareSequence cs: this.sequences){
			String subject = cs.getSubject();
			ArrayList<CompareSequence> temp = hash.get(subject);
			if(temp != null){
				temp.add(cs);
			}
			else{
				temp = new ArrayList<CompareSequence>();
				temp.add(cs);
				hash.put(subject, temp);
			}
		}
	}
	public class CustomComparator {
	    public int compareTo(CompareSequence object1, CompareSequence object2) {
	        return object1.getDelStart() - object2.getDelStart();
	    }
	}
}

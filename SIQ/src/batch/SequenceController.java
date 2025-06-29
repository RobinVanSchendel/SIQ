package batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.jcvi.jillion.core.qual.QualitySequence;
import org.jcvi.jillion.core.residue.nt.NucleotideSequence;
import org.jcvi.jillion.trace.chromat.Chromatogram;
import org.jcvi.jillion.trace.chromat.ChromatogramFactory;
import utils.CompareSequence;
import utils.MyError;
import utils.MyOptions;
import utils.SequenceFileThread;
import utils.Subject;

public class SequenceController {
	private boolean printOnlyIsParts = false;
	private HashMap<String, String> colorMap;
	private File outputFile;
	private HashMap<String, Integer> countEvents = new HashMap<String, Integer>();
	private HashMap<String, String> actualEvents = new HashMap<String, String>();
	private ArrayList<CompareSequence> sequences;
	private HashMap<String, ArrayList<CompareSequence>> hash = new HashMap<String, ArrayList<CompareSequence>>();
	private boolean includeStartEnd = false;
	private boolean checkReverse = true;
	
	public void readFilesFASTQMultiThreaded (MyOptions options){
		readFilesFASTQMultiThreaded(options.getSingleFile(),options.getSubject(), options.getLeftFlank(), options.getRightFlank(), null, options.getSearchAdditional(), true, null, options);
		
	}
	public void readFilesFASTQMultiThreaded(String file, String subjectFile, String leftFlank, String rightFlank, String type, File searchAdditional, boolean writeToOutput, String containsString, MyOptions options){
		File f = new File(subjectFile);
		if(!isDNA(subjectFile) && !f.exists()) {
			MyError.err("The reference input file does not exist: "+f.getAbsolutePath()+"\tor is not DNA");
		}
		BufferedReader is = null, is2 = null, is3 = null;
		RichSequence subject = null;
		RichSequenceIterator rsiHDR = null;
		Vector<Sequence> additional = new Vector<Sequence>();
		HashMap<String, String> hmAdditional = new HashMap<String, String>();
		try {
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
			if(options.getHDR()!=null) {
				is3 =  new BufferedReader(new FileReader(options.getHDR()));
				rsiHDR = IOTools.readFastaDNA(is3, null);
			}
			if(!isDNA(subjectFile)) {
				is = new BufferedReader(new FileReader(subjectFile));
				RichSequenceIterator si = IOTools.readFastaDNA(is, null);
				subject = si.nextRichSequence();
			}
			else {
				subject = RichSequence.Tools.createRichSequence("subject", DNATools.createDNA(subjectFile));
			}
			
			
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
		SequenceFileThread sft = null;
		Subject subjectObject = new Subject(subject,options.getLeftFlank(),options.getRightFlank(), true);
		subjectObject.setLeftPrimer(options.getLeftPrimer());
		subjectObject.setRightPrimer(options.getRightPrimer());
		System.out.println("Swapping in readFilesFASTQMultiThreaded");
		subjectObject.swapPrimersIfNeeded();
		subjectObject.setMinPassedPrimer(options.getMinPassedPrimer());
		//only set if non NULL
		if(rsiHDR!=null && rsiHDR.hasNext()) {
			while(rsiHDR.hasNext()) {
				try {
					//add all sequences to HDR
					subjectObject.addHDR(rsiHDR.nextRichSequence());
				} catch (NoSuchElementException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BioException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		//kmerl = null;
		for(File seqs: ab1s){
			System.out.println(fileNr+"/"+ab1s.size()+":"+seqs.getName());
			File output = new File(options.getOutput());
			if(!options.overwrite() && output.exists()) {
				//rename
				System.err.println("File "+output.getAbsolutePath()+" already exists, skipping!");
				continue;
			}
			File stats = new File(output.getAbsoluteFile()+"_stats.txt");
			File topStats = new File(output.getAbsoluteFile()+"_top_stats.txt");
			sft = new SequenceFileThread(seqs, true, subjectObject, output, stats, options.getMaxError(), hmAdditional,topStats, false, true, false);
			sft.setMinimalCount(options.getMinNumber());
			sft.setCollapseStartEnd(includeStartEnd);
			sft.setMaximumReads(options.getMaxReads());
			sft.setAlias(options.getAlias());
			sft.setAllowJump(options.allowJump());
			if(options.getSingleFile()!= null) {
				sft.printHeader();
			}
			sft.setFileF(options.getSingleFileF());
			sft.setFileR(options.getSingleFileR());
			sft.setTinsDistance((int)options.getSearchSpace());
			fileNr++;
		}
		sft.runReal();
	}
	private boolean isDNA(String subjectFile) {
		return subjectFile.matches("[agctAGCT]*");
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
	public ArrayList<CompareSequence> readFilesTryToMatch(
			File dir, RichSequence currentSequence, String leftFlank,
			String rightFlank, String type, String searchAdditional, boolean printNonCorrect, double quality, boolean checkLeftRight) {
		
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
		Subject subjectObject = new Subject(currentSequence, leftFlank, rightFlank);
		for(File seqs: ab1s){
			try {
				//System.out.println("accessing "+seqs.getAbsolutePath());
				Chromatogram chromo = ChromatogramFactory.create(seqs);
				NucleotideSequence seq = chromo.getNucleotideSequence();
				QualitySequence quals = chromo.getQualitySequence();
				//mask
				CompareSequence cs = new CompareSequence(subjectObject, seq.toString(), quals, seqs.getParent(), checkReverse, seqs.getName());
				cs.setAndDetermineCorrectRange(quality);
				cs.maskSequenceToHighQualityRemove();
				cs.determineFlankPositions(false);
				cs.setAdditionalSearchString(hmAdditional);
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

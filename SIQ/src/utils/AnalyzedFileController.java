package utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;
import org.biojavax.bio.seq.RichSequence;
import org.jcvi.jillion.core.qual.QualitySequence;
import org.jcvi.jillion.core.residue.nt.NucleotideSequence;
import org.jcvi.jillion.trace.chromat.Chromatogram;
import org.jcvi.jillion.trace.chromat.ChromatogramFactory;

import dnaanalysis.Blast;
import gui.PropertiesManager;
import gui.ReportPanel;

public class AnalyzedFileController implements Runnable{
	public File subjectFile;
	public ArrayList<File> queries;
	//public ArrayList<Anal>
	public String left, right;
	public int current = 0;
	private double maxError;
	private boolean maskLowQuality;
	private boolean maskLowQualityRemove;
	public ArrayList<CompareSequence> result = new ArrayList<CompareSequence>();
	private PropertiesManager pm;
	private boolean runningBlast;
	private JProgressBar progressBar;
	private JButton button;
	private JFileChooser jfc;
	private boolean split;
	
	public AnalyzedFileController(PropertiesManager pm) {
		this.pm = pm;
	}
	public void setFiles(File[] files) {
		if(files.length>0) {
			subjectFile = files[0];
			queries = new ArrayList<File>();
			for(int i = 1;i<files.length;i++) {
				queries.add(files[i]);
			}
		}
	}

	public void setLeft(String text) {
		this.left = text;
	}

	public void setRight(String text) {
		this.right = text;
	}

	@Override
	public void run() {
		button.setEnabled(false);
		//Get the subject first
		ArrayList<RichSequence> subjectSequences = Utils.fillArrayListSequences(subjectFile);
		RichSequence subject = subjectSequences.get(0);
		progressBar.setValue(0);
		progressBar.setMaximum(nrFiles());
		Subject subjectObject = new Subject(subject,left,right);
		boolean splitCs = false;
		if(left.length()==0 && right.length()==0) {
			splitCs = split;
		}
		for(File f: queries) {
			System.out.println(f.getName());
			Chromatogram chromo = null;
			try {
				chromo = ChromatogramFactory.create(f);
			} catch (Exception e1) {
				//cannot handle that currently
				System.out.println(f.getName()+" gives exception");
				e1.printStackTrace();
				continue;
			}
			NucleotideSequence seq = chromo.getNucleotideSequence();
			QualitySequence quals = chromo.getQualitySequence();
			chromo.getChannelGroup().getAChannel().toString();
			
			//kmerl = null;
			String name = f.getName();
			CompareSequence cs = new CompareSequence(subjectObject, seq.toString(), quals, f.getParent(), true, name);
			cs.setCurrentFile(f);
			cs.setAndDetermineCorrectRange(maxError);
			if(maskLowQuality){
				cs.maskSequenceToHighQuality(left, right);
			}
			if(splitCs && maskLowQualityRemove) {
				ArrayList<CompareSequence> al = cs.maskSequenceToHighQualityRemoveNoFlanks();
				for(CompareSequence tempCS: al) {
					tempCS.setAndDetermineCorrectRange(maxError);
					tempCS.maskSequenceToHighQualityRemove();
					tempCS.determineFlankPositions(true);
					//cs.setAdditionalSearchString(hmAdditional);
					//do we want to print it?
					if(pm.getPropertyBoolean("printCorrectColumnsOnly") && tempCS.getRemarks().length()>0) {
						
					}
					else {
						result.add(tempCS);
					}
					
				}
			}
			else {
				if(maskLowQualityRemove){
					cs.maskSequenceToHighQualityRemove();
				}
				cs.determineFlankPositions(true);
				//cs.setAdditionalSearchString(hmAdditional);
				//do we want to print it?
				if(pm.getPropertyBoolean("printCorrectColumnsOnly") && cs.getRemarks().length()>0) {
					
				}
				else {
					result.add(cs);
				}
			}
			current++;
			progressBar.setValue(current);
		}
		if(queries.size()>1) {
			String refString = subjectObject.getRefString();
			CompareSequence ref = new CompareSequence(subjectObject,refString,null, queries.get(0).getParent(), true, "wt_query");
			ref.setCurrentAlias("Reference", "Reference");
			ref.determineFlankPositions(false);
			result.add(ref);
		}
		
		//progressBar.setIndeterminate(true);
		//checkAndPerformBlast();
		//progressBar.setIndeterminate(false);
		
		ReportPanel rp = new ReportPanel(subjectFile.getName());
		rp.setLeftFlank(left);
		rp.setRightFlank(right);
		rp.setFileChooser(jfc);
		rp.setMasked(maskLowQualityRemove);
		rp.setErrorRate(maxError);
		//should be last
		rp.setup(result);
		rp.setSplit(split);
		rp.removeColumns(pm.getOutputColumns());
		
		//JOptionPane.showMessageDialog(
			//	   null, area, "Result", JOptionPane.PLAIN_MESSAGE);
		button.setEnabled(true);
	}
	
	private String removeUnneededColumns(String s) {
		if(s == null) {
			return null;
		}
		String[] rows = s.split("\n");
		StringBuffer sb = new StringBuffer(5000);
		//System.out.println("removeUnneededColumns");
		boolean[] keepColumns = pm.getOutputColumns();
		for(String row: rows) {
			StringBuffer rowBuffer = new StringBuffer(5000);
			String[] columns = row.split("\t");
			for(int column = 0; column<columns.length;column++) {
				if(keepColumns[column]) {
					if(rowBuffer.length()>0) {
						rowBuffer.append("\t");
					}
					rowBuffer.append(columns[column]);
				}
			}
			sb.append(rowBuffer);
			sb.append("\n");
		}
		return sb.toString();
	}
	
	private void checkAndPerformBlast() {
		String blast = pm.getProperty("blast");
		String blastDb = pm.getProperty("blastDB");
		System.out.println("db: "+blastDb);
		System.out.println("blast"+blast);
		if(blast == null || blastDb == null) {
			return;
		}
		//System.out.println("Performing Blast");
		runningBlast = true;
		HashMap<String, ArrayList<Blast>> blasts = AnalyzedFileController.getBlastResultDB(result, blastDb, blast);
		runningBlast = false;
		for(CompareSequence cs: result) {
			if(blasts.containsKey(cs.getName())) {
				cs.addBlastResult(blasts.get(cs.getName()));
			}
		}
	}
	
	public int currentFileNr() {
		//System.out.println("Returning "+current);
		return current;
	}
	public int nrFiles() {
		return queries.size();
	}
	public String getResultString() {
		StringBuffer sb = new StringBuffer();
		sb.append(CompareSequence.getOneLineHeader());
		for(CompareSequence cs: result) {
			sb.append("\n");
			sb.append(cs.toStringOneLine(""));
		}
		return sb.toString();
	}

	public void setMaxError(double value) {
		this.maxError = value;
	}
	public void setMaskLowQuality(boolean maskLowQuality) {
		this.maskLowQuality = maskLowQuality;
	}
	public void setMaskLowQualityRemove(boolean maskLowQualityRemove) {
		this.maskLowQualityRemove = maskLowQualityRemove;
	}
	public static HashMap<String, ArrayList<Blast>> getBlastResultDB(ArrayList<CompareSequence> seqs, String db, String exec){
		//test for blast
		try {
			PrintWriter qWriter = new PrintWriter("query.fa", "UTF-8");
			for(CompareSequence cs: seqs) {
				if(cs.getInsertion().length()>=15) {
					qWriter.println(">"+cs.getName());
					qWriter.println(cs.getInsertion());
				}
			}
			qWriter.close();
			//String execTotal = exec +" -query query.fa -db "+db+" -word_size 18 -outfmt \"6 std qseq sseq\"";
			String execTotal = exec +" -query query.fa -db "+db+" -word_size 15 -ungapped -outfmt \"6 std qseq sseq\"";
			//System.out.println(execTotal);
			Process p = Runtime.getRuntime().exec(execTotal);
			//Process p = Runtime.getRuntime().exec("ping");
			// any error message?get
            StreamGobbler errorGobbler = new 
                StreamGobbler(p.getErrorStream(), "ERROR", true);            
            
            // any output?
            StreamGobbler outputGobbler = new 
                StreamGobbler(p.getInputStream(), "OUTPUT", true);
                
            // kick them off
            errorGobbler.start();
            outputGobbler.start();
            int exitVal = p.waitFor();
            return outputGobbler.getBlastResult();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public boolean runningBlast() {
		return this.runningBlast;
	}
	public void setProgressBar(JProgressBar progressBar) {
		this.progressBar = progressBar;
	}
	public void setStartButton(JButton analyzeFiles) {
		this.button = analyzeFiles;
	}
	public void setFileChooser(JFileChooser jfc) {
		this.jfc = jfc;
	}
	public boolean rightOK() {
		if(right==null || right.length()==0) {
			return true;
		}
		ArrayList<RichSequence> subjectSequences = Utils.fillArrayListSequences(subjectFile);
		RichSequence subject = subjectSequences.get(0);
		Subject subjectObject = new Subject(subject,left,right);
		return subjectObject.hasRight();
	}
	public boolean leftOK() {
		if(left==null || left.length()==0) {
			return true;
		}
		ArrayList<RichSequence> subjectSequences = Utils.fillArrayListSequences(subjectFile);
		RichSequence subject = subjectSequences.get(0);
		Subject subjectObject = new Subject(subject,left,right);
		return subjectObject.hasLeft();
	}
	public void setSplit(boolean split) {
		this.split = split;
		
	}
}

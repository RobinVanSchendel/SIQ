package gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojava.bio.BioException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequence.IOTools;

import utils.Subject;

public class NGS {
	private File R1, R2, assembled = null, unassF = null, unassR = null, hdr = null;
	private String subject;
	private String leftFlank, rightFlank;
	private String alias;
	private String leftPrimer, rightPrimer;
	private int minPassedPrimer = 5;
	private Subject subjectObject;
	//fixed for now
	private double maxError = 0.05;
	private float status = 0.0f;
	private int rowNr;
	private int totalReads;
	private int correctReads;
	private float percentage;
	private File outputDir;
	private String textStatus = "";
	
	
	public NGS(File R1, File R2, String Subject, String alias, String leftFlank, String rightFlank
			, String leftPrimer, String rightPrimer, int minPassedPrimer) {
		this.R1 = R1;
		this.R2 = R2;
		this.subject = Subject;
		this.leftFlank = leftFlank;
		this.rightFlank = rightFlank;
		this.leftPrimer = leftPrimer;
		this.rightPrimer = rightPrimer;
		this.alias = alias;
		this.minPassedPrimer = minPassedPrimer;
	   }
	public NGS(File R1, File R2, File ass, File unassF, File unassR, String Subject, String alias, String leftFlank, String rightFlank
			, String leftPrimer, String rightPrimer, int minPassedPrimer) {
		this.R1 = R1;
		this.R2 = R2;
		this.assembled = ass;
		this.unassF = unassF;
		this.unassR = unassR;
		this.subject = Subject;
		this.leftFlank = leftFlank;
		this.rightFlank = rightFlank;
		this.leftPrimer = leftPrimer;
		this.rightPrimer = rightPrimer;
		this.alias = alias;
		this.minPassedPrimer = minPassedPrimer;
	   }
	public NGS() {
		
	}
	public NGS(File R1, File R2) {
		this.R1 = R1;
		this.R2 = R2;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		if(this.subject !=null && !this.subject.equals(subject)) {
			subjectObject = null;
		}
		this.subject = subject;
	}
	public String getLeftFlank() {
		return leftFlank;
	}
	public void setLeftFlank(String leftFlank) {
		this.leftFlank = leftFlank;
		if(leftFlank !=null) {
			this.leftFlank = leftFlank.trim();
		}
	}
	public String getRightFlank() {
		return rightFlank;
	}
	public void setRightFlank(String rightFlank) {
		this.rightFlank = rightFlank;
		if(rightFlank !=null) {
			this.rightFlank = rightFlank.trim();
		}
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getLeftPrimer() {
		return leftPrimer;
	}
	public void setLeftPrimer(String leftPrimer) {
		this.leftPrimer = leftPrimer;
		if(leftPrimer !=null) {
			this.leftPrimer = leftPrimer.trim();
		}
	}
	public String getRightPrimer() {
		return rightPrimer;
	}
	public void setRightPrimer(String rightPrimer) {
		this.rightPrimer = rightPrimer;
		if(rightPrimer !=null) {
			this.rightPrimer = rightPrimer.trim();
		}
	}
	public int getMinPassedPrimer() {
		if(this.subjectObject != null) {
			subjectObject.setMinPassedPrimer(minPassedPrimer);
		}
		return minPassedPrimer;
	}
	public void setMinPassedPrimer(int minPassedPrimer) {
		this.minPassedPrimer = minPassedPrimer;
	}
	public boolean getSubjectOK() {
		if(this.subject==null) {
			return false;
		}
		else {
			File f = new File(this.subject);
			if(f.exists()) {
				if(isFasta(f)) {
					return true;
				}
			}
		}
		return false;
	}
	private static boolean isFasta(File f) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(f));
			RichSequenceIterator si = IOTools.readFastaDNA(br, null);
			if(si.hasNext()) {
				//read it
				si.nextRichSequence();
				br.close();
				return true;
			}
			br.close();
		} catch (IOException | NoSuchElementException | BioException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	public boolean R1OK() {
		if(this.R1==null) {
			return false;
		}
		else {
			if(R1.exists()) {
				return true;
			}
			else {
				return false;
			}
		}
	}
	public boolean R2OK() {
		//changed this behaviour now. It does not need to be there
		if(this.R2==null) {
			return true;
		}
		else {
			if(R2.exists()) {
				return true;
			}
			else {
				return false;
			}
		}
	}
	public boolean leftFlankOK() {
		Subject s = getSubjectObject();
		if(s == null) {
			return false;
		}
		s.setLeftFlank(leftFlank, rightFlank);
		//System.out.println("leftFlank in NGS is "+leftFlank);
		//System.out.println("leftFlank in NGS subject is "+s.getLeftFlank());
		
		if(s.hasLeft()) {
			return true;
		}
		return false;
	}
	public Subject getSubjectObject() {
		if(this.subjectObject != null) {
			return subjectObject;
		}
		if(subject == null) {
			return null;
		}
		File subjectFile = new File(subject);
		if(!subjectFile.exists()) {
			return null;
		}
		
		BufferedReader is = null;
		try {
			is = new BufferedReader(new FileReader(subject));
			RichSequenceIterator si = IOTools.readFastaDNA(is, null);
			if(si.hasNext()) {
				RichSequence rs = si.nextRichSequence();
				Subject s = new Subject(rs);
				//System.out.println(s.getSubjectName());
				//System.out.println(s.getString());
				subjectObject = s;
				return subjectObject;
			}
		} catch (FileNotFoundException | NoSuchElementException | BioException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	public boolean rightFlankOK() {
		Subject s = getSubjectObject();
		if(s == null) {
			return false;
		}
		s.setRightFlank(rightFlank);
		if(s.hasRight()) {
			return true;
		}
		return false;
	}
	public boolean leftPrimerOK() {
		Subject s = getSubjectObject();
		if(s == null) {
			return false;
		}
		if(leftPrimer==null) {
			return true;
		}
		s.setLeftPrimer(leftPrimer);
		
		if(s.isLeftPrimerSet()) {
			return true;
		}
		return false;
	}
	public boolean rightPrimerOK() {
		Subject s = getSubjectObject();
		if(s == null) {
			return false;
		}
		if(rightPrimer==null) {
			return true;
		}
		s.setRightPrimer(rightPrimer);
		
		if(s.isRightPrimerSet()) {
			return true;
		}
		return false;
	}
	public double getMaxError() {
		return maxError;
	}
	public File getAssembledFile() {
		if(R1OK() && this.R2==null) {
			return R1;
		}
		//TODO I broke the FLASH thingy now
		if(this.assembledOK()) {
			return this.assembled;
		}
		else {
			return this.getAssembledFileDerived();
		}
		//
	}
	public File getAssembledFileDerived() {
		if(this.assembled != null) {
			return this.assembled;
		}
		return new File(R1.getAbsolutePath()+"_"+this.getRowNumber()+"_assembled.fastq.gz");
	}
	public File getUnassembledFFileDerived() {
		if(this.unassF != null) {
			return this.unassF;
		}
		if(R1 != null) {
			return new File(R1.getAbsolutePath()+"_"+this.getRowNumber()+"_unassembledF.fastq.gz");
		}
		return null;
	}
	public File getUnassembledRFileDerived() {
		if(this.unassR != null) {
			return this.unassR;
		}
		if(R1 != null) {
			return new File(R1.getAbsolutePath()+"_"+this.getRowNumber()+"_unassembledR.fastq.gz");
		}
		return null;
	}
	public boolean assembledOK() {
		//use R1
		if(R1OK() && this.R2 == null) {
			return true;
		}
		else if(this.assembled == null) {
			return false;
		}
		else {
			if(this.assembled.exists()) {
				return true;
			}
			System.out.println("assembled["+this.assembled+"]");
		}
		return false;
	}
	//output needs to be unique, so add the rowNumber. Shitty solution, but ok
	public File getOutput() {
		if(outputDir!=null) {
			return new File(outputDir.getAbsolutePath()+File.separator+getAssembledFile().getName()+"_"+this.getRowNumber()+"_output.txt");
		}
		return new File(getAssembledFile()+"_"+this.getRowNumber()+"_output.txt");
	}
	public File getOutputStats() {
		if(outputDir!=null) {
			return new File(outputDir.getAbsolutePath()+File.separator+getAssembledFile().getName()+"_"+this.getRowNumber()+"_stats_output.txt");
		}
		return new File(getAssembledFile()+"_"+this.getRowNumber()+"_stats_output.txt");
	}
	
	public float getStatus() {
		return status;
	}
	public void setStatus(float status) {
		this.status = status;
	}
	public void setRowNumber(int i) {
		this.rowNr = i;
	}
	public int getRowNumber() {
		return rowNr;
	}
	public int getTotalReads() {
		return this.totalReads;
	}
	public void setTotalReads(int total) {
		this.totalReads = total;
	}
	public int getCorrectReads() {
		return correctReads;
	}
	public void setCorrectReads(int correctReads) {
		this.correctReads = correctReads;
	}
	public float getCorrectPercentage() {
		return percentage;
	}
	public void setPercentage(float correct) {
		this.percentage = correct;
		
	}
	public static NGS getDummy() {
		NGS ngs = new NGS();
		return ngs;
	}
	public boolean filesOK() {
		//check if not the same files
		if(R1equalsR2()) {
			return false;
		}
		if(R1OK() && R2OK()) {
			return true;
		}
		/*
		else if(this.assembledOK()) {
			return true;
		}
		*/
		return false;
	}
	public boolean allOK() {
		return filesOK()
				&& this.getSubjectOK()
				&& this.leftFlankOK() 
				&& this.rightFlankOK()
				&& this.leftPrimerOK()
				&& this.rightPrimerOK();
	}
	public File getR1() {
		return R1;
	}
	public File getR2() {
		return R2;
	}
	public void setR1(File file) {
		this.R1 = file;
	}
	public void setR2(File file) {
		this.R2 = file;
	}
	public boolean unAssembledFOK() {
		if(this.unassF == null) {
			return false;
		}
		else {
			if(this.unassF.exists()) {
				return true;
			}
		}
		return false;
	}
	public boolean unAssembledROK() {
		if(this.unassR == null) {
			return false;
		}
		else {
			if(this.unassR.exists()) {
				return true;
			}
		}
		return false;
	}
	public File getUnassembledFileF() {
		return this.unassF;
	}
	public File getUnassembledFileR() {
		return this.unassR;
	}
	public void setAssembled(File aValue) {
		this.assembled = aValue;
	}
	public void setUnassembledF(File aValue) {
		this.unassF = aValue;
	}
	public void setUnassembledR(File aValue) {
		this.unassR = aValue;
	}
	public void setMaxError(double maxError) {
		if(maxError <0 || maxError>1) {
			return;
		}
		this.maxError = maxError;
	}
	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}
	public File getOutputDir() {
		return this.outputDir;
	}
	public File getOutputTopStats() {
		if(outputDir!=null) {
			return new File(outputDir.getAbsolutePath()+File.separator+getAssembledFile().getName()+"_"+this.getRowNumber()+"_top_stats_output.txt");
		}
		return new File(getAssembledFile()+"_"+this.getRowNumber()+"_top_stats_output.txt");
	}
	public File getHDR() {
		return hdr;
	}
	public boolean R1equalsR2() {
		if(R1 !=null && R2 != null && R1.getAbsolutePath().contentEquals(R2.getAbsolutePath())) {
			return true;
		}
		return false;
	}
	public void setHDR(String hdrString) {
		if(hdrString == null) {
			this.hdr = null;
		}
		else {
			this.hdr = new File(hdrString);
		}
	}
	public boolean getHDROK() {
		if(this.hdr==null) {
			return true;
		}
		else {
			File f = hdr;
			if(f.exists()) {
				return true;
			}
			else {
				return false;
			}
		}
	}
	public void setTextStatus(String status) {
		this.textStatus = status;
	}
	public String getTextStatus() {
		if(textStatus.length()==0 && allOK()) {
			return "Ready";
		}
		return this.textStatus ;
	}
}

package gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.NoSuchElementException;

import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojava.bio.BioException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequence.IOTools;

import utils.Subject;

public class NGS {
	private String file, subject;
	private String leftFlank, rightFlank;
	private String alias;
	private String leftPrimer, rightPrimer;
	private int minPassedPrimer;
	private Subject subjectObject;
	public NGS(String File, String Subject, String leftFlank, String rightFlank,
			String alias, String leftPrimer, String rightPrimer, int minPassedPrimer) {
		this.file = File;
		this.subject = Subject;
		this.leftFlank = leftFlank;
		this.rightFlank = rightFlank;
		this.leftPrimer = leftPrimer;
		this.rightPrimer = rightPrimer;
		this.alias = alias;
		this.minPassedPrimer = minPassedPrimer;
	   }
	public NGS(File f) {
		this.file = f.getAbsolutePath();
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
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
		this.leftFlank = leftFlank.trim();
	}
	public String getRightFlank() {
		return rightFlank;
	}
	public void setRightFlank(String rightFlank) {
		this.rightFlank = rightFlank.trim();
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
		this.leftPrimer = leftPrimer.trim();
	}
	public String getRightPrimer() {
		return rightPrimer;
	}
	public void setRightPrimer(String rightPrimer) {
		this.rightPrimer = rightPrimer.trim();
	}
	public int getMinPassedPrimer() {
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
				return true;
			}
			else {
				return false;
			}
		}
	}
	public boolean FileOK() {
		if(this.file==null) {
			return false;
		}
		else {
			File f = new File(this.file);
			if(f.exists()) {
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
		s.setLeftFlank(leftFlank);
		if(s.hasLeft()) {
			return true;
		}
		return false;
	}
	private Subject getSubjectObject() {
		if(this.subjectObject != null) {
			return subjectObject;
		}
		if(subject == null) {
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
		s.setRightPrimer(rightPrimer);
		
		if(s.isRightPrimerSet()) {
			return true;
		}
		return false;
	}
	
}

package utils;

import org.biojavax.bio.seq.RichSequence;

public class Subject {
	private String subject, subjectName, subjectComments;
	private String leftFlank, rightFlank;
	private String leftPrimer, rightPrimer;
	
	//calculated values
	private boolean leftSet = false;
	private boolean rightSet = false;
	private int endOfLeftFlank, startOfRightFlank;
	private boolean leftRightIsOK = false;
	private int startOfLeftPrimer;
	private int endOfLeftPrimer;
	private boolean leftPrimerSet;
	private int startOfRightPrimer;
	private int endOfRightPrimer;
	private boolean rightPrimerSet;
	private int minPassedPrimer;
	
	public Subject(RichSequence subject) {
		this.subject = subject.seqString().toString();
		this.subjectName = subject.getName();
		this.subjectComments = subject.getDescription();
		if(subjectComments == null) {
			this.subjectComments = subjectName;
		}
	}
	public Subject(RichSequence subject, String left, String right) {
		this.subject = subject.seqString().toString();
		this.subjectName = subject.getName();
		this.subjectComments = subject.getDescription();
		if(subjectComments == null) {
			this.subjectComments = subjectName;
		}
		this.setLeftFlank(left);
		this.setRightFlank(right);
		
		checkLeftRight();
	}
	public String getSubjectName() {
		return this.subjectName;
	}
	public String getSubjectComments() {
		return this.subjectComments;
	}
	public int getEndOfLeftFlank() {
		if(isOK()) {
			return this.endOfLeftFlank;
		}
		return 0;
	}
	private boolean isOK() {
		return leftSet && rightSet;
	}
	public int getStartOfRightFlank() {
		if(isOK()) {
			return this.startOfRightFlank;
		}
		return 0;
	}
	public void setRightFlank(String tempRightFlank) {
		this.rightFlank = tempRightFlank.toLowerCase();
		if(this.rightFlank.length()==0) {
			this.rightFlank = null;
			rightSet = false;
			return;
		}
		if(subject.indexOf(rightFlank)<0) {
			System.err.println("Cannot find rightFlank "+rightFlank);
			//System.exit(0);
		}
		else {
			this.startOfRightFlank = subject.indexOf(rightFlank);
			rightSet = true;
		}
	}
	public void setLeftPrimer(String tempLeftPrimer) {
		this.leftPrimer = tempLeftPrimer.toLowerCase();

		if(subject.indexOf(this.leftPrimer)<0) {
			System.err.println("Cannot find leftPrimer "+leftPrimer);
			//System.exit(0);
		}
		else {
			this.startOfLeftPrimer = subject.indexOf(leftPrimer);
			this.endOfLeftPrimer = startOfLeftPrimer+leftPrimer.length();
			this.leftPrimerSet = true;
			
			//System.out.println("LeftPrimer "+startOfLeftPrimer+":"+endOfLeftPrimer);
		}
	}
	public void setRightPrimer(String tempRightPrimer) {
		this.rightPrimer = tempRightPrimer.toLowerCase();
		//take rc
		this.rightPrimer = Utils.reverseComplement(rightPrimer);
		
		if(subject.indexOf(this.rightPrimer)<0) {
			System.err.println("Cannot find rightPrimer "+rightPrimer);
			//System.exit(0);
		}
		else {
			this.startOfRightPrimer = subject.indexOf(rightPrimer);
			this.endOfRightPrimer = startOfRightPrimer+rightPrimer.length();
			this.rightPrimerSet = true;
			//System.out.println("RightPrimer "+startOfRightPrimer+":"+endOfRightPrimer);
		}
	}
	public void setLeftFlank(String tempLeftFlank) {
		this.leftFlank = tempLeftFlank.toLowerCase();
		if(this.leftFlank.length()==0) {
			this.leftFlank = null;
			leftSet = false;
			return;
		}
		if(subject.indexOf(this.leftFlank)<0) {
			//System.err.println("Cannot find leftFlank "+leftFlank+" "+subject.indexOf(this.leftFlank));
			//System.err.println(subject);
			//System.err.println(subject.length());
			//System.exit(0);
		}
		else {
			this.endOfLeftFlank = subject.indexOf(leftFlank)+leftFlank.length();
			//System.out.println("endOfLeftFlank "+endOfLeftFlank);
			leftSet = true;
		}
	}
	public boolean hasLeft() {
		return leftSet;
	}
	public boolean hasRight() {
		return rightSet;
	}
	public boolean hasLeftRight() {
		return leftSet && rightSet;
	}
	public boolean isStringUnique(String string) {
		int index = subject.indexOf(string);
		if(index<0) {
			System.err.println("We cannot even find string once");
			System.exit(0);
		}
		else {
			int index2 = subject.indexOf(string, index+1);
			if(index2>=0) {
				return false;
			}
		}
		return true;	
	}
	public String getString() {
		return subject;
	}
	public String getLeftFlank() {
		return this.leftFlank;
	}
	public String getRightFlank() {
		return this.rightFlank;
	}
	private void checkLeftRight() {
		if(!hasLeftRight()) {
			this.leftRightIsOK  = true;
		}
		else{
			if(this.getEndOfLeftFlank()==this.getStartOfRightFlank()) {
				this.leftRightIsOK = true;
			}
			else if(this.getStartOfRightFlank() > getEndOfLeftFlank()) {
				this.leftRightIsOK = true;
			}
			//this can now happen as well, so maybe the check is nonsense now
			else {
				this.leftRightIsOK = true;
			}
		}
		if(!leftRightIsOK) {
			System.err.println("Something wrong with left or right "+getLeftFlank()+":"+getRightFlank());
		}
	}
	public void setMinPassedPrimer(long minPassedPrimer) {
		this.minPassedPrimer = (int) minPassedPrimer;
	}
	public int getMinLocationStartEvent() {
		return this.endOfLeftPrimer+this.minPassedPrimer;
	}
	public int getMinLocationEndEvent() {
		return this.startOfRightPrimer-this.minPassedPrimer;
		
	}
	public boolean isLeftPrimerSet() {
		return leftPrimerSet;
	}
	public boolean isRightPrimerSet() {
		return rightPrimerSet;
	}
	public boolean hasPrimers() {
		return this.leftPrimerSet && this.rightPrimerSet;
	}
	public boolean evenStartsBehindPrimer(int delStart) {
		if(minPassedPrimer == 0) {
			return true;
		}
		return delStart>getMinLocationStartEvent();
	}
	public boolean seqStartsWithinLeftPrimer(int matchStart) {
		return matchStart>=this.startOfLeftPrimer && matchStart <= this.endOfLeftPrimer;
	}
	public boolean seqStartsWithinRightPrimer(int matchEnd) {
		return matchEnd>=this.startOfRightPrimer && matchEnd<= this.endOfRightPrimer;
	}
	public boolean evenEndsBeforePrimer(int delEnd) {
		if(minPassedPrimer == 0) {
			return true;
		}
		return delEnd<getMinLocationEndEvent();
	}
	public String toString() {
		String ret = this.subjectName+" "+this.getLeftFlank()+" "+this.getRightFlank()+" "+this.leftPrimer+" "+this.rightPrimer;
		return ret;
	}
}

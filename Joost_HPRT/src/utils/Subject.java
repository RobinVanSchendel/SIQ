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
	private int endOfRightFlank;
	private String errorMessage = "";
	private boolean exitOnError = false;
	
	public Subject(RichSequence subject) {
		this.subject = subject.seqString().toString();
		this.subjectName = subject.getName();
		this.subjectComments = subject.getDescription();
		if(subjectComments == null) {
			this.subjectComments = subjectName;
		}
	}
	public Subject(RichSequence subject, String left, String right, boolean exitOnError) {
		this(subject, left, right);
		this.exitOnError = exitOnError;
		if(this.exitOnError && errorMessage.length()>0) {
			System.exit(0);
		}
	}
	public Subject(RichSequence subject, String left, String right) {
		this.subject = subject.seqString().toString();
		this.subjectName = subject.getName();
		this.subjectComments = subject.getDescription();
		if(subjectComments == null) {
			this.subjectComments = subjectName;
		}
		if(left == null) {
			left = "";
		}
		this.setLeftFlank(left);
		if(right == null) {
			right = "";
		}
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
			rightSet = false;
		}
		else {
			this.startOfRightFlank = subject.indexOf(rightFlank);
			this.endOfRightFlank = startOfRightFlank+rightFlank.length();
			rightSet = true;
		}
	}
	public void setLeftPrimer(String tempLeftPrimer) {
		this.leftPrimer = tempLeftPrimer.toLowerCase();
		//can also be in reverse complement orientation
		if(subject.indexOf(this.leftPrimer)<0) {
			//check reverse complement
			String leftPrimerRC = Utils.reverseComplement(leftPrimer);
			//really cannot find it
			if(subject.indexOf(leftPrimerRC)<0) {
				System.err.println("Cannot find leftPrimer "+leftPrimer);
				if(this.exitOnError) {
					System.exit(0);
				}
				this.leftPrimerSet = false;
				//still have to return otherwise primer gets set
				return;
			}
			else {
				this.leftPrimer = leftPrimerRC;
			}
		}
		this.startOfLeftPrimer = subject.indexOf(leftPrimer);
		this.endOfLeftPrimer = startOfLeftPrimer+leftPrimer.length();
		this.leftPrimerSet = true;
	}
	public void setRightPrimer(String tempRightPrimer) {
		this.rightPrimer = tempRightPrimer.toLowerCase();
		//take rc
		if(rightPrimer!=null) {
			this.rightPrimer = Utils.reverseComplement(rightPrimer);
		}
		if(subject.indexOf(this.rightPrimer)<0) {
			//try normal orientation
			String rightPrimerTemp = tempRightPrimer.toLowerCase();
			if(subject.indexOf(rightPrimerTemp)<0) {
				System.err.println("Cannot find rightPrimer "+rightPrimer);
				this.rightPrimerSet = false;
				if(this.exitOnError) {
					System.exit(0);
				}
				//still have to return here
				return;
			}
			else {
				this.rightPrimer = rightPrimerTemp;
			}
			//System.exit(0);
		}
		this.startOfRightPrimer = subject.indexOf(rightPrimer);
		this.endOfRightPrimer = startOfRightPrimer+rightPrimer.length();
		this.rightPrimerSet = true;
	}
	public void setLeftFlank(String tempLeftFlank) {
		this.leftFlank = tempLeftFlank.toLowerCase();
		if(this.leftFlank.length()==0) {
			this.leftFlank = null;
			leftSet = false;
			return;
		}
		if(subject.indexOf(this.leftFlank)<0) {
			System.err.println("Cannot find leftFlank "+leftFlank+" "+subject.indexOf(this.leftFlank));
			//System.err.println(subject);
			//System.err.println(subject.length());
			//System.exit(0);
			leftSet = false;
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
		String message = "";
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
			//they cannot overlap
			else if(this.getStartOfRightFlank()<this.getEndOfLeftFlank() && this.getEndOfRightFlank() > this.getEndOfLeftFlank()) {
				this.leftRightIsOK = false;
				message = "the flanks overlap and that is not allowed";
			}
			//this can now happen as well, so maybe the check is nonsense now
			else {
				this.leftRightIsOK = true;
			}
		}
		if(!leftRightIsOK) {
			this.errorMessage = "Something wrong with left or right "+getLeftFlank()+":"+getRightFlank()+"\n"+message;
			System.err.println(errorMessage);
		}
	}
	private int getEndOfRightFlank() {
		return this.endOfRightFlank;
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
		//if 0 this filter should be disabled
		if(minPassedPrimer == 0) {
			return true;
		}
		return delStart>getMinLocationStartEvent();
	}
	public boolean seqStartsWithinLeftPrimer(int matchStart) {
		/*
		System.out.println(matchStart>=this.startOfLeftPrimer);
		System.out.println(matchStart <= this.endOfLeftPrimer);
		System.out.println(matchStart);
		System.out.println(endOfLeftPrimer);
		*/
		return matchStart>=this.startOfLeftPrimer && matchStart <= this.endOfLeftPrimer;
	}
	public boolean seqStartsWithinRightPrimer(int matchEnd) {
		return matchEnd>=this.startOfRightPrimer && matchEnd<= this.endOfRightPrimer;
	}
	public boolean evenEndsBeforePrimer(int delEnd) {
		//if 0 this filter should be disabled
		if(minPassedPrimer == 0) {
			return true;
		}
		return delEnd<getMinLocationEndEvent();
	}
	public String toString() {
		String ret = this.subjectName+" "+this.getLeftFlank()+" "+this.getRightFlank()+" "+this.leftPrimer+" "+this.rightPrimer;
		return ret;
	}
	public void swapPrimersIfNeeded() {
		if(this.leftPrimerSet && this.rightPrimerSet) {
			if(this.startOfLeftPrimer>this.startOfRightPrimer) {
				String leftPrimerTemp = this.leftPrimer;
				String rightPrimerTemp = this.rightPrimer;
				this.setLeftPrimer(rightPrimerTemp);
				this.setRightPrimer(leftPrimerTemp);
			}
		}
	}
}

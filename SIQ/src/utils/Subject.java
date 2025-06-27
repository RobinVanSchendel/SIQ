package utils;

import java.util.ArrayList;

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
	private ArrayList<RichSequence> hdrList;
	private ArrayList<CompareSequence> hdrListCS;
	private KMERLocation kmerl;
	//for testing it is now true
	private boolean isLongRead = false;
	private String leftPrimerMatchPacBio;
	private String rightPrimerMatchPacBio;
	
	public Subject(RichSequence subject) {
		this.subject = subject.seqString().toString().toUpperCase();
		this.subjectName = subject.getName();
		this.subjectComments = subject.getDescription();
		if(subjectComments == null) {
			this.subjectComments = subjectName;
		}
		kmerl = new KMERLocation(this.subject);
	}
	public Subject(RichSequence subject, String left, String right, boolean exitOnError) {
		this(subject, left, right);
		this.exitOnError = exitOnError;
		if(this.exitOnError && errorMessage.length()>0) {
			System.exit(0);
		}
	}
	public Subject(RichSequence subject, String left, String right) {
		this(subject);
		if(left == null) {
			left = "";
		}
		if(right == null) {
			right = "";
		}
		this.setLeftFlank(left, right);
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
		if(tempRightFlank!=null && tempRightFlank.length()>=15) {
			this.rightFlank = tempRightFlank.toUpperCase();
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
		else {
			this.startOfRightFlank = -1;
			this.endOfRightFlank = -1;
			rightSet = false;
			rightFlank = null;
		}
	}
	public void setLeftPrimer(String tempLeftPrimer) {
		if(tempLeftPrimer !=null && tempLeftPrimer.length()>=15) {
			this.leftPrimer = tempLeftPrimer.toUpperCase();
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
			int lastPos = subject.lastIndexOf(leftPrimer);
			//check if the primer occurs in the subject or if it occur multiple times
			if(startOfLeftPrimer == -1 || startOfLeftPrimer != lastPos) {
				this.startOfLeftPrimer = -1;
				this.endOfLeftPrimer = -1;
				this.leftPrimerSet = false;
				leftPrimer = null;
				return;
			}
			
			this.endOfLeftPrimer = startOfLeftPrimer+leftPrimer.length();
			
			int longReadStart = startOfLeftPrimer;
			int longReadEnd = endOfLeftPrimer+this.minPassedPrimer;
			if(longReadEnd > subject.length()) {
				longReadStart = startOfLeftPrimer - this.minPassedPrimer;
				longReadEnd = endOfLeftPrimer;
			}
			//another safety
			this.leftPrimerMatchPacBio = subject.substring(longReadStart, longReadEnd);
			this.leftPrimerSet = true;
		}
		else {
			this.startOfLeftPrimer = -1;
			this.endOfLeftPrimer = -1;
			this.leftPrimerSet = false;
			leftPrimer = null;
		}
		
	}
	public void setRightPrimer(String tempRightPrimer) {
		if(tempRightPrimer!=null && tempRightPrimer.length()>=15) {
			this.rightPrimer = tempRightPrimer.toUpperCase();
			//take rc
			if(rightPrimer!=null) {
				this.rightPrimer = Utils.reverseComplement(rightPrimer);
			}
			if(subject.indexOf(this.rightPrimer)<0) {
				//try normal orientation
				String rightPrimerTemp = tempRightPrimer.toUpperCase();
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
			int lastPos = subject.lastIndexOf(rightPrimer);
			//check if the primer occurs in the subject or if it occur multiple times
			if(startOfRightPrimer == -1 || startOfRightPrimer != lastPos) {
				startOfRightPrimer = -1;
				endOfRightPrimer = -1;
				rightPrimerSet = false;	
				rightPrimer = null;
				return;
			}
			
			this.endOfRightPrimer = startOfRightPrimer+rightPrimer.length();
			int longReadStart = startOfRightPrimer-this.minPassedPrimer;
			int longReadSend = endOfRightPrimer;
			if(longReadStart<0) {
				longReadStart = 0;
				longReadSend = longReadSend + this.minPassedPrimer;
			}
			this.rightPrimerMatchPacBio = subject.substring(longReadSend,longReadSend);
			this.rightPrimerSet = true;
		}
		else {
			startOfRightPrimer = -1;
			endOfRightPrimer = -1;
			rightPrimerSet = false;	
			rightPrimer = null;
		}
		
		//System.out.println("rightPrimerSet "+rightPrimerSet+" "+rightPrimer);
	}
	public void setLeftFlank(String tempLeftFlank, String right) {
		if(tempLeftFlank!=null && tempLeftFlank.length()>=15) {
			this.leftFlank = tempLeftFlank.toUpperCase();
			if(subject.indexOf(this.leftFlank)<0) {
				System.err.println("Cannot find leftFlank "+leftFlank+" "+subject.indexOf(this.leftFlank));
				System.err.println(subject);
				//System.err.println(subject.length());
				//System.exit(0);
				leftSet = false;
			}
			//found
			else {
				this.endOfLeftFlank = subject.indexOf(leftFlank)+leftFlank.length();
				//System.out.println("endOfLeftFlank "+endOfLeftFlank);
				//check
				int first = subject.indexOf(leftFlank);
				int last = subject.lastIndexOf(leftFlank);
				if(first != last) {
					//check if we can select it based on the right
					String tempLeftRight = leftFlank+right.toUpperCase();
					int firstLR = subject.indexOf(tempLeftRight);
					int lastLR = subject.lastIndexOf(tempLeftRight);
					if(firstLR == lastLR) {
						this.endOfLeftFlank = firstLR+leftFlank.length();
						leftSet = true;
					}
					else {
						System.err.println("leftFlank seen multiple times");
						leftSet = false;
					}
				}
				else {
					leftSet = true;
				}
			}
		}
		else {
			leftFlank = null;
			leftSet = false;
			endOfLeftFlank = -1;
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
	public String getRefString() {
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
		//overwrite if there are no primers
		if(!this.hasPrimers()) {
			return -1;
		}
		else if(this.endOfLeftPrimer==0 && this.minPassedPrimer == 0) {
			return -1;
		}
		return this.endOfLeftPrimer+this.minPassedPrimer;
	}
	public int getMinLocationEndEvent() {
		if(this.startOfRightPrimer==0 && this.minPassedPrimer == 0) {
			return -1;
		}
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
		String ret = this.subjectName+" "+this.getLeftFlank()+" "+this.getEndOfLeftFlank()+" "+this.getRightFlank()+" "+this.getStartOfRightFlank()+" "+this.leftPrimer+" "+this.rightPrimer;
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
	/**Method to add an HDR sequence to capture events that are HDR event
	 * 
	 * @param hdr the RichSequence object to add, only unique sequences are added
	 */
	public void addHDR(RichSequence hdr) {
		//check for null
		if(this.hdrList == null) {
			hdrList = new ArrayList<RichSequence>();
		}
		//check for errors
		boolean error = false;
		
		//the leftPrimer needs to be present in the HDR sequence
		if(hdr.seqString().toUpperCase().indexOf(leftPrimer)<0) {
			System.err.println("HDR error, left primer is not present in HDR sequence: "+hdr.getName()+" "+this.getLeftPrimer() +" "+hdr.seqString().toUpperCase().indexOf(leftPrimer));
			error = true;
		}
		//the rightPrimer needs to be present in the HDR sequence
		if(hdr.seqString().toUpperCase().indexOf(rightPrimer)<0) {
			System.err.println("HDR error, right primer is not present in HDR sequence: "+hdr.getName()+" "+this.getRightPrimer() +" "+hdr.seqString().toUpperCase().indexOf(rightPrimer));
			error = true;
		}
		
		//check if it is unique
		for(RichSequence r: hdrList) {
			if(r.seqString().toUpperCase().contentEquals(hdr.seqString().toUpperCase())) {
				System.err.println("Error while adding HDR sequence: sequence already exists in the HDR list "+hdr.getName());
				error = true;
			}
		}
		
		//add if no error
		if(!error) {
			hdrList.add(hdr);
		}
	}
	/** This method is there to retrieve the HDR event that this CompareSequence matches to.
	 *  HDR events are unique, so as soon as a match is found it is returned
	 * @param cs the CompareSequence to match against
	 * @return null if no HDR match can be found
	 */
	public CompareSequence getHDREvent(CompareSequence cs) {
		if(this.hdrList == null || hdrList.size() == 0) {
			return null;
		}
		//make HDR CompareSequence objects, this is only done once
		if(hdrListCS == null) {
			//initialize
			hdrListCS = new ArrayList<CompareSequence>();
			for(RichSequence hdr: this.hdrList) {
				//create a CompareSequence object, be sure to save the HDR name
				//bug as RichSequence will return lowercase DNA, so make it uppercase
				//set the name of the HDR event, no spaces allowed!
				String hdrName = hdr.getName().replaceAll(" ", "_");
				CompareSequence hdrCS = new CompareSequence(this,hdr.seqString().toUpperCase(),null, null, true, hdrName);
				hdrCS.determineFlankPositions(false);
				//add to list
				hdrListCS.add(hdrCS);
			}
		}
		//if there are HDR events, start matching
		if(hdrListCS !=null) {
			//iterate HDR events
			for(CompareSequence hdr: this.hdrListCS) {
				if(hdr.getDel().contentEquals(cs.getDel()) && hdr.getInsertion().contentEquals(cs.getInsertion())) {
					return hdr;
				}
			}
		}
		//no match
		return null;
	}
	public KMERLocation getKmerl() {
		return kmerl;
	}
	/** Method that is able to detect if a 1bp mismatch may explain the sequence
	 *  NOTE: only works if a single HDR event was provided
	 * 
	 * @param cs the sequence to compare with
	 * @return the number of mismatches found comparing the first HDR sequence to the given sequence
	 */
	public int getHDREventOneMismatch(CompareSequence cs) {
		if(this.hdrList == null || this.hdrList.size() != 1) {
			return -1;
		}
		//test if it is similar to hdrsequence
		//String[] lcs = Utils.longestCommonSubstringAllowMismatch(cs.getRaw(), hdr.seqString().toUpperCase(), 1, true);
		RichSequence hdr = hdrList.get(0);
		String query = cs.getRaw().replaceAll("X", "");
		String subject = hdr.seqString().toUpperCase();
		
		int startPart = 20;
		int endPart = 20;
		int start = -1;
		int end = -1;
		//System.out.println(query);
		//System.out.println(leftPrimer);
		//System.out.println(rightPrimer);
		int startQuery = -1;
		int endQuery = -1;
		
		if(this.leftPrimerSet && rightPrimerSet) {
			//not completely correct as query could be shortened
			start = subject.indexOf(leftPrimer);
			end = subject.indexOf(rightPrimer);
			//also find the left and rightPrimer in the query
			String leftPrimerPart = leftPrimer.substring(0, 15);
			startQuery = query.indexOf(leftPrimerPart);
			//primer can be much longer, so cut it
			endQuery = query.indexOf(rightPrimer.substring(0, 15));
			
			//could be a mismatch within the start of the primer sequence
			if(startQuery == -1 && query.length()>=20) {
				String lcs = Utils.longestCommonSubstring(query.substring(0, 20), leftPrimerPart);
				if(lcs.length()>=8) {
					int pos = query.indexOf(lcs);
					int primerPos = leftPrimerPart.indexOf(lcs);
					startQuery = pos;
					//update the subject position accordingly
					start+=primerPos;
				}
			}
			
			//if not found, this will not work at all
			//or at the wrong location
			//ensure the queries do make sense (within the first 15bp or in the last 15bp)
			if(startQuery < 0 || endQuery < 0 || startQuery >= endQuery
					|| startQuery >= 15 || endQuery < (query.length()-15)) {
				return -1;
			}
			query = query.substring(startQuery, endQuery);
		}
		else {
			start = subject.indexOf(query.substring(0, startPart));
			end = subject.indexOf(query.substring(query.length()-endPart));
		}
		//now check positions
		if(start<0 || end <0 || end<start) {
			return -1;
		}
		//otherwise update ends and go on
		else {
			if(this.leftPrimerSet && rightPrimerSet) {
				//no this is not always correct
			}
			else {
				//does this still work?
				end+=endPart;
			}
		}
		String subjectSub = subject.substring(start,end);
		
		//shortcut
		if(Math.abs(query.length()-subjectSub.length())>1) {
			return -1;
		}
		int mm = 0;
		int maxLength = Math.min(query.length(), subjectSub.length());
		for(int i = 0;i<maxLength;i++) {
			if(query.charAt(i)!=subjectSub.charAt(i)) {
				mm++;
				if(mm>1) {
					//can also be a 1bp del or insert, so check that now
					//for now hardcoded 1bp difference
					boolean mismatches =  checkSkipOneBase(query,subjectSub,1);
					if(mismatches) {
						return 1;
					}
				}
			}
		}
		return mm;
	}
	private static boolean checkSkipOneBase(String s1, String s2, int maxSkips) {
		boolean skipS1 = true;
		if(s2.length()>s1.length()) {
			//skip S2
			skipS1 = false;
		}
		int maxLength = Math.min(s1.length(), s2.length());
		int skip = 0;
		int indexS1 = 0;
		int indexS2 = 0;
		for(int i = 0;i<maxLength;i++) {
			if(s1.charAt(indexS1)==s2.charAt(indexS2)) {
				indexS1++;
				indexS2++;
			}
			else {
				if(skipS1) {
					skip++;
					indexS1++;
				}
				else {
					skip++;
					indexS2++;
				}
				if(skip>maxSkips) {
					return false;
				}
			}
		}
		return true;
	}
	public boolean isLongRead() {
		return isLongRead ;
	}
	public void setLongRead(boolean longread) {
		this.isLongRead = longread;
	}
	public String printPrimers() {
		String ret = "";
		if(this.hasPrimers()) {
			ret+=this.leftPrimer+" "+this.startOfLeftPrimer+"\n";
			ret+=this.rightPrimer+" "+this.startOfRightPrimer;
		}
		return ret;
	}
	public boolean hasHDR() {
		return hdrList != null && hdrList.size()>0;
	}
	public String getLeftPrimer() {
		return this.leftPrimer;
	}
	public String getRightPrimer() {
		return this.rightPrimer;
	}
	public String getLeftPrimerMatchPacBio() {
		return leftPrimerMatchPacBio;
	}
	public String getRightPrimerMatchPacBio() {
		return rightPrimerMatchPacBio;
	}
}

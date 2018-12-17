package utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojavax.bio.seq.RichSequence;
import org.jcvi.jillion.core.Range;
import org.jcvi.jillion.core.qual.QualitySequence;
import org.jcvi.jillion.core.qual.QualitySequenceBuilder;

import gui.PropertiesManager;
import utils.InsertionSolverTwoSides;


public class CompareSequence {

	private RichSequence subject;
	private String query;
	private final String queryName;
	private String rightFlank = "", del = "", insert = "", remarks = "";
	private Left leftFlank;
	private int minimumSizeWithoutLeftRight = 15; // was 30
	private int minimumSizeWithLeftRight = 15;
	private int pamSiteLocation;
	private final static String replacementFlank = "FLK1";
	private String leftSite, rightSite;
	public final static int minimalRangeSize = 40;
	private final static double maxMismatchRate = 0.1;
	private static final int ALLLOWEDJUMPDISTANCE = 1;
	private static final int MAXIMUMTRIESSOLVING = 5;
	//this introduces possible problems... I am aware of this 'feature' missing SNVs 30bp away from flanks
	private static final int MINIMUMSECONDSIZE = 30;
	private ArrayList<Range> ranges = new ArrayList<Range>();
	private boolean masked = false;
	private QualitySequence quals;
	private InsertionSolverTwoSides is;
	private boolean leftRightIsOK = false;
	private boolean leftRightIsFilled = false;
	private boolean entireQueryUsed = false;
	
	
	public enum Type {WT, SNV, DELETION, DELINS, INSERTION, UNKNOWN, TANDEMDUPLICATION, TANDEMDUPLICATION_COMPOUND, TANDEMDUPLICATION_MULTI};
	public String dir;
	private String cutType;
	private Vector<Sequence> additionalSearchSequence;
	private boolean possibleDouble = false;
	//be careful to change also the value for minSizeInsertionSolver
	private final int solveInsertStart = -100;
	private final int solveInsertEnd = 100;
	//changed to 6!
	private final int minSizeInsertionSolver = 6;
	
	private String fileName;
	private boolean reversed = false;
	private boolean jumpedRight = false;
	private boolean jumpedLeft = false;
	private String alias = "";
	private KMERLocation kmerl;
	//Default = yes!
	private boolean allowJump = true;
	
	public CompareSequence(RichSequence subject, String query, QualitySequence quals, String left, String right, String pamSite, String dir, boolean checkReverse, String queryName, KMERLocation kmerl) {
		this.queryName = queryName;
		this.subject = subject;
		this.query = query.toLowerCase();
		this.dir = dir;
		if(left == null){
			System.err.println("Specified left is null, that is not allowed");
			System.exit(0);
		}
		if(right == null){
			System.err.println("Specified left is null, that is not allowed");
			System.exit(0);
		}
		this.quals = quals;
		this.leftSite = left.toLowerCase();
		this.rightSite = right.toLowerCase();
		checkLeftRight();
		this.kmerl = kmerl;
		if(pamSite != null){
			this.pamSiteLocation = Integer.parseInt(pamSite.substring(pamSite.indexOf(":")+1));
		}
		else{
			this.pamSiteLocation = 0;
		}
		if(checkReverse) {
			checkAndPossibleReverse();
		}
	}
	private void checkLeftRight() {
		if(leftSite == null || rightSite == null || leftSite.length()==0 || rightSite.length()==0) {
			this.leftRightIsFilled = false;
			this.leftRightIsOK = true;
		}
		else{
			this.leftRightIsFilled = true;
			int indexLeft = subject.seqString().indexOf(leftSite);
			int indexRight = subject.seqString().indexOf(rightSite);
			if(indexLeft+leftSite.length()==indexRight) {
				this.leftRightIsOK = true;
			}
			else if(indexRight > indexLeft) {
				
				this.leftRightIsOK = true;
			}
			else {
				this.leftRightIsOK = false;
			}
		}
		if(!leftRightIsOK) {
			System.err.println("Something wrong with left or right "+leftSite+":"+rightSite);
			this.setRemarks("Something wrong with left or right");
			//System.exit(0);
		}
	}
	private String longestCommonSubstring(String s1, String s2) {
		return Utils.longestCommonSubstring(s1,s2);
		
	}
	private void checkAndPossibleReverse() {
		String queryS = query;
		String subjectS = subject.seqString().toString();
		String lcs = "";
		if(kmerl == null) {
			lcs = this.longestCommonSubstring(subjectS, queryS);
		}
		else {
			LCS lcsObject = kmerl.getLCS(queryS);
			if(lcsObject == null) {
				lcs = null;
			}
			else {
				lcs = lcsObject.getString();
			}
		}
		//take the reverse complement of the query.
		//sometimes that is not correct, although we don't really know if that is true
		if(lcs == null || lcs.length() <40) {
			String revCom = Utils.reverseComplement(queryS);
			String rc = null;
			if(kmerl == null) {
				rc = longestCommonSubstring(revCom, subjectS);
			}
			else {
				LCS lcsObject = kmerl.getLCS(revCom);
				if(lcsObject == null) {
					rc = null;
				}
				else {
					rc = lcsObject.getString();
				}
			}
			//nothing to be done
			if(rc == null) {
				return;
			}
			int altSize = rc.length();
			if( lcs == null || altSize>lcs.length()){
				query = revCom;
				//also turn around the quality
				if(quals!= null){
					QualitySequenceBuilder qsb = new QualitySequenceBuilder(quals);
					quals = qsb.reverse().build();
				}
				this.reversed  = true;
			}
		}
	}
	public void determineFlankPositions(){
		int leftPos = -2;
		int rightPos = -2;
		Left flankOne = null;
		String flankTwo = "";
		boolean print = false;
		if(this.leftRightIsFilled){
			//System.out.println("leftRightIsFilled");
			leftPos = subject.seqString().indexOf(leftSite)+leftSite.length();
			//misuse the pamSiteLocation to make it relative to the left position
			if(this.pamSiteLocation == 0){
				this.pamSiteLocation = leftPos;
			}
			if(kmerl == null) {
				flankOne = findLeft(subject.seqString().substring(0, leftPos), query);
			}
			else {			
				Left kmerFlankOne = kmerl.getMatchLeft(query, leftPos, allowJump);
				//System.out.println("leftKMER:"+kmerFlankOne);
				if(kmerFlankOne != null) {
					this.jumpedLeft = kmerFlankOne.getJumped();
				}
				flankOne = kmerFlankOne;
				//if(print) {
				//	System.out.println("left: "+flankOne);
				//}
			}
			/*
			if(kmerFlankOne != null && !flankOne.getString().equals(kmerFlankOne.getString())) {
				System.out.println(flankOne.getString());
				System.out.println(kmerFlankOne.getString());
				System.out.println("======");
				//System.exit(0);
			}
			*/
			//System.out.println("======");
			//System.out.println(flankOne);
			String seqRemain = "";
			if(flankOne == null) {
				this.setRemarks("Cannot find the Left flank of the event");
				return;
			}
			else {
				seqRemain = query.replace(flankOne.getString(), replacementFlank);
				//some error checking on the length of the rightFlank
				int indexRemain = seqRemain.indexOf(replacementFlank);
				//System.out.println(indexRemain);
				if(indexRemain>0){
					String queryRemain = seqRemain.substring(indexRemain+replacementFlank.length());
					//System.out.println("here:"+queryRemain);
					if(queryRemain.length() == 0 || queryRemain.startsWith("n") || queryRemain.startsWith("x")){
						this.setRemarks("We have nothing to search for on the rightFlank");
					}
				}
			}
			//the rightPos can theoretically be incorrect. here it it better to use the subjectEnd of flankOne
			if(kmerl == null) {
				flankTwo = findRight(subject.seqString().substring(flankOne.getSubjectEnd()), seqRemain);
			}
			else {
				//switched to minimumSizeWithLeftRight //15
				int replacementIndex = seqRemain.indexOf(replacementFlank);
				String seqRemainRightPart = seqRemain.substring(replacementIndex);
				LCS flankTwoLCS = kmerl.getMatchRight(seqRemainRightPart, flankOne.getSubjectEnd(), minimumSizeWithLeftRight, allowJump);
				if(flankTwoLCS!= null) {
					flankTwo = flankTwoLCS.getString();
					this.jumpedRight = flankTwoLCS.getJumped();
				}
				//System.out.println("two "+flankTwo);
				/*
				if(print) {
					System.out.println("PRINTYA right :"+flankTwo);
					System.out.println(seqRemainRightPart);
					System.out.println(flankOne.getSubjectEnd());
					System.out.println(subject.seqString());
					System.out.println(this.getRemarks());
					//System.exit(0);
				}
				*/
			}
				/*
				if(!flankTwo.equals(two)) {
					System.out.println(flankTwo);
					System.out.println(two);
				}
				*/
			//System.out.println("flankOne"+":"+flankOne+":"+flankOne.length());
			//System.out.println("flankTwo"+":"+flankTwo+":"+flankTwo.length());
			int posTest = flankOne.getSubjectEnd();
			//check if really unique
			if(subject.seqString().indexOf(flankOne.getString(), posTest+1)>0){
				this.setRemarks("leftFlank can be found at multiple places");
			}
			//check size
			if(flankTwo == null || flankOne.length()<minimumSizeWithLeftRight || flankTwo.length()<minimumSizeWithLeftRight ){
				//System.out.println(flankOne.length());
				//System.out.println(flankOne);
				//System.out.println(flankTwo.length());
				//System.out.println(flankTwo);
				if(flankOne.length()>=minimumSizeWithoutLeftRight && (flankTwo == null || flankTwo.length()<minimumSizeWithoutLeftRight )){
					this.setRemarks("Cannot find the Right flank of the event");
					//this.setRemarks("boo!");
					//System.out.println("Cannot find the second flank of the event, please do it manually");
					//System.err.println("Cannot find the second flank of the event, please do it manually");
				}
				else if( flankOne.length()<minimumSizeWithoutLeftRight && flankTwo != null && flankTwo.length()>=minimumSizeWithoutLeftRight) {
					//only allow this if query starts ok
					if(flankOne.getQueryStart()>0) {
						this.setRemarks("Second flank is ok, but left is not");
					}
				}
				else if(flankOne.length()<50 && flankTwo != null && flankTwo.length()<50 ){
					//unless the query start at the beginning
					//this.leftFlank = flankOne;
					//System.out.println(flankOne);
					//System.out.println(flankTwo);
					//System.out.println(this.query.seqString());
					setRemarks("Cannot find the flanks of the event, please do it manually");
					//System.err.println("Cannot find the flanks of the event, please do it manually");
				}
				else{
					this.setRemarks("Not exactly sure what is happening, but something is wrong");
					//System.err.println("Not exactly sure what is happening, but something is wrong");
				}
			}
			if(flankOne != null) {
				leftFlank = flankOne;
			}
			if(flankTwo != null) {
				rightFlank = flankTwo;
			}
		}
		else{
			if(kmerl == null) {
				flankOne = Left.getLeft(subject.seqString(), query, true, allowJump);
			}
			else {
				flankOne = kmerl.getMatchLongestLeft(query, allowJump);
			}
			//kmerl.getMatchLeft(subject.seqString(), -1);
			//System.out.println(flankOne); 
			//this assumes the leftFlank is always on the left side
			if(flankOne != null ) {
				this.leftFlank = flankOne;
				String querySub = query.substring(0, flankOne.getQueryEnd());
				//replace the entire query part, otherwise right can lie before left
				String seqRemain = query.replace(querySub, replacementFlank);
				String seqRemainSubject = subject.seqString().replace(flankOne.getString(), replacementFlank);
				int pos = seqRemainSubject.indexOf(replacementFlank);
				//System.out.println("remain:"+seqRemain);
				//System.out.println(seqRemainSubject);
				//System.out.println(seqRemainSubject.length());
				//System.out.println(seqRemain);
				//System.out.println(seqRemain.length());
				String seqRemainSubjectRest = seqRemainSubject.substring(pos);
				//start searching after the leftFlank
				if(kmerl == null) {
					flankTwo = longestCommonSubstring(seqRemainSubjectRest, seqRemain);
				}
				else {
					if(flankOne!=null) {
						LCS flankTwoLCS = kmerl.getMatchRight(seqRemain, flankOne.getSubjectEnd(), minimumSizeWithoutLeftRight, allowJump);
						if(flankTwoLCS != null) {
							flankTwo = flankTwoLCS.getString();
						}
						//flankTwo = kmerl.getMatchRight(seqRemain, flankOne.getSubjectEnd(), minimumSizeWithoutLeftRight);
					}
				}
				rightFlank = flankTwo;
				//System.out.println(flankOne);
				//System.out.println(flankTwo);
				if(flankOne == null || flankTwo == null || flankOne.length()<minimumSizeWithoutLeftRight || flankTwo.length()<minimumSizeWithoutLeftRight ){
					//System.out.println(flankOne.length());
					//System.out.println(flankTwo.length());
					
					if(flankTwo == null) {
						this.setRemarks("Cannot find the right flank");
					}
					else{
						this.rightFlank = flankTwo;
					}
					if(flankOne.length()>=minimumSizeWithoutLeftRight && (flankTwo == null || flankTwo.length()<minimumSizeWithoutLeftRight )){
						this.leftFlank = flankOne;
						this.setRemarks("2Cannot find the second flank of the event, please do it manually");
						//System.err.println("Cannot find the second flank of the event, please do it manually");
					}
					else if(flankOne.length()<50 && ( flankTwo == null || flankTwo.length()<50 )){
						this.setRemarks("Cannot find the flanks of the event, please do it manually");
						//System.err.println("Cannot find the flanks of the event, please do it manually");
					}
					else{
						this.setRemarks("Not exactly sure what is happening, but something is wrong");
						//System.err.println("Not exactly sure what is happening, but something is wrong");
					}
					//TODO: fix this case
					return;
				}
			}
			else {
				this.setRemarks("LeftFlank could not be found");
			}
		}
		
		//System.out.println("leftFlank:"+leftFlank);
		//System.out.println("rightFlank:"+rightFlank);
		if(leftFlank == null || rightFlank == null) {
			return;
		}
		int delPosStart = leftFlank.getSubjectEnd()+1;
		int delPosEnd = subject.seqString().indexOf(rightFlank);
		if(delPosEnd-delPosStart >=0){
			del = subject.seqString().substring(leftFlank.getSubjectEnd(), subject.seqString().indexOf(rightFlank));
		}
		else{
			del = "";
		}
			//System.out.println("del:"+del);
		//get the insertion
		//System.out.println(this.getName());
		//System.out.println("l:"+leftFlank);
		//System.out.println("r:"+rightFlank);
		int begin = leftFlank.getQueryStart();
		//System.out.println("start: "+(begin+leftFlank.length()));
		//System.out.println(query.seqString().indexOf(rightFlank,begin+leftFlank.length()));
		int end = query.indexOf(rightFlank,begin+leftFlank.length())+rightFlank.length();
		//System.out.println("begin:"+begin);
		//System.out.println("end:"+end);
		//System.out.println("q:"+query.seqString());
		//System.out.println("queryEnd "+leftFlank.getQueryEnd());
		//System.out.println(rightFlank.length());
		//System.out.println("reversed:"+this.reversed);
		//String insertContainingPart = query.seqString().substring(begin, end);
		//System.out.println("queryEnd"+leftFlank.getQueryEnd());
		//System.out.println(end);
		//System.out.println(rightFlank.length());
		int tempStart = leftFlank.getQueryEnd();
		int tempEnd = end-rightFlank.length();
		if(tempEnd>=tempStart){
			insert = query.substring(leftFlank.getQueryEnd(), end-rightFlank.length());
		}
		//qual stuff
		//insert = insertContainingPart.replace(leftFlank.getString(), "").replace(rightFlank, "");
		//insertContainingPart.substring(begin+leftFlank.length(), end-r)
		
		int size = 40;
		if(leftFlank.length()<size){
			size = leftFlank.length();
		}
		//System.out.println(query.getName());
		//System.out.println("leftFlank:"+leftFlank);
		//System.out.println("rightFlank:"+rightFlank);
		//System.out.println("leftFlank:"+leftFlank.substring(leftFlank.length()-size));
		size = 40;
		if(rightFlank.length()<size){
			size = rightFlank.length();
		}
		//System.out.println("rightFlank:"+rightFlank.substring(0,size));
		//System.out.println("del:"+del);
		//System.out.println("insert:"+insert);
		//Check if deletion and insertion are minimal
		//checkCall();
		while(del.length()>0 && insert.length()>0 && del.charAt(0) == insert.charAt(0)){
			//System.out.println("1!");
			//System.out.println(getName());
			leftFlank.addCharEnd(del.charAt(0));
			del = del.substring(1);
			insert = insert.substring(1);
		}
		while(del.length()>0 && insert.length()>0 && del.charAt(del.length()-1) == insert.charAt(insert.length()-1)){
			//System.out.println("2!");
			//System.out.println(getName());
			//System.out.println("r:"+rightFlank);
			//System.out.println("d:"+del);
			//System.out.println("i:"+insert);
			//System.out.println("l:"+leftFlank);
			rightFlank = del.charAt(del.length()-1)+rightFlank;
			del = del.substring(0,del.length()-1);
			insert = insert.substring(0,insert.length()-1);
		}
		//first check if the homology is at the right position, as it could be wrong
		//bug, this is only correct if there is no INSERT present anymore!
		while(del.length()>0 && del.charAt(0) == rightFlank.charAt(0) && insert.length()==0){
			//System.out.println("3!");
			//System.out.println(getName());
			leftFlank.addCharEnd(del.charAt(0));
			del = del.substring(1)+rightFlank.charAt(0);
			rightFlank = rightFlank.substring(1);
		}
		//bug, insert is not placed as far as possible to the left
		if(rightFlank != null) {
			while(del.length()==0 && insert.length()>0 && rightFlank.length()>0 && insert.charAt(0) == rightFlank.charAt(0)) {
				leftFlank.addCharEnd(insert.charAt(0));
				insert = insert.substring(1)+rightFlank.charAt(0);
				rightFlank = rightFlank.substring(1);
			}
		}
		//no longer report as people might see it as an error, while it is more of a warning
		//if(madeMinimal){
			//this.setRemarks("Made deletion and insertion minimal, probably that means the cut was at a different location, moved by "+moved+" positions");
		//}
		//System.out.println("left :"+this.leftFlank);
		checkCall();
		int maxLengthMatch = 10;
		//only check if long enough
		if(del!= null && insert != null && del.length()>maxLengthMatch && insert.length()>maxLengthMatch) {
			String insertDelCommon =  longestCommonSubstring(del, insert);
			//TODO: I became unsure if this is really a good idea. 
			if(insertDelCommon.length()>maxLengthMatch){
				//if we masked, then probably this check is not correct
				//after testing it turns out that most often this is correct
				this.setRemarks("Probably there is a mismatch/gap somewhere in the flank, which caused a DELINS which is probably incorrect.");
			}
		}
	}
	private String findRight(String substring, String query) {
		//System.out.println("queryInit:"+query);
		int index = query.indexOf(replacementFlank);
		if(index>0){
			query = query.substring(index);
		}
		String first = longestCommonSubstring(substring, query);
		String leftOver = substring.substring(0,substring.indexOf(first));
		String queryOver = query.substring(0,query.indexOf(first));
		String second = longestCommonSubstring(leftOver, queryOver);
		if(second.length()>MINIMUMSECONDSIZE){
			//check if we allow the jump, previously this led to deletions not being spotted
			int locFirstSub = substring.indexOf(first);
			int secSecondSubEnd = substring.indexOf(second)+second.length();
			int jumpDist = locFirstSub-secSecondSubEnd;
			//System.out.println("jumpDist right has size: "+jumpDist);
			//System.out.println(locFirstSub);
			//System.out.println(secSecondSubEnd);
			//System.out.println(first);
			//System.out.println(second);
			if(jumpDist<=ALLLOWEDJUMPDISTANCE){
				//System.out.println("jumping Right "+jumpDist);
				//System.out.println("jumping Right "+second);
				//System.out.println(first);
				jumpedRight  = true;
				return second;
			}
		}
		return first;
	}
	private Left findLeft(String substring, String query) {
		Left l =  Left.getLeft(substring, query, false, allowJump);
		this.jumpedLeft = l.getJumped();
		return l;
	}
	public String getLeftFlank(int size){
		if(leftFlank == null) {
			return "";
		}
		if(size == 0 || leftFlank.length()<size){
			return leftFlank.getString();
		}
		else{
			return leftFlank.getString().substring(leftFlank.length()-size);
		}
		
	}
	public String getRightFlank(int size){
		if(size == 0 || rightFlank.length()<size){
			return rightFlank;
		}
		else{
			return rightFlank.substring(0, size);
		}
	}
	public String getDel(){
		return del;
	}
	public String getInsertion(){
		return insert;
	}
	public String getName(){
		return queryName;
	}
	public boolean inZone(){
		//Pattern zone = Pattern.compile("[ag]{10,}");
		//quite complex already
		Pattern zone = Pattern.compile("[ct]{10,}[ag]{0,10}[ct]{10,}");
		Matcher m = zone.matcher(subject.seqString().toString());
		boolean found = false;
		while(m.find()){
			if(m.group().length()>30){
				found = true;
				if(this.getDelEnd()>m.start() && this.getDelEnd()<m.end()){
					return true;
				}
			}
		}
		if(!found){
			//check the high zones
			zone = Pattern.compile("[ga]{10,}[ct]{0,10}[ga]{10,}");
			m = zone.matcher(subject.seqString().toString());
			while(m.find()){
				if(m.group().length()>30){
					if(this.getDelEnd()>m.start() && this.getDelEnd()<m.end()){
						return true;
					}
				}
			}
		}
		return false;
	}
	public String toStringOneLine(){
		//switched to StringBuffer
		String s = "\t";
		int size = 20;
		String homology = "";
		int homologyLength = -1;
		String homologyM = "";
		int homologyLengthM = -1;
		checkCall();
		//only do it when no weird things found
		if(this.remarks.length() == 0 && insert.length()>0){
			//currently fixed value
			solveInsertion(solveInsertStart,solveInsertEnd, MAXIMUMTRIESSOLVING);
		}
		if(del.length()>0 && insert.length() == 0){
			homology = Utils.getHomologyAtBreak(leftFlank.getString(), del, rightFlank);
			homologyLength = homology.length();
			homologyM = Utils.getHomologyAtBreakWithMismatch(leftFlank.getString(), del, rightFlank, maxMismatchRate);
			homologyLengthM = homologyM.length();
		}
		if(this.getType()== Type.TANDEMDUPLICATION){
			homology = getHomologyTandemDuplication();
			homologyLength = homology.length();
		}
		int delLength = this.getDel().length();
		if(this.getDel().contains(" - ")){
			delLength -= 3;
		}
		int mod = (this.getInsertion().length()-this.getDel().length())%3;
		if(mod<0){
			mod+=3;
		}
		int leftFlankLength = -1;
		if(leftFlank != null) {
			leftFlankLength = leftFlank.length();
		}
		StringBuffer ret = new StringBuffer(5000);
		//if(cutType == null && kmerl != null) {
		//	ret.append("KMERL!").append(s);
		//}
		ret.append(cutType).append(s);
		ret.append(getName()).append(s);
		ret.append(dir).append(s);
		if(fileName != null) {
			ret.append(fileName).append(s);
		}
		else {
			ret.append(getName()).append(s);
		}
		if(alias != null) {
			ret.append(alias).append(s);
		}
		else {
			ret.append(getName()).append(s);
		}
		ret.append(getIDPart()).append(s);
		ret.append(possibleDouble).append(s);
		ret.append(getSubject()).append(s);
		ret.append(getSubjectComments()).append(s);
		ret.append(query).append(s);
		ret.append(getLeftFlank(size)).append(s);
		ret.append(getDel()).append(s);
		ret.append(getRightFlank(size)).append(s);
		ret.append(getInsertion()).append(s);
		ret.append(getDelStart()).append(s);
		ret.append(getDelEnd()).append(s);
		ret.append((this.getDelStart()-this.pamSiteLocation)).append(s);
		ret.append((this.getDelEnd()-this.pamSiteLocation)).append(s);
		ret.append((this.getDelStart()-this.pamSiteLocation)).append(s);
		ret.append(getRightFlankRelativePos()).append(s);
		ret.append(getColorHomology()).append(s);
		ret.append(homology).append(s);
		ret.append(homologyLength).append(s);
		ret.append(homologyM).append(s);
		ret.append(homologyLengthM).append(s);
		ret.append(delLength).append(s);
		ret.append(getInsertion().length()).append(s);
		ret.append(mod).append(s);
		ret.append(getSNVMutation()).append(s);
		ret.append(getType()).append(s);
		ret.append(getSecondaryType()).append(s);
		ret.append(getRangesString()).append(s);
		ret.append(masked).append(s);
		ret.append(getLeftSideRemoved()).append(s);
		ret.append(getRightSideRemoved()).append(s);
		ret.append(remarks).append(s);
		ret.append(reversed).append(s);
		ret.append(getSchematic()).append(s);
		ret.append(getUniqueClass()).append(s);
		ret.append(inZone()).append(s);
		ret.append(leftFlankLength).append(s);
		ret.append(rightFlank.length()).append(s);
		ret.append(this.getMatchStart()).append(s);
		ret.append(getMatchEnd()).append(s);
		ret.append(this.jumpedLeft).append(s);
		ret.append(this.jumpedRight).append(s);
		ret.append(this.entireQueryUsed).append(s);
		if(is != null){
			int isStartPos = getIsStartPos();
			int isEndPos = getIsEndPos();
			int isStartPosRel = isStartPos-this.pamSiteLocation;
			int isEndPosRel = isEndPos-this.pamSiteLocation;
			ret.append(is.getLargestMatch()).append(s);
			ret.append(is.getLargestMatchString()).append(s);
			ret.append(is.getSubS()).append(s);
			ret.append(is.getSubS2()).append(s);
			ret.append(is.getType()).append(s);
			ret.append(is.getLengthS()).append(s);
			ret.append(is.getPosS()).append(s);
			ret.append(is.getFirstHit()).append(s);
			ret.append(is.getFirstPos()).append(s);
			ret.append(isStartPos).append(s);
			ret.append(isEndPos).append(s);
			ret.append(isStartPosRel).append(s);
			ret.append(isEndPosRel).append(s);
		}
		return ret.toString();
	}
	private String getSNVMutation() {
		if(this.getType()==Type.SNV) {
			return Utils.getMutation(del, insert);
		}
		return "";
	}
	private int getIsEndPos() {
		if(is != null) {
			int pos = is.getFirstPos();
			String side = is.getFirstHit();
			int length = is.getFirstLength();
			//Right
			if(side.contains("R")) {
				return this.getDelEnd()+pos+length;
			}
			//Left
			else {
				return this.getDelStart()-pos;
			}
		}
		return -1;
	}
	private int getIsStartPos() {
		if(is != null) {
			int pos = is.getFirstPos();
			String side = is.getFirstHit();
			int length = is.getFirstLength();
			//Right
			if(side.contains("R")) {
				return this.getDelEnd()+pos;
			}
			//Left
			else {
				return this.getDelStart()-pos-length;
			}
		}
		return -1;
	}
	private void checkCall() {
		//only if remarks are not set this call is valid
		if(this.remarks.length()==0 && query!=null && subject != null){
			String totalquery = this.getLeftFlank(0)+this.getInsertion()+this.getRightFlank(0);
			if(!query.contains(totalquery)){
				System.out.println("QUERY IS COMPLETELY BROKEN!!");
				System.out.println("l:"+getLeftFlank(0));
				System.out.println("i:"+getInsertion());
				System.out.println("r:"+getRightFlank(0));
				System.out.println("del:"+this.getDel());
				System.out.println("raw:"+query);
				System.out.println(getName());
				System.out.println(this.getRemarks());
				System.exit(0);
			}
			//set a variable here
			if(query.replace("X", "").equals(totalquery)) {
				entireQueryUsed = true;
			}
			String totalSubject = this.getLeftFlank(0)+this.getDel()+this.getRightFlank(0);
			if(!subject.seqString().contains(totalSubject)){
				System.out.println("SUBJECT IS COMPLETELY BROKEN!!");
				System.out.println("l:"+getLeftFlank(0));
				System.out.println("i:"+getInsertion());
				System.out.println("r:"+getRightFlank(0));
				System.out.println("del:"+this.getDel());
				System.out.println("raw:"+query);
				System.out.println("subject:"+subject.seqString());
				System.out.println(getName());
				System.out.println(this.getRemarks());
				this.setRemarks("SUBJECT BROKEN");
				//System.exit(0);
			}
			else{
				//System.out.println("SOMETHING IS COMPLETELY OK!!");
			}
		}
	}
	private String getHomologyTandemDuplication() {
		//bug, the tandem duplication is not always left
		//added check
		if(!leftFlank.getString().endsWith(insert)) {
			System.out.println(this.getName());
			System.err.println("The TD is not placed on the left side");
			System.out.println(leftFlank.getString());
			System.out.println(rightFlank);
			System.out.println(insert);
			System.exit(0);
		}
		int pos = leftFlank.getSubjectEnd();
		int newPos = pos - insert.length();
		String left = subject.seqString().substring(0, pos);
		String right = subject.seqString().substring(0, newPos);
		String hom = "";
		while(left.charAt(left.length()-1)==right.charAt(right.length()-1)){
			//need the reverse
			hom = left.charAt(left.length()-1)+hom;
			left = left.substring(0, left.length()-1);
			right = right.substring(0, right.length()-1);
		}
		return hom;
	}
	private int getRightFlankRelativePos(){
		int right = this.getDelEnd()-this.pamSiteLocation;
		//check if we have overlap
		if(this.getInsertion() != null){
			String insertTemp = this.getInsertion();
			String leftTemp = this.getLeftFlank(0);
			while(insertTemp.length()>0 && leftTemp.length()>0 
					&& insertTemp.charAt(insertTemp.length()-1) == leftTemp.charAt(leftTemp.length()-1)){
				//move right one bp, is that always correct
				insertTemp = insertTemp.substring(0, insertTemp.length()-1);
				leftTemp = leftTemp.substring(0, leftTemp.length()-1);
				right--;
			}
		}
		return right;
	}
	public String getIDPart() {
		if(this.getName().contains("_")){
			String[] parts = this.getName().split("_");
			String part = "";
			for(int i = 0;i<parts.length-1;i++){
				if(part.length()>0){
					part += "_";
				}
				part += parts[i];
			}
			return part;
		}
		return "";
	}
	private void solveInsertion(int start, int end, int maxTries) {
		//disabled for translocation
		if(this.insert.length()>=minSizeInsertionSolver){
			String left = this.getCorrectedLeftFlankRelative(start, end);
			String right = this.getCorrectedRightFlankRelative(start, end);
			InsertionSolverTwoSides is = new InsertionSolverTwoSides(left, right,this.insert,getName());
			is.setAdjustedPositionLeft(start);		
			is.setAdjustedPositionRight(start);
			is.search(true, true);
			is.setMaxTriesSolved(maxTries);
			if(this.additionalSearchSequence != null){
				is.setTDNA(additionalSearchSequence);
			}
			is.setMinimumMatch(minSizeInsertionSolver, false);
			is.solveInsertion();
			this.is = is;
		}
	}
	private String getCorrectedLeftFlankRelative(int start, int end) {
		//adjust position
		int startTemp = -1*end;
		end = -1*start;
		start = startTemp;
		int leftEnd = leftFlank.getSubjectEnd();
		start += leftEnd;
		end += leftEnd;
		if(start<0){
			start = 0;
		}
		if(end>subject.seqString().length()){
			end = subject.seqString().length();
		}
		String ret = subject.seqString().substring(start, end);
		return ret;
	}
	private String getCorrectedRightFlankRelative(int start, int end) {
		//adjust position
		//System.out.println(start);
		//System.out.println(end);
		int rightStart = subject.seqString().indexOf(rightFlank);
		start += rightStart;
		end += rightStart;
		//System.out.println(rightStart);
		//safety
		if(start<0){
			start = 0;
		}
		if(end>subject.seqString().length()){
			end = subject.seqString().length();
		}
		String ret = subject.seqString().substring(start, end);
		return ret;
	}
	public Type getType() {
		boolean containsTD = false;
		if(is!= null){
			for(String s :is.getPosS().split(";")){
				if(s.length()>0 && Integer.parseInt(s) == 0){
					containsTD = true;
				}
			}
		}
		if(this.getDel().length()== 0 && this.getInsertion().length() == 0){
			return Type.WT;
		}
		else if(this.getDel().length()== 1 && this.getInsertion().length() == 1){
			return Type.SNV;
		}
		else if(this.getDel().length()== 0 && this.getInsertion().length() > 0 &&
			is != null && is.getType().equals("SOLVED") && is.getPosS().equals("0") 
					&& !is.getMatchS().contains(";") && is.getLargestMatch()>=minSizeInsertionSolver && !is.getSubS2().contains("rc")){
				return Type.TANDEMDUPLICATION;
		}
		else if(this.getDel().length()== 0 && this.getInsertion().length()>0 && containsTD){
			return Type.TANDEMDUPLICATION_COMPOUND;
		}
		else if(this.getDel().length()==0 && this.getInsertion().length()>0){
			return Type.INSERTION;
		}
		else if(this.getDel().length()> 0 && this.getInsertion().length() == 0){
			return Type.DELETION;
		}
		else if(this.getDel().length()> 0 && this.getInsertion().length() > 0){
			return Type.DELINS;
		}
		return Type.UNKNOWN;
	}
	public String getSecondaryType() {
		if(this.remarks.length() != 0) {
			return "";
		}
		if(this.getType() == Type.TANDEMDUPLICATION_COMPOUND){
			//how long is the remaining part
			String[] poss = is.getPosS().split(";");
			String[] lengths = is.getLengthS().split(";");
			boolean multi = true;
			//mulitple tandemduplication?
			//this can only be true if it is completely solved
			if(is.getType().equals("SOLVED")){
				for(int i = 0;i<poss.length;i++){
					String s = poss[i];
					if(s.length()>0 && Integer.parseInt(s) != 0){
						multi = false;
					}
				}
				if(multi){
					return ""+Type.TANDEMDUPLICATION_MULTI;
				}
			}
			//length left
			int lengthTD = 0;
			int lenghtOther = 0;
			for(int i = 0;i<poss.length;i++){
				String s = poss[i];
				if(s.length()>0 && Integer.parseInt(s) == 0){
					lengthTD+=Integer.parseInt(lengths[i]);
				}
				else{
					lenghtOther+=Integer.parseInt(lengths[i]);
				}
			}
			int insLengthRem = this.getInsertion().length()-lengthTD-lenghtOther;
			if(lengthTD>0 && lenghtOther == 0 && insLengthRem < minSizeInsertionSolver){
				return "TANDEMDUPLICATION_AND_REMAINING_TOO_SMALL";
			}
			else if(lenghtOther>0 && insLengthRem==0){
				return "TANDEMDUPLICATION_AND_OTHER_EVENT_FOUND";
			}
			else{
				return "TANDEMDUPLICATION_AND_OTHER_EVENT_FOUND_AND_REMAINING";
			}
			//add other if you can find them
		}
		//experimental code for DELINS
		if(getType() == Type.DELINS) {
			if(this.getInsertion().length()<minSizeInsertionSolver) {
				return "DELINS_<"+minSizeInsertionSolver;
			}
			//solving has been tried
			else {
				//SOLVED
				String ret = "FLANKINSERT";
				String tDNA = "";
				String orientation = "";
				if(is.getType().equals("NOT SOLVED")) {
					ret="DELINS_UNKNOWN";
				}
				else if(is.getType().equals("PARTIALLY SOLVED") && is.getLengthS().split(";").length>3) {
					if(is.getFirstLength()<10 && getInsertion().length()>20) {
						ret="DELINS_LOCATION_UNKNOWN";
					}
				}
				if(is.getFirstHitInTDNA()) {
					tDNA="_OTHERSEQ";
					//tDNA+=is.getFirstHit();
					//System.out.println(tDNA);
				}
				if(is.getFirstHit().contains("rc")) {
					orientation="_RC";
				}
				
				return ret+tDNA+orientation;
			}
		}
		return ""+getType();
	}
	public String getSubject() {
		if(subject == null) {
			return "";
		}
		String ret = subject.getName();
		return ret;
	}
	public String getSubjectComments() {
		if(subject == null) {
			return "";
		}
		if(subject.getDescription() == null){
			return getSubject();
		}
		return subject.getDescription();
	}
	private String getRangesString() {
		String ret = "";
		for(Range r: ranges){
			if(ret.length()>0){
				ret+=" | ";
			}
			ret+=r.getBegin()+"-"+r.getEnd();
		}
		return ret;
	}
	public String getRemarks() {
		return remarks;
	}
	private void setRemarks(String remark) {
		if(this.remarks.length()>0){
			remarks += ":";
		}
		this.remarks += remark;
	}
	public static String getOneLineHeader() {
		//return "Name\tSubject\tRaw\tleftFlank\tdel\trightFlank\tinsertion\tdelStart\tdelEnd\tdelRelativeStart\tdelRelativeEnd\thomology\thomologyLength\tdelSize\tinsSize\tLongestRevCompInsert\tRanges\tMasked\tRemarks";
		String s = "\t";
		String ret = "CutType\tName\tDir\tFile\tAlias\tgetIDPart\tpossibleDouble\tSubject\tgetSubjectComments\tRaw\tleftFlank\tdel\trightFlank\tinsertion\tdelStart\tdelEnd\tdelRelativeStart\tdelRelativeEnd\tdelRelativeStartTD\tdelRelativeEndTD\tgetHomologyColor\thomology\thomologyLength\thomologyMismatch10%\thomologyLengthMismatch10%\tdelSize\tinsSize\tMod3\tSNVMutation\tType\tSecondaryType\tRanges\tMasked\t"
				+ "getLeftSideRemoved\tgetRightSideRemoved\tRemarks\tReversed\tSchematic\tClassName"+s+"InZone"+s+"leftFlankLength"+s+"rightFlankLength"+s+"matchStart"+s+"matchEnd"+s+"jumpedLeft"+s+"jumpedRight"+s+"entireQueryUsed";
		ret+= s+"isGetLargestMatch"+s+"isGetLargestMatchString"+s
					+"isGetSubS"+s+"isGetSubS2"+s+"isGetType"+s+"isGetLengthS"+s+"isPosS"+s+"isFirstHit"+s+"getFirstPos"+s+"isStartPos"+s+"isEndPos"+s+"isStartPosRel"+s+"isEndPosRel";
		return ret;
	}
	public int getMatchStart() {
		if(leftFlank != null) {
			return leftFlank.getSubjectStart();
		}
		return -1;
	}
	public int getMatchEnd() {
		return subject.seqString().indexOf(rightFlank)+rightFlank.length();
	}
	public int getDelStart(){
		//System.out.println(this.getName());
		//System.out.println(leftFlank);
		if(leftFlank == null) {
			return -10000;
		}
		return leftFlank.getSubjectEnd();
	}
	public int getDelEnd(){
		return subject.seqString().indexOf(rightFlank);
	}
	public void setMinimumSizeWithoutLeftRight(int min){
		this.minimumSizeWithoutLeftRight = min;
	}
	public void setAndDetermineCorrectRange(double relaxedMaxError) {
		double maxError = relaxedMaxError;
		long first = -1;
		long last = quals.getLength()-1;
		for(long j = 0;j<quals.getLength();j++){
			double errorP = quals.get(j).getErrorProbability();
			if(first == -1 && errorP <= maxError){
				first = j;
			}
			//break range on the first bad base
			if(first >= 0 && errorP > maxError){
				//System.out.println("range is "+first+"-"+last +"("+(last-first+1)+")");
				if(j-first >= minimalRangeSize){
					//System.out.println("range is "+first+"-"+last +"("+(last-first+1)+")");
					ranges.add(Range.of(first, last));
				}
				first = -1;
				last = quals.getLength()-1;
			}
			last = j;
			//System.out.println((j+1)+" "+this.query.seqString().charAt((int)j)+" "+quals.get(j).getErrorProbability());
		}
		if(first >= 0 && (last - first) >= minimalRangeSize){
			//System.out.println("range is "+first+"-"+last +"("+(last-first+1)+")");
			ranges.add(Range.of(first, last));
		}
	}
	public void maskSequenceToHighQuality(String left, String right){
		if(ranges.size()==0){
			this.setRemarks("No high quality range found, unable to mask");
		}
		if(ranges.size()>=1){
			String dna = this.query;
			StringBuffer tempDNA = new StringBuffer();
			long j=0;
			for(Range r: ranges){
				long begin = r.getBegin();
				for(;j<begin;j++){
					tempDNA.append("X");
				}
				long end = r.getEnd();
				for(;j<=end;j++){
					tempDNA.append(dna.charAt((int) j));
				}
				//System.out.println(this.getName());
				//System.out.println(r);
				//System.out.println(tempDNA);
			}
			while(tempDNA.length()<dna.length()){
				tempDNA.append("X");
			}
			//System.out.println("mod:"+tempDNA);
			//no assignment
			this.query = tempDNA.toString();
			masked = true;
		}
	}
	public void maskSequenceToHighQualityRemove(){
		String left = leftSite;
		String right = rightSite;
		if(ranges.size()==0){
			this.setRemarks("No high quality range found, unable to mask");
		}
		if(ranges.size()>=1){
			String dna = this.query;
			StringBuffer tempDNA = new StringBuffer();
			long j=0;
			Range correct = null;
			String largestCommon = null;
			Range rangeContainingLargest = null;			
			Range largestRange = null;
			long largestRangeLength = -1;
			for(Range r: ranges){
				String sub = dna.substring((int)r.getBegin(), (int)r.getEnd());
				String lcsL = longestCommonSubstring(sub, left);
				String lcsR = longestCommonSubstring(sub, right);
				if(largestCommon == null || (lcsL.length()+lcsR.length()) >largestCommon.length()){
					largestCommon = lcsL+"_"+lcsR;
					rangeContainingLargest = r;
				}
				if(r.getLength()>largestRangeLength){
					largestRangeLength = r.getLength();
					largestRange = r;
				}
				//take the first
				if(largestCommon.length()>=10){
					correct = rangeContainingLargest;
				}
				else{
					correct = largestRange;
				}
				//removed break
				//break;
			}
			if(correct != null){
				long begin = correct.getBegin();
				for(;j<begin;j++){
					tempDNA.append("X");
				}
				long end = correct.getEnd();
				for(;j<=end;j++){
					tempDNA.append(dna.charAt((int) j));
				}
				//System.out.println(this.getName());
				//System.out.println(correct);
				//System.out.println(tempDNA);
				while(tempDNA.length()<dna.length()){
					tempDNA.append("X");
				}
				//System.out.println("mod:"+tempDNA);
				this.query = tempDNA.toString();
				masked = true;
			}
			else{
				this.setRemarks("No high quality range found with left and right part, unable to mask");
			}
		}
	}
	public void setAdditionalSearchString(HashMap<String, String> additional){
		//safety
		if(additional== null) {
			return;
		}
		Vector<Sequence> temp = new Vector<Sequence>();
		for(String name: additional.keySet()) {
			String str = additional.get(name);
			Sequence seq;
			try {
				seq = DNATools.createDNASequence(str, name);
				temp.add(seq);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.additionalSearchSequence = temp;
	}
	public void setCutType(String type) {
		this.cutType = type;
	}
	public int getLeftSideRemoved(){
		if(cutType != null){
			int posRight = subject.seqString().indexOf(rightSite);
			int posLeft = leftFlank.getSubjectEnd();
			return posRight-posLeft;
		}
		return -1;
	}
	public int getRightSideRemoved(){
		if(cutType != null){
			int posRight = subject.seqString().indexOf(leftSite)+leftSite.length();
			int posLeft = subject.seqString().indexOf(rightFlank);
			return posLeft-posRight;
		}
		return -1;
	}
	public String getSchematic(){
		int start = 250;
		int end = 350;
		if(leftFlank == null) {
			return null;
		}
		int pos = leftFlank.getSubjectEnd();
		int delLength = del.length();
		StringBuffer sb = new StringBuffer(1000);
		if(pos>=start){
			String s = subject.seqString().substring(start, leftFlank.getSubjectEnd());
			//safety
			if(s.length()>1000) {
				s = s.substring(s.length()-1000);
			}
			sb.append(s);
		}
		else{
			delLength = pos+delLength-start;
		}
		sb.append("|");
		for(int i =0;i<delLength;i++){
			sb.append("-");
		}
		if(insert.length()>0){
			sb.append(this.insert.toUpperCase());
		}
		sb.append("|");
		int posRight = subject.seqString().indexOf(rightFlank);
		
		if(end-posRight>=0){
			String right = subject.seqString().substring(subject.seqString().indexOf(rightFlank),end);
			//safety
			if(right.length()>1000) {
				right = right.substring(right.length()-1000);
			}
			sb.append(right);
		}
		return sb.toString();
	}
	public String getColorHomology(){
		if(this.getType() == Type.TANDEMDUPLICATION){
			String homology = this.getHomologyTandemDuplication();
			return CompareSequence.acquireColor(homology.length()); 
		}
		if(del.length()>0 && insert.length() == 0){
			String homology = Utils.getHomologyAtBreak(leftFlank.getString(), del, rightFlank);
			return CompareSequence.acquireColor(homology.length());
		}
		else if(insert.length()>0){
			return "grey";
		}
		return "";
	}
	private static String acquireColor(int length) {
		//String[] colors = {"#DAE8F5","#B9D5E9","#88BDDC","#539CCB","#2A7ABA","#0E559F"};
		String[] colors = {"#C9DDF2","#6899D0","#0D99B2","#054E61"};
		if(length >= colors.length){
			return colors[colors.length-1];
		}
		return colors[length];
	}
	public String getUniqueClass(){
		String s = "|";
		String insert = ""+this.getInsertion().length();
		//insertions of one nucleotide can be different, so add the actual insert
		if(this.getInsertion().length()==1){
			insert = this.getInsertion();
		}
		String ret = this.getType()+s+this.getDelStart()+s+this.getDelEnd()+s+insert;
		return ret;
	}
	public void flagPossibleDouble(boolean b) {
		this.possibleDouble = b;
	}
	public String[] printISParts(HashMap<String, String> colorMap) {
		if(this.insert.length()>0){
			solveInsertion(solveInsertStart,solveInsertEnd, -1);
			if(this.is != null){
				return this.is.printISParts(colorMap);
			}
			//to small probably
			else{
				String[] ret = {0+"\t"+getInsertion().length()+"\tblack"};
				return ret;
			}
		}
		return null;
	}
	public String toStringCompare(int size) {
		String header = "";
		String seq = "";
		String comp = "";
		int nrNucleotides = 0;
		if(this.leftFlank.length()>0 ){
			int indexSubject = leftFlank.getSubjectStart();
			int indexQuery = leftFlank.getQueryStart();
			String tempSubject = subject.seqString().substring(indexSubject);
			String tempQuery = query.substring(indexQuery);
			while(tempSubject.length()>0 && tempQuery.length()>0){
				char c = '*';
				if(tempSubject.charAt(0) == tempQuery.charAt(0) || tempQuery.charAt(0) == 'n' ){
					c = ' ';
				}
				header += tempSubject.charAt(0);
				seq += tempQuery.charAt(0);
				comp += c;
				tempSubject = tempSubject.substring(1);
				tempQuery = tempQuery.substring(1);
				nrNucleotides++;
			}
			
			String headerH = this.subject.getName();
			String seqH = this.getName();
			String compH = "compare";
			if(nrNucleotides>0){
				StringBuffer ret = new StringBuffer();
				while(comp.length()>0){
					if(comp.length()<size){
						ret.append(headerH+"\t"+indexSubject+"\t"+header+"\n");
						ret.append(seqH+"\t"+indexQuery+"\t"+seq+"\n");
						ret.append(compH+"\t"+indexSubject+"\t"+comp+"\n");
						break;
					}
					else{
						ret.append(headerH+"\t"+indexSubject+"\t"+header.substring(0,size)+"\n");
						ret.append(seqH+"\t"+indexQuery+"\t"+seq.substring(0,size)+"\n");
						ret.append(compH+"\t"+indexSubject+"\t"+comp.substring(0, size)+"\n");
					}
					header = header.substring(size);
					seq = seq.substring(size);
					comp = comp.substring(size);
					indexSubject+=size;
					indexQuery+=size;
				}
				return ret.toString()+"\n";
			}
		}
		return null;
	}
	public void setCurrentFile(String name) {
		this.fileName = name;
	}
	public String getKey(boolean appendStartEnd) {
		//left, right, del, insert
		//added match Left and Right
		String s = "_";
		StringBuffer ret = new StringBuffer(fileName).append(s);
		ret.append(getDelStart()).append(s);
		ret.append(getDelEnd()).append(s);
		ret.append(del).append(s);
		ret.append(insert).append(s);
		if(appendStartEnd) {
			ret.append(getMatchStart()).append(s);
			ret.append(getMatchEnd()).append(s);
		}
		//String key = this.fileName+"_"+this.getDelStart()+"_"+this.getDelEnd()+"_"+this.del+"_"+this.insert+"_"+this.getMatchStart()+"_"+this.getMatchEnd();
		return ret.toString();
	}
	public int getRelativeDelEnd(){
		return this.getDelEnd()-this.pamSiteLocation;
	}
	public long getNrNs() {
		//changed to X
		long count = query.chars().filter(ch -> ch == 'X').count();
		return count;
	}
	public boolean isCorrectPositionLeft(ArrayList<SetAndPosition> poss) {
		//System.out.println("Compare: "+getMatchStart()+" >= "+poss.get(0).getMin()+" : "+(getMatchStart()>=poss.get(0).getMin()));
		//System.out.println("Compare: "+getMatchStart()+" <= "+poss.get(1).getMin()+" : " +(getMatchStart()<= poss.get(1).getMin()));
		boolean firstFilterOKa = false;
		boolean secondFilterOK = false;
		if(getMatchStart()>=poss.get(0).getMin() && getMatchStart()<= poss.get(1).getMin()) {
			firstFilterOKa = true;
		}
		//System.out.println("Result firstFilterOKa "+firstFilterOKa);
		//if(!firstFilterOKa) {
			//System.out.println(query);
			//System.out.println(getLeftFlank(0));
		//}
		if(poss.get(2) != null && getDelStart() >= poss.get(2).getMin()) {
			secondFilterOK = true;
		}
		//if filter is disabled.
		else if(poss.get(2) == null) {
			secondFilterOK = true;
		}
		return firstFilterOKa && secondFilterOK;
	}
	public boolean isCorrectPositionRight(ArrayList<SetAndPosition> poss) {
		boolean firstFilterOKb = false;
		boolean secondFilterOK = false;
		if(getMatchEnd()>=poss.get(1).getMax() && getMatchEnd()<= poss.get(0).getMax()) {
			firstFilterOKb = true;
		}
		if(poss.get(2) != null && getDelEnd() <= poss.get(2).getMax()) {
			secondFilterOK = true;
		}
		//if filter is disabled
		else if(poss.get(2) == null) {
			secondFilterOK = true;
		}
		return firstFilterOKb && secondFilterOK;
	}
	public boolean isCorrectPosition(ArrayList<SetAndPosition> poss) {
		//does matchStart begin within the primer?
		boolean firstFilterOKa = false;
		boolean firstFilterOKb = false;
		boolean secondFilterOK = false;
		//System.out.println("match: "+getMatchStart()+" "+this.getMatchEnd());
		//System.out.println(getDelStart()+" "+this.getDelEnd());
		 
		if(getMatchStart()>=poss.get(0).getMin() && getMatchStart()<= poss.get(1).getMin()) {
			firstFilterOKa = true;
		}
		if(getMatchEnd()>=poss.get(1).getMax() && getMatchEnd()<= poss.get(0).getMax()) {
			firstFilterOKb = true;
		}
		if(poss.get(2).positionsBounded(getDelStart(), getDelEnd())) {
			secondFilterOK = true;
		}
		//System.out.println(firstFilterOKa);
		//System.out.println(firstFilterOKb);
		//System.out.println(secondFilterOK);
		boolean ret = firstFilterOKa && firstFilterOKb && secondFilterOK;
		if(ret == false) {
			this.setRemarks("Position problem");
		}
		return ret;
		
	}
	public String getRaw() {
		return this.query;
	}
	public void setCurrentAlias(String alias, String fallback) {
		if(alias != null) {
			this.alias  = alias;
		}
		else {
			this.alias = fallback;
		}
	}
	public static String[] getOneLineHeaderArray() {
		String header = getOneLineHeader();
		String[] strings =  header.split("\t");
		//for(String s: strings) {
			//System.out.println(s);
		//}
		return strings;
	}
	public static String[] mandatoryColumns() {
		String[] mandatory = {"Name","Dir","File","Subject","Raw","leftFlank","del","rightFlank","insertion","delStart","delEnd","homology","homologyLength","delSize", "insSize", "Type","Remarks"};
		return mandatory;
	}
	public void setAllowJump(boolean allowJump) {
		this.allowJump = allowJump;
	}
}

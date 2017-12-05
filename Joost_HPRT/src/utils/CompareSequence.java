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

import utils.InsertionSolverTwoSides;


public class CompareSequence {

	private RichSequence subject, subject2, query;
	private String rightFlank = "", del = "", insert = "", remarks = "";
	private Left leftFlank;
	private int minimumSizeWithoutLeftRight = 30;
	private int minimumSizeWithLeftRight = 15;
	private int pamSiteLocation;
	private final static String replacementFlank = "FLK1";
	private String leftSite, rightSite;
	public final static int minimalRangeSize = 40;
	private static final int ALLLOWEDJUMPDISTANCE = 1;
	//this introduces possible problems... I am aware of this 'feature' missing SNVs 30bp away from flanks
	private static final int MINIMUMSECONDSIZE = 30;
	private ArrayList<Range> ranges = new ArrayList<Range>();
	private boolean masked = false;
	private QualitySequence quals;
	private InsertionSolverTwoSides is;
	private int minSizeInsertionSolver = 5;
	public boolean searchTranslocation = false;
	public enum Type {WT, SNV, DELETION, DELINS, INSERTION, UNKNOWN, TANDEMDUPLICATION, TANDEMDUPLICATION_COMPOUND, TANDEMDUPLICATION_MULTI};
	public String dir;
	private String cutType;
	private Vector<Sequence> additionalSearchSequence;
	private boolean possibleDouble = false;
	private int solveInsertStart = -100;
	private int solveInsertEnd = 100;
	private String fileName;
	private boolean reversed = false;
	
	public CompareSequence(RichSequence subject, RichSequence subject2, RichSequence query, QualitySequence quals, String left, String right, String pamSite, String dir) {
		this.subject = subject;
		this.subject2 = subject2;
		this.query = query;
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
		if(pamSite != null){
			this.pamSiteLocation = Integer.parseInt(pamSite.substring(pamSite.indexOf(":")+1));
		}
		else{
			this.pamSiteLocation = 0;
		}
		checkAndPossibleReverse();
	}
	private void checkAndPossibleReverse() {
		int size = Utils.longestCommonSubstring(query.seqString().toString(), subject.seqString().toString()).length();
		String rc = Utils.longestCommonSubstring(Utils.reverseComplement(query.seqString().toString()), subject.seqString().toString());
		int altSize = rc.length();
		//System.out.println(this.getName());
		//System.out.println("size: "+size+" rcSize: "+altSize);
		//take the reverse complement of the query.
		//sometimes that is not correct, although we don't really know if that is true
		if(size <40 && altSize>size){
			try {
				query = RichSequence.Tools.createRichSequence(this.query.getName(), DNATools.createDNA(Utils.reverseComplement(query.seqString().toString())));
				//also turn around the quality
				if(quals!= null){
					//System.out.println("Reversing the qual!");
					QualitySequenceBuilder qsb = new QualitySequenceBuilder(quals);
					quals = qsb.reverse().build();
				}
				this.reversed  = true;
			} catch (IllegalSymbolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("Took RC");		
		}
	}
	public void determineFlankPositions(){
		determineFlanksPositions(leftSite.toLowerCase(), rightSite.toLowerCase());
	}
	private void determineFlanksPositions(String left, String right) {
		int leftPos = -2;
		int rightPos = -2;
		Left flankOne = null;
		String flankTwo = "";
		//System.out.println(left+":"+left.length());
		//System.out.println(right+":"+right.length());
		//System.out.println(subject.seqString().toString());
		//System.out.println(subject2.seqString().toString());
		if(left != null && left.length()>=15 && right != null && right.length()>=15){
			if(subject.seqString().indexOf(left) <0){
				System.err.println("Cannot find left, is it the correct sequence?");
				//System.err.println(left);
				//System.err.println(subject.seqString());
				return;
			}
			leftPos = subject.seqString().indexOf(left)+left.length();
			//misuse the pamSiteLocation to make it relative to the left position
			if(this.pamSiteLocation == 0){
				this.pamSiteLocation = leftPos;
			}
			//System.out.println(right);
			if(subject2 != null){
				rightPos = subject2.seqString().indexOf(right);
			}
			else{
				rightPos = subject.seqString().indexOf(right);
			}
			if(rightPos<0){
				System.err.println("Cannot find right, is it the correct sequence?");
				return;
			}
			flankOne = findLeft(subject.seqString().substring(0, leftPos), query.seqString());
			String seqRemain = query.seqString().replace(flankOne.getString(), replacementFlank);
			//some error checking on the length of the rightFlank
			int indexRemain = seqRemain.indexOf(replacementFlank);
			if(indexRemain>0){
				String queryRemain = seqRemain.substring(indexRemain+replacementFlank.length());
				//System.out.println("here:"+queryRemain);
				if(queryRemain.length() == 0 || queryRemain.startsWith("n")){
					this.setRemarks("We have nothing to search for on the rightFlank");
				}
			}
			// translocation
			if(subject2!= null){
				flankTwo = findRight(subject2.seqString().substring(rightPos), seqRemain);
				searchTranslocation = true;
			}
			else{
				flankTwo = findRight(subject.seqString().substring(rightPos), seqRemain);
			}
			//System.out.println("flankOne"+":"+flankOne+":"+flankOne.length());
			//System.out.println("flankTwo"+":"+flankTwo+":"+flankTwo.length());
			int posTest = flankOne.getSubjectEnd();
			if(subject.seqString().indexOf(flankOne.getString(), posTest+1)>0){
				this.setRemarks("leftFlank can be found at multiple places ["+flankOne+"]"+posTest);
			}
			
			if(flankOne.length()<minimumSizeWithLeftRight || flankTwo.length()<minimumSizeWithLeftRight ){
				//System.out.println(flankOne.length());
				//System.out.println(flankOne);
				//System.out.println(flankTwo.length());
				//System.out.println(flankTwo);
				if(flankOne.length()>=minimumSizeWithoutLeftRight && flankTwo.length()<minimumSizeWithoutLeftRight ){
					this.leftFlank = flankOne;
					this.setRemarks("Cannot find the second flank of the event, please do it manually");
					//System.out.println("Cannot find the second flank of the event, please do it manually");
					//System.err.println("Cannot find the second flank of the event, please do it manually");
				}
				else if(flankOne.length()<50 && flankTwo.length()<50 ){
					this.setRemarks("Cannot find the flanks of the event, please do it manually");
					//System.err.println("Cannot find the flanks of the event, please do it manually");
				}
				else{
					this.setRemarks("Not exactly sure what is happening, but something is wrong "+flankOne.length()+" "+flankTwo.length());
					//System.err.println("Not exactly sure what is happening, but something is wrong");
				}
			}
		}
		else{
			//assuming it is NOT on the reverse complement strand
			//TODO use the functions findLeft and findRight here
			flankOne = Left.getLeft(subject.seqString(), query.seqString(), true);
			//System.out.println(flankOne); 
			//this assumes the leftFlank is always on the left side
			String seqRemain = query.seqString().replace(flankOne.getString(), replacementFlank);
			String seqRemainSubject = subject.seqString().replace(flankOne.getString(), replacementFlank);
			int pos = seqRemainSubject.indexOf(replacementFlank);
			//System.out.println("remain:"+seqRemain);
			//System.out.println(seqRemainSubject);
			//System.out.println(seqRemainSubject.length());
			//System.out.println(seqRemain);
			//System.out.println(seqRemain.length());
			String seqRemainSubjectRest = seqRemainSubject.substring(pos);
			//start searching after the leftFlank
			flankTwo = Utils.longestCommonSubstring(seqRemainSubjectRest, seqRemain);
			if(flankOne.length()<minimumSizeWithoutLeftRight || flankTwo.length()<minimumSizeWithoutLeftRight ){
				//System.out.println(flankOne.length());
				//System.out.println(flankTwo.length());
				if(flankOne.length()>=minimumSizeWithoutLeftRight && flankTwo.length()<minimumSizeWithoutLeftRight ){
					this.leftFlank = flankOne;
					this.setRemarks("Cannot find the second flank of the event, please do it manually");
					System.err.println("Cannot find the second flank of the event, please do it manually");
				}
				else if(flankOne.length()<50 && flankTwo.length()<50 ){
					this.setRemarks("Cannot find the flanks of the event, please do it manually");
					System.err.println("Cannot find the flanks of the event, please do it manually");
				}
				else{
					this.setRemarks("Not exactly sure what is happening, but something is wrong");
					System.err.println("Not exactly sure what is happening, but something is wrong");
				}
				//TODO: fix this case
				return;
			}
		}
		
		//which one is left
		int posWithinSubjectOne = flankOne.getSubjectStart();
		int posWithinSubjectTwo = subject.seqString().indexOf(flankTwo);
		leftFlank = flankOne;
		rightFlank = flankTwo;
		if(!searchTranslocation){
			if(posWithinSubjectOne < posWithinSubjectTwo){
				leftFlank = flankOne;
				rightFlank = flankTwo;
			}
			else{
				//leftFlank = flankTwo;
				//rightFlank = flankOne;
			}
		}
		//System.out.println("leftFlank:"+leftFlank);
		//System.out.println("rightFlank:"+rightFlank);
		if(!searchTranslocation){
			int delPosStart = leftFlank.getSubjectEnd()+1;
			int delPosEnd = subject.seqString().indexOf(rightFlank);
			if(delPosEnd-delPosStart >=0){
				del = subject.seqString().substring(leftFlank.getSubjectEnd(), subject.seqString().indexOf(rightFlank));
			}
			else{
				del = "";
			}
			//System.out.println("del:"+del);
		}
		//translocation
		else if(searchTranslocation){
			leftPos = subject.seqString().indexOf(left)+left.length();
			int leftQueryPos = posWithinSubjectOne+flankOne.length();
			if(leftQueryPos < leftPos){
				del = subject.seqString().substring(leftQueryPos, leftPos);
			}
			del+=" - ";
			int rightQueryPos = subject2.seqString().indexOf(flankTwo);
			rightPos = subject2.seqString().indexOf(right);
			if(rightPos<rightQueryPos){
				del += subject2.seqString().substring(rightPos, rightQueryPos);
			}
		}
		//get the insertion
		//System.out.println(this.getName());
		//System.out.println("l:"+leftFlank);
		//System.out.println("r:"+rightFlank);
		int begin = leftFlank.getQueryStart();
		//System.out.println("start: "+(begin+leftFlank.length()));
		//System.out.println(query.seqString().indexOf(rightFlank,begin+leftFlank.length()));
		int end = query.seqString().indexOf(rightFlank,begin+leftFlank.length())+rightFlank.length();
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
			insert = query.seqString().substring(leftFlank.getQueryEnd(), end-rightFlank.length());
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
		if(subject2 == null){
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
		}
		//translocation
		else if(searchTranslocation){
			posWithinSubjectOne = flankOne.getSubjectEnd();
			while(insert.length()>0 && subject.seqString().charAt(posWithinSubjectOne) == insert.charAt(0)){
				leftFlank.addCharEnd(Character.toUpperCase(insert.charAt(0)));
				insert = insert.substring(1);
				posWithinSubjectOne++;
			}
			posWithinSubjectTwo = subject2.seqString().indexOf(flankTwo);
			while(insert.length()>0 && subject2.seqString().charAt(posWithinSubjectTwo) == insert.charAt(insert.length()-1)){
				rightFlank = Character.toUpperCase(insert.charAt(0))+ rightFlank;
				insert = insert.substring(0,insert.length()-1);
				posWithinSubjectTwo--;
			}
			//maybe we made an insertion which is incorrect
			if(del.startsWith(" - ")){
				String tempDel = del.replace(" - ", "");
				while(tempDel.length()>0 && insert.length()>0 && tempDel.charAt(0) == insert.charAt(0)){
					tempDel = tempDel.substring(1);
					insert = insert.substring(1);
				}
				//replace with right one
				del = " - "+tempDel;
			}
			//TODO also make the homology at the right side (copy & paste from above)
			/*
			//first check if the homology is at the right position, as it could be wrong
			while(del.length()>0 && del.charAt(0) == rightFlank.charAt(0)){
				leftFlank = leftFlank +del.charAt(0);
				del = del.substring(1)+rightFlank.charAt(0);
				rightFlank = rightFlank.substring(1);
			}
			*/
		}
		//no longer report as people might see it as an error, while it is more of a warning
		//if(madeMinimal){
			//this.setRemarks("Made deletion and insertion minimal, probably that means the cut was at a different location, moved by "+moved+" positions");
		//}
		//System.out.println("left :"+this.leftFlank);
		checkCall();
		String insertDelCommon =  Utils.longestCommonSubstring(del, insert);
		if(insertDelCommon.length()>10){
			//if we masked, then probably this check is not correct
			//after testing it turns out that most often this is correct
			this.setRemarks("Probably there is a mismatch/gap somewhere in the flank, which caused problems. Please inspect this file manually:"+insertDelCommon+" delpos: "+del.indexOf(insertDelCommon));
		}
	}
	private String findRight(String substring, String query) {
		//System.out.println("queryInit:"+query);
		int index = query.indexOf(replacementFlank);
		if(index>0){
			query = query.substring(index);
		}
		String first = Utils.longestCommonSubstring(substring, query);
		String leftOver = substring.substring(0,substring.indexOf(first));
		String queryOver = query.substring(0,query.indexOf(first));
		String second = Utils.longestCommonSubstring(leftOver, queryOver);
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
				return second;
			}
		}
		return first;
	}
	private Left findLeft(String substring, String query) {
		return Left.getLeft(substring, query, false);
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
		return query.getName();
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
		String s = "\t";
		int size = 20;
		String homology = "";
		int homologyLength = -1;
		checkCall();
		//only do it when no weird things found
		if(this.remarks.length() == 0 && insert.length()>0){
			//currently fixed value
			solveInsertion(solveInsertStart,solveInsertEnd);
		}
		if(del.length()>0 && insert.length() == 0){
			homology = Utils.getHomologyAtBreak(leftFlank.getString(), del, rightFlank);
			homologyLength = homology.length();
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
		String ret = cutType+s+getName()+s+dir+s+this.fileName+s+getIDPart()+s+possibleDouble+s+getSubject()+s+getSubjectComments()+s+query.seqString()+s+getLeftFlank(size)+s+getDel()+s+getRightFlank(size)+s+getInsertion()+s+this.getDelStart()+s+this.getDelEnd()+
				s+(this.getDelStart()-this.pamSiteLocation)+s+(this.getDelEnd()-this.pamSiteLocation)+s+(this.getDelStart()-this.pamSiteLocation)+s+getRightFlankRelativePos()+s+getColorHomology()+s+homology+s+homologyLength+s+delLength+s+this.getInsertion().length()+s+mod+s+getType()+s+getSecondaryType()+s+this.getRevCompInsertion()
				+s+this.getRangesString()+s+masked+s+getLeftSideRemoved()+s+getRightSideRemoved()+s+getRemarks()+s+reversed+s+this.getSchematic()+s+this.getUniqueClass()+s+this.inZone()+s+leftFlankLength+s+this.rightFlank.length();
		if(is != null){
			ret+= s+is.getLargestMatch()+s+is.getLargestMatchString()+s
					+is.getSubS()+s+is.getSubS2()+s+is.getType()+s+is.getLengthS()+s+is.getPosS()+s+is.getFirstHit()+s+is.getFirstPos();
		}
		return ret;
	}
	private void checkCall() {
		//only if remarks are not set this call is valid
		if(this.remarks.length()==0){
			String totalquery = this.getLeftFlank(0)+this.getInsertion()+this.getRightFlank(0);
			if(!query.seqString().contains(totalquery)){
				System.out.println("QUERY IS COMPLETELY BROKEN!!");
				System.out.println("l:"+getLeftFlank(0));
				System.out.println("i:"+getInsertion());
				System.out.println("r:"+getRightFlank(0));
				System.out.println("del:"+this.getDel());
				System.out.println("raw:"+query.seqString());
				System.out.println(getName());
				System.out.println(this.getRemarks());
				System.exit(0);
				
			}
			String totalSubject = this.getLeftFlank(0)+this.getDel()+this.getRightFlank(0);
			if(!subject.seqString().contains(totalSubject)){
				System.out.println("SUBJECT IS COMPLETELY BROKEN!!");
				System.out.println("l:"+getLeftFlank(0));
				System.out.println("i:"+getInsertion());
				System.out.println("r:"+getRightFlank(0));
				System.out.println("del:"+this.getDel());
				System.out.println("raw:"+query.seqString());
				System.out.println(getName());
				System.out.println(this.getRemarks());
				System.exit(0);
			}
			else{
				//System.out.println("SOMETHING IS COMPLETELY OK!!");
			}
		}
	}
	private String getHomologyTandemDuplication() {
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
	private void solveInsertion(int start, int end) {
		//disabled for translocation
		if(!searchTranslocation && this.insert.length()>=minSizeInsertionSolver){
			String left = this.getCorrectedLeftFlankRelative(start, end);
			String right = this.getCorrectedRightFlankRelative(start, end);
			InsertionSolverTwoSides is = new InsertionSolverTwoSides(left, right,this.insert,getName());
			is.setAdjustedPositionLeft(start);		
			is.setAdjustedPositionRight(start);
			is.search(true, true);
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
		int rightStart = subject.seqString().indexOf(rightFlank.toLowerCase());
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
					&& !is.getMatchS().contains(";") && is.getLargestMatch()>=minSizeInsertionSolver){
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
				return "TANDEMDUPLICATION_AND_OTHER_EVENT_FOUND_AND_REMAINING_"+lenghtOther;
			}
			//add other if you can find them
		}
		return "";
	}
	public String getSubject() {
		String ret = subject.getName();
		if(subject2 != null){
			ret += " | "+subject2.getName();
		}
		return ret;
	}
	public String getSubjectComments() {
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
		String ret = "CutType\tName\tDir\tFile\tgetIDPart\tpossibleDouble\tSubject\tgetSubjectComments\tRaw\tleftFlank\tdel\trightFlank\tinsertion\tdelStart\tdelEnd\tdelRelativeStart\tdelRelativeEnd\tdelRelativeStartTD\tdelRelativeEndTD\tgetHomologyColor\thomology\thomologyLength\tdelSize\tinsSize\tMod3\tType\tSecondaryType\tLongestRevCompInsert\tRanges\tMasked\t"
				+ "getLeftSideRemoved\tgetRightSideRemoved\tRemarks\tReversed\tSchematic\tClassName"+s+"InZone"+s+"leftFlankLength"+s+"rightFlankLength";
		ret+= s+"isGetLargestMatch"+s+"isGetLargestMatchString"+s
					+"isGetSubS"+s+"isGetSubS2"+s+"isGetType"+s+"isGetLengthS"+s+"isPosS"+s+"isFirstHit"+s+"getFirstPos";
		return ret;
	}
	public String getRevCompInsertion(){
		if(insert.length()>=5){
			String found = Utils.longestCommonSubstring(subject.seqString(), Utils.reverseComplement(insert)); 
			if(found.length()>=5){
				return found+"|"+subject.seqString().indexOf(found)+"-"+(subject.seqString().indexOf(found)+found.length());
			}
		}
		return "";
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
		if(subject2==null){
			return subject.seqString().indexOf(rightFlank);
		}
		return subject2.seqString().indexOf(rightFlank.toLowerCase());
	}
	public void setMinimumSizeWithoutLeftRight(int min){
		this.minimumSizeWithoutLeftRight = min;
	}
	public void setAndDetermineCorrectRange(double relaxedMaxError) {
		double maxError = relaxedMaxError;
		long first = -1;
		long last = quals.getLength()-1;
		for(long j = 0;j<quals.getLength();j++){
			if(first == -1 && quals.get(j).getErrorProbability()<=maxError){
				first = j;
			}
			//break range on the first bad base
			if(first >= 0 && quals.get(j).getErrorProbability()>maxError){
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
			String dna = this.query.seqString();
			String tempDNA = "";
			long j=0;
			for(Range r: ranges){
				long begin = r.getBegin();
				for(;j<begin;j++){
					tempDNA += "X";
				}
				long end = r.getEnd();
				for(;j<=end;j++){
					tempDNA += dna.charAt((int) j);
				}
				//System.out.println(this.getName());
				//System.out.println(r);
				//System.out.println(tempDNA);
			}
			while(tempDNA.length()<dna.length()){
				tempDNA += "X";
			}
			//System.out.println("mod:"+tempDNA);
			try {
				//no assignment
				this.query = RichSequence.Tools.createRichSequence(this.query.getName(), DNATools.createDNA(tempDNA));
				masked = true;
			} catch (IllegalSymbolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	public void maskSequenceToHighQualityRemove(String left, String right){
		left = left.toLowerCase();
		right = right.toLowerCase();
		if(ranges.size()==0){
			this.setRemarks("No high quality range found, unable to mask");
		}
		if(ranges.size()>=1){
			String dna = this.query.seqString();
			String tempDNA = "";
			long j=0;
			Range correct = null;
			String largestCommon = null;
			Range rangeContainingLargest = null;			
			Range largestRange = null;
			long largestRangeLength = -1;
			for(Range r: ranges){
				String sub = dna.substring((int)r.getBegin(), (int)r.getEnd());
				String lcsL = Utils.longestCommonSubstring(sub, left);
				String lcsR = Utils.longestCommonSubstring(sub, right);
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
					tempDNA += "X";
				}
				long end = correct.getEnd();
				for(;j<=end;j++){
					tempDNA += dna.charAt((int) j);
				}
				//System.out.println(this.getName());
				//System.out.println(correct);
				//System.out.println(tempDNA);
				while(tempDNA.length()<dna.length()){
					tempDNA += "X";
				}
				//System.out.println("mod:"+tempDNA);
				try {
					//no assignment
					this.query = RichSequence.Tools.createRichSequence(this.query.getName(), DNATools.createDNA(tempDNA));
					masked = true;
				} catch (IllegalSymbolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				this.setRemarks("No high quality range found with left and right part, unable to mask");
			}
		}
	}
	public void setAdditionalSearchString(Vector<Sequence> additional){
		this.additionalSearchSequence = additional;
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
			solveInsertion(solveInsertStart,solveInsertEnd);
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
			String tempQuery = query.seqString().substring(indexQuery);
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
	public String getKey() {
		//left, right, del, insert
		String key = this.fileName+"_"+this.getDelStart()+"_"+this.getDelEnd()+"_"+this.del+"_"+this.insert;
		return key;
	}
	public int getRelativeDelEnd(){
		return this.getDelEnd()-this.pamSiteLocation;
	}
}

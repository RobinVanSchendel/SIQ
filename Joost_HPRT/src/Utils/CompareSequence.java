package Utils;
import java.util.ArrayList;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.jcvi.jillion.core.Range;
import org.jcvi.jillion.core.qual.QualitySequence;


public class CompareSequence {

	private Sequence subject, query, subject2;
	private String leftFlank = "", rightFlank = "", del = "", insert = "", remarks = "";
	private int minimumSizeWithoutLeftRight = 30;
	private int minimumSizeWithLeftRight = 15;
	private int pamSiteLocation;
	private final static String replacementFlank = "FLK1";
	private String leftSite, rightSite;
	public final static int minimalRangeSize = 100;
	private ArrayList<Range> ranges = new ArrayList<Range>();
	private boolean masked = false;
	public CompareSequence(Sequence subject, Sequence subject2, Sequence query, String left, String right, String pamSite) {
		this.subject = subject;
		this.subject2 = subject2;
		this.query = query;
		this.leftSite = left;
		this.rightSite = right;
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
		if(altSize>size){
			try {
				this.query = DNATools.createDNASequence(Utils.reverseComplement(query.seqString().toString()), this.query.getName());
			} catch (IllegalSymbolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void determineFlankPositions(){
		determineFlanksPositions(leftSite.toLowerCase(), rightSite.toLowerCase());
	}
	private void determineFlanksPositions(String left, String right) {
		int leftPos = -2;
		int rightPos = -2;
		String flankOne = "";
		String flankTwo = "";
		//System.out.println(left+":"+left.length());
		//System.out.println(right+":"+right.length());
		//System.out.println(subject.seqString().toString());
		//System.out.println(subject2.seqString().toString());
		if(left != null && left.length()>=15 && right != null && right.length()>=15){
			leftPos = subject.seqString().indexOf(left)+left.length();
			System.out.println(right);
			if(subject2 != null){
				rightPos = subject2.seqString().indexOf(right);
			}
			else{
				rightPos = subject.seqString().indexOf(right);
			}
			flankOne = findLeft(subject.seqString().substring(0, leftPos), query.seqString());
			String seqRemain = query.seqString().replace(flankOne, replacementFlank);
			// translocation
			if(subject2!= null){
				flankTwo = findRight(subject2.seqString().substring(rightPos), seqRemain);
			}
			else{
				flankTwo = findRight(subject.seqString().substring(rightPos), seqRemain);
			}
			//System.out.println("flankOne"+""+flankOne);
			//System.out.println("flankTwo"+""+flankTwo);
			if(flankOne.length()<minimumSizeWithLeftRight || flankTwo.length()<minimumSizeWithLeftRight ){
				System.out.println(flankOne.length());
				System.out.println(flankOne);
				System.out.println(flankTwo.length());
				System.out.println(flankTwo);
				if(flankOne.length()>=minimumSizeWithoutLeftRight && flankTwo.length()<minimumSizeWithoutLeftRight ){
					this.leftFlank = flankOne;
					this.setRemarks("Cannot find the second flank of the event, please do it manually");
					System.out.println("Cannot find the second flank of the event, please do it manually");
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
			}
		}
		else{
			//assuming it is NOT on the reverse complement strand
			flankOne = Utils.longestCommonSubstring(subject.seqString(), query.seqString());
			 
			//this assumes the leftFlank is always on the left side
			String seqRemain = query.seqString().replace(flankOne, replacementFlank);
			String seqRemainSubject = subject.seqString().replace(flankOne, replacementFlank);
			flankTwo = Utils.longestCommonSubstring(seqRemainSubject, seqRemain);
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
			}
		}
		
		//which one is left
		int posWithinSubjectOne = subject.seqString().indexOf(flankOne);
		int posWithinSubjectTwo = subject.seqString().indexOf(flankTwo);
		leftFlank = flankOne;
		rightFlank = flankTwo;
		if(subject2 == null){
			if(posWithinSubjectOne < posWithinSubjectTwo){
				leftFlank = flankOne;
				rightFlank = flankTwo;
			}
			else{
				leftFlank = flankTwo;
				rightFlank = flankOne;
			}
		}
		//System.out.println("leftFlank:"+leftFlank);
		//System.out.println("rightFlank:"+rightFlank);
		if(subject2 == null){
			int delPosStart = subject.seqString().indexOf(leftFlank)+leftFlank.length()+1;
			int delPosEnd = subject.seqString().indexOf(rightFlank);
			if(delPosEnd-delPosStart >=0){
				del = subject.seqString().substring(subject.seqString().indexOf(leftFlank)+leftFlank.length(), subject.seqString().indexOf(rightFlank));
			}
			else{
				del = "";
			}
		}
		//translocation
		else{
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
		//System.out.println("l:"+leftFlank);
		//System.out.println("r:"+rightFlank);
		int begin = Math.min(query.seqString().indexOf(leftFlank), query.seqString().indexOf(rightFlank)); 
		int end = Math.max(query.seqString().indexOf(leftFlank)+leftFlank.length(), query.seqString().indexOf(rightFlank)+rightFlank.length());
		String insertContainingPart = query.seqString().substring(begin, end);
		insert = insertContainingPart.replace(leftFlank, "").replace(rightFlank, "");
		
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
		boolean madeMinimal = false;
		int moved = 0;
		if(subject2 == null){
			while(del.length()>0 && insert.length()>0 && del.charAt(0) == insert.charAt(0)){
				leftFlank = leftFlank +del.charAt(0);
				del = del.substring(1);
				insert = insert.substring(1);
				madeMinimal = true;
				moved++;
			}
			while(del.length()>0 && insert.length()>0 && del.charAt(del.length()-1) == insert.charAt(insert.length()-1)){
				rightFlank = del.charAt(del.length()-1)+rightFlank;
				del = del.substring(0,del.length()-1);
				insert = insert.substring(0,insert.length()-1);
				madeMinimal = true;
				moved++;
			}
		}
		//translocation
		else{
			posWithinSubjectOne = subject.seqString().indexOf(flankOne)+flankOne.length();
			while(insert.length()>0 && subject.seqString().charAt(posWithinSubjectOne) == insert.charAt(0)){
				leftFlank = leftFlank +Character.toUpperCase(insert.charAt(0));
				insert = insert.substring(1);
				posWithinSubjectOne++;
				madeMinimal = true;
				moved++;
			}
			posWithinSubjectTwo = subject2.seqString().indexOf(flankTwo);
			while(insert.length()>0 && subject2.seqString().charAt(posWithinSubjectTwo) == insert.charAt(insert.length()-1)){
				rightFlank = Character.toUpperCase(insert.charAt(0))+ rightFlank;
				insert = insert.substring(0,insert.length()-1);
				madeMinimal = true;
				moved++;
				posWithinSubjectTwo--;
			}
		}
		if(madeMinimal){
			this.setRemarks("Made deletion and insertion minimal, probably that means the cut was at a different location, moved by "+moved+" positions");
		}
		String insertDelCommon =  Utils.longestCommonSubstring(del, insert);
		if(insertDelCommon.length()>10){
			this.setRemarks("Probably there is a mismatch somewhere in the flank, which caused problems. Please inspect this file manually:"+insertDelCommon);
			System.err.println("Probably there is a mismatch somewhere in the flank, which caused problems. Please inspect this file manually:"+insertDelCommon);
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
		//System.out.println("substring:"+substring);
		//System.out.println("first:"+first);
		//System.out.println("query:"+query);
		String queryOver = query.substring(0,query.indexOf(first));
		if(Utils.longestCommonSubstring(leftOver, queryOver).length()>10){
			return Utils.longestCommonSubstring(leftOver, queryOver);
		}
		return first;
	}
	private String findLeft(String substring, String query) {
		String first = Utils.longestCommonSubstring(substring, query);
		String leftOver = substring.substring(substring.indexOf(first)+first.length());
		String queryOver = query.substring(query.indexOf(first)+first.length());
		if(Utils.longestCommonSubstring(leftOver, queryOver).length()>10){
			return Utils.longestCommonSubstring(leftOver, queryOver);
		}
		return first;
	}
	public String getLeftFlank(int size){
		if(leftFlank.length()<size){
			return leftFlank;
		}
		else{
			return leftFlank.substring(leftFlank.length()-size);
		}
		
	}
	public String getRightFlank(int size){
		if(rightFlank.length()<size){
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
	public String toStringOneLine(){
		String spacer = "\t";
		int size = 20;
		String homology = "";
		int homologyLength = -1;
		if(del.length()>0 && insert.length() == 0){
			homology = Utils.getHomologyAtBreak(leftFlank, del, rightFlank);
			homologyLength = homology.length();
		}
		int delLength = this.getDel().length();
		if(this.getDel().contains(" - ")){
			delLength -= 3;
		}
		String ret = getName()+spacer+getSubject()+spacer+query.seqString()+spacer+getLeftFlank(size)+spacer+getDel()+spacer+getRightFlank(size)+spacer+getInsertion()+spacer+this.getDelStart()+spacer+this.getDelEnd()+
				spacer+(this.getDelStart()-this.pamSiteLocation)+spacer+(this.getDelEnd()-this.pamSiteLocation)+spacer+homology+spacer+homologyLength+spacer+delLength+spacer+this.getInsertion().length()+spacer+this.getRevCompInsertion()
				+spacer+this.getRangesString()+spacer+masked+spacer+getRemarks();
		return ret;
	}
	private String getSubject() {
		String ret = subject.getName();
		if(subject2 != null){
			ret += " | "+subject2.getName();
		}
		return ret;
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
	private String getRemarks() {
		return remarks;
	}
	private void setRemarks(String remark) {
		if(this.remarks.length()>0){
			remarks += ":";
		}
		this.remarks += remark;
	}
	public static String getOneLineHeader() {
		return "Name\tSubject\tRaw\tleftFlank\tdel\trightFlank\tinsertion\tdelStart\tdelEnd\tdelRelativeStart\tdelRelativeEnd\thomology\thomologyLength\tdelSize\tinsSize\tLongestRevCompInsert\tRanges\tMasked\tRemarks";
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
		System.out.println(this.getName());
		System.out.println(leftFlank);
		return subject.seqString().indexOf(leftFlank.toLowerCase())+leftFlank.length();
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
	public void setAndDetermineCorrectRange(QualitySequence quals, double relaxedMaxError) {
		double maxError = relaxedMaxError;
		long first = -1;
		long last = quals.getLength()-1;
		for(long j = 0;j<quals.getLength();j++){
			if(first == -1 && quals.get(j).getErrorProbability()<=maxError){
				first = j;
			}
			//break range on the first bad base
			if(first >= 0 && quals.get(j).getErrorProbability()>maxError){
				if(j-first >= minimalRangeSize){
					//System.out.println("range is "+first+"-"+last);
					ranges.add(Range.of(first, last));
				}
				first = -1;
				last = quals.getLength()-1;
			}
			last = j;
		}
		if(first >= 0 && first - last >= minimalRangeSize){
			//System.out.println("range is "+first+"-"+last);
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
				System.out.println(this.getName());
				System.out.println(r);
				System.out.println(tempDNA);
			}
			while(tempDNA.length()<dna.length()){
				tempDNA += "X";
			}
			//System.out.println("mod:"+tempDNA);
			try {
				//no assignment
				this.query = DNATools.createDNASequence(tempDNA, this.query.getName());
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
			for(Range r: ranges){
				String sub = dna.substring((int)r.getBegin(), (int)r.getEnd());
				//take the first
				correct = r;
				break;
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
				System.out.println(this.getName());
				System.out.println(correct);
				System.out.println(tempDNA);
				while(tempDNA.length()<dna.length()){
					tempDNA += "X";
				}
				//System.out.println("mod:"+tempDNA);
				try {
					//no assignment
					this.query = DNATools.createDNASequence(tempDNA, this.query.getName());
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
}

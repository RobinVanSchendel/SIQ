package utils;

public class Left {
	
	private static final int ALLLOWEDJUMPDISTANCE = 1;
	//this introduces possible problems... I am aware of this 'feature' missing SNVs 30bp away from flanks
	private static final int MINIMUMSECONDSIZE = 30;
	
	private int subjectStart, subjectEnd, queryStart, queryEnd;
	private String string;
	private boolean jumpedLeft = false;
	public Left(String s, int start, int end, int queryStart, int queryEnd, boolean jumpedLeft){
		this.string = s;
		this.subjectStart = start;
		this.subjectEnd = end;
		this.queryStart = queryStart;
		this.queryEnd = queryEnd;
		this.jumpedLeft = jumpedLeft;
	}
	public Left(String s, int start, int end, int queryStart, int queryEnd){
		this.string = s;
		this.subjectStart = start;
		this.subjectEnd = end;
		this.queryStart = queryStart;
		this.queryEnd = queryEnd;
	}
	public String getString(){
		return string;
	}
	public static Left getLeft(String subjectPart, String query, boolean findLeftMost, boolean allowJump){
		String first = Utils.longestCommonSubstring(subjectPart, query);
		int start = subjectPart.indexOf(first);
		int end = start+first.length();
		int queryStart = query.indexOf(first);
		int queryEnd = query.indexOf(first)+first.length();
		
		String leftOver = subjectPart.substring(end);
		String queryOver = query.substring(queryEnd);
		String second = Utils.longestCommonSubstring(leftOver, queryOver);
		int startSecond = subjectPart.indexOf(second, start);
		int endSecond = startSecond+second.length();
		int queryStartSecond = query.indexOf(second,queryStart);
		int queryEndSecond = queryStartSecond+second.length();
		//System.out.println(query);
		//System.out.println(preFirst);
		//System.out.println("left:"+first+":"+first.length());
		//System.out.println("leftS:"+second+":"+second.length());
		//when no left or rightflank is given we need to maybe report the first biggest part!
		if(findLeftMost) {
			String preFirst = Utils.longestCommonSubstring(subjectPart.substring(0, start),query);
			if(preFirst.length()>MINIMUMSECONDSIZE) {
				start = subjectPart.indexOf(preFirst);
				end = start+preFirst.length();
				queryStart = query.indexOf(preFirst);
				queryEnd = query.indexOf(preFirst)+preFirst.length();
				Left firstLeft = new Left(preFirst,start, end,queryStart, queryEnd);
				return firstLeft;
			}
		}
		
		Left firstLeft = new Left(first,start, end,queryStart, queryEnd);
		Left secondLeft = new Left(second,startSecond, endSecond, queryStartSecond,queryEndSecond);
		//System.out.println(leftOver.endsWith(second));
		if(allowJump && second.length()>MINIMUMSECONDSIZE){
			//check if we allow the jump, previously this led to deletions not being spotted
			int locFirstSub = subjectPart.indexOf(first)+first.length();
			int secSecondSubEnd = subjectPart.indexOf(second);
			int jumpDist = secSecondSubEnd-locFirstSub;
			if(jumpDist<=ALLLOWEDJUMPDISTANCE){
				//System.out.println("jumping Left "+jumpDist);
				secondLeft.setJumpedLeft();
				return secondLeft;
			}
		}
		return firstLeft;
	}
	private void setJumpedLeft() {
		this.jumpedLeft = true;
	}
	public int length(){
		return string.length();
	}
	public String toString(){
		return this.queryStart+"-"+this.queryEnd+":"+this.subjectStart+"-"+this.subjectEnd+"::"+string;
	}
	public int getSubjectEnd() {
		return this.subjectEnd;
	}
	public int getSubjectStart() {
		return this.subjectStart;
	}
	public int getQueryStart() {
		return this.queryStart;
	}
	public int getQueryEnd() {
		return this.queryEnd;
	}
	public void addCharEnd(char c){
		this.string+=c;
		this.queryEnd++;
		this.subjectEnd++;
	}
	public boolean getJumped() {
		return this.jumpedLeft;
	}
}

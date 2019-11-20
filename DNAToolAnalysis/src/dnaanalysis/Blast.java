package utils;


public class Blast {
	private String id, chr, subjectId;
	private double pIdentity;
	private int length, mismatch, gapopen, qStart, qEnd, sStart, sEnd;
	private boolean revComMatch = false;
	private int nrMerges = 1;
	private float evalue;
	private int hitsForThisID;
	private String subject;
	private String query;
	private String remarks = "";
	
	public Blast(String id){
		this.setId(id);
	}

	public static Blast parseBlast(String line) {
		// TODO Auto-generated method stub
		//System.out.println(line);
		String[] parts = line.split("\t");
		int index = 0;
		Blast b = new Blast(parts[index++]);
		b.setChr(parts[index++]);
		b.setpIdentity(Double.parseDouble(parts[index++]));
		b.setLength(Integer.parseInt(parts[index++]));
		b.setMismatch(Integer.parseInt(parts[index++]));
		b.setGapopen(Integer.parseInt(parts[index++]));
		b.setqStart(Integer.parseInt(parts[index++]));
		b.setqEnd(Integer.parseInt(parts[index++]));
		b.setSStart(Integer.parseInt(parts[index++]));
		b.setSEnd(Integer.parseInt(parts[index++]));
		b.setEValue(Float.parseFloat(parts[index++]));
		//skip one
		index++;
		if(parts.length>=14){
			b.setQuery(parts[index++]);
			b.setSubject(parts[index++]);
		}
		if(b.getsEnd()<b.getsStart()){
			b.setRevComMatch(true);
		}
		return b;
	}

	private void setSubject(String string) {
		this.subject = string;
	}

	private void setQuery(String string) {
		this.query = string;
	}

	private void setEValue(float evalue) {
		this.evalue = evalue;
		
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	public String toString(){
		String s = "\t";
		String ret = "id:"+id+s;
		ret += "chr:"+chr+s;
		ret += "length:"+getLength()+s;
		ret += "identity:"+s+getpIdentity()+s;
		ret += "qStart:"+this.getqStart()+s;
		ret += "qEnd:"+this.getqEnd()+s;
		ret += "sStart:"+this.getsStart()+s;
		ret += "sEnd:"+this.getsEnd()+s;
		ret += "gaps:"+this.getGapopen()+s;
		ret += "mismatch:"+this.getMismatch()+s;
		ret += "evalue:"+this.getEValue()+s;
		ret += "identical:"+this.querySubjectIdentical()+s;
		ret += "remarks:"+this.remarks+s;
		if(this.isMerged()){
			ret += "MERGED:"+this.nrMerges+s;
		}
		ret += "leftNrBases:"+s+this.nrOfBasesIdentityStart()+s;
		ret += "rightNrBases:"+s+this.nrOfBasesIdentityEnd()+s;
		ret += query+s;
		ret += subject;
		return ret;
		//return id+s+chr+s+this.getLength()+s+this.getpIdentity()+s+this.getsStart()+s+this.getsEnd()+s+this.revComMatch+s+this.getMismatch()+s+gapopen;
	}

	/**
	 * @return the chr
	 */
	public String getChr() {
		return chr;
	}

	/**
	 * @param chr the chr to set
	 */
	public void setChr(String chr) {
		this.chr = chr;
	}

	/**
	 * @return the pIdentity
	 */
	public double getpIdentity() {
		return pIdentity;
	}

	/**
	 * @param pIdentity the pIdentity to set
	 */
	public void setpIdentity(double pIdentity) {
		this.pIdentity = pIdentity;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @param length the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * @return the mismatch
	 */
	public int getMismatch() {
		return mismatch;
	}

	/**
	 * @param mismatch the mismatch to set
	 */
	public void setMismatch(int mismatch) {
		this.mismatch = mismatch;
	}

	/**
	 * @return the gapopen
	 */
	public int getGapopen() {
		return gapopen;
	}

	/**
	 * @param gapopen the gapopen to set
	 */
	public void setGapopen(int gapopen) {
		this.gapopen = gapopen;
	}

	/**
	 * @return the qStart
	 */
	public int getqStart() {
		return qStart-1;
	}

	/**
	 * @param qStart the qStart to set
	 */
	public void setqStart(int qStart) {
		this.qStart = qStart;
	}

	/**
	 * @return the qEnd
	 */
	public int getqEnd() {
		return qEnd;
	}

	/**
	 * @param qEnd the qEnd to set
	 */
	public void setqEnd(int qEnd) {
		this.qEnd = qEnd;
	}

	/**
	 * @return the sStart
	 */
	public int getsStart() {
		return sStart;
	}
	
	public float getEValue(){
		return evalue;
	}

	/**
	 * @param sStart the sStart to set
	 */
	public void setSStart(int sStart) {
		this.sStart = sStart;
	}

	/**
	 * @return the send
	 */
	public int getsEnd() {
		return sEnd;
	}

	/**
	 * @param send the send to set
	 */
	public void setSEnd(int send) {
		sEnd = send;
	}

	/**
	 * @return the revComMatch
	 */
	public boolean isRevComMatch() {
		return revComMatch;
	}

	/**
	 * @param revComMatch the revComMatch to set
	 */
	public void setRevComMatch(boolean revComMatch) {
		this.revComMatch = revComMatch;
	}
	public boolean matchesWholeQuery(int length){
		if(this.getLength()>=length){
			return true;
		}
		return false;
	}
	//return the distance to these positions
	//if there is an overlap, the distance will be -1
	public int getDistance(int start, int end) {
		//overlap?
		if(this.getsStart()<end && this.getsEnd()>start){
			//instead of returning -1, let's see what the min distance is to the begin
			//return -1;
			if(this.revComMatch){
				return Math.max(this.getsStart()-end, start-this.getsEnd());
			}
			else{
				//System.out.println(this.getsStart()-start);
				//System.out.println(end-this.getSEnd());
				return Math.max(start-this.getsStart(), this.getsEnd()-end);
			}
		}
		else{
			//query if before
			if(end < this.getsStart()){
				return Math.min(Math.abs(end-this.getsStart()), Math.abs(end-this.getsEnd()));
			}
			else if(start > this.getsEnd()){
				return Math.min(Math.abs(start-this.getsStart()), Math.abs(start-this.getsEnd()));
			}
		}
		// TODO Auto-generated method stub
		return -2;
	}

	public String getOverlapType(int start, int end) {
		int tempBStart = this.sStart;
		int tempBEnd = this.sEnd;
		//switch
		if(this.revComMatch){
			//start = end
			tempBStart = this.sEnd;
			tempBEnd = this.sStart;
		}
		if(start<= tempBStart && tempBEnd <= end){
			return "OVERLAP_BLASTWITHIN";
		}
		else if(tempBEnd <= start){
			return "BEFORE";
		}
		else if(tempBStart >= end){
			return "AFTER";
		}
		else if(tempBStart<= start && tempBEnd > start){
			return "OVERLAP_BLASTLEFT";
		}
		else if(tempBStart<= end && tempBEnd > end){
			return "OVERLAP_BLASTRIGHT";
		}
		return null;
	}
	public int getHitsForThisID() {
		return hitsForThisID;
	}
	public void setHitsForThisID(int hitsForThisID) {
		this.hitsForThisID = hitsForThisID;
	}
	public String getSubjectLocation() {
		return this.getChr()+":"+this.getsStart()+"-"+this.getsEnd() +"("+this.getLength()+")";
	}
	public boolean querySubjectIdentical(){
		//System.out.println("query");
		//System.out.println("["+query+"]");
		//System.out.println("["+subject+"]");
		if(query == null || subject == null){
			return false;
		}
		return query.equals(subject);
	}
	public String getQuery() {
		return this.query;
	}
	public String getSubject(){
		return this.subject;
	}
	public void makeSubjectQueryIdentical(boolean trimLeft, boolean trimRight, int nrOfBasesRequired){
		//all of them, which is the longest commonsubstring
		if(nrOfBasesRequired == -1){
			nrOfBasesRequired = Utils.longestCommonSubstring(query, subject).length();
		}
		if(!querySubjectIdentical()){
			//trim left side
			//System.out.println(this);
			if(trimLeft){
				//this.addRemark("trimmed left ("+reduction+")");
				
				int total = 0;
				//System.out.println(query);
				//System.out.println(subject);
				while(this.nrOfBasesIdentityStart()<nrOfBasesRequired){
					//no gap in query
					if(query.charAt(0) != '-'){
						this.qStart++;
					}
					//System.out.println(query.charAt(0));
					//System.out.println(qStart);
					//no gap in subject
					if(subject.charAt(0) != '-'){
						if(this.isRevComMatch()){
							this.sStart--;
						}
						else{
							this.sStart++;
						}
					}
					this.query = query.substring(1);
					this.subject = subject.substring(1);
					total++;
				}
				updateIdentityMismatchGapLength();
				if(total>0){
					this.addRemark("Trimmed left "+total);
				}
			}
			if(trimRight){
				int total = 0;
				//System.out.println(this.getId());
				//System.out.println("position:"+position);
				//System.out.println(longest);
				//System.out.println(query);
				//System.out.println(subject);
				//Tolerate one mismatch
				/*
				if(this.id.equals("ENA|LN484420|LN484420.1")){
					System.out.println(this.query);
					System.out.println(this.subject);
				}
				*/
				int queryRem = 0;
				int subjectRem = 0;
				while(this.nrOfBasesIdentityEnd()<nrOfBasesRequired){
					if(query.charAt(query.length()-1) != '-'){
						queryRem++;
					}
					if(subject.charAt(subject.length()-1) != '-'){
						subjectRem++;
					}
					query = query.substring(0, query.length()-1);
					subject = subject.substring(0, subject.length()-1);
					total++;
				}
				/*
				if(this.id.equals("ENA|LN484420|LN484420.1")){
					System.out.println(this.query);
					System.out.println(this.subject);
				}
				*/
				this.updateIdentityMismatchGapLength();
				this.qEnd = qEnd-queryRem;
				if(this.isRevComMatch()){
					this.sEnd = sEnd+subjectRem;
				}
				else{
					this.sEnd = sEnd-subjectRem;
				}
				if(total>0){
					this.addRemark("Trimmed right "+total);
				}
			}
		}
	}
	private void updateIdentityMismatchGapLength() {
		this.length = query.length();
		int gaps = 0;
		int mismatches = 0;
		int identity = 0;
		for(int i = 0;i<length;i++){
			if(query.charAt(i) == '-' || subject.charAt(i) == '-'){
				gaps++;
			}
			else if(query.charAt(i) != subject.charAt(i)){
				mismatches++;
			}
			else{
				identity++;
			}
		}
		this.gapopen = gaps;
		this.mismatch = mismatches;
		this.pIdentity = 100*identity/(double)this.length;
	}

	public void addRemark(String s){
		if(this.remarks.length()>0){
			remarks += ":";
		}
		remarks += s;
	}

	public boolean inVicinity(Blast b, int distance) {
		if(this.getChr().equals(b.getChr()) && this.isRevComMatch() == b.isRevComMatch()){
			if(Math.abs(Math.max(b.getsStart(),b.getsEnd()-Math.min(this.getsStart(), this.getsEnd())))< distance){
				return true;
			}
			if(Math.abs(Math.min(b.getsStart(),b.getsEnd()-Math.max(this.getsStart(), this.getsEnd())))< distance){
				return true;
			}
		}
		return false;
	}

	public int getNrMerges() {
		return nrMerges;
	}
	public boolean isMerged() {
		return nrMerges>1;
	}

	public void setNrMerges(int nrMerges) {
		this.nrMerges = nrMerges;
	}

	public void mergeBlast(Blast b) {
		if(this.isRevComMatch()){
			this.sStart = Math.max(this.sStart, b.getsStart());
			this.sEnd = Math.min(this.sEnd, b.getsEnd());
		}
		else{
			this.sStart = Math.min(this.sStart, b.getsStart());
			this.sEnd = Math.max(this.sEnd, b.getsEnd());
		}
		this.qStart = Math.min(this.qStart, b.getqStart());
		this.qEnd = Math.max(this.qEnd, b.getqEnd());
		this.nrMerges++;
		this.length = Math.abs(sEnd-sStart);
		//NOTE THIS DOES NOT TAKE INTO ACCOUNT THE order of both the query and the subject
		this.query +=":"+b.getQuery();
		this.subject +=":"+b.getSubject();
	}
	public String getRemarks(){
		return remarks;
	}
	public int nrOfBasesIdentityEnd(){
		int nr = 0;
		int max = Math.max(query.length(), subject.length());
		for(int i = max-1;i>=0;i--){
			//Not sure if allowing N is a good thing here...
			if(query.charAt(i) == subject.charAt(i) || (query.charAt(i) == 'N' && subject.charAt(i) != 'N')){
				nr++;
			}
			else{
				break;
			}
		}
		return nr;
	}
	public int nrOfBasesIdentityStart(){
		int nr = 0;
		int max = Math.max(query.length(), subject.length());
		for(int i = 0;i<max;i++){
			//Not sure if allowing N is a good thing here...
			if(query.charAt(i) == subject.charAt(i) || (query.charAt(i) == 'N' && subject.charAt(i) != 'N')){
				nr++;
			}
			else{
				break;
			}
		}
		return nr;
		
	}
	public int getFinalOkPosition(int nrBases, boolean mismatchIsWrong) {
		for(int i = query.length()-1;i>0;i--){
			boolean allOK = true;
			for (int j = i;j>i-nrBases;j--){
				if(mismatchIsWrong){
					//all differences are NOT ok
					if(query.charAt(j) != subject.charAt(j)){
						allOK = false;
					}
				}
				else{
					//only gaps are not ok
					if(query.charAt(j) == '-' || subject.charAt(j) == '-'){
						allOK = false;
					}
				}
			}
			if(allOK){
				return getqStart()+i;
			}
		}
		System.out.println("I have no clue...");
		return -1;
	}
	public int getBeginOkPosition(int nrBases, boolean mismatchIsWrong) {
		for(int i = 0;i<query.length();i++){
			boolean allOK = true;
			for (int j = i;j<i+nrBases;j++){
				if(mismatchIsWrong){
					//all differences are NOT ok
					if(query.charAt(j) != subject.charAt(j)){
						allOK = false;
					}
				}
				else{
					//only gaps are not ok
					if(query.charAt(j) == '-' || subject.charAt(j) == '-'){
						allOK = false;
					}
				}
			}
			if(allOK){
				return getqStart()+i;
			}
		}
		System.out.println("I have no clue...");
		return -1;
	}
	public int nrOfBasesGapEnd() {
		int nr = 0;
		int max = Math.max(query.length(), subject.length());
		for(int i = max-1;i>=0;i--){
			//NOT sure if it is ok to allow 'N' here
			if(query.charAt(i) == '-' || subject.charAt(i) == '-'){
				break;
			}
			else{
				nr++;
			}
		}
		return nr;
	}

	public int nrOfBasesGapStart() {
		int nr = 0;
		int max = Math.max(query.length(), subject.length());
		for(int i = 0;i<max;i++){
			//NOT sure if it is ok to allow 'N' here
			if(query.charAt(i) == '-' || subject.charAt(i) == '-'){
				break;
			}
			else{
				nr++;
			}
		}
		return nr;
	}
	public int getStartInfoNr(int minIdentity) {
		int max = Math.max(query.length(), subject.length());
		if(max<minIdentity){
			minIdentity = max;
		}
		int nr = 0;
		for(int i = 0;i<minIdentity;i++){
			if((query.charAt(i) == 'N' || subject.charAt(i) == 'N')){
				nr++;
			}
			else if(query.charAt(i) == '-' || subject.charAt(i) == '-'){
				nr++;
			}
			else if(query.charAt(i) != subject.charAt(i)){
				nr++;
			}
		}
		return nr;
	}
	public String getStartInfo(int minIdentity) {
		String ret = "";
		int max = Math.max(query.length(), subject.length());
		if(max<minIdentity){
			minIdentity = max;
		}
		for(int i = 0;i<minIdentity;i++){
			String temp = "";
			if((query.charAt(i) == 'N' || subject.charAt(i) == 'N')){
				temp+= i+":N";
			}
			else if(query.charAt(i) == '-' || subject.charAt(i) == '-'){
				temp+= i+":gap";
			}
			else if(query.charAt(i) != subject.charAt(i)){
				temp+= i+":"+subject.charAt(i)+">"+query.charAt(i);
			}
			if(temp != ""){
				if(ret.length()>0){
					ret+="|";
				}
				ret+= temp;
			}
		}
		return ret;
	}
}

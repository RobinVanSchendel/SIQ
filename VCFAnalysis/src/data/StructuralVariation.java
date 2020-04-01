package data;

import java.util.ArrayList;
import java.util.List;

import dnaanalysis.InsertionSolverTwoSides;
import dnaanalysis.RandomInsertionSolverTwoSides;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.variant.variantcontext.Allele;

public class StructuralVariation {
	public enum SVType {DEL,DELINS,SINS,INV,TD,TDINS, TRANS};
	private SVType type;
	private String insert = "";
	private Location start, end;
	private ArrayList<Sample> samples;
	private ArrayList<MetaData> metadata;
	private List<Allele> alleles;
	private String info;
	private String origCaller;
	
	public StructuralVariation(SVType type, Location start, Location end, String caller) {
		setType(type);
		setStart(start);
		setEnd(end);
		checkLeftRight();
		if(caller==null) {
			System.err.println("caller can not be null");
			System.exit(0);
		}
		this.origCaller = caller;
	}
	public void addMetaData(ReferenceSequenceFile rsf) {
		metadata = new ArrayList<MetaData>();
		setHomology(rsf);
		//maybe these should be configurable??
		int sizeOutDel = 50;
		int sizeInDel = 50;
		setFlankInsert(rsf, 5, sizeInDel, sizeOutDel);
		setHighestGriddCall();
	}
	
	private void setHighestGriddCall() {
		MetaData vf = new MetaData("GRIDSS_MAX_VF");
		double max = 0;
		for(Sample s: samples) {
			Caller c = s.getCaller(GridssCaller.nameCaller);
			if(c!= null && c.getGt()!= null) {
				String rpString = (String)c.getGt().getExtendedAttribute("VF");
				double rp = Double.parseDouble(rpString);
				if(rp>max) {
					max = rp;
				}
			}
		}
		vf.setValue(max);
		metadata.add(vf);
	}
	private void setFlankInsert(ReferenceSequenceFile rsf, int minSize, int sizeInDel, int sizeOutDel) {
		MetaData flankInsert = new MetaData("FlankInsert");
		MetaData flankInsertLM = new MetaData("FlankInsertLargestMatch");
		
		if(this.getType()==SVType.DELINS || this.getType()==SVType.SINS || this.getType()==SVType.TDINS) {
			if(this.getInsert().length()>=minSize && (!this.getInsert().contains("n") && !this.getInsert().contains("N"))) {
				int startLeft = getStart().getPosition()-sizeOutDel;
				int endLeft = getStart().getPosition()+sizeInDel;
				String left = rsf.getSubsequenceAt(getStart().getChr(),startLeft,endLeft).getBaseString();
				
				int startRight = getEnd().getPosition()-sizeInDel;
				int endRight = getEnd().getPosition()+sizeOutDel;
				//safety
				if(endRight>=rsf.getSequence(getStart().getChr()).length()) {
					endRight = rsf.getSequence(getStart().getChr()).length()-1;
				}
				String right = rsf.getSubsequenceAt(getStart().getChr(),startRight,endRight).getBaseString();
				
				InsertionSolverTwoSides is = new InsertionSolverTwoSides(left, right,this.getInsert(),this.getStartEndLocation());
				is.setAdjustedPositionLeft(-sizeInDel);		
				is.setAdjustedPositionRight(-sizeInDel);
				//search both directions
				is.search(true, true);
				//change this?
				is.setMaxTriesSolved(2);
				//set at 5
				is.setMinimumMatch(minSize-1, false);
				is.solveInsertion();
				//this.is = is;
				//now determine if this is random or not
				//one peculiar thing is if the flanks overlap it is not quite fair anymore
				if( endLeft > startRight) {
					//System.out.println(endLeft);
					//System.out.println(startRight);
					int tooLarge = endLeft-startRight;
					//System.out.println(tooLarge);
					//System.out.println(right.length());
					int cut = tooLarge/2;
					right = right.substring(cut);
					left = left.substring(0, left.length()-cut);
				}
				
				RandomInsertionSolverTwoSides ris = new RandomInsertionSolverTwoSides(left,right, this.getInsert());
				flankInsert.setValue(ris.isNonRandomInsert(0.9, is.getLargestMatch()));
				flankInsertLM.setValue(is.getLargestMatchString());
			}
			//this.isFlankInsert = ris.isNonRandomInsert(0.9, is.getLargestMatch());
		}
		metadata.add(flankInsert);
		metadata.add(flankInsertLM);
	}
	private void setHomology(ReferenceSequenceFile rsf) {
		MetaData hom = new MetaData("Homology");
		MetaData homLength = new MetaData("HomologyLength");
		if(type == SVType.DEL || type == SVType.DEL) {
			int delStart = start.getPosition();
			int delEnd = end.getPosition();
			String del = null, left = null, right = null;
			if(this.origCaller.contentEquals("Pindel")){
				del = rsf.getSubsequenceAt(start.getChr(), delStart, delEnd-1).getBaseString();
				left = rsf.getSubsequenceAt(start.getChr(),delStart-50, delStart-1).getBaseString();
				right = rsf.getSubsequenceAt(start.getChr(),delEnd, delEnd+50-1).getBaseString();
			}
			//normal VCF behaviour
			else {
				int delStartCorr = delStart+1;
				del = rsf.getSubsequenceAt(start.getChr(), delStartCorr, delEnd-1).getBaseString();
				left = rsf.getSubsequenceAt(start.getChr(),delStartCorr-50, delStartCorr-1).getBaseString();
				right = rsf.getSubsequenceAt(start.getChr(),delEnd, delEnd+50-1).getBaseString();
			}
			//System.out.println(left);
			//System.out.println(del);
			//System.out.println(right);
			String homology = Utils.getHomology(left, del, right);
			/*
			if(this.getStart().getPosition()> 12045000 && getStart().getPosition()<12046000) {
				System.out.println(start.getPosition());
				System.out.println(end.getPosition());
				System.out.println(left);
				System.out.println(del.length()+" "+del);
				System.out.println(right);
				System.out.println(homology);
				//System.out.println(left.length());
				String total = left+del+right;
				String largeSub = rsf.getSubsequenceAt(start.getChr(),delStart-100, delEnd+100).getBaseString();
				System.out.println(largeSub.contains(total));
				System.out.println(largeSub);
				System.out.println(this);
				//System.exit(0);
			}
			*/
			hom.setValue(homology);
		}
		if(hom.getValue()==null) {
			hom.setValue("");
		}
		homLength.setValue(hom.getValue().length());
		metadata.add(hom);
		metadata.add(homLength);
	}

	public void setInsert(String insert) {
		if(insert != null && insert.length()>0) {
			this.insert = insert.toUpperCase();
			if(this.type == SVType.DEL) {
				type = SVType.DELINS;
			}
			else if(this.type == SVType.TD) {
				type = SVType.TDINS;
			}
		}
	}

	private void checkLeftRight() {
		//also do this for TRANSLOCATIONS
		//if(start.onSameChromosome(end)) {
			if(start.getPosition()>end.getPosition()) {
				Location temp = end;
				this.end = start;
				this.start = temp;
			}
		//}
	}

	public SVType getType() {
		return type;
	}
	public void setType(SVType type) {
		this.type = type;
	}
	public Location getStart() {
		return start;
	}
	public void setStart(Location start) {
		this.start = start;
	}
	public Location getEnd() {
		return end;
	}
	public void setEnd(Location end) {
		this.end = end;
	}
	public String toOneLineString() {
		return toOneLineString(null);
	}
	public String toOneLineString(ArrayList<String> callers) {
		StringBuffer sb = new StringBuffer();
		String sep = "\t";
		sb.append(type).append(sep);
		sb.append(getStartEndLocation()).append(sep);
		sb.append(getStartEndLocation(0.2)).append(sep);
		sb.append(getSize()).append(sep);
		sb.append(getInsert()).append(sep);
		if(metadata!=null) {
			for(MetaData md: metadata) {
				sb.append(md.getValue()).append(sep);
			}
		}
		sb.append(info).append(sep);
		//sb.append(alleles).append(sep);
		if(callers!=null) {
			for(String caller: callers) {
				sb.append(getSupportedSamplesString(caller)).append(sep);
			}
		}
		/*
		if(samples!=null) {
			for(Sample s: samples) {
				sb.append(s);
			}
		}
		*/
		return sb.toString();
	}
	
	private String getSupportedSamplesString(String caller) {
		String ret = "";
		for(Sample s: samples) {
			if(s.isSupported(caller)) {
				if(ret.length()>0) {
					ret+= " ";
				}
				ret+=s.getName();
			}
		}
		return ret;
	}
	private String getStartEndLocation(double d) {
		int distance = start.getDistance(end);
		if(distance <0 ) {
			return getStartEndLocation();
		}
		int startTemp = (int) (start.getPosition()-d*distance);
		int endTemp = (int) (end.getPosition()+d*distance);
		return start.getChr()+":"+startTemp+"-"+endTemp;
			
	}

	public String getInsert() {
		return insert;
	}

	private int getSize() {
		if(start.onSameChromosome(end)) {
			if(this.origCaller.contentEquals("Pindel")) {
				return end.getPosition()-start.getPosition();
			}
			else {
				//if(this.type == SVType.DEL || type == SVType.DELINS) {
				//not so sure if this is correct for all
					return end.getPosition()-start.getPosition()-1;
				//}
				//else {
				//	return end.getPosition()-start.getPosition();
				//}
			}
		}
		return -1;
	}

	public String getStartEndLocation() {
		if(start.onSameChromosome(end)) {
			return start.getChr()+":"+start.getPosition()+"-"+end.getPosition();
		}
		else {
			return start.getChr()+":"+start.getPosition()+"|"+end.getChr()+":"+end.getPosition();
		}
	}

	public String getKey() {
		//for now this is sufficient
		StringBuffer sb = new StringBuffer();
		String sep = "_";
		sb.append(type).append(sep);
		sb.append(getStartEndLocation()).append(sep);
		sb.append(getSize()).append(sep);
		//for not the insert is also part of the key
		sb.append(getInsert());
		return sb.toString();
	}

	public void addSample(Sample s) {
		if(samples==null) {
			samples = new ArrayList<Sample>();
		}
		samples.add(s);
	}
	public int getNrSampleSupport() {
		int support = 0;
		if(samples == null) {
			return 0;
		}
		// TODO Auto-generated method stub
		for(Sample s: samples) {
			if(s.isSupported()) {
				support++;
			}
		}
		return support;
	}
	public void addAllele(List<Allele> alleles) {
		this.alleles = alleles;
	}
	public boolean inNeighbourhood(StructuralVariation sv) {
		if(sv.getStart().isNeighbourHooud(getStart()) && sv.getEnd().isNeighbourHooud(getEnd())) {
			return true;
		}
		return false;
	}
	public  ArrayList<Sample> getSamples() {
		return samples;
	}
	public String getCallers() {
		ArrayList<String> callers = new ArrayList<String>();
		for(Sample s: samples) {
			callers.addAll(s.getCallers());
		}
		String ret = "";
		for(String s: callers) {
			if(ret.length()>0) {
				ret+=" ";
			}
			ret+=s;
		}
		return ret;
	}
	public void merge(StructuralVariation sv) {
		for(Sample s: sv.getSamples()) {
			if(s.isSupported()) {
				Sample temp = findSample(s.getName());
				if(temp == null) {
					this.addSample(s);
				}
				else {
					for(Caller c: s.getCall()) {
						temp.addCall(c);
					}
				}
			}
		}
	}
	private Sample findSample(String name) {
		for(Sample s: samples) {
			if(s.getName().contentEquals(name)) {
				return s;
			}
		}
		return null;
	}
	public int getNrSampleSupport(String caller) {
		int support = 0;
		if(samples == null) {
			return 0;
		}
		// TODO Auto-generated method stub
		for(Sample s: samples) {
			if(s.isSupported(caller)) {
				support++;
			}
		}
		return support;
	}
	public void setCallerString(String totalString) {
		this.info = totalString;
	}
	public String getHeader(ArrayList<String> callers) {
		String head =  "Type\tlocation\tIGVLocation\tSize\tInsert";
		if(metadata != null) {
			for(MetaData md: metadata) {
				head+="\t"+md.getName();
			}
		}
		for(String caller: callers) {
			head+="\t"+caller;
		}
		for(String caller: callers) {
			head+="\tSample"+caller;
		}
		return head;
	}
	public String getOrigCaller() {
		return origCaller;
	}
}

package data;

import java.util.ArrayList;
import java.util.List;

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
	
	public StructuralVariation(SVType type, Location start, Location end) {
		setType(type);
		setStart(start);
		setEnd(end);
		checkLeftRight();
	}
	public void addMetaData(ReferenceSequenceFile rsf) {
		metadata = new ArrayList<MetaData>();
		setHomology(rsf);
	}
	
	private void setHomology(ReferenceSequenceFile rsf) {
		//no yet implemented
		/*
		MetaData hom = new MetaData("homology");
		if(type == SVType.DEL || type == SVType.DEL) {
			int delStart = start.getPosition();
			int delEnd = end.getPosition();
			String del = rsf.getSubsequenceAt(start.getChr(), delStart, delEnd).getBaseString();
			String left = rsf.getSubsequenceAt(start.getChr(),start.getPosition()-50, start.getPosition()).getBaseString();
			String right = rsf.getSubsequenceAt(start.getChr(),end.getPosition(), end.getPosition()+50).getBaseString();
			if(getSize()==1222) {
				System.out.println(del);
				System.out.println(left);
				System.out.println(right);
				System.out.println(this);
				System.exit(0);
			}
		}
		metadata.add(hom);
		*/
	}
	public StructuralVariation(SVType type, Location start, Location end, String insert) {
		this(type, start, end);
		setInsert(insert);
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
	public String toOneLineString(ArrayList<String> callers) {
		StringBuffer sb = new StringBuffer();
		String sep = "\t";
		sb.append(type).append(sep);
		sb.append(getStartEndLocation()).append(sep);
		sb.append(getStartEndLocation(0.2)).append(sep);
		sb.append(getSize()).append(sep);
		sb.append(getInsert()).append(sep);
		sb.append(info).append(sep);
		//sb.append(alleles).append(sep);
		for(String caller: callers) {
			sb.append(getSupportedSamplesString(caller)).append(sep);
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
			return end.getPosition()-start.getPosition()+1;
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
	public static String getHeader(ArrayList<String> callers) {
		String head =  "Type\tlocation\tIGVLocation\tSize\tInsert";
		for(String caller: callers) {
			head+="\t"+caller;
		}
		for(String caller: callers) {
			head+="\tSample"+caller;
		}
		return head;
	}
	
	 
}

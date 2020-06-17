package controller;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import data.G4;
import data.StructuralVariation;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;

public class G4Controller {
	private HashMap<String, Vector<G4>> g4s = new HashMap<String, Vector<G4>>();
	private void insertG4(G4 g4) {
		if(!g4s.containsKey(g4.getChr())) {
			Vector<G4> v = new Vector<G4>();
			g4s.put(g4.getChr(), v);
		}
		Vector<G4> v = g4s.get(g4.getChr());
		v.add(g4);
		//System.out.println(g4.getLocation());
	}
	
	public void fillHash(ReferenceSequenceFile rsf) {
		SAMSequenceDictionary dict = rsf.getSequenceDictionary();
		if(dict == null) {
			System.err.println("DICT is null");
		}
		Pattern g = Pattern.compile("g{3,5}[acgt]{1,5}g{3,5}[acgt]{1,5}g{3,5}[acgt]{1,5}g{3,5}");
		Pattern c = Pattern.compile("c{3,5}[acgt]{1,5}c{3,5}[acgt]{1,5}c{3,5}[acgt]{1,5}c{3,5}");
		for(SAMSequenceRecord ssr: dict.getSequences()) {
			String chr = ssr.getSequenceName();
			ReferenceSequence rs = rsf.getSequence(ssr.getSequenceName());
			Matcher matcher = g.matcher(rs.getBaseString());
			boolean forward = false;
		    while (matcher.find()) {
		    	int start = matcher.start();
		    	int end = matcher.end();
		    	String g4 = matcher.group();
		    	G4 g4class = new G4(g4, chr, start, end, forward);
		    	insertG4(g4class);
		    }
		    forward = true;
		    Matcher matcher2 = c.matcher(rs.getBaseString());
		    while (matcher2.find()) {
		    	int start = matcher2.start();
		    	int end = matcher2.end();
		    	String g4 = matcher2.group();
		    	G4 g4class = new G4(g4, chr, start, end, forward);
		    	insertG4(g4class);
		    }
		}
	}

	public Vector<G4> getOverlaps(StructuralVariation sv) {
		Vector<G4> v= new Vector<G4>();
		String chrStart = sv.getStart().getChr();
		String chrEnd = sv.getEnd().getChr();
		if(chrStart.contentEquals(chrEnd)) {
			Vector<G4> g4Chr = g4s.get(sv.getStart().getChr());
			if(g4Chr!= null) {
				for(G4 g4: g4Chr) {
					if(g4.overlaps(sv)) {
						v.add(g4);
					}
				}
			}
		}
		return v;
	}

	public G4 getNearestG4Left(StructuralVariation sv) {
		G4 nearest = null;
		int minDist = Integer.MAX_VALUE;
		String chrStart = sv.getStart().getChr();
		String chrEnd = sv.getEnd().getChr();
		if(chrStart.contentEquals(chrEnd)) {
			Vector<G4> g4Chr = g4s.get(sv.getStart().getChr());
			if(g4Chr!= null) {
				for(G4 g4: g4Chr) {
					int dis = sv.getStart().getPosition()-g4.getEnd();
					if(dis>0 && dis<minDist) {
						nearest = g4;
						minDist = dis;
					}
				}
			}
		}
		return nearest;
	}

	public G4 getNearestG4Right(StructuralVariation sv) {
		G4 nearest = null;
		int minDist = Integer.MIN_VALUE;
		String chrStart = sv.getStart().getChr();
		String chrEnd = sv.getEnd().getChr();
		if(chrStart.contentEquals(chrEnd)) {
			Vector<G4> g4Chr = g4s.get(sv.getStart().getChr());
			if(g4Chr!= null) {
				for(G4 g4: g4Chr) {
					int dis = sv.getEnd().getPosition()-g4.getStart();
					if(dis<0 && dis>minDist) {
						nearest = g4;
						minDist = dis;
					}
				}
			}
		}
		return nearest;
	}

	public G4 getClosest(StructuralVariation sv) {
		Vector<G4> overlap = this.getOverlaps(sv);
		if(overlap.size()>0) {
			return overlap.get(0);
		}
		G4 left = this.getNearestG4Left(sv);
		G4 right = this.getNearestG4Right(sv);
		if(left == null && right != null) {
			return right;
		}
		if(right == null && left != null) {
			return left;
		}
		if(left!= null && right!=null) {
			int distL = sv.getStart().getPosition()-left.getEnd();
			int distR = right.getStart()-sv.getEnd().getPosition();
			if(distL<distR) {
				return left;
			}
			else {
				return right;
			} 
		}
		return null;
	}
	
	public int getDistanceClosest(StructuralVariation sv) {
		Vector<G4> overlap = this.getOverlaps(sv);
		if(overlap.size()>0) {
			//overlap == 0
			return 0;
		}
		G4 left = this.getNearestG4Left(sv);
		G4 right = this.getNearestG4Right(sv);
		int distL, distR;
		if(left == null && right != null) {
			if(right.isForward()) {
				distR = right.getStart()-sv.getEnd().getPosition();
			}
			else {
				distR = right.getEnd()-sv.getEnd().getPosition();
			}
			return distR;
		}
		if(right == null && left != null) {
			if(left.isForward()) {
				distL = sv.getStart().getPosition()-left.getStart();
			}
			else {
				distL = sv.getStart().getPosition()-left.getEnd();
			}
			return distL;
		}
		if(left!= null && right!=null) {
			if(left.isForward()) {
				distL = sv.getStart().getPosition()-left.getStart();
			}
			else {
				distL = sv.getStart().getPosition()-left.getEnd();
			}
			if(right.isForward()) {
				distR = right.getStart()-sv.getEnd().getPosition();
			}
			else {
				distR = right.getEnd()-sv.getEnd().getPosition();
			}
			if(distL<distR) {
				//System.out.println("bothLeft");
				return distL;
			}
			else {
				//System.out.println("bothRight");
				return distR;
			} 
		}
		return -1;
	}
	
}

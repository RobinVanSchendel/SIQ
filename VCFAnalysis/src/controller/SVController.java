package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

import data.G4;
import data.Sample;
import data.StructuralVariation;
import htsjdk.samtools.reference.ReferenceSequenceFile;

public class SVController {
	private HashMap<String, StructuralVariation> svs = new HashMap<String, StructuralVariation>();
	private ReferenceSequenceFile rsf;
	private ArrayList<String> callers = new ArrayList<String>();
	private boolean checkForLocations = false;
	private int maxSupportingSamples;
	private HashMap<String, String> names = new HashMap<String, String>();
	private int debugLocation = -1;
	private G4Controller g4s;
	private Vector<String> unknownNames = new Vector<String>();
	
	public SVController(ReferenceSequenceFile rsf, int maxSupportingSamples) {
		this.rsf = rsf;
		this.maxSupportingSamples = maxSupportingSamples;
	}
	//only add SVs if they are not yet in
	public void addSV(StructuralVariation sv) {
		if(debugLocation >0 && sv.getStart().getPosition()>(debugLocation-1000) && sv.getStart().getPosition()<(debugLocation+1000)) {
			System.out.println("DEBUG");
			System.out.println(sv.toOneLineString());
			System.out.println("Supports "+sv.getNrSampleSupport());
			System.out.println("Supports "+sv.getSupportingSamples());
			System.out.println("END DEBUG");
		}
		//check if not too many support
		int support = sv.getNrSampleSupport();
		if(support==0 || sv.getNrSampleSupport()>maxSupportingSamples) {
			return;
		}
		
		//change names if needed
		for(Sample s: sv.getSamples()) {
			s.setName(lookupName(s.getName()));
		}
		
		if(!callers.contains(sv.getOrigCaller())) {
			callers.add(sv.getOrigCaller());
		}
		if(callers.size()>1) {
			checkForLocations = true;
		}
		//can also be a different caller
		if(svs.containsKey(sv.getKey())) {
			StructuralVariation svTemp = svs.get(sv.getKey());
			//only stop if the callers match
			if(svTemp.getCallers().contentEquals(sv.getOrigCaller())) {
				return;
			}
		}
		boolean inserted = insertSV(sv);
		//if(sv.getStartEndLocation().contentEquals("CHROMOSOME_II:967255-1050920")) {
			//System.out.println("hier "+inserted);
			//System.exit(0);
		//}
	}
	private String getHeader() {
		for(String key: svs.keySet()) {
			StructuralVariation sv = svs.get(key);
			return sv.getHeader(callers);
		}
		return null;
	}
	private boolean insertSV(StructuralVariation sv) {
		/*
		if(sv.getStart().getPosition()>(7758914-100) && sv.getStart().getPosition()<(7758914+100)) {
			System.out.println(sv.toOneLineString());
		}
		*/
		if(!checkForLocations) {
			//System.out.println("Adding");
			svs.put(sv.getKey(), sv);
			if(isDebugEvent(sv)) {
				System.out.println("DEBUG insertSV");
				System.out.println("Insert without checking");
				System.out.println("END DEBUG");
			}
			return true;
		}
		else {
			//improve this search later on
			//System.out.println("Checking");
			boolean added = false;
			ArrayList<StructuralVariation> closest  = new ArrayList<StructuralVariation>();
			for(String key: svs.keySet()) {
				StructuralVariation svTemp = svs.get(key);
				if(svTemp.inNeighbourhood(sv) && !svTemp.getCallers().contains(sv.getOrigCaller())) {
					closest.add(svTemp);
				}
			}
			if(isDebugEvent(sv)) {
				System.out.println("DEBUG insertSV");
				System.out.println(sv.getSupportingSamples());
				System.out.println("Insert found matches "+closest.size());
				for(StructuralVariation tempSV: closest) {
					System.out.println(tempSV.toOneLineString());
					System.out.println(tempSV.getSupportingSamples());
				}
				System.out.println("END DEBUG");
			}
			if(closest.size()>0) {
				if(closest.size()==1) {
					closest.get(0).merge(sv);
					added = true;
					return false;
				}
				else {
					//check for the same supporting sample
					ArrayList<StructuralVariation> nameSame = new ArrayList<StructuralVariation>();
					for(StructuralVariation close : closest) {
						if(close.getSupportingSamples().equals(sv.getSupportingSamples())) {
							nameSame.add(close);
						}
					}
					if(nameSame.size()==1) {
						nameSame.get(0).merge(sv);
						added = true;
						if(isDebugEvent(sv)) {
							System.out.println("DEBUG insertSV");
							System.out.println("merged on name");
							System.out.println("END DEBUG");
						}
					}
					//same type
					else {
						//pick the same type
						ArrayList<StructuralVariation> typeSame = new ArrayList<StructuralVariation>();
						for(StructuralVariation close : closest) {
							if(close.getType().equals(sv.getType())) {
								typeSame.add(close);
							}
							//System.out.println(close.toOneLineString());
						}
						if(typeSame.size()==1) {
							typeSame.get(0).merge(sv);
							added = true;
							if(isDebugEvent(sv)) {
								System.out.println("DEBUG insertSV");
								System.out.println("merged on type");
								System.out.println("END DEBUG");
							}
						}
						//now don't know what to do yet
						else {
							//System.out.println("====");
							//System.out.println(sv.toOneLineString());
							//System.out.println("======");
							//System.exit(0);
						}
					}
				}
			}
			if(!added) {
				svs.put(sv.getKey(), sv);
				return true;
			}
		}
		return false;
	}
	public int countEvents() {
		return svs.size();
	}
	public void addMetaData() {
		System.out.println("Adding metadata "+svs.size() );
		int counter = 0;
		for(String key: svs.keySet()) {
			svs.get(key).addMetaData(rsf, g4s);
			if(counter%1000==0) {
				System.out.println("Already processed "+counter+ " keys");
			}
			counter++;
		}
		System.out.println("Done adding metadata");
		//add vicinityInfo
		for(String key: svs.keySet()) {
			StructuralVariation sv = svs.get(key);
			for(String key2: svs.keySet()) {
				if(!key.contentEquals(key2)) {
					StructuralVariation sv2 = svs.get(key2);
					if(sv.inNeighbourhood(sv2)) {
						sv.setInNeighbourhood(true);
						sv.setInNeighbourhood(sv2.getStartEndLocation());
						sv2.setInNeighbourhood(true);
						sv2.setInNeighbourhood(sv.getStartEndLocation());
						/*
						if(sv.getStartEndLocation().contentEquals(sv2.getStartEndLocation())) {
							System.out.println(sv.toOneLineString());
							System.out.println(sv2.toOneLineString());
							System.out.println("=====");
						}
						*/
					}
				}
			}
		}
	}
	
	
	public void printSVs(int maxSupportingFiles) {
		System.out.println(getHeader());
		int counter = 0;
		for(String key: svs.keySet()) {
			StructuralVariation sv = svs.get(key);
			
			//if(sv.getStartEndLocation().contentEquals("CHROMOSOME_V:11138746-11138835")) {
				//System.out.println("Filter");
				//System.out.println("Filter "+sv.getNrSampleSupport());
			//}
			
			int total = 0;
			int max = 0;
			String totalString = "";
			for(String caller: callers) {
				int nrFileSupports = sv.getNrSampleSupport(caller);
				if(nrFileSupports>max) {
					max = nrFileSupports;
				}
				total += nrFileSupports;
				if(totalString.length()>0) {
					totalString+="\t";
				}
				totalString+=nrFileSupports;
			}
			
			if(total>0 && max<=maxSupportingFiles) {
				sv.setCallerString(totalString+"\t"+total);
				System.out.println(sv.toOneLineString(callers));
				counter++;
				//System.out.println(sv.getNrFileSupport());
			}
		}
		System.out.println("Printed "+counter+" events");
	}
	public void printSVs() {
		System.out.println(getHeader());
		for(String key: svs.keySet()) {
			StructuralVariation sv = svs.get(key);
			System.out.println(sv);
		}
	}
	public void addLookupNames(String string) throws FileNotFoundException {
		Scanner s = new Scanner(new File(string));
		while(s.hasNextLine()) {
			String line = s.nextLine();
			String[] parts = line.split("\t");
			names.put(parts[0],parts[1]);
		}
		s.close();
	}
	public String lookupName(String name) {
		if(names.containsKey(name)) {
			return names.get(name);
		}
		else {
			if(!unknownNames.contains(name)) {
				System.err.println(name);
				unknownNames.add(name);
			}
		}
		return name;
	}
	public void setDebugLocation(int debugLocation) {
		this.debugLocation = debugLocation;
		
	}
	public void printLocations(File locs) {
		try {
			Scanner s = new Scanner(locs);
			System.out.println(getHeader());
			while(s.hasNextLine()) {
				String line = s.nextLine();
				for(String key: svs.keySet()) {
					StructuralVariation sv = svs.get(key);
					if(sv.getStartEndLocation().contentEquals(line)) {
						String totalString = "";
						int total = 0;
						for(String caller: callers) {
							int nrFileSupports = sv.getNrSampleSupport(caller);
							if(totalString.length()>0) {
								totalString+="\t";
							}
							totalString+=nrFileSupports;
							total += nrFileSupports;
						}
						sv.setCallerString(totalString+"\t"+total);
						System.out.println(sv.toOneLineString(callers));
					}
				}
			}
			s.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void setG4Controller(G4Controller g4s) {
		this.g4s = g4s;
	}
	public boolean isDebugEvent(StructuralVariation sv) {
		if(debugLocation >0 && sv.getStart().getPosition()>(debugLocation-1000) && sv.getStart().getPosition()<(debugLocation+1000)) {
			return true;
		}
		return false;
	}
}

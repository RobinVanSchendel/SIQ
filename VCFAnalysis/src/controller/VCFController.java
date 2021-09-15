package controller;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.vcf.VCFFileReader;

public class VCFController {
	HashMap<String,VariantContext> vcfs = new HashMap<String,VariantContext>();
	private HashMap<String, Integer> generations;
	private HashMap<String, String> wormDB;
	private PindelController pc;
	private String[] blacklist = {"XF1319-BEG58"};
	
	public void addFile(File f) {
		if(f.exists() && f.getName().endsWith(".vcf")) {
			//System.out.println(f.getName());
			VCFFileReader reader = new VCFFileReader(f, false);
	        CloseableIterator<VariantContext> it = reader.iterator();
	        int count = 0;
	        int SNVcount = 0;
	        String groupName = f.getName().replace(".vcf", "");
	        while(it.hasNext()) {
	        	VariantContext vc = it.next();
	        	if(vc.isSNP()) {
	        		String locMutation = getLocMutation(vc);
	        		VariantContext vcTemp = vcfs.get(locMutation);
	        		if(vcTemp!=null) {
	        			VariantContextBuilder vcb = new VariantContextBuilder(vcTemp);
	        			ArrayList<Genotype> g = new ArrayList<Genotype>();
	        			g.addAll(vcTemp.getGenotypes());
	        			for(Genotype gc : vc.getGenotypes()) {
	        				GenotypeBuilder gb = new GenotypeBuilder(gc);
	        				gb.attribute("group", groupName);
	        				g.add(gb.make());
	        			}
	        			vcb.genotypes(g);
	        			VariantContext vcHash = vcb.make();
	        			vcfs.put(locMutation,vcHash);
	        		}
	        		else {
	        			VariantContextBuilder vcb = new VariantContextBuilder(vc);
	        			ArrayList<Genotype> g = new ArrayList<Genotype>();
	        			for(Genotype gc : vc.getGenotypes()) {
	        				GenotypeBuilder gb = new GenotypeBuilder(gc);
	        				gb.attribute("group", groupName);
	        				g.add(gb.make());
	        			}
	        			vcb.genotypes(g);
	        			vcfs.put(locMutation,vcb.make());
	        		}
	        		SNVcount++;
	        	}
	        }
	        reader.close();
		}
		
	}
	private String getLocMutation(VariantContext vc) {
		String chr = vc.getContig();
		int pos = vc.getStart();
		String alt = vc.getAlleles().toString();
		return chr+":"+pos+"_"+alt;
	}
	public void printVCFs() {
		int count = 0;
		System.out.println(getHeader());
		for(String key: vcfs.keySet()) {
			StringBuffer out = new StringBuffer();
			VariantContext v = vcfs.get(key);
			String location = v.getContig();
			int pos = v.getStart();
			ArrayList<String> groups = getGroups(v);
			String ref = v.getReference().getBaseString();
			String alt = getAltAllele(v);
			//ArrayList<String> supSamples = getHomVarSupportingSamples(v);
			//ArrayList<String> supSamples = getHomVarSingleSupportingSample(v);
			ArrayList<String> supSamples = getSingleSupportingSample(v);
			String locIGV = location+":"+pos;
			if(supSamples!=null && supSamples.size()==1 && groups.size()==1) {
				//get the distance to a CNV
				int distance = pc.getDistance(supSamples.get(0), locIGV);
				if(distance<10) {
					continue;
				}
				if(isBlacklisted(supSamples.get(0))) {
					continue;
				}
				//ID
				out.append(locIGV+"_"+supSamples.get(0)).append("\t");
				//LocationID
				out.append(locIGV+"_"+supSamples.get(0)).append("\t");
				out.append(String.join(":",supSamples)).append("\t");
				int gens = getGenerations(supSamples.get(0));
				out.append(gens).append("\t");
				String genotype = getGroupName(groups.get(0).toUpperCase());
				out.append(groups.get(0)).append("\t");
				out.append(genotype).append("\t");
				out.append("SNV").append("\t");
				//size
				out.append(1).append("\t");
				out.append(location).append("\t");
				out.append(pos).append("\t");
				out.append(v.getEnd()).append("\t");
				out.append(getMutation(ref,alt)).append("\t");
				
				//needed for plotter
				out.append("true\t");
				out.append("TRUE\t");
				out.append("VERYLIKELY\t");
				out.append("UNKNOWN\t");
				out.append(isHom(v,supSamples.get(0))).append("\t");
				out.append(location+":"+pos);
				//out.append(v.getGenotype(supSamples.get(0)).isPhased()).append("\t");
				//out.append(locIGV);
				/*
				
				
				out.append(location).append("\t");
				out.append(pos).append("\t");
				out.append(locIGV).append("\t");
				out.append(ref).append("\t");
				out.append(alt).append("\t");
				out.append(getMutation(ref,alt)).append("\t");
				out.append(supSamples.size()).append("\t");
				out.append(String.join(":",supSamples)).append("\t");
				
				if(supSamples.size()==1) {
					double gens = getGenerations(supSamples.get(0));
					out.append(1/gens).append("\t");
				}
				else {
					out.append(-1).append("\t");
				}
				out.append(groups.size()).append("\t");
				out.append(String.join(":",groups)).append("\t");
				if(groups.size()==1) {
					String groupName = getGroupName(groups.get(0));
					out.append(groupName).append("\t");
				}
				else {
					out.append("").append("\t");
				}
				//distance to pindelEvent
				out.append(distance).append("\t");
				if(distance<=10) {
					out.append(true).append("\t");
				}
				*/
				count++;
				System.out.println(out.toString());
			}
		}
		System.out.println("Printed "+count+" SNVs");
	}
	private ArrayList<String> getSingleSupportingSample(VariantContext v) {
		ArrayList<String> hom = this.getHomVarSingleSupportingSample(v);
		if(hom!=null && hom.size()==1) {
			return hom;
		}
		ArrayList<String> al = new ArrayList<String>();
		int countWT = 0;
		boolean has0Generation = false;
		boolean generation0IsWT = false;
		int nr = v.getGenotypes().size();
		for(Genotype g: v.getGenotypes()) {
			int generation = this.getGenerations(g.getSampleName());
			if(generation == 0){
				has0Generation = true;
			}
			//System.out.println(g.getSampleName());
			if(g.getGQ()>=40 && g.getDP()>=8 && g.isHomRef()) {
				countWT++;
				if(generation==0) {
					generation0IsWT = true;
				}
			}
			if(g.getGQ()>=20 && g.getDP()>=8 && g.isHet()) {
				int[] ad = g.getAD();
				double percHet = ad[1]/(double)(ad[0]+ad[1]);
				if(percHet>=0.4) {
					al.add(g.getSampleName());
				}
				//System.out.println(ad.length);
				//System.out.println(percHet);
			}
		}
		//all should be WT, except one
		if(al.size()==1 && countWT==nr-1) {
			//System.out.println(v.getGenotype(al.get(0)));
			//only call if the 0 generation is WT
			if(has0Generation && generation0IsWT) {
				return al;
			}
			else if(!has0Generation) {
				return al;
			}
		}
		return null;
	}
	private String isHom(VariantContext v, String string) {
		Genotype g = v.getGenotype(string);
		if(g.isHomVar()) {
			return "HOM";
		}
		else if(g.isHet()) {
			return "HET";
		}
		return null;
	}
	private ArrayList<String> getHomVarSingleSupportingSample(VariantContext v) {
		ArrayList<String> al = new ArrayList<String>();
		int countWT = 0;
		boolean has0Generation = false;
		boolean generation0IsWT = false;
		for(Genotype g: v.getGenotypes()) {
			int generation = this.getGenerations(g.getSampleName());
			if(generation == 0){
				has0Generation = true;
			}
			//System.out.println(g.getSampleName());
			if(g.getGQ()>=40 && g.getDP()>=8 && g.isHomRef()) {
				countWT++;
				if(generation==0) {
					generation0IsWT = true;
				}
			}
			if(g.getGQ()>=20 && g.getDP()>=5 && g.isHomVar()) {
				al.add(g.getSampleName());
			}
		}
		if(al.size()==1 && countWT>0) {
			//only call if the 0 generation is WT
			if(has0Generation && generation0IsWT) {
				return al;
			}
			else if(!has0Generation) {
				return al;
			}
		}
		return null;
	}
	private boolean isBlacklisted(String check) {
		for(String s: blacklist) {
			if(s.contentEquals(check)) {
				return true;
			}
		}
		return false;
	}
	private String getHeader() {
		StringBuffer sb = new StringBuffer();
		sb.append("ID\t");
		sb.append("LocationID\t");
		sb.append("Sample\t");
		sb.append("Generation\t");
		sb.append("SampleGroup\t");
		sb.append("Genotype\t");
		sb.append("SVType\t");
		sb.append("Size\t");
		sb.append("Chr\t");
		sb.append("Start\t");
		sb.append("End\t");
		sb.append("Mutation\t");
		sb.append("Unique\t");
		sb.append("Curated\t");
		sb.append("LikelyHoodOfEvent\t");
		sb.append("Tract\t");
		sb.append("HOM_HET\t");
		sb.append("IGV");
		return sb.toString();
	}
	private String getGroupName(String string) {
		if(wormDB.containsKey(string)) {
			return wormDB.get(string);
		}
		return null;
	}
	private int getGenerations(String string) {
		if(generations.containsKey(string)) {
			return generations.get(string);
		}
		String[] parts = string.split("-");
		String nr = "";
		if(parts.length>=2) {
			for(char c: parts[1].toCharArray()) {
				if(Character.isDigit(c)) {
					nr+=c;
				}
			}
			if(nr.length()>0) {
				return Integer.parseInt(nr);
			}
		}
		return -1;
	}
	private String getAltAllele(VariantContext v) {
		String alt = v.getAltAlleleWithHighestAlleleCount().getBaseString();
		if(!alt.contentEquals("*")) {
			return alt;
		}
		int maxCount = 0;
		Allele maxAllele = null;
		for(Allele a: v.getAlternateAlleles()) {
			if(v.getCalledChrCount(a)>=maxCount && !a.getBaseString().contentEquals("*")) {
				maxCount = v.getCalledChrCount(a);
				maxAllele = a;
			}
		}
		return maxAllele.getBaseString();
	}
	private ArrayList<String> getHomVarSupportingSamples(VariantContext v) {
		ArrayList<String> al = new ArrayList<String>();
		String location = v.getContig()+":"+v.getStart();
		StringBuffer sb = new StringBuffer(v.getContig()+":"+v.getStart()+"\t");
		int countWT = 0;
		for(Genotype g: v.getGenotypes()) {
			if(g.getGQ()>=40 && g.getDP()>=8 && g.isHomRef()) {
				countWT++;
			}
			if(g.getGQ()>=40 && g.getDP()>=8 && g.isHomVar()) {
				al.add(g.getSampleName());
			}
			else if(g.getGQ()>=20 && g.getDP()>=4 && g.isHomVar()) {
				//System.out.println(g.getSampleName());
			}
			sb.append(g.getSampleName()+"\t"+g.getGQ()+"\t"+g.getDP()+"\t"+g.isHomVar()+"\t");
		}
		System.out.println(sb.toString()+"\n"+countWT);
		
		if(location.contentEquals("CHROMOSOME_IV:8579897")) {
			System.out.println(v.toStringWithoutGenotypes());
			for(Genotype g: v.getGenotypes()) {
				System.out.println(g.toString()+" "+g.isPhased());
			}
			System.exit(0);
		}
		return al;
	}
	private ArrayList<String> getGroups(VariantContext v) {
		ArrayList<String> al = new ArrayList<String>();
		for(Genotype g: v.getGenotypes()) {
			String group = (String) g.getAnyAttribute("group");
			if(!al.contains(group)) {
				al.add(group);
			}
		}
		return al;
	}
	public static String getMutation(String ref, String alt){
		ref = ref.toUpperCase();
		alt = alt.toUpperCase();
		if((ref.equals("A") && alt.equals("G")) || (ref.equals("T") && alt.equals("C"))){
			return "AT->GC";
		}
		else if((ref.equals("G") && alt.equals("A")) || (ref.equals("C") && alt.equals("T"))){
			return "GC->AT";
		}
		else if((ref.equals("A") && alt.equals("C")) || (ref.equals("T") && alt.equals("G"))){
			return "AT->CG";
		}
		else if((ref.equals("G") && alt.equals("T")) || (ref.equals("C") && alt.equals("A"))){
			return "GC->TA";
		}
		else if((ref.equals("A") && alt.equals("T")) || (ref.equals("T") && alt.equals("A"))){
			return "AT->TA";
		}
		else if((ref.equals("G") && alt.equals("C")) || (ref.equals("C") && alt.equals("G"))){
			return "GC->CG";
		}
		return ref+"->"+alt;
	}
	public void setGenerations(HashMap<String, Integer> generations) {
		this.generations = generations;
	}
	public void setWormDB(HashMap<String, String> wormDB) {
		this.wormDB = wormDB;
	}
	public void setPindelController(PindelController pc) {
		this.pc = pc;
	}

}

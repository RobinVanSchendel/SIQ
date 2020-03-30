import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeType;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder.OutputType;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import controller.PindelController;
import controller.SVController;
import data.GeneralCaller;
import data.GridssCaller;
import data.Location;
import data.Sample;
import data.StructuralVariation;
import data.StructuralVariation.SVType;
import data.Utils;

public class GridssCall extends GeneralCaller {

	private static final int MINQG = 50;
	private File vcf;
	
	public GridssCall(File vcf) {
		this.vcf = vcf;
	}
	public void parseFile(SVController svc) {
		VCFFileReader reader = new VCFFileReader(vcf, false);
        CloseableIterator<VariantContext> it = reader.iterator();
        while(it.hasNext()){
        	VariantContext vc = it.next();
        	StructuralVariation sv = this.parseStructuralVariation(vc);
        	if(sv!=null) {
        		svc.addSV(sv, getName());
        		//add call details here
        	}
        }
        reader.close();
	}
	

	public static void test(String[] args) throws IOException{
		//File vcf = new File("E:/Project_Genome_Scan_Brd_Rtel_Brc_RB873/multisample.final.vcf");
		//File vcf = new File("E:/Project_TLS/20150928_combinevariants.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Hartwig\\LUMC-001-004\\Temp\\vcf\\mergeGVCF_output_filtered.g.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\MA lines - BRC-1 POLQ-1 analysis\\20190313_gvcf_brc-1_project.genotyped.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Next Sequence Run\\Analysis\\20190920_gvcf_LUMC-003-001_worms_filtered.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Next Sequence Run\\Analysis\\20190920_gvcf_LUMC-003-001_arab_filtered.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\MA lines - BRC-1 POLQ-1 analysis\\createAndCombineGVCF_Project_Juul.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Next Sequence Run\\Analysis\\createAndCombineGVCF_Project_Primase.vcf");
		//File vcf = new File("E:\\temp\\createAndCombineGVCF_Project_4TLS.vcf");
		File genomeFile = new File("E:\\genomes\\caenorhabditis_elegans\\c_elegans.WS235.genomic.fa");
		File vcf = new File("E:\\temp\\gridss.vcf");
		ReferenceSequenceFile rsf = ReferenceSequenceFileFactory.getReferenceSequenceFile(genomeFile);
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\MMP\\Gridss\\gridss.vcf");
		GridssCall gc = new GridssCall(vcf);
		SVController svc = new SVController(rsf);
		gc.parseFile(svc);
		svc.addMetaData();
		int maxSupportingFiles = 2;
		svc.printSVs(maxSupportingFiles);
		System.exit(0);
		
		//cross check locations with SVs
		File pindel = null; //new File("Z:\\Robin\\Project_Primase\\Paper\\project_primase_pindel.txt");
		Scanner scan = new Scanner(new File("printToVCF.txt"));
		ArrayList<String> locs = new ArrayList<String>();
		while(scan.hasNext()) {
			String line = scan.nextLine();
			locs.add(line);
		}
		//File pindel = null;
		PindelController pc = new PindelController(pindel);
		
		
		
		boolean excludeINVandTD = true;
		
		pc.parsePindel(excludeINVandTD);
		
		
		VariantContextWriterBuilder vcWriterBuilder = new VariantContextWriterBuilder().clearOptions()
				.setOutputFile(new File("test.txt")).setOutputFileType(OutputType.VCF);
		VariantContextWriter vcw = vcWriterBuilder.build();
		
		
		
		File outputAll = new File(vcf.getAbsolutePath()+"_all_snvs.txt");
		File outputUnique = new File(vcf.getAbsolutePath()+"_unique_snvs.txt");
		File outputNonUnique = new File(vcf.getAbsolutePath()+"_diffFromZero_snvs.txt");
		BufferedWriter outputAllWriter = new BufferedWriter(new FileWriter(outputAll));;
		BufferedWriter outputUniqueWriter = new BufferedWriter(new FileWriter(outputUnique));;
		BufferedWriter outputDiffFromZeroWriter = new BufferedWriter(new FileWriter(outputNonUnique));;
		
		VCFFileReader reader = new VCFFileReader(vcf, false);
        VCFHeader header = reader.getFileHeader();
        vcw.setHeader(header);
        ArrayList<String> al = header.getSampleNamesInOrder();
        HashMap<String,ArrayList<String>> strains = getStrains(al);
        
        StringBuffer sb = new StringBuffer("Location");
        for(String s: al){
        	sb.append("\t");
        	sb.append(s);
        }
        System.out.println(sb.toString());
        outputAllWriter.write(sb.toString()+"\n");
        String uniqueHeader = "Genotype\tChr\tPos\tLocation\tSample\tRef\tAlt\tMutation\tGQ(Phred-based quality)\tDP\tnrHomCallInSet\tnonhomRefCall\tnoCall\tphased\tnonhomRefCallStrains\tRemark\tDistanceToPindelEvent";
        System.out.println(uniqueHeader);
        outputUniqueWriter.write(uniqueHeader+"\n");
        outputDiffFromZeroWriter.write(uniqueHeader+"\n");
        CloseableIterator<VariantContext> it = reader.iterator();
        int uniqueCounter = 0;
        while(it.hasNext()){
        	sb = new StringBuffer();
        	VariantContext vc = it.next();
        	String locationS = vc.getContig()+":"+vc.getStart();
        	/*
        	if(vc.getStart()==768301) {
        		System.out.println(vc.toStringWithoutGenotypes());
        		System.out.println(vc.isSimpleDeletion());
        		System.out.println(vc.isSNP());
        		System.out.println(vc.getType());
        		for(Genotype gt: vc.getGenotypes()) {
        			System.out.println(gt.toString());
        		}
        		System.out.println(locationS);
        		System.out.println(vc.getAlleles());
        		System.exit(0);
        	}
        	*/
        	
        	//System.out.println(key);
    		int nrCalls = 0;
        	int noOtherCall = 0;
        	boolean unique = false;
        	Genotype last = null;
        	double assrValue = -1;
        	List<Genotype> called = new ArrayList<Genotype>();
        	for(String key: strains.keySet()) {
        		for(String sample: strains.get(key)) {
        			Genotype gt = vc.getGenotype(sample);
    				String rpString = (String)gt.getExtendedAttribute("VF");
    				//System.out.println(rpString);
    				double rp = Double.parseDouble(rpString);
    				if(rp>5) {
    					//if(gt.getExtendedAttribute("RP"));
    					//System.out.println(gt);
    					nrCalls++;
    					last = gt;
    					assrValue = rp;
    					called.add(gt);
    				}
        		}
        	}
        	Allele one = vc.getAlleles().get(1);
        	/*
        	System.out.println(vc.getType());
        	System.out.println(vc.toString());
        	System.out.println("===");
        	System.out.println(vc.getAlleles());
        	System.out.println("===");
        	System.out.println(one);
        	*/
    		String chrEnd = retrieveChr(one);
    		int endLocation = retrieveLoc(one);
    		StructuralVariation.SVType type = retrieveType(one);
    		int size = -1;
    		if(chrEnd != null) {
    			Location start = new Location(vc.getContig(),vc.getStart());
    			Location end = new Location(chrEnd,endLocation);
    			StructuralVariation sv = new StructuralVariation(type, start, end);
    			svc.addSV(sv,"GRIDSS");
	    		if(chrEnd.contentEquals(vc.getContig())) {
	    			size = endLocation-vc.getStart();
	    		}
	    		String locationE = chrEnd+":"+endLocation;
	        	if(nrCalls == 1) {
	        		//System.out.println(vc.toStringWithoutGenotypes());
	        		String names = getSampleNames(called);
	        		String values = getVFValues(called);
	    			System.out.println(nrCalls+"\t"+locationS+"\t"+locationE+"\t"+size+"\t"+assrValue+"\t"+names+"\t"+vc.getAlleles()+"\t"+values+"\t"+sv.toString());
	    			//for(Allele a: vc.getAlleles()) {
	    				//System.out.println(a.toString());
	    				//System.out.println(a.getDisplayString());
	    			//}
	    			//System.out.println(last);
	    		}
    		}
    		else if(!vc.getFilters().toString().contains("LOW_QUAL") && called.size()==1){
    			System.out.println("====");
    			System.out.println(vc.getFilters());
    			System.out.println(vc.toStringWithoutGenotypes());
    			for(Genotype gt: vc.getGenotypes()) {
    				System.out.println(gt.toString());
    			}
    			//System.out.println(vc.toStringDecodeGenotypes());
    			System.out.println("====");
    		}
        }
        vcw.close();
        reader.close();
        outputAllWriter.close();
        outputUniqueWriter.close();
        outputDiffFromZeroWriter.close();
        
        svc.printSVs();
        
        System.out.println("written ALL to "+outputAll.getAbsolutePath());
        System.out.println("written "+uniqueCounter+" UNIQUE to "+outputUnique.getAbsolutePath());
        System.out.println("written outputDiffFromZeroWriter to "+outputNonUnique.getAbsolutePath());
        
        
	}
	private static SVType retrieveType(Allele one) {
		// TODO Auto-generated method stub
		return null;
	}

	private static String getVFValues(List<Genotype> called) {
		StringBuffer names = new StringBuffer();
		String sep = " ";
		for(Genotype gt: called) {
			if(names.length()>0) {
				names.append(sep);
			}
			String rpString = (String)gt.getExtendedAttribute("VF");
			names.append(rpString);
		}
		return names.toString();
	}

	private static String getSampleNames(List<Genotype> called) {
		StringBuffer names = new StringBuffer();
		String sep = " ";
		for(Genotype gt: called) {
			if(names.length()>0) {
				names.append(sep);
			}
			names.append(gt.getSampleName());
		}
		return names.toString();
	}

	private static int retrieveLoc(Allele one) {
		//System.out.println(one.getBaseString());
		//System.out.println(one.toString());
		String str = one.toString();
		String[] parts = str.split("[\\[\\]:]");
		//System.out.println(String.join("\t", parts));
		boolean next = false;
		for(String part: parts) {
			if(next) {
				return Integer.parseInt(part);
			}
			if(part.contains("CHROM")) {
				next = true;
			}
		}
		return -1;
	}

	private static String retrieveChr(Allele one) {
		//System.out.println(one.getBaseString());
		//System.out.println(one.toString());
		String str = one.toString();
		String[] parts = str.split("[\\[\\]:]");
		//System.out.println(String.join("\t", parts));
		for(String part: parts) {
			if(part.contains("CHROM")) {
				return part;
			}
		}
		return null;
	}

	private static boolean isHet(Genotype gt) {
		int[] numbers = gt.getAD();
		int minimum = 5;
		if(numbers.length==2) {
			if(numbers[0]>minimum && numbers[1]>minimum) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsSampleName(HashMap<String, ArrayList<String>> strains, String sampleName) {
		for(String key: strains.keySet()) {
			ArrayList<String> list = strains.get(key);
			for(String name: list) {
				if(name.equals(sampleName)) {
					return true;
				}
			}
		}
		return false;
	}

	private static HashMap<String,ArrayList<String>> getStrains(ArrayList<String> al) {
		HashMap<String,ArrayList<String>> strainMap = new HashMap<String,ArrayList<String>>(); 
		for(String s: al) {
			String strain = getStrain(s);
			if(!strainMap.containsKey(strain)) {
				strainMap.put(strain,new ArrayList<String>());
			}
			//HACK!
			if(!s.contains("gfpminus")) {
				strainMap.get(strain).add(s);
			}
		}
		return strainMap;
	}

	private static String getStrain(String s) {
		//should be char, char, digits
		String part = s.substring(0, 2);
		int index = 2;
		while(index<s.length() && Character.isDigit(s.charAt(index))) {
			part+=s.charAt(index);
			index++;
		}
		//different way
		if(part.length()==2) {
			if(s.contains("-")) {
				String[] parts = s.split("-");
				//System.out.println("or "+parts[0]);
				return parts[0];
			}
			else {
				//N2 hack
				if(part.contentEquals("N2")) {
					//System.out.println("N2");
					return "N2";
				}
			}
		}
		//MMP hack
		if(part.contains("VC")) {
			return "VC";
		}
		//System.out.println(part);
		return part;
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

	@Override
	public StructuralVariation parseStructuralVariation(VariantContext vc) {
		//already remove SVs with low quality
		if(vc.getFilters().contains("LOW_QUAL")) {
			return null;
		}
		
		Location start = new Location(vc.getContig(),vc.getStart());
		Allele high = vc.getAltAlleleWithHighestAlleleCount();
		Location end = parseEnd(high);
		if(end != null) {
			//System.out.println("===");
			//System.out.println(end);
			//System.out.println("===");
			//get rid of the ones that are a double call
			if(start.getPosition()>end.getPosition()) {
				return null;
			}
			
			String typeSigns = obtainTypeSigns(high);
			String insertReplacement = typeSigns.charAt(0)+end.toString()+typeSigns.charAt(1);
			boolean altLeft = high.toString().indexOf(insertReplacement)==0;
			String insert = high.toString().replace(insertReplacement, "");
			//System.out.println("==");
			//System.out.println(insert);
			insert = obtainInsert(vc.getReference().getBaseString(),insert, altLeft);
			//System.out.println("==");
			//String insert = obtainInsert(vc.getReference().getBaseString(),high);
			SVType type = null;
			//System.out.println(typeSigns);
			
			//Determine the Type of the SV
			if(start.onSameChromosome(end)) {
				if(start.getPosition()<end.getPosition()) {
					if(typeSigns.contentEquals("]]") && altLeft) {
						if(insert==null) {
							type = SVType.TD;
						}
						else {
							type = SVType.TDINS;
						}
					}
					else if(typeSigns.contentEquals("[[") && !altLeft) {
						type = SVType.DEL;
						if(insert==null) {
							type = SVType.DEL;
						}
						else {
							type = SVType.DELINS;
						}
					}
					//is that an inversion??
					else {
						type = SVType.INV;
					}
					/*
					else if(typeSigns.contentEquals("[[") && altLeft) {
						if(insert==null) {
							type = SVType.INV;
						}
						else {
							type = SVType.INV;
						}
					}
					else if(typeSigns.contentEquals("]]") && !altLeft) {
						if(insert==null) {
							type = SVType.INV;
						}
						else {
							type = SVType.INV;
						}
					}
					*/

				}
				//start position past the begin position
				else {
					if(typeSigns.contentEquals("[[") && !altLeft) {
						if(insert==null) {
							type = SVType.TD;
						}
						else {
							type = SVType.TDINS;
						}
					}
					else if(typeSigns.contentEquals("]]") && altLeft) {
						type = SVType.DEL;
						if(insert==null) {
							type = SVType.DEL;
						}
						else {
							type = SVType.DELINS;
						}
					}
					//need to revcomplement the insert
					else {
						type = SVType.INV;
						insert = Utils.reverseComplement(insert);
					}
					
				}
			}
			else {
				type = SVType.TRANS;
			}
			StructuralVariation sv = new StructuralVariation(type,start,end, insert);
			
			for(String name: vc.getSampleNamesOrderedByName()) {
				//System.out.println(name);
				Sample s = new Sample(name);
				GridssCaller c = new GridssCaller(vc.getGenotype(name));
				c.process();
				s.addCall(c);
				//s.setGt(vc.getGenotype(name));
				sv.addSample(s);
			}
			
			/*
			if(sv.getStartEndLocation().contains("CHROMOSOME_III:829517-830738")) {
				System.out.println(vc.toString());
				System.out.println(sv.toString());
			}
			*/
			
			return sv;
		}
		return null;
	}
	private String obtainInsert(String refString, String insert, boolean altLeft) {
		if(refString.contentEquals(insert)) {
			return null;
		}
		if(!altLeft && insert.startsWith(refString)) {
			return insert.substring(1);
		}
		else if(altLeft && insert.endsWith(refString)) {
			return insert.substring(0, insert.length()-1);
		}
		System.err.println("Something I don't get yet");
		System.err.println("ref: "+refString);
		System.err.println("ins: "+insert);
		return null;
	}
	
	@Override
	public String getName() {
		return GridssCaller.nameCaller;
	}
}

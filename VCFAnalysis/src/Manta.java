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

import controller.PindelController;


public class Manta {

	private static final int MINQG = 50;

	public static void main(String[] args) throws IOException {
		//File vcf = new File("E:/Project_Genome_Scan_Brd_Rtel_Brc_RB873/multisample.final.vcf");
		//File vcf = new File("E:/Project_TLS/20150928_combinevariants.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Hartwig\\LUMC-001-004\\Temp\\vcf\\mergeGVCF_output_filtered.g.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\MA lines - BRC-1 POLQ-1 analysis\\20190313_gvcf_brc-1_project.genotyped.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Next Sequence Run\\Analysis\\20190920_gvcf_LUMC-003-001_worms_filtered.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Next Sequence Run\\Analysis\\20190920_gvcf_LUMC-003-001_arab_filtered.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\MA lines - BRC-1 POLQ-1 analysis\\createAndCombineGVCF_Project_Juul.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Next Sequence Run\\Analysis\\createAndCombineGVCF_Project_Primase.vcf");
		//File vcf = new File("E:\\temp\\createAndCombineGVCF_Project_4TLS.vcf");
		File vcf = new File("E:\\temp\\gridss.vcf");
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
    		int nrCalles = 0;
        	int noOtherCall = 0;
        	boolean unique = false;
        	Genotype last = null;
        	double assrValue = -1;
        	for(String key: strains.keySet()) {
        		for(String sample: strains.get(key)) {
        			Genotype gt = vc.getGenotype(sample);
    				String rpString = (String)gt.getExtendedAttribute("VF");
    				//System.out.println(rpString);
    				double rp = Double.parseDouble(rpString);
    				if(rp>5) {
    					//if(gt.getExtendedAttribute("RP"));
    					//System.out.println(gt);
    					nrCalles++;
    					last = gt;
    					assrValue = rp;
    				}
        		}
        	}
        	if(nrCalles == 1) {
        		Allele one = vc.getAlleles().get(1);
        		String chrEnd = retrieveChr(one);
        		int endLocation = retrieveLoc(one);
        		int size = -1;
        		if(chrEnd.contentEquals(vc.getContig())) {
        			size = endLocation-vc.getStart();
        		}
        		String locationE = chrEnd+":"+endLocation;
        		//System.out.println(vc.toStringWithoutGenotypes());
    			System.out.println(locationS+"\t"+locationE+"\t"+size+"\t"+assrValue+"\t"+last.getSampleName()+"\t"+vc.getAlleles());
    			for(Allele a: vc.getAlleles()) {
    				//System.out.println(a.toString());
    				//System.out.println(a.getDisplayString());
    			}
    			//System.out.println(last);
    		}
        	/*
        	System.exit(0);
        	if(locs.contains(locationS)) {
        		//System.out.println(vc.toStringWithoutGenotypes());
        		vcw.add(vc);
        	}
        	List<String> i = vc.getSampleNamesOrderedByName();
        	int nrCalles = 0;
        	int noOtherCall = 0;
        	boolean unique = false;
        	
        	int maxDP = 0;
        	for(String s: i){
        		Genotype gt = vc.getGenotype(s);
        		System.out.println(gt.toString());
        	}
        	
        	/*
        		sb.append("\t");
        		String venn = gt.getType().toString();
        		if(venn.equals("HOM_VAR")) {
        			venn = "1";
        			if(gt.getDP()>maxDP) {
        				maxDP = gt.getDP();
        			}
        		}
        		//maybe too simplistic
        		else {
        			venn = "0";
        		}
        		//sb.append(gt.getType());
        		sb.append(venn);
        	}
        		
        	int homCallInSet = 0;
        	int nonhomRefCall = 0;
        	int noCall = 0;
        	int hetCall = 0;
        	int homrefCall = 0;
        	int phased = 0;
        	String nonhomRefCallStrains = "";
        	String location = vc.getContig()+":"+vc.getStart()+"-"+vc.getEnd();
        	int locationPos = vc.getStart();
        	String chr = vc.getContig();
    		for(Genotype gt: vc.getGenotypes()) {
    			if(containsSampleName(strains,gt.getSampleName())){
    				if(gt.isPhased()) {
    					phased++;
    				}
    				if(location.equals("CHROMOSOME_IV:15764424-15764424")) {
    					System.out.println(gt);
    					//System.out.println(gt.isPhased());
    					//System.out.println(gt.getExtendedAttribute("PID"));
    					//System.out.println(vc);
    				}
    				
    				if(isHet(gt)) {
    					hetCall++;
    				}
	    			if(gt.isHomVar()) {
	    				homCallInSet++;
	    			}
	    			if(gt.isHomRef()) {
	    				homrefCall++;
	    			}
	    			if(gt.isNoCall()) {
	    				noCall++;
	    			}
	    			if(!gt.isHomRef() && !gt.isNoCall()) {
	    				nonhomRefCall++;
	    				if(nonhomRefCallStrains.length()>0) {
	    					nonhomRefCallStrains+=":";
	    				}
	    				nonhomRefCallStrains+=gt.getSampleName();
	    			}
    			}
    		}
    		//System.exit(0);
        	for(String key: strains.keySet()) {
        		int uniqueHomCount = 0;
        		int uniqueHomCall = 0; //REF or VAR
        		Genotype lastUniqueHom = null;
        		Genotype lastUniqueHet = null;
        		boolean zeroGenerationStrainHomRef = true;
        		boolean zeroGenerationFound = false;
        		boolean printToDiffFromZeroWriter = false;

        		
        		for(String name: strains.get(key)) {
        			//check for 0 strain
        			String[] parts = name.split("-");
        			//System.out.println("checking "+name);
        			Genotype gt = vc.getGenotype(name);
        			if(isHet(gt)) {
        				//System.out.println("hier!");
        				lastUniqueHet = gt;
        			}
        			if(gt.isHomVar()) {
        				uniqueHomCount++;
        				lastUniqueHom = gt;
        			}
        			if(gt.isHomRef() || gt.isHomVar()) {
        				uniqueHomCall++;
        			}
        			if(parts[parts.length-1].equals("0") || name.contains("mother")) {
        				if(gt.isHomRef()) {
        					zeroGenerationStrainHomRef = true;
        				}
        				else {
        					zeroGenerationStrainHomRef = false;
        				}
        				zeroGenerationFound = true;
        			}
        			else if(gt.isHomVar()) {
        				printToDiffFromZeroWriter = true;
        			}
        		}
        		//only call unique when all are called HOM
        		//GQ>=50 probably leads to false positives!
        		//20190312 80 seems a fair number. At 70 I also get strange sites
        		//20190319 switched back to 50
        		//20190319 err... removed GQ check altogether
        		if(hetCall == 1 && lastUniqueHet != null && uniqueHomCall==strains.get(key).size()-1) {
        			//System.out.println(hetCall);
        			//System.out.println(lastUniqueHet);
        			String ref = vc.getReference().getBaseString();
        			String alt = lastUniqueHet.getAllele(0).getBaseString();
        			//this is not a correct SNV position, but probably overlaps with a cnv
        			String hetCallString = lastUniqueHet.getAD()[0]+"|"+lastUniqueHet.getAD()[1];
        			        			
        			
        			if(!alt.equals("*")) {
	        			String mut = getMutation(ref, alt);
	        			String remark = "";
	        			//if(nonhomRefCall == 1 && lastUniqueHom.getGQ()>=40 && lastUniqueHom.getDP()>=8) {
	        				remark = hetCallString; 
	        			//}
	        			int distance = pc.getDistance(lastUniqueHet.getSampleName(), location);
	        			String output =	key+"\t"+chr+"\t"+locationPos+"\t"+location+"\t"+lastUniqueHet.getSampleName()+"\t"+ref+"\t"+alt+"\t"+mut+"\t"+lastUniqueHet.getGQ()+"\t"+lastUniqueHet.getDP()+"\t"+homCallInSet+"\t"+nonhomRefCall+"\t"+noCall+"\t"+phased+"\t"+nonhomRefCallStrains+"\t"+remark+"\t"+distance+"\t"+homrefCall+"\t"+vc.getContig();
	        			//utputUniqueWriter.write(output+"\n");
	        			//System.out.println(output);
        			}
        			
        		}
        		
        		if( uniqueHomCount==1 && uniqueHomCall==strains.get(key).size() ) {
        			//System.out.println("hier");
        			String ref = vc.getReference().getBaseString();
        			
        			String alt = lastUniqueHom.getAllele(0).getBaseString();
        			//this is not a correct SNV position, but probably overlaps with a cnv
        			        			
        			
        			if(!alt.equals("*")) {
	        			String mut = getMutation(ref, alt);
	        			String remark = "";
	        			if(nonhomRefCall == 1 && lastUniqueHom.getGQ()>=40 && lastUniqueHom.getDP()>=8) {
	        				remark = "VERY LIKELY CORRECT"; 
	        			}
	        			uniqueCounter++;
	        			int distance = pc.getDistance(lastUniqueHom.getSampleName(), location);
	        			String output =	key+"\t"+chr+"\t"+locationPos+"\t"+location+"\t"+lastUniqueHom.getSampleName()+"\t"+ref+"\t"+alt+"\t"+mut+"\t"+lastUniqueHom.getGQ()+"\t"+lastUniqueHom.getDP()+"\t"+homCallInSet+"\t"+nonhomRefCall+"\t"+noCall+"\t"+phased+"\t"+nonhomRefCallStrains+"\t"+remark+"\t"+distance;
	        			outputUniqueWriter.write(output+"\n");
	        			//System.out.println(output);
        			}
        		}
        		if( zeroGenerationFound && zeroGenerationStrainHomRef && printToDiffFromZeroWriter ) {
        			String ref = vc.getReference().getBaseString();
        			
        			//print all genotypes
        			for(String name: strains.get(key)) {
        				Genotype gtTemp = vc.getGenotype(name);
        				//overwriting
        				lastUniqueHom = gtTemp;
	        			String alt = lastUniqueHom.getAllele(0).getBaseString();
	        			//this is not a correct SNV position, but probably overlaps with a cnv
	        			if(!alt.equals("*")) {
		        			String mut = getMutation(ref, alt);
		        			String output =	key+"\t"+chr+"\t"+locationPos+"\t"+vc.getContig()+":"+vc.getStart()+"-"+vc.getEnd()+"\t"+lastUniqueHom.getSampleName()+"\t"+ref+"\t"+alt+"\t"+mut+"\t"+lastUniqueHom.getGQ()+"\t"+lastUniqueHom.getDP()+"\t"+homCallInSet+"\t"+nonhomRefCall+"\t"+noCall+"\t"+phased+"\t"+nonhomRefCallStrains+"\t"+lastUniqueHom.getType().toString();
		        			outputDiffFromZeroWriter.write(output+"\n");
		        			//System.out.println(output);
	        			}
        			}
        		}
        	}
        	/*
        	if( noOtherCall == 0 && nrCalles > 0 && vc.getNAlleles() == 2){
        		//System.out.println(vc.getStart());
        		//System.out.println(vc.getGenotype(vc.getSampleNamesOrderedByName().get(0)).toBriefString());
        		//System.out.println(inSample);
        		if(vc.isSNP()){
        			String printVar = vc.getContig()+"\t"+vc.getStart()+"\t"+vc.getAlleles()+"\t"+vc.getPhredScaledQual();
        			List<String> list = vc.getSampleNamesOrderedByName();
        			for(String s: list){
       					Genotype gt = vc.getGenotype(s);
       					printVar += "\t"+gt.getType();
        			}
        			System.out.println(printVar);
        			
        		}
    			//System.out.println(gt.getType());
        	}
        	*/
        }
        vcw.close();
        reader.close();
        outputAllWriter.close();
        outputUniqueWriter.close();
        outputDiffFromZeroWriter.close();
        System.out.println("written ALL to "+outputAll.getAbsolutePath());
        System.out.println("written "+uniqueCounter+" UNIQUE to "+outputUnique.getAbsolutePath());
        System.out.println("written outputDiffFromZeroWriter to "+outputNonUnique.getAbsolutePath());
        
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
		while(Character.isDigit(s.charAt(index))) {
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

}

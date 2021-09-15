import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeType;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder.OutputType;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import controller.PindelController;
import controller.VCFController;


public class SNVCall {

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
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\SNVs\\20190313_gvcf_xf677_project_filtered.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\WGS_LUMC-003-001_Hartwig\\Analysis\\20200403_gvcf_LUMC-003-001_arab_filtered.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\bams\\worms\\Pindel_Raw\\LUMC-001-010\\20201019_gvcf_LUMC-001-010_Genotype_gvcf.txt");
		File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\LUMC-001-101\\Analysis\\GATK");
		ArrayList<File> vcfs = getVCFFiles(vcf);
		VCFController vcfC = new VCFController();
		for(File f: vcfs) {
			vcfC.addFile(f);
		}
		HashMap<String, Integer> Generations = createLookupTableGen(new File("C:\\Users\\rvanschendel\\git\\test_robin\\MMR_java\\wormSampleGen.txt"));
		HashMap<String, String> WormDB = createLookupTable(new File("C:\\Users\\rvanschendel\\git\\test_robin\\MMR_java\\wormDB.txt"));
		vcfC.setGenerations(Generations);
		vcfC.setWormDB(WormDB);
		File pindel = new File("C:\\Users\\rvanschendel\\git\\test_robin\\MMR_java\\Curated.txt");
		PindelController pc = new PindelController(pindel);
		pc.parsePindel(true);
		vcfC.setPindelController(pc);
		vcfC.printVCFs();
		System.exit(0);
		//cross check locations with SVs
		//File pindel = new File("C:\\Users\\rvanschendel\\Dropbox\\4TLS_Paper\\NGS data\\Pindel_Analysis.txt");
		Scanner scan = new Scanner(new File("printToVCF.txt"));
		ArrayList<String> locs = new ArrayList<String>();
		while(scan.hasNext()) {
			String line = scan.nextLine();
			locs.add(line);
		}
		scan.close();
		boolean excludeINVandTD = true;
		

		
		pc.parsePindel(excludeINVandTD);
		HashMap<String,Integer> snvCounts = new HashMap<String,Integer>();
		
		
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
    	VariantContextBuilder vcb = new VariantContextBuilder();
        
        StringBuffer sb = new StringBuffer("Location");
        for(String s: al){
        	sb.append("\t");
        	sb.append(s);
        }
        System.out.println(sb.toString());
        outputAllWriter.write(sb.toString()+"\n");
        String uniqueHeader = "Genotype\tChr\tPos\tLocation\tSample\tRef\tAlt\tMutation\tGQ(Phred-based quality)\tDP\tnrHomCallInSet\tnonhomRefCall\tnoCall\tphased\tnonhomRefCallStrains\tRemark\tDistanceToPindelEvent\t#Generations\tSNV_per_generation";
        System.out.println(uniqueHeader);
        outputUniqueWriter.write(uniqueHeader+"\n");
        outputDiffFromZeroWriter.write(uniqueHeader+"\n");
        CloseableIterator<VariantContext> it = reader.iterator();
        int uniqueCounter = 0;
        while(it.hasNext()){
        	sb = new StringBuffer();
        	VariantContext vc = it.next();


        	//only SNPs at the moment!
        	if(!vc.isSNP()) {
        		//System.out.println(vc);
        		continue;
        		
        	}
        	String locationS = vc.getContig()+":"+vc.getStart()+"-"+vc.getEnd()+"["+vc.getReference().getBaseString()+"]";
        	if(locs.contains(locationS)) {
        		//System.out.println(vc.toStringWithoutGenotypes());
        		vcw.add(vc);
        	}
        	sb.append(locationS);
        	List<String> i = vc.getSampleNamesOrderedByName();
        	//System.out.println(vc);
        	int nrCalles = 0;
        	int noOtherCall = 0;
        	boolean unique = false;
        	
        	int maxDP = 0;
        	for(String s: i){
        		Genotype gt = vc.getGenotype(s);
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
    					//System.out.println(gt.isCalled());
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
        			//incorrect file!!!!
        			if(name.contentEquals("XF1012_F44")) {
        				continue;
        			}
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
        			String alt = getAlt(lastUniqueHet.getAlleles());
        			//this is not a correct SNV position, but probably overlaps with a cnv
        			String hetCallString = lastUniqueHet.getAD()[0]+"|"+lastUniqueHet.getAD()[1];
        			        			
        			
        			if(alt != null && !alt.equals("*")) {
	        			String mut = getMutation(ref, alt);
	        			String remark = "";
	        			//if(nonhomRefCall == 1 && lastUniqueHom.getGQ()>=40 && lastUniqueHom.getDP()>=8) {
	        				remark = hetCallString;
	        				//get a percentage
	        				double sum = lastUniqueHet.getAD()[0]+lastUniqueHet.getAD()[1];
	        				remark = ""+(lastUniqueHet.getAD()[1]/sum);
	        			//}
	        			int distance = pc.getDistance(lastUniqueHet.getSampleName(), location);
	        			String output =	key+"\t"+chr+"\t"+locationPos+"\t"+location+"\t"+lastUniqueHet.getSampleName()+"\t"+ref+"\t"+alt+"\t"+mut+"\t"+lastUniqueHet.getGQ()+"\t"+lastUniqueHet.getDP()+"\t"+homCallInSet+"\t"+nonhomRefCall+"\t"+noCall+"\t"+phased+"\t"+nonhomRefCallStrains+"\t"+remark+"\t"+distance+"\t"+homrefCall+"\t"+vc.getContig();
	        			outputUniqueWriter.write(output+"\n");
	        			//System.out.println(output);
        			}
        			
        		}
        		
        		if( uniqueHomCount==1 && ( uniqueHomCall==strains.get(key).size()  || (uniqueHomCall+noCall)==strains.get(key).size() )) {
        			if(location.equals("CHROMOSOME_IV:15764424-15764424")) {
        				System.out.println("hier");
            		}
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
	        			String sampleName = lastUniqueHom.getSampleName();
	        			if(sampleName.endsWith("_R1")) {
	        				sampleName = sampleName.replace("_R1", "");
	        			}
	        			int generations = -1;
	        			if(Generations.containsKey(sampleName)) {
	        				generations = Generations.get(sampleName);
	        			}
	        			else {
	        				//let's try to derive it
	        				String[] parts = sampleName.split("-");
	        				if(parts.length==2) {
	        					String total = "";
	        					for(int index = 0;index<parts[1].length();index++) {
	        						char c = parts[1].charAt(index);
	        						if(Character.isDigit(c)) {
	        							total+=c;
	        						}
	        					}
	        					generations = Integer.parseInt(total);
	        				}
	        				//System.err.println(sampleName);
	        			}
	        			
	        			//System.out.println(generations);
	        			//System.out.println(genotype);
	        			
	        			String output =	key+"\t"+chr+"\t"+locationPos+"\t"+location+"\t"+lastUniqueHom.getSampleName()+"\t"+ref+"\t"+alt+"\t"+mut+"\t"+lastUniqueHom.getGQ()+"\t"+lastUniqueHom.getDP()+"\t"+homCallInSet+"\t"+nonhomRefCall+"\t"+noCall+"\t"+phased+"\t"+nonhomRefCallStrains+"\t"+remark+"\t"+distance+"\t"+generations+"\t"+1/(double)generations;
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
        	//System.out.println(vc);
        	if(vc.getAltAlleleWithHighestAlleleCount().getBaseString().contentEquals("*")) {
        		continue;
        	}
        	String locationSNV = vc.getContig()+":"+vc.getStart()+"["+vc.getReference().getBaseString()+">"+vc.getAltAlleleWithHighestAlleleCount().getBaseString()+"]";
        	String output = locationSNV;
        	int totalSNV = 0;
        	int totalGroupsHaveSNV = 0;
        	int totalSample = 0;
        	for(String key: strains.keySet()) {
        		ArrayList<String> names = strains.get(key);
        		int snpCounter = 0;
        		int motherSNPCounter = 0;
        		//System.out.println(key);
        		for(String name: strains.get(key)) {
        			if(name.contentEquals("atku80-SALK-016627-5-2") || name.contentEquals("atbrca1-1-SALK-014731-5-16")) {
            			continue;
            		}
        			totalSample++;
        			Genotype gt = vc.getGenotype(name);
        			if(gt.isHomVar()) {
        				snpCounter++;
        				totalSNV++;
        				//System.out.println(name);
        				if(isZeroStrain(name)) {
        					motherSNPCounter++;
            			}
        			}
        			//System.out.println("\t"+name);
        		}
        		if(snpCounter>0) {
        			totalGroupsHaveSNV++;
        		}
        		output+="\t"+key+"\t"+motherSNPCounter+"\t"+snpCounter+"\t"+names.size();
        	}
        	if(totalSNV>0) {
        		double fractionSNV = totalSNV/(double)totalSample;
        		//System.out.print(fractionSNV+"\t"+totalSNV+"\t"+totalGroupsHaveSNV+"\t"+output+"\n");
        	}
        	
        	//compare pairwise
        	if(vc.getAltAlleleWithHighestAlleleCount().getBaseString().contentEquals("*")) {
        		continue;
        	}
        	ArrayList<String> tempNames = new ArrayList<String>();
        	ArrayList<String> totalNames = new ArrayList<String>();
        	int maxGQ = 0 ;
        	for(String name: vc.getSampleNamesOrderedByName()) {
        		if(name.contentEquals("atku80-SALK-016627-5-2") || name.contentEquals("atbrca1-1-SALK-014731-5-16")) {
        			continue;
        		}
        		totalNames.add(name);
        		Genotype gt = vc.getGenotype(name);
        		//add some depth
    			if(gt.isHomVar()) {
    				tempNames.add(name);
    				if(gt.getGQ()>maxGQ) {
    					maxGQ = gt.getGQ();
    				}
    			}
        	}
        	//should be done only once, but ok
        	if(snvCounts.size()==0) {
	        	for(String n: totalNames) {
	        		//System.out.println(n);
	        		for(String m: totalNames) {
	        			String tempName = n+"|"+m;
	        			if(!snvCounts.containsKey(tempName)) {
	        				snvCounts.put(tempName,0);
	        			}
	        		}
	        	}
        	}
        	
//        	if(totalGroupsHaveSNV >= 1 && maxGQ>90) {// && tempNames.size()<vc.getSampleNames().size()) {
        	if(totalGroupsHaveSNV == 1 && maxGQ>90) {
	        	for(String n: tempNames) {
	        		if(n.contentEquals("atku80-SALK-016627-5-2") || n.contentEquals("atbrca1-1-SALK-014731-5-16")) {
	        			continue;
	        		}
	        		for(String m: tempNames) {
	        			if(m.contentEquals("atku80-SALK-016627-5-2") || m.contentEquals("atbrca1-1-SALK-014731-5-16")) {
	            			continue;
	            		}
	        			String tempName = n+"|"+m;
	    				//if(!snvCounts.containsKey(tempName)) {
	    				//	snvCounts.put(tempName, 0);
	    				//}
	    				snvCounts.put(tempName, snvCounts.get(tempName)+1);
	        		}
	        	}
        	}
        	//for inspection purposes
        	/*
        	if(tempNames.contains("XF1648-0") && maxGQ>90) {
        		//System.out.println(totalGroupsHaveSNV);
        		if(totalGroupsHaveSNV>1 && totalGroupsHaveSNV<=3) {
        			System.out.println(totalGroupsHaveSNV);
        			System.out.println(String.join("\n", tempNames));
        			System.out.println(vc.getContig()+":"+vc.getStart());
        		}
        	}
        	*/
        	
        	//System.exit(0);
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
        //process snvCounts
        //getNames
        ArrayList<String> names = new ArrayList<String>();
        for(String key: snvCounts.keySet()) {
        	//System.out.println(key);
        	String name = key.split("\\|")[0];
        	if(!names.contains(name)) {
        		names.add(name);
        	}
        }
        Collections.sort(names);
        String headerStr = "\t";
        for(String name: names) {
        	headerStr+=name+"\t";
        }
        System.out.println(headerStr);
        for(String name: names) {
        	int max = 0;
        	for(String name2: names) {
        		String tempKey = name+"|"+name2;
        		if(snvCounts.containsKey(tempKey)) {
        			if(snvCounts.get(tempKey)>max) {
        				max = snvCounts.get(tempKey);
        			}
        		}
        	}
        	//again and print
        	String print = name;
        	for(String name2: names) {
        		String tempKey = name+"|"+name2;
        		if(snvCounts.containsKey(tempKey)) {
        			//double fraction = (snvCounts.get(tempKey)/(double)max); 
        			double fraction = snvCounts.get(tempKey);
        			print+="\t"+String.format(Locale.ROOT,"%.2f", fraction);
        		}
        		else {
        			print+="\t"+0;
        		}
        	}
        	System.out.println(print);
        }
        for(String key: snvCounts.keySet()) {
        	//System.out.println(key+"\t"+snvCounts.get(key));
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

	private static ArrayList<File> getVCFFiles(File vcf) {
		ArrayList<File> vcfs = new ArrayList<File>();
		if(vcf.isDirectory()) {
			for(File f: vcf.listFiles()) {
				vcfs.addAll(getVCFFiles(f));
			}
		}
		else {
			if(vcf.getName().endsWith(".vcf")) {
				vcfs.add(vcf);
			}
		}
		return vcfs;
	}

	private static String getAlt(List<Allele> alleles) {
		for(Allele a: alleles) {
			if(a.isNonReference()) {
				return a.getBaseString();
			}
		}
		return null;
	}

	private static boolean isZeroStrain(String name) {
		if(name.contains("mother")) {
			return true;
		}
		//TODO add the worm syntax
		return false;
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
	public static HashMap<String, Integer> createLookupTableGen(File file) {
		if(!file.exists()) {
			System.err.println("Lookup file "+file+" does not exist");
			System.exit(0);
		}
		try {
			Scanner s =new Scanner(file);
			HashMap<String, Integer> lookup = new HashMap<String, Integer>();
			while(s.hasNextLine()) {
				String line = s.nextLine();
				String[] parts = line.split("\t");
				if(parts.length>=2) {
					lookup.put(parts[0],Integer.parseInt(parts[1]));
				}
			}
			s.close();
			return lookup;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	static HashMap<String, String> createLookupTable(File file) {
		if(!file.exists()) {
			System.err.println("Lookup file "+file+" does not exist");
		}
		try {
			Scanner s =new Scanner(file);
			HashMap<String, String> lookup = new HashMap<String, String>();
			while(s.hasNextLine()) {
				String line = s.nextLine();
				String[] parts = line.split("\t");
				if(parts.length>=2) {
					String text = parts[1].replaceAll("\"", "");
					lookup.put(parts[0].toUpperCase(),text);
				}
			}
			s.close();
			return lookup;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

}

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeType;
import htsjdk.variant.variantcontext.VariantContext;
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


public class TestMain {

	private static final int MINQG = 50;

	public static void main(String[] args) throws IOException {
		//File vcf = new File("E:/Project_Genome_Scan_Brd_Rtel_Brc_RB873/multisample.final.vcf");
		//File vcf = new File("E:/Project_TLS/20150928_combinevariants.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Hartwig\\LUMC-001-004\\Temp\\vcf\\mergeGVCF_output_filtered.g.vcf");
		File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\MA lines - BRC-1 POLQ-1 analysis\\20190313_gvcf_brc-1_project.genotyped.vcf");
		File outputAll = new File(vcf.getAbsolutePath()+"_all_snvs.txt");
		File outputUnique = new File(vcf.getAbsolutePath()+"_unique_snvs.txt");
		BufferedWriter outputAllWriter = new BufferedWriter(new FileWriter(outputAll));;
		BufferedWriter outputUniqueWriter = new BufferedWriter(new FileWriter(outputUnique));;
        VCFFileReader reader = new VCFFileReader(vcf, false);
        VCFHeader header = reader.getFileHeader();
        ArrayList<String> al = header.getSampleNamesInOrder();
        HashMap<String,ArrayList<String>> strains = getStrains(al);
        
        StringBuffer sb = new StringBuffer("Location");
        for(String s: al){
        	sb.append("\t");
        	sb.append(s);
        }
        System.out.println(sb.toString());
        outputAllWriter.write(sb.toString()+"\n");
        String uniqueHeader = "Genotype\tLocation\tStrain\tRef\tAlt\tMutation\tGQ(Phred-based quality)\tDP\tnrHomCallInSet\tnonhomRefCall\tnonhomRefCallStrains";
        System.out.println(uniqueHeader);
        outputUniqueWriter.write(uniqueHeader+"\n");
        CloseableIterator<VariantContext> it = reader.iterator();
        while(it.hasNext()){
        	sb = new StringBuffer();
        	VariantContext vc = it.next();
        	//only SNPs at the moment!
        	if(!vc.isSNP()) {
        		continue;
        	}
        	sb.append(vc.getContig()+":"+vc.getStart()+"-"+vc.getEnd()+"["+vc.getReference()+"]");
        	List<String> i = vc.getSampleNamesOrderedByName();
        	//System.out.println(vc);
        	int nrCalles = 0;
        	int noOtherCall = 0;
        	boolean unique = false;
        	
        	for(String s: i){
        		Genotype gt = vc.getGenotype(s);
        		sb.append("\t");
        		sb.append(gt.getType());
        	}
        	//System.out.println(sb.toString());
        	outputAllWriter.write(sb.toString()+"\n");
        	int homCallInSet = 0;
        	int nonhomRefCall = 0;
        	String nonhomRefCallStrains = "";
    		for(Genotype gt: vc.getGenotypes()) {
    			if(gt.isHomVar()) {
    				homCallInSet++;
    			}
    			if(!gt.isHomRef()) {
    				nonhomRefCall++;
    				if(nonhomRefCallStrains.length()>0) {
    					nonhomRefCallStrains+=":";
    				}
    				nonhomRefCallStrains+=gt.getSampleName();
    			}
    		}
        	for(String key: strains.keySet()) {
        		int uniqueHomCount = 0;
        		int uniqueHomCall = 0; //REF or VAR
        		Genotype lastUniqueHom = null;
        		
        		for(String name: strains.get(key)) {
        			Genotype gt = vc.getGenotype(name);
        			if(gt.isHomVar()) {
        				uniqueHomCount++;
        				lastUniqueHom = gt;
        			}
        			if(gt.isHomRef() || gt.isHomVar()) {
        				uniqueHomCall++;
        			}
        		}
        		/*
        		if(vc.getStart()==5973139) {
        			System.out.println("hier");
        			System.out.println(key+"\t"+uniqueHomCount+"\t"+uniqueHomCall);
        			if(lastUniqueHom != null) {
        				System.out.println(lastUniqueHom.getGQ());
        			}
        		}
        		*/
        		//only call unique when all are called HOM
        		//GQ>=50 probably leads to false positives!
        		//20190312 80 seems a fair number. At 70 I also get strange sites
        		//20190319 switched back to 50
        		//20190319 err... removed GQ check altogether
        		
        		if(uniqueHomCount==1 && uniqueHomCall==strains.get(key).size()) {
        			String ref = vc.getReference().getBaseString();
        			String alt = lastUniqueHom.getAllele(0).getBaseString();
        			//this is not a correct SNV position, but probably overlaps with a cnv
        			if(!alt.equals("*")) {
	        			String mut = getMutation(ref, alt);
	        			String output =	key+"\t"+vc.getContig()+":"+vc.getStart()+"-"+vc.getEnd()+"\t"+lastUniqueHom.getSampleName()+"\t"+ref+"\t"+alt+"\t"+mut+"\t"+lastUniqueHom.getGQ()+"\t"+lastUniqueHom.getDP()+"\t"+homCallInSet+"\t"+nonhomRefCall+"\t"+nonhomRefCallStrains;
	        			outputUniqueWriter.write(output+"\n");
	        			//System.out.println(output);
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
        reader.close();
        outputAllWriter.close();
        outputUniqueWriter.close();
        System.out.println("written ALL to "+outputAll.getAbsolutePath());
        System.out.println("written UNIQUE to "+outputUnique.getAbsolutePath());
        
	}

	private static HashMap<String,ArrayList<String>> getStrains(ArrayList<String> al) {
		HashMap<String,ArrayList<String>> strainMap = new HashMap<String,ArrayList<String>>(); 
		for(String s: al) {
			String strain = getStrain(s);
			if(!strainMap.containsKey(strain)) {
				strainMap.put(strain,new ArrayList<String>());
			}
			strainMap.get(strain).add(s);
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

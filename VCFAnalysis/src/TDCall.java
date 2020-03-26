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

import controller.PindelController;


public class TDCall {

	private static final int MINQG = 50;

	public static void main(String[] args) throws IOException {
		//File vcf = new File("E:/Project_Genome_Scan_Brd_Rtel_Brc_RB873/multisample.final.vcf");
		//File vcf = new File("E:/Project_TLS/20150928_combinevariants.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Hartwig\\LUMC-001-004\\Temp\\vcf\\mergeGVCF_output_filtered.g.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\MA lines - BRC-1 POLQ-1 analysis\\20190313_gvcf_brc-1_project.genotyped.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Next Sequence Run\\Analysis\\20190920_gvcf_LUMC-003-001_worms_filtered.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Next Sequence Run\\Analysis\\20190920_gvcf_LUMC-003-001_arab_filtered.vcf");
		//File vcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\MA lines - BRC-1 POLQ-1 analysis\\createAndCombineGVCF_Project_Juul.vcf");
		File vcf = new File("E:\\temp\\merged_worms.vcf");
		//cross check locations with SVs
		//File pindel = new File("Z:\\Juliette\\Experiments and Projects\\brc-1 brd-1 polq-1\\NGS\\MA_BQ_All_merged_2019.txt");
		//PindelController pc = new PindelController(pindel);
		//boolean excludeINVandTD = true;
		
		//pc.parsePindel(excludeINVandTD);
		
		
		File outputAll = new File(vcf.getAbsolutePath()+"_all_snvs.txt");
		File outputUnique = new File(vcf.getAbsolutePath()+"_unique_snvs.txt");
		File outputNonUnique = new File(vcf.getAbsolutePath()+"_diffFromZero_snvs.txt");
		BufferedWriter outputAllWriter = new BufferedWriter(new FileWriter(outputAll));;
		BufferedWriter outputUniqueWriter = new BufferedWriter(new FileWriter(outputUnique));;
		BufferedWriter outputDiffFromZeroWriter = new BufferedWriter(new FileWriter(outputNonUnique));;
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
        String uniqueHeader = "Genotype\tSVType\tLocation\tLocationIGV\tSample\tRef\tAlt\tMutation\tGQ(Phred-based quality)\tDP\tnrHomCallInSet\tnonhomRefCall\tnonhomRefCallStrains\tRemark\tSize\tSupports";
        System.out.println(uniqueHeader);
        outputUniqueWriter.write(uniqueHeader+"\n");
        outputDiffFromZeroWriter.write(uniqueHeader+"\n");
        CloseableIterator<VariantContext> it = reader.iterator();
        
        int counterUnique = 0;
        while(it.hasNext()){
        	sb = new StringBuffer();
        	VariantContext vc = it.next();
        	//only SNPs at the moment!
        	sb.append(vc.getContig()+":"+vc.getStart()+"-"+vc.getEnd()+"["+vc.getReference()+"]");
        	List<String> i = vc.getSampleNamesOrderedByName();
        	int nrCalles = 0;
        	int noOtherCall = 0;
        	boolean unique = false;
        	
        	for(String s: i){
        		Genotype gt = vc.getGenotype(s);
        		sb.append("\t");
        		sb.append(gt.getType());
        	}
        	
        	//System.out.println(sb.toString());
        	String svtype = vc.getAttribute("SVTYPE").toString();
        	String precise = vc.getAttributeAsString("PRECISE", "false");
        	if(!precise.equals("true")){
        		continue;
        	}
    		//System.out.println(precise +"\t"+vc.getAttribute("SVTYPE")+"\t"+vc.toStringWithoutGenotypes());
        	outputAllWriter.write(sb.toString()+"\n");
        	int homCallInSet = 0;
        	int nonhomRefCall = 0;
        	String nonhomRefCallStrains = "";
        	Genotype lastUniqueHom = null;
        	int lastRv = 0;
    		for(Genotype gt: vc.getGenotypes()) {
    			//System.out.println(gt.getSampleName()+" "+gt.toString());
    			int rv = Integer.parseInt(""+gt.getAnyAttribute("RV"));
    			if(rv>0) {
    				homCallInSet++;
    				lastUniqueHom = gt;
    				lastRv = rv;
    			}
    		}
    		if( homCallInSet==1 ) {
    			String ref = vc.getReference().getBaseString();
    			String alt = lastUniqueHom.getAllele(0).getBaseString();
    			//this is not a correct SNV position, but probably overlaps with a cnv
    			//if(!alt.equals("*")) {
    			String mut = getMutation(ref, alt);
    			String remark = "";
    			if(nonhomRefCall == 1 && lastUniqueHom.getGQ()>=40 && lastUniqueHom.getDP()>=8) {
    				remark = "VERY LIKELY CORRECT"; 
    			}
    			String location = vc.getContig()+":"+vc.getStart()+"-"+vc.getEnd();
    			 
    			int size = vc.getEnd()-vc.getStart();
    			int igvStart = vc.getStart()-(size/10);
    			int igvEnd = vc.getEnd()+(size/10);
    			String locationIGV = vc.getContig()+":"+igvStart+"-"+igvEnd;
    			String key = getStrain(lastUniqueHom.getSampleName());
    			//int distance = pc.getDistance(lastUniqueHom.getSampleName(), location);
    			String output =	key+"\t"+svtype+"\t"+location+"\t"+locationIGV+"\t"+lastUniqueHom.getSampleName()+"\t"+ref+"\t"+alt+"\t"+mut+"\t"+lastUniqueHom.getGQ()+"\t"+lastUniqueHom.getDP()+"\t"+homCallInSet+"\t"+nonhomRefCall+"\t"+nonhomRefCallStrains+"\t"+remark+"\t"+size+"\t"+lastRv;
    			outputUniqueWriter.write(output+"\n");
    			counterUnique++;
        	}
        }
        reader.close();
        outputAllWriter.close();
        outputUniqueWriter.close();
        outputDiffFromZeroWriter.close();
        System.out.println("written ALL to "+outputAll.getAbsolutePath());
        System.out.println("written "+counterUnique+" UNIQUE to "+outputUnique.getAbsolutePath());
        System.out.println("written outputDiffFromZeroWriter to "+outputNonUnique.getAbsolutePath());
        
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
		//different way
		if(part.length()==2) {
			String[] parts = s.split("-");
			return parts[0];
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

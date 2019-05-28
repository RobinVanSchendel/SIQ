import htsjdk.samtools.reference.FastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeType;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import utils.CompareSequence;
import utils.KMERLocation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojavax.bio.seq.RichSequence;
import org.jcvi.jillion.core.qual.QualitySequence;


public class TestCosmic {

	public static void main(String[] args) {
		//File vcf = new File("E:/Project_Genome_Scan_Brd_Rtel_Brc_RB873/multisample.final.vcf");
		File vcf = new File("E:\\Project_Cosmic\\CosmicCodingMuts.vcf");
        VCFFileReader reader = new VCFFileReader(vcf, false);
        VCFHeader header = reader.getFileHeader();
        VCFHeaderLine refGenome = header.getOtherHeaderLine("reference");
        //System.out.println(refGenome.getValue());
        //for now just human
        File ref = new File("E:\\genomes\\homo_sapiens\\"+refGenome.getValue()+".fa");
        ReferenceSequenceFile rsf = ReferenceSequenceFileFactory.getReferenceSequenceFile(ref);
        CloseableIterator<VariantContext> it = reader.iterator();
        int count = 0;
        int sizeFlank = 200;
        File out = new File("E:\\Cosmic_Out_FLT3.txt");
        BufferedWriter b = null;
        try {
			b = new BufferedWriter(new FileWriter(out));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        try {
			b.write(CompareSequence.getOneLineHeader()+"\n");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        while(it.hasNext()){
        	VariantContext vc = it.next();
        	if(vc.getAttribute("GENE").equals("FLT3")){
        		//System.out.println(vc.getReference().getBaseString());
        		//String dna = vc.getContig()
        		//ReferenceSequence rs = rsf.getSubsequenceAt(vc.getContig(), vc.getStart(), vc.getEnd());
        		//System.out.println(vc);
        		//System.out.println(vc.toString());
        		//System.exit(0);
        		int start = vc.getStart()-1;
        		int end = vc.getEnd()+1;
        		String dna = rsf.getSubsequenceAt(vc.getContig(), vc.getStart()-sizeFlank, vc.getEnd()+sizeFlank).getBaseString();
        		String left = rsf.getSubsequenceAt(vc.getContig(), start-sizeFlank, start).getBaseString();
        		String leftFlank = left;
        		String rightFlank = rsf.getSubsequenceAt(vc.getContig(), start+1, start+30).getBaseString();
        		String right = rsf.getSubsequenceAt(vc.getContig(), end, end+sizeFlank).getBaseString();
        		String del = vc.getReference().getBaseString();
        		String ins = vc.getAlternateAlleles().get(0).getBaseString();
        		//System.out.println(vc.getReference().getBaseString()+" : "+vc.getAlternateAlleles());
        		//System.out.println(vc.getStart()+":"+vc.getEnd());
        		//System.out.println(left);
        		//System.out.println(right);
        		//System.out.println(del);
        		//System.out.println(ins);
        		RichSequence subject = null;
        		try {
					subject = RichSequence.Tools.createRichSequence(vc.getContig(),DNATools.createDNA(dna));
				} catch (IllegalSymbolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		String query = left+ins+right;
        		CompareSequence cs = new CompareSequence(subject, query, null, leftFlank, rightFlank, null, null, false, vc.getID(), null);
        		cs.determineFlankPositions(true);
        		cs.setCutType(vc.getContig()+":"+vc.getStart()+"-"+vc.getEnd());
        		cs.setCurrentAlias(""+vc.getAttribute("GENE"),"");
        		cs.setCurrentFile(""+vc.getAttribute("CNT"));
        		try {
        			if(cs.getRemarks().length()==0) {
        				b.write(cs.toStringOneLine()+"\n");
        			}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
       		
        		
        		//if(vc.getReference().length()>1) {
        			
        		//}
        		count++;
        	}
        	//System.out.println(vc.getContig());
        		if(count%100000 == 0) {
        			//System.out.println("Already processed "+count+" reads");
        		}
        }
        reader.close();
        System.out.println(count+ " FLT3 mutations" );
	}

}

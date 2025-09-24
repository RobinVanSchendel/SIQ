package utils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojavax.bio.seq.RichSequence;

import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.reference.FastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;
import utils.CompareSequence.Type;

public class CigarAlignmentMain {

    public static String consensus(String s, String s2) {
    	StringBuilder sb = new StringBuilder(s.length());
    	for(int i = 0;i<s.length();i++) {
    		if(s.charAt(i) == s2.charAt(i)) {
    			sb.append(" ");
    		} else{
    			sb.append("|");
    		}
    	}
    	return sb.toString();
    }
    

    public static void main(String[] args) throws IllegalSymbolException {
    	//File fastaFile = new File("Z:\\Robin\\Project_Adam_Ameur\\SIQinput_PureTarget\\SIQinput_PureTarget\\for_IGV\\sh2b3_off1.fa");
    	File fastaFile = new File("Z:\\Robin\\TedToal\\ForRobin_SIQ\\ForRobin_SIQ\\NoAdaps_WT1.fasta");
    	//File fastaFile = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Targeted Sequencing\\Hartwig\\20210402_PacBio\\ref\\HPRT-exon3_PacBio_o1540-o1541.txt");
    	File hdrFile = new File("Z:\\Robin\\TedToal\\ForRobin_SIQ\\ForRobin_SIQ\\NoAdaps_WT\\NoAdaps_WT1_hdr.fasta");
    	//File hdrFile = null;
		ReferenceSequenceFile fastaSequenceFile = ReferenceSequenceFileFactory.getReferenceSequenceFile(fastaFile);
		ReferenceSequence refSeq = fastaSequenceFile.nextSequence();
		//ReferenceSequenceFile fastaSequenceFileHDR = ReferenceSequenceFileFactory.getReferenceSequenceFile(hdrFile);
		//ReferenceSequence hdrSeq = fastaSequenceFileHDR.nextSequence();
		String ref = new String(refSeq.getBases()).toUpperCase();
		System.out.println("ref:"+ref);
		//String hdr = new String(hdrSeq.getBases()).toUpperCase();
		
		String leftFlank = "GGAGAGTGGATTAGTGATGA";
		String rightFlank = "GAGTGGTATTAGTTGAGAAG";
		//String leftFlank = "GAGATGGGAGGCCATCA";
		//String rightFlank = "TGCTGGATTACATTAAAGCA";
		int leftFlankPos = ref.indexOf(leftFlank);
		if(leftFlankPos != -1) {
			leftFlankPos += leftFlank.length()-1;
		}
		int rightFlankPos = ref.indexOf(rightFlank);
		
		RichSequence subject = RichSequence.Tools.createRichSequence(refSeq.getName(), DNATools.createDNA(ref));
		//RichSequence hdrS = RichSequence.Tools.createRichSequence(hdrSeq.getName(), DNATools.createDNA(hdr));
    	//String f = "Z:\\Robin\\Project_Adam_Ameur\\SIQinput_PureTarget\\SIQinput_PureTarget\\for_IGV\\pr_180_027_sh2b3_off1.sorted.bam";
		String f = "Z:\\Robin\\TedToal\\ForRobin_SIQ\\ForRobin_SIQ\\WT1.10000.bam";
		//String f = "Z:\\Datasets - NGS, UV_TMP, MMP\\Targeted Sequencing\\Hartwig\\20210402_PacBio\\raw\\J-Schimmel\\sam\\CCS.H08--H08.bam.fq.sam.bam.sorted.bam";
		Subject subjectObject = new Subject(subject,leftFlank,rightFlank, true);
		//subjectObject.addHDR(hdrS);
        File bamFile = new File(f);
        System.out.println(String.join("\t",CompareSequence.getOneLineHeaderArray()));
        
        try (SamReader reader = SamReaderFactory.makeDefault()
                .validationStringency(ValidationStringency.SILENT)
                .open(bamFile)) {
            SAMRecordIterator iterator = reader.iterator();
            while (iterator.hasNext()) {
                SAMRecord record = iterator.next();
                
                if(record.getReadName().contentEquals("77f22d57-a382-47b0-a08a-50b175a16890")) {
                	System.out.println(record.getReadString());
                	System.out.println(record.getBaseQualityString());
                	System.exit(0);
                }
                
                String[] align = CigarAlignment.align(record, ref);
                //System.out.println(record.getContig());
                //System.out.println(leftFlankPos);
                //System.out.println(record.getCigarString());
                //System.out.println(align[0]);
                //System.out.println(align[1]);
                //System.out.println(align[2]);
                //System.out.println(consensus(align[0],align[1]));
                CigarAlignment ca = new CigarAlignment(record, refSeq);
                //
                CigarAlignmentSpan spanLeft = ca.getLeftFlank(leftFlankPos, 15);
                CigarAlignmentSpan spanRight = ca.getRightFlank(rightFlankPos+1, 15);
                //System.out.println(ca.getSpanRef(spanLeft));
                String leftFlankS = ca.getSpanRef(spanLeft);
                //System.out.println("left:"+leftFlankS);
                //System.out.println("right:"+ca.getSpanRef(spanRight));
                String rightFlankS = "";
                String del = "";
                String insert = "";
                //System.out.println("xxxxx");
                if(spanLeft != null) {
	                rightFlankS = ca.getSpanRef(spanRight);
	                del = ca.getDeletion(spanLeft, spanRight);
	                insert = ca.getInsert(spanLeft, spanRight);
	                //System.out.println("rightFlank:"+ca.getSpanRef(spanRight));
	                //System.out.println("del:"+ca.getDeletion(spanLeft, spanRight));
	                //System.out.println("ins:"+insert);
	                String query = leftFlankS+insert+rightFlankS;
	                //System.out.println("query:"+query);
	                //System.out.println("xxxx");
	                boolean queryInRecord = record.getReadString().contains(query);
	                if(!queryInRecord) {
	                	System.err.println("MAJOR ISSUE!");
	                	System.exit(0);
	                }
	                CompareSequence cs = new CompareSequence(subjectObject, query, null, null, false, record.getReadName());
	                cs.setCurrentAlias("WT1.10000.bam", f);
	                cs.determineFlankPositions(false);
	                if(cs.getType() == Type.INSERTION && cs.getInsertion().length() == 1) {
	                	System.out.println(cs.toStringOneLine("dummy"));
	                }
                }
                int leftLength = -1;
                if(leftFlankS != null) {
                	leftLength = leftFlankS.length();
                }
                
                //System.out.println(leftLength+"\t"+leftFlankS+"\t"+del+"\t"+rightFlankS+"\t"+insert);
                
                //ca.printAlignment();
                //System.exit(0);
                //System.out.println("=====");
                //int leftMost = getLeftmostQueryPosNearReference(record, leftFlankPos);
                //System.out.println(leftMost);
                //System.out.println(ref);
                //System.out.println(record.getReadString());
                //System.out.println(record.getReadString().substring(0, leftMost+1));
                //System.out.println(record.getCigarString());
                //System.out.println("xxxxxxxxxx");
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public static int getLeftmostQueryPosNearReference(SAMRecord record, int refPosition) {
        int refStart = record.getAlignmentStart(); // 1-based
        int readPos = 0;
        int refPos = refStart;

        int closestQueryPos = -1;

        for (CigarElement elem : record.getCigar().getCigarElements()) {
            CigarOperator op = elem.getOperator();
            int length = elem.getLength();

            if (op.consumesReferenceBases() && op.consumesReadBases()) {
                // Match/mismatch
                for (int i = 0; i < length; i++) {
                    if (refPos > refPosition) {
                        return closestQueryPos; //here you need to return: the subject locations and the query locations!
                    }
                    closestQueryPos = readPos;
                    refPos++;
                    readPos++;
                }
                System.out.println(op.name()+"\t"+closestQueryPos);
            } else if (op.consumesReferenceBases()) {
                // Deletion/skipped region
                for (int i = 0; i < length; i++) {
                    if (refPos > refPosition) {
                        return closestQueryPos;
                    }
                    refPos++;
                }
            } else if (op.consumesReadBases()) {
                // Insertion/soft clip
                readPos += length;
            }
        }

        return closestQueryPos;
    }


}


package main.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.junit.jupiter.api.Test;

import utils.CompareSequence;
import utils.CompareSequence.Type;
import utils.Subject;

public class HDRTest {

    private static final String QUERY =
        "GGCAGAGCCATCTATTGCTTACACTTTCTTCTGACATAACAGTGTTCACTAGCAACCTCAAACAGACACCATGGTGCATCTGACTCCTGAGGAGAAGACTGCTGTCAATGCCCTGTGGGGCAAAGTGAACGTGGATGCAGTTGGTGGTGAGGCCCTGGGCAGGTTGGTATCAAGGTTACAAGACAGGTTTAAGGAGACCAATA";

    @Test
    public void shouldNotClassifyAsHDR_whenQueryDoesNotMatchHDRTemplate() {
        CompareSequence cs = buildCompareSequence(
            new File("src/test/resources/hdr_hbd_wrong.txt"),
            QUERY
        );

        assertNotEquals(Type.HDR, cs.getType(), "Expected sequence NOT to be classified as HDR");
    }

    @Test
    public void shouldClassifyAsHDR_whenQueryMatchesHDRTemplate() {
        CompareSequence cs = buildCompareSequence(
            new File("src/test/resources/hdr_hbd_correct.txt"),
            QUERY
        );

        assertEquals(Type.HDR, cs.getType(), "Expected sequence to be classified as HDR");
    }

    private CompareSequence buildCompareSequence(File hdrFile, String query) {
        File ref = new File("src/test/resources/hbb.txt");

        String left = "GGCAAGGTGAACGTGGATGA";
        String right = "AGTTGGTGGTGAGGCCCTGG";
        String leftP = "ggcagagccatctattgcttA";
        String rightP = "tattggtctccttaaacctgtctt";

        ArrayList<RichSequence> hdrs = getHDRSequences(hdrFile);
        RichSequence refSeq = getRefSeq(ref);

        Subject subjectObject = new Subject(refSeq, left, right);
        subjectObject.setLeftPrimer(leftP);
        subjectObject.setRightPrimer(rightP);

        for (RichSequence hdrSeq : hdrs) {
            subjectObject.addHDR(hdrSeq);
        }

        CompareSequence cs = new CompareSequence(
            subjectObject,
            query.toUpperCase(),
            null,
            null,
            true,
            ""
        );

        cs.determineFlankPositions(false);
        return cs;
    }

    private RichSequence getRefSeq(File f) {
        ArrayList<RichSequence> seqs = getHDRSequences(f);
        assertFalse(seqs.isEmpty(), "Reference FASTA file is empty: " + f.getPath());
        return seqs.get(0);
    }

    private ArrayList<RichSequence> getHDRSequences(File f) {
        ArrayList<RichSequence> ret = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            RichSequenceIterator rsi = IOTools.readFastaDNA(reader, null);

            while (rsi.hasNext()) {
                ret.add(rsi.nextRichSequence());
            }

        } catch (Exception e) {
            fail("Failed to read FASTA file: " + f.getPath() + " -> " + e.getMessage());
        }

        return ret;
    }
}
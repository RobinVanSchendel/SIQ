package main.utils;

import org.biojava.bio.seq.DNATools;
import org.biojavax.bio.seq.RichSequence;
import org.junit.jupiter.api.Test;

import utils.Subject;

import static org.junit.jupiter.api.Assertions.*;

class SubjectTest {

    @Test
    void testConstructorSetsNameAndComments() throws Exception {
        RichSequence rs = RichSequence.Tools.createRichSequence("test1", DNATools.createDNA("ATGCGTACGTA"));
        Subject subj = new Subject(rs);

        assertEquals("test1", subj.getSubjectName());
        assertEquals("test1", subj.getSubjectComments());
        assertEquals("ATGCGTACGTA", subj.getString());
    }

    @Test
    void testLeftFlankSetting() throws Exception {
        RichSequence rs = RichSequence.Tools.createRichSequence("test2", DNATools.createDNA("AAATTTCCCGGG"));
        Subject subj = new Subject(rs, "AAATTT", "CCCGGG");
        assertFalse(subj.hasLeft());
    }

    @Test
    void testRightFlankSetting() throws Exception {
        RichSequence rs = RichSequence.Tools.createRichSequence("test3", DNATools.createDNA("AAATTTCCCGGG"));
        Subject subj = new Subject(rs, "AAATTT", "CCCGGG");
        assertFalse(subj.hasRight());
    }

    @Test
    void testLeftRightOverlapDetection() throws Exception {
        RichSequence rs = RichSequence.Tools.createRichSequence("test4", DNATools.createDNA("AAATTTCCCGGG"));
        Subject subj = new Subject(rs, "AAATTT", "TTCCCG");
        // should not allow overlapping
        assertFalse(subj.hasLeftRight());
    }

    @Test
    void testLongReadFlag() throws Exception {
        RichSequence rs = RichSequence.Tools.createRichSequence("test5", DNATools.createDNA("AAATTTCCCGGG"));
        Subject subj = new Subject(rs);
        assertFalse(subj.isLongRead());

        subj.setLongRead(true);
        assertTrue(subj.isLongRead());
    }
}
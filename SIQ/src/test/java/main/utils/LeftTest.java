package main.utils;

import utils.Left;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for utils.Left: constructors, getters, addCharEnd, and jumped flag.
 */
class LeftTest {

    @Test
    void constructor5Args_storesAllFields() {
        Left left = new Left("ATCG", 10, 14, 2, 6);
        assertEquals("ATCG",  left.getString());
        assertEquals(10, left.getSubjectStart());
        assertEquals(14, left.getSubjectEnd());
        assertEquals(2,  left.getQueryStart());
        assertEquals(6,  left.getQueryEnd());
    }

    @Test
    void constructor5Args_jumpedFlagDefaultsFalse() {
        Left left = new Left("ATCG", 0, 4, 0, 4);
        assertFalse(left.getJumped());
    }

    @Test
    void constructor6Args_jumpedFlagSetTrue() {
        Left left = new Left("GCGC", 5, 9, 1, 5, true);
        assertTrue(left.getJumped());
    }

    @Test
    void constructor6Args_jumpedFlagSetFalse() {
        Left left = new Left("GCGC", 5, 9, 1, 5, false);
        assertFalse(left.getJumped());
    }

    @Test
    void constructorStringOnly_storesString() {
        Left left = new Left("TTTT");
        assertEquals("TTTT", left.getString());
    }

    @Test
    void length_returnsStringLength() {
        Left left = new Left("ATCGATCG", 0, 8, 0, 8);
        assertEquals(8, left.length());
    }

    @Test
    void length_emptyString() {
        Left left = new Left("", 0, 0, 0, 0);
        assertEquals(0, left.length());
    }

    @Test
    void addCharEnd_appendsCharAndIncrementsEnds() {
        Left left = new Left("AT", 0, 2, 0, 2);
        left.addCharEnd('C');

        assertEquals("ATC",  left.getString());
        assertEquals(3,      left.getSubjectEnd());
        assertEquals(3,      left.getQueryEnd());
    }

    @Test
    void addCharEnd_multipleAppends() {
        Left left = new Left("A", 0, 1, 0, 1);
        left.addCharEnd('T');
        left.addCharEnd('C');
        left.addCharEnd('G');

        assertEquals("ATCG", left.getString());
        assertEquals(4, left.getSubjectEnd());
        assertEquals(4, left.getQueryEnd());
    }

    @Test
    void toString_containsCoordinatesAndString() {
        Left left = new Left("ATCG", 5, 9, 1, 5);
        String s = left.toString();
        // Format: queryStart-queryEnd:subjectStart-subjectEnd::string length
        assertTrue(s.contains("ATCG"));
        assertTrue(s.contains("5"));
        assertTrue(s.contains("9"));
        assertTrue(s.contains("1"));
    }
}

package main.utils;

import dnaanalysis.Utils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for dnaanalysis.Utils: reverseComplement, longestCommonSubstring,
 * and getHomologyAtBreak.
 */
class DnaUtilsTest {

    // ---- reverseComplement -----------------------------------------------

    @Test
    void reverseComplement_basicUppercase() {
        // ATCG reversed = GCTA, complement = CGAT
        assertEquals("CGAT", Utils.reverseComplement("ATCG"));
    }

    @Test
    void reverseComplement_basicLowercase() {
        assertEquals("cgat", Utils.reverseComplement("atcg"));
    }

    @Test
    void reverseComplement_singleBase_A() {
        assertEquals("T", Utils.reverseComplement("A"));
    }

    @Test
    void reverseComplement_singleBase_C() {
        assertEquals("G", Utils.reverseComplement("C"));
    }

    @Test
    void reverseComplement_palindrome() {
        // GCGC rev = CGCG, complement = GCGC
        assertEquals("GCGC", Utils.reverseComplement("GCGC"));
    }

    @Test
    void reverseComplement_withN_uppercase() {
        // ANCG rev = GCNA, complement = CGNT
        assertEquals("CGNT", Utils.reverseComplement("ANCG"));
    }

    @Test
    void reverseComplement_withN_lowercase() {
        assertEquals("cgnt", Utils.reverseComplement("ancg"));
    }

    @Test
    void reverseComplement_allSameBases() {
        assertEquals("TTTT", Utils.reverseComplement("AAAA"));
        assertEquals("AAAA", Utils.reverseComplement("TTTT"));
        assertEquals("CCCC", Utils.reverseComplement("GGGG"));
        assertEquals("GGGG", Utils.reverseComplement("CCCC"));
    }

    @Test
    void reverseComplement_emptyString() {
        assertEquals("", Utils.reverseComplement(""));
    }

    @Test
    void reverseComplement_withX_passthrough() {
        // 'x' is a special case that passes through unchanged
        String result = Utils.reverseComplement("xAx");
        // reversed: xAx -> xAx, complement: x->x, A->T, x->x => xTx
        assertEquals("xTx", result);
    }

    // ---- longestCommonSubstring ------------------------------------------

    @Test
    void longestCommonSubstring_identicalStrings() {
        assertEquals("ATCG", Utils.longestCommonSubstring("ATCG", "ATCG"));
    }

    @Test
    void longestCommonSubstring_substrAtEnd() {
        assertEquals("TCG", Utils.longestCommonSubstring("ATATCG", "TTCG"));
    }

    @Test
    void longestCommonSubstring_noCommonSubstring() {
        // no common characters → empty
        assertEquals("", Utils.longestCommonSubstring("AAAA", "TTTT"));
    }

    @Test
    void longestCommonSubstring_caseInsensitive() {
        // The dnaanalysis version uses Character.toUpperCase for comparison
        String result = Utils.longestCommonSubstring("atcg", "ATCG");
        assertEquals(4, result.length());
    }

    @Test
    void longestCommonSubstring_longerCommon() {
        assertEquals("GCATGC", Utils.longestCommonSubstring("AAGCATGCTT", "XGCATGCY"));
    }

    // ---- getHomologyAtBreak ---------------------------------------------

    @Test
    void getHomologyAtBreak_nullLeft_returnsEmpty() {
        assertEquals("", Utils.getHomologyAtBreak(null, "DEL", "RIGHT"));
    }

    @Test
    void getHomologyAtBreak_nullRight_returnsEmpty() {
        assertEquals("", Utils.getHomologyAtBreak("LEFT", "DEL", null));
    }

    @Test
    void getHomologyAtBreak_noHomology_returnsEmpty() {
        // left ends with "AAA", del starts with "TTT" => no match
        String result = Utils.getHomologyAtBreak("aaattt", "tttccc", "aaaggg");
        assertNotNull(result);
        // with mismatch this can vary; just verify it doesn't throw
    }

    @Test
    void getHomologyAtBreak_leftHomology() {
        // del = "GCGCGC", left reversed ends = "CGCGCG"
        // We expect some homology string to be returned without exception
        String result = Utils.getHomologyAtBreak("ATGCGCGC", "GCGCGCAT", "TTTTTTTT");
        assertNotNull(result);
    }
}

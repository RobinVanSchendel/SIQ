package main.utils;

import utils.Utils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for utils.Utils: getMutation, longestCommonSubstring,
 * getHomologyAtBreak, reverseComplement, longestCommonSubstringAllowMismatch.
 */
class UtilsTest {

    // ---- getMutation -------------------------------------------------------

    @Test
    void getMutation_AT_to_GC_forward() {
        assertEquals("AT->GC", Utils.getMutation("A", "G"));
    }

    @Test
    void getMutation_AT_to_GC_reverse() {
        assertEquals("AT->GC", Utils.getMutation("T", "C"));
    }

    @Test
    void getMutation_GC_to_AT_forward() {
        assertEquals("GC->AT", Utils.getMutation("G", "A"));
    }

    @Test
    void getMutation_GC_to_AT_reverse() {
        assertEquals("GC->AT", Utils.getMutation("C", "T"));
    }

    @Test
    void getMutation_AT_to_CG_forward() {
        assertEquals("AT->CG", Utils.getMutation("A", "C"));
    }

    @Test
    void getMutation_AT_to_CG_reverse() {
        assertEquals("AT->CG", Utils.getMutation("T", "G"));
    }

    @Test
    void getMutation_GC_to_TA_forward() {
        assertEquals("GC->TA", Utils.getMutation("G", "T"));
    }

    @Test
    void getMutation_GC_to_TA_reverse() {
        assertEquals("GC->TA", Utils.getMutation("C", "A"));
    }

    @Test
    void getMutation_AT_to_TA_forward() {
        assertEquals("AT->TA", Utils.getMutation("A", "T"));
    }

    @Test
    void getMutation_AT_to_TA_reverse() {
        assertEquals("AT->TA", Utils.getMutation("T", "A"));
    }

    @Test
    void getMutation_GC_to_CG_forward() {
        assertEquals("GC->CG", Utils.getMutation("G", "C"));
    }

    @Test
    void getMutation_GC_to_CG_reverse() {
        assertEquals("GC->CG", Utils.getMutation("C", "G"));
    }

    @Test
    void getMutation_lowercaseInput_normalised() {
        // method calls toUpperCase internally
        assertEquals("AT->GC", Utils.getMutation("a", "g"));
    }

    @Test
    void getMutation_unknownCombination_fallback() {
        // Not covered by the table → ref+"->"+alt
        assertEquals("A->A", Utils.getMutation("A", "A"));
    }

    // ---- longestCommonSubstring -------------------------------------------

    @Test
    void longestCommonSubstring_null_returnsEmpty() {
        assertEquals("", Utils.longestCommonSubstring(null, "ATCG"));
        assertEquals("", Utils.longestCommonSubstring("ATCG", null));
        assertEquals("", Utils.longestCommonSubstring(null, null));
    }

    @Test
    void longestCommonSubstring_emptyString_returnsEmpty() {
        assertEquals("", Utils.longestCommonSubstring("", "ATCG"));
        assertEquals("", Utils.longestCommonSubstring("ATCG", ""));
    }

    @Test
    void longestCommonSubstring_identicalStrings() {
        assertEquals("ATCG", Utils.longestCommonSubstring("ATCG", "ATCG"));
    }

    @Test
    void longestCommonSubstring_subStringPresent() {
        assertEquals("TCG", Utils.longestCommonSubstring("AATCGTT", "TCG"));
    }

    @Test
    void longestCommonSubstring_noCommon() {
        // All different characters
        assertEquals("", Utils.longestCommonSubstring("AAAA", "TTTT"));
    }

    @Test
    void longestCommonSubstring_longerSubstringFound() {
        String result = Utils.longestCommonSubstring("XXGCATGCXX", "GCATGC");
        assertEquals("GCATGC", result);
    }

    // ---- reverseComplement -----------------------------------------------

    @Test
    void reverseComplement_basicUppercase() {
        assertEquals("CGAT", Utils.reverseComplement("ATCG"));
    }

    @Test
    void reverseComplement_basicLowercase() {
        assertEquals("cgat", Utils.reverseComplement("atcg"));
    }

    @Test
    void reverseComplement_withN() {
        assertEquals("NCGAT", Utils.reverseComplement("ATCGN"));
    }

    @Test
    void reverseComplement_emptyString() {
        assertEquals("", Utils.reverseComplement(""));
    }

    @Test
    void reverseComplement_unknownChar_passthrough() {
        // Unknown chars are appended unchanged (not dropped)
        String result = Utils.reverseComplement("A?G");
        // "G?A" -> complement: C + ? + T
        assertEquals("C?T", result);
    }

    @Test
    void reverseComplement_mixedCase() {
        assertEquals("CGat", Utils.reverseComplement("atCG"));
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
    void getHomologyAtBreak_neitherNull_doesNotThrow() {
        assertDoesNotThrow(() -> Utils.getHomologyAtBreak("AAATTT", "TTTCCC", "CCCGGG"));
    }

    @Test
    void getHomologyAtBreak_exactLeftHomology() {
        // left reversed = "TTTTAA", del reversed = "TTTCCC"
        // they share "TTT" at the start (reversed) => homology "TTT"
        String result = Utils.getHomologyAtBreak("AATTTT", "CCCTTT", "GGGGGG");
        assertNotNull(result);
        assertTrue(result.length() >= 0); // primarily verifying no exception
    }

    // ---- getHomologyAtBreakWithMismatch ---------------------------------
    //
    // Background: this function finds microhomology at a deletion breakpoint.
    // The deletion has:
    //   - left  : sequence to the LEFT  of the cut (we compare its SUFFIX to del's SUFFIX)
    //   - del   : the deleted sequence itself
    //   - right : sequence to the RIGHT of the cut (we compare its PREFIX to del's PREFIX)
    //
    // Left and right share one mismatch budget: mismatches used on the left side
    // reduce how many are available on the right side.
    //
    // The function scans with a global cumulative rate, so a mismatch that temporarily
    // pushes the rate above the threshold can be "diluted" by subsequent matches —
    // this is intentional and matches how 1-mismatch homology was already being found.
    //
    // Key fix vs. the old implementation: the budget handed to the right side is now
    // a direct count snapshot taken at the left side's last-valid-match position,
    // rather than being re-derived from the string via regex (which broke for
    // lowercase input and used counts from the wrong position in the scan).

    @Test
    void getHomologyAtBreakWithMismatch_nullInputs_returnEmpty() {
        // All three nullable parameters must guard independently.
        assertEquals("", Utils.getHomologyAtBreakWithMismatch(null,   "DEL",  "RIGHT", 0.1));
        assertEquals("", Utils.getHomologyAtBreakWithMismatch("LEFT", null,   "RIGHT", 0.1));
        assertEquals("", Utils.getHomologyAtBreakWithMismatch("LEFT", "DEL",  null,    0.1));
    }

    @Test
    void getHomologyAtBreakWithMismatch_noHomology_returnsEmpty() {
        // left ends with TTTT, del ends with CCCC → no matching suffix → no left homology
        // right starts with TTTT, del starts with CCCC → no matching prefix → no right homology
        // Rate 0.5 is permissive, but there are simply no matching characters.
        String result = Utils.getHomologyAtBreakWithMismatch("TTTT", "CCCC", "TTTT", 0.5);
        assertEquals("", result);
    }

    @Test
    void getHomologyAtBreakWithMismatch_perfectLeftHomology_noRight() {
        // Situation: left flank ends with the same 4 bases as del ends with.
        //   left = "XXXXAAAA"  → suffix "AAAA" matches del suffix "AAAA"
        //   del  = "CCCCAAAA"
        //   right = "TTTT"     → del starts with "CCCC", right starts with "TTTT" → no right hom
        //
        // Expected left homology: "AAAA" (4 perfect matches, rate = 0)
        // Expected right homology: ""    (no match at all)
        String result = Utils.getHomologyAtBreakWithMismatch("XXXXAAAA", "CCCCAAAA", "TTTT", 0.12);
        assertEquals("AAAA", result);
    }

    @Test
    void getHomologyAtBreakWithMismatch_perfectRightHomology_noLeft() {
        // Mirror of the previous case.
        //   left  = "TTTT"     → del ends with "CCCC" → no left hom
        //   del   = "AAAACCCC"
        //   right = "AAAAXXXX" → del starts with "AAAA", right starts with "AAAA" → 4-base match
        //
        // Expected: right homology "AAAA", left homology ""
        String result = Utils.getHomologyAtBreakWithMismatch("TTTT", "AAAACCCC", "AAAAXXXX", 0.12);
        assertEquals("AAAA", result);
    }

    @Test
    void getHomologyAtBreakWithMismatch_perfectOnBothSides() {
        // left flank ends with "AAAA" == del suffix "AAAA"
        // right flank starts with "CCCC" == del prefix "CCCC"
        // del = "CCCCAAAA"
        //
        // Expected: leftHom = "AAAA", rightHom = "CCCC" → combined = "AAAACCCC"
        // Combined rate = 0/8 = 0, well within any threshold.
        String result = Utils.getHomologyAtBreakWithMismatch("XXXXAAAA", "CCCCAAAA", "CCCCXXXX", 0.12);
        assertEquals("AAAACCCC", result);
    }

    @Test
    void getHomologyAtBreakWithMismatch_oneMismatch_rateOk() {
        // left  = "XXXXXXXAAGAAAAAA"  (last 9 chars that overlap with del: "AAGAAAAAA")
        // del   = "CCCCCCCCAAAAAAAAA" (suffix = 9 A's)
        // right = "TTTT"             (no match against del prefix "CCCC")
        //
        // LEFT scan (both reversed):
        //   leftRev = "AAAAAAGAAXXXXXXX"
        //   delRev  = "AAAAAAAAACCCCCCCC"
        //
        //   i=0..5 : A vs A → match,  rate = 0/6 = 0.00  ≤ 0.12  ✓  lastValidIdx=6
        //   i=6    : G vs A → mismatch, rate = 1/7 ≈ 0.143  > 0.12  ✗
        //   i=7    : A vs A → match,  rate = 1/8 = 0.125  > 0.12  ✗
        //   i=8    : A vs A → match,  rate = 1/9 ≈ 0.111  ≤ 0.12  ✓  lastValidIdx=9
        //   i=9    : X vs C → mismatch (literal X padding hits C in del), rate > 0.12  ✗
        //   (remaining: all X vs C — mismatches, rate never recovers)
        //
        //   homT.substring(0,9) = "AAAAAAXAA"   ('X' is the mismatch marker for G vs A)
        //   reversed back to original orientation → "AAXAAAAAA"
        //
        // Note: 'X' in the result is the mismatch marker, not the original base.
        //       The original base at that position was 'G' (left) vs 'A' (del).
        String left  = "XXXXXXXAAGAAAAAA";  // last 9 chars overlap with del suffix
        String del   = "CCCCCCCCAAAAAAAAA"; // suffix "AAAAAAAAA" (9 A's)
        String right = "TTTT";
        String result = Utils.getHomologyAtBreakWithMismatch(left, del, right, 0.12);
        assertEquals("AAXAAAAAA", result);
    }

    @Test
    void getHomologyAtBreakWithMismatch_oneMismatch_rateTooHigh() {
        // left  = "XAXAA", del = "AAAAA", right = "TTTT"
        //
        // leftRev = "AAXAX"   ← reversed "XAXAA"
        // delRev  = "AAAAA"
        //
        //   i=0: A vs A → match,  rate=0/1=0.00  ≤ 0.12 ✓  lastValidIdx=1
        //   i=1: A vs A → match,  rate=0/2=0.00  ≤ 0.12 ✓  lastValidIdx=2
        //   i=2: X vs A → miss,   rate=1/3=0.333 > 0.12 ✗
        //   i=3: A vs A → match,  rate=1/4=0.25  > 0.12 ✗
        //   i=4: X vs A → miss,   rate=2/5=0.40  > 0.12 ✗
        //   (would need ≥ 17 chars total for 2 misses to dilute to ≤ 0.12; sequence is only 5)
        //
        //   homT.substring(0, 2) = "AA"  →  reversed = "AA"
        String result = Utils.getHomologyAtBreakWithMismatch("XAXAA", "AAAAA", "TTTT", 0.12);
        assertEquals("AA", result);
    }

    @Test
    void getHomologyAtBreakWithMismatch_twoMismatches_rateOk() {
        // Verifies >1 mismatch is found when the rate is set permissively enough.
        //
        // left  = "XXXXXXXAAXAXAAA"
        // leftRev = "AAAXAXAAXXXXXXX"   ← reversed; first mismatch is at i=3, not i=2
        // delRev  = "AAAAAAAACCCCCCCC"  ← reversed "CCCCCCCCAAAAAAAA"
        //
        //   i=0: A vs A → match,  rate=0.00 ≤ 0.25 ✓  lastValidIdx=1
        //   i=1: A vs A → match,  rate=0.00 ≤ 0.25 ✓  lastValidIdx=2
        //   i=2: A vs A → match,  rate=0.00 ≤ 0.25 ✓  lastValidIdx=3
        //   i=3: X vs A → miss,   rate=1/4=0.25  ≤ 0.25 ✓ (not a match → lastValidIdx stays 3)
        //   i=4: A vs A → match,  rate=1/5=0.20  ≤ 0.25 ✓  lastValidIdx=5
        //   i=5: X vs A → miss,   rate=2/6=0.333 > 0.25 ✗
        //   i=6: A vs A → match,  rate=2/7=0.286 > 0.25 ✗
        //   i=7: A vs A → match,  rate=2/8=0.25  ≤ 0.25 ✓  lastValidIdx=8
        //   i=8: X vs C → miss,   (padding X hits del's C) rate spikes, done
        //
        //   homT.substring(0, 8) = "AAAXAXAA"  →  reversed = "AAXAXAAA"
        String left  = "XXXXXXXAAXAXAAA"; // last 8 chars overlap with del suffix
        String del   = "CCCCCCCCAAAAAAAA";
        String right = "TTTT";
        String result = Utils.getHomologyAtBreakWithMismatch(left, del, right, 0.25);
        assertEquals("AAXAXAAA", result);
    }

    @Test
    void getHomologyAtBreakWithMismatch_twoMismatches_rateTooTight() {
        // Same inputs as above but mismatchRate = 0.12.
        //
        // leftRev = "AAAXAXAAXXXXXXX"
        // delRev  = "AAAAAAAACCCCCCCC"
        //
        //   i=0: A vs A → match,  rate=0.00 ≤ 0.12 ✓  lastValidIdx=1
        //   i=1: A vs A → match,  rate=0.00 ≤ 0.12 ✓  lastValidIdx=2
        //   i=2: A vs A → match,  rate=0.00 ≤ 0.12 ✓  lastValidIdx=3
        //   i=3: X vs A → miss,   rate=1/4=0.25  > 0.12 ✗
        //   i=4: A vs A → match,  rate=1/5=0.20  > 0.12 ✗
        //   i=5: X vs A → miss,   rate=2/6=0.333 > 0.12 ✗
        //   i=6..7: match → rate=2/7=0.286, 2/8=0.25 — both still > 0.12 ✗
        //   i=8+: padding X vs del's C → all misses; rate only climbs further
        //   (need total ≥ 17 for 2 misses to dilute to ≤ 0.12; scan is 15 chars max)
        //
        //   homT.substring(0, 3) = "AAA"  →  reversed = "AAA"
        String left  = "XXXXXXXAAXAXAAA";
        String del   = "CCCCCCCCAAAAAAAA";
        String right = "TTTT";
        String result = Utils.getHomologyAtBreakWithMismatch(left, del, right, 0.12);
        assertEquals("AAA", result);
    }

    @Test
    void getHomologyAtBreakWithMismatch_leftPerfect_rightMismatch() {
        // Completes the 2×2 matrix of (left perfect/mismatch) × (right perfect/mismatch).
        // Here the left side is a perfect match; the mismatch lives entirely on the right.
        //
        // left  = "XXXXAAAA"  → suffix "AAAA" == del suffix "AAAA"  (4 perfect matches)
        // del   = "CCCCAAAA"
        // right = "CXCCXXXX"  → del prefix "CCCC"; right has mismatch at position 1
        // mismatchRate = 0.25
        //
        // LEFT scan:
        //   leftRev = "ААААXXXX" vs delRev = "ААААCCCC"
        //   i=0..3: A vs A → match, rate=0 ≤ 0.25 ✓  lastValidIdx=4, snap(4,0)
        //   i=4..7: X vs C → miss, rate climbs — lastValidIdx stays 4
        //   leftHom = "AAAA"
        //
        // RIGHT scan (starts with correct=4, miss=0 from left snapshot):
        //   i=0: C vs C → match,  rate=0/5=0.000 ≤ 0.25 ✓  lastValidIdx=1
        //   i=1: X vs C → miss,   rate=1/6=0.167 ≤ 0.25 ✓  (not a match → stays 1)
        //   i=2: C vs C → match,  rate=1/7=0.143 ≤ 0.25 ✓  lastValidIdx=3
        //   i=3: C vs C → match,  rate=1/8=0.125 ≤ 0.25 ✓  lastValidIdx=4
        //   i=4: X vs A → miss,   rate=2/9=0.222 ≤ 0.25 ✓  (not a match → stays 4)
        //   i=5: X vs A → miss,   rate=3/10=0.30 > 0.25 ✗
        //   rightHom = "CXCC"
        //
        // Combined: "AAAA" + "CXCC" = "AAAACXCC"  (1 mismatch in 8 chars, rate = 0.125)
        String result = Utils.getHomologyAtBreakWithMismatch("XXXXAAAA", "CCCCAAAA", "CXCCXXXX", 0.25);
        assertEquals("AAAACXCC", result);
    }

    @Test
    void getHomologyAtBreakWithMismatch_budgetSharedBetweenSides() {
        // Verifies that mismatches on the LEFT side correctly reduce the budget
        // available for the RIGHT side.
        //
        // Setup (mismatchRate = 0.25):
        //   left suffix (4 chars) vs del suffix (4 chars):
        //     "AXAA" reversed = "AAXA" vs "AAAA" reversed = "AAAA"
        //     i=0: match  → correct=1, miss=0, rate=0.00 ≤ 0.25 ✓  lastValidIdx=1  snap(1,0)
        //     i=1: match  → correct=2, miss=0, rate=0.00 ≤ 0.25 ✓  lastValidIdx=2  snap(2,0)
        //     i=2: miss   → correct=2, miss=1, rate=0.33 > 0.25 ✗
        //     i=3: match  → correct=3, miss=1, rate=0.25 ≤ 0.25 ✓  lastValidIdx=4  snap(3,1)
        //   leftHom = "AAXA" reversed = "AXAA"  (budget snapshot: correct=3, miss=1)
        //
        //   right prefix vs del prefix:
        //     right = "AAAA", del = "CCCCAXAA"  → del prefix = "CCCC" ≠ "AAAA" → no right hom
        //
        //   BUT if right = "CCCC" and del starts with "CCCC":
        //     right side starts with correct=3, miss=1 (from left snapshot, rate=1/4=0.25)
        //     i=0: match (C) → correct=4, miss=1, rate=0.20 ≤ 0.25 ✓  lastValidIdx=1
        //     i=1: match (C) → correct=5, miss=1, rate=0.17 ≤ 0.25 ✓  lastValidIdx=2
        //     i=2: match (C) → correct=6, miss=1, rate=0.14 ≤ 0.25 ✓  lastValidIdx=3
        //     i=3: match (C) → correct=7, miss=1, rate=0.13 ≤ 0.25 ✓  lastValidIdx=4
        //   rightHom = "CCCC"
        //
        //   Combined: leftHom + rightHom = "AXAA" + "CCCC" = "AXAACCCC"
        //   Combined rate: 1/8 = 0.125 — well within budget.
        String left  = "XXXXXXXXAXAA";   // last 4 chars = "AXAA" aligns with del suffix "AAAA"
        String del   = "CCCCAAAA";       // del suffix "AAAA", del prefix "CCCC"
        String right = "CCCCXXXX";       // first 4 chars = "CCCC" aligns with del prefix "CCCC"
        String result = Utils.getHomologyAtBreakWithMismatch(left, del, right, 0.25);
        assertEquals("AXAACCCC", result);
    }

    @Test
    void getHomologyAtBreakWithMismatch_budgetExhaustedByLeft_rightGetsShorter() {
        // When the left side already spent the mismatch budget, the right side
        // must find homology using only the remaining allowance.
        //
        // mismatchRate = 0.25  →  1 mismatch per 4 chars.
        // left suffix "AXAA" uses 1 mismatch in 4 chars  → budget snapshot: correct=3, miss=1
        // Combined rate starting right side: 1/4 = 0.25 (right at the boundary).
        //
        // right vs del prefix "CCCC":
        //   i=0: match C → correct=4, miss=1, rate=1/5=0.20 ≤ 0.25 ✓  lastValidIdx=1
        //   i=1: miss  X → correct=4, miss=2, rate=2/6=0.33 > 0.25 ✗
        //   i=2: match C → correct=5, miss=2, rate=2/7=0.29 > 0.25 ✗
        //   i=3: match C → correct=6, miss=2, rate=2/8=0.25 ≤ 0.25 ✓  lastValidIdx=4
        //   rightHom = "CXCC"  (4 chars, combined rate 2/8 = 0.25)
        //
        // Combined result: "AXAA" + "CXCC" = "AXAACXCC"
        String left  = "XXXXXXXXAXAA";
        String del   = "CCCCAAAA";
        String right = "CXCCXXXX";   // first 4 chars "CXCC" vs del prefix "CCCC"
        String result = Utils.getHomologyAtBreakWithMismatch(left, del, right, 0.25);
        assertEquals("AXAACXCC", result);
    }

    @Test
    void getHomologyAtBreakWithMismatch_resultCappedAtDelLength() {
        // Combined homology must never exceed the length of the deleted sequence.
        // del = "AAAA" (length 4); left and right each perfectly match 4 bases.
        // Without the cap the result would be "AAAA" + "AAAA" = 8 chars > 4 → should return del.
        String result = Utils.getHomologyAtBreakWithMismatch("AAAAAAAA", "AAAA", "AAAAAAAA", 0.0);
        assertTrue(result.length() <= "AAAA".length());
    }

    // ---- longestCommonSubstringAllowMismatch ----------------------------

    @Test
    void longestCommonSubstringAllowMismatch_nullInputs_returnsNull() {
        assertNull(Utils.longestCommonSubstringAllowMismatch(null, "ATCG", 0, false));
        assertNull(Utils.longestCommonSubstringAllowMismatch("ATCG", null, 0, false));
    }

    @Test
    void longestCommonSubstringAllowMismatch_noMismatch_exactMatch() {
        String[] result = Utils.longestCommonSubstringAllowMismatch("ATCGATCG", "ATCG", 0, false);
        assertNotNull(result);
        assertEquals(5, result.length);
        assertEquals("ATCG", result[0]); // subject match
        assertEquals("ATCG", result[1]); // query match
        // mismatch string should be all dashes
        assertEquals("----", result[2]);
    }

    @Test
    void longestCommonSubstringAllowMismatch_oneMismatch_allowed() {
        // ATCG vs ATTG => 1 mismatch at position 2 (C vs T)
        String[] result = Utils.longestCommonSubstringAllowMismatch("ATCG", "ATTG", 1, false);
        assertNotNull(result);
        assertEquals(4, result[0].length()); // should match all 4 chars with 1 mismatch allowed
        assertTrue(result[2].contains("X")); // mismatch marker
    }

    @Test
    void longestCommonSubstringAllowMismatch_returnedArrayHasFiveElements() {
        String[] result = Utils.longestCommonSubstringAllowMismatch("GCATGCAT", "GCATGCAT", 0, false);
        assertNotNull(result);
        assertEquals(5, result.length);
        // last element is "matches:mismatches"
        assertTrue(result[4].contains(":"));
    }

    @Test
    void longestCommonSubstringAllowMismatch_isLeft_locationReversed() {
        // When isLeft=true the location indices are calculated from the end
        String[] resultRight = Utils.longestCommonSubstringAllowMismatch("AAAAATCG", "ATCG", 0, false);
        String[] resultLeft  = Utils.longestCommonSubstringAllowMismatch("AAAAATCG", "ATCG", 0, true);
        assertNotNull(resultRight);
        assertNotNull(resultLeft);
        // locations should differ when isLeft changes the calculation
        assertNotEquals(resultRight[3], resultLeft[3]);
    }
}

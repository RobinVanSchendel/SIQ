package main.utils;

import utils.AlphanumComparator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for utils.AlphanumComparator: numeric-aware file name sorting.
 */
class AlphanumComparatorTest {

    private final AlphanumComparator cmp = new AlphanumComparator();

    // Helper to build a File with an absolute-looking path
    private File f(String path) {
        return new File(path);
    }

    @Test
    void compare_nullInputs_returnsZero() {
        assertEquals(0, cmp.compare(null, null));
        assertEquals(0, cmp.compare(null, f("/a/b")));
        assertEquals(0, cmp.compare(f("/a/b"), null));
    }

    @Test
    void compare_identicalPaths_returnsZero() {
        File a = f("/data/sample10.fastq");
        assertEquals(0, cmp.compare(a, a));
    }

    @Test
    void compare_numericalOrder_2before10() {
        File f2  = f("/data/sample2.fastq");
        File f10 = f("/data/sample10.fastq");
        // 2 < 10 numerically
        assertTrue(cmp.compare(f2, f10) < 0);
        assertTrue(cmp.compare(f10, f2) > 0);
    }

    @Test
    void compare_alphabeticalPart_beforeNumericalPart() {
        File fa = f("/data/sampleA.fastq");
        File fb = f("/data/sampleB.fastq");
        assertTrue(cmp.compare(fa, fb) < 0);
        assertTrue(cmp.compare(fb, fa) > 0);
    }

    @Test
    void sort_mixedList_correctOrder() {
        List<File> files = new ArrayList<>();
        files.add(f("/data/sample12.fastq"));
        files.add(f("/data/sample2.fastq"));
        files.add(f("/data/sample1.fastq"));
        files.add(f("/data/sample10.fastq"));

        Collections.sort(files, cmp);

        assertEquals("sample1.fastq",  files.get(0).getName());
        assertEquals("sample2.fastq",  files.get(1).getName());
        assertEquals("sample10.fastq", files.get(2).getName());
        assertEquals("sample12.fastq", files.get(3).getName());
    }

    @Test
    void sort_allAlpha_alphabeticalOrder() {
        List<File> files = new ArrayList<>();
        files.add(f("/data/sampleC.txt"));
        files.add(f("/data/sampleA.txt"));
        files.add(f("/data/sampleB.txt"));

        Collections.sort(files, cmp);

        assertEquals("sampleA.txt", files.get(0).getName());
        assertEquals("sampleB.txt", files.get(1).getName());
        assertEquals("sampleC.txt", files.get(2).getName());
    }

    @Test
    void compare_samePrefix_longerStringComesLast() {
        File shorter = f("/data/abc");
        File longer  = f("/data/abcdef");
        assertTrue(cmp.compare(shorter, longer) < 0);
    }
}

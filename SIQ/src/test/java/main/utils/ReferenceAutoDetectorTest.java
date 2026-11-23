package main.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utils.ReferenceAutoDetector;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for utils.ReferenceAutoDetector (consensus read + flank suggestion, FASTA writing).
 */
class ReferenceAutoDetectorTest {

    @TempDir
    File tmp;

    /** A deterministic sequence of the requested length. */
    private static String seq(int len) {
        String unit = "ACGTTGCAAGCTTCGAGGATCCTAGGCATGCCATTGACGT";
        StringBuilder sb = new StringBuilder();
        while (sb.length() < len) sb.append(unit);
        return sb.substring(0, len);
    }

    private File fastq(String name, String... reads) throws IOException {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String r : reads) {
            sb.append("@read").append(i++).append('\n')
              .append(r).append('\n')
              .append("+\n")
              .append("I".repeat(r.length())).append('\n');
        }
        File f = new File(tmp, name);
        Files.write(f.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
        return f;
    }

    // ---- detect ------------------------------------------------------------

    @Test
    void detect_picksMostCommonSequence() throws IOException {
        String common = seq(100);
        String other = seq(90);
        File f = fastq("a.fastq", common, other, common, common, other);
        ReferenceAutoDetector.Result r = ReferenceAutoDetector.detect(f, 0);
        assertNotNull(r);
        assertEquals(common, r.sequence);
        assertEquals(3, r.readCount);
        assertEquals(5, r.totalSampled);
        assertEquals(0.6, r.getFrequency(), 1e-9);
    }

    @Test
    void detect_lowercaseReadsAreUppercasedAndCounted() throws IOException {
        String s = seq(80);
        File f = fastq("b.fastq", s.toLowerCase(), s);
        ReferenceAutoDetector.Result r = ReferenceAutoDetector.detect(f, 0);
        assertNotNull(r);
        assertEquals(s, r.sequence);
        assertEquals(2, r.readCount);
    }

    @Test
    void detect_emptyFile_returnsNull() throws IOException {
        File f = fastq("empty.fastq");
        assertNull(ReferenceAutoDetector.detect(f, 0));
    }

    @Test
    void detect_sequenceShorterThanMinimum_returnsNull() throws IOException {
        File f = fastq("short.fastq", seq(ReferenceAutoDetector.MIN_SEQUENCE_LENGTH - 1));
        assertNull(ReferenceAutoDetector.detect(f, 0));
    }

    @Test
    void detect_sequenceExactlyMinimum_isAccepted() throws IOException {
        String s = seq(ReferenceAutoDetector.MIN_SEQUENCE_LENGTH);
        ReferenceAutoDetector.Result r = ReferenceAutoDetector.detect(fastq("min.fastq", s), 0);
        assertNotNull(r);
        assertEquals(s, r.sequence);
    }

    @Test
    void detect_maxReadsLimitsSampling() throws IOException {
        String a = seq(100);
        String b = seq(70);
        // first two reads are b, the remaining three would make a the winner
        File f = fastq("max.fastq", b, b, a, a, a);
        ReferenceAutoDetector.Result r = ReferenceAutoDetector.detect(f, 2);
        assertNotNull(r);
        assertEquals(2, r.totalSampled);
        assertEquals(b, r.sequence);
        // maxReads <= 0 means all
        assertEquals(5, ReferenceAutoDetector.detect(f, 0).totalSampled);
        assertEquals(5, ReferenceAutoDetector.detect(f, -1).totalSampled);
        assertEquals(a, ReferenceAutoDetector.detect(f, 0).sequence);
    }

    @Test
    void detect_flanksHaveFlankLengthAndLieInSequence() throws IOException {
        for (int len : new int[] {50, 51, 55, 74, 75, 100, 150, 301}) {
            String s = seq(len);
            ReferenceAutoDetector.Result r = ReferenceAutoDetector.detect(fastq("f" + len + ".fastq", s), 0);
            assertNotNull(r, "len " + len);
            assertEquals(ReferenceAutoDetector.FLANK_LENGTH, r.leftFlank.length(), "left len " + len);
            assertEquals(ReferenceAutoDetector.FLANK_LENGTH, r.rightFlank.length(), "right len " + len);
            assertTrue(s.contains(r.leftFlank), "left in seq " + len);
            assertTrue(s.contains(r.rightFlank), "right in seq " + len);
        }
    }

    @Test
    void detect_flankPositionsAt35And60Percent_for100bp() throws IOException {
        String s = seq(100);
        ReferenceAutoDetector.Result r = ReferenceAutoDetector.detect(fastq("p.fastq", s), 0);
        assertEquals(s.substring(35, 60), r.leftFlank);
        assertEquals(s.substring(60, 85), r.rightFlank);
    }

    @Test
    void detect_rightFlankClampedToEnd_for60bp() throws IOException {
        // 60 bp: rightStart 36 -> 35 would fit? 36+25=61 > 60, so clamped to 35; leftStart 21
        String s = seq(60);
        ReferenceAutoDetector.Result r = ReferenceAutoDetector.detect(fastq("c60.fastq", s), 0);
        assertEquals(s.substring(35, 60), r.rightFlank);
        assertEquals(s.substring(5, 30), r.leftFlank); // 21+25 > 35 -> 35-25-5 = 5
    }

    @Test
    void detect_minimumLength50_flanksClampedToStartAndEnd() throws IOException {
        // 50 bp: rightStart 30 -> 25 (must fit), leftStart 17 -> -5 -> clamped to 0
        String s = seq(50);
        ReferenceAutoDetector.Result r = ReferenceAutoDetector.detect(fastq("m.fastq", s), 0);
        assertEquals(s.substring(0, 25), r.leftFlank);
        assertEquals(s.substring(25, 50), r.rightFlank);
    }

    @Test
    void detect_gzippedFastq() throws IOException {
        String s = seq(80);
        File plain = fastq("z.fastq", s, s);
        File gz = new File(tmp, "z.fastq.gz");
        try (java.util.zip.GZIPOutputStream out = new java.util.zip.GZIPOutputStream(Files.newOutputStream(gz.toPath()))) {
            out.write(Files.readAllBytes(plain.toPath()));
        }
        ReferenceAutoDetector.Result r = ReferenceAutoDetector.detect(gz, 0);
        assertNotNull(r);
        assertEquals(s, r.sequence);
        assertEquals(2, r.readCount);
    }

    @Test
    void detect_malformedFastq_throws() throws IOException {
        File f = new File(tmp, "bad.fastq");
        Files.write(f.toPath(), "this is not a fastq file\nat all\n".getBytes(StandardCharsets.UTF_8));
        assertThrows(Exception.class, () -> ReferenceAutoDetector.detect(f, 0));
    }

    @Test
    void detect_truncatedFastq_throws() throws IOException {
        File f = new File(tmp, "trunc.fastq");
        Files.write(f.toPath(), ("@r1\n" + seq(80) + "\n+\n").getBytes(StandardCharsets.UTF_8));
        assertThrows(Exception.class, () -> ReferenceAutoDetector.detect(f, 0));
    }

    @Test
    void detect_missingFile_throws() {
        File f = new File(tmp, "does_not_exist.fastq");
        assertThrows(Exception.class, () -> ReferenceAutoDetector.detect(f, 0));
    }

    // ---- Result ------------------------------------------------------------

    @Test
    void result_frequency_zeroSampled_isZero() {
        ReferenceAutoDetector.Result r = new ReferenceAutoDetector.Result("A", "A", "A", 0, 0);
        assertEquals(0.0, r.getFrequency());
    }

    @Test
    void result_frequencyString_oneDecimalPercent() {
        ReferenceAutoDetector.Result r = new ReferenceAutoDetector.Result("A", "A", "A", 1, 3);
        // String.format is locale dependent (decimal comma); accept either separator
        assertTrue(r.getFrequencyString().matches("33[.,]3%"), r.getFrequencyString());
        assertTrue(new ReferenceAutoDetector.Result("A", "A", "A", 5, 5).getFrequencyString().matches("100[.,]0%"));
    }

    // ---- writeFasta --------------------------------------------------------

    @Test
    void writeFasta_stripsFastqGzExtension() throws IOException {
        File r1 = new File(tmp, "sample_R1.fastq.gz");
        File fa = ReferenceAutoDetector.writeFasta(r1, "ACGT");
        assertEquals("sample_R1_auto_reference.fasta", fa.getName());
        assertEquals(tmp, fa.getParentFile());
        List<String> lines = Files.readAllLines(fa.toPath());
        assertEquals(List.of(">auto_reference_sample_R1", "ACGT"), lines);
    }

    @Test
    void writeFasta_stripsOtherExtensions() throws IOException {
        for (String ext : new String[] {".fastq", ".fq.gz", ".fq"}) {
            File fa = ReferenceAutoDetector.writeFasta(new File(tmp, "x" + ext), "AC");
            assertEquals("x_auto_reference.fasta", fa.getName(), ext);
        }
    }

    @Test
    void writeFasta_unknownExtension_kept() throws IOException {
        File fa = ReferenceAutoDetector.writeFasta(new File(tmp, "x.txt"), "AC");
        assertEquals("x.txt_auto_reference.fasta", fa.getName());
    }

    @Test
    void writeFasta_overwritesExisting() throws IOException {
        File r1 = new File(tmp, "o.fastq");
        ReferenceAutoDetector.writeFasta(r1, "AAAA");
        File fa = ReferenceAutoDetector.writeFasta(r1, "CC");
        assertEquals(List.of(">auto_reference_o", "CC"), Files.readAllLines(fa.toPath()));
    }
}

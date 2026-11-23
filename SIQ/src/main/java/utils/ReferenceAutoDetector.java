package utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.jcvi.jillion.core.datastore.DataStoreProviderHint;
import org.jcvi.jillion.core.util.iter.StreamingIterator;
import org.jcvi.jillion.trace.fastq.FastqDataStore;
import org.jcvi.jillion.trace.fastq.FastqFileDataStoreBuilder;
import org.jcvi.jillion.trace.fastq.FastqQualityCodec;
import org.jcvi.jillion.trace.fastq.FastqRecord;

public class ReferenceAutoDetector {

    public static final int DEFAULT_MAX_READS = 10_000;
    public static final int FLANK_LENGTH = 25;
    public static final int MIN_SEQUENCE_LENGTH = 50;

    public static class Result {
        public final String sequence;
        public final String leftFlank;
        public final String rightFlank;
        public final int readCount;
        public final int totalSampled;

        public Result(String sequence, String leftFlank, String rightFlank, int readCount, int totalSampled) {
            this.sequence = sequence;
            this.leftFlank = leftFlank;
            this.rightFlank = rightFlank;
            this.readCount = readCount;
            this.totalSampled = totalSampled;
        }

        public double getFrequency() {
            if (totalSampled == 0) return 0;
            return (double) readCount / totalSampled;
        }

        public String getFrequencyString() {
            return String.format("%.1f%%", getFrequency() * 100);
        }
    }

    /**
     * Detect the most common read sequence from an R1 FASTQ file and suggest
     * left/right flanks placed at ~35% and ~60% of the sequence length.
     *
     * @param r1       R1 FASTQ file (may be .gz)
     * @param maxReads maximum number of reads to sample (0 = all)
     * @return Result with consensus sequence and suggested flanks, or null if detection failed
     */
    public static Result detect(File r1, int maxReads) throws IOException {
        HashMap<String, Integer> counts = new HashMap<>();
        int total = 0;

        FastqDataStore datastore = new FastqFileDataStoreBuilder(r1)
                .qualityCodec(FastqQualityCodec.SANGER)
                .hint(DataStoreProviderHint.ITERATION_ONLY)
                .build();

        try (StreamingIterator<FastqRecord> it = datastore.iterator()) {
            while (it.hasNext() && (maxReads <= 0 || total < maxReads)) {
                FastqRecord record = it.next();
                String seq = record.getNucleotideSequence().toString().toUpperCase();
                counts.merge(seq, 1, Integer::sum);
                total++;
            }
        }

        if (counts.isEmpty()) return null;

        String best = null;
        int bestCount = 0;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (e.getValue() > bestCount) {
                bestCount = e.getValue();
                best = e.getKey();
            }
        }

        if (best == null || best.length() < MIN_SEQUENCE_LENGTH) return null;

        int len = best.length();
        // Place leftFlank at 35% and rightFlank at 60% of the sequence
        int leftStart = (int) (len * 0.35);
        int rightStart = (int) (len * 0.60);

        // Ensure rightFlank fits and does not overlap leftFlank
        if (rightStart + FLANK_LENGTH > len) {
            rightStart = len - FLANK_LENGTH;
        }
        if (leftStart + FLANK_LENGTH > rightStart) {
            leftStart = rightStart - FLANK_LENGTH - 5;
        }
        if (leftStart < 0) leftStart = 0;

        String leftFlank = best.substring(leftStart, leftStart + FLANK_LENGTH);
        String rightFlank = best.substring(rightStart, rightStart + FLANK_LENGTH);

        return new Result(best, leftFlank, rightFlank, bestCount, total);
    }

    /**
     * Write the consensus sequence as a FASTA file next to the R1 file.
     *
     * @param r1       R1 file — used to determine output directory and base name
     * @param sequence the consensus sequence to write
     * @return the written FASTA file
     */
    public static File writeFasta(File r1, String sequence) throws IOException {
        String baseName = r1.getName()
                .replaceAll("\\.fastq\\.gz$", "")
                .replaceAll("\\.fastq$", "")
                .replaceAll("\\.fq\\.gz$", "")
                .replaceAll("\\.fq$", "");
        File fasta = new File(r1.getParentFile(), baseName + "_auto_reference.fasta");
        try (PrintWriter pw = new PrintWriter(fasta)) {
            pw.println(">auto_reference_" + baseName);
            pw.println(sequence);
        }
        return fasta;
    }
}

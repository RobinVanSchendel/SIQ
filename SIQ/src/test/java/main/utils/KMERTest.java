package main.utils;

import utils.KMER;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for utils.KMER: construction, location tracking, and toString.
 */
class KMERTest {

    @Test
    void constructor_setsInitialLocation() {
        KMER kmer = new KMER("ATCGATCG", 0);
        ArrayList<Integer> locs = kmer.getLocation();
        assertNotNull(locs);
        assertEquals(1, locs.size());
        assertEquals(0, locs.get(0));
    }

    @Test
    void addLocation_appendsNewLocation() {
        KMER kmer = new KMER("GCGCGCGC", 5);
        kmer.addLocation(42);
        kmer.addLocation(100);

        ArrayList<Integer> locs = kmer.getLocation();
        assertEquals(3, locs.size());
        assertEquals(5,   locs.get(0));
        assertEquals(42,  locs.get(1));
        assertEquals(100, locs.get(2));
    }

    @Test
    void getLocation_returnsLiveMutableList() {
        KMER kmer = new KMER("TTTT", 10);
        kmer.addLocation(20);

        // The list returned should reflect the current state
        assertEquals(2, kmer.getLocation().size());
    }

    @Test
    void toString_containsSequenceAndLocations() {
        KMER kmer = new KMER("ATCG", 7);
        kmer.addLocation(14);
        String s = kmer.toString();
        assertTrue(s.contains("ATCG"));
        assertTrue(s.contains("7"));
        assertTrue(s.contains("14"));
    }

    @Test
    void constructor_zeroIndex() {
        KMER kmer = new KMER("AAAA", 0);
        assertEquals(0, kmer.getLocation().get(0));
    }

    @Test
    void addLocation_multipleLocations_preservesOrder() {
        KMER kmer = new KMER("GCGC", 1);
        for (int i = 2; i <= 10; i++) {
            kmer.addLocation(i);
        }
        ArrayList<Integer> locs = kmer.getLocation();
        assertEquals(10, locs.size());
        for (int i = 0; i < locs.size(); i++) {
            assertEquals(i + 1, locs.get(i));
        }
    }
}

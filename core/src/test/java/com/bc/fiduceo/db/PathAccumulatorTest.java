package com.bc.fiduceo.db;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PathAccumulatorTest {

    private PathAccumulator pathAccumulator;

    @Before
    public void setUp() {
        pathAccumulator = new PathAccumulator("/root/path/to/exchange", 3);
    }

    @Test
    public void test_accumulate_nothing() {
        final PathCount match = pathAccumulator.getMatches();
        assertEquals("/root/path/to/exchange", match.getPath());
        assertEquals(0, match.getCount());

        final List<PathCount> misses = pathAccumulator.getMisses();
        assertEquals(0, misses.size());
    }

    @Test
    public void test_accumulate_addMatches() {
        pathAccumulator.addMatch();
        pathAccumulator.addMatch();

        final PathCount match = pathAccumulator.getMatches();
        assertEquals("/root/path/to/exchange", match.getPath());
        assertEquals(2, match.getCount());

        final List<PathCount> misses = pathAccumulator.getMisses();
        assertEquals(0, misses.size());
    }

    @Test
    public void test_accumulate_addMisses_onePath() {
        final String sep = File.separator;
        pathAccumulator.addMiss(sep + "root" + sep + "path" + sep + "different" + sep + "file_1");
        pathAccumulator.addMiss(sep + "root" + sep + "path" + sep + "different" + sep + "file_2");
        pathAccumulator.addMiss(sep + "root" + sep + "path" + sep + "different" + sep + "file_3");

        final PathCount match = pathAccumulator.getMatches();
        assertEquals(0, match.getCount());

        final List<PathCount> misses = pathAccumulator.getMisses();
        assertEquals(1, misses.size());

        final PathCount pathMatch = misses.get(0);
        assertEquals(sep + "root" + sep + "path" + sep + "different", pathMatch.getPath());
        assertEquals(3, pathMatch.getCount());
    }

    @Test
    public void test_accumulate_addMisses_twoPaths() {
        final String sep = File.separator;
        pathAccumulator.addMiss(sep + "root" + sep + "path" + sep + "different" + sep + "file_1");
        pathAccumulator.addMiss(sep + "root" + sep + "path" + sep + "other" + sep + "file_2");
        pathAccumulator.addMiss(sep + "root" + sep + "path" + sep + "other" + sep + "file_3");
        pathAccumulator.addMiss(sep + "root" + sep + "path" + sep + "different" + sep + "file_4");

        final PathCount match = pathAccumulator.getMatches();
        assertEquals(0, match.getCount());

        final List<PathCount> misses = pathAccumulator.getMisses();
        assertEquals(2, misses.size());

        PathCount pathMatch = misses.get(0);
        assertEquals(sep + "root" + sep + "path" + sep + "different", pathMatch.getPath());
        assertEquals(2, pathMatch.getCount());

        pathMatch = misses.get(1);
        assertEquals(sep + "root" + sep + "path" + sep + "other", pathMatch.getPath());
        assertEquals(2, pathMatch.getCount());
    }

    @Test
    public void testStripPath() {
        final String sep = File.separator;
        final String path = sep + "root" + sep + "sub_1" + sep + "sub_2" + sep + "file";

        String stripped = PathAccumulator.stripPath(path, 1);
        assertEquals(sep + "root", stripped);

        stripped = PathAccumulator.stripPath(path, 2);
        assertEquals(sep + "root" + sep + "sub_1", stripped);

        stripped = PathAccumulator.stripPath(path, 3);
        assertEquals(sep + "root" + sep + "sub_1" + sep + "sub_2", stripped);
    }

    @Test
    public void testStripPath_borderCases() {
        final String sep = File.separator;
        final String path = sep + "root" + sep + "sub_1" + sep + "sub_2" + sep + "file";

        String stripped = PathAccumulator.stripPath(path, 0);
        assertEquals("", stripped);

        stripped = PathAccumulator.stripPath(path, 5);
        assertEquals(path, stripped);
    }
}

package com.bc.fiduceo.qc;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class MatchupAccumulatorTest {

    private MatchupAccumulator accumulator;

    @Before
    public void setUp() {
        accumulator = new MatchupAccumulator();
    }

    @Test
    public void testGetDaysMap_empty() {
        final HashMap<String, Integer> daysMap = accumulator.getDaysMap();
        assertEquals(0, daysMap.size());
    }

    @Test
    public void testGetSummaryCount_empty() {
        assertEquals(0, accumulator.getSummaryCount());
    }

    @Test
    public void testGetFileCount_empty() {
        assertEquals(0, accumulator.getFileCount());
    }

    @Test
    public void testAdd_one() {
        accumulator.add(1676332800);

        final HashMap<String, Integer> daysMap = accumulator.getDaysMap();
        assertEquals(1, daysMap.size());
        assertEquals(1, daysMap.get("2023-02-14").intValue());

        assertEquals(1, accumulator.getSummaryCount());
    }

    @Test
    public void testAdd_three_sameDay() {
        accumulator.add(1676332800);
        accumulator.add(1676332810);
        accumulator.add(1676332850);

        final HashMap<String, Integer> daysMap = accumulator.getDaysMap();
        assertEquals(1, daysMap.size());
        assertEquals(3, daysMap.get("2023-02-14").intValue());

        assertEquals(3, accumulator.getSummaryCount());
    }

    @Test
    public void testAdd_five_differentDays() {
        accumulator.add(1676332800);
        accumulator.add(1676332810);
        accumulator.add(1676332850);
        accumulator.add(1676246400);
        accumulator.add(1676246500);

        final HashMap<String, Integer> daysMap = accumulator.getDaysMap();
        assertEquals(2, daysMap.size());
        assertEquals(2, daysMap.get("2023-02-13").intValue());
        assertEquals(3, daysMap.get("2023-02-14").intValue());

        assertEquals(5, accumulator.getSummaryCount());
    }

    @Test
    public void testCountFile() {
        accumulator.countFile();
        accumulator.countFile();
        accumulator.countFile();

        assertEquals(3, accumulator.getFileCount());
    }
}

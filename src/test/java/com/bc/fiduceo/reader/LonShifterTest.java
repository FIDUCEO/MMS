package com.bc.fiduceo.reader;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LonShifterTest {

    @Test
    public void testFilter() {
        final LonShifter lonShifter = new LonShifter(56.0);


        Coordinate coordinate = new Coordinate(10, 45);
        lonShifter.filter(coordinate);
        assertEquals(66.0, coordinate.x, 1e-8);


        coordinate = new Coordinate(-66, 45);
        lonShifter.filter(coordinate);
        assertEquals(-10.0, coordinate.x, 1e-8);
    }
}

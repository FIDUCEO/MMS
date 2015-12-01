package com.bc.fiduceo.geometry.jts;


import com.vividsolutions.jts.geom.Coordinate;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class JTSPointTest {

    @Test
    public void testGetInner() {
        final Coordinate coordinate = new Coordinate(23, 34);

        final JTSPoint jtsPoint = new JTSPoint(coordinate);

        final Object inner = jtsPoint.getInner();
        assertSame(coordinate, inner);
    }
}

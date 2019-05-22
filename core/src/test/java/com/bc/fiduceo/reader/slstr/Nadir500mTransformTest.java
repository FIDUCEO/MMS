package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import org.junit.Test;
import ucar.ma2.Array;

import static org.junit.Assert.assertEquals;

public class Nadir500mTransformTest {

    @Test
    public void testGetRasterSize() {
        final Nadir500mTransform transform = new Nadir500mTransform(310, 122);

        final Dimension rasterSize = transform.getRasterSize();
        assertEquals(310, rasterSize.getNx());
        assertEquals(122, rasterSize.getNy());
    }

    @Test
    public void testMapCoordinate() {
        final Nadir500mTransform transform = new Nadir500mTransform(200, 180);

        assertEquals(248, transform.mapCoordinate(124));
        assertEquals(7600, transform.mapCoordinate(3800));
    }

    @Test
    public void testMapInterval() {
        final Nadir500mTransform transform = new Nadir500mTransform(201, 181);

        final Interval interval = new Interval(3, 5);
        final Interval mapped = transform.mapInterval(interval);

        assertEquals(6, mapped.getX());
        assertEquals(10, mapped.getY());
    }

    @Test
    public void testProcess_1x1_noFills() {
        final Nadir500mTransform transform = new Nadir500mTransform(201, 181);
        final float[][] data = new float[][]{{0.f, 1.f}, {2.f, 3.f}};

        final Array array = Array.factory(data);
        final Array processed = transform.process(array, -1.0);

        assertEquals("(1,1)", processed.shapeToString());
        assertEquals(1.5, processed.getDouble(0), 1e-8);
    }
}

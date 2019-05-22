package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import org.junit.Test;
import ucar.ma2.Array;

import static org.junit.Assert.assertEquals;

public class Nadir1kmTransformTest {

    @Test
    public void testGetRasterSize() {
        final Nadir1kmTransform transform = new Nadir1kmTransform(200, 180);

        final Dimension rasterSize = transform.getRasterSize();
        assertEquals(100, rasterSize.getNx());
        assertEquals(90, rasterSize.getNy());
    }

    @Test
    public void testMapCoordinate() {
        final Nadir1kmTransform transform = new Nadir1kmTransform(200, 180);

        assertEquals(123, transform.mapCoordinate(123));
        assertEquals(4000, transform.mapCoordinate(4000));
    }

    @Test
    public void testMapInterval() {
        final Nadir1kmTransform transform = new Nadir1kmTransform(201, 181);

        final Interval interval = new Interval(5, 3);
        final Interval mapped = transform.mapInterval(interval);

        assertEquals(5, mapped.getX());
        assertEquals(3, mapped.getY());
    }

    @Test
    public void testProcess() {
        final Nadir1kmTransform transform = new Nadir1kmTransform(201, 181);
        final float[] data = new float[]{0.f, 1.f, 2.f, 3.f, 4.f, 5.f};

        final Array array = Array.factory(data);
        final Array processed = transform.process(array, -1.0);

        assertEquals(array.shapeToString(), processed.shapeToString());
        assertEquals(0.f, processed.getFloat(0), 1e-8);
        assertEquals(2.f, processed.getFloat(2), 1e-8);
    }
}

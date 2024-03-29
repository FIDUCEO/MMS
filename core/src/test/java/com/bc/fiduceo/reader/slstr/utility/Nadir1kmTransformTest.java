package com.bc.fiduceo.reader.slstr.utility;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.slstr.utility.Nadir1kmTransform;
import com.bc.fiduceo.util.NetCDFUtils;
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
    public void testMapCoordinate_XY() {
        final Nadir1kmTransform transform = new Nadir1kmTransform(200, 180);

        assertEquals(123, transform.mapCoordinate_X(123), 1e-8);
        assertEquals(4000, transform.mapCoordinate_Y(4000), 1e-8);
    }

    @Test
    public void testInverseCoordinate_XY() {
        final Nadir1kmTransform transform = new Nadir1kmTransform(200, 180);

        assertEquals(123, transform.inverseCoordinate_X(123), 1e-8);
        assertEquals(4000, transform.inverseCoordinate_X(4000), 1e-8);
    }

    @Test
    public void testGetOffset() {
        final Nadir1kmTransform transform = new Nadir1kmTransform(201, 181);

        assertEquals(0, transform.getOffset());
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

        final Array array = NetCDFUtils.create(data);
        final Array processed = transform.process(array, -1.0);

        assertEquals(array.shapeToString(), processed.shapeToString());
        assertEquals(0.f, processed.getFloat(0), 1e-8);
        assertEquals(2.f, processed.getFloat(2), 1e-8);
    }

    @Test
    public void testProcessFlags()  {
        final Nadir1kmTransform transform = new Nadir1kmTransform(202, 182);
        final int[] data = new int[]{0, 1, 2, 4, 8, 16};

        final Array array = NetCDFUtils.create(data);
        final Array processed = transform.processFlags(array, -1);

        assertEquals(array.shapeToString(), processed.shapeToString());
        assertEquals(1, processed.getInt(1));
        assertEquals(4, processed.getInt(3));
    }
}

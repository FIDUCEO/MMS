package com.bc.fiduceo.reader.slstr.utility;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.slstr.utility.Oblique1kmTransform;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Test;
import ucar.ma2.Array;

import static org.junit.Assert.assertEquals;

public class Oblique1kmTransformTest {

    @Test
    public void testGetRasterSize() {
        final Oblique1kmTransform transform = new Oblique1kmTransform(2000, 1800, 256);

        final Dimension rasterSize = transform.getRasterSize();
        assertEquals(400, rasterSize.getNx());
        assertEquals(900, rasterSize.getNy());
    }

    @Test
    public void testMapCoordinate_XY() {
        final Oblique1kmTransform transform = new Oblique1kmTransform(2000, 1800, 548);

        assertEquals(-425, transform.mapCoordinate_X(123), 1e-8);
        assertEquals(4000, transform.mapCoordinate_Y(4000), 1e-8);
    }

    @Test
    public void testInverseCoordinate_XY() {
        final Oblique1kmTransform transform = new Oblique1kmTransform(2000, 1800, 548);

        assertEquals(123, transform.inverseCoordinate_X(-425), 1e-8);
        assertEquals(4000, transform.inverseCoordinate_Y(4000), 1e-8);
    }

    @Test
    public void testGetOffset() {
        final Oblique1kmTransform transform = new Oblique1kmTransform(202, 182, 549);

        assertEquals(0, transform.getOffset());
    }

    @Test
    public void testMapInterval() {
        final Oblique1kmTransform transform = new Oblique1kmTransform(203, 183, 550);

        final Interval interval = new Interval(3, 5);
        final Interval mapped = transform.mapInterval(interval);

        assertEquals(3, mapped.getX());
        assertEquals(5, mapped.getY());
    }

    @Test
    public void testProcess() {
        final Oblique1kmTransform transform = new Oblique1kmTransform(204, 184, 551);
        final float[] data = new float[]{0.f, 1.f, 2.f, 3.f, 4.f, 5.f};

        final Array array = NetCDFUtils.create(data);
        final Array processed = transform.process(array, -1.0);

        assertEquals(array.shapeToString(), processed.shapeToString());
        assertEquals(0.f, processed.getFloat(0), 1e-8);
        assertEquals(2.f, processed.getFloat(2), 1e-8);
    }

    @Test
    public void testProcessFlags()  {
        final Oblique1kmTransform transform = new Oblique1kmTransform(205, 185, 552);
        final int[] data = new int[]{0, 1, 2, 4, 8, 16};

        final Array array = NetCDFUtils.create(data);
        final Array processed = transform.processFlags(array, -1);

        assertEquals(array.shapeToString(), processed.shapeToString());
        assertEquals(1, processed.getInt(1));
        assertEquals(4, processed.getInt(3));
    }
}

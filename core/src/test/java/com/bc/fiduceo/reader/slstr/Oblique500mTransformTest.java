package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import static org.junit.Assert.assertEquals;

public class Oblique500mTransformTest {

    @Test
    public void testGetRasterSize() {
        final Oblique500mTransform transform = new Oblique500mTransform(3000, 2400, 422);

        final Dimension rasterSize = transform.getRasterSize();
        assertEquals(900, rasterSize.getNx());
        assertEquals(1200, rasterSize.getNy());
    }

    @Test
    public void testMapCoordinate_XY() {
        final Oblique500mTransform transform = new Oblique500mTransform(3000, 2400, 423);

        assertEquals(1154, transform.mapCoordinate_X(1000));
        assertEquals(2800, transform.mapCoordinate_Y(1400));
    }

    @Test
    public void testGetOffset_XY() {
        final Oblique500mTransform transform = new Oblique500mTransform(3000, 2400, 423);

        assertEquals(1, transform.getOffset_X());
        assertEquals(1, transform.getOffset_Y());
    }

    @Test
    public void testMapInterval() {
        final Oblique500mTransform transform = new Oblique500mTransform(3000, 2400, 423);

        final Interval interval = new Interval(5, 7);
        final Interval mapped = transform.mapInterval(interval);

        assertEquals(10, mapped.getX());
        assertEquals(14, mapped.getY());
    }

    @Test
    public void testProcess_1x1_noFills() throws InvalidRangeException {
        final Oblique500mTransform transform = new Oblique500mTransform(3000, 2400, 423);
        final float[][] data = new float[][]{{2.f, 3.f}, {4.f, 5.f}};

        final Array array = Array.factory(data);
        final Array processed = transform.process(array, -1.0);

        assertEquals("(1,1)", processed.shapeToString());
        assertEquals(3.5, processed.getDouble(0), 1e-8);
    }

    @Test
    public void testProcess_1x1_fills() throws InvalidRangeException {
        final Oblique500mTransform transform = new Oblique500mTransform(3000, 2400, 423);
        final float[][] data = new float[][]{{0.f, -4.f}, {1.f, 2.f}};

        final Array array = Array.factory(data);
        final Array processed = transform.process(array, -4.0);

        assertEquals("(1,1)", processed.shapeToString());
        assertEquals(1.0, processed.getDouble(0), 1e-8);
    }

    @Test
    public void testProcess_3x1_noFills() throws InvalidRangeException {
        final Oblique500mTransform transform = new Oblique500mTransform(3000, 2400, 423);
        final float[][] data = new float[][]{{2.f, 3.f, 4.f, 5.f, 6.f, 7.f},
                {8.f, 9.f, 10.f, 11.f, 12.f, 13.f}};

        final Array array = Array.factory(data);
        final Array processed = transform.process(array, -2.0);

        assertEquals("(1,3)", processed.shapeToString());
        assertEquals(5.5, processed.getDouble(0), 1e-8);
        assertEquals(7.5, processed.getDouble(1), 1e-8);
        assertEquals(9.5, processed.getDouble(2), 1e-8);
    }

    @Test
    public void testProcess_3x3_fills() throws InvalidRangeException {
        final Oblique500mTransform transform = new Oblique500mTransform(3000, 2400, 423);
        final float[][] data = new float[][]{{2.f, 3.f, 4.f, 5.f, -1000.f, 6.f},
                {-1000.f, 7.f, 8.f, 9.f, 10.f, -1000.f},
                {11.f, -1000.f, 12.f, 13.f, -1000.f, 14.f},
                {12.f, 13.f, 14.f, 15.f, -1000.f, -1000.f},
                {11.f, -1000.f, 12.f, 13.f, -1000.f, 14.f},
                {12.f, 13.f, 14.f, 15.f, -1000.f, -1000.f}};

        final Array array = Array.factory(data);
        final Array processed = transform.process(array, -1000.0);

        assertEquals("(3,3)", processed.shapeToString());
        assertEquals(4.0, processed.getDouble(0), 1e-8);
        assertEquals(6.5, processed.getDouble(1), 1e-8);
        assertEquals(8.0, processed.getDouble(2), 1e-8);
        assertEquals(12.0, processed.getDouble(3), 1e-8);
        assertEquals(13.5, processed.getDouble(4), 1e-8);
        assertEquals(14.0, processed.getDouble(5), 1e-8);
        assertEquals(12.0, processed.getDouble(6), 1e-8);
        assertEquals(13.5, processed.getDouble(7), 1e-8);
        assertEquals(14.0, processed.getDouble(8), 1e-8);
    }

    @Test
    public void testProcessFlags_1x1_noFills() throws InvalidRangeException {
        final Oblique500mTransform transform = new Oblique500mTransform(3000, 2400, 423);
        final int[][] data = new int[][]{{2, 3}, {4, 5}};

        final Array array = Array.factory(data);
        final Array processed = transform.processFlags(array, -1);

        assertEquals("(1,1)", processed.shapeToString());
        assertEquals(7, processed.getInt(0));
    }

    @Test
    public void testProcessFlags_3x3_fills() throws InvalidRangeException {
        final Oblique500mTransform transform = new Oblique500mTransform(3000, 2400, 423);
        final int[][] data = new int[][]{{1, 2, 3, 4, 5, 6},
                {7, 65535, 8, 9, 10, 11},
                {65535, 12, 13, 14, 65535, 15},
                {65535, 65535, 16, 17, 18, 19},
                {65535, 65535, 10, 11, 12, 13},
                {65535, 65535, 14, 15, 16, 17}};

        final Array array = Array.factory(data);
        final Array processed = transform.processFlags(array, 65535);

        assertEquals("(3,3)", processed.shapeToString());
        assertEquals(7, processed.getInt(0));
        assertEquals(15, processed.getInt(1));
        assertEquals(15, processed.getInt(2));
        assertEquals(12, processed.getInt(3));
        assertEquals(31, processed.getInt(4));
        assertEquals(31, processed.getInt(5));
        assertEquals(65535, processed.getInt(6));
        assertEquals(15, processed.getInt(7));
        assertEquals(29, processed.getInt(8));
    }
}

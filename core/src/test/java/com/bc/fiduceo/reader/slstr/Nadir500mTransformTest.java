package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

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
    public void testGetOffset() {
        final Nadir500mTransform transform = new Nadir500mTransform(201, 181);

        assertEquals(1, transform.getOffset());
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
    public void testProcess_1x1_noFills() throws InvalidRangeException {
        final Nadir500mTransform transform = new Nadir500mTransform(201, 181);
        final float[][] data = new float[][]{{0.f, 1.f}, {2.f, 3.f}};

        final Array array = Array.factory(data);
        final Array processed = transform.process(array, -1.0);

        assertEquals("(1,1)", processed.shapeToString());
        assertEquals(1.5, processed.getDouble(0), 1e-8);
    }

    @Test
    public void testProcess_1x1_fills() throws InvalidRangeException {
        final Nadir500mTransform transform = new Nadir500mTransform(201, 181);
        final float[][] data = new float[][]{{0.f, -1.f}, {2.f, 3.f}};

        final Array array = Array.factory(data);
        final Array processed = transform.process(array, -1.0);

        assertEquals("(1,1)", processed.shapeToString());
        assertEquals(1.6666666269302368, processed.getDouble(0), 1e-8);
    }

    @Test
    public void testProcess_1x1_onlyFills() throws InvalidRangeException {
        final Nadir500mTransform transform = new Nadir500mTransform(201, 181);
        final float[][] data = new float[][]{{-11.f, -11.f}, {-11.f, -11.f}};

        final Array array = Array.factory(data);
        final Array processed = transform.process(array, -11.0);

        assertEquals("(1,1)", processed.shapeToString());
        assertEquals(-11.0, processed.getDouble(0), 1e-8);
    }

    @Test
    public void testProcess_3x1_noFills() throws InvalidRangeException {
        final Nadir500mTransform transform = new Nadir500mTransform(202, 182);
        final float[][] data = new float[][]{{1.f, 2.f, 3.f, 4.f, 5.f, 6.f},
                {10.f, 11.f, 12.f, 13.f, 14.f, 15.f}};

        final Array array = Array.factory(data);
        final Array processed = transform.process(array, -2.0);

        assertEquals("(1,3)", processed.shapeToString());
        assertEquals(6.0, processed.getDouble(0), 1e-8);
        assertEquals(8.0, processed.getDouble(1), 1e-8);
        assertEquals(10.0, processed.getDouble(2), 1e-8);
    }

    @Test
    public void testProcess_3x3_fills() throws InvalidRangeException {
        final Nadir500mTransform transform = new Nadir500mTransform(202, 182);
        final float[][] data = new float[][]{{1.f, 2.f, 3.f, 4.f, 5.f, 6.f},
                {10.f, 11.f, 12.f, 13.f, 14.f, 15.f},
                {11.f, -4.f, 12.f, 13.f, -4.f, 14.f},
                {12.f, 13.f, 14.f, 15.f -4.f, -4.f},
                {11.f, -4.f, 12.f, 13.f, -4.f, 14.f},
                {12.f, 13.f, 14.f, 15.f -4.f, -4.f}};

        final Array array = Array.factory(data);
        final Array processed = transform.process(array, -4.0);

        assertEquals("(3,3)", processed.shapeToString());
        assertEquals(6.0, processed.getDouble(0), 1e-8);
        assertEquals(8.0, processed.getDouble(1), 1e-8);
        assertEquals(10.0, processed.getDouble(2), 1e-8);
        assertEquals(12.0, processed.getDouble(3), 1e-8);
        assertEquals(12.5, processed.getDouble(4), 1e-8);
        assertEquals(12.5, processed.getDouble(5), 1e-8);
        assertEquals(13.0, processed.getDouble(6), 1e-8);
        assertEquals(12.0, processed.getDouble(7), 1e-8);
        assertEquals(6.5, processed.getDouble(8), 1e-8);
    }

    @Test
    public void testProcessFlags_1x1_noFills() throws InvalidRangeException {
        final Nadir500mTransform transform = new Nadir500mTransform(202, 182);
        final int[][] data = new int[][]{{0, 1}, {2, 3}};

        final Array array = Array.factory(data);
        final Array processed = transform.processFlags(array, -1);

        assertEquals("(1,1)", processed.shapeToString());
        assertEquals(3, processed.getInt(0));
    }

    @Test
    public void testProcessFlags_3x1_noFills() throws InvalidRangeException {
        final Nadir500mTransform transform = new Nadir500mTransform(203, 183);
        final int[][] data = new int[][]{{0, 1, 2, 3, 4, 5},
                {6, 7, 8, 9, 10, 11}};

        final Array array = Array.factory(data);
        final Array processed = transform.processFlags(array, -2);

        assertEquals("(1,3)", processed.shapeToString());
        assertEquals(7, processed.getInt(0));
        assertEquals(11, processed.getInt(1));
        assertEquals(15, processed.getInt(2));
    }

    @Test
    public void testProcessFlags_3x3_fills() throws InvalidRangeException {
        final Nadir500mTransform transform = new Nadir500mTransform(203, 183);
        final int[][] data = new int[][]{{1, 1, 1, 1, 1, 1},
                {2, 65535, 2, 2, 2, 2},
                {65535, 4, 4, 4, 65535, 4},
                {5, 65535, 6, 7, 8, 9},
                {65535, 65535, 10, 11, 12, 13},
                {65535, 65535, 14, 15, 16, 17}};

        final Array array = Array.factory(data);
        final Array processed = transform.processFlags(array, 65535);

        assertEquals("(3,3)", processed.shapeToString());
        assertEquals(3, processed.getInt(0));
        assertEquals(3, processed.getInt(1));
        assertEquals(3, processed.getInt(2));
        assertEquals(5, processed.getInt(3));
        assertEquals(7, processed.getInt(4));
        assertEquals(13, processed.getInt(5));
        assertEquals(65535, processed.getInt(6));
        assertEquals(15, processed.getInt(7));
        assertEquals(29, processed.getInt(8));
    }
}

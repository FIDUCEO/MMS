package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;

import java.io.IOException;

import static org.junit.Assert.*;

public class RawDataReaderTest_context2D_FirstDimensionIsOne_double {

    private Interval windowSize;
    private Number fillValue;
    private double f;
    private Array rawArray;

    @Before
    public void setUp() throws Exception {
        windowSize = new Interval(3, 3);
        fillValue = -2;
        f = fillValue.byteValue();
        rawArray = getDoubleRawArray();
    }

    @Test
    public void testWindowCenter() throws Exception {
        final Array array = RawDataReader.read(3, 3, windowSize, fillValue, rawArray, new Dimension("size", 10, 0));

        assertNotNull(array);
        assertEquals(double.class, array.getElementType());
        assertEquals(9, array.getSize());
        final double[] expecteds = {
                2, 2, 2,
                3, 3, 3,
                4, 4, 4};
        final double[] actuals = (double[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals, 1e-8);
    }

    @Test
    public void testTopRightWindowOut() throws Exception {
        final Array array = RawDataReader.read(9, 0, windowSize, fillValue, rawArray, new Dimension("size", 10, 0));

        assertNotNull(array);
        assertEquals(double.class, array.getElementType());
        assertEquals(9, array.getSize());
        final double[] expecteds = {
                f, f, f,
                0, 0, f,
                1, 1, f};
        final double[] actuals = (double[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals, 1e-8);
    }

    @Test
    public void testTopLeftWindowOut() throws Exception {
        final Array array = RawDataReader.read(0, 0, windowSize, fillValue, rawArray, new Dimension("size", 10, 0));

        assertNotNull(array);
        assertEquals(double.class, array.getElementType());
        assertEquals(9, array.getSize());
        final double[] expecteds = {
                f, f, f,
                f, 0, 0,
                f, 1, 1};
        final double[] actuals = (double[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals, 1e-8);
    }

    @Test
    public void testBottomLeftWindowOut() throws Exception {
        final Array array = RawDataReader.read(0, 9, windowSize, fillValue, rawArray, new Dimension("size", 10, 0));

        assertNotNull(array);
        assertEquals(double.class, array.getElementType());
        assertEquals(9, array.getSize());
        final double[] expecteds = {
                f, 8, 8,
                f, 9, 9,
                f, f, f};
        final double[] actuals = (double[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals, 1e-8);
    }

    @Test
    public void testBottomRightWindowOut() throws Exception {
        final Array array = RawDataReader.read(9, 9, windowSize, fillValue, rawArray, new Dimension("size", 10, 0));

        assertNotNull(array);
        assertEquals(double.class, array.getElementType());
        assertEquals(9, array.getSize());
        final double[] expecteds = {
                8, 8, f,
                9, 9, f,
                f, f, f};
        final double[] actuals = (double[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals, 1e-8);
    }

    @Test
    public void testRawArrayHasMoreThanTwoDimensions() throws IOException {
        final Array rawArray = Array.factory(new double[][][]{
                {{11, 12, 13}, {14, 15, 16}, {17, 18, 19},},
                {{21, 22, 23}, {24, 25, 26}, {27, 28, 29},},
                {{31, 32, 33}, {34, 35, 36}, {37, 38, 39},}
        });

        try {
            RawDataReader.read(1, 1, new Interval(3, 3), -4d, rawArray, new Dimension("size", 10, 0));
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    private Array getDoubleRawArray() {
        final double[][] bytes = new double[1][];
        bytes[0] = new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        return Array.factory(bytes);
    }
}
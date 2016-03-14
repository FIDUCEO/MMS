package com.bc.fiduceo.reader;

import static org.junit.Assert.*;

import com.bc.fiduceo.core.Interval;
import org.junit.*;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;


@SuppressWarnings("Duplicates")
public class WindowArrayFactory2DTest_caseNetCDF_ArrayDoubleD2 {

    private WindowArrayFactory2D windowArrayFactory;
    private Interval windowSize;
    private double fillValue;
    private double fv;

    @Before
    public void setUp() throws Exception {
        windowSize = new Interval(3, 3);
        fillValue = -2;
        fv = fillValue;
        final Array rawArray = getDoubleRawArray();
        windowArrayFactory = new WindowArrayFactory2D(rawArray);
    }

    @Test
    public void testWindowCenter() throws Exception {

        final Array array = windowArrayFactory.get(3, 3, windowSize, fillValue);

        assertNotNull(array);
        assertEquals(9, array.getSize());
        final double[] expecteds = {22, 32, 42, 23, 33, 43, 24, 34, 44};
        final double[] actuals = (double[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals, 1e-8);
    }

    @Test
    public void testTopRightWindowOut() throws Exception {

        final Array array = windowArrayFactory.get(9, 0, windowSize, fillValue);

        assertNotNull(array);
        assertEquals(9, array.getSize());
        final double[] expecteds = {fv, fv, fv, 80, 90, fv, 81, 91, fv};
        final double[] actuals = (double[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals, 1e-8);
    }

    @Test
    public void testTopLeftWindowOut() throws Exception {

        final Array array = windowArrayFactory.get(0, 0, windowSize, fillValue);

        assertNotNull(array);
        assertEquals(9, array.getSize());
        final double[] expecteds = {fv, fv, fv, fv, 0, 10, fv, 1, 11};
        final double[] actuals = (double[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals, 1e-8);
    }

    @Test
    public void testBottomLeftWindowOut() throws Exception {
        final Array array = windowArrayFactory.get(0, 9, windowSize, fillValue);

        assertNotNull(array);
        assertEquals(9, array.getSize());
        final double[] expecteds = {fv, 8, 18, fv, 9, 19, fv, fv, fv};
        final double[] actuals = (double[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals, 1e-8);
    }

    @Test
    public void testBottomRightWindowOut() throws Exception {
        final Array array = windowArrayFactory.get(9, 9, windowSize, fillValue);

        assertNotNull(array);
        assertEquals(9, array.getSize());
        final double[] expecteds = {88, 98, fv, 89, 99, fv, fv, fv, fv};
        final double[] actuals = (double[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals, 1e-8);
    }

    @Test
    public void testRawArrayHasMoreThanTwoDimensions() {
        final Array rawArray = Array.factory(new double[][][]{
                    {{11, 12, 13}, {14, 15, 16}, {17, 18, 19},},
                    {{21, 22, 23}, {24, 25, 26}, {27, 28, 29},},
                    {{31, 32, 33}, {34, 35, 36}, {37, 38, 39},}
        });
        final WindowArrayFactory2D factory = new WindowArrayFactory2D(rawArray);

        try {
            factory.get(1, 1, new Interval(3, 3), -4d);
            fail("InvalidRangeException expected");
        } catch (InvalidRangeException expected) {
        }
    }


    @Test
    public void testRawArrayHasLessThanTwoDimensions() throws InvalidRangeException {
        final Array rawArray = Array.factory(new double[]{11, 12, 13});
        final WindowArrayFactory2D factory = new WindowArrayFactory2D(rawArray);

        try {
            factory.get(1, 1, new Interval(3, 3), -4d);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException expected) {
        }
    }

    private Array getDoubleRawArray() {
        return Array.factory(new double[][]{
                    {0, 10, 20, 30, 40, 50, 60, 70, 80, 90},
                    {1, 11, 21, 31, 41, 51, 61, 71, 81, 91},
                    {2, 12, 22, 32, 42, 52, 62, 72, 82, 92},
                    {3, 13, 23, 33, 43, 53, 63, 73, 83, 93},
                    {4, 14, 24, 34, 44, 54, 64, 74, 84, 94},
                    {5, 15, 25, 35, 45, 55, 65, 75, 85, 95},
                    {6, 16, 26, 36, 46, 56, 66, 76, 86, 96},
                    {7, 17, 27, 37, 47, 57, 67, 77, 87, 97},
                    {8, 18, 28, 38, 48, 58, 68, 78, 88, 98},
                    {9, 19, 29, 39, 49, 59, 69, 79, 89, 99}
        });
    }
}
package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.Interval;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class WindowArrayFactoryTest {

    private WindowArrayFactory windowArrayFactory;

    @Before
    public void setUp() throws Exception {
        final Array factory = Array.factory(AVHRR18_LONGITUDE);
        windowArrayFactory = new WindowArrayFactory(factory);
    }

    @Test
    public void testWindowCenter() throws Exception {
        Interval interval = new Interval(3, 3);
        double fillValue = 0.02;
        final Array array = windowArrayFactory.get(3, 3, interval, fillValue);
        assertNotNull(array);
        assertEquals(9,array.getSize());
        assertEquals("-153.887 -152.674 -151.419 -153.629 -152.411 -151.151 -153.371 -152.149 -150.885 ",array.toString());
    }

    @Test
    public void testTopRightWindowOut() throws Exception {
        Interval interval = new Interval(3,3);
        double fillValue = 0.02;
        final Array array = windowArrayFactory.get(9, 0, interval, fillValue);
        assertNotNull(array);
        assertEquals(9,array.getSize());
        assertEquals("0.02 0.02 0.02 -146.525 -145.049 0.02 -146.24 -144.761 0.02 ",array.toString());
    }

    @Test
    public void testTopLeftWindowOut() throws Exception {
        Interval interval = new Interval(3,3);
        double fillValue = 0.02;
        final Array array = windowArrayFactory.get(0, 0, interval, fillValue);
        assertNotNull(array);
        assertEquals(9,array.getSize());
        assertEquals("0.02 0.02 0.02 0.02 -156.691 -155.571 0.02 -156.439 -155.313 ",array.toString());
    }

    @Test
    public void testBottomLeftWindowOut() throws Exception {
        Interval interval = new Interval(3,3);
        double fillValue = 0.02;
        final Array array = windowArrayFactory.get(0, 18, interval, fillValue);
        assertNotNull(array);
        assertEquals(9,array.getSize());
        assertEquals("0.02 -152.604 -151.405 0.02 -152.375 -151.173 0.02 0.02 0.02 ",array.toString());
    }


    @Test
    public void testBottomRightWindowOut() throws Exception {
        Interval interval = new Interval(3,3);
        double fillValue = 0.02;
        final Array array = windowArrayFactory.get(9, 18, interval, fillValue);
        assertNotNull(array);
        assertEquals(9,array.getSize());
        assertEquals("-141.99 -140.498 0.02 -141.744 -140.252 0.02 0.02 0.02 0.02 ",array.toString());
    }


    @Test
    public void testCentreWindowIntegerArray() throws Exception {
        final Array factoryArrayInt = Array.factory(TEST_INT);
        final WindowArrayFactory windowArrayFactory = new WindowArrayFactory(factoryArrayInt);

        final int fillValue = 4;
        final Interval interval = new Interval(3,3);
        final Array array = windowArrayFactory.get(2, 2, interval, fillValue);

        assertNotNull(array);
        assertEquals(9,array.getSize());
        assertEquals("31 6 43 31 5 14 31 5 94 ",array.toString());
    }

    @Test
    public void testOutWindowByteArray() throws Exception {
        final Array factoryArrayInt = Array.factory(TEST_BYTE);
        final WindowArrayFactory windowArrayFactory = new WindowArrayFactory(factoryArrayInt);

        final byte fillValue = 16;
        final Interval interval = new Interval(3,3);
        final Array array = windowArrayFactory.get(0, 1, interval, fillValue);

        assertNotNull(array);
        assertEquals(9,array.getSize());
        assertEquals("16 -5 -5 16 89 31 16 67 31 ",array.toString());
    }

    @Test
    public void testOutWindowFloatArray() throws Exception {
        final Array factoryArrayInt = Array.factory(TEST_FLOAT);
        final WindowArrayFactory windowArrayFactory = new WindowArrayFactory(factoryArrayInt);

        final float fillValue = 32;
        final Interval interval = new Interval(3,3);
        final Array array = windowArrayFactory.get(4, 6, interval, fillValue);

        assertNotNull(array);
        assertEquals(9,array.getSize());
        assertEquals("56.0 75.0 32.0 89.0 90.0 32.0 32.0 32.0 32.0 ",array.toString());
    }


    private double[][] AVHRR18_LONGITUDE= new double[][]{
            {-156.691,	-155.571,	-154.409,	-153.206,	-151.96	,	-150.67	,	-149.335,	-147.954,	-146.525,	-145.049 },
            {-156.439,	-155.313,	-154.147,	-152.939,	-151.689,	-150.394,	-149.055,	-147.671,	-146.24	,	-144.761 },
            {-156.189,	-155.058,	-153.887,	-152.674,	-151.419,	-150.121,	-148.778,	-147.39	,	-145.956,	-144.476 },
            {-155.942,	-154.805,	-153.629,	-152.411,	-151.151,	-149.849,	-148.502,	-147.111,	-145.675,	-144.193 },
            {-155.694,	-154.553,	-153.371,	-152.149,	-150.885,	-149.579,	-148.229,	-146.835,	-145.397,	-143.913 },
            {-155.451,	-154.304,	-153.117,	-151.89	,	-150.621,	-149.311,	-147.958,	-146.561,	-145.12	,	-143.635 },
            {-155.205,	-154.053,	-152.862,	-151.631,	-150.359,	-149.045,	-147.689,	-146.29	,	-144.847,	-143.361 },
            {-154.962,	-153.805,	-152.61	,	-151.374,	-150.098,	-148.781,	-147.422,	-146.02	,	-144.576,	-143.088 },
            {-154.72	,-153.559,	-152.359,	-151.119,	-149.84	,	-148.519,	-147.157,	-145.753,	-144.307,	-142.818 },
            {-154.481,	-153.314,	-152.11	,	-150.866,	-149.583,	-148.259,	-146.894,	-145.488,	-144.04	,	-142.551 },
            {-154.241,	-153.07	,	-151.862,	-150.614,	-149.327,	-148.0	,	-146.633,	-145.225,	-143.776,	-142.286 },
            {-154.004,	-152.829,	-151.616,	-150.364,	-149.074,	-147.744,	-146.374,	-144.964,	-143.514,	-142.023 },
            {-153.767,	-152.588,	-151.371,	-150.116,	-148.822,	-147.49	,	-146.118,	-144.706,	-143.254,	-141.763 },
            {-153.531,	-152.348,	-151.127,	-149.869,	-148.573,	-147.237,	-145.863,	-144.45	,	-142.997,	-141.506 },
            {-153.298,	-152.111,	-150.886,	-149.624,	-148.324,	-146.986,	-145.61	,	-144.195,	-142.742,	-141.25  },
            {-153.065,	-151.874,	-150.646,	-149.381,	-148.078,	-146.738,	-145.359,	-143.943,	-142.489,	-140.997 },
            {-152.834,	-151.639,	-150.407,	-149.139,	-147.834,	-146.491,	-145.111,	-143.693,	-142.238,	-140.746 },
            {-152.604,	-151.405,	-150.17	,	-148.899,	-147.591,	-146.246,	-144.864,	-143.445,	-141.99	,	-140.498 },
            {-152.375,	-151.173,	-149.935,	-148.66	,	-147.35	,	-146.003,	-144.619,	-143.199,	-141.744,	-140.252 }
    };

    private int[][] TEST_INT = new int[][]{
            {-5	,-5	,-55,97	,-29},
            {89	,31,6	,43	,43},
            {67	,31	,5	,14	,97},
            {12	,31	,5	,94	,43},
            {14	,31	,5	,65	,45},
            {15	,67,56	,56	,75},
            {16	,17	,56	,89	,90}
    };

    private byte[][] TEST_BYTE = new byte[][]{
            {-5	,-5	,-55,97	,-29},
            {89	,31,6	,43	,43},
            {67	,31	,5	,14	,97},
            {12	,31	,5	,94	,43},
            {14	,31	,5	,65	,45},
            {15	,67,56	,56	,75},
            {16	,17	,56	,89	,90}
    };

    private float[][] TEST_FLOAT = new float[][]{
            {589,-8785	,-550,97,-29},
            {89	,31,6	,43	,43},
            {67	,31	,5	,14	,97},
            {12	,31	,5	,94	,43},
            {14	,31	,5	,65	,45},
            {15	,67,56	,56	,75},
            {16	,17	,56	,89	,90}
    };
}
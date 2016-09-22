package com.bc.fiduceo.reader;

import static org.junit.Assert.*;

import com.bc.fiduceo.core.Interval;
import org.junit.*;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Read2dFrom3d_Test {

    private NetcdfFile netcdfFile;
    private ArrayCache arrayCache;

    @Before
    public void setUp() throws Exception {
        final URL url = this.getClass().getResource("testfile.nc");
        final String filePath = new File(url.toURI()).getAbsolutePath();
        netcdfFile = NetcdfFile.open(filePath);
        arrayCache = new ArrayCache(netcdfFile);
    }

    @After
    public void tearDown() throws Exception {
        netcdfFile.close();
    }

    @Test
    public void testReadDouble_layer0_window3_pos1_1() throws IOException {
        final double[] expected = {
                    0, 1, 2,
                    2, 3, 4,
                    4, 5, 6
        };
        final String layer = "0";
        final int centerX = 1;
        final int centerY = 1;
        final int windowWidth = 3;
        final int windowHeight = 3;
        read("Double3D", layer, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadDouble_layer4_window5_pos6_7() throws IOException {
        final double[] expected = {
                    54, 55, 56, 57, 58,
                    56, 57, 58, 59, 60,
                    58, 59, 60, 61, 62,
                    60, 61, 62, 63, 64,
                    62, 63, 64, 65, 66
        };
        final String layer = "4";
        final int centerX = 6;
        final int centerY = 7;
        final int windowWidth = 5;
        final int windowHeight = 5;
        read("Double3D", layer, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadFloat_layer0_window3_pos1_1() throws IOException {
        final float[] expected = {
                    1, 2, 3,
                    3, 4, 5,
                    5, 6, 7
        };
        final String layer = "0";
        final int centerX = 1;
        final int centerY = 1;
        final int windowWidth = 3;
        final int windowHeight = 3;
        read("Float3D", layer, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadFloat_layer4_window5_pos6_7() throws IOException {
        final float[] expected = {
                    55, 56, 57, 58, 59,
                    57, 58, 59, 60, 61,
                    59, 60, 61, 62, 63,
                    61, 62, 63, 64, 65,
                    63, 64, 65, 66, 67
        };
        final String layer = "4";
        final int centerX = 6;
        final int centerY = 7;
        final int windowWidth = 5;
        final int windowHeight = 5;
        read("Float3D", layer, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadLong_layer0_window3_pos1_1() throws IOException {
        final long[] expected = {
                    2, 3, 4,
                    4, 5, 6,
                    6, 7, 8
        };
        final String layer = "0";
        final int centerX = 1;
        final int centerY = 1;
        final int windowWidth = 3;
        final int windowHeight = 3;
        read("Long3D", layer, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadLong_layer4_window5_pos6_7() throws IOException {
        final long[] expected = {
                    56, 57, 58, 59, 60,
                    58, 59, 60, 61, 62,
                    60, 61, 62, 63, 64,
                    62, 63, 64, 65, 66,
                    64, 65, 66, 67, 68
        };
        final String layer = "4";
        final int centerX = 6;
        final int centerY = 7;
        final int windowWidth = 5;
        final int windowHeight = 5;
        read("Long3D", layer, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadInt_layer0_window3_pos1_1() throws IOException {
        final int[] expected = {
                    3, 4, 5,
                    5, 6, 7,
                    7, 8, 9
        };
        final String layer = "0";
        final int centerX = 1;
        final int centerY = 1;
        final int windowWidth = 3;
        final int windowHeight = 3;
        read("Int3D", layer, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadInt_layer4_window5_pos6_7() throws IOException {
        final int[] expected = {
                    57, 58, 59, 60, 61,
                    59, 60, 61, 62, 63,
                    61, 62, 63, 64, 65,
                    63, 64, 65, 66, 67,
                    65, 66, 67, 68, 69
        };
        final String layer = "4";
        final int centerX = 6;
        final int centerY = 7;
        final int windowWidth = 5;
        final int windowHeight = 5;
        read("Int3D", layer, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadShort_layer0_window3_pos1_1() throws IOException {
        final short[] expected = {
                    4, 5, 6,
                    6, 7, 8,
                    8, 9, 10
        };
        final String layer = "0";
        final int centerX = 1;
        final int centerY = 1;
        final int windowWidth = 3;
        final int windowHeight = 3;
        read("Short3D", layer, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadShort_layer4_window5_pos6_7() throws IOException {
        final short[] expected = {
                    58, 59, 60, 61, 62,
                    60, 61, 62, 63, 64,
                    62, 63, 64, 65, 66,
                    64, 65, 66, 67, 68,
                    66, 67, 68, 69, 70
        };
        final String layer = "4";
        final int centerX = 6;
        final int centerY = 7;
        final int windowWidth = 5;
        final int windowHeight = 5;
        read("Short3D", layer, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadByte_layer0_window3_pos1_1() throws IOException {
        final byte[] expected = {
                    5, 6, 7,
                    7, 8, 9,
                    9, 10, 11
        };
        final String layer = "0";
        final int centerX = 1;
        final int centerY = 1;
        final int windowWidth = 3;
        final int windowHeight = 3;
        read("Byte3D", layer, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadByte_layer4_window5_pos6_7() throws IOException {
        final byte[] expected = {
                    59, 60, 61, 62, 63,
                    61, 62, 63, 64, 65,
                    63, 64, 65, 66, 67,
                    65, 66, 67, 68, 69,
                    67, 68, 69, 70, 71
        };
        final String layer = "4";
        final int centerX = 6;
        final int centerY = 7;
        final int windowWidth = 5;
        final int windowHeight = 5;
        read("Byte3D", layer, centerX, centerY, windowWidth, windowHeight, expected);
    }

    private void read(final String name, String layer, final int centerX, final int centerY, final int windowWidth, final int windowHeight, Object expected) throws IOException {
        final Read2dFrom3d.ArraySource arraySource = () -> arrayCache.get(name);
        final String[] offsetMapping = {"y", "x", layer};
        final int fillValue = -1;
        final WindowReader reader = new Read2dFrom3d(arraySource, offsetMapping, fillValue);

        final Array array = reader.read(centerX, centerY, new Interval(windowWidth, windowHeight));
        assertNotNull(array);
        assertEquals(2, array.getRank());
        final DataType dataType = array.getDataType();
        if (DataType.DOUBLE.equals(dataType)) {
            assertArrayEquals((double[]) expected, (double[]) array.getStorage(), 1e-128);
        } else if (DataType.FLOAT.equals(dataType)) {
            assertArrayEquals((float[]) expected, (float[]) array.getStorage(), 1e-45f);
        } else if (DataType.LONG.equals(dataType)) {
            assertArrayEquals((long[]) expected, (long[]) array.getStorage());
        } else if (DataType.INT.equals(dataType)) {
            assertArrayEquals((int[]) expected, (int[]) array.getStorage());
        } else if (DataType.SHORT.equals(dataType)) {
            assertArrayEquals((short[]) expected, (short[]) array.getStorage());
        } else if (DataType.BYTE.equals(dataType)) {
            assertArrayEquals((byte[]) expected, (byte[]) array.getStorage());
        } else {
            fail();
        }
    }
}

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

public class Read1dFrom3dAndExpandTo2d_Test {

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
    public void testReadDouble_layer0_col1_window3_pos1_1() throws IOException {
        final double[] expected = {
                    1, 1, 1,
                    3, 3, 3,
                    5, 5, 5
        };
        final String layer = "0";
        final String column = "1";
        final int centerX = 1;
        final int centerY = 1;
        final int windowWidth = 3;
        final int windowHeight = 3;
        read("Double3D", layer, column, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadDouble_layer4_col5_window5_pos6_7() throws IOException {
        final double[] expected = {
                    55, 55, 55, 55, 55,
                    57, 57, 57, 57, 57,
                    59, 59, 59, 59, 59,
                    61, 61, 61, 61, 61,
                    63, 63, 63, 63, 63
        };
        final String layer = "4";
        final String column = "5";
        final int centerX = 6;
        final int centerY = 7;
        final int windowWidth = 5;
        final int windowHeight = 5;
        read("Double3D", layer, column, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadFloat_layer0_col1_window3_pos1_1() throws IOException {
        final float[] expected = {
                    2, 2, 2,
                    4, 4, 4,
                    6, 6, 6
        };
        final String layer = "0";
        final String column = "1";
        final int centerX = 1;
        final int centerY = 1;
        final int windowWidth = 3;
        final int windowHeight = 3;
        read("Float3D", layer, column, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadFloat_layer4_col5_window5_pos6_7() throws IOException {
        final float[] expected = {
                    56, 56, 56, 56, 56,
                    58, 58, 58, 58, 58,
                    60, 60, 60, 60, 60,
                    62, 62, 62, 62, 62,
                    64, 64, 64, 64, 64
        };
        final String layer = "4";
        final String column = "5";
        final int centerX = 6;
        final int centerY = 7;
        final int windowWidth = 5;
        final int windowHeight = 5;
        read("Float3D", layer, column, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadLong_layer0_col1_window3_pos1_1() throws IOException {
        final long[] expected = {
                    3, 3, 3,
                    5, 5, 5,
                    7, 7, 7
        };
        final String layer = "0";
        final String column = "1";
        final int centerX = 1;
        final int centerY = 1;
        final int windowWidth = 3;
        final int windowHeight = 3;
        read("Long3D",layer, column, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadLong_layer4_col5_window5_pos6_7() throws IOException {
        final long[] expected = {
                    57, 57, 57, 57, 57,
                    59, 59, 59, 59, 59,
                    61, 61, 61, 61, 61,
                    63, 63, 63, 63, 63,
                    65, 65, 65, 65, 65
        };
        final String layer = "4";
        final String column = "5";
        final int centerX = 6;
        final int centerY = 7;
        final int windowWidth = 5;
        final int windowHeight = 5;
        read("Long3D",layer, column, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadInt_layer0_col1_window3_pos1_1() throws IOException {
        final int[] expected = {
                    4, 4, 4,
                    6, 6, 6,
                    8, 8, 8
        };
        final String layer = "0";
        final String column = "1";
        final int centerX = 1;
        final int centerY = 1;
        final int windowWidth = 3;
        final int windowHeight = 3;
        read("Int3D",layer, column, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadInt_layer4_col5_window5_pos6_7() throws IOException {
        final int[] expected = {
                    58, 58, 58, 58, 58,
                    60, 60, 60, 60, 60,
                    62, 62, 62, 62, 62,
                    64, 64, 64, 64, 64,
                    66, 66, 66, 66, 66
        };
        final String layer = "4";
        final String column = "5";
        final int centerX = 6;
        final int centerY = 7;
        final int windowWidth = 5;
        final int windowHeight = 5;
        read("Int3D",layer, column, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadShort_layer0_col1_window3_pos1_1() throws IOException {
        final short[] expected = {
                    5, 5, 5,
                    7, 7, 7,
                    9, 9, 9
        };
        final String layer = "0";
        final String column = "1";
        final int centerX = 1;
        final int centerY = 1;
        final int windowWidth = 3;
        final int windowHeight = 3;
        read("Short3D",layer, column, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadShort_layer4_col5_window5_pos6_7() throws IOException {
        final short[] expected = {
                    59, 59, 59, 59, 59,
                    61, 61, 61, 61, 61,
                    63, 63, 63, 63, 63,
                    65, 65, 65, 65, 65,
                    67, 67, 67, 67, 67
        };
        final String layer = "4";
        final String column = "5";
        final int centerX = 6;
        final int centerY = 7;
        final int windowWidth = 5;
        final int windowHeight = 5;
        read("Short3D",layer, column, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadByte_layer0_col1_window3_pos1_1() throws IOException {
        final byte[] expected = {
                    6, 6, 6,
                    8, 8, 8,
                    10, 10, 10
        };
        final String layer = "0";
        final String column = "1";
        final int centerX = 1;
        final int centerY = 1;
        final int windowWidth = 3;
        final int windowHeight = 3;
        read("Byte3D", layer, column, centerX, centerY, windowWidth, windowHeight, expected);
    }

    @Test
    public void testReadByte_layer4_col5_window5_pos6_7() throws IOException {
        final byte[] expected = {
                    60, 60, 60, 60, 60,
                    62, 62, 62, 62, 62,
                    64, 64, 64, 64, 64,
                    66, 66, 66, 66, 66,
                    68, 68, 68, 68, 68
        };
        final String layer = "4";
        final String column = "5";
        final int centerX = 6;
        final int centerY = 7;
        final int windowWidth = 5;
        final int windowHeight = 5;
        read("Byte3D", layer, column, centerX, centerY, windowWidth, windowHeight, expected);
    }

    private void read(final String variableName, String layer, String column, final int centerX, final int centerY, final int windowWidth, final int windowHeight, Object expected) throws IOException {
        final Read1dFrom3dAndExpandTo2d.ArraySource arraySource = () -> arrayCache.get(variableName);
        final String[] offsetMapping = {"y", column, layer};
        final int fillValue = -1;
        final WindowReader reader = new Read1dFrom3dAndExpandTo2d(arraySource, offsetMapping, fillValue);

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

package com.bc.fiduceo.reader.window_reader;

import static org.junit.Assert.*;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.ArrayCache;
import org.junit.*;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayLong;
import ucar.ma2.ArrayShort;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class TestRead1dFrom3dAndExpandTo2d {

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
        readDouble(layer, column, centerX, centerY, windowWidth, windowHeight, expected);
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
        readDouble(layer, column, centerX, centerY, windowWidth, windowHeight, expected);
    }

    public void readDouble(String layer, String column, final int centerX, final int centerY, final int windowWidth, final int windowHeight, double[] expected) throws IOException {
        final Read1dFrom3dAndExpandTo2dDouble.ArraySourceDouble arraySource = () -> (ArrayDouble.D3) arrayCache.get("Double3D");
        final String[] offsetMapping = {"y", column, layer};
        final int fillValue = -1;
        final WindowReader reader = new Read1dFrom3dAndExpandTo2dDouble(arraySource, offsetMapping, fillValue);

        final Array array = reader.read(centerX, centerY, new Interval(windowWidth, windowHeight));
        assertNotNull(array);
        assertTrue(array instanceof ArrayDouble.D2);
        final ArrayDouble.D2 arrayDouble = (ArrayDouble.D2) array;
        final double[] storage = (double[]) arrayDouble.getStorage();
        assertArrayEquals(expected, storage, 1e-8);
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
        readFloat(layer, column, centerX, centerY, windowWidth, windowHeight, expected);
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
        readFloat(layer, column, centerX, centerY, windowWidth, windowHeight, expected);
    }

    public void readFloat(String layer, String column, final int centerX, final int centerY, final int windowWidth, final int windowHeight, float[] expected) throws IOException {
        final Read1dFrom3dAndExpandTo2dFloat.ArraySourceFloat arraySource = () -> (ArrayFloat.D3) arrayCache.get("Float3D");
        final String[] offsetMapping = {"y", column, layer};
        final int fillValue = -1;
        final WindowReader reader = new Read1dFrom3dAndExpandTo2dFloat(arraySource, offsetMapping, fillValue);

        final Array array = reader.read(centerX, centerY, new Interval(windowWidth, windowHeight));
        assertNotNull(array);
        assertTrue(array instanceof ArrayFloat.D2);
        final ArrayFloat.D2 arrayFloat = (ArrayFloat.D2) array;
        final float[] storage = (float[]) arrayFloat.getStorage();
        assertArrayEquals(expected, storage, 1e-8f);
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
        readLong(layer, column, centerX, centerY, windowWidth, windowHeight, expected);
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
        readLong(layer, column, centerX, centerY, windowWidth, windowHeight, expected);
    }

    public void readLong(String layer, String column, final int centerX, final int centerY, final int windowWidth, final int windowHeight, long[] expected) throws IOException {
        final Read1dFrom3dAndExpandTo2dLong.ArraySourceLong arraySource = () -> (ArrayLong.D3) arrayCache.get("Long3D");
        final String[] offsetMapping = {"y", column, layer};
        final int fillValue = -1;
        final WindowReader reader = new Read1dFrom3dAndExpandTo2dLong(arraySource, offsetMapping, fillValue);

        final Array array = reader.read(centerX, centerY, new Interval(windowWidth, windowHeight));
        assertNotNull(array);
        assertTrue(array instanceof ArrayLong.D2);
        final ArrayLong.D2 arrayLong = (ArrayLong.D2) array;
        final long[] storage = (long[]) arrayLong.getStorage();
        assertArrayEquals(expected, storage);
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
        readInt(layer, column, centerX, centerY, windowWidth, windowHeight, expected);
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
        readInt(layer, column, centerX, centerY, windowWidth, windowHeight, expected);
    }

    public void readInt(String layer, String column, final int centerX, final int centerY, final int windowWidth, final int windowHeight, int[] expected) throws IOException {
        final Read1dFrom3dAndExpandTo2dInt.ArraySourceInt arraySource = () -> (ArrayInt.D3) arrayCache.get("Int3D");
        final String[] offsetMapping = {"y", column, layer};
        final int fillValue = -1;
        final WindowReader reader = new Read1dFrom3dAndExpandTo2dInt(arraySource, offsetMapping, fillValue);

        final Array array = reader.read(centerX, centerY, new Interval(windowWidth, windowHeight));
        assertNotNull(array);
        assertTrue(array instanceof ArrayInt.D2);
        final ArrayInt.D2 arrayInt = (ArrayInt.D2) array;
        final int[] storage = (int[]) arrayInt.getStorage();
        assertArrayEquals(expected, storage);
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
        readShort(layer, column, centerX, centerY, windowWidth, windowHeight, expected);
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
        readShort(layer, column, centerX, centerY, windowWidth, windowHeight, expected);
    }

    public void readShort(String layer, String column, final int centerX, final int centerY, final int windowWidth, final int windowHeight, short[] expected) throws IOException {
        final Read1dFrom3dAndExpandTo2dShort.ArraySourceShort arraySource = () -> (ArrayShort.D3) arrayCache.get("Short3D");
        final String[] offsetMapping = {"y", column, layer};
        final int fillValue = -1;
        final WindowReader reader = new Read1dFrom3dAndExpandTo2dShort(arraySource, offsetMapping, fillValue);

        final Array array = reader.read(centerX, centerY, new Interval(windowWidth, windowHeight));
        assertNotNull(array);
        assertTrue(array instanceof ArrayShort.D2);
        final ArrayShort.D2 arrayShort = (ArrayShort.D2) array;
        final short[] storage = (short[]) arrayShort.getStorage();
        assertArrayEquals(expected, storage);
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
        readByte(layer, column, centerX, centerY, windowWidth, windowHeight, expected);
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
        readByte(layer, column, centerX, centerY, windowWidth, windowHeight, expected);
    }

    public void readByte(String layer, String column, final int centerX, final int centerY, final int windowWidth, final int windowHeight, byte[] expected) throws IOException {
        final Read1dFrom3dAndExpandTo2dByte.ArraySourceByte arraySource = () -> (ArrayByte.D3) arrayCache.get("Byte3D");
        final String[] offsetMapping = {"y", column, layer};
        final int fillValue = -1;
        final WindowReader reader = new Read1dFrom3dAndExpandTo2dByte(arraySource, offsetMapping, fillValue);

        final Array array = reader.read(centerX, centerY, new Interval(windowWidth, windowHeight));
        assertNotNull(array);
        assertTrue(array instanceof ArrayByte.D2);
        final ArrayByte.D2 arrayByte = (ArrayByte.D2) array;
        final byte[] storage = (byte[]) arrayByte.getStorage();
        assertArrayEquals(expected, storage);
    }

}

package com.bc.fiduceo.reader.window_reader;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.ArrayCache;
import org.junit.*;

import static org.junit.Assert.*;

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

public class TestRead2dFrom3d {

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
        readDouble(layer, centerX, centerY, windowWidth, windowHeight, expected);
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
        readDouble(layer, centerX, centerY, windowWidth, windowHeight, expected);
    }

    public void readDouble(String layer, final int centerX, final int centerY, final int windowWidth, final int windowHeight, double[] expected) throws IOException {
        final Read2dFrom3dDouble.ArraySourceDouble arraySource = () -> (ArrayDouble.D3) arrayCache.get("Double3D");
        final String[] offsetMapping = {"y", "x", layer};
        final int fillValue = -1;
        final Read2dFrom3dDouble reader = new Read2dFrom3dDouble(arraySource, offsetMapping, fillValue);

        final Array array = reader.read(centerX, centerY, new Interval(windowWidth, windowHeight));
        assertNotNull(array);
        assertTrue(array instanceof ArrayDouble.D2);
        final ArrayDouble.D2 arrayDouble = (ArrayDouble.D2) array;
        final double[] storage = (double[]) arrayDouble.getStorage();
        assertArrayEquals(expected, storage, 1e-8);
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
        readFloat(layer, centerX, centerY, windowWidth, windowHeight, expected);
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
        readFloat(layer, centerX, centerY, windowWidth, windowHeight, expected);
    }

    public void readFloat(String layer, final int centerX, final int centerY, final int windowWidth, final int windowHeight, float[] expected) throws IOException {
        final Read2dFrom3dFloat.ArraySourceFloat arraySource = () -> (ArrayFloat.D3) arrayCache.get("Float3D");
        final String[] offsetMapping = {"y", "x", layer};
        final int fillValue = -1;
        final Read2dFrom3dFloat reader = new Read2dFrom3dFloat(arraySource, offsetMapping, fillValue);

        final Array array = reader.read(centerX, centerY, new Interval(windowWidth, windowHeight));
        assertNotNull(array);
        assertTrue(array instanceof ArrayFloat.D2);
        final ArrayFloat.D2 arrayFloat = (ArrayFloat.D2) array;
        final float[] storage = (float[]) arrayFloat.getStorage();
        assertArrayEquals(expected, storage, 1e-8f);
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
        readLong(layer, centerX, centerY, windowWidth, windowHeight, expected);
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
        readLong(layer, centerX, centerY, windowWidth, windowHeight, expected);
    }

    public void readLong(String layer, final int centerX, final int centerY, final int windowWidth, final int windowHeight, long[] expected) throws IOException {
        final Read2dFrom3dLong.ArraySourceLong arraySource = () -> (ArrayLong.D3) arrayCache.get("Long3D");
        final String[] offsetMapping = {"y", "x", layer};
        final int fillValue = -1;
        final Read2dFrom3dLong reader = new Read2dFrom3dLong(arraySource, offsetMapping, fillValue);

        final Array array = reader.read(centerX, centerY, new Interval(windowWidth, windowHeight));
        assertNotNull(array);
        assertTrue(array instanceof ArrayLong.D2);
        final ArrayLong.D2 arrayLong = (ArrayLong.D2) array;
        final long[] storage = (long[]) arrayLong.getStorage();
        assertArrayEquals(expected, storage);
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
        readInt(layer, centerX, centerY, windowWidth, windowHeight, expected);
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
        readInt(layer, centerX, centerY, windowWidth, windowHeight, expected);
    }

    public void readInt(String layer, final int centerX, final int centerY, final int windowWidth, final int windowHeight, int[] expected) throws IOException {
        final Read2dFrom3dInt.ArraySourceInt arraySource = () -> (ArrayInt.D3) arrayCache.get("Int3D");
        final String[] offsetMapping = {"y", "x", layer};
        final int fillValue = -1;
        final Read2dFrom3dInt reader = new Read2dFrom3dInt(arraySource, offsetMapping, fillValue);

        final Array array = reader.read(centerX, centerY, new Interval(windowWidth, windowHeight));
        assertNotNull(array);
        assertTrue(array instanceof ArrayInt.D2);
        final ArrayInt.D2 arrayInt = (ArrayInt.D2) array;
        final int[] storage = (int[]) arrayInt.getStorage();
        assertArrayEquals(expected, storage);
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
        readShort(layer, centerX, centerY, windowWidth, windowHeight, expected);
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
        readShort(layer, centerX, centerY, windowWidth, windowHeight, expected);
    }

    public void readShort(String layer, final int centerX, final int centerY, final int windowWidth, final int windowHeight, short[] expected) throws IOException {
        final Read2dFrom3dShort.ArraySourceShort arraySource = () -> (ArrayShort.D3) arrayCache.get("Short3D");
        final String[] offsetMapping = {"y", "x", layer};
        final int fillValue = -1;
        final Read2dFrom3dShort reader = new Read2dFrom3dShort(arraySource, offsetMapping, fillValue);

        final Array array = reader.read(centerX, centerY, new Interval(windowWidth, windowHeight));
        assertNotNull(array);
        assertTrue(array instanceof ArrayShort.D2);
        final ArrayShort.D2 arrayShort = (ArrayShort.D2) array;
        final short[] storage = (short[]) arrayShort.getStorage();
        assertArrayEquals(expected, storage);
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
        readByte(layer, centerX, centerY, windowWidth, windowHeight, expected);
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
        readByte(layer, centerX, centerY, windowWidth, windowHeight, expected);
    }

    public void readByte(String layer, final int centerX, final int centerY, final int windowWidth, final int windowHeight, byte[] expected) throws IOException {
        final Read2dFrom3dByte.ArraySourceByte arraySource = () -> (ArrayByte.D3) arrayCache.get("Byte3D");
        final String[] offsetMapping = {"y", "x", layer};
        final int fillValue = -1;
        final Read2dFrom3dByte reader = new Read2dFrom3dByte(arraySource, offsetMapping, fillValue);

        final Array array = reader.read(centerX, centerY, new Interval(windowWidth, windowHeight));
        assertNotNull(array);
        assertTrue(array instanceof ArrayByte.D2);
        final ArrayByte.D2 arrayByte = (ArrayByte.D2) array;
        final byte[] storage = (byte[]) arrayByte.getStorage();
        assertArrayEquals(expected, storage);
    }
}

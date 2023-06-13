package com.bc.fiduceo.reader.insitu.tao;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.time.TimeLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class TaoReader_IO_Test {

    private TaoReader reader;

    @Before
    public void setUp() {
        reader = new TaoReader();
    }

    @After
    public void tearDown() throws IOException {
        reader.close();
    }

    @Test
    public void testReadAcquisitionInfo_TAO() throws Exception {
        final File insituDataFile = getTAOProduct();

        reader.open(insituDataFile);

        final AcquisitionInfo info = reader.read();
        TestUtil.assertCorrectUTCDate(2016, 6, 1, 0, 0, 0, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2016, 6, 30, 23, 0, 0, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }


    @Test
    public void testReadAcquisitionInfo_TRITON() throws Exception {
        final File insituDataFile = getTritonProduct();

        reader.open(insituDataFile);

        final AcquisitionInfo info = reader.read();
        TestUtil.assertCorrectUTCDate(2017, 10, 1, 12, 0, 0, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2017, 10, 31, 12, 0, 0, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    @Test
    public void testReadAcquisitionInfo_PIRATA() throws Exception {
        final File insituDataFile = getPirataProduct();

        reader.open(insituDataFile);

        final AcquisitionInfo info = reader.read();
        TestUtil.assertCorrectUTCDate(2016, 10, 1, 0, 0, 0, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2016, 10, 31, 23, 0, 0, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    @Test
    public void testGetProductSize_TAO() throws IOException {
        final File insituDataFile = getTAOProduct();

        reader.open(insituDataFile);

        final Dimension productSize = reader.getProductSize();

        assertNotNull(productSize);
        assertEquals("product_size", productSize.getName());
        assertEquals(1, productSize.getNx());
        assertEquals(720, productSize.getNy());
    }

    @Test
    public void testGetProductSize_TRITON() throws IOException {
        final File insituDataFile = getTritonProduct();

        reader.open(insituDataFile);

        final Dimension productSize = reader.getProductSize();

        assertNotNull(productSize);
        assertEquals("product_size", productSize.getName());
        assertEquals(1, productSize.getNx());
        assertEquals(31, productSize.getNy());
    }

    @Test
    public void testGetProductSize_RAMA() throws IOException {
        final File insituDataFile = getRamaProduct();

        reader.open(insituDataFile);

        final Dimension productSize = reader.getProductSize();

        assertNotNull(productSize);
        assertEquals("product_size", productSize.getName());
        assertEquals(1, productSize.getNx());
        assertEquals(720, productSize.getNy());
    }

    @Test
    public void testGetTimeLocator_TAO() throws IOException {
        final File insituDataFile = getTAOProduct();

        reader.open(insituDataFile);

        final TimeLocator timeLocator = reader.getTimeLocator();
        assertEquals(1464739200000L, timeLocator.getTimeFor(0, 0));
        assertEquals(1465664400000L, timeLocator.getTimeFor(10, 257));
        assertEquals(1466982000000L, timeLocator.getTimeFor(20, 623));

        assertEquals(-1L, timeLocator.getTimeFor(20, -1));
        assertEquals(-1L, timeLocator.getTimeFor(20, 2567));
    }

    @Test
    public void testGetTimeLocator_TRITON() throws IOException {
        final File insituDataFile = getTritonProduct();

        reader.open(insituDataFile);

        final TimeLocator timeLocator = reader.getTimeLocator();
        assertEquals(1506859200000L, timeLocator.getTimeFor(0, 0));
        assertEquals(1508155200000L, timeLocator.getTimeFor(10, 15));
        assertEquals(1509451200000L, timeLocator.getTimeFor(20, 30));

        assertEquals(-1L, timeLocator.getTimeFor(20, -1));
        assertEquals(-1L, timeLocator.getTimeFor(20, 45));
    }

    @Test
    public void testGetTimeLocator_PIRATA() throws IOException {
        final File insituDataFile = getPirataProduct();

        reader.open(insituDataFile);

        final TimeLocator timeLocator = reader.getTimeLocator();
        assertEquals(1475280000000L, timeLocator.getTimeFor(0, 0));
        assertEquals(1475337600000L, timeLocator.getTimeFor(10, 16));
        assertEquals(1475391600000L, timeLocator.getTimeFor(20, 31));

        assertEquals(-1L, timeLocator.getTimeFor(20, -1));
        assertEquals(-1L, timeLocator.getTimeFor(20, 1734));
    }

    @Test
    public void testReadRaw_TAO() throws IOException, InvalidRangeException {
        final File insituDataFile = getTAOProduct();

        reader.open(insituDataFile);

        final Interval singlePx = new Interval(1, 1);

        // x is ignored
        Array array = reader.readRaw(8, 451, singlePx, "time");
        assertEquals(DataType.INT, array.getDataType());
        assertEquals(1466362800, array.getInt(0));

        array = reader.readRaw(9, 452, singlePx, "longitude");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(-140.02f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 453, singlePx, "latitude");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(-2.04f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 454, singlePx, "SSS");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(35.631f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 455, singlePx, "SST");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(27.316f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 456, singlePx, "AIRT");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(26.83f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 457, singlePx, "RH");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(82.99f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 458, singlePx, "WSPD");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(8.1f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 459, singlePx, "WDIR");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(265.f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 460, singlePx, "BARO");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(-9.9f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 461, singlePx, "RAIN");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(-9.99f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 462, singlePx, "Q");
        assertEquals(DataType.INT, array.getDataType());
        assertEquals(11111199, array.getInt(0));

        array = reader.readRaw(9, 463, singlePx, "M");
        assertEquals(DataType.STRING, array.getDataType());
        assertEquals("DDDDDDDD", array.getObject(0));
    }

    @Test
    public void testReadRaw_TRITON_3x3() throws IOException, InvalidRangeException {
        final File insituDataFile = getTritonProduct();

        reader.open(insituDataFile);

        Array array = reader.readRaw(9, 19, new Interval(3, 3), "SSS");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(-9.999f, array.getFloat(0), 1e-8);
        assertEquals(35.113f, array.getFloat(4), 1e-8);
        assertEquals(-9.999f, array.getFloat(8), 1e-8);
    }

    @Test
    public void testReadRaw_RAMA() throws IOException, InvalidRangeException {
        final File insituDataFile = getRamaProduct();

        reader.open(insituDataFile);

        final Interval singlePx = new Interval(1, 1);

        // x is ignored
        Array array = reader.readRaw(8, 452, singlePx, "time");
        assertEquals(DataType.INT, array.getDataType());
        assertEquals(1524168000, array.getInt(0));

        array = reader.readRaw(9, 453, singlePx, "longitude");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(80.5f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 454, singlePx, "latitude");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(-8.f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 455, singlePx, "SSS");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(34.251f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 456, singlePx, "SST");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(28.6f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 457, singlePx, "AIRT");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(27.6f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 458, singlePx, "RH");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(84.1f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 459, singlePx, "WSPD");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(6.6f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 460, singlePx, "WDIR");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(295.4f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 461, singlePx, "BARO");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(1010.6f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 462, singlePx, "RAIN");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(0.f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 463, singlePx, "Q");
        assertEquals(DataType.INT, array.getDataType());
        assertEquals(22222222, array.getInt(0));

        array = reader.readRaw(9, 464, singlePx, "M");
        assertEquals(DataType.STRING, array.getDataType());
        assertEquals("11111111", array.getObject(0));
    }

    @Test
    public void testReadScaled_TRITON() throws IOException, InvalidRangeException {
        final File insituDataFile = getTritonProduct();

        reader.open(insituDataFile);

        final Interval singlePx = new Interval(1, 1);

        // x is ignored
        Array array = reader.readScaled(8, 5, singlePx, "time");
        assertEquals(DataType.INT, array.getDataType());
        assertEquals(1507291200, array.getInt(0));

        array = reader.readScaled(9, 6, singlePx, "longitude");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(156.f, array.getFloat(0), 1e-8);

        array = reader.readScaled(9, 7, singlePx, "latitude");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(0.f, array.getFloat(0), 1e-8);

        array = reader.readScaled(9, 8, singlePx, "SSS");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(34.877f, array.getFloat(0), 1e-8);

        array = reader.readScaled(9, 9, singlePx, "SST");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(29.69f, array.getFloat(0), 1e-8);

        array = reader.readScaled(9, 10, singlePx, "AIRT");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(28.79f, array.getFloat(0), 1e-8);

        array = reader.readScaled(9, 11, singlePx, "RH");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(78.37f, array.getFloat(0), 1e-8);

        array = reader.readScaled(9, 12, singlePx, "WSPD");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(7.f, array.getFloat(0), 1e-8);

        array = reader.readScaled(9, 13, singlePx, "WDIR");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(300.7f, array.getFloat(0), 1e-8);

        array = reader.readScaled(9, 14, singlePx, "BARO");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(1005.3f, array.getFloat(0), 1e-8);

        array = reader.readScaled(9, 15, singlePx, "RAIN");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(0.f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 16, singlePx, "Q");
        assertEquals(DataType.INT, array.getDataType());
        assertEquals(22222222, array.getInt(0));

        array = reader.readScaled(9, 17, singlePx, "M");
        assertEquals(DataType.STRING, array.getDataType());
        assertEquals("MMMMMMMM", array.getObject(0));
    }

    @Test
    public void testReadScaled_PIRATA() throws IOException, InvalidRangeException {
        final File insituDataFile = getPirataProduct();

        reader.open(insituDataFile);

        final Interval singlePx = new Interval(1, 1);

        // x is ignored
        Array array = reader.readScaled(8, 6, singlePx, "time");
        assertEquals(DataType.INT, array.getDataType());
        assertEquals(1475301600, array.getInt(0));

        array = reader.readScaled(9, 7, singlePx, "longitude");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(-35.f, array.getFloat(0), 1e-8);

        array = reader.readScaled(9, 8, singlePx, "latitude");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(0.f, array.getFloat(0), 1e-8);

        array = reader.readScaled(9, 9, singlePx, "SSS");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(35.649f, array.getFloat(0), 1e-8);

        array = reader.readScaled(9, 10, singlePx, "SST");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(-9.999f, array.getFloat(0), 1e-8);

        array = reader.readScaled(9, 11, singlePx, "AIRT");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(-9.99f, array.getFloat(0), 1e-8);

        array = reader.readScaled(9, 12, singlePx, "RH");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(-9.99f, array.getFloat(0), 1e-8);

        array = reader.readScaled(9, 13, singlePx, "WSPD");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(-99.9f, array.getFloat(0), 1e-8);

        array = reader.readScaled(9, 14, singlePx, "WDIR");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(-99.9f, array.getFloat(0), 1e-8);

        array = reader.readScaled(9, 15, singlePx, "BARO");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(-9.9f, array.getFloat(0), 1e-8);

        array = reader.readScaled(9, 16, singlePx, "RAIN");
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(-9.99f, array.getFloat(0), 1e-8);

        array = reader.readRaw(9, 17, singlePx, "Q");
        assertEquals(DataType.INT, array.getDataType());
        assertEquals(39999999, array.getInt(0));

        array = reader.readScaled(9, 18, singlePx, "M");
        assertEquals(DataType.STRING, array.getDataType());
        assertEquals("5DDDDDDD", array.getObject(0));
    }

    @Test
    public void testReadAcquisitionTime_TAO() throws IOException, InvalidRangeException {
        final File insituDataFile = getTAOProduct();

        reader.open(insituDataFile);

        ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(3, 701, new Interval(1, 1));
        assertEquals(1467262800, acquisitionTime.getInt(0));
    }

    @Test
    public void testReadAcquisitionTime_RAMA() throws IOException, InvalidRangeException {
        final File insituDataFile = getRamaProduct();

        reader.open(insituDataFile);

        ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(3, 702, new Interval(1, 1));
        assertEquals(1525068000, acquisitionTime.getInt(0));
    }

    private static File getTAOProduct() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "tao-sss", "v1", "2016", "06", "TAO_T2S140W_DM167A-20160228_2016-06.txt"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private static File getTritonProduct() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "tao-sss", "v1", "2017", "10", "TRITON_TR0N156E_1998_2017-10.txt"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private static File getPirataProduct() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "pirata-sss", "v1", "2016", "10", "PIRATA_0N35W_sss_2016-10.txt"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private static File getRamaProduct() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "rama-sss", "v1", "2018", "04", "RAMA_8S805E_sss_2018-04.txt"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}

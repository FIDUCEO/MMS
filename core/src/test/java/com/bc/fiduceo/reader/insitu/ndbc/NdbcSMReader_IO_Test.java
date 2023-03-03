package com.bc.fiduceo.reader.insitu.ndbc;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.time.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class NdbcSMReader_IO_Test {

    private NdbcSMReader reader;

    @Before
    public void setUp()  {
        reader = new NdbcSMReader();
    }

    @Test
    public void testReadAcquisitionInfo_coastBuoy() throws IOException {
        final File testFile = getCOAST_BUOY();

        try {
            reader.open(testFile);
            final AcquisitionInfo info = reader.read();

            TestUtil.assertCorrectUTCDate(2017, 1, 1, 0, 0, 0, 0, info.getSensingStart());
            TestUtil.assertCorrectUTCDate(2017, 10, 19, 1, 0, 0, 0, info.getSensingStop());

            assertEquals(NodeType.UNDEFINED, info.getNodeType());

            assertNull(info.getBoundingGeometry());
            assertNull(info.getTimeAxes());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_oceanStation() throws IOException {
        final File testFile = getOCEAN_STATION();

        try {
            reader.open(testFile);
            final AcquisitionInfo info = reader.read();

            TestUtil.assertCorrectUTCDate(2017, 1, 1, 0, 0, 0, 0, info.getSensingStart());
            TestUtil.assertCorrectUTCDate(2017, 12, 31, 23, 54, 0, 0, info.getSensingStop());

            assertEquals(NodeType.UNDEFINED, info.getNodeType());

            assertNull(info.getBoundingGeometry());
            assertNull(info.getTimeAxes());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_coastBuoy() throws IOException {
        final File testFile = getCOAST_BUOY();

        try {
            reader.open(testFile);

            final Dimension productSize = reader.getProductSize();

            assertNotNull(productSize);
            assertEquals("product_size", productSize.getName());
            assertEquals(1, productSize.getNx());
            assertEquals(41834, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator_oceanStation() throws IOException {
        final File testFile = getOCEAN_STATION();

        try {
            reader.open(testFile);

            reader.getPixelLocator();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetSubScenePixelLocator_coastBuoy() throws IOException {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Polygon polygon = geometryFactory.createPolygon(Arrays.asList(
                geometryFactory.createPoint(6, 9),
                geometryFactory.createPoint(7, 0),
                geometryFactory.createPoint(7, 10)
        ));

        final File testFile = getCOAST_BUOY();

        try {
            reader.open(testFile);
            reader.getSubScenePixelLocator(polygon);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_oceanStation() throws IOException {
        final File testFile = getOCEAN_STATION();

        try {
            reader.open(testFile);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertEquals(1483228800000L, timeLocator.getTimeFor(0, 0));
            assertEquals(1498017600000L, timeLocator.getTimeFor(10, 357));
            assertEquals(1498186080000L, timeLocator.getTimeFor(20, 823));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_coastBuoy() throws IOException {
        final File testFile = getCOAST_BUOY();

        try {
            reader.open(testFile);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertEquals(1483228800000L, timeLocator.getTimeFor(0, 0));
            assertEquals(1483443000000L, timeLocator.getTimeFor(10, 357));
            assertEquals(1483722600000L, timeLocator.getTimeFor(20, 823));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_1x1_oceanStation() throws InvalidRangeException, IOException {
        final File testFile = getOCEAN_STATION();

        try {
            reader.open(testFile);

            Array array = reader.readRaw(7, 2004, new Interval(1, 1), "station_id");
            assertEquals(DataType.STRING, array.getDataType());
            assertEquals("BRND1", array.getObject(0));

            array = reader.readRaw(8, 2005, new Interval(1, 1), "longitude");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(-75.113f, array.getFloat(0), 1e-8);

            array = reader.readRaw(8, 2005, new Interval(1, 1), "air_temp_height");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(20.6f, array.getFloat(0), 1e-8);

            array = reader.readRaw(8, 2006, new Interval(1, 1), "time");
            assertEquals(DataType.INT, array.getDataType());
            assertEquals(1498611960, array.getInt(0));

            array = reader.readRaw(8, 2007, new Interval(1, 1), "GST");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(6.7f, array.getFloat(0), 1e-8);

            array = reader.readRaw(8, 2008, new Interval(1, 1), "APD");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(99.f, array.getFloat(0), 1e-8);

            array = reader.readRaw(8, 2009, new Interval(1, 1), "ATMP");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(22.7f, array.getFloat(0), 1e-8);

            array = reader.readRaw(8, 2009, new Interval(1, 1), "TIDE");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(99.f, array.getFloat(0), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_1x1_coastBuoy() throws InvalidRangeException, IOException {
        final File testFile = getCOAST_BUOY();

        try {
            reader.open(testFile);

            Array array = reader.readRaw(7, 2010, new Interval(1, 1), "station_type");
            assertEquals(DataType.BYTE, array.getDataType());
            assertEquals(1, array.getByte(0));

            array = reader.readRaw(8, 2011, new Interval(1, 1), "latitude");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(11.301f, array.getFloat(0), 1e-8);

            array = reader.readRaw(8, 2012, new Interval(1, 1), "barometer_height");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(Float.NaN, array.getFloat(0), 1e-8);

            array = reader.readRaw(8, 2013, new Interval(1, 1), "WDIR");
            assertEquals(DataType.SHORT, array.getDataType());
            assertEquals(38, array.getShort(0));

            array = reader.readRaw(8, 2014, new Interval(1, 1), "WVHT");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(99.f, array.getFloat(0), 1e-8);

            array = reader.readRaw(8, 2015, new Interval(1, 1), "MWD");
            assertEquals(DataType.SHORT, array.getDataType());
            assertEquals(999, array.getShort(0));

            array = reader.readRaw(8, 2016, new Interval(1, 1), "DEWP");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(20.2f, array.getFloat(0), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_1x1_oceanStation() throws InvalidRangeException, IOException {
        final File testFile = getOCEAN_STATION();

        try {
            reader.open(testFile);

            Array array = reader.readScaled(13, 2017, new Interval(1, 1), "measurement_type");
            assertEquals(DataType.BYTE, array.getDataType());
            assertEquals(1, array.getByte(0));

            array = reader.readRaw(14, 2018, new Interval(1, 1), "anemometer_height");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(21.1f, array.getFloat(0), 1e-8);

            array = reader.readRaw(14, 2019, new Interval(1, 1), "sst_depth");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(0.46f, array.getFloat(0), 1e-8);

            array = reader.readRaw(15, 2020, new Interval(1, 1), "WSPD");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(5.2f, array.getFloat(0), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_1x1_coastBuoy() throws InvalidRangeException, IOException {
        final File testFile = getCOAST_BUOY();

        try {
            reader.open(testFile);

            Array array = reader.readScaled(13, 2021, new Interval(1, 1), "DPD");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(99.f, array.getFloat(0), 1e-8);

            array = reader.readRaw(14, 2022, new Interval(1, 1), "PRES");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(1013.f, array.getFloat(0), 1e-8);

            array = reader.readRaw(14, 2023, new Interval(1, 1), "VIS");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(99.f, array.getFloat(0), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_1x1_oceanStation() throws IOException, InvalidRangeException {
        final File testFile = getOCEAN_STATION();

        try {
            reader.open(testFile);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(13, 36, new Interval(1, 1));
            NCTestUtils.assertValueAt(1483241760, 0, 0, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_3x1_coastBuoy() throws IOException, InvalidRangeException {
        final File testFile = getCOAST_BUOY();

        try {
            reader.open(testFile);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(14, 37, new Interval(3, 1));
            NCTestUtils.assertValueAt(-2147483647, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1483251000, 1, 0, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 2, 0, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    private static File getCOAST_BUOY() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "ndbc", "ndbc-sm-cb", "v1", "2017", "42088h2017.txt"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }

    private static File getOCEAN_STATION() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "ndbc", "ndbc-sm-os", "v1", "2017", "brnd1h2017.txt"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }
}

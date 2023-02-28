package com.bc.fiduceo.reader.insitu.ndbc;

import com.bc.fiduceo.IOTestRunner;
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
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class NdbcCWReader_IO_Test {

    private NdbcCWReader reader;

    @Before
    public void setUp()  {
        reader = new NdbcCWReader();
    }

    @Test
    public void testReadAcquisitionInfo_oceanBuoy() throws IOException {
        final File testFile = getOCEAN_BUOY();

        try {
            reader.open(testFile);

            final AcquisitionInfo info = reader.read();
            TestUtil.assertCorrectUTCDate(2016, 5, 31, 23, 0, 0, 0, info.getSensingStart());
            TestUtil.assertCorrectUTCDate(2016, 12, 31, 22, 50, 0, 0, info.getSensingStop());

            assertEquals(NodeType.UNDEFINED, info.getNodeType());

            assertNull(info.getBoundingGeometry());
            assertNull(info.getTimeAxes());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_lakeBuoy() throws IOException {
        final File testFile = getLAKE_BUOY();

        try {
            reader.open(testFile);

            final AcquisitionInfo info = reader.read();
            TestUtil.assertCorrectUTCDate(2017, 5, 5, 10, 0, 0, 0, info.getSensingStart());
            TestUtil.assertCorrectUTCDate(2017, 10, 23, 11, 50, 0, 0, info.getSensingStop());

            assertEquals(NodeType.UNDEFINED, info.getNodeType());

            assertNull(info.getBoundingGeometry());
            assertNull(info.getTimeAxes());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_oceanBuoy() throws IOException {
        final File testFile = getOCEAN_BUOY();

        try {
            reader.open(testFile);

            final Dimension productSize = reader.getProductSize();

            assertNotNull(productSize);
            assertEquals("product_size", productSize.getName());
            assertEquals(1, productSize.getNx());
            assertEquals(30516, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator_lakeBuoy() throws IOException {
        final File testFile = getLAKE_BUOY();

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
    public void testGetSubScenePixelLocator_oceanBuoy() throws IOException {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Polygon polygon = geometryFactory.createPolygon(Arrays.asList(
                geometryFactory.createPoint(5, 9),
                geometryFactory.createPoint(6, 0),
                geometryFactory.createPoint(6, 10)
        ));

        final File testFile = getOCEAN_BUOY();

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
    public void testGetTimeLocator_lakeBuoy() throws IOException {
        final File testFile = getLAKE_BUOY();

        try {
            reader.open(testFile);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertEquals(1493978400000L, timeLocator.getTimeFor(0, 0));
            assertEquals(1494192000000L, timeLocator.getTimeFor(10, 356));
            assertEquals(1494478800000L, timeLocator.getTimeFor(20, 822));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_oceanBuoy() throws IOException {
        final File testFile = getOCEAN_BUOY();

        try {
            reader.open(testFile);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertEquals(1464735600000L, timeLocator.getTimeFor(0, 0));
            assertEquals(1465019400000L, timeLocator.getTimeFor(10, 467));
            assertEquals(1465306200000L, timeLocator.getTimeFor(20, 933));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_1x1_lakeBuoy() throws InvalidRangeException, IOException {
        final File testFile = getLAKE_BUOY();

        try {
            reader.open(testFile);

            Array array = reader.readRaw(7, 4, new Interval(1, 1), "station_type");
            assertEquals(DataType.BYTE, array.getDataType());
            assertEquals(2, array.getByte(0));

            array = reader.readRaw(8, 5, new Interval(1, 1), "latitude");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(48.061f, array.getFloat(0), 1e-8);

            array = reader.readRaw(9, 6, new Interval(1, 1), "barometer_height");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(Float.NaN, array.getFloat(0), 1e-8);

            array = reader.readRaw(10, 7, new Interval(1, 1), "barometer_height");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(Float.NaN, array.getFloat(0), 1e-8);

            array = reader.readRaw(11, 8, new Interval(1, 1), "WDIR");
            assertEquals(DataType.SHORT, array.getDataType());
            assertEquals(70, array.getShort(0));

            array = reader.readRaw(12, 9, new Interval(1, 1), "GST");
            assertEquals(99.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_1x1_oceanBuoy() throws InvalidRangeException, IOException {
        final File testFile = getOCEAN_BUOY();

        try {
            reader.open(testFile);

            Array array = reader.readRaw(13, 10, new Interval(1, 1), "measurement_type");
            assertEquals(DataType.BYTE, array.getDataType());
            assertEquals(0, array.getByte(0));

            array = reader.readRaw(14, 11, new Interval(1, 1), "anemometer_height");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(3.8f, array.getFloat(0), 1e-8);

            array = reader.readRaw(15, 11, new Interval(1, 1), "sst_depth");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(Float.NaN, array.getFloat(0), 1e-8);

            array = reader.readRaw(16, 12, new Interval(1, 1), "WSPD");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(6.4f, array.getFloat(0), 1e-8);

            array = reader.readRaw(17, 13, new Interval(1, 1), "GTIME");
            assertEquals(9999, array.getShort(0));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_1x1_oceanBuoy() throws InvalidRangeException, IOException {
        final File testFile = getOCEAN_BUOY();

        try {
            reader.open(testFile);

            Array array = reader.readScaled(13, 11, new Interval(1, 1), "air_temp_height");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(Float.NaN, array.getFloat(0), 1e-8);

            array = reader.readRaw(14, 12, new Interval(1, 1), "time");
            assertEquals(DataType.INT, array.getDataType());
            assertEquals(1464742800, array.getInt(0));

            array = reader.readRaw(15, 13, new Interval(1, 1), "GDR");
            assertEquals(DataType.SHORT, array.getDataType());
            assertEquals(999, array.getShort(0));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_1x1_lakeBuoy() throws InvalidRangeException, IOException {
        final File testFile = getLAKE_BUOY();

        try {
            reader.open(testFile);

            Array array = reader.readScaled(13, 14, new Interval(1, 1), "barometer_height");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(Float.NaN, array.getFloat(0), 1e-8);

            array = reader.readRaw(14, 15, new Interval(1, 1), "WDIR");
            assertEquals(DataType.SHORT, array.getDataType());
            assertEquals(24, array.getShort(0));

            array = reader.readRaw(15, 16, new Interval(1, 1), "GST");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(99.f, array.getFloat(0), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_3x3_lakeBuoy() throws InvalidRangeException, IOException {
        final File testFile = getLAKE_BUOY();

        try {
            reader.open(testFile);

            final Array array = reader.readRaw(7, 4, new Interval(3, 3), "longitude");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(Float.NaN, array.getFloat(0), 1e-8);
            assertEquals(Float.NaN, array.getFloat(2), 1e-8);
            assertEquals(-87.793f, array.getFloat(4), 1e-8);
            assertEquals(Float.NaN, array.getFloat(6), 1e-8);
            assertEquals(Float.NaN, array.getFloat(8), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_String() throws IOException, InvalidRangeException {
        final File testFile = getOCEAN_BUOY();

        try {
            reader.open(testFile);

            final Array array = reader.readRaw(7, 4, new Interval(3, 3), "station_id");
            assertEquals(DataType.STRING, array.getDataType());
            assertEquals("42002", array.getObject(0));
        } finally {
            reader.close();
        }
    }

    private static File getOCEAN_BUOY() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "ndbc", "ndbc-cw-ob", "v1", "2016", "42002c2016.txt"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }

    private static File getLAKE_BUOY() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "ndbc", "ndbc-cw-lb", "v1", "2017", "45001c2017.txt"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }
}

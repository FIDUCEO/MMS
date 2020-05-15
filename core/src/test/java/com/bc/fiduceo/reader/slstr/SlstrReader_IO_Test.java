package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.snap.SNAP_PixelLocator;
import com.bc.fiduceo.util.TempFileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class SlstrReader_IO_Test {

    private SlstrReader reader;
    private TempFileUtils tempFileUtils;

    @Before
    public void setUp() throws IOException {
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));
        tempFileUtils = new TempFileUtils();
        readerContext.setTempFileUtils(tempFileUtils);

        reader = new SlstrReader(readerContext, ProductType.ALL);
    }

    @After
    public void tearDown() {
        tempFileUtils.cleanup();
        tempFileUtils = null;
    }

    @Test
    public void testReadAcquisitionInfo_S3A() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2018, 10, 13, 22, 24, 36, 182, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2018, 10, 13, 22, 27, 36, 182, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.DESCENDING, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);
            final Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(27, coordinates.length);
            assertEquals(169.3240509033203, coordinates[0].getLon(), 1e-8);
            assertEquals(83.7747802734375, coordinates[0].getLat(), 1e-8);

            assertEquals(-148.5464630126953, coordinates[14].getLon(), 1e-8);
            assertEquals(69.24473571777344, coordinates[14].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2018, 10, 13, 22, 24, 36, 356, time);
            time = timeAxes[0].getTime(coordinates[15]);
            TestUtil.assertCorrectUTCDate(2018, 10, 13, 22, 27, 6, 290, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_S3A_zip() throws IOException {
        final File file = getS3A_zip_File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2018, 10, 26, 23, 16, 11, 490, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2018, 10, 26, 23, 19, 11, 490, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.ASCENDING, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);
            final Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(27, coordinates.length);
            assertEquals(-15.488251686096193, coordinates[0].getLon(), 1e-8);
            assertEquals(54.510459899902344, coordinates[0].getLat(), 1e-8);

            assertEquals(-43.97860717773437, coordinates[15].getLon(), 1e-8);
            assertEquals(59.59437942504883, coordinates[15].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2018, 10, 26, 23, 16, 11, 721, time);
            time = timeAxes[0].getTime(coordinates[16]);
            TestUtil.assertCorrectUTCDate(2018, 10, 26, 23, 18, 26, 529, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_S3A() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(1539469494226L, timeLocator.getTimeFor(15, 0));
            assertEquals(1539469509225L, timeLocator.getTimeFor(16, 100));
            assertEquals(1539469644219L, timeLocator.getTimeFor(1189, 1000));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_S3A_zip() throws IOException {
        final File file = getS3A_zip_File();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(1540595789732L, timeLocator.getTimeFor(15, 0));
            assertEquals(1540595804732L, timeLocator.getTimeFor(16, 100));
            assertEquals(1540595939725L, timeLocator.getTimeFor(1189, 1000));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_S3A_zip_timing_issue() throws IOException {
        final File file = getS3A_zip_File_timing_issue();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(1522886298063L, timeLocator.getTimeFor(16, 0));
            assertEquals(1522886328062L, timeLocator.getTimeFor(17, 200));
            assertEquals(1522886418058L, timeLocator.getTimeFor(810, 800));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_S3A() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(52, variables.size());

            Variable variable = variables.get(0);
            assertEquals("bayes_in", variable.getShortName());
            assertEquals(DataType.BYTE, variable.getDataType());

            variable = variables.get(12);
            assertEquals("S2_exception_ao", variable.getShortName());
            assertEquals(DataType.BYTE, variable.getDataType());

            variable = variables.get(27);
            assertEquals("S6_radiance_an", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(48);
            assertEquals("sat_azimuth_to", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_S3A() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertEquals(1500, productSize.getNx());
            assertEquals(1200, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_S3A() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(3, 3);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(1056, 624, interval);
            assertNotNull(acquisitionTime);

            NCTestUtils.assertValueAt(1539469588, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1539469588, 0, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1539469588, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1539469588, 1, 2, acquisitionTime);
            NCTestUtils.assertValueAt(1539469588, 2, 2, acquisitionTime);
            NCTestUtils.assertValueAt(1539469588, 2, 0, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_S3A_upper_right() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(1499, 1, interval);
            assertNotNull(acquisitionTime);

            NCTestUtils.assertValueAt(-2147483647, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1539469494, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1539469494, 1, 2, acquisitionTime);
            NCTestUtils.assertValueAt(1539469494, 2, 2, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 3, 2, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 4, 2, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_S3A_bottom() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(126, 1199, interval);
            assertNotNull(acquisitionTime);

            NCTestUtils.assertValueAt(1539469674, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1539469674, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1539469674, 1, 2, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 2, 3, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 3, 4, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_S3A_1km_nadir() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readScaled(337, 810, interval, "bayes_in");
            NCTestUtils.assertValueAt(2, 0, 0, array);
            NCTestUtils.assertValueAt(2, 1, 0, array);
            NCTestUtils.assertValueAt(2, 2, 0, array);

            array = reader.readScaled(662, 617, interval, "confidence_in");
            NCTestUtils.assertValueAt(17410, 3, 0, array);
            NCTestUtils.assertValueAt(17410, 4, 0, array);
            NCTestUtils.assertValueAt(25602, 0, 1, array);

            array = reader.readScaled(1265, 244, interval, "S9_exception_in");
            NCTestUtils.assertValueAt(2, 1, 1, array);
            NCTestUtils.assertValueAt(0, 2, 1, array);
            NCTestUtils.assertValueAt(0, 3, 1, array);

            array = reader.readScaled(113, 955, interval, "S9_BT_in");
            NCTestUtils.assertValueAt(251.02000427246094, 4, 1, array);
            NCTestUtils.assertValueAt(245.8300018310547, 0, 2, array);
            NCTestUtils.assertValueAt(248.49000549316406, 1, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_S3A_1km_nadir_bottom() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final Array array = reader.readScaled(1483, 1199, interval, "S8_BT_in");
            NCTestUtils.assertValueAt(267.5400085449219, 2, 0, array);
            NCTestUtils.assertValueAt(266.4599914550781, 2, 1, array);
            NCTestUtils.assertValueAt(266.4599914550781, 2, 2, array);
            NCTestUtils.assertValueAt(-43.95000076293945, 2, 3, array);
            NCTestUtils.assertValueAt(-43.95000076293945, 2, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_S3A_1km_nadir() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(338, 811, interval, "S7_BT_in");
            NCTestUtils.assertValueAt(-1479, 0, 0, array);
            NCTestUtils.assertValueAt(-1450, 1, 0, array);
            NCTestUtils.assertValueAt(-1450, 2, 0, array);

            array = reader.readRaw(663, 618, interval, "S8_exception_in");
            NCTestUtils.assertValueAt(0, 3, 0, array);
            NCTestUtils.assertValueAt(0, 4, 0, array);
            NCTestUtils.assertValueAt(0, 0, 1, array);

            array = reader.readRaw(1266, 245, interval, "pointing_in");
            NCTestUtils.assertValueAt(0, 1, 1, array);
            NCTestUtils.assertValueAt(0, 2, 1, array);
            NCTestUtils.assertValueAt(0, 3, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_S3A_1km_nadir_crashed() throws IOException {
        final File file = getS3A_zip_File_crashed_reading();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(1453, 10, interval, "bayes_io");
            NCTestUtils.assertValueAt(-1, 0, 1, array);
            NCTestUtils.assertValueAt(-1, 1, 1, array);
            NCTestUtils.assertValueAt(-1, 2, 1, array);
            NCTestUtils.assertValueAt(-1, 3, 1, array);
            NCTestUtils.assertValueAt(-1, 4, 1, array);

            array = reader.readRaw(1447, 10, interval, "bayes_io");
            NCTestUtils.assertValueAt(0, 0, 1, array);
            NCTestUtils.assertValueAt(0, 1, 1, array);
            NCTestUtils.assertValueAt(0, 2, 1, array);
            NCTestUtils.assertValueAt(-1, 3, 1, array);
            NCTestUtils.assertValueAt(-1, 4, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_S3A_1km_nadir_left() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final Array array = reader.readScaled(0, 318, interval, "S7_exception_in");
            NCTestUtils.assertValueAt(-1, 0, 1, array);
            NCTestUtils.assertValueAt(-1, 1, 1, array);
            NCTestUtils.assertValueAt(128, 2, 1, array);
            NCTestUtils.assertValueAt(128, 3, 1, array);
            NCTestUtils.assertValueAt(128, 4, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_S3A_500m_nadir() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readScaled(339, 812, interval, "sat_azimuth_tn");
            NCTestUtils.assertValueAt(117.35515594482422, 0, 0, array);
            NCTestUtils.assertValueAt(117.35452270507812, 1, 0, array);
            NCTestUtils.assertValueAt(117.35389709472656, 2, 0, array);

            array = reader.readScaled(662, 617, interval, "S1_radiance_an");
            NCTestUtils.assertValueAt(19.536823272705078, 3, 0, array);
            NCTestUtils.assertValueAt(19.34047508239746, 4, 0, array);
            NCTestUtils.assertValueAt(18.055274963378906, 0, 1, array);

            array = reader.readScaled(1256, 239, interval, "S5_radiance_an");
            NCTestUtils.assertValueAt(2.7090353965759277, 1, 1, array);
            NCTestUtils.assertValueAt(1.8899459838867188, 2, 2, array);
            NCTestUtils.assertValueAt(0.53175902366638182, 3, 3, array);
            NCTestUtils.assertValueAt(0.5018404722213745, 4, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_S3A_500m_nadir_upper() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readScaled(100, 0, interval, "S3_radiance_an");
            NCTestUtils.assertValueAt(-294.9119873046875, 0, 0, array);
            NCTestUtils.assertValueAt(-294.9119873046875, 4, 0, array);

            NCTestUtils.assertValueAt(-294.9119873046875, 0, 1, array);
            NCTestUtils.assertValueAt(-294.9119873046875, 3, 1, array);

            NCTestUtils.assertValueAt(1.4670000076293945, 0, 2, array);
            NCTestUtils.assertValueAt(1.3252500295639038, 2, 2, array);
            NCTestUtils.assertValueAt(1.2734999656677246, 4, 2, array);

            NCTestUtils.assertValueAt(1.3792500495910645, 0, 3, array);
            NCTestUtils.assertValueAt(1.3680000305175781, 2, 3, array);
            NCTestUtils.assertValueAt(1.4197499752044678, 4, 3, array);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_S3A_500m_nadir() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(340, 813, interval, "S2_radiance_an");
            NCTestUtils.assertValueAt(659, 0, 0, array);
            NCTestUtils.assertValueAt(666, 1, 0, array);
            NCTestUtils.assertValueAt(672, 2, 0, array);

            array = reader.readRaw(663, 618, interval, "S3_radiance_an");
            NCTestUtils.assertValueAt(1045, 3, 0, array);
            NCTestUtils.assertValueAt(907, 4, 0, array);
            NCTestUtils.assertValueAt(1124, 0, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_S3A_500m_nadir_bottom() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(688, 1199, interval, "S4_radiance_an");
            NCTestUtils.assertValueAt(28, 0, 0, array);
            NCTestUtils.assertValueAt(18, 0, 1, array);
            NCTestUtils.assertValueAt(26, 0, 2, array);
            NCTestUtils.assertValueAt(-32768, 0, 3, array);
            NCTestUtils.assertValueAt(-32768, 0, 4, array);
            NCTestUtils.assertValueAt(24, 3, 0, array);
            NCTestUtils.assertValueAt(12, 3, 1, array);
            NCTestUtils.assertValueAt(23, 3, 2, array);
            NCTestUtils.assertValueAt(-32768, 3, 3, array);
            NCTestUtils.assertValueAt(-32768, 3, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_S3A_500m_nadir_flags() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(730, 542, interval, "S5_exception_an");
            NCTestUtils.assertValueAt(0, 0, 0, array);
            NCTestUtils.assertValueAt(2, 1, 1, array);
            NCTestUtils.assertValueAt(2, 2, 2, array);

            array = reader.readRaw(733, 592, interval, "S6_exception_an");
            NCTestUtils.assertValueAt(2, 0, 0, array);
            NCTestUtils.assertValueAt(2, 1, 1, array);
            NCTestUtils.assertValueAt(34, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_S3A_500m_nadir_flags_top_left() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(0, 0, interval, "S1_exception_an");
            NCTestUtils.assertValueAt(-1, 0, 0, array);
            NCTestUtils.assertValueAt(-1, 0, 1, array);
            NCTestUtils.assertValueAt(-1, 0, 2, array);
            NCTestUtils.assertValueAt(-1, 0, 3, array);
            NCTestUtils.assertValueAt(-1, 0, 4, array);
            NCTestUtils.assertValueAt(-1, 3, 0, array);
            NCTestUtils.assertValueAt(-1, 3, 1, array);
            NCTestUtils.assertValueAt(-128, 3, 2, array);
            NCTestUtils.assertValueAt(-128, 3, 3, array);
            NCTestUtils.assertValueAt(-128, 3, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_S3A_1km_oblique() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readScaled(595, 194, interval, "bayes_io");
            NCTestUtils.assertValueAt(0, 0, 2, array);
            NCTestUtils.assertValueAt(0, 1, 2, array);
            NCTestUtils.assertValueAt(2, 2, 2, array);
            NCTestUtils.assertValueAt(2, 3, 2, array);
            NCTestUtils.assertValueAt(0, 4, 2, array);

            array = reader.readScaled(662, 617, interval, "S7_BT_io");
            NCTestUtils.assertValueAt(269.9200134277344, 3, 3, array);
            NCTestUtils.assertValueAt(269.9200134277344, 4, 3, array);
            NCTestUtils.assertValueAt(271.6700134277344, 0, 4, array);

            array = reader.readScaled(1414, 320, interval, "S9_exception_io");
            NCTestUtils.assertValueAt(0, 0, 2, array);
            NCTestUtils.assertValueAt(2, 1, 2, array);
            NCTestUtils.assertValueAt(2, 2, 2, array);
            NCTestUtils.assertValueAt(128, 3, 2, array);
            NCTestUtils.assertValueAt(128, 4, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_S3A_1km_oblique_top() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final Array array = reader.readScaled(1208, 0, interval, "S8_BT_io");
            NCTestUtils.assertValueAt(-43.95000076293945, 2, 0, array);
            NCTestUtils.assertValueAt(-43.95000076293945, 2, 1, array);
            NCTestUtils.assertValueAt(257.1700134277344, 2, 2, array);
            NCTestUtils.assertValueAt(257.1199951171875, 2, 3, array);
            NCTestUtils.assertValueAt(257.6199951171875, 2, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_S3A_1km_oblique_rasterLeft() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final Array array = reader.readScaled(548, 573, interval, "S9_exception_io");
            NCTestUtils.assertValueAt(-1.0, 0, 2, array);
            NCTestUtils.assertValueAt(-1.0, 1, 2, array);
            NCTestUtils.assertValueAt(128.0, 2, 2, array);
            NCTestUtils.assertValueAt(128.0, 3, 2, array);
            NCTestUtils.assertValueAt(128.0, 4, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_S3A_1km_oblique() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(639, 812, interval, "cloud_io");
            NCTestUtils.assertValueAt(192, 0, 0, array);
            NCTestUtils.assertValueAt(64, 1, 0, array);
            NCTestUtils.assertValueAt(192, 2, 0, array);

            array = reader.readRaw(664, 619, interval, "S7_BT_io");
            NCTestUtils.assertValueAt(-1399, 3, 0, array);
            NCTestUtils.assertValueAt(-1378, 4, 0, array);
            NCTestUtils.assertValueAt(-1276, 0, 1, array);

            array = reader.readRaw(1267, 246, interval, "S8_exception_io");
            NCTestUtils.assertValueAt(2, 1, 1, array);
            NCTestUtils.assertValueAt(0, 2, 1, array);
            NCTestUtils.assertValueAt(2, 3, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_S3A_1km_oblique_bottom() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final Array array = reader.readRaw(657, 1199, interval, "S8_BT_io");
            NCTestUtils.assertValueAt(-1761, 2, 0, array);
            NCTestUtils.assertValueAt(-1777, 2, 1, array);
            NCTestUtils.assertValueAt(-1772, 2, 2, array);
            NCTestUtils.assertValueAt(-32768, 2, 3, array);
            NCTestUtils.assertValueAt(-32768, 2, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_S3A_500m_oblique() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readScaled(739, 812, interval, "sat_azimuth_to");
            NCTestUtils.assertValueAt(190.94570922851562, 1, 0, array);
            NCTestUtils.assertValueAt(191.03924560546875, 2, 0, array);
            NCTestUtils.assertValueAt(191.1328125, 3, 0, array);

            array = reader.readScaled(662, 618, interval, "S1_radiance_ao");
            NCTestUtils.assertValueAt(30.425325393676758, 4, 0, array);
            NCTestUtils.assertValueAt(33.36164855957031, 0, 1, array);
            NCTestUtils.assertValueAt(33.04927444458008, 1, 1, array);

            array = reader.readScaled(1156, 240, interval, "S6_radiance_ao");
            NCTestUtils.assertValueAt(0.12057500332593918, 2, 1, array);
            NCTestUtils.assertValueAt(0.15285199880599976, 3, 2, array);
            NCTestUtils.assertValueAt(0.14821450412273407, 4, 3, array);
            NCTestUtils.assertValueAt(0.15656200051307678, 0, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_S3A_500m_oblique_top() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readScaled(600, 0, interval, "S3_radiance_ao");
            NCTestUtils.assertValueAt(-294.9119873046875, 0, 0, array);
            NCTestUtils.assertValueAt(-294.9119873046875, 4, 0, array);

            NCTestUtils.assertValueAt(-294.9119873046875, 0, 1, array);
            NCTestUtils.assertValueAt(-294.9119873046875, 3, 1, array);

            NCTestUtils.assertValueAt(6.317999839782715, 0, 2, array);
            NCTestUtils.assertValueAt(7.083000183105469, 2, 2, array);
            NCTestUtils.assertValueAt(7.625249862670898, 4, 2, array);

            NCTestUtils.assertValueAt(6.290999889373779, 0, 3, array);
            NCTestUtils.assertValueAt(6.934499740600586, 2, 3, array);
            NCTestUtils.assertValueAt(7.897500038146973, 4, 3, array);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_S3A_500m_oblique() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(840, 813, interval, "S4_radiance_ao");
            NCTestUtils.assertValueAt(16, 0, 0, array);
            NCTestUtils.assertValueAt(19, 1, 0, array);
            NCTestUtils.assertValueAt(-1, 2, 0, array);

            array = reader.readRaw(664, 619, interval, "S6_radiance_ao");
            NCTestUtils.assertValueAt(782, 3, 0, array);
            NCTestUtils.assertValueAt(945, 4, 0, array);
            NCTestUtils.assertValueAt(971, 0, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_S3A_500m_oblique_bottom() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(788, 1199, interval, "S2_radiance_ao");
            NCTestUtils.assertValueAt(3738, 0, 0, array);
            NCTestUtils.assertValueAt(3269, 0, 1, array);
            NCTestUtils.assertValueAt(3244, 0, 2, array);
            NCTestUtils.assertValueAt(-32768, 0, 3, array);
            NCTestUtils.assertValueAt(-32768, 0, 4, array);
            NCTestUtils.assertValueAt(3671, 3, 0, array);
            NCTestUtils.assertValueAt(3678, 3, 1, array);
            NCTestUtils.assertValueAt(3920, 3, 2, array);
            NCTestUtils.assertValueAt(-32768, 3, 3, array);
            NCTestUtils.assertValueAt(-32768, 3, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_S3A_500m_oblique_flags() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(731, 543, interval, "S2_exception_ao");
            NCTestUtils.assertValueAt(0, 0, 0, array);
            NCTestUtils.assertValueAt(2, 1, 1, array);
            NCTestUtils.assertValueAt(0, 2, 2, array);

            array = reader.readRaw(734, 593, interval, "S5_exception_ao");
            NCTestUtils.assertValueAt(2, 0, 0, array);
            NCTestUtils.assertValueAt(2, 1, 1, array);
            NCTestUtils.assertValueAt(2, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            Point2D geoLocation = pixelLocator.getGeoLocation(144.5, 1044.5, null);
            assertEquals(175.59606804163354, geoLocation.getX(), 1e-8);
            assertEquals(74.64973211288452, geoLocation.getY(), 1e-8);

            Point2D[] pixelLocation = pixelLocator.getPixelLocation(175.59606804163354, 74.64973231778033);
            assertEquals(1, pixelLocation.length);
            assertEquals(144.49764200959243, pixelLocation[0].getX(), 1e-8);
            assertEquals(1044.5169966986932, pixelLocation[0].getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(667.5, 804.5, null);
            assertEquals(-164.62233701811968, geoLocation.getX(), 1e-8);
            assertEquals(75.498695373535167, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(1000.5, 850.5, null);
            assertEquals(-155.51157004968402, geoLocation.getX(), 1e-8);
            assertEquals(73.69826984405518, geoLocation.getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(-155.51157004968402, 73.69826950929934);
            assertEquals(1, pixelLocation.length);
            assertEquals(1000.4949924312589, pixelLocation[0].getX(), 1e-8);
            assertEquals(850.5086006908218, pixelLocation[0].getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(1724, -89);
            assertEquals(0, pixelLocation.length);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testPixelLocatorConsistentWithLonLatBands() throws IOException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getPixelLocator();

            final double x = 244.5;
            final double y = 1044.5;

            final Point2D geoLocation = pixelLocator.getGeoLocation(x, y, null);
            assertEquals(178.94019381241702, geoLocation.getX(), 1e-8);
            assertEquals(74.53633832931519, geoLocation.getY(), 1e-8);

            final Interval interval = new Interval(1, 1);
            final Array lonArray = reader.readRaw((int) x, (int) y, interval, "longitude_tx");
            final Array latArray = reader.readRaw((int) x, (int) y, interval, "latitude_tx");

            assertEquals(178.94019381241702, lonArray.getFloat(0), 1e-1);
            assertEquals(74.536338250559, latArray.getFloat(0), 1e-2);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetSubScenePixelLocator() throws IOException {
        final File file = getS3AFile();

        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.JTS);
        final Polygon polygon = (Polygon) geometryFactory.parse("POLYGON((-1 1, 0 0, 0 -2, -1 1))");

        try {
            reader.open(file);

            // polygon is supplied just for interface compatibility ... is ignored in this reasder tb 2019-06-04
            final PixelLocator pixelLocator = reader.getSubScenePixelLocator(polygon);
            assertNotNull(pixelLocator);
            assertTrue(pixelLocator instanceof SNAP_PixelLocator);
        } finally {
            reader.close();
        }
    }

    private File getS3AFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"slstr-s3a", "1.0", "2018", "10", "13", "S3A_SL_1_RBT____20181013T222436_20181013T222736_20181015T035102_0179_037_001_1620_LN2_O_NT_003.SEN3", "xfdumanifest.xml"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File getS3A_zip_File() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"slstr-s3a", "1.0", "2018", "10", "26", "S3A_SL_1_RBT____20181026T231611_20181026T231911_20181028T023445_0180_037_187_0900_LN2_O_NT_003.zip"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File getS3A_zip_File_crashed_reading() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"slstr-s3a", "1.0", "2018", "06", "17", "S3A_SL_1_RBT____20180617T012237_20180617T012537_20180617T034823_0179_032_231_1080_SVL_O_NR_003.zip"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File getS3A_zip_File_timing_issue() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"slstr-s3a", "1.0", "2018", "04", "04", "S3A_SL_1_RBT____20180404T235800_20180405T000012_20181006T172032_0132_029_344______LR1_R_NT_003.zip"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}

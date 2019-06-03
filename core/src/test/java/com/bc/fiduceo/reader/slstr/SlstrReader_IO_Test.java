package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class SlstrReader_IO_Test {

    private File dataDirectory;
    private SlstrReader reader;

    @Before
    public void setUp() throws IOException {
        dataDirectory = TestUtil.getTestDataDirectory();

        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new SlstrReader(readerContext);
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
            assertEquals(29, coordinates.length);
            assertEquals(168.0067138671875, coordinates[0].getLon(), 1e-8);
            assertEquals(83.76530456542969, coordinates[0].getLat(), 1e-8);

            assertEquals(-141.01283264160156, coordinates[14].getLon(), 1e-8);
            assertEquals(65.22335052490234, coordinates[14].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2018, 10, 13, 22, 24, 36, 356, time);
            time = timeAxes[0].getTime(coordinates[15]);
            TestUtil.assertCorrectUTCDate(2018, 10, 13, 22, 27, 21, 312, time);
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

            assertEquals(1542147896326L, timeLocator.getTimeFor(15, 0));
            assertEquals(1542147896626L, timeLocator.getTimeFor(16, 100));
            assertEquals(1542148072417L, timeLocator.getTimeFor(1189, 1000));
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

            NCTestUtils.assertValueAt(1542147988, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1542147988, 0, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1542147988, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1542147988, 1, 2, acquisitionTime);
            NCTestUtils.assertValueAt(1542147988, 2, 2, acquisitionTime);
            NCTestUtils.assertValueAt(1542147988, 2, 0, acquisitionTime);
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
            NCTestUtils.assertValueAt(1542147894, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1542147894, 1, 2, acquisitionTime);
            NCTestUtils.assertValueAt(1542147894, 2, 2, acquisitionTime);
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

            NCTestUtils.assertValueAt(1542148074, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1542148074, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1542148074, 1, 2, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 2, 3, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 3, 4, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_S3A_1km_nadir() throws IOException, InvalidRangeException {
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
    public void testReadScaled_S3A_1km_nadir_bottom() throws IOException, InvalidRangeException {
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
    public void testReadRaw_S3A_1km_nadir() throws IOException, InvalidRangeException {
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
    public void testReadRaw_S3A_1km_nadir_left() throws IOException, InvalidRangeException {
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
    public void testReadScaled_S3A_500m_nadir() throws IOException, InvalidRangeException {
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
    public void testReadScaled_S3A_500m_nadir_upper() throws IOException, InvalidRangeException {
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
    public void testReadRaw_S3A_500m_nadir() throws IOException, InvalidRangeException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(340, 813, interval, "S2_radiance_an");
            NCTestUtils.assertValueAt(659, 0, 0, array);
            NCTestUtils.assertValueAt(666, 1, 0, array);
            NCTestUtils.assertValueAt(672, 2, 0, array);

            array = reader.readScaled(663, 618, interval, "S3_radiance_an");
            NCTestUtils.assertValueAt(9, 3, 0, array);
            NCTestUtils.assertValueAt(8, 4, 0, array);
            NCTestUtils.assertValueAt(10, 0, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_S3A_500m_nadir_bottom() throws IOException, InvalidRangeException {
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
    public void testReadRaw_S3A_500m_nadir_flags() throws IOException, InvalidRangeException {
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
    public void testReadRaw_S3A_500m_nadir_flags_top_left() throws IOException, InvalidRangeException {
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
    public void testReadScaled_S3A_1km_oblique() throws IOException, InvalidRangeException {
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
    public void testReadScaled_S3A_1km_oblique_top() throws IOException, InvalidRangeException {
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
    public void testReadScaled_S3A_1km_oblique_rasterLeft() throws IOException, InvalidRangeException {
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
    public void testReadRaw_S3A_1km_oblique() throws IOException, InvalidRangeException {
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
    public void testReadRaw_S3A_1km_oblique_bottom() throws IOException, InvalidRangeException {
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

    private File getS3AFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"slstr-s3a", "1.0", "2018", "10", "13", "S3A_SL_1_RBT____20181013T222436_20181013T222736_20181015T035102_0179_037_001_1620_LN2_O_NT_003.SEN3", "xfdumanifest.xml"}, false);
        return getFileAsserted(testFilePath);
    }

    // @todo 3 tb/tb move this to a common location and refactor 2019-05-11
    private File getFileAsserted(String testFilePath) {
        final File file = new File(dataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}

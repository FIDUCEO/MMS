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
import org.junit.Ignore;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    public void testReadAcquisitionTime_S3A() throws IOException, InvalidRangeException {
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
    public void testReadAcquisitionTime_S3A_upper_right() throws IOException, InvalidRangeException {
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
    public void testReadAcquisitionTime_S3A_bottom() throws IOException, InvalidRangeException {
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
    @Ignore
    public void testReadScaled_S3A_1km() throws IOException, InvalidRangeException {
        final File file = getS3AFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readScaled(14, 232, interval, "cloud_in");
            NCTestUtils.assertValueAt(287.3299865722656, 0, 0, array);
            NCTestUtils.assertValueAt(287.2799987792969, 1, 0, array);

//            array = reader.readScaled(0, 233, interval, "reflec_nadir_1600");
//            NCTestUtils.assertValueAt(-0.019999999552965164, 0, 1, array);
//            NCTestUtils.assertValueAt(-0.019999999552965164, 1, 1, array);
//            NCTestUtils.assertValueAt(0.0, 2, 1, array);
//
//            array = reader.readScaled(511, 235, interval, "reflec_nadir_0550");
//            NCTestUtils.assertValueAt(0.0, 2, 2, array);
//            NCTestUtils.assertValueAt(-0.019999999552965164, 3, 2, array);
//            NCTestUtils.assertValueAt(-0.019999999552965164, 4, 2, array);
//
//            array = reader.readScaled(145, 0, interval, "btemp_fward_1200");
//            NCTestUtils.assertValueAt(-0.019999999552965164, 3, 0, array);
//            NCTestUtils.assertValueAt(-0.019999999552965164, 3, 1, array);
//            NCTestUtils.assertValueAt(226.5500030517578, 3, 2, array);
//            NCTestUtils.assertValueAt(223.33999633789062, 3, 3, array);
//
//            array = reader.readScaled(155, 40255, interval, "reflec_fward_1600");
//            NCTestUtils.assertValueAt(0.0, 2, 0, array);
//            NCTestUtils.assertValueAt(0.0, 2, 1, array);
//            NCTestUtils.assertValueAt(0.0, 2, 2, array);
//            NCTestUtils.assertValueAt(-0.019999999552965164, 2, 3, array);
//
//            array = reader.readScaled(157, 4023, interval, "reflec_fward_0550");
//            NCTestUtils.assertValueAt(0.0, 2, 0, array);
//            NCTestUtils.assertValueAt(0.0, 3, 0, array);
//
//            array = reader.readScaled(196, 4153, interval, "cloud_flags_fward");
//            NCTestUtils.assertValueAt(0, 4, 0, array);
//            NCTestUtils.assertValueAt(0, 0, 1, array);
//            NCTestUtils.assertValueAt(4098, 1, 1, array);
//
//            array = reader.readScaled(198, 4155, interval, "lat_corr_nadir");
//            NCTestUtils.assertValueAt(0.0, 1, 1, array);
//            NCTestUtils.assertValueAt(0.0, 2, 1, array);
//
//            array = reader.readScaled(200, 4157, interval, "view_elev_nadir");
//            NCTestUtils.assertValueAt(85.38400268554688, 1, 1, array);
//            NCTestUtils.assertValueAt(85.46600341796875, 2, 1, array);
//
//            array = reader.readScaled(202, 4159, interval, "latitude");
//            NCTestUtils.assertValueAt(36.974647521972656, 3, 1, array);
//            NCTestUtils.assertValueAt(36.972557067871094, 4, 1, array);
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

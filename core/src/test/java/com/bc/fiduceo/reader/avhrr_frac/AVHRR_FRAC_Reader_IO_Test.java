package com.bc.fiduceo.reader.avhrr_frac;

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
import com.bc.fiduceo.util.TempFileUtils;
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
public class AVHRR_FRAC_Reader_IO_Test {

    private AVHRR_FRAC_Reader reader;

    @Before
    public void setUp() {
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));
        readerContext.setTempFileUtils(new TempFileUtils());

        reader = new AVHRR_FRAC_Reader(readerContext);
    }

    @Test
    public void testReadAcquisitionInfo_MA() throws IOException {
        final File file = getAvhrrFRAC_MA_File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2018, 5, 11, 14, 4, 15, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2018, 5, 11, 15, 44, 35, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            assertEquals(2, polygons.size());

            Point[] coordinates = polygons.get(0).getCoordinates();
            assertEquals(67, coordinates.length);
            assertEquals(-112.33999633789062, coordinates[0].getLon(), 1e-8);
            assertEquals(84.6032943725586, coordinates[0].getLat(), 1e-8);

            assertEquals(170.42459106445312, coordinates[28].getLon(), 1e-8);
            assertEquals(-81.03530120849611, coordinates[28].getLat(), 1e-8);

            coordinates = polygons.get(1).getCoordinates();
            assertEquals(67, coordinates.length);
            assertEquals(179.9835968017578, coordinates[0].getLon(), 1e-8);
            assertEquals(-68.8104019165039, coordinates[0].getLat(), 1e-8);

            assertEquals(-8.743000030517578, coordinates[29].getLon(), 1e-8);
            assertEquals(79.99319458007814, coordinates[29].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);

            coordinates = polygons.get(0).getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2018, 5, 11, 14, 4, 16, time);

            coordinates = polygons.get(1).getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2018, 5, 11, 14, 54, 25, time);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_MB() throws IOException {
        final File file = getAvhrrFRAC_MB_File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2019, 9, 11, 2, 20, 46, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2019, 9, 11, 3, 19, 28, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);
            final Polygon polygon = (Polygon) boundingGeometry;

            final Point[] coordinates = polygon.getCoordinates();
            assertEquals(75, coordinates.length);
            assertEquals(59.658199310302734, coordinates[0].getLon(), 1e-8);
            assertEquals(-61.54189682006836, coordinates[0].getLat(), 1e-8);

            assertEquals(99.13909912109376, coordinates[28].getLon(), 1e-8);
            assertEquals(80.86370086669922, coordinates[28].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);

            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2019, 9, 11, 2, 20, 46, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_MA_GZ() throws IOException {
        final File file = getAvhrrFRAC_MA_GZ_File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2014, 11, 9, 17, 46, 38, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2014, 11, 9, 18, 37, 8, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);
            final Polygon polygon = (Polygon) boundingGeometry;

            Point[] coordinates = polygon.getCoordinates();
            assertEquals(67, coordinates.length);
            assertEquals(87.18699645996094, coordinates[0].getLon(), 1e-8);
            assertEquals(83.14579772949219, coordinates[0].getLat(), 1e-8);

            assertEquals(172.0540008544922, coordinates[28].getLon(), 1e-8);
            assertEquals(-78.92060089111328, coordinates[28].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);

            coordinates = polygon.getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2014, 11, 9, 17, 46, 40, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_MC() throws IOException {
        final File file = getAvhrrFRAC_MC_File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2019, 9, 18, 17, 8, 2, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2019, 9, 18, 18, 49, 39, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;

            final List<Polygon> polygons = multiPolygon.getPolygons();
            assertEquals(2, polygons.size());

            Polygon polygon = polygons.get(0);
            Point[] coordinates = polygon.getCoordinates();
            assertEquals(67, coordinates.length);
            assertEquals(-32.34819793701172, coordinates[0].getLon(), 1e-8);
            assertEquals(68.38959503173828, coordinates[0].getLat(), 1e-8);

            assertEquals(118.37899780273439, coordinates[35].getLon(), 1e-8);
            assertEquals(-66.94749450683594, coordinates[35].getLat(), 1e-8);

            assertEquals(9.459699630737305, coordinates[58].getLon(), 1e-8);
            assertEquals(82.74839782714844, coordinates[58].getLat(), 1e-8);

            polygon = polygons.get(1);
            coordinates = polygon.getCoordinates();
            assertEquals(67, coordinates.length);
            assertEquals(-96.81669616699219, coordinates[0].getLon(), 1e-8);
            assertEquals(-82.56119537353516, coordinates[0].getLat(), 1e-8);

            assertEquals(93.87649536132814, coordinates[33].getLon(), 1e-8);
            assertEquals(82.41559600830078, coordinates[33].getLat(), 1e-8);

            assertEquals(-179.3227996826172, coordinates[61].getLon(), 1e-8);
            assertEquals(-79.1001968383789, coordinates[61].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);

            polygon = polygons.get(0);
            Date time = timeAxes[0].getTime(polygon.getCoordinates()[0]);
            TestUtil.assertCorrectUTCDate(2019, 9, 18, 17, 8, 2, time);

            polygon = polygons.get(1);
            time = timeAxes[1].getTime(polygon.getCoordinates()[0]);
            TestUtil.assertCorrectUTCDate(2019, 9, 18, 17, 58, 53, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_MA() throws IOException {
        final File file = getAvhrrFRAC_MA_File();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(1526047455000L, timeLocator.getTimeFor(169, 0));
            assertEquals(1526047455167L, timeLocator.getTimeFor(168, 1));
            assertEquals(1526047457333L, timeLocator.getTimeFor(169, 14));
            assertEquals(1526047624167L, timeLocator.getTimeFor(170, 1015));
            assertEquals(1526047791000L, timeLocator.getTimeFor(171, 2016));
            assertEquals(1526053475000L, timeLocator.getTimeFor(172, 36120));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_MB() throws IOException {
        final File file = getAvhrrFRAC_MB_File();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(1568168446000L, timeLocator.getTimeFor(170, 0));
            assertEquals(1568168446167L, timeLocator.getTimeFor(169, 1));
            assertEquals(1568168448501L, timeLocator.getTimeFor(170, 15));
            assertEquals(1568168615430L, timeLocator.getTimeFor(171, 1016));
            assertEquals(1568168782358L, timeLocator.getTimeFor(172, 2017));
            assertEquals(1568171968000L, timeLocator.getTimeFor(173, 21120));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_MC() throws IOException {
        final File file = getAvhrrFRAC_MC_File();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(1568826482000L, timeLocator.getTimeFor(170, 0));
            assertEquals(1568826482167L, timeLocator.getTimeFor(169, 1));
            assertEquals(1568826484668L, timeLocator.getTimeFor(170, 16));
            assertEquals(1568826651602L, timeLocator.getTimeFor(171, 1017));
            assertEquals(1568826818536L, timeLocator.getTimeFor(172, 2018));
            assertEquals(1568830004285L, timeLocator.getTimeFor(173, 21121));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_MA() throws IOException {
        final File file = getAvhrrFRAC_MA_File();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(19, variables.size());

            Variable variable = variables.get(0);
            assertEquals("radiance_1", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());

            variable = variables.get(4);
            assertEquals("radiance_4", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());

            variable = variables.get(12);
            assertEquals("flags", variable.getShortName());
            assertEquals(DataType.BYTE, variable.getDataType());

            variable = variables.get(18);
            assertEquals("longitude", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_MB() throws IOException {
        final File file = getAvhrrFRAC_MB_File();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(19, variables.size());

            Variable variable = variables.get(1);
            assertEquals("radiance_2", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());

            variable = variables.get(5);
            assertEquals("radiance_5", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());

            variable = variables.get(9);
            assertEquals("temp_3b", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());

            variable = variables.get(17);
            assertEquals("latitude", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_MC() throws IOException {
        final File file = getAvhrrFRAC_MC_File();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(19, variables.size());

            Variable variable = variables.get(2);
            assertEquals("radiance_3a", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());

            variable = variables.get(6);
            assertEquals("reflec_1", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());

            variable = variables.get(10);
            assertEquals("temp_4", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());

            variable = variables.get(18);
            assertEquals("longitude", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_MA() throws IOException {
        final File file = getAvhrrFRAC_MA_File();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertEquals(2001, productSize.getNx());
            assertEquals(36121, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_MB() throws IOException {
        final File file = getAvhrrFRAC_MB_File();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertEquals(2001, productSize.getNx());
            assertEquals(21121, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_MC() throws IOException {
        final File file = getAvhrrFRAC_MC_File();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertEquals(2001, productSize.getNx());
            assertEquals(36561, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_MA() throws IOException {
        final File file = getAvhrrFRAC_MA_File();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(108, 2540, interval);
            assertNotNull(acquisitionTime);

            NCTestUtils.assertValueAt(1526047878, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1526047878, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1526047878, 2, 2, acquisitionTime);
            NCTestUtils.assertValueAt(1526047879, 1, 3, acquisitionTime);
            NCTestUtils.assertValueAt(1526047879, 0, 4, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_MB() throws IOException {
        final File file = getAvhrrFRAC_MB_File();

        try {
            reader.open(file);

            final Interval interval = new Interval(3, 5);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(109, 2541, interval);
            assertNotNull(acquisitionTime);

            NCTestUtils.assertValueAt(1568168869, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1568168870, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1568168870, 2, 2, acquisitionTime);
            NCTestUtils.assertValueAt(1568168870, 0, 3, acquisitionTime);
            NCTestUtils.assertValueAt(1568168870, 1, 4, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_MA_borderPixel() throws IOException {
        final File file = getAvhrrFRAC_MA_File();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(2000, 2559, interval);
            assertNotNull(acquisitionTime);

            NCTestUtils.assertValueAt(1526047881, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1526047881, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1526047882, 2, 2, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 3, 3, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 4, 4, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_MB_borderPixel() throws IOException {
        final File file = getAvhrrFRAC_MB_File();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(256, 21120, interval);
            assertNotNull(acquisitionTime);

            NCTestUtils.assertValueAt(1568171968, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1568171968, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1568171968, 2, 2, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 3, 3, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 4, 4, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_MA() throws IOException {
        final File file = getAvhrrFRAC_MA_File();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readScaled(15, 235, interval, "radiance_1");
            NCTestUtils.assertValueAt(122.01721954345703, 0, 0, array);
            NCTestUtils.assertValueAt(122.30326843261719, 1, 0, array);

            array = reader.readScaled(16, 236, interval, "radiance_3a");
            NCTestUtils.assertValueAt(15.061016082763672, 0, 1, array);
            NCTestUtils.assertValueAt(15.061016082763672, 1, 1, array);
            NCTestUtils.assertValueAt(15.061016082763672, 2, 1, array);

            array = reader.readScaled(17, 237, interval, "radiance_4");
            NCTestUtils.assertValueAt(47.82387924194336, 2, 2, array);
            NCTestUtils.assertValueAt(47.628501892089844, 3, 2, array);
            NCTestUtils.assertValueAt(47.82387924194336, 4, 2, array);

            array = reader.readScaled(18, 238, interval, "reflec_1");
            NCTestUtils.assertValueAt(25.765380859375, 3, 0, array);
            NCTestUtils.assertValueAt(25.765380859375, 3, 1, array);
            NCTestUtils.assertValueAt(25.765380859375, 3, 2, array);
            NCTestUtils.assertValueAt(25.709070205688477, 3, 3, array);

            array = reader.readScaled(19, 239, interval, "reflec_3a");
            NCTestUtils.assertValueAt(21.297500610351562, 2, 0, array);
            NCTestUtils.assertValueAt(21.059200286865234, 2, 1, array);
            NCTestUtils.assertValueAt(20.820899963378906, 2, 2, array);
            NCTestUtils.assertValueAt(21.059200286865234, 2, 3, array);

            array = reader.readScaled(20, 240, interval, "temp_4");
            NCTestUtils.assertValueAt(252.68516540527344, 2, 0, array);
            NCTestUtils.assertValueAt(256.4285888671875, 3, 0, array);

            array = reader.readScaled(21, 241, interval, "flags");
            NCTestUtils.assertValueAt(4, 4, 0, array);
            NCTestUtils.assertValueAt(4, 0, 1, array);
            NCTestUtils.assertValueAt(4, 1, 1, array);

            array = reader.readScaled(22, 242, interval, "sun_zenith");
            NCTestUtils.assertValueAt(69.0618667602539, 1, 1, array);
            NCTestUtils.assertValueAt(69.04086303710938, 2, 1, array);

            array = reader.readScaled(23, 243, interval, "delta_azimuth");
            NCTestUtils.assertValueAt(-47.687774658203125, 1, 1, array);
            NCTestUtils.assertValueAt(-47.69678878784185, 2, 1, array);

            array = reader.readScaled(24, 244, interval, "longitude");
            NCTestUtils.assertValueAt(-94.69221496582031, 3, 1, array);
            NCTestUtils.assertValueAt(-94.45693206787114, 4, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_MB() throws IOException {
        final File file = getAvhrrFRAC_MB_File();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readScaled(16, 236, interval, "radiance_2");
            NCTestUtils.assertValueAt(0.3400716185569763, 0, 0, array);
            NCTestUtils.assertValueAt(0.3400716185569763, 1, 0, array);

            array = reader.readScaled(17, 237, interval, "radiance_3b");
            NCTestUtils.assertValueAt(0.0, 2, 1, array);
            NCTestUtils.assertValueAt(0.0, 3, 1, array);
            NCTestUtils.assertValueAt(0.0, 4, 1, array);

            array = reader.readScaled(18, 238, interval, "radiance_5");
            NCTestUtils.assertValueAt(51.58249282836914, 0, 2, array);
            NCTestUtils.assertValueAt(51.769927978515625, 1, 2, array);
            NCTestUtils.assertValueAt(52.3323860168457, 2, 2, array);

            array = reader.readScaled(19, 239, interval, "reflec_2");
            NCTestUtils.assertValueAt(0.1666399985551834, 3, 2, array);
            NCTestUtils.assertValueAt(0.10316000133752823, 4, 2, array);
            NCTestUtils.assertValueAt(0.1666399985551834, 0, 3, array);
            NCTestUtils.assertValueAt(0.10316000133752823, 1, 3, array);

            array = reader.readScaled(20, 240, interval, "temp_3b");
            NCTestUtils.assertValueAt(0.0, 2, 3, array);
            NCTestUtils.assertValueAt(0.0, 3, 3, array);
            NCTestUtils.assertValueAt(0.0, 4, 3, array);
            NCTestUtils.assertValueAt(0.0, 0, 4, array);

            array = reader.readScaled(21, 241, interval, "temp_5");
            NCTestUtils.assertValueAt(247.535888671875, 1, 4, array);
            NCTestUtils.assertValueAt(248.7532196044922, 2, 4, array);
            NCTestUtils.assertValueAt(252.4775848388672, 3, 4, array);

            array = reader.readScaled(22, 242, interval, "flags");
            NCTestUtils.assertValueAt(4, 4, 4, array);
            NCTestUtils.assertValueAt(4, 0, 0, array);
            NCTestUtils.assertValueAt(4, 1, 0, array);

            array = reader.readScaled(23, 243, interval, "sun_zenith");
            NCTestUtils.assertValueAt(92.952392578125, 2, 0, array);
            NCTestUtils.assertValueAt(92.93389892578125, 3, 0, array);
            NCTestUtils.assertValueAt(92.9154052734375, 4, 0, array);

            array = reader.readScaled(24, 244, interval, "delta_azimuth");
            NCTestUtils.assertValueAt(55.599998474121094, 0, 1, array);
            NCTestUtils.assertValueAt(55.5984992980957, 1, 1, array);
            NCTestUtils.assertValueAt(55.59700012207031, 2, 1, array);

            array = reader.readScaled(25, 245, interval, "latitude");
            NCTestUtils.assertValueAt(-63.75167465209961, 3, 1, array);
            NCTestUtils.assertValueAt(-63.77696228027344, 4, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_MA() throws IOException {
        final File file = getAvhrrFRAC_MA_File();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(26, 334, interval, "radiance_2");
            NCTestUtils.assertValueAt(85.00337219238281, 3, 0, array);
            NCTestUtils.assertValueAt(86.53598022460938, 4, 0, array);

            array = reader.readRaw(1, 336, interval, "radiance_3b");
            NCTestUtils.assertValueAt(0.0, 0, 1, array);
            NCTestUtils.assertValueAt(0.0, 1, 1, array);
            NCTestUtils.assertValueAt(0.0, 2, 1, array);

            array = reader.readRaw(512, 338, interval, "radiance_5");
            NCTestUtils.assertValueAt(61.82318878173828, 2, 1, array);
            NCTestUtils.assertValueAt(61.61263656616211, 3, 1, array);
            NCTestUtils.assertValueAt(61.82318878173828, 4, 1, array);

            array = reader.readRaw(218, 0, interval, "reflec_2");
            NCTestUtils.assertValueAt(0.0, 2, 0, array);
            NCTestUtils.assertValueAt(0.0, 2, 1, array);
            NCTestUtils.assertValueAt(27.149749755859375, 2, 2, array);
            NCTestUtils.assertValueAt(27.219499588012695, 2, 3, array);

            array = reader.readRaw(212, 33215, interval, "temp_3b");
            NCTestUtils.assertValueAt(271.8573913574219, 2, 0, array);
            NCTestUtils.assertValueAt(272.1737976074219, 2, 1, array);
            NCTestUtils.assertValueAt(271.8985290527344, 2, 2, array);
            NCTestUtils.assertValueAt(272.5256042480469, 2, 3, array);

            array = reader.readRaw(244, 21770, interval, "temp_5");
            NCTestUtils.assertValueAt(264.5557861328125, 0, 2, array);
            NCTestUtils.assertValueAt(260.2013244628906, 1, 2, array);

            array = reader.readRaw(246, 21772, interval, "cloudFlag");
            NCTestUtils.assertValueAt(3, 2, 2, array);
            NCTestUtils.assertValueAt(3, 3, 2, array);

            array = reader.readRaw(248, 21774, interval, "view_zenith");
            NCTestUtils.assertValueAt(47.40487289428711, 4, 2, array);
            NCTestUtils.assertValueAt(47.677310943603516, 0, 3, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_MB() throws IOException {
        final File file = getAvhrrFRAC_MB_File();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(25, 333, interval, "radiance_2");
            NCTestUtils.assertValueAt(0.13080692291259766, 3, 0, array);
            NCTestUtils.assertValueAt(0.13080692291259766, 4, 0, array);

            array = reader.readRaw(0, 335, interval, "radiance_3b");
            NCTestUtils.assertValueAt(0.0, 0, 1, array);
            NCTestUtils.assertValueAt(0.0, 1, 1, array);
            NCTestUtils.assertValueAt(0.0, 2, 1, array);

            array = reader.readRaw(511, 337, interval, "radiance_5");
            NCTestUtils.assertValueAt(39.852996826171875, 2, 1, array);
            NCTestUtils.assertValueAt(39.481597900390625, 3, 1, array);
            NCTestUtils.assertValueAt(38.36802291870117, 4, 1, array);

            array = reader.readRaw(217, 0, interval, "reflec_2");
            NCTestUtils.assertValueAt(0.0, 2, 0, array);
            NCTestUtils.assertValueAt(0.0, 2, 1, array);
            NCTestUtils.assertValueAt(1.5631999969482422, 2, 2, array);
            NCTestUtils.assertValueAt(1.4997199773788452, 2, 3, array);

            array = reader.readRaw(219, 21120, interval, "temp_3b");
            NCTestUtils.assertValueAt(0.0, 2, 0, array);
            NCTestUtils.assertValueAt(0.0, 2, 1, array);
            NCTestUtils.assertValueAt(0.0, 2, 2, array);
            NCTestUtils.assertValueAt(0.0, 2, 3, array);

            array = reader.readRaw(245, 21099, interval, "temp_5");
            NCTestUtils.assertValueAt(251.50001525878906, 0, 2, array);
            NCTestUtils.assertValueAt(251.33255004882812, 1, 2, array);

            array = reader.readRaw(247, 21120, interval, "cloudFlag");
            NCTestUtils.assertValueAt(3, 2, 2, array);
            NCTestUtils.assertValueAt(3, 3, 2, array);

            array = reader.readRaw(248, 21120, interval, "view_zenith");
            NCTestUtils.assertValueAt(47.29999923706055, 4, 2, array);
            NCTestUtils.assertValueAt(9.969209968386869E36, 0, 3, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_corner_pixels_MA() throws IOException {
        final File file = getAvhrrFRAC_MA_File();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(0, 0, interval, "radiance_1");
            NCTestUtils.assertValueAt(0.0, 0, 2, array);
            NCTestUtils.assertValueAt(0.0, 1, 2, array);
            NCTestUtils.assertValueAt(109.43113708496094, 2, 2, array);
            NCTestUtils.assertValueAt(106.28462219238281, 3, 2, array);
            NCTestUtils.assertValueAt(116.01022338867188, 4, 2, array);

            array = reader.readRaw(2000, 0, interval, "radiance_2");
            NCTestUtils.assertValueAt(7.497435569763184, 0, 2, array);
            NCTestUtils.assertValueAt(7.716379165649414, 1, 2, array);
            NCTestUtils.assertValueAt(7.716379165649414, 2, 2, array);
            NCTestUtils.assertValueAt(0.0, 3, 2, array);
            NCTestUtils.assertValueAt(0.0, 4, 2, array);

            array = reader.readRaw(2000, 36120, interval, "radiance_3a");
            NCTestUtils.assertValueAt(16.1448974609375, 0, 2, array);
            NCTestUtils.assertValueAt(12.351310729980469, 1, 2, array);
            NCTestUtils.assertValueAt(10.661288261413574, 2, 2, array);
            NCTestUtils.assertValueAt(0.0, 3, 2, array);
            NCTestUtils.assertValueAt(0.0, 4, 2, array);

            array = reader.readRaw(0, 36120, interval, "radiance_3b");
            NCTestUtils.assertValueAt(0.0, 0, 2, array);
            NCTestUtils.assertValueAt(0.0, 1, 2, array);
            NCTestUtils.assertValueAt(0.0, 2, 2, array);
            NCTestUtils.assertValueAt(0.0, 3, 2, array);
            NCTestUtils.assertValueAt(0.0, 4, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_corner_pixels_MB() throws IOException {
        final File file = getAvhrrFRAC_MB_File();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(0, 0, interval, "radiance_1");
            NCTestUtils.assertValueAt(0.0, 0, 2, array);
            NCTestUtils.assertValueAt(0.0, 1, 2, array);
            NCTestUtils.assertValueAt(2.274825096130371, 2, 2, array);
            NCTestUtils.assertValueAt(2.274825096130371, 3, 2, array);
            NCTestUtils.assertValueAt(2.274825096130371, 4, 2, array);

            array = reader.readRaw(2000, 0, interval, "radiance_2");
            NCTestUtils.assertValueAt(54.95815658569336, 0, 2, array);
            NCTestUtils.assertValueAt(54.95815658569336, 1, 2, array);
            NCTestUtils.assertValueAt(55.37668991088867, 2, 2, array);
            NCTestUtils.assertValueAt(0.0, 3, 2, array);
            NCTestUtils.assertValueAt(0.0, 4, 2, array);

            array = reader.readRaw(2000, 21120, interval, "radiance_3a");
            NCTestUtils.assertValueAt(7.413973808288574, 0, 2, array);
            NCTestUtils.assertValueAt(9.419328689575195, 1, 2, array);
            NCTestUtils.assertValueAt(12.11072826385498, 2, 2, array);
            NCTestUtils.assertValueAt(0.0, 3, 2, array);
            NCTestUtils.assertValueAt(0.0, 4, 2, array);

            array = reader.readRaw(0, 21120, interval, "temp_4");
            NCTestUtils.assertValueAt(0.0, 0, 2, array);
            NCTestUtils.assertValueAt(0.0, 1, 2, array);
            NCTestUtils.assertValueAt(266.4667053222656, 2, 2, array);
            NCTestUtils.assertValueAt(266.4667053222656, 3, 2, array);
            NCTestUtils.assertValueAt(266.316650390625, 4, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator() throws IOException {
        final File file = getAvhrrFRAC_MA_File();

        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            Point2D geoLocation = pixelLocator.getGeoLocation(144.5, 20044.5, null);
            assertEquals(133.2231219776277, geoLocation.getX(), 1e-8);
            assertEquals(-62.11543647766112, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(67.5, 25804.5, null);
            assertEquals(103.31359732380614, geoLocation.getX(), 1e-8);
            assertEquals(-8.9021154808998, geoLocation.getY(), 1e-8);

            Point2D[] pixelLocation = pixelLocator.getPixelLocation(133.2231, -62.1154);
            assertEquals(1, pixelLocation.length);
            assertEquals(143.8646932683579, pixelLocation[0].getX(), 1e-8);
            assertEquals(20044.771857088923, pixelLocation[0].getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(1000.5, 25804.5, null);
            assertEquals(93.33047104606814, geoLocation.getX(), 1e-8);
            assertEquals(-11.28578977584838, geoLocation.getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(93.3305, -11.2858);
            assertEquals(1, pixelLocation.length);
            assertEquals(1000.561571522018, pixelLocation[0].getX(), 1e-8);
            assertEquals(25804.514560896023, pixelLocation[0].getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(1723, -88);
            assertEquals(0, pixelLocation.length);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetSubscenePixelLocator() throws IOException {
        final File file = getAvhrrFRAC_MA_File();

        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getSubScenePixelLocator(null); // geometry is ignored tb 2019-01-17
            assertNotNull(pixelLocator);

            Point2D geoLocation = pixelLocator.getGeoLocation(446.5, 5794.5, null);
            assertEquals(-74.72590899920027, geoLocation.getX(), 1e-8);
            assertEquals(29.254373288154607, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(470.5, 21176.5, null);
            assertEquals(114.77256666894323, geoLocation.getX(), 1e-8);
            assertEquals(-54.64908885955811, geoLocation.getY(), 1e-8);

            Point2D[] pixelLocation = pixelLocator.getPixelLocation(114.7726, -54.6491);
            assertEquals(1, pixelLocation.length);
            assertEquals(470.2046655871767, pixelLocation[0].getX(), 1e-8);
            assertEquals(21176.572688391992, pixelLocation[0].getY(), 1e-8);
        } finally {
            reader.close();
        }
    }

    private File getAvhrrFRAC_MA_File() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-frac-ma", "v1", "2018", "05", "11", "NSS.FRAC.M2.D18131.S1404.E1544.B5998081.SV"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File getAvhrrFRAC_MB_File() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-frac-mb", "v1", "2019", "09", "11", "NSS.FRAC.M1.D19254.S0220.E0319.B3621920.SV"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File getAvhrrFRAC_MA_GZ_File() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-frac-ma", "v1", "2014", "11", "09", "NSS.FRAC.M1.D14313.S1746.E1837.B1112525.MM.gz"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File getAvhrrFRAC_MC_File() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-frac-mc", "v1", "2019", "09", "18", "NSS.FRAC.M3.D19261.S1708.E1849.B0448586.SV"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}

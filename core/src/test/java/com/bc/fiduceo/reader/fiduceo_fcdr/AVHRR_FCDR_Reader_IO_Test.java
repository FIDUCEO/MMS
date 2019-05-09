package com.bc.fiduceo.reader.fiduceo_fcdr;

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
import com.bc.fiduceo.reader.TimeLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class AVHRR_FCDR_Reader_IO_Test {

    private File testDataDirectory;
    private AVHRR_FCDR_Reader reader;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory();

        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new AVHRR_FCDR_Reader(readerContext);
    }

    @After
    public void tearDown() throws IOException {
          reader.close();
    }

    @Test
    public void testReadAcquisitionInfo_NOAA12() throws IOException {
        final File file = createAvhrrNOAA12File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1992, 4, 14, 14, 14, 12, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1992, 4, 14, 15, 55, 32, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            assertEquals(2, polygons.size());

            Point[] coordinates = polygons.get(0).getCoordinates();
            assertEquals(145, coordinates.length);
            assertEquals(-113.10220638290049, coordinates[0].getLon(), 1e-8);
            assertEquals(1.9309060927480464, coordinates[0].getLat(), 1e-8);

            assertEquals(-159.40000595524907, coordinates[25].getLon(), 1e-8);
            assertEquals(-62.739341352134936, coordinates[25].getLat(), 1e-8);

            coordinates = polygons.get(1).getCoordinates();
            assertEquals(145, coordinates.length);
            assertEquals(79.80712294578552, coordinates[0].getLon(), 1e-8);
            assertEquals(1.593066193163395, coordinates[0].getLat(), 1e-8);

            assertEquals(81.58696241676807, coordinates[26].getLon(), 1e-8);
            assertEquals(76.53035058639944, coordinates[26].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = polygons.get(0).getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1992, 4, 14, 14, 14, 26, time);

            coordinates = polygons.get(1).getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1992, 4, 14, 15, 4, 52, time);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_NOAA19() throws IOException {
        final File file = createAvhrrNOAA19File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2011, 7, 5, 5, 57, 21, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2011, 7, 5, 7, 39, 27, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            assertEquals(2, polygons.size());

            Point[] coordinates = polygons.get(0).getCoordinates();
            assertEquals(147, coordinates.length);
            assertEquals(127.1431622095406, coordinates[0].getLon(), 1e-8);
            assertEquals(2.0050660707056522, coordinates[0].getLat(), 1e-8);

            assertEquals(127.33542881906034, coordinates[25].getLon(), 1e-8);
            assertEquals(73.09701827354729, coordinates[25].getLat(), 1e-8);

            coordinates = polygons.get(1).getCoordinates();
            assertEquals(147, coordinates.length);
            assertEquals(-92.25501257926226, coordinates[0].getLon(), 1e-8);
            assertEquals(2.1698660217225556, coordinates[0].getLat(), 1e-8);

            assertEquals(-143.13425078988075, coordinates[26].getLon(), 1e-8);
            assertEquals(-63.69792773388326, coordinates[26].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = polygons.get(0).getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2011, 7, 5, 5, 57, 21, time);

            coordinates = polygons.get(1).getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2011, 7, 5, 6, 48, 38, time);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_METOPA() throws IOException {
        final File file = createAvhrrMetopAFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2016, 11, 8, 7, 37, 29, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2016, 11, 8, 8, 28, 17, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);
            final Polygon boundingPolygon = (Polygon) boundingGeometry;

            Point[] coordinates = boundingPolygon.getCoordinates();
            assertEquals(145, coordinates.length);
            assertEquals(-95.06759840995073, coordinates[0].getLon(), 1e-8);
            assertEquals(-63.95336765795946, coordinates[0].getLat(), 1e-8);

            assertEquals(-145.73259668424726, coordinates[27].getLon(), 1e-8);
            assertEquals(6.561784716323017, coordinates[27].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            coordinates = boundingPolygon.getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2016, 11, 8, 7, 37, 29, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocatorNOAA12() throws IOException {
        final File file = createAvhrrNOAA12File();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(703260852587L, timeLocator.getTimeFor(169, 0));
            assertEquals(703260853092L, timeLocator.getTimeFor(168, 1));
            assertEquals(703260859587L, timeLocator.getTimeFor(169, 14));
            assertEquals(703261360090L, timeLocator.getTimeFor(170, 1015));
            assertEquals(703261860586L, timeLocator.getTimeFor(171, 2016));
            assertEquals(703265975587L, timeLocator.getTimeFor(172, 10246));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocatorNOAA19() throws IOException {
        final File file = createAvhrrNOAA19File();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(1309845441668L, timeLocator.getTimeFor(169, 0));
            assertEquals(1309845442171L, timeLocator.getTimeFor(168, 1));
            assertEquals(1309845448668L, timeLocator.getTimeFor(169, 14));
            assertEquals(1309845949171L, timeLocator.getTimeFor(170, 1015));
            assertEquals(1309846449669L, timeLocator.getTimeFor(171, 2016));
            assertEquals(1309850564670L, timeLocator.getTimeFor(172, 10246));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocatorMETOPA() throws IOException {
        final File file = createAvhrrMetopAFile();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(1478590649018L, timeLocator.getTimeFor(117, 0));
            assertEquals(1478590649518L, timeLocator.getTimeFor(118, 1));
            assertEquals(1478590650017L, timeLocator.getTimeFor(119, 2));
            assertEquals(1478590701516L, timeLocator.getTimeFor(120, 105));
            assertEquals(1478590702516L, timeLocator.getTimeFor(121, 107));
            assertEquals(1478593266521L, timeLocator.getTimeFor(122, 5235));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime() throws IOException, InvalidRangeException {
        final File file = createAvhrrNOAA12File();

        try {
            reader.open(file);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(38, 217, new Interval(3, 3));
            NCTestUtils.assertValueAt(703260961, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(703260961, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(703260962, 2, 2, acquisitionTime);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_borderPixel() throws IOException, InvalidRangeException {
        final File file = createAvhrrMetopAFile();

        try {
            reader.open(file);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(408, 1298, new Interval(3, 3));
            NCTestUtils.assertValueAt(1478591298, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1478591298, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 2, 2, acquisitionTime);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariablesNOAA12() throws IOException {
        final File file = createAvhrrNOAA12File();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(35, variables.size());
            Variable variable = variables.get(1);
            assertEquals("longitude", variable.getFullName());

            variable = variables.get(9);
            assertEquals("Ch3a", variable.getFullName());

            variable = variables.get(16);
            assertEquals("scanline_origl1b", variable.getFullName());

            variable = variables.get(25);
            assertEquals("u_common_Ch3a", variable.getFullName());

            variable = variables.get(34);
            assertEquals("u_common_Ch5", variable.getFullName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariablesNOAA19() throws IOException {
        final File file = createAvhrrNOAA19File();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(35, variables.size());
            Variable variable = variables.get(2);
            assertEquals("quality_pixel_bitmask", variable.getFullName());

            variable = variables.get(10);
            assertEquals("Ch3b", variable.getFullName());

            variable = variables.get(17);
            assertEquals("u_independent_Ch1", variable.getFullName());

            variable = variables.get(26);
            assertEquals("u_independent_Ch3b", variable.getFullName());

            variable = variables.get(34);
            assertEquals("u_common_Ch5", variable.getFullName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_windowCenter() throws Exception {
        final File file = createAvhrrMetopAFile();
        reader.open(file);
        try {
            final Array array = reader.readRaw(29, 6067, new Interval(3, 3), "Ch3a");
            assertNotNull(array);

            NCTestUtils.assertValueAt(12613, 0, 0, array);
            NCTestUtils.assertValueAt(12685, 1, 0, array);
            NCTestUtils.assertValueAt(12661, 2, 0, array);
            NCTestUtils.assertValueAt(12781, 0, 1, array);
            NCTestUtils.assertValueAt(12542, 1, 1, array);
            NCTestUtils.assertValueAt(12398, 2, 1, array);
            NCTestUtils.assertValueAt(12637, 0, 2, array);
            NCTestUtils.assertValueAt(12422, 1, 2, array);
            NCTestUtils.assertValueAt(12446, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomWindowOut() throws Exception {
        final File file = createAvhrrNOAA12File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(78, 12159, new Interval(3, 3), "Time");
            assertNotNull(array);

            NCTestUtils.assertValueAt(7.0326693159027E8, 0, 0, array);
            NCTestUtils.assertValueAt(7.0326693159027E8, 1, 0, array);
            NCTestUtils.assertValueAt(7.0326693159027E8, 2, 0, array);
            NCTestUtils.assertValueAt(7.03266932091522E8, 0, 1, array);
            NCTestUtils.assertValueAt(7.03266932091522E8, 1, 1, array);
            NCTestUtils.assertValueAt(7.03266932091522E8, 2, 1, array);
            NCTestUtils.assertValueAt(Double.NaN, 0, 2, array);
            NCTestUtils.assertValueAt(Double.NaN, 1, 2, array);
            NCTestUtils.assertValueAt(Double.NaN, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomWindowOutNOAA19() throws Exception {
        final File file = createAvhrrNOAA19File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(79, 12252, new Interval(3, 3), "data_quality_bitmask");
            assertNotNull(array);

            NCTestUtils.assertValueAt(0, 0, 0, array);
            NCTestUtils.assertValueAt(0, 1, 0, array);
            NCTestUtils.assertValueAt(0, 2, 0, array);
            NCTestUtils.assertValueAt(0, 0, 1, array);
            NCTestUtils.assertValueAt(0, 1, 1, array);
            NCTestUtils.assertValueAt(0, 2, 1, array);
            NCTestUtils.assertValueAt(-1, 0, 2, array);
            NCTestUtils.assertValueAt(-1, 1, 2, array);
            NCTestUtils.assertValueAt(-1, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topWindowOut() throws Exception {
        final File file = createAvhrrMetopAFile();
        reader.open(file);

        try {
            final Array array = reader.readRaw(5, 1, new Interval(3, 4), "latitude");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-32768, 0, 0, array);
            NCTestUtils.assertValueAt(-32768, 1, 0, array);
            NCTestUtils.assertValueAt(-32768, 2, 0, array);

            NCTestUtils.assertValueAt(-23515, 0, 1, array);
            NCTestUtils.assertValueAt(-23569, 1, 1, array);
            NCTestUtils.assertValueAt(-23621, 2, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_leftWindowOut() throws Exception {
        final File file = createAvhrrNOAA12File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(0, 122, new Interval(5, 5), "relative_azimuth_angle");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-32767, 0, 0, array);
            NCTestUtils.assertValueAt(-32767, 1, 0, array);
            NCTestUtils.assertValueAt(1928, 2, 0, array);
            NCTestUtils.assertValueAt(1930, 3, 0, array);
            NCTestUtils.assertValueAt(1931, 4, 0, array);

            NCTestUtils.assertValueAt(-32767, 0, 4, array);
            NCTestUtils.assertValueAt(-32767, 1, 4, array);
            NCTestUtils.assertValueAt(1939, 2, 4, array);
            NCTestUtils.assertValueAt(1939, 3, 4, array);
            NCTestUtils.assertValueAt(1940, 4, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_leftWindowOutNOAA19() throws Exception {
        final File file = createAvhrrNOAA19File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(0, 123, new Interval(5, 5), "satellite_zenith_angle");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-32767, 0, 0, array);
            NCTestUtils.assertValueAt(-32767, 1, 0, array);
            NCTestUtils.assertValueAt(6859, 2, 0, array);
            NCTestUtils.assertValueAt(6813, 3, 0, array);
            NCTestUtils.assertValueAt(6766, 4, 0, array);

            NCTestUtils.assertValueAt(-32767, 0, 4, array);
            NCTestUtils.assertValueAt(-32767, 1, 4, array);
            NCTestUtils.assertValueAt(6859, 2, 4, array);
            NCTestUtils.assertValueAt(6813, 3, 4, array);
            NCTestUtils.assertValueAt(6766, 4, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topLeftWindowOut() throws Exception {
        final File file = createAvhrrMetopAFile();
        reader.open(file);

        try {
            final Array array = reader.readRaw(2, 3, new Interval(9, 9), "satellite_zenith_angle");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-32767, 0, 0, array);
            NCTestUtils.assertValueAt(-32767, 4, 0, array);
            NCTestUtils.assertValueAt(-32767, 8, 0, array);

            NCTestUtils.assertValueAt(-32767, 0, 4, array);
            NCTestUtils.assertValueAt(6742, 4, 4, array);
            NCTestUtils.assertValueAt(6563, 8, 4, array);

            NCTestUtils.assertValueAt(-32767, 0, 8, array);
            NCTestUtils.assertValueAt(6742, 4, 8, array);
            NCTestUtils.assertValueAt(6563, 8, 8, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomRightWindowOut() throws Exception {
        final File file = createAvhrrNOAA12File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(406, 12159, new Interval(9, 9), "solar_zenith_angle");
            assertNotNull(array);

            NCTestUtils.assertValueAt(5559, 3, 3, array);
            NCTestUtils.assertValueAt(5544, 4, 4, array);
            NCTestUtils.assertValueAt(-32767, 6, 6, array);
            NCTestUtils.assertValueAt(-32767, 8, 8, array);

            NCTestUtils.assertValueAt(5574, 2, 2, array);
            NCTestUtils.assertValueAt(5542, 4, 2, array);
            NCTestUtils.assertValueAt(-32767, 6, 8, array);
            NCTestUtils.assertValueAt(-32767, 8, 8, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomRightWindowOutNOAA19() throws Exception {
        final File file = createAvhrrNOAA19File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(408, 12252, new Interval(3, 3), "u_common_Ch1");
            assertNotNull(array);

            NCTestUtils.assertValueAt(3000, 0, 0, array);
            NCTestUtils.assertValueAt(-32767, 0, 1, array);
            NCTestUtils.assertValueAt(-32767, 0, 2, array);

            NCTestUtils.assertValueAt(3000, 0, 0, array);
            NCTestUtils.assertValueAt(3000, 1, 0, array);
            NCTestUtils.assertValueAt(-32767, 2, 0, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_scalingAndOffset() throws IOException, InvalidRangeException {
        final File file = createAvhrrMetopAFile();
        reader.open(file);

        try {
            final Array array = reader.readScaled(101, 2019, new Interval(3, 3), "Ch3b");
            assertNotNull(array);

            NCTestUtils.assertValueAt(292.53999999999996, 0, 0, array);
            NCTestUtils.assertValueAt(280.23999999999995, 1, 0, array);
            NCTestUtils.assertValueAt(281.73999999999995, 2, 0, array);

            NCTestUtils.assertValueAt(293.59999999999997, 0, 1, array);
            NCTestUtils.assertValueAt(286.10999999999996, 1, 1, array);
            NCTestUtils.assertValueAt(287.78999999999996, 2, 1, array);

            NCTestUtils.assertValueAt(293.989999999999952, 0, 2, array);
            NCTestUtils.assertValueAt(290.41999999999996, 1, 2, array);
            NCTestUtils.assertValueAt(291.85999999999996, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_onlyScaling() throws IOException, InvalidRangeException {
        final File file = createAvhrrNOAA12File();
        reader.open(file);

        try {
            final Array array = reader.readScaled(122, 3757, new Interval(3, 3), "latitude");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-66.167179122, 0, 0, array);
            NCTestUtils.assertValueAt(-66.1891524484, 1, 0, array);
            NCTestUtils.assertValueAt(-66.2111257748, 2, 0, array);

            NCTestUtils.assertValueAt(-66.1452057956, 0, 1, array);
            NCTestUtils.assertValueAt(-66.167179122, 1, 1, array);
            NCTestUtils.assertValueAt(-66.1891524484, 2, 1, array);

            NCTestUtils.assertValueAt(-66.1204858034, 0, 2, array);
            NCTestUtils.assertValueAt(-66.1424591298, 1, 2, array);
            NCTestUtils.assertValueAt(-66.1644324562, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_onlyScalingNOAA19() throws IOException, InvalidRangeException {
        final File file = createAvhrrNOAA19File();
        reader.open(file);

        try {
            final Array array = reader.readScaled(123, 3758, new Interval(3, 3), "latitude");
            assertNotNull(array);

            NCTestUtils.assertValueAt(69.0951248648, 0, 0, array);
            NCTestUtils.assertValueAt(69.0813915358, 1, 0, array);
            NCTestUtils.assertValueAt(69.0676582068, 2, 0, array);

            NCTestUtils.assertValueAt(69.064911541, 0, 1, array);
            NCTestUtils.assertValueAt(69.0539248778, 1, 1, array);
            NCTestUtils.assertValueAt(69.0401915488, 2, 1, array);

            NCTestUtils.assertValueAt(69.037444883, 0, 2, array);
            NCTestUtils.assertValueAt(69.023711554, 1, 2, array);
            NCTestUtils.assertValueAt(69.0127248908, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_noScale_noOffset() throws IOException, InvalidRangeException {
        final File file = createAvhrrMetopAFile();
        reader.open(file);

        try {
            final Array array = reader.readScaled(266, 4622, new Interval(3, 3), "data_quality_bitmask");
            assertNotNull(array);

            NCTestUtils.assertValueAt(0, 0, 0, array);
            NCTestUtils.assertValueAt(0, 1, 0, array);
            NCTestUtils.assertValueAt(0, 2, 0, array);

            NCTestUtils.assertValueAt(0, 0, 1, array);
            NCTestUtils.assertValueAt(0, 1, 1, array);
            NCTestUtils.assertValueAt(0, 2, 1, array);

            NCTestUtils.assertValueAt(0, 0, 2, array);
            NCTestUtils.assertValueAt(0, 1, 2, array);
            NCTestUtils.assertValueAt(0, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize() throws Exception {
        final File file = createAvhrrNOAA12File();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertNotNull(productSize);
            assertEquals(409, productSize.getNx());
            assertEquals(12160, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSizeNOAA19() throws Exception {
        final File file = createAvhrrNOAA19File();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertNotNull(productSize);
            assertEquals(409, productSize.getNx());
            assertEquals(12253, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator() throws IOException {
        final File file = createAvhrrMetopAFile();

        try {
            reader.open(file);
            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            final Point2D[] pixelLocation = pixelLocator.getPixelLocation(-147.53, -23.51);
            assertNotNull(pixelLocation);
            assertEquals(1, pixelLocation.length);

            assertEquals(104.24472976552683, pixelLocation[0].getX(), 1e-8);
            assertEquals(1736.5209274942072, pixelLocation[0].getY(), 1e-8);

            final Point2D geoLocation = pixelLocator.getGeoLocation(104.5, 1736.5, null);
            assertNotNull(geoLocation);
            assertEquals(-147.53990173339844, geoLocation.getX(), 1e-8);
            assertEquals(-23.514205932617188, geoLocation.getY(), 1e-8);
        } finally {
            reader.close();
        }
    }

    private File createAvhrrNOAA12File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n12-fcdr", "vBeta", "1992", "04", "14", "FIDUCEO_FCDR_L1C_AVHRR_NOAA12_19920414141412_19920414155532_EASY_vBeta_fv2.0.0.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }

    private File createAvhrrNOAA19File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n19-fcdr", "v0.2Bet", "2011", "07", "05", "FIDUCEO_FCDR_L1C_AVHRR_N19ALL_20110705055721_20110705073927_EASY_v0.2Bet_fv2.0.0.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }

    private File createAvhrrMetopAFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-ma-fcdr", "vBeta", "2016", "11", "08", "FIDUCEO_FCDR_L1C_AVHRR_METOPA_20161108073729_20161108082817_EASY_vBeta_fv2.0.0.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}

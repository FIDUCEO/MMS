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
import com.bc.fiduceo.reader.time.TimeLocator;
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
public class HIRS_FCDR_Reader_IO_Test {

    private HIRS_FCDR_Reader reader;

    @Before
    public void setUp() throws IOException {
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new HIRS_FCDR_Reader(readerContext);
    }

    @After
    public void tearDown() throws IOException {
        reader.close();
    }

    @Test
    public void testReadAcquisitionInfo_NOAA07() throws IOException {
        final File file = createHirsNOAA07File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1983, 10, 4, 16, 24, 22, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1983, 10, 4, 18, 6, 14, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            assertEquals(2, polygons.size());

            Point[] coordinates = polygons.get(0).getCoordinates();
            assertEquals(45, coordinates.length);
            assertEquals(-27.021698635071516, coordinates[0].getLon(), 1e-8);
            assertEquals(-1.6315195150673392, coordinates[0].getLat(), 1e-8);

            assertEquals(139.99755838885903, coordinates[22].getLon(), 1e-8);
            assertEquals(0.02471999265253544, coordinates[22].getLat(), 1e-8);

            coordinates = polygons.get(1).getCoordinates();
            assertEquals(45, coordinates.length);
            assertEquals(159.83397915959358, coordinates[0].getLon(), 1e-8);
            assertEquals(-2.7878658380359416, coordinates[0].getLat(), 1e-8);

            assertEquals(-36.55262913554907, coordinates[23].getLon(), 1e-8);
            assertEquals(0.8047730941325426, coordinates[23].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);

            coordinates = polygons.get(0).getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1983, 10, 4, 16, 24, 30, time);

            coordinates = polygons.get(1).getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1983, 10, 4, 17, 15, 18, time);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_NOAA09() throws IOException {
        final File file = createHirsNOAA09File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1988, 10, 12, 7, 22, 19, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1988, 10, 12, 9, 4, 18, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            assertEquals(2, polygons.size());

            Point[] coordinates = polygons.get(0).getCoordinates();
            assertEquals(45, coordinates.length);
            assertEquals(121.24881729483606, coordinates[0].getLon(), 1e-8);
            assertEquals(-1.6809595003724098, coordinates[0].getLat(), 1e-8);

            assertEquals(-71.89123196527362, coordinates[22].getLon(), 1e-8);
            assertEquals(0.4998931847512721, coordinates[22].getLat(), 1e-8);

            coordinates = polygons.get(1).getCoordinates();
            assertEquals(45, coordinates.length);
            assertEquals(-51.75817128270865, coordinates[0].getLon(), 1e-8);
            assertEquals(-2.397839287295937, coordinates[0].getLat(), 1e-8);

            assertEquals(111.58604683354497, coordinates[23].getLon(), 1e-8);
            assertEquals(0.8212530892342329, coordinates[23].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);

            coordinates = polygons.get(0).getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1988, 10, 12, 7, 22, 27, time);

            coordinates = polygons.get(1).getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1988, 10, 12, 8, 13, 18, time);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_MetopA() throws IOException {
        final File file = createHirsMetopAFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2015, 3, 26, 17, 36, 56, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2015, 3, 26, 19, 18, 10, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            assertEquals(2, polygons.size());

            Point[] coordinates = polygons.get(0).getCoordinates();
            assertEquals(47, coordinates.length);
            assertEquals(48.91262546181679, coordinates[0].getLon(), 1e-8);
            assertEquals(-1.9501327537000182, coordinates[0].getLat(), 1e-8);

            assertEquals(-143.8648639060557, coordinates[23].getLon(), 1e-8);
            assertEquals(1.40903958119452, coordinates[23].getLat(), 1e-8);

            coordinates = polygons.get(1).getCoordinates();
            assertEquals(45, coordinates.length);
            assertEquals(-124.92934953421356, coordinates[0].getLon(), 1e-8);
            assertEquals(-2.499465923756361, coordinates[0].getLat(), 1e-8);

            assertEquals(36.06921594589949, coordinates[24].getLon(), 1e-8);
            assertEquals(0.6125064846128224, coordinates[24].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);

            coordinates = polygons.get(0).getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2015, 3, 26, 17, 36, 56, time);

            coordinates = polygons.get(1).getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2015, 3, 26, 18, 27, 33, time);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocatorNOAA07() throws IOException {
        final File file = createHirsNOAA07File();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            // todo 1 tb/tb test-files only contain zeros. Update when we have a new FCDR. 2019-02-18
            assertEquals(0L, timeLocator.getTimeFor(169, 0));
            assertEquals(0L, timeLocator.getTimeFor(168, 1));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocatorNOAA09() throws IOException {
        final File file = createHirsNOAA09File();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(592644139616L, timeLocator.getTimeFor(169, 0));
            assertEquals(592644158816L, timeLocator.getTimeFor(168, 3));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocatorMetopA() throws IOException {
        final File file = createHirsMetopAFile();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            // todo 1 tb/tb test-files only contain zeros. Update when we have a new FCDR. 2019-02-18
            assertEquals(0L, timeLocator.getTimeFor(169, 0));
            assertEquals(0L, timeLocator.getTimeFor(168, 1));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables() throws IOException, InvalidRangeException {
        final File file = createHirsMetopAFile();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(108, variables.size());
            Variable variable = variables.get(1);
            assertEquals("longitude", variable.getFullName());

            variable = variables.get(19);
            assertEquals("bt_ch16", variable.getFullName());

            variable = variables.get(40);
            assertEquals("quality_channel_bitmask_ch10", variable.getFullName());

            variable = variables.get(61);
            assertEquals("u_independent_ch11", variable.getFullName());

            variable = variables.get(82);
            assertEquals("u_structured_ch13", variable.getFullName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize() throws Exception {
        final File file = createHirsNOAA07File();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertNotNull(productSize);
            assertEquals(56, productSize.getNx());
            assertEquals(880, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_windowCenter() throws Exception {
        final File file = createHirsNOAA07File();
        reader.open(file);
        try {
            final Array array = reader.readRaw(6, 461, new Interval(3, 3), "bt_ch01");
            assertNotNull(array);

            NCTestUtils.assertValueAt(8152, 0, 0, array);
            NCTestUtils.assertValueAt(8126, 1, 0, array);
            NCTestUtils.assertValueAt(8126, 2, 0, array);
            NCTestUtils.assertValueAt(8126, 0, 1, array);
            NCTestUtils.assertValueAt(8153, 1, 1, array);
            NCTestUtils.assertValueAt(8153, 2, 1, array);
            NCTestUtils.assertValueAt(8232, 0, 2, array);
            NCTestUtils.assertValueAt(8126, 1, 2, array);
            NCTestUtils.assertValueAt(8126, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomWindowOut() throws Exception {
        final File file = createHirsMetopAFile();
        reader.open(file);

        try {
            final Array array = reader.readRaw(39, 901, new Interval(3, 3), "bt_ch02");
            assertNotNull(array);

            NCTestUtils.assertValueAt(5854, 0, 0, array);
            NCTestUtils.assertValueAt(5663, 1, 0, array);
            NCTestUtils.assertValueAt(5816, 2, 0, array);
            NCTestUtils.assertValueAt(5790, 0, 1, array);
            NCTestUtils.assertValueAt(5675, 1, 1, array);
            NCTestUtils.assertValueAt(5675, 2, 1, array);
            NCTestUtils.assertValueAt(-999, 0, 2, array);
            NCTestUtils.assertValueAt(-999, 1, 2, array);
            NCTestUtils.assertValueAt(-999, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topWindowOut() throws Exception {
        final File file = createHirsNOAA07File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(46, 1, new Interval(3, 4), "u_independent_ch03");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-1, 0, 0, array);
            NCTestUtils.assertValueAt(-1, 1, 0, array);
            NCTestUtils.assertValueAt(-1, 2, 0, array);

            NCTestUtils.assertValueAt(127, 0, 1, array);
            NCTestUtils.assertValueAt(127, 1, 1, array);
            NCTestUtils.assertValueAt(127, 2, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_leftWindowOut() throws Exception {
        final File file = createHirsMetopAFile();
        reader.open(file);

        try {
            final Array array = reader.readRaw(0, 277, new Interval(5, 5), "quality_channel_bitmask_ch04");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-127, 0, 0, array);
            NCTestUtils.assertValueAt(-127, 1, 0, array);
            NCTestUtils.assertValueAt(16, 2, 0, array);
            NCTestUtils.assertValueAt(16, 3, 0, array);
            NCTestUtils.assertValueAt(16, 4, 0, array);

            NCTestUtils.assertValueAt(-127, 0, 4, array);
            NCTestUtils.assertValueAt(-127, 1, 4, array);
            NCTestUtils.assertValueAt(16, 2, 4, array);
            NCTestUtils.assertValueAt(16, 3, 4, array);
            NCTestUtils.assertValueAt(16, 4, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_scalingAndOffset() throws IOException {
        final File file = createHirsNOAA07File();
        reader.open(file);

        try {
            final Array array = reader.readScaled(31, 280, new Interval(3, 3), "bt_ch05");
            assertNotNull(array);

            NCTestUtils.assertValueAt(236.3, 0, 0, array);
            NCTestUtils.assertValueAt(235.56, 1, 0, array);
            NCTestUtils.assertValueAt(235.42, 2, 0, array);

            NCTestUtils.assertValueAt(236.49, 0, 1, array);
            NCTestUtils.assertValueAt(235.52, 1, 1, array);
            NCTestUtils.assertValueAt(235.71, 2, 1, array);

            NCTestUtils.assertValueAt(234.97, 0, 2, array);
            NCTestUtils.assertValueAt(234.83, 1, 2, array);
            NCTestUtils.assertValueAt(234.83, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_onlyScaling() throws IOException {
        final File file = createHirsMetopAFile();
        reader.open(file);

        try {
            final Array array = reader.readScaled(27, 656, new Interval(3, 3), "longitude");
            assertNotNull(array);

            NCTestUtils.assertValueAt(175.8690143755, 0, 0, array);
            NCTestUtils.assertValueAt(175.03952128880002, 1, 0, array);
            NCTestUtils.assertValueAt(174.2320015289, 2, 0, array);

            NCTestUtils.assertValueAt(174.523148109, 0, 1, array);
            NCTestUtils.assertValueAt(173.69365502230002, 1, 1, array);
            NCTestUtils.assertValueAt(172.88613526240002, 2, 1, array);

            NCTestUtils.assertValueAt(173.1113618621, 0, 2, array);
            NCTestUtils.assertValueAt(172.2818687754, 1, 2, array);
            NCTestUtils.assertValueAt(171.47984234720002, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_noScale_noOffset() throws IOException {
        final File file = createHirsNOAA07File();
        reader.open(file);

        try {
            final Array array = reader.readScaled(38, 182, new Interval(3, 3), "scanline_origl1b");
            assertNotNull(array);

            NCTestUtils.assertValueAt(49, 0, 0, array);
            NCTestUtils.assertValueAt(49, 1, 0, array);
            NCTestUtils.assertValueAt(49, 2, 0, array);

            NCTestUtils.assertValueAt(50, 0, 1, array);
            NCTestUtils.assertValueAt(50, 1, 1, array);
            NCTestUtils.assertValueAt(50, 2, 1, array);

            NCTestUtils.assertValueAt(51, 0, 2, array);
            NCTestUtils.assertValueAt(51, 1, 2, array);
            NCTestUtils.assertValueAt(51, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime() throws IOException, InvalidRangeException {
        final File file = createHirsNOAA09File();

        try {
            reader.open(file);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(39, 218, new Interval(3, 3));
            NCTestUtils.assertValueAt(592645676, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(592645682, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(592645688, 2, 2, acquisitionTime);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator() throws IOException {
        final File file = createHirsMetopAFile();

        try {
            reader.open(file);
            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            final Point2D geoLocation = pixelLocator.getGeoLocation(38.5, 782.5, null);
            assertNotNull(geoLocation);
            assertEquals(49.66521072387695, geoLocation.getX(), 1e-8);
            assertEquals(-46.84164047241211, geoLocation.getY(), 1e-8);

            final Point2D[] pixelLocation = pixelLocator.getPixelLocation(49.66521072387695, -46.84164047241211);
            assertNotNull(pixelLocation);
            assertEquals(1, pixelLocation.length);

            assertEquals(38.317269513397704, pixelLocation[0].getX(), 1e-8);
            assertEquals(781.666865947429, pixelLocation[0].getY(), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator_N09() throws IOException {
        final File file = createHirsNOAA09File();

        try {
            reader.open(file);
            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            final Point2D geoLocation = pixelLocator.getGeoLocation(38.5, 782.5, null);
            assertNotNull(geoLocation);
            assertEquals(117.93634033203125, geoLocation.getX(), 1e-8);
            assertEquals(-35.07767105102539, geoLocation.getY(), 1e-8);

            final Point2D[] pixelLocation = pixelLocator.getPixelLocation(117.93634033203125, -35.07767105102539);
            assertNotNull(pixelLocation);
            assertEquals(1, pixelLocation.length);

            assertEquals(38.50497313441438, pixelLocation[0].getX(), 1e-8);
            assertEquals(783.6241097278424, pixelLocation[0].getY(), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetSubScenePixelLocator() throws IOException {
        final File file = createHirsNOAA07File();

        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Polygon polygon = (Polygon) geometryFactory.parse("POLYGON((149 7, 149 9, 151 9, 151 7, 149 7))");

        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getSubScenePixelLocator(polygon);

            final Point2D geoLocation = pixelLocator.getGeoLocation(32.5, 415.5, null);
            assertEquals(150.97872924804688, geoLocation.getX(), 1e-8);
            assertEquals(8.10815715789795, geoLocation.getY(), 1e-8);

            final Point2D[] pixelLocation = pixelLocator.getPixelLocation(150.97872924804688, 8.10815715789795);
            assertNotNull(pixelLocation);
            assertEquals(1, pixelLocation.length);

            assertEquals(33.59545482099081, pixelLocation[0].getX(), 1e-8);
            assertEquals(416.5860033509568, pixelLocation[0].getY(), 1e-8);

        } finally {
            reader.close();
        }
    }

    private File createHirsNOAA07File() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-n07-fcdr", "v0.8rc1", "1983", "10", "04", "FIDUCEO_FCDR_L1C_HIRS2_NOAA07_19831004162422_19831004180614_EASY_v0.8rc1_fv2.0.0.nc"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File createHirsNOAA09File() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-n09-fcdr", "v1.00", "1988", "10", "12", "FIDUCEO_FCDR_L1C_HIRS2_NOAA09_19881012072219_19881012090418_EASY_v1.00_fv2.0.0.nc"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File createHirsMetopAFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-ma-fcdr", "v0.8rc1", "2015", "03", "26", "FIDUCEO_FCDR_L1C_HIRS4_METOPA_20150326173656_20150326191810_EASY_v0.8rc1_fv2.0.0.nc"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}

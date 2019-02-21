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
import com.bc.fiduceo.reader.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class AVHRR_FRAC_Reader_IO_Test {

    private File dataDirectory;
    private AVHRR_FRAC_Reader reader;

    @Before
    public void setUp() throws IOException {
        dataDirectory = TestUtil.getTestDataDirectory();

        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new AVHRR_FRAC_Reader(readerContext);
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File file = getAvhrrFRACFile();

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

    @SuppressWarnings("PointlessArithmeticExpression")
    @Test
    public void testGetTimeLocator() throws IOException {
        final File file = getAvhrrFRACFile();

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
    public void testGetVariables() throws IOException {
        final File file = getAvhrrFRACFile();

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
    public void testGetProductSize() throws IOException {
        final File file = getAvhrrFRACFile();

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
    public void testReadAcquisitionTime() throws IOException, InvalidRangeException {
        final File file = getAvhrrFRACFile();

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
    public void testReadAcquisitionTime_borderPixel() throws IOException, InvalidRangeException {
        final File file = getAvhrrFRACFile();

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
    public void testReadScaled() throws IOException, InvalidRangeException {
        final File file = getAvhrrFRACFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readScaled(15, 235, interval, "radiance_1");
            NCTestUtils.assertValueAt(122.30326843261719, 0, 0, array);
            NCTestUtils.assertValueAt(122.58930969238281, 1, 0, array);

            array = reader.readScaled(16, 236, interval, "radiance_3a");
            NCTestUtils.assertValueAt(15.061016082763672, 0, 1, array);
            NCTestUtils.assertValueAt(15.061016082763672, 1, 1, array);
            NCTestUtils.assertValueAt(15.241663932800293, 2, 1, array);

            array = reader.readScaled(17, 237, interval, "radiance_4");
            NCTestUtils.assertValueAt(47.628501892089844, 2, 2, array);
            NCTestUtils.assertValueAt(47.82387924194336, 3, 2, array);
            NCTestUtils.assertValueAt(48.801361083984375, 4, 2, array);

            array = reader.readScaled(18, 238, interval, "reflec_1");
            NCTestUtils.assertValueAt(27.267599105834964, 3, 0, array);
            NCTestUtils.assertValueAt(27.101200103759766, 3, 1, array);
            NCTestUtils.assertValueAt(26.934799194335938, 3, 2, array);
            NCTestUtils.assertValueAt(26.768400192260742, 3, 3, array);

            array = reader.readScaled(19, 239, interval, "reflec_3a");
            NCTestUtils.assertValueAt(21.059200286865234, 2, 0, array);
            NCTestUtils.assertValueAt(20.820899963378906, 2, 1, array);
            NCTestUtils.assertValueAt(20.820899963378906, 2, 2, array);
            NCTestUtils.assertValueAt(21.53580093383789, 2, 3, array);

            array = reader.readScaled(20, 240, interval, "temp_4");
            NCTestUtils.assertValueAt(256.4285888671875, 2, 0, array);
            NCTestUtils.assertValueAt(259.6644287109375, 3, 0, array);

            array = reader.readScaled(21, 241, interval, "flags");
            NCTestUtils.assertValueAt(0, 4, 0, array);
            NCTestUtils.assertValueAt(0, 0, 1, array);
            NCTestUtils.assertValueAt(0, 1, 1, array);

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
    public void testReadRaw() throws IOException, InvalidRangeException {
        final File file = getAvhrrFRACFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(25, 333, interval, "radiance_2");
            NCTestUtils.assertValueAt(85.879150390625, 3, 0, array);
            NCTestUtils.assertValueAt(86.09809112548828, 4, 0, array);

            array = reader.readRaw(0, 335, interval, "radiance_3b");
            NCTestUtils.assertValueAt(0.0, 0, 1, array);
            NCTestUtils.assertValueAt(0.0, 1, 1, array);
            NCTestUtils.assertValueAt(0.0, 2, 1, array);

            array = reader.readRaw(511, 337, interval, "radiance_5");
            NCTestUtils.assertValueAt(61.82318878173828, 2, 1, array);
            NCTestUtils.assertValueAt(61.61263656616211, 3, 1, array);
            NCTestUtils.assertValueAt(61.61263656616211, 4, 1, array);

            array = reader.readRaw(217, 0, interval, "reflec_2");
            NCTestUtils.assertValueAt(0.0, 2, 0, array);
            NCTestUtils.assertValueAt(0.0, 2, 1, array);
            NCTestUtils.assertValueAt(27.149749755859375, 2, 2, array);
            NCTestUtils.assertValueAt(27.219499588012695, 2, 3, array);

            array = reader.readRaw(219, 33215, interval, "temp_3b");
            NCTestUtils.assertValueAt(0.0, 2, 0, array);
            NCTestUtils.assertValueAt(0.0, 2, 1, array);
            NCTestUtils.assertValueAt(0.0, 2, 2, array);
            NCTestUtils.assertValueAt(0.0, 2, 3, array);

            array = reader.readRaw(244, 21770, interval, "temp_5");
            NCTestUtils.assertValueAt(260.2013244628906, 0, 2, array);
            NCTestUtils.assertValueAt(264.3916015625, 1, 2, array);

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
    public void testReadRaw_corner_pixels() throws IOException, InvalidRangeException {
        final File file = getAvhrrFRACFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(0, 0, interval, "radiance_1");
            NCTestUtils.assertValueAt(0.0, 0, 2, array);
            NCTestUtils.assertValueAt(0.0, 1, 2, array);
            NCTestUtils.assertValueAt(106.28462219238281, 2, 2, array);
            NCTestUtils.assertValueAt(116.01022338867188, 3, 2, array);
            NCTestUtils.assertValueAt(120.30093383789062, 4, 2, array);

            array = reader.readRaw(2000, 0, interval, "radiance_2");
            NCTestUtils.assertValueAt(7.716379165649414, 0, 2, array);
            NCTestUtils.assertValueAt(7.716379165649414, 1, 2, array);
            NCTestUtils.assertValueAt(7.716379165649414, 2, 2, array);
            NCTestUtils.assertValueAt(0.0, 3, 2, array);
            NCTestUtils.assertValueAt(0.0, 4, 2, array);

            array = reader.readRaw(2000, 36120, interval, "radiance_3a");
            NCTestUtils.assertValueAt(12.351310729980469, 0, 2, array);
            NCTestUtils.assertValueAt(10.661288261413574, 1, 2, array);
            NCTestUtils.assertValueAt(15.061016082763672, 2, 2, array);
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
    public void testGetPixelLocator() throws IOException {
        final File file = getAvhrrFRACFile();

        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            Point2D geoLocation = pixelLocator.getGeoLocation(986.5, 3869.5, null);
            assertEquals(-64.01632605615693, geoLocation.getX(), 1e-8);
            assertEquals(47.05343743801117, geoLocation.getY(), 1e-8);

            Point2D[] pixelLocation = pixelLocator.getPixelLocation(-64.01632605615693, 47.05343743801117);
            assertEquals(1, pixelLocation.length);
            // error: 5.46 px
            //assertEquals(986.5, pixelLocation[0].getX(), 1e-8);
            assertEquals(3869.5451168166296, pixelLocation[0].getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(12.5, 3869.5, null);
            assertEquals(-81.15115974907908, geoLocation.getX(), 1e-8);
            assertEquals(48.856072149276734, geoLocation.getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(-81.15115974907908, 48.856072149276734);
            assertEquals(1, pixelLocation.length);
            // error: 15.35 px
            // assertEquals(12.5, pixelLocation[0].getX(), 1e-8);
            assertEquals(3869.923861607758, pixelLocation[0].getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(1940.5, 3869.5, null);
            assertEquals(-50.24830100693062, geoLocation.getX(), 1e-8);
            assertEquals(43.46240911483765, geoLocation.getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(-50.24830100693062, 43.46240911483765);
            assertEquals(1, pixelLocation.length);
            // error: 18.48 px
            // assertEquals(1940.5, pixelLocation[0].getX(), 1e-8);
            assertEquals(3869.691890616665, pixelLocation[0].getY(), 1e-8);


//            Point2D[] pixelLocation = pixelLocator.getPixelLocation(133.2231219776277, -62.11543647766112);
//            assertEquals(1, pixelLocation.length);
//            //assertEquals(133.7834674142037, pixelLocation[0].getX(), 1e-8);
//           // assertEquals(20044.5295334818, pixelLocation[0].getY(), 1e-8);
//
//            geoLocation = pixelLocator.getGeoLocation(1000.5, 25804.5, null);
//            assertEquals(93.33047104606814, geoLocation.getX(), 1e-8);
//            assertEquals(-11.28578977584838, geoLocation.getY(), 1e-8);
//
//            pixelLocation = pixelLocator.getPixelLocation(93.33047104606814, -11.28578977584838);
//            assertEquals(1, pixelLocation.length);
//            assertEquals(995.9942357452363, pixelLocation[0].getX(), 1e-8);
//            assertEquals(25804.59016708001, pixelLocation[0].getY(), 1e-8);
//
//            pixelLocation = pixelLocator.getPixelLocation(1723, -88);
//            assertEquals(0, pixelLocation.length);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetSubscenePixelLocator() throws IOException {
        final File file = getAvhrrFRACFile();

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
            assertEquals(490.5967612225541, pixelLocation[0].getX(), 1e-8);
            assertEquals(21176.498130707176, pixelLocation[0].getY(), 1e-8);
        } finally {
            reader.close();
        }
    }

    private File getAvhrrFRACFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-frac-ma", "v1", "2018", "05", "11", "NSS.FRAC.M2.D18131.S1404.E1544.B5998081.SV"}, false);

        final File file = new File(dataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}

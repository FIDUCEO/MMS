package com.bc.fiduceo.reader.smos;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.util.TempFileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.*;

@SuppressWarnings("NewClassNamingConvention")
@RunWith(IOTestRunner.class)
public class SmosL1CDailyGriddedReader_IO_Test {

    private SmosL1CDailyGriddedReader reader;

    @Before
    public void setUp() throws IOException {
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        final File testDir = TestUtil.getTestDir();
        if (!testDir.mkdirs()) {
            fail("unable to create test directory");
        }

        readerContext.setTempFileUtils(new TempFileUtils(testDir.getAbsolutePath()));
        reader = new SmosL1CDailyGriddedReader(readerContext);
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testReadAcquisitionInfo_CDF3TA() throws IOException {
        final File file = getCDF3TAFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertTrue(boundingGeometry instanceof Polygon);

            Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(5, coordinates.length);
            assertEquals(-179.8703155517578, coordinates[0].getLon(), 1e-8);
            assertEquals(-83.51713562011719, coordinates[0].getLat(), 1e-8);
            assertEquals(-179.8703155517578, coordinates[1].getLon(), 1e-8);
            assertEquals(83.51713562011719, coordinates[1].getLat(), 1e-8);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2016, 6, 10, 0, 0, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2016, 6, 10, 23, 59, 59, sensingStop);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            final TimeAxis timeAxis = timeAxes[0];
            assertTrue(timeAxis instanceof L3TimeAxis);
            final Geometry geometry = timeAxis.getGeometry();
            coordinates = geometry.getCoordinates();
            assertEquals(4, coordinates.length);
            assertEquals(-179.8703155517578, coordinates[0].getLon(), 1e-8);
            assertEquals(0.0, coordinates[0].getLat(), 1e-8);
            assertEquals(0.0, coordinates[3].getLon(), 1e-8);
            assertEquals(-83.51713562011719, coordinates[3].getLat(), 1e-8);

            TestUtil.assertCorrectUTCDate(2016, 6, 10, 0, 0, 0, timeAxis.getStartTime());
            TestUtil.assertCorrectUTCDate(2016, 6, 10, 23, 59, 59, timeAxis.getEndTime());

            assertEquals(NodeType.UNDEFINED, acquisitionInfo.getNodeType());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_CDF3TD() throws IOException {
        final File file = getCDF3TDFile();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertNotNull(productSize);
            assertEquals(1388, productSize.getNx());
            assertEquals(584, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator_CDF3TA() throws IOException {
        final File file = getCDF3TAFile();

        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            Point2D geoLocation = pixelLocator.getGeoLocation(0.5, 0.5, null);
            assertEquals(-179.8703155517578, geoLocation.getX(), 1e-8);
            assertEquals(-83.51713562011719, geoLocation.getY(), 1e-8);

            Point2D[] pixelLocations = pixelLocator.getPixelLocation(-179.8703155517578, -83.51713562011719);
            assertEquals(1, pixelLocations.length);
            assertEquals(0.5, pixelLocations[0].getX(), 1e-8);
            assertEquals(0.5, pixelLocations[0].getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(762.5, 404.5, null);
            assertEquals(17.766571044921875, geoLocation.getX(), 1e-8);
            assertEquals(22.638275146484375, geoLocation.getY(), 1e-8);

            pixelLocations = pixelLocator.getPixelLocation(17.766571044921875, 22.638275146484375);
            assertEquals(1, pixelLocations.length);
            assertEquals(762.5, pixelLocations[0].getX(), 1e-8);
            assertEquals(404.5, pixelLocations[0].getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(1387.5, 583.5, null);
            assertEquals(179.8703155517578, geoLocation.getX(), 1e-8);
            assertEquals(83.51713562011719, geoLocation.getY(), 1e-8);

            pixelLocations = pixelLocator.getPixelLocation(179.9, 83.6);
            assertEquals(1, pixelLocations.length);
            assertEquals(1387.5, pixelLocations[0].getX(), 1e-8);
            assertEquals(583.5, pixelLocations[0].getY(), 1e-8);

            // check outside locations
            geoLocation = pixelLocator.getGeoLocation(-1, 0.5, null);
            assertNull(geoLocation);

            geoLocation = pixelLocator.getGeoLocation(22.5, 685.5, null);
            assertNull(geoLocation);

            pixelLocations = pixelLocator.getPixelLocation(116.7, -89.6);
            assertEquals(0, pixelLocations.length);

            pixelLocations = pixelLocator.getPixelLocation(116.7, 89.6);
            assertEquals(0, pixelLocations.length);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetSubScenePixelLocator_CDF3TD() throws IOException {
        final File file = getCDF3TDFile();

        try {
            reader.open(file);

            final PixelLocator subScenePixelLocator = reader.getSubScenePixelLocator(null);// geometry is not used here tb 2022-09-29
            final PixelLocator pixelLocator = reader.getPixelLocator();

            assertSame(pixelLocator, subScenePixelLocator);
        } finally {
            reader.close();
        }
    }

    private File getCDF3TAFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"miras-smos-CDF3TA", "re07", "2016", "162", "SM_RE07_MIR_CDF3TA_20160610T000000_20160610T235959_330_001_7.tgz"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File getCDF3TDFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"miras-smos-CDF3TD", "re07", "2017", "324", "SM_RE07_MIR_CDF3TD_20171120T000000_20171120T235959_330_001_7.tgz"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}

package com.bc.fiduceo.reader.smap;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class SmapPixelLocatorTest {

    private NetcdfFile ncOpened;

    @Before
    public void setUp() throws Exception {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"smap-sss", "v05.0", "2018", "02", "04", "RSS_SMAP_SSS_L2C_r16092_20180204T202311_2018035_FNL_V05.0.nc"}, false);
        final File file = TestUtil.getTestDataFileAsserted(testFilePath);
        ncOpened = NetcdfFiles.open(file.getAbsolutePath());
    }

    @After
    public void tearDown() throws Exception {
        ncOpened.close();
    }

    @Test
    public void testConstructor() throws IOException {
        assertNotNull(createLocator());
    }

    @Test
    public void testThatGetGeoLocationReturnsANewPoint2DInstanceIfGivenGelolocationIsNull() throws IOException {
        final SmapPixelLocator locator = createLocator();

        Point2D geoLocation = null;

        geoLocation = locator.getGeoLocation(1455.5, 20.5, geoLocation);
        assertNotNull(geoLocation);
        assertEquals(50.03237915, geoLocation.getX(), 1e-6);
        assertEquals(-84.91764832, geoLocation.getY(), 1e-6);
    }

    @Test
    public void testThatGetGeoLocationReusesAGivenPoint2DInstanceToReturnLonLatValues() throws IOException {
        final SmapPixelLocator locator = createLocator();

        final Point2D geoLocation = new Point2D.Float();

        final Point2D retVal = locator.getGeoLocation(15.5, 20.5, geoLocation);
        assertNotNull(retVal);
        assertSame(geoLocation, retVal);
        assertEquals(50.1703681, retVal.getX(), 1e-6);
        assertEquals(-84.8761215, retVal.getY(), 1e-6);

        final Point2D retVal2 = locator.getGeoLocation(1455.5, 20.5, geoLocation);
        assertNotNull(retVal2);
        assertSame(geoLocation, retVal2);
        assertEquals(50.03237915, retVal2.getX(), 1e-6);
        assertEquals(-84.91764832, retVal2.getY(), 1e-6);
    }

    @Test
    public void testThatGetGeoLocationReturnsNullIfThereIsNoValidGeoposition() throws IOException {
        final SmapPixelLocator locator = createLocator();

        final Point2D geoLocation = locator.getGeoLocation(13.5, 7.5, null);
        assertNull(geoLocation);
    }

    @Test
    public void testThatGetPixelLocationReturns2LocationsIfAvailable() throws IOException {
        final SmapPixelLocator locator = createLocator();

        final Point2D[] pixelLoc = locator.getPixelLocation(50.1703681, -84.8761215);
        assertNotNull(pixelLoc);
        assertEquals(2, pixelLoc.length);
        assertEquals(15.5, pixelLoc[0].getX(), 1e-6);
        assertEquals(20.5, pixelLoc[0].getY(), 1e-6);
        assertEquals(1454.5, pixelLoc[1].getX(), 1e-6);
        assertEquals(20.5, pixelLoc[1].getY(), 1e-6);
    }

    @Test
    public void testThatGetPixelLocationWorksOnLeftProductBorder() throws IOException {
        final SmapPixelLocator locator = createLocator();

        final Point2D[] pixelLoc = locator.getPixelLocation(53.83325958, -79.87584686);
        assertNotNull(pixelLoc);
        assertEquals(2, pixelLoc.length);
        assertEquals(0.5, pixelLoc[0].getX(), 1e-6);
        assertEquals(40.5, pixelLoc[0].getY(), 1e-6);
        assertEquals(1440.5, pixelLoc[1].getX(), 1e-6);
        assertEquals(40.5, pixelLoc[1].getY(), 1e-6);
    }

    @Test
    public void testGetPixelLocationReturnsEmptyArrayIfGeoLocationPointsToFillValuesArea() throws IOException {
        final SmapPixelLocator locator = createLocator();

        // points to x: 1135.0  y: 487.0
        final Point2D[] pixelLoc = locator.getPixelLocation(130.125, 31.88);
        assertNotNull(pixelLoc);
        assertEquals(0, pixelLoc.length);
    }

    private SmapPixelLocator createLocator() throws IOException {
        final Variable latVar = ncOpened.findVariable("cellat");
        final Variable lonVar = ncOpened.findVariable("cellon");
        final SmapPixelLocator locator = new SmapPixelLocator(lonVar, latVar, 0);
        return locator;
    }
}
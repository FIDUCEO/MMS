package com.bc.fiduceo.location;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class SwathPixelLocatorTest {

    private NetcdfFile netcdfFile;
    private PixelLocator pixelLocator;
    private int height;
    private int width;

    @Before
    public void setUp() throws Exception {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n17", "v01.3", "2007", "04", "01", "20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc"}, false);
        netcdfFile = NetcdfFile.open(new File(testDataDirectory, testFilePath).getAbsolutePath());

        final Variable lonVar = netcdfFile.findVariable("lon");
        final Variable latVar = netcdfFile.findVariable("lat");

        final int[] shape = lonVar.getShape();
        height = shape[0];
        width = shape[1];

        final Array lonArray = lonVar.read();
        final Array latArray = latVar.read();

        pixelLocator = new SwathPixelLocator(lonArray, latArray, width, height);
    }

    @After
    public void tearDown() throws Exception {
        netcdfFile.close();
    }

    @Test
    public void testBothWaysOfPixelLocator() throws Exception {

        final int border = 15;
        final double delta = 0.49999;
        for (int h = 0; h < height; h++) {
            for (int w = border; w < width - border; w++) {
                final double y = h + 0.5;
                final double x = w + 0.5;
                final Point2D geoPos = pixelLocator.getGeoLocation(x, y, null);
                assertNotNull("Unable to fetch a geoposition for x=" + x + " y=" + y, geoPos);

                final double lon = geoPos.getX();
                final double lat = geoPos.getY();
                final Point2D[] locations = pixelLocator.getPixelLocation(lon, lat);

                assertNotNull("x=" + x + " y=" + y + " | <null> Unable to fetch a pixel position for lon=" + lon + " lat=" + lat, locations);
                assertTrue("x=" + x + " y=" + y + " |  Unable to fetch a pixel position for lon=" + lon + " lat=" + lat, locations.length > 0);
                if (locations.length == 1) {
                    assertEquals("x=" + x + " y=" + y + " |  Unable to fetch the correct pixel position for lon=" + lon + " lat=" + lat, x, locations[0].getX(), delta);
                    assertEquals("x=" + x + " y=" + y + " |  Unable to fetch the correct pixel position for lon=" + lon + " lat=" + lat, y, locations[0].getY(), delta);
                } else {
                    final int idx = getCloserIndex(y, locations);
                    assertEquals("x=" + x + " y=" + y + " |  Unable to fetch the correct pixel position for lon=" + lon + " lat=" + lat, x, locations[idx].getX(), delta);
                    assertEquals("x=" + x + " y=" + y + " |  Unable to fetch the correct pixel position for lon=" + lon + " lat=" + lat, y, locations[idx].getY(), delta);
                }
            }
        }
    }

    private int getCloserIndex(double y, Point2D[] locations) {
        return Math.abs(y - locations[0].getY()) < Math.abs(y - locations[1].getY()) ? 0 : 1;
    }
}
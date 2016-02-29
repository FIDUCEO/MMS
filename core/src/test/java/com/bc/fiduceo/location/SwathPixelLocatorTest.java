package com.bc.fiduceo.location;

import static org.junit.Assert.assertEquals;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.ImageUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.Point;
import java.awt.image.RenderedImage;
import java.io.File;

/**
 * Created by Sabine on 29.02.2016.
 */
@RunWith(IOTestRunner.class)
public class SwathPixelLocatorTest {

    private NetcdfFile netcdfFile;
    private PixelLocator pixelLocator;
    private int height;
    private int width;

    @Before
    public void setUp() throws Exception {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        netcdfFile = NetcdfFile.open(new File(testDataDirectory, "20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc").getAbsolutePath());

        final Variable lonVar = netcdfFile.findVariable("lon");
        final int[] shape = lonVar.getShape();
        height = shape[0];
        width = shape[1];

        final ArrayFloat lon = (ArrayFloat) lonVar.read();
        final ArrayFloat lat = (ArrayFloat) netcdfFile.findVariable("lat").read();

        pixelLocator = SwathPixelLocator.create(lon, lat, width, height, 2048);
    }

    @After
    public void tearDown() throws Exception {
        netcdfFile.close();
    }

    @Test
    public void testBothWaysOfPixelLocator() throws Exception {

        for (int y = 0; y < height; y++) {
            // todo 1 se/mb implement a step over e.g. 50 2016-02-29
            final int x = y % width;
            boolean worksFine;
            final Point lonLat = new Point();
            worksFine = pixelLocator.getGeoLocation(x, y, lonLat);
            assertEquals("Unable to fetch a geoposition for x=" + x + " y=" + y, true, worksFine);

            final double lon = lonLat.getX();
            final double lat = lonLat.getY();
            final Point pixelPos = new Point();
            worksFine = pixelLocator.getPixelLocation(lon, lat, pixelPos);

            // todo 1 se/mb remove the //      2016-02-29
//            assertEquals("x=" + x + " y=" + y +" |  Unable to fetch a pixel position for lon=" + lon + " lat=" + lat, true, worksFine);
//            assertEquals(x, (int) Math.floor(pixelPos.getX()));
//            assertEquals(y, (int) Math.floor(pixelPos.getY()));

        }
    }
}
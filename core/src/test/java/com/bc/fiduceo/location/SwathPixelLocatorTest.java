package com.bc.fiduceo.location;

import static org.junit.Assert.*;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.TiePointGrid;
import org.esa.snap.core.util.ImageUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.Section;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import javax.media.jai.PlanarImage;
import java.awt.geom.Point2D;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.Arrays;

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
        final Variable latVar = netcdfFile.findVariable("lat");

        final int[] shape = lonVar.getShape();
        height = shape[0];
        width = shape[1];

        final Array lonArray = lonVar.read();
        final Array latArray = latVar.read();

        pixelLocator = new SwathPixelLocator(lonArray, latArray, width, height);
//        pixelLocator = new SwathPixelLocator(lonPi, latPi);
    }

    @After
    public void tearDown() throws Exception {
        netcdfFile.close();
    }

    @Test
    public void testBothWaysOfPixelLocator() throws Exception {

        final int border = 0;
        final double delta = 0.49999;
        for (int h = 0; h < height; h++) {
            final boolean borderTouched = h < border
                                          || h >= height - border
                                          || h % width < border
                                          || h % width >= width - border;
            if (borderTouched) {
                continue;
            }
            final double y = h + 0.5;
            final double x = h % width + 0.5;

            System.out.println("x/y = " + x + " / " + y);
            boolean worksFine;
            final Point2D pos = new Point2D.Double();
            worksFine = pixelLocator.getGeoLocation(x, y, pos);
            assertEquals("Unable to fetch a geoposition for x=" + x + " y=" + y, true, worksFine);

            final double lon = pos.getX();
            final double lat = pos.getY();
            final Point2D[] locations = pixelLocator.getPixelLocation(lon, lat, pos);

            assertNotNull("x=" + x + " y=" + y + " |  Unable to fetch a pixel position for lon=" + lon + " lat=" + lat, locations);
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

    private int getCloserIndex(double y, Point2D[] locations) {
        return Math.abs(y - locations[0].getY()) < Math.abs(y - locations[1].getY()) ? 0 : 1;
    }

    @Test
    public void testEstimationWithPoleInside() throws Exception {
        int orIdxCol = 32;
        int orIdxRow = 3278;

        final int[] stride = {10, 10};

        final int targetWidth = 41;
        final int targetHeight = 9;
        final int[] size = {(targetHeight - 1) * stride[1] + 1, (targetWidth - 1) * stride[0] + 1};


        final int offsetXDirection = orIdxCol % stride[0];
        final int countOfGaps = targetHeight - 1;
        final int halfCountOfGaps = countOfGaps / 2;
        final int offsetYDirection = orIdxRow - stride[1] * halfCountOfGaps;
        final int[] origin = {offsetYDirection, offsetXDirection};

        final Section section = new Section(origin, size, stride);

        final Variable lat = netcdfFile.findVariable("lat");
        final ArrayFloat latTiePoints = (ArrayFloat) lat.read(section);

        final Variable lon = netcdfFile.findVariable("lon");
        final float expectedLon = lon.read(new int[]{orIdxRow, orIdxCol}, new int[]{1, 1}).getFloat(0);
        final float expectedLat = lat.read(new int[]{orIdxRow, orIdxCol}, new int[]{1, 1}).getFloat(0);
        final ArrayFloat lonTiePoints = (ArrayFloat) lon.read(section);

        final int poleIndex = targetWidth * halfCountOfGaps + orIdxCol / stride[0];
        assertEquals(expectedLat, latTiePoints.getFloat(poleIndex), 0.000001);
        assertEquals(expectedLon, lonTiePoints.getFloat(poleIndex), 0.000001);

        final float[] latFloats = (float[]) latTiePoints.getStorage();
        final float[] lonFloats = (float[]) lonTiePoints.getStorage();

        final double[][] latValues = getReorganizedDoubleValues(targetWidth, targetHeight, latFloats);
        final double[][] lonValues = getReorganizedDoubleValues(targetWidth, targetHeight, lonFloats);

//        final GeoApproximation latApproximation = GeoApproximation.create(latValues, 0.3, new Rectangle(0, 0, 41, 9));
//        final GeoApproximation lonApproximation = GeoApproximation.create(lonValues, 0.3, new Rectangle(0, 0, 41, 9));
//        new SwathPixelLocator.PixelLocationEstimator(new org.esa.snap.core.datamodel.GeoApproximation[]{})
    }

    private double[][] getReorganizedDoubleValues(int targetWidth, int targetHeight, float[] floats) {
        final double[][] latValues = new double[targetHeight][];
        for (int i = 0; i < targetHeight; i++) {
            final int start = i * targetWidth;
            final int end = start + targetWidth;
            final float[] fLine = Arrays.copyOfRange(floats, start, end);
            final double[] dLine = new double[targetWidth];
            for (int j = 0; j < dLine.length; j++) {
                dLine[j] = fLine[j];
            }
            latValues[i] = dLine;
        }
        return latValues;
    }
}
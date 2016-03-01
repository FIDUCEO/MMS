package com.bc.fiduceo.location;

import static org.junit.Assert.*;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.math.GeoApproximation;
import org.junit.*;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.Section;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
        final int[] shape = lonVar.getShape();
        height = shape[0];
        width = shape[1];

        final ArrayFloat lon = (ArrayFloat) lonVar.read();
        final ArrayFloat lat = (ArrayFloat) netcdfFile.findVariable("lat").read();

        pixelLocator = SwathPixelLocator.create(lon, lat, width, height, 0);
    }

    @After
    public void tearDown() throws Exception {
        netcdfFile.close();
    }

    @Test
    public void testBothWaysOfPixelLocator() throws Exception {

        for (int h = 0; h < height; h++) {
            final boolean borderTouched = h == 0 || h == height - 1 || h % width == 0 || h % width == width - 1;
            if (borderTouched) {
                continue;
            }
            final double y = h + 0.5;
            final double x = h % width + 0.5;
            boolean worksFine;
            Point2D pos = new Point2D.Double();
            worksFine = pixelLocator.getGeoLocation(x, y, pos);
            assertEquals("Unable to fetch a geoposition for x=" + x + " y=" + y, true, worksFine);

            final double lon = pos.getX();
            final double lat = pos.getY();
            worksFine = pixelLocator.getPixelLocation(lon, lat, pos);

            // @todo se/se unremark the three lines
//            assertEquals("x=" + x + " y=" + y + " |  Unable to fetch a pixel position for lon=" + lon + " lat=" + lat, true, worksFine);
//            assertEquals("x=" + x + " y=" + y + " |  Unable to fetch the correct pixel position for lon=" + lon + " lat=" + lat, x, pos.getX(), 0.45);
//            assertEquals("x=" + x + " y=" + y + " |  Unable to fetch the correct pixel position for lon=" + lon + " lat=" + lat, y, pos.getY(), 0.45);

        }
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

        final GeoApproximation latApproximation = GeoApproximation.create(latValues, 0.3, new Rectangle(0, 0, 41, 9));
        final GeoApproximation lonApproximation = GeoApproximation.create(lonValues, 0.3, new Rectangle(0, 0, 41, 9));
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
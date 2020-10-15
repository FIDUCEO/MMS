/*
 * Copyright (C) 2018 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader.airs;

import static com.bc.fiduceo.reader.airs.AIRS_Constants.AIRS_NUM_CHANELS;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.*;
import org.junit.runner.*;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(IOTestRunner.class)
public class AIRS_L1B_Reader_IO_Test {

    private final String FirstProductOfTheDay = "AIRS.2010.01.07.001.L1B.AIRS_Rad.v5.0.0.0.G10007112420.hdf";
    private final String CloseToFirstProductOfTheDay = "AIRS.2010.01.07.005.L1B.AIRS_Rad.v5.0.0.0.G10007113615.hdf";
    private final String CloseToLastProductOfTheDay = "AIRS.2010.01.07.236.L1B.AIRS_Rad.v5.0.0.0.G10008113144.hdf";
    private final String LastProductOfTheDay = "AIRS.2010.01.07.240.L1B.AIRS_Rad.v5.0.0.0.G10008113232.hdf";
    private final String[] expVariableNames = new String[]{
            "Latitude", "Longitude", "Time",
            "scanang",
            "satheight", "satroll", "satpitch", "satyaw", "satgeoqa",
            "glintgeoqa", "moongeoqa", "ftptgeoqa", "zengeoqa", "demgeoqa",
            "nadirTAI",
            "sat_lat", "sat_lon",
            "scan_node_type",
            "satzen", "satazi", "solzen", "solazi",
            "glintlat", "glintlon", "sun_glint_distance",
            "topog", "topog_err",
            "landFrac", "landFrac_err",
            "state",
            "CalScanSummary",
            "spaceview_selection",
            "Rdiff_swindow", "Rdiff_lwindow",
            "SceneInhomogeneous",
            "dust_flag", "dust_score",
            "spectral_clear_indicator",
            "BT_diff_SO2",
            "OpMode",
            "EDCBOARD",
            };
    private final Interval interval_1_1 = new Interval(1, 1);
    private Path airsDataPath;
    private DateFormat dateFormat;
    private AIRS_L1B_Reader airsL1bReader;

    @Before
    public void setUp() throws IOException {
        airsDataPath = TestUtil.getTestDataDirectory().toPath()
                .resolve("airs-aq").resolve("v5.0.0.0").resolve("2010").resolve("007");

        dateFormat = ProductData.UTC.createDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));
        airsL1bReader = new AIRS_L1B_Reader(readerContext);
    }

    @After
    public void tearDown() throws Exception {
        airsL1bReader.close();
    }

    @Test
    public void testReadAcquisitionInfo_FirstProductOfTheDay() throws IOException, ParseException, InvalidRangeException {

        airsL1bReader.open(airsDataPath.resolve(FirstProductOfTheDay).toFile());

        final AcquisitionInfo acquisitionInfo = airsL1bReader.read();
        assertNotNull(acquisitionInfo);

        final String sensingStart = "2010-01-07 00:05:24.000";
        final String sensingStop = "2010-01-07 00:11:23.999";
        final NodeType nodeType = NodeType.ASCENDING;
        final int numBoundingCoordinates = 25;
        final IndexedPoint[] boundingPoints = {
                new IndexedPoint(0, -160.58036121827877, -41.004036486781956),
                new IndexedPoint(4, -161.05254137389082, -19.224336964357146),
                new IndexedPoint(9, -153.6135499287492, -18.145799200699678),
                new IndexedPoint(14, -142.23738370588939, -32.68196330611892),
                new IndexedPoint(19, -149.31553953668907, -39.80039622557189),
                new IndexedPoint(24, -160.58036121827877, -41.004036486781956)
        };
        final int timeAxisDuration = 359999;
        final int numTimeAxisPoints = 4;
        final IndexedPoint[] expTimeAxisPoints = {
                new IndexedPoint(0, -149.94851774135594, -39.900237141024554),
                new IndexedPoint(1, -152.26983032923866, -31.9310548433196),
                new IndexedPoint(2, -154.30902622054185, -23.925098294196466),
                new IndexedPoint(3, -155.5917138838205, -18.46488718833132)
        };

        assertAquisitionInfo(acquisitionInfo,
                             sensingStart, sensingStop, nodeType,
                             numBoundingCoordinates, boundingPoints,
                             timeAxisDuration, numTimeAxisPoints, expTimeAxisPoints);

        final Dimension productSize = airsL1bReader.getProductSize();
        assertNotNull(productSize);
        assertEquals(90, productSize.getNx());
        assertEquals(135, productSize.getNy());

        final PixelLocator pixelLocator = airsL1bReader.getPixelLocator();
        assertNotNull(pixelLocator);
        final Point2D geoLocation = pixelLocator.getGeoLocation(10.5, 12.5, new Point2D.Double());
        final Point2D[] pixelLocations = pixelLocator.getPixelLocation(geoLocation.getX(), geoLocation.getY());
        assertNotNull(pixelLocations);
        assertEquals(1, pixelLocations.length);
        final Point2D pixelLocation = pixelLocations[0];
        assertNotNull(pixelLocation);
        assertEquals(10.5, pixelLocation.getX(), 0.05);
        assertEquals(12.5, pixelLocation.getY(), 0.05);

        final TimeLocator timeLocator = airsL1bReader.getTimeLocator();
        assertNotNull(timeLocator);
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(0, 0)).getTime(), timeLocator.getTimeFor(0, 0));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(1, 0)).getTime(), timeLocator.getTimeFor(1, 0));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(44, 61)).getTime(), timeLocator.getTimeFor(44, 61));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(47, 68)).getTime(), timeLocator.getTimeFor(47, 68));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(89, 133)).getTime(), timeLocator.getTimeFor(89, 133));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(89, 134)).getTime(), timeLocator.getTimeFor(89, 134));

        assertGetVariables(airsL1bReader);
    }

    @Test
    public void testReadAcquisitionInfo_CloseToFirstProductOfTheDay() throws IOException, ParseException, InvalidRangeException {
        airsL1bReader.open(airsDataPath.resolve(CloseToFirstProductOfTheDay).toFile());

        final AcquisitionInfo acquisitionInfo = airsL1bReader.read();
        assertNotNull(acquisitionInfo);

        final String sensingStart = "2010-01-07 00:29:24.000";
        final String sensingStop = "2010-01-07 00:35:23.999";
        final NodeType nodeType = NodeType.ASCENDING;

        final int numBoundingCoordinates = 25;
        final IndexedPoint[] boundingPoints = {
                new IndexedPoint(0, 177.3923460702796, 44.49116274166107),
                new IndexedPoint(4, 163.83469904779346, 65.15509282631304),
                new IndexedPoint(9, -179.79775976537454, 68.24818312168115),
                new IndexedPoint(14, -160.87279750459268, 53.32304100688955),
                new IndexedPoint(19, -170.88670253253346, 46.86273350972246),
                new IndexedPoint(24, 177.3923460702796, 44.49116274166107)
        };

        final int timeAxisDuration = 359999;
        final int numTimeAxisPoints = 4;
        final IndexedPoint[] expTimeAxisPoints = {
                new IndexedPoint(0, -171.58591251481585, 46.75805806021887),
                new IndexedPoint(1, -175.01312278072135, 54.646004393037586),
                new IndexedPoint(2, -179.81070704199072, 62.4201870893187),
                new IndexedPoint(3, 175.4312904888272, 67.59035616165096)
        };

        assertAquisitionInfo(acquisitionInfo,
                             sensingStart, sensingStop, nodeType,
                             numBoundingCoordinates, boundingPoints,
                             timeAxisDuration, numTimeAxisPoints, expTimeAxisPoints);

        final Dimension productSize = airsL1bReader.getProductSize();
        assertNotNull(productSize);
        assertEquals(90, productSize.getNx());
        assertEquals(135, productSize.getNy());

        final PixelLocator pixelLocator = airsL1bReader.getPixelLocator();
        assertNotNull(pixelLocator);
        final Point2D geoLocation = pixelLocator.getGeoLocation(10.5, 12.5, new Point2D.Double());
        final Point2D[] pixelLocations = pixelLocator.getPixelLocation(geoLocation.getX(), geoLocation.getY());
        assertNotNull(pixelLocations);
        assertEquals(1, pixelLocations.length);
        final Point2D pixelLocation = pixelLocations[0];
        assertNotNull(pixelLocation);
        assertEquals(10.5, pixelLocation.getX(), 0.05);
        assertEquals(12.5, pixelLocation.getY(), 0.05);

        final TimeLocator timeLocator = airsL1bReader.getTimeLocator();
        assertNotNull(timeLocator);
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(0, 0)).getTime(), timeLocator.getTimeFor(0, 0));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(1, 0)).getTime(), timeLocator.getTimeFor(1, 0));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(44, 61)).getTime(), timeLocator.getTimeFor(44, 61));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(47, 68)).getTime(), timeLocator.getTimeFor(47, 68));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(89, 133)).getTime(), timeLocator.getTimeFor(89, 133));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(89, 134)).getTime(), timeLocator.getTimeFor(89, 134));

        assertGetVariables(airsL1bReader);
    }

    @Test
    public void testReadAcquisitionInfo_CloseToLastProductOfTheDay() throws IOException, ParseException, InvalidRangeException {
        airsL1bReader.open(airsDataPath.resolve(CloseToLastProductOfTheDay).toFile());

        final AcquisitionInfo acquisitionInfo = airsL1bReader.read();
        assertNotNull(acquisitionInfo);

        final String sensingStart = "2010-01-07 23:35:24.000";
        final String sensingStop = "2010-01-07 23:41:23.999";
        final NodeType nodeType = NodeType.ASCENDING;
        final int numBoundingCoordinates = 25;
        final IndexedPoint[] boundingPoints = {
                new IndexedPoint(0, -172.27133582792965, 49.9423509168333),
                new IndexedPoint(4, 168.11832902638176, 69.86166126288212),
                new IndexedPoint(9, -172.51500576157054, 73.7847658106488),
                new IndexedPoint(14, -147.8540238995572, 59.16644183567005),
                new IndexedPoint(19, -159.29997125412646, 52.646366874661226),
                new IndexedPoint(24, -172.27133582792965, 49.9423509168333)
        };
        final int timeAxisDuration = 359999;
        final int numTimeAxisPoints = 4;
        final IndexedPoint[] expTimeAxisPoints = {
                new IndexedPoint(0, -160.08426185741553, 52.52941926262613),
                new IndexedPoint(1, -164.4162005056497, 60.341837662286686),
                new IndexedPoint(2, -171.0924637408649, 67.96965235826016),
                new IndexedPoint(3, -178.49355932258163, 72.93192501547036)
        };


        assertAquisitionInfo(acquisitionInfo,
                             sensingStart, sensingStop, nodeType,
                             numBoundingCoordinates, boundingPoints,
                             timeAxisDuration, numTimeAxisPoints, expTimeAxisPoints);

        final Dimension productSize = airsL1bReader.getProductSize();
        assertNotNull(productSize);
        assertEquals(90, productSize.getNx());
        assertEquals(135, productSize.getNy());

        final PixelLocator pixelLocator = airsL1bReader.getPixelLocator();
        assertNotNull(pixelLocator);
        final Point2D geoLocation = pixelLocator.getGeoLocation(10.5, 12.5, new Point2D.Double());
        final Point2D[] pixelLocations = pixelLocator.getPixelLocation(geoLocation.getX(), geoLocation.getY());
        assertNotNull(pixelLocations);
        assertEquals(1, pixelLocations.length);
        final Point2D pixelLocation = pixelLocations[0];
        assertNotNull(pixelLocation);
        assertEquals(10.5, pixelLocation.getX(), 0.05);
        assertEquals(12.5, pixelLocation.getY(), 0.05);

        final TimeLocator timeLocator = airsL1bReader.getTimeLocator();
        assertNotNull(timeLocator);
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(0, 0)).getTime(), timeLocator.getTimeFor(0, 0));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(1, 0)).getTime(), timeLocator.getTimeFor(1, 0));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(44, 61)).getTime(), timeLocator.getTimeFor(44, 61));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(47, 68)).getTime(), timeLocator.getTimeFor(47, 68));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(89, 133)).getTime(), timeLocator.getTimeFor(89, 133));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(89, 134)).getTime(), timeLocator.getTimeFor(89, 134));

        assertGetVariables(airsL1bReader);
    }

    @Test
    public void testReadAcquisitionInfo_LastProductOfTheDay() throws IOException, ParseException, InvalidRangeException {
        airsL1bReader.open(airsDataPath.resolve(LastProductOfTheDay).toFile());

        final AcquisitionInfo acquisitionInfo = airsL1bReader.read();
        assertNotNull(acquisitionInfo);

        final String sensingStart = "2010-01-07 23:59:24.000";
        final String sensingStop = "2010-01-08 00:05:23.999";
        final NodeType nodeType = NodeType.DESCENDING;
        final int numBoundingCoordinates = 25;
        final IndexedPoint[] boundingPoints = {
                new IndexedPoint(0, 41.13878818856406, 37.23502459334875),
                new IndexedPoint(4, 31.012294490703606, 16.774711614878182),
                new IndexedPoint(9, 23.678166447899063, 17.83152001447589),
                new IndexedPoint(14, 20.165963444715054, 34.569152009864204),
                new IndexedPoint(19, 30.580425601541442, 39.1540590819922),
                new IndexedPoint(24, 41.13878818856406, 37.23502459334875)
        };
        final int timeAxisDuration = 359999;
        final int numTimeAxisPoints = 4;
        final IndexedPoint[] expTimeAxisPoints = {
                new IndexedPoint(0, 31.19984799556, 39.07174966950319),
                new IndexedPoint(1, 28.925058120604955, 31.07533396419192),
                new IndexedPoint(2, 26.915686824838257, 23.04849053661578),
                new IndexedPoint(3, 25.646540679335928, 17.577617949148454)
        };


        assertAquisitionInfo(acquisitionInfo,
                             sensingStart, sensingStop, nodeType,
                             numBoundingCoordinates, boundingPoints,
                             timeAxisDuration, numTimeAxisPoints, expTimeAxisPoints);

        final Dimension productSize = airsL1bReader.getProductSize();
        assertNotNull(productSize);
        assertEquals(90, productSize.getNx());
        assertEquals(135, productSize.getNy());

        final PixelLocator pixelLocator = airsL1bReader.getPixelLocator();
        assertNotNull(pixelLocator);
        final Point2D geoLocation = pixelLocator.getGeoLocation(10.5, 12.5, new Point2D.Double());
        final Point2D[] pixelLocations = pixelLocator.getPixelLocation(geoLocation.getX(), geoLocation.getY());
        assertNotNull(pixelLocations);
        assertEquals(1, pixelLocations.length);
        final Point2D pixelLocation = pixelLocations[0];
        assertNotNull(pixelLocation);
        assertEquals(10.5, pixelLocation.getX(), 0.05);
        assertEquals(12.5, pixelLocation.getY(), 0.05);

        final TimeLocator timeLocator = airsL1bReader.getTimeLocator();
        assertNotNull(timeLocator);
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(0, 0)).getTime(), timeLocator.getTimeFor(0, 0));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(1, 0)).getTime(), timeLocator.getTimeFor(1, 0));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(44, 61)).getTime(), timeLocator.getTimeFor(44, 61));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(47, 68)).getTime(), timeLocator.getTimeFor(47, 68));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(89, 133)).getTime(), timeLocator.getTimeFor(89, 133));
        assertEquals(TimeUtils.tai1993ToUtc(readTai93(89, 134)).getTime(), timeLocator.getTimeFor(89, 134));

        assertGetVariables(airsL1bReader);
    }

    @Test
    public void testReadRawAndScaledFrom1dVariable() throws IOException, InvalidRangeException {
        airsL1bReader.open(airsDataPath.resolve(FirstProductOfTheDay).toFile());
        final Array arrayRaw = airsL1bReader.readRaw(87, 1, new Interval(5, 5), "satheight");
        final Array arrayScaled = airsL1bReader.readScaled(87, 1, new Interval(5, 5), "satheight");

        assertNotNull(arrayRaw);
        assertTrue(arrayRaw instanceof ArrayFloat.D2);
        final ArrayFloat.D2 d2Raw = (ArrayFloat.D2) arrayRaw;
        float[] fRaw = (float[]) d2Raw.get1DJavaArray(Float.class);

        assertNotNull(arrayScaled);
        assertTrue(arrayScaled instanceof ArrayFloat.D2);
        final ArrayFloat.D2 d2Scaled = (ArrayFloat.D2) arrayScaled;
        float[] fScaled = (float[]) d2Scaled.get1DJavaArray(Float.class);

        final float[] expectedRawAndScaled = new float[]{
                -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                718.0556f, 718.0556f, 718.0556f, 718.0556f, 718.0556f,
                717.98834f, 717.98834f, 717.98834f, 717.98834f, 717.98834f,
                717.92114f, 717.92114f, 717.92114f, 717.92114f, 717.92114f,
                717.8539f, 717.8539f, 717.8539f, 717.8539f, 717.8539f,
                };

        assertArrayEquals(expectedRawAndScaled, fRaw, 1e-8f);
        assertArrayEquals(expectedRawAndScaled, fScaled, 1e-8f);
    }

    @Test
    public void testReadFrom2dVariable() throws IOException, InvalidRangeException {
        airsL1bReader.open(airsDataPath.resolve(FirstProductOfTheDay).toFile());
        final Array array = airsL1bReader.readRaw(1, 1, new Interval(5, 5), "BT_diff_SO2");
        assertNotNull(array);
        assertTrue(array instanceof ArrayFloat.D2);
        final ArrayFloat.D2 d2 = (ArrayFloat.D2) array;
        float[] f = (float[]) d2.get1DJavaArray(Float.class);
        final float[] expected = new float[]{
                -9999.0f, -9999.0f, -9999.0f, -9999.0f, -9999.0f,
                -9999.0f, -2.1013947f, -1.8912964f, -1.5602264f, -1.8875885f,
                -9999.0f, -1.9546509f, -1.8443756f, -1.8810272f, -1.7983246f,
                -9999.0f, -2.3438263f, -2.243332f, -2.0015259f, -1.9786682f,
                -9999.0f, -2.1965332f, -2.193283f, -1.809494f, -1.878479f
        };
        assertArrayEquals(expected, f, 1e-8f);
    }

    @Test
    public void readAcquisitionTime() throws Exception {
        //preparation
        airsL1bReader.open(airsDataPath.resolve(FirstProductOfTheDay).toFile());
        final int x = 1;
        final int y = 1;
        final Interval interval = new Interval(5, 5);
        final Array pt = airsL1bReader.readRaw(x, y, interval, "Time");
        final int[] expectedShape = {5, 5};
        assertThat(pt.getShape(), is(equalTo(expectedShape)));
        final List<Variable> variables = airsL1bReader.getVariables();
        Number fillValue = null;
        for (Variable variable : variables) {
            if (variable.getShortName().equals("Time")) {
                fillValue = variable.findAttribute(NetCDFUtils.CF_FILL_VALUE_NAME).getNumericValue();
            }
        }
        assertThat(fillValue, is(notNullValue()));

        //execution
        final ArrayInt.D2 at = airsL1bReader.readAcquisitionTime(x, y, interval);

        //verification
        assertThat(at.getDataType(), is(equalTo(DataType.INT)));
        assertThat(at.getShape(), is(equalTo(expectedShape)));
        int fillValueCount = 0;
        for (int i = 0; i < at.getSize(); i++) {
            final double ptVal = pt.getDouble(i);
            final int expected;
            if (fillValue.equals(ptVal)) {
                expected = NetCDFUtils.getDefaultFillValue(int.class).intValue();
                fillValueCount++;
            } else {
                expected = (int) Math.round(TimeUtils.tai1993ToUtcInstantSeconds(ptVal));
            }
            assertThat("Loop number " + i, at.getInt(i), is(expected));
        }
        assertThat(fillValueCount, is(9));
    }

    @Test
    public void testReadSpectrum_radiance() throws IOException, InvalidRangeException {
        //preparation
        airsL1bReader.open(airsDataPath.resolve(FirstProductOfTheDay).toFile());

        //execution
        final Array radiances = airsL1bReader.readSpectrum(18, 8, new int[]{5, 5, AIRS_NUM_CHANELS}, "radiances");

        //verification
        assertNotNull(radiances);
        assertArrayEquals(new int[]{5, 5, AIRS_NUM_CHANELS}, radiances.getShape());
        final HashMap<Integer, float[]> expectations = new HashMap<>();
        expectations.put(0, new float[]{
                51.0f, 50.0f, 50.75f, 48.75f, 51.0f,
                51.5f, 50.5f, 50.75f, 49.5f, 50.25f,
                50.25f, 50.0f, 50.5f, 50.75f, 50.0f,
                52.25f, 50.75f, 51.25f, 50.0f, 50.0f,
                50.75f, 49.5f, 50.5f, 52.25f, 51.0f

        });
        expectations.put(1000, new float[]{
                73.96875f, 74.03125f, 74.3125f, 74.4375f, 74.375f,
                74.09375f, 74.25f, 74.03125f, 74.03125f, 73.8125f,
                73.9375f, 74.15625f, 73.9375f, 73.65625f, 73.78125f,
                73.5f, 73.8125f, 73.5625f, 72.75f, 72.21875f,
                73.5f, 73.40625f, 71.5f, 70.28125f, 70.8125f

        });
        expectations.put(AIRS_NUM_CHANELS -1, new float[]{
                0.5551758f, 0.5805664f, 0.58251953f, 0.5805664f, 0.5839844f,
                0.5449219f, 0.56347656f, 0.5722656f, 0.57910156f, 0.5834961f,
                0.5410156f, 0.5498047f, 0.55615234f, 0.56347656f, 0.578125f,
                0.546875f, 0.5498047f, 0.57714844f, 0.6381836f, 0.66796875f,
                0.5317383f, 0.5654297f, 0.70996094f, 0.765625f, 0.7265625f

        });
        for (Map.Entry<Integer, float[]> entry : expectations.entrySet()) {
            int channel = entry.getKey();
            final float[] expected = entry.getValue();
            assertArrayEquals("Wrong values at channel: " + channel, expected, (float[]) radiances.section(new int[]{0, 0, channel}, new int[]{5, 5, 1}).copy().getStorage(), 0.000001f);
        }
    }

    private void assertAquisitionInfo(AcquisitionInfo acquisitionInfo, String sensingStart, String sensingStop, NodeType nodeType, int numBoundingCoordinates, IndexedPoint[] boundingPoints, int timeAxisDuration, int numTimeAxisPoints, IndexedPoint[] expTimeAxisPoints) throws ParseException {
        assertCorrectDate(sensingStart, acquisitionInfo.getSensingStart());
        assertCorrectDate(sensingStop, acquisitionInfo.getSensingStop());
        assertEquals(nodeType, acquisitionInfo.getNodeType());

        final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
        assertNotNull(boundingGeometry);
        assertEquals("class com.bc.fiduceo.geometry.s2.BcS2Polygon", boundingGeometry.getClass().toString());

        final Point[] coordinates = boundingGeometry.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(numBoundingCoordinates, coordinates.length);
        assertCoordinates(boundingPoints, coordinates);

        final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
        assertNotNull(timeAxes);
        assertEquals(1, timeAxes.length);
        final TimeAxis timeAxis = timeAxes[0];
        assertNotNull(timeAxis);
        assertEquals(sensingStart, dateFormat.format(timeAxis.getStartTime()));
        assertEquals(sensingStop, dateFormat.format(timeAxis.getEndTime()));
        assertEquals(timeAxisDuration, timeAxis.getDurationInMillis());
        final Geometry axisGeometry = timeAxis.getGeometry();
        assertNotNull(axisGeometry);
        final Point[] axisCoordinates = axisGeometry.getCoordinates();
        assertNotNull(axisCoordinates);
        assertEquals(numTimeAxisPoints, axisCoordinates.length);
        assertCoordinates(expTimeAxisPoints, axisCoordinates);
    }

    private void assertCorrectDate(String expected, Date date) {
        assertNotNull(date);
        assertEquals(expected, dateFormat.format(date));
    }

    private void assertCoordinates(IndexedPoint[] expected, Point[] actual) {
        for (IndexedPoint ip : expected) {
            final String msg = "error at index " + ip.idx;
            final Point point = actual[ip.idx];
            assertEquals("Lon " + msg, ip.lon, point.getLon(), 1e-8);
            assertEquals("Lat " + msg, ip.lat, point.getLat(), 1e-8);
        }
    }

    private void assertCoordinate(String msg, double expectedLon, double expectedLat, Point coordinate) {
        assertEquals(expectedLon, coordinate.getLon(), 1e-8);
        assertEquals(expectedLat, coordinate.getLat(), 1e-8);
    }

    private void assertGetVariables(AIRS_L1B_Reader reader) throws InvalidRangeException {
        final List<Variable> variables = reader.getVariables();
        assertEquals(expVariableNames.length, variables.size());
        for (int i = 0; i < variables.size(); i++) {
            Variable variable = variables.get(i);
            final String name = variable.getShortName();
            assertEquals("Assertion error at pos: " + i, expVariableNames[i], name);
        }
    }

    private double readTai93(int x, int y) throws IOException, InvalidRangeException {
        final Array array = airsL1bReader.readRaw(x, y, interval_1_1, "Time");
        return array.getDouble(array.getIndex());
    }

    static class IndexedPoint {

        int idx;
        double lon, lat;

        public IndexedPoint(int idx, double lon, double lat) {
            this.idx = idx;
            this.lon = lon;
            this.lat = lat;
        }
    }
}

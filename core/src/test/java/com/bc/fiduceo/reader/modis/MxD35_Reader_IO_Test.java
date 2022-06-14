/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.archive.Archive;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.bc.fiduceo.util.NetCDFUtils.CF_ADD_OFFSET_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_FLAG_MASKS_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_FLAG_MEANINGS_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_FLAG_VALUES_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_SCALE_FACTOR_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_VALID_RANGE_NAME;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class MxD35_Reader_IO_Test {

    private static MxD35_Reader reader;
    private static GeometryFactory geometryFactory;

    @Before
    public void setUp() throws IOException {
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Archive archive = TestUtil.getArchive();

        ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(geometryFactory);
        readerContext.setArchive(archive);

        reader = new MxD35_Reader(readerContext);
    }

    @After
    public void tearDown() throws Exception {
        reader.close();
    }

    @Test
    public void testReadAcquisitionInfo_Terra() throws IOException {
        reader.open(getTerraFile());

        final AcquisitionInfo acquisitionInfo = reader.read();
        assertNotNull(acquisitionInfo);

        final Date sensingStart = acquisitionInfo.getSensingStart();
        TestUtil.assertCorrectUTCDate(2022, 4, 25, 11, 25, 0, 0, sensingStart);

        final Date sensingStop = acquisitionInfo.getSensingStop();
        TestUtil.assertCorrectUTCDate(2022, 4, 25, 11, 30, 0, 0, sensingStop);

        final NodeType nodeType = acquisitionInfo.getNodeType();
        assertEquals(NodeType.UNDEFINED, nodeType);

        final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
        assertNotNull(boundingGeometry);
        assertTrue(boundingGeometry instanceof Polygon);
        final Point[] coordinates = boundingGeometry.getCoordinates();
        assertEquals(31, coordinates.length);
        assertEquals(-22.925876617431644, coordinates[0].getLon(), 1e-8);
        assertEquals(53.60942459106445, coordinates[0].getLat(), 1e-8);

        assertEquals(10.10037326812744, coordinates[24].getLon(), 1e-8);
        assertEquals(48.87732315063477, coordinates[24].getLat(), 1e-8);

        final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
        assertEquals(1, timeAxes.length);
        Point[] locations = coordinates[0].getCoordinates();
        Date time = timeAxes[0].getTime(locations[0]);
        TestUtil.assertCorrectUTCDate(2022, 4, 25, 11, 25, 6, 233, time);

        locations = coordinates[9].getCoordinates();
        time = timeAxes[0].getTime(locations[0]);
        TestUtil.assertCorrectUTCDate(2022, 4, 25, 11, 30, 0, 0, time);
    }

    @Test
    public void testReadAcquisitionInfo_Aqua() throws IOException {
        reader.open(getAquaFile());
        final AcquisitionInfo acquisitionInfo = reader.read();
        assertNotNull(acquisitionInfo);

        final Date sensingStart = acquisitionInfo.getSensingStart();
        TestUtil.assertCorrectUTCDate(2022, 4, 25, 11, 35, 0, 0, sensingStart);

        final Date sensingStop = acquisitionInfo.getSensingStop();
        TestUtil.assertCorrectUTCDate(2022, 4, 25, 11, 40, 0, 0, sensingStop);

        final NodeType nodeType = acquisitionInfo.getNodeType();
        assertEquals(NodeType.UNDEFINED, nodeType);

        final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
        assertNotNull(boundingGeometry);
        assertTrue(boundingGeometry instanceof Polygon);
        final Point[] coordinates = boundingGeometry.getCoordinates();
        assertEquals(31, coordinates.length);
        assertEquals(37.362316131591804, coordinates[0].getLon(), 1e-8);
        assertEquals(43.49676513671875, coordinates[0].getLat(), 1e-8);

        assertEquals(9.78400707244873, coordinates[24].getLon(), 1e-8);
        assertEquals(39.534942626953125, coordinates[24].getLat(), 1e-8);

        final Dimension psze = reader.getProductSize();
        final PixelLocator pixelLocator = reader.getPixelLocator();
        final Point2D centerLoc = pixelLocator.getGeoLocation(psze.getNx() / 2.0, psze.getNy() / 2.0, null);
        final Geometry intersection = boundingGeometry.getIntersection(geometryFactory.createPoint(centerLoc.getX(), centerLoc.getY()));
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());

        final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
        assertEquals(1, timeAxes.length);
        Point[] locations = coordinates[0].getCoordinates();
        Date time = timeAxes[0].getTime(locations[0]);
        TestUtil.assertCorrectUTCDate(2022, 4, 25, 11, 35, 0, 0, time);

        locations = coordinates[9].getCoordinates();
        time = timeAxes[0].getTime(locations[0]);
        TestUtil.assertCorrectUTCDate(2022, 4, 25, 11, 39, 54, 445, time);
    }

    @Test
    public void testGetProductSize_Terra() throws IOException {
        reader.open(getTerraFile());
        final Dimension productSize = reader.getProductSize();
        assertEquals(1354, productSize.getNx());
        assertEquals(2030, productSize.getNy());
    }

    @Test
    public void testGetProductSize_Aqua() throws IOException {
        reader.open(getAquaFile());
        final Dimension productSize = reader.getProductSize();
        assertEquals(1354, productSize.getNx());
        assertEquals(2030, productSize.getNy());
    }

    @Test
    public void testGetTimeLocator_Terra() throws IOException {
        reader.open(getTerraFile());
        final TimeLocator timeLocator = reader.getTimeLocator();
        assertEquals(1650885874131L, timeLocator.getTimeFor(0, 0));
        assertEquals(1650885874131L, timeLocator.getTimeFor(269, 0));

        assertEquals(1650885903674L, timeLocator.getTimeFor(76, 203));
        assertEquals(1650885933216L, timeLocator.getTimeFor(145, 405));
    }

    @Test
    public void testGetTimeLocator_Aqua() throws IOException {
        reader.open(getAquaFile());
        final TimeLocator timeLocator = reader.getTimeLocator();
        assertEquals(1650886473917L, timeLocator.getTimeFor(4, 0));
        assertEquals(1650886473917L, timeLocator.getTimeFor(223, 0));

        assertEquals(1650886503460L, timeLocator.getTimeFor(21, 202));
        assertEquals(1650886533002L, timeLocator.getTimeFor(147, 405));
    }

    @Test
    public void testGetVariables_Terra() throws IOException, InvalidRangeException {
        reader.open(getTerraFile());
        checkVariables(reader);
    }

    @Test
    public void testGetVariables_Terra_Mod03FileNotAvailable() throws IOException, InvalidRangeException {
        reader.packageLocalPropertyForUnitLevelTestsOnly_toSimulate_correspondingMod03FileNotAvailable = true;
        reader.open(getTerraFile());
        checkVariables(reader);
    }

    @Test
    public void testGetVariables_Aqua() throws IOException, InvalidRangeException {
        reader.open(getAquaFile());
        checkVariables(reader);
    }

    @Test
    public void testGetVariables_Aqua_Mod03FileNotAvailable() throws IOException, InvalidRangeException {
        reader.packageLocalPropertyForUnitLevelTestsOnly_toSimulate_correspondingMod03FileNotAvailable = true;
        reader.open(getAquaFile());
        checkVariables(reader);
    }

    @Test
    public void testReadAcquisitionTime_Terra() throws IOException {
        reader.open(getTerraFile());
        final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(34, 110, new Interval(2, 4));
        assertEquals(8, acquisitionTime.getSize());

        // one scan
        NCTestUtils.assertValueAt(1650885888, 0, 0, acquisitionTime);
        NCTestUtils.assertValueAt(1650885888, 1, 0, acquisitionTime);
        NCTestUtils.assertValueAt(1650885888, 0, 1, acquisitionTime);
        NCTestUtils.assertValueAt(1650885888, 1, 1, acquisitionTime);

        // next scan
        NCTestUtils.assertValueAt(1650885890, 0, 2, acquisitionTime);
        NCTestUtils.assertValueAt(1650885890, 1, 2, acquisitionTime);
        NCTestUtils.assertValueAt(1650885890, 0, 3, acquisitionTime);
        NCTestUtils.assertValueAt(1650885890, 1, 3, acquisitionTime);
    }

    @Test
    public void testReadAcquisitionTime_Terra_outside_top() throws IOException {
        // fillValue see {@link com.bc.fiduceo.reader.Reader#readAcquisitionTime(int, int, Interval)}
        final int fillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();
        reader.open(getTerraFile());

        final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(36, 0, new Interval(2, 3));
        assertEquals(6, acquisitionTime.getSize());

        // outside line
        NCTestUtils.assertValueAt(fillValue, 0, 0, acquisitionTime);
        NCTestUtils.assertValueAt(fillValue, 1, 0, acquisitionTime);

        // inside lines
        NCTestUtils.assertValueAt(1650885874, 0, 1, acquisitionTime);
        NCTestUtils.assertValueAt(1650885874, 1, 1, acquisitionTime);
        NCTestUtils.assertValueAt(1650885874, 0, 2, acquisitionTime);
        NCTestUtils.assertValueAt(1650885874, 1, 2, acquisitionTime);
    }

    @Test
    public void testReadAcquisitionTime_Terra_outside_bottom() throws IOException {
        // fillValue see {@link com.bc.fiduceo.reader.Reader#readAcquisitionTime(int, int, Interval)}
        final int fillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();
        reader.open(getTerraFile());

        final int ny = reader.getProductSize().getNy();

        final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(36, ny - 1, new Interval(2, 3));
        assertEquals(6, acquisitionTime.getSize());

        // inside lines
        NCTestUtils.assertValueAt(725846373, 0, 0, acquisitionTime);
        NCTestUtils.assertValueAt(725846373, 1, 0, acquisitionTime);
        NCTestUtils.assertValueAt(725846373, 0, 1, acquisitionTime);
        NCTestUtils.assertValueAt(725846373, 1, 1, acquisitionTime);

        // outside line
        NCTestUtils.assertValueAt(fillValue, 0, 2, acquisitionTime);
        NCTestUtils.assertValueAt(fillValue, 1, 2, acquisitionTime);
    }

    @Test
    public void testReadAcquisitionTime_Aqua() throws IOException {
        reader.open(getAquaFile());
        final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(36, 216, new Interval(2, 15));
        assertEquals(30, acquisitionTime.getSize());

        // first scan
        NCTestUtils.assertValueAt(1650886503, 0, 0, acquisitionTime);
        NCTestUtils.assertValueAt(1650886503, 1, 0, acquisitionTime);

        // next scan
        NCTestUtils.assertValueAt(1650886504, 0, 1, acquisitionTime);
        NCTestUtils.assertValueAt(1650886504, 1, 1, acquisitionTime);
        NCTestUtils.assertValueAt(1650886504, 0, 10, acquisitionTime);
        NCTestUtils.assertValueAt(1650886504, 1, 10, acquisitionTime);

        // next scan
        NCTestUtils.assertValueAt(1650886506, 0, 11, acquisitionTime);
        NCTestUtils.assertValueAt(1650886506, 1, 11, acquisitionTime);
        NCTestUtils.assertValueAt(1650886506, 0, 14, acquisitionTime);
        NCTestUtils.assertValueAt(1650886506, 1, 14, acquisitionTime);
    }

    @Test
    public void testReadAcquisitionTime_Aqua_outside_top() throws IOException {
        // fillValue see {@link com.bc.fiduceo.reader.Reader#readAcquisitionTime(int, int, Interval)}
        final int fillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();
        reader.open(getAquaFile());

        final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(36, 5, new Interval(2, 13));
        assertEquals(26, acquisitionTime.getSize());

        // first scan outside
        NCTestUtils.assertValueAt(fillValue, 0, 0, acquisitionTime);
        NCTestUtils.assertValueAt(fillValue, 1, 0, acquisitionTime);

        // first scan inside
        NCTestUtils.assertValueAt(1650886473, 0, 1, acquisitionTime);
        NCTestUtils.assertValueAt(1650886473, 1, 1, acquisitionTime);
        NCTestUtils.assertValueAt(1650886473, 0, 10, acquisitionTime);
        NCTestUtils.assertValueAt(1650886473, 1, 10, acquisitionTime);

        // next scan
        NCTestUtils.assertValueAt(1650886475, 0, 11, acquisitionTime);
        NCTestUtils.assertValueAt(1650886475, 1, 11, acquisitionTime);
        NCTestUtils.assertValueAt(1650886475, 0, 12, acquisitionTime);
        NCTestUtils.assertValueAt(1650886475, 1, 12, acquisitionTime);
    }

    @Test
    public void testReadAcquisitionTime_Aqua_outside_bottom() throws IOException {
        // fillValue see {@link com.bc.fiduceo.reader.Reader#readAcquisitionTime(int, int, Interval)}
        final int fillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();
        reader.open(getAquaFile());

        final int ny = reader.getProductSize().getNy();

        final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(36, ny - 2, new Interval(5, 5));
        assertEquals(25, acquisitionTime.getSize());

        System.out.println(acquisitionTime);
        // first line
        NCTestUtils.assertValueAt(725846373, 0, 0, acquisitionTime);
        NCTestUtils.assertValueAt(725846373, 1, 0, acquisitionTime);

        // second line
        NCTestUtils.assertValueAt(725846373, 1, 1, acquisitionTime);
        NCTestUtils.assertValueAt(725846373, 2, 1, acquisitionTime);

        // third line
        NCTestUtils.assertValueAt(725846373, 2, 2, acquisitionTime);
        NCTestUtils.assertValueAt(725846373, 3, 2, acquisitionTime);

        // fourth line
        NCTestUtils.assertValueAt(725846373, 3, 3, acquisitionTime);
        NCTestUtils.assertValueAt(725846373, 4, 3, acquisitionTime);

        // fifth line
        NCTestUtils.assertValueAt(fillValue, 3, 4, acquisitionTime);
        NCTestUtils.assertValueAt(fillValue, 4, 4, acquisitionTime);
    }

    @Test
    public void testReadCloudMaskSPI_unscaled_and_scaled_Aqua() throws IOException, InvalidRangeException {
        //preparation
        final int centerX = 174;
        final int centerY = 389;
        final Interval interval = new Interval(3, 3);
        reader.open(getAquaFile());

        //execution
        Array arrayR = reader.readRaw(centerX, centerY, interval, "Cloud_Mask_SPI_0");
        Array arrayS = reader.readScaled(centerX, centerY, interval, "Cloud_Mask_SPI_0");

        //verification
        Array expectedR = Array.factory(DataType.SHORT, new int[]{3, 3}, new short[]{
                685, 621, 696,
                1391, 750, 588,
                914, 1170, 798});
        Array expectedS = Array.factory(DataType.DOUBLE, new int[]{3, 3}, new double[]{
                6.849999846890569, 6.2099998611956835, 6.959999844431877,
                13.909999689087272, 7.499999832361937, 5.879999868571758,
                9.13999979570508, 11.699999738484621, 7.9799998216331005});
        assertThat("\nActual  : " + arrayR + "\nExpected: " + expectedR, MAMath.equals(arrayR, expectedR), is(true));
        assertThat("\nActual  : " + arrayS + "\nExpected: " + expectedS, MAMath.equals(arrayS, expectedS), is(true));

        //execution
        arrayR = reader.readRaw(centerX, centerY, interval, "Cloud_Mask_SPI_1");
        arrayS = reader.readScaled(centerX, centerY, interval, "Cloud_Mask_SPI_1");

        //verification
        expectedR = Array.factory(DataType.SHORT, new int[]{3, 3}, new short[]{
                1051, 1365, 1182,
                2008, 1377, 871,
                2549, 1465, 1278});
        expectedS = Array.factory(DataType.DOUBLE, new int[]{3, 3}, new double[]{
                10.509999765083194, 13.649999694898725, 11.819999735802412,
                20.079999551177025, 13.769999692216516, 8.709999805316329,
                25.4899994302541, 14.649999672546983, 12.77999971434474});
        assertThat("\nActual  : " + arrayR + "\nExpected: " + expectedR, MAMath.equals(arrayR, expectedR), is(true));
        assertThat("\nActual  : " + arrayS + "\nExpected: " + expectedS, MAMath.equals(arrayS, expectedS), is(true));
    }

    @Test
    public void testReadCloudMaskSPI_unscaled_and_scaled_Aqua_outside_upper_left() throws IOException, InvalidRangeException {
        //preparation
        int centerX = 0;
        int centerY = 0;
        final Interval interval = new Interval(3, 3);
        String varName = "Cloud_Mask_SPI_0";
        reader.open(getAquaFile());
        short fillR = findVariableFillRaw(reader, varName).shortValue();
        double fillS = findVariableFillScaled(reader, varName).doubleValue();

        //execution
        Array arrayR = reader.readRaw(centerX, centerY, interval, varName);
        Array arrayS = reader.readScaled(centerX, centerY, interval, varName);

        //verification
        Array expectedR = Array.factory(DataType.SHORT, new int[]{3, 3}, new short[]{
                fillR, fillR, fillR,
                fillR, 165, 189,
                fillR, 366, 138});
        Array expectedS = Array.factory(DataType.DOUBLE, new int[]{3, 3}, new double[]{
                fillS, fillS, fillS,
                fillS, 1.649999963119626, 1.889999957755208,
                fillS, 3.659999918192625, 1.3799999691545963});
        assertThat("\nActual  : " + arrayR + "\nExpected: " + expectedR, MAMath.equals(arrayR, expectedR), is(true));
        assertThat("\nActual  : " + arrayS + "\nExpected: " + expectedS, MAMath.equals(arrayS, expectedS), is(true));

        //execution
        varName = "Cloud_Mask_SPI_1";
        arrayR = reader.readRaw(centerX, centerY, interval, varName);
        arrayS = reader.readScaled(centerX, centerY, interval, varName);

        //verification
        expectedR = Array.factory(DataType.SHORT, new int[]{3, 3}, new short[]{
                fillR, fillR, fillR,
                fillR, 204, 224,
                fillR, 419, 167});
        expectedS = Array.factory(DataType.DOUBLE, new int[]{3, 3}, new double[]{
                fillS, fillS, fillS,
                fillS, 2.0399999544024467, 2.2399999499320984,
                fillS, 4.189999906346202, 1.6699999626725912});
        assertThat("\nActual  : " + arrayR + "\nExpected: " + expectedR, MAMath.equals(arrayR, expectedR), is(true));
        assertThat("\nActual  : " + arrayS + "\nExpected: " + expectedS, MAMath.equals(arrayS, expectedS), is(true));
    }

    @Test
    public void testReadCloudMaskSPI_unscaled_and_scaled_Aqua_outside_lower_right() throws IOException, InvalidRangeException {
        //preparation
        reader.open(getAquaFile());
        final Dimension productSize = reader.getProductSize();
        int centerX = productSize.getNx() - 1;
        int centerY = productSize.getNy() - 1;
        final Interval interval = new Interval(3, 3);
        String varName = "Cloud_Mask_SPI_0";
        short fillR = findVariableFillRaw(reader, varName).shortValue();
        double fillS = findVariableFillScaled(reader, varName).doubleValue();

        //execution
        Array arrayR = reader.readRaw(centerX, centerY, interval, varName);
        Array arrayS = reader.readScaled(centerX, centerY, interval, varName);

        //verification
        Array expectedR = Array.factory(DataType.SHORT, new int[]{3, 3}, new short[]{
                1058, 1446, fillR,
                1868, 3091, fillR,
                fillR, fillR, fillR});
        Array expectedS = Array.factory(DataType.DOUBLE, new int[]{3, 3}, new double[]{
                10.579999763518572, 14.459999676793814, fillS,
                18.679999582469463, 30.90999930910766, fillS,
                fillS, fillS, fillS});
        assertThat("\nActual  : " + arrayR + "\nExpected: " + expectedR, MAMath.equals(arrayR, expectedR), is(true));
        assertThat("\nActual  : " + arrayS + "\nExpected: " + expectedS, MAMath.equals(arrayS, expectedS), is(true));

        //execution
        varName = "Cloud_Mask_SPI_1";
        arrayR = reader.readRaw(centerX, centerY, interval, varName);
        arrayS = reader.readScaled(centerX, centerY, interval, varName);

        //verification
        expectedR = Array.factory(DataType.SHORT, new int[]{3, 3}, new short[]{
                851, 1226, fillR,
                1508, 2332, fillR,
                fillR, fillR, fillR});
        expectedS = Array.factory(DataType.DOUBLE, new int[]{3, 3}, new double[]{
                8.509999809786677, 12.259999725967646, fillS,
                15.079999662935734, 23.31999947875738, fillS,
                fillS, fillS, fillS});
        assertThat("\nActual  : " + arrayR + "\nExpected: " + expectedR, MAMath.equals(arrayR, expectedR), is(true));
        assertThat("\nActual  : " + arrayS + "\nExpected: " + expectedS, MAMath.equals(arrayS, expectedS), is(true));
    }

    @Test
    public void testGetPixelLocator_Aqua() throws IOException {
        reader.open(getAquaFile());
        final PixelLocator pixelLocator = reader.getPixelLocator();
        Point2D geoLocation = pixelLocator.getGeoLocation(636.5, 176.5, null);
        assertEquals(23.081594400612136, geoLocation.getX(), 1e-8);
        assertEquals(44.011016845703125, geoLocation.getY(), 1e-8);

        geoLocation = pixelLocator.getGeoLocation(223.5, 296.5, null);
        assertEquals(29.132194669424624, geoLocation.getX(), 1e-8);
        assertEquals(45.781856536865234, geoLocation.getY(), 1e-8);

        geoLocation = pixelLocator.getGeoLocation(0.5, 0.5, null);
        assertEquals(37.36231459053941, geoLocation.getX(), 1e-8);
        assertEquals(43.49676513671875, geoLocation.getY(), 1e-8);

        Point2D[] pixelLocation = pixelLocator.getPixelLocation(23.081594400612136, 44.011016845703125);
        assertEquals(1, pixelLocation.length);
        assertEquals(636.5, pixelLocation[0].getX(), 0.1);
        assertEquals(176.5, pixelLocation[0].getY(), 0.1);

        pixelLocation = pixelLocator.getPixelLocation(29.132194669424624, 45.781856536865234);
        assertEquals(1, pixelLocation.length);
        assertEquals(223.5, pixelLocation[0].getX(), 0.1);
        assertEquals(296.5, pixelLocation[0].getY(), 0.1);
    }

    @Test
    public void testGetPixelLocator_Terra() throws IOException {
        reader.open(getTerraFile());
        final PixelLocator pixelLocator = reader.getPixelLocator();
        Point2D geoLocation = pixelLocator.getGeoLocation(263.5, 91.5, null);
        assertEquals(-12.790703f, (float) geoLocation.getX(), 1e-8);
        assertEquals(52.37138f, (float) geoLocation.getY(), 1e-8);

        geoLocation = pixelLocator.getGeoLocation(656.5, 378.5, null);
        assertEquals(-7.192285f, (float) geoLocation.getX(), 1e-8);
        assertEquals(49.096855f, (float) geoLocation.getY(), 1e-8);

        geoLocation = pixelLocator.getGeoLocation(6.5, 402.5, null);
        assertEquals(-22.722326f, (float) geoLocation.getX(), 1e-8);
        assertEquals(50.045197f, (float) geoLocation.getY(), 1e-8);

        Point2D[] pixelLocation = pixelLocator.getPixelLocation(-12.790703f, 52.37138f);
        assertEquals(1, pixelLocation.length);
        assertEquals(263.5, pixelLocation[0].getX(), 0.1);
        assertEquals(91.5, pixelLocation[0].getY(), 0.1);

        pixelLocation = pixelLocator.getPixelLocation(-7.192285f, 49.096855f);
        assertEquals(1, pixelLocation.length);
        assertEquals(656.5, pixelLocation[0].getX(), 0.1);
        assertEquals(378.5, pixelLocation[0].getY(), 0.1);
    }

    @Test
    public void testGetPixelLocator_Terra_NoCorrespondingMod03File() throws IOException {
        reader.packageLocalPropertyForUnitLevelTestsOnly_toSimulate_correspondingMod03FileNotAvailable = true;
        reader.open(getTerraFile());
        final PixelLocator pixelLocator = reader.getPixelLocator();
        Point2D geoLocation = pixelLocator.getGeoLocation(263.5, 91.5, null);
        assertEquals(-12.790703f, (float) geoLocation.getX(), 0.00011f);
        assertEquals(52.37138f, (float) geoLocation.getY(), 0.000004f);

        geoLocation = pixelLocator.getGeoLocation(656.5, 378.5, null);
        assertEquals(-7.192285f, (float) geoLocation.getX(), 0.00001f);
        assertEquals(49.096855f, (float) geoLocation.getY(), 0.000005f);

        geoLocation = pixelLocator.getGeoLocation(6.5, 402.5, null);
        assertEquals(-22.722326f, (float) geoLocation.getX(), 0.0011f);
        assertEquals(50.045197f, (float) geoLocation.getY(), 0.00007f);

        Point2D[] pixelLocation = pixelLocator.getPixelLocation(-12.790703f, 52.37138f);
        assertEquals(1, pixelLocation.length);
        assertEquals(263.5, pixelLocation[0].getX(), 0.1);
        assertEquals(91.5, pixelLocation[0].getY(), 0.1);

        pixelLocation = pixelLocator.getPixelLocation(-7.192285f, 49.096855f);
        assertEquals(1, pixelLocation.length);
        assertEquals(656.5, pixelLocation[0].getX(), 0.1);
        assertEquals(378.5, pixelLocation[0].getY(), 0.1);
    }

    @Test
    public void testReadRawAndScaledGiveTheSameResults_except_Cloud_Mask_SPI() throws IOException, InvalidRangeException {
        reader.open(getTerraFile());

        final List<String> names = reader.getVariables().stream()
                .map(v -> {
                    final String name = v.getShortName();
                    System.out.println(name);
                    return name;
                })
                .filter(n -> !n.startsWith("Cloud_Mask_SPI"))
                .collect(Collectors.toList());
        for (String name : names) {
            final Array rawArray = reader.readRaw(2, 2, new Interval(5, 5), name);
            final Array scaledArray = reader.readScaled(2, 2, new Interval(5, 5), name);
            final Object rawStorage = rawArray.getStorage();
            final Object scaledStorage = scaledArray.getStorage();
            assertSame(name, rawStorage, scaledStorage);
        }
    }

    private void checkVariables(MxD35_Reader reader) throws InvalidRangeException, IOException {
        final List<Variable> variables = reader.getVariables();
        assertEquals(25, variables.size());

        final boolean noMod03 = reader.packageLocalPropertyForUnitLevelTestsOnly_toSimulate_correspondingMod03FileNotAvailable;
        final String expectedLonLatClass = noMod03 ? MxD35BowTieVariable.class.getName() : Variable.class.getName();
        Variable variable = variables.get(0);
        assertThat(variable.getClass().getName(), is(expectedLonLatClass));
        assertEquals("Latitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(1);
        assertThat(variable.getClass().getName(), is(expectedLonLatClass));
        assertEquals("Longitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(2);
        assertThat(variable.getClass().getName(), is(MxD35ScanTimeVariable.class.getName()));
        assertEquals("Scan_Start_Time", variable.getShortName());
        assertEquals(DataType.DOUBLE, variable.getDataType());

        variable = variables.get(3);
        assertThat(variable.getClass().getName(), is(MxD35BowTieVariable.class.getName()));
        assertEquals("Solar_Zenith", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(4);
        assertThat(variable.getClass().getName(), is(MxD35BowTieVariable.class.getName()));
        assertEquals("Solar_Azimuth", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(5);
        assertThat(variable.getClass().getName(), is(MxD35BowTieVariable.class.getName()));
        assertEquals("Sensor_Zenith", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(6);
        assertThat(variable.getClass().getName(), is(MxD35BowTieVariable.class.getName()));
        assertEquals("Sensor_Azimuth", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(7);
        assertThat(variable.getClass().getName(), is(Variable.class.getName()));
        assertEquals("Cloud_Mask_SPI_0", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());

        variable = variables.get(8);
        assertThat(variable.getClass().getName(), is(Variable.class.getName()));
        assertEquals("Cloud_Mask_SPI_1", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());

        variable = variables.get(9);
        assertThat(variable.getClass().getName(), is(Variable.class.getName()));
        assertEquals("Cloud_Mask_0", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(14);
        assertThat(variable.getClass().getName(), is(Variable.class.getName()));
        assertEquals("Cloud_Mask_5", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(15);
        assertThat(variable.getClass().getName(), is(Variable.class.getName()));
        assertEquals("Quality_Assurance_0", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(24);
        assertThat(variable.getClass().getName(), is(Variable.class.getName()));
        assertEquals("Quality_Assurance_9", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        final boolean Y = true;
        final boolean n = false;

        final List<Variable> cloudMaskVars = variables.subList(9, 15);
        final boolean[] expValuCM = {Y, n, n, n, n, n};
        final int[] expSizeCM = {12, 8, 8, 8, 8, 8};
        for (int i = 0; i < cloudMaskVars.size(); i++) {
            Variable cloudMaskVar = cloudMaskVars.get(i);
            assertThat(cloudMaskVar.getShortName(), startsWith("Cloud_Mask_"));
            assertThat(cloudMaskVar.hasAttribute(CF_SCALE_FACTOR_NAME), is(false));
            assertThat(cloudMaskVar.hasAttribute(CF_ADD_OFFSET_NAME), is(false));
            assertThat(cloudMaskVar.hasAttribute(CF_VALID_RANGE_NAME), is(false));
            assertThat(cloudMaskVar.hasAttribute("description"), is(true));
            assertThat(cloudMaskVar.hasAttribute(CF_FLAG_MASKS_NAME), is(true));
            assertThat(cloudMaskVar.findAttribute(CF_FLAG_MASKS_NAME).getLength(), is(equalTo(expSizeCM[i])));
            assertThat(cloudMaskVar.hasAttribute(CF_FLAG_VALUES_NAME), is(expValuCM[i]));
            if (expValuCM[i]) {
                assertThat(cloudMaskVar.findAttribute(CF_FLAG_VALUES_NAME).getLength(), is(equalTo(expSizeCM[i])));
            }
            assertThat(cloudMaskVar.hasAttribute(CF_FLAG_MEANINGS_NAME), is(true));
            assertThat(cloudMaskVar.findAttribute(CF_FLAG_MEANINGS_NAME)
                               .getStringValue().split(" ").length, is(equalTo(expSizeCM[i])));
        }
        final List<Variable> qualAssurVars = variables.subList(15, 25);
        final boolean[] expFValQA = {Y, n, n, n, n, n, Y, Y, Y, Y, Y};
        final int[] expSizeQA = {9, 8, 8, 8, 8, 8, 8, 16, 16, 5};
        for (int i = 0; i < qualAssurVars.size(); i++) {
            Variable qualAssurVar = qualAssurVars.get(i);
            final String msg = "Idx: " + i;
            assertThat(msg, qualAssurVar.getShortName(), startsWith("Quality_Assurance_"));
            assertThat(msg, qualAssurVar.hasAttribute(CF_SCALE_FACTOR_NAME), is(false));
            assertThat(msg, qualAssurVar.hasAttribute(CF_ADD_OFFSET_NAME), is(false));
            assertThat(msg, qualAssurVar.hasAttribute(CF_VALID_RANGE_NAME), is(false));
            assertThat(msg, qualAssurVar.hasAttribute("description"), is(true));
            assertThat(msg, qualAssurVar.hasAttribute(CF_FLAG_MASKS_NAME), is(true));
            assertThat(msg, qualAssurVar.findAttribute(CF_FLAG_MASKS_NAME).getLength(), is(equalTo(expSizeQA[i])));
            assertThat(msg, qualAssurVar.hasAttribute(CF_FLAG_VALUES_NAME), is(expFValQA[i]));
            if (expFValQA[i]) {
                assertThat(msg, qualAssurVar.findAttribute(CF_FLAG_VALUES_NAME).getLength(), is(equalTo(expSizeQA[i])));
            }
            assertThat(msg, qualAssurVar.hasAttribute(CF_FLAG_MEANINGS_NAME), is(true));
            assertThat(msg, qualAssurVar.findAttribute(CF_FLAG_MEANINGS_NAME)
                    .getStringValue().split(" ").length, is(equalTo(expSizeQA[i])));
        }
    }

    private Number findVariableFillRaw(MxD35_Reader reader, String varName) throws InvalidRangeException, IOException {
        final Variable variable = findVariable(reader, varName);
        return findAttrNumberValue(variable, CF_FILL_VALUE_NAME);
    }

    private Number findVariableFillScaled(MxD35_Reader reader, String varName) throws InvalidRangeException, IOException {
        final Variable variable = findVariable(reader, varName);
        Number rawFill = findAttrNumberValue(variable, CF_FILL_VALUE_NAME);
        if (rawFill == null) {
            return null;
        }
        Number scaledFill = rawFill;
        Number scaling = findAttrNumberValue(variable, CF_SCALE_FACTOR_NAME);
        if (scaling != null && scaling.doubleValue() != 1.0) {
            scaledFill = rawFill.doubleValue() * scaling.doubleValue();
        }
        Number offset = findAttrNumberValue(variable, CF_ADD_OFFSET_NAME);
        if (offset != null && offset.doubleValue() != 0.0) {
            scaledFill = scaledFill.doubleValue() + offset.doubleValue();
        }
        return scaledFill;
    }

    private Number findAttrNumberValue(Variable variable, String attName) {
        final Optional<Attribute> optFill = variable.getAttributes().stream().filter(a -> a.getShortName().equals(attName)).findFirst();
        if (optFill.isPresent()) {
            return optFill.get().getNumericValue();
        }
        return null;
    }

    private Variable findVariable(MxD35_Reader reader, String varName) throws InvalidRangeException, IOException {
        final List<Variable> variables = reader.getVariables();
        for (Variable variable : variables) {
            if (variable.getShortName().equals(varName)) {
                return variable;
            }
        }
        return null;
    }

    private static File getTerraFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"mod35-te", "v61", "2022", "115", "MOD35_L2.A2022115.1125.061.2022115193707.hdf"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private static File getAquaFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"myd35-aq", "v61", "2022", "115", "MYD35_L2.A2022115.1135.061.2022116151528.hdf"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}

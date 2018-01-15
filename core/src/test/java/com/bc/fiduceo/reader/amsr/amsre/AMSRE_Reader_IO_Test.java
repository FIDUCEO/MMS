/*
 * Copyright (C) 2016 Brockmann Consult GmbH
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

package com.bc.fiduceo.reader.amsr.amsre;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.reader.TimeLocator_TAI1993Vector;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class AMSRE_Reader_IO_Test {

    private File testDataDirectory;
    private AMSRE_Reader reader;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory();
        reader = new AMSRE_Reader(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File file = getAmsreFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2005, 2, 17, 5, 36, 6, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2005, 2, 17, 6, 25, 56, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.DESCENDING, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);

            Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(67, coordinates.length);

            assertEquals(111.96620178222658, coordinates[0].getLon(), 1e-8);
            assertEquals(83.54039001464844, coordinates[0].getLat(), 1e-8);

            assertEquals(-159.9243621826172, coordinates[23].getLon(), 1e-8);
            assertEquals(-77.13606262207031, coordinates[23].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);

            final TimeAxis timeAxis = timeAxes[0];
            coordinates = timeAxis.getGeometry().getCoordinates();
            final Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2005, 2, 17, 5, 36, 6, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator() throws IOException {
        final File file = getAmsreFile();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);
            assertTrue(timeLocator instanceof TimeLocator_TAI1993Vector);

            long time = timeLocator.getTimeFor(67, 0);
            assertEquals(1108618539107L, time);
            TestUtil.assertCorrectUTCDate(2005, 2, 17, 5, 35, 39, new Date(time));

            time = timeLocator.getTimeFor(68, 1000);
            assertEquals(1108620038998L, time);

            time = timeLocator.getTimeFor(68, 1994);
            assertEquals(1108621529890L, time);
            TestUtil.assertCorrectUTCDate(2005, 2, 17, 6, 25, 29, new Date(time));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables() throws IOException, InvalidRangeException {
        final File file = getAmsreFile();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(37, variables.size());

            Variable variable = variables.get(0);
            assertEquals("Time", variable.getShortName());
            assertEquals(DataType.DOUBLE, variable.getDataType());

            variable = variables.get(3);
            assertEquals("Earth_Incidence", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(6);
            assertEquals("Sun_Azimuth", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(7);
            assertEquals("Land_Ocean_Flag_6", variable.getShortName());
            assertEquals(DataType.BYTE, variable.getDataType());

            variable = variables.get(8);
            assertEquals("6_9V_Res_1_TB", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(11);
            assertEquals("10_7H_Res_1_TB", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(14);
            assertEquals("23_8V_Res_1_TB", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(17);
            assertEquals("36_5H_Res_1_TB", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(20);
            assertEquals("Scan_Quality_Flag", variable.getShortName());
            assertEquals(DataType.INT, variable.getDataType());

            variable = variables.get(21);
            assertEquals("Channel_Quality_Flag_6V", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(24);
            assertEquals("Channel_Quality_Flag_10H", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(27);
            assertEquals("Channel_Quality_Flag_23V", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(30);
            assertEquals("Channel_Quality_Flag_36H", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(33);
            assertEquals("Res1_Surf", variable.getShortName());
            assertEquals(DataType.BYTE, variable.getDataType());

            variable = variables.get(34);
            assertEquals("Sun_Glint_Angle", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(36);
            assertEquals("Geostationary_Reflection_Longitude", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize() throws IOException, InvalidRangeException {
        final File file = getAmsreFile();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertEquals(243, productSize.getNx());
            assertEquals(1995, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw() throws IOException, InvalidRangeException {
        final File file = getAmsreFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(25, 333, interval, "Time");
            NCTestUtils.assertValueAt(3.827726675720466E8, 0, 0, array);
            NCTestUtils.assertValueAt(3.827726675720466E8, 1, 0, array);

            array = reader.readRaw(26, 334, interval, "Longitude");
            NCTestUtils.assertValueAt(-60.86850357055664, 2, 0, array);
            NCTestUtils.assertValueAt(-60.78384017944336, 3, 0, array);
            NCTestUtils.assertValueAt(-60.69780349731445, 4, 0, array);

            array = reader.readRaw(27, 335, interval, "Earth_Azimuth");
            NCTestUtils.assertValueAt(-11544, 0, 1, array);
            NCTestUtils.assertValueAt(-11598, 1, 1, array);
            NCTestUtils.assertValueAt(-11652, 2, 1, array);

            array = reader.readRaw(28, 395, interval, "Land_Ocean_Flag_6");
            NCTestUtils.assertValueAt(0, 2, 0, array);
            NCTestUtils.assertValueAt(0, 2, 1, array);
            NCTestUtils.assertValueAt(0, 2, 2, array);
            NCTestUtils.assertValueAt(1, 2, 3, array);
            NCTestUtils.assertValueAt(5, 2, 4, array);

            array = reader.readRaw(28, 396, interval, "6_9H_Res_1_TB");
            NCTestUtils.assertValueAt(-11083, 3, 1, array);
            NCTestUtils.assertValueAt(-11132, 4, 1, array);
            NCTestUtils.assertValueAt(-11213, 0, 2, array);

            // check one px at the swath borders to check fill value handling tb 2016-09-06
            array = reader.readRaw(241, 397, interval, "18_7V_Res_1_TB");
            NCTestUtils.assertValueAt(-14200, 2, 2, array);
            NCTestUtils.assertValueAt(-14182, 3, 2, array);
            NCTestUtils.assertValueAt(-32767, 4, 2, array);

            array = reader.readRaw(29, 398, interval, "Scan_Quality_Flag");
            NCTestUtils.assertValueAt(0, 0, 3, array);
            NCTestUtils.assertValueAt(0, 1, 3, array);
            NCTestUtils.assertValueAt(0, 2, 3, array);

            array = reader.readRaw(1, 1, interval, "Channel_Quality_Flag_10V");
            NCTestUtils.assertValueAt(-32767, 2, 0, array);
            NCTestUtils.assertValueAt(7, 2, 1, array);
            NCTestUtils.assertValueAt(0, 2, 2, array);

            array = reader.readRaw(2, 2, interval, "Channel_Quality_Flag_89V");
            NCTestUtils.assertValueAt(7, 3, 0, array);
            NCTestUtils.assertValueAt(11, 3, 1, array);
            NCTestUtils.assertValueAt(11, 3, 2, array);

            array = reader.readRaw(30, 399, interval, "Res1_Surf");
            NCTestUtils.assertValueAt(75, 4, 1, array);
            NCTestUtils.assertValueAt(94, 4, 2, array);
            NCTestUtils.assertValueAt(130, 4, 3, array);

            array = reader.readRaw(93, 1152, interval, "Geostationary_Reflection_Latitude");
            NCTestUtils.assertValueAt(-5372, 2, 2, array);
            NCTestUtils.assertValueAt(-5409, 3, 2, array);
            NCTestUtils.assertValueAt(-5380, 2, 3, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_rightBorder() throws IOException, InvalidRangeException {
        final File file = getAmsreFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(242, 335, interval, "Time");
            NCTestUtils.assertValueAt(3.8277267057169616E8, 2, 0, array);
            NCTestUtils.assertValueAt(9.969209968386869E36, 3, 0, array);

            array = reader.readRaw(242, 334, interval, "Longitude");
            NCTestUtils.assertValueAt(-30.30225944519043, 2, 1, array);
            NCTestUtils.assertValueAt(9.969209968386869E36, 3, 1, array);

            array = reader.readRaw(242, 336, interval, "Earth_Azimuth");
            NCTestUtils.assertValueAt(13653, 2, 2, array);
            NCTestUtils.assertValueAt(-32767, 3, 2, array);

            array = reader.readRaw(242, 337, interval, "Land_Ocean_Flag_6");
            NCTestUtils.assertValueAt(0, 2, 3, array);
            NCTestUtils.assertValueAt(-127, 3, 3, array);

            array = reader.readRaw(242, 338, interval, "6_9H_Res_1_TB");
            NCTestUtils.assertValueAt(-23777, 2, 4, array);
            NCTestUtils.assertValueAt(-32767, 3, 4, array);

            array = reader.readRaw(242, 339, interval, "18_7V_Res_1_TB");
            NCTestUtils.assertValueAt(-13934, 2, 0, array);
            NCTestUtils.assertValueAt(-32767, 3, 0, array);

            array = reader.readRaw(242, 340, interval, "Scan_Quality_Flag");
            NCTestUtils.assertValueAt(0, 2, 1, array);
            NCTestUtils.assertValueAt(-2147483647, 3, 1, array);

            array = reader.readRaw(242, 341, interval, "Channel_Quality_Flag_10V");
            NCTestUtils.assertValueAt(0, 2, 2, array);
            NCTestUtils.assertValueAt(-32767, 3, 2, array);

            array = reader.readRaw(242, 342, interval, "Channel_Quality_Flag_89V");
            NCTestUtils.assertValueAt(11, 2, 3, array);
            NCTestUtils.assertValueAt(-32767, 3, 3, array);

            array = reader.readRaw(242, 343, interval, "Res1_Surf");
            NCTestUtils.assertValueAt(0, 2, 4, array);
            NCTestUtils.assertValueAt(-127, 3, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled() throws IOException, InvalidRangeException {
        final File file = getAmsreFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(7, 7);
            Array array = reader.readScaled(156, 533, interval, "Latitude");
            NCTestUtils.assertValueAt(42.16802215576172, 0, 0, array);
            NCTestUtils.assertValueAt(42.17781066894531, 1, 0, array);
            NCTestUtils.assertValueAt(42.18836975097656, 2, 0, array);

            array = reader.readScaled(157, 534, interval, "Sun_Elevation");
            NCTestUtils.assertValueAt(84.50000125914812, 3, 0, array);
            NCTestUtils.assertValueAt(84.400001257658, 4, 0, array);
            NCTestUtils.assertValueAt(84.400001257658, 5, 0, array);

            array = reader.readScaled(158, 535, interval, "Land_Ocean_Flag_6");
            NCTestUtils.assertValueAt(0, 6, 0, array);
            NCTestUtils.assertValueAt(0, 0, 1, array);
            NCTestUtils.assertValueAt(0, 1, 1, array);

            array = reader.readScaled(159, 536, interval, "10_7V_Res_1_TB");
            NCTestUtils.assertValueAt(170.6299961861223, 2, 1, array);
            NCTestUtils.assertValueAt(170.48999618925154, 3, 1, array);
            NCTestUtils.assertValueAt(170.36999619193375, 4, 1, array);

            array = reader.readScaled(160, 537, interval, "18_7H_Res_1_TB");
            NCTestUtils.assertValueAt(123.63999723643064, 5, 1, array);
            NCTestUtils.assertValueAt(123.36999724246562, 6, 1, array);
            NCTestUtils.assertValueAt(125.73999718949199, 0, 2, array);

            array = reader.readScaled(161, 538, interval, "Scan_Quality_Flag");
            NCTestUtils.assertValueAt(0, 1, 2, array);
            NCTestUtils.assertValueAt(0, 2, 2, array);
            NCTestUtils.assertValueAt(0, 3, 2, array);

            array = reader.readScaled(162, 539, interval, "Channel_Quality_Flag_89H");
            NCTestUtils.assertValueAt(11, 4, 2, array);
            NCTestUtils.assertValueAt(11, 5, 2, array);
            NCTestUtils.assertValueAt(11, 6, 2, array);

            array = reader.readScaled(101, 647, interval, "Geostationary_Reflection_Longitude");
            NCTestUtils.assertValueAt(-78.58999824337661, 0, 3, array);
            NCTestUtils.assertValueAt(-78.08999825455248, 1, 3, array);
            NCTestUtils.assertValueAt(-77.58999826572835, 2, 3, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime() throws IOException, InvalidRangeException {
        final File file = getAmsreFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(3, 3);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(72, 1107, interval);
            assertNotNull(acquisitionTime);
            final Index index = acquisitionTime.getIndex();

            index.set(0, 0);
            assertEquals(1108620197, acquisitionTime.getInt(index));
            index.set(1, 0);
            assertEquals(1108620199, acquisitionTime.getInt(index));
            index.set(2, 0);
            assertEquals(1108620200, acquisitionTime.getInt(index));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_withFillValue() throws IOException, InvalidRangeException {
        final File file = getAmsreFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(1, 1107, interval);
            assertNotNull(acquisitionTime);
            final Index index = acquisitionTime.getIndex();

            index.set(0, 0);
            assertEquals(NetCDFUtils.getDefaultFillValue(int.class), acquisitionTime.getInt(index));
            index.set(1, 0);
            assertEquals(-2147483647, acquisitionTime.getInt(index));
            index.set(2, 0);
            assertEquals(-2147483647, acquisitionTime.getInt(index));
            index.set(0, 1);
            assertEquals(1108620196, acquisitionTime.getInt(index));
            index.set(1, 1);
            assertEquals(1108620197, acquisitionTime.getInt(index));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator() throws IOException {
        final File file = getAmsreFile();

        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            Point2D geoLocation = pixelLocator.getGeoLocation(144.5, 1401.5, null);
            assertEquals(-73.31768035888672, geoLocation.getX(), 1e-8);
            assertEquals(-36.6185188293457, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(67.5, 1862.5, null);
            assertEquals(-117.21066284179688, geoLocation.getX(), 1e-8);
            assertEquals(-72.551445, geoLocation.getY(), 1e-8);

            Point2D[] pixelLocation = pixelLocator.getPixelLocation(-51.034195, 52.659184);
            assertEquals(1, pixelLocation.length);
            assertEquals(132.51219668358698, pixelLocation[0].getX(), 1e-8);
            assertEquals(411.4166814102514, pixelLocation[0].getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(-4, 1176);
            assertEquals(0, pixelLocation.length);
        } finally {
            reader.close();
        }
    }

    private File getAmsreFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"amsre-aq", "v12", "2005", "02", "17", "AMSR_E_L2A_BrightnessTemperatures_V12_200502170536_D.hdf"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}

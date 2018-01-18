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

package com.bc.fiduceo.reader.amsr.amsr2;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.reader.TimeLocator_TAI1993Vector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class AMSR2_Reader_IO_Test {

    private File testDataDirectory;
    private AMSR2_Reader reader;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory();
        reader = new AMSR2_Reader(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File amsr2File = getAmsr2File();

        try {
            reader.open(amsr2File);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2013, 7, 1, 9, 42, 53, 154, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2013, 7, 1, 10, 32, 27, 365, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.ASCENDING, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);

            Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(69, coordinates.length);

            assertEquals(176.48370361328125, coordinates[0].getLon(), 1e-8);
            assertEquals(-72.13760375976562, coordinates[0].getLat(), 1e-8);

            assertEquals(-60.5750846862793, coordinates[28].getLon(), 1e-8);
            assertEquals(79.94698333740234, coordinates[28].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);

            final TimeAxis timeAxis = timeAxes[0];
            coordinates = timeAxis.getGeometry().getCoordinates();
            assertEquals(22, coordinates.length);
            assertEquals(109.5756301879883, coordinates[1].getLon(), 1e-8);
            assertEquals(-79.83582305908203, coordinates[1].getLat(), 1e-8);

            assertEquals(47.50423812866211, coordinates[12].getLon(), 1e-8);
            assertEquals(15.753782272338867, coordinates[12].getLat(), 1e-8);

            Date time = timeAxis.getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2013, 7, 1, 9, 42, 53, time);

            time = timeAxis.getTime(coordinates[12]);
            TestUtil.assertCorrectUTCDate(2013, 7, 1, 10, 11, 56, time);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator() throws IOException {
        final File amsr2File = getAmsr2File();

        try {
            reader.open(amsr2File);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);
            assertTrue(timeLocator instanceof TimeLocator_TAI1993Vector);

            long time = timeLocator.getTimeFor(68, 0);
            assertEquals(1372671701158L, time);
            TestUtil.assertCorrectUTCDate(2013, 7, 1, 9, 41, 41, new Date(time));

            time = timeLocator.getTimeFor(69, 1000);
            assertEquals(1372673201011L, time);
            TestUtil.assertCorrectUTCDate(2013, 7, 1, 10, 6, 41, new Date(time));

            time = timeLocator.getTimeFor(70, 1994);
            assertEquals(1372674691867L, time);
            TestUtil.assertCorrectUTCDate(2013, 7, 1, 10, 31, 31, new Date(time));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables() throws IOException {
        final File amsr2File = getAmsr2File();

        try {
            reader.open(amsr2File);

            final List<Variable> variables = reader.getVariables();
            assertEquals(49, variables.size());

            Variable variable = variables.get(0);
            assertEquals("Area_Mean_Height", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(4);
            assertEquals("Brightness_Temperature_(res06,18.7GHz,V)", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(9);
            assertEquals("Brightness_Temperature_(res06,6.9GHz,H)", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(13);
            assertEquals("Brightness_Temperature_(res06,89.0GHz,H)", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(25);
            assertEquals("Brightness_Temperature_(res23,18.7GHz,H)", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(38);
            assertEquals("Earth_Incidence", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(43);
            assertEquals("Land_Ocean_Flag_6", variable.getShortName());
            assertEquals(DataType.BYTE, variable.getDataType());

            variable = variables.get(44);
            assertEquals("Land_Ocean_Flag_23", variable.getShortName());
            assertEquals(DataType.BYTE, variable.getDataType());

            variable = variables.get(45);
            assertEquals("Latitude_of_Observation_Point_for_89A", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());

            variable = variables.get(46);
            assertEquals("Land_Ocean_Flag_10", variable.getShortName());
            assertEquals(DataType.BYTE, variable.getDataType());

            variable = variables.get(47);
            assertEquals("Land_Ocean_Flag_36", variable.getShortName());
            assertEquals(DataType.BYTE, variable.getDataType());

            variable = variables.get(48);
            assertEquals("Pixel_Data_Quality_6_to_36", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw() throws IOException, InvalidRangeException {
        final File amsr2File = getAmsr2File();

        try {
            reader.open(amsr2File);

            final Interval interval = new Interval(3, 3);
            Array array = reader.readRaw(102, 247, interval, "Area_Mean_Height");
            NCTestUtils.assertValueAt(1425, 0, 0, array);
            NCTestUtils.assertValueAt(1430, 1, 0, array);

            array = reader.readRaw(103, 248, interval, "Brightness_Temperature_(res06,10.7GHz,V)");
            NCTestUtils.assertValueAt(24337, 2, 0, array);
            NCTestUtils.assertValueAt(24504, 0, 1, array);

            array = reader.readRaw(104, 249, interval, "Brightness_Temperature_(res10,89.0GHz,V)");
            NCTestUtils.assertValueAt(23899, 1, 1, array);
            NCTestUtils.assertValueAt(23845, 2, 1, array);

            array = reader.readRaw(105, 250, interval, "Earth_Azimuth");
            NCTestUtils.assertValueAt(16406, 0, 2, array);
            NCTestUtils.assertValueAt(16364, 1, 2, array);

            array = reader.readRaw(106, 251, interval, "Land_Ocean_Flag_10");
            NCTestUtils.assertValueAt(100, 2, 2, array);
            NCTestUtils.assertValueAt(100, 0, 0, array);

            array = reader.readRaw(107, 252, interval, "Latitude_of_Observation_Point_for_89A");
            NCTestUtils.assertValueAt(-68.30419158935547, 1, 0, array);
            NCTestUtils.assertValueAt(-68.32845306396484, 2, 0, array);

            array = reader.readRaw(0, 297, interval, "Pixel_Data_Quality_6_to_36");
            NCTestUtils.assertValueAt(32, 1, 1, array);
            NCTestUtils.assertValueAt(0, 2, 1, array);

            array = reader.readRaw(107, 330, interval, "Scan_Time");
            NCTestUtils.assertValueAt(6.468258326109686E8, 0, 2, array);
            NCTestUtils.assertValueAt(6.468258326109686E8, 1, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_onBorders() throws IOException, InvalidRangeException {
        final File file = getAmsr2File();

        try {
            reader.open(file);

            final Interval interval = new Interval(3, 3);
            // upper left
            Array array = reader.readRaw(0, 0, interval, "Brightness_Temperature_(res06,10.7GHz,V)");
            NCTestUtils.assertValueAt(-1, 0, 0, array);
            NCTestUtils.assertValueAt(-1, 1, 0, array);
            NCTestUtils.assertValueAt(24946, 1, 1, array);
            NCTestUtils.assertValueAt(24940, 2, 1, array);

            // upper center
            array = reader.readRaw(125, 0, interval, "Brightness_Temperature_(res10,10.7GHz,H)");
            NCTestUtils.assertValueAt(-1, 0, 0, array);
            NCTestUtils.assertValueAt(-1, 1, 0, array);
            NCTestUtils.assertValueAt(17707, 1, 1, array);
            NCTestUtils.assertValueAt(17793, 1, 2, array);

            // upper right
            array = reader.readRaw(242, 0, interval, "Earth_Incidence");
            NCTestUtils.assertValueAt(-32767, 0, 0, array);
            NCTestUtils.assertValueAt(-32767, 2, 1, array);
            NCTestUtils.assertValueAt(5527, 0, 1, array);
            NCTestUtils.assertValueAt(5527, 1, 2, array);

            // right center
            array = reader.readRaw(242, 1023, interval, "Land_Ocean_Flag_6");
            NCTestUtils.assertValueAt(0, 0, 0, array);
            NCTestUtils.assertValueAt(-1, 2, 0, array);
            NCTestUtils.assertValueAt(-1, 2, 1, array);
            NCTestUtils.assertValueAt(0, 1, 2, array);

            // lower right
            array = reader.readRaw(242, 2043, interval, "Latitude_of_Observation_Point_for_89A");
            NCTestUtils.assertValueAt(74.34542846679688, 0, 0, array);
            NCTestUtils.assertValueAt(74.36033630371094, 0, 1, array);
            NCTestUtils.assertValueAt(9.969209968386869E36, 2, 0, array);
            NCTestUtils.assertValueAt(9.969209968386869E36, 2, 2, array);

            // bottom center
            array = reader.readRaw(121, 2043, interval, "Pixel_Data_Quality_6_to_36");
            NCTestUtils.assertValueAt(0, 1, 0, array);
            NCTestUtils.assertValueAt(-4, 1, 1, array);
            NCTestUtils.assertValueAt(-32767, 2, 2, array);

            // lower left
            array = reader.readRaw(0, 2043, interval, "Position_in_Orbit");
            NCTestUtils.assertValueAt(9.969209968386869E36, 0, 0, array);
            NCTestUtils.assertValueAt(9.969209968386869E36, 0, 1, array);
            NCTestUtils.assertValueAt(5972.236457044598, 1, 1, array);
            NCTestUtils.assertValueAt(5972.236457044598, 2, 1, array);

            // left center
            array = reader.readRaw(0, 1045, interval, "Scan_Time");
            NCTestUtils.assertValueAt(9.969209968386869E36, 0, 0, array);
            NCTestUtils.assertValueAt(9.969209968386869E36, 0, 1, array);
            NCTestUtils.assertValueAt(6.468269035062965E8, 1, 1, array);
            NCTestUtils.assertValueAt(6.468269035062965E8, 2, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled() throws IOException, InvalidRangeException {
        final File file = getAmsr2File();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readScaled(104, 923, interval, "Area_Mean_Height");
            NCTestUtils.assertValueAt(0, 0, 0, array);
            NCTestUtils.assertValueAt(0, 2, 1, array);

            array = reader.readScaled(105, 924, interval, "Brightness_Temperature_(res10,36.5GHz,H)");
            NCTestUtils.assertValueAt(159.72999642975628, 1, 1, array);
            NCTestUtils.assertValueAt(159.95999642461538, 2, 2, array);

            array = reader.readScaled(106, 925, interval, "Earth_Azimuth");
            NCTestUtils.assertValueAt(-179.27999599277973, 2, 1, array);
            NCTestUtils.assertValueAt(-179.91999597847462, 3, 2, array);

            array = reader.readScaled(107, 926, interval, "Land_Ocean_Flag_36");
            NCTestUtils.assertValueAt(0, 4, 1, array);
            NCTestUtils.assertValueAt(0, 0, 3, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize() throws IOException {
        final File amsr2File = getAmsr2File();

        try {
            reader.open(amsr2File);

            final Dimension productSize = reader.getProductSize();
            assertEquals(243, productSize.getNx());
            assertEquals(2044, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    private File getAmsr2File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"amsr2-gcw1", "v220", "2013", "07", "01", "GW1AM2_201307010942_035A_L1SGRTBR_2220220.h5"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}

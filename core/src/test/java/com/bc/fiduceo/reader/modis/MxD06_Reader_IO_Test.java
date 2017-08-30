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
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
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
public class MxD06_Reader_IO_Test {

    private File dataDirectory;
    private MxD06_Reader reader;

    @Before
    public void setUp() throws IOException {
        dataDirectory = TestUtil.getTestDataDirectory();
        reader = new MxD06_Reader(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testReadAcquisitionInfo_Terra() throws IOException {
        final File file = getTerraFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2013, 2, 6, 14, 35, 0, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2013, 2, 6, 14, 40, 0, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);
            final Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(31, coordinates.length);
            assertEquals(-67.80709838867188, coordinates[0].getLon(), 1e-8);
            assertEquals(48.376953125, coordinates[0].getLat(), 1e-8);

            assertEquals(-38.39603042602539, coordinates[24].getLon(), 1e-8);
            assertEquals(44.151187896728516, coordinates[24].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            Point[] locations = coordinates[0].getCoordinates();
            Date time = timeAxes[0].getTime(locations[0]);
            TestUtil.assertCorrectUTCDate(2013, 2, 6, 14, 35, 7, 198, time);

            locations = coordinates[9].getCoordinates();
            time = timeAxes[0].getTime(locations[0]);
            TestUtil.assertCorrectUTCDate(2013, 2, 6, 14, 40, 0, 0, time);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_Aqua() throws IOException {
        final File file = getAquaFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2009, 5, 13, 10, 35, 0, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2009, 5, 13, 10, 40, 0, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);
            final Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(31, coordinates.length);
            assertEquals(95.26064300537111, coordinates[0].getLon(), 1e-8);
            assertEquals(-65.07981872558594, coordinates[0].getLat(), 1e-8);

            assertEquals(36.58618927001953, coordinates[24].getLon(), 1e-8);
            assertEquals(-73.6755599975586, coordinates[24].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            Point[] locations = coordinates[0].getCoordinates();
            Date time = timeAxes[0].getTime(locations[0]);
            TestUtil.assertCorrectUTCDate(2009, 5, 13, 10, 35, 0, 0, time);

            locations = coordinates[9].getCoordinates();
            time = timeAxes[0].getTime(locations[0]);
            TestUtil.assertCorrectUTCDate(2009, 5, 13, 10, 39, 54, 17, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_Terra() throws IOException {
        final File file = getTerraFile();

        try {
            reader.open(file);
            final Dimension productSize = reader.getProductSize();
            assertEquals(270, productSize.getNx());
            assertEquals(406, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_Aqua() throws IOException {
        final File file = getAquaFile();

        try {
            reader.open(file);
            final Dimension productSize = reader.getProductSize();
            assertEquals(270, productSize.getNx());
            assertEquals(406, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_Terra() throws IOException {
        final File file = getTerraFile();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertEquals(1360161273778L, timeLocator.getTimeFor(0, 0));
            assertEquals(1360161273778L, timeLocator.getTimeFor(269, 0));

            assertEquals(1360161422969L, timeLocator.getTimeFor(76, 203));
            assertEquals(1360161572161L, timeLocator.getTimeFor(145, 405));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_Aqua() throws IOException {
        final File file = getAquaFile();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertEquals(1242210874206L, timeLocator.getTimeFor(4, 0));
            assertEquals(1242210874206L, timeLocator.getTimeFor(223, 0));

            assertEquals(1242211023395L, timeLocator.getTimeFor(21, 202));
            assertEquals(1242211172583L, timeLocator.getTimeFor(147, 405));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_Terra() throws IOException, InvalidRangeException {
        final File file = getTerraFile();

        try {
            reader.open(file);
            final List<Variable> variables = reader.getVariables();
            assertEquals(112, variables.size());

            Variable variable = variables.get(0);
            assertEquals("Latitude", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());

            variable = variables.get(2);
            assertEquals("Scan_Start_Time", variable.getShortName());
            assertEquals(DataType.DOUBLE, variable.getDataType());

            variable = variables.get(4);
            assertEquals("Solar_Zenith_Day", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(8);
            assertEquals("Solar_Azimuth_Night", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(15);
            assertEquals("Surface_Temperature", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(21);
            assertEquals("Cloud_Top_Height_Nadir_Night", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(32);
            assertEquals("Cloud_Top_Temperature_Day", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(43);
            assertEquals("Cloud_Effective_Emissivity_Night", variable.getShortName());
            assertEquals(DataType.BYTE, variable.getDataType());

            variable = variables.get(54);
            assertEquals("os_top_flag_1km", variable.getShortName());
            assertEquals(DataType.BYTE, variable.getDataType());

            variable = variables.get(65);
            assertEquals("Cloud_Effective_Radius", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(76);
            assertEquals("Cloud_Optical_Thickness_37_PCL", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(87);
            assertEquals("Cloud_Water_Path_37", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(98);
            assertEquals("Cloud_Water_Path_Uncertainty_1621", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(107);
            assertEquals("Cloud_Mask_5km", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_Terra() throws IOException, InvalidRangeException {
        final File file = getTerraFile();

        try {
            reader.open(file);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(34, 108, new Interval(3, 5));
            assertEquals(15, acquisitionTime.getSize());

            // one scan
            NCTestUtils.assertValueAt(1360161352, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1360161352, 1, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1360161352, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1360161352, 2, 1, acquisitionTime);

            // next scan
            NCTestUtils.assertValueAt(1360161353, 1, 2, acquisitionTime);
            NCTestUtils.assertValueAt(1360161353, 2, 2, acquisitionTime);
            NCTestUtils.assertValueAt(1360161353, 1, 3, acquisitionTime);
            NCTestUtils.assertValueAt(1360161353, 2, 3, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_Aqua() throws IOException, InvalidRangeException {
        final File file = getAquaFile();

        try {
            reader.open(file);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(36, 211, new Interval(5, 5));
            assertEquals(25, acquisitionTime.getSize());

            // first scan
            NCTestUtils.assertValueAt(1242211027, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1242211027, 1, 0, acquisitionTime);

            // next scan
            NCTestUtils.assertValueAt(1242211029, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1242211029, 2, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1242211029, 1, 2, acquisitionTime);
            NCTestUtils.assertValueAt(1242211029, 2, 2, acquisitionTime);

            // next scan
            NCTestUtils.assertValueAt(1242211030, 1, 3, acquisitionTime);
            NCTestUtils.assertValueAt(1242211030, 2, 3, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_Aqua() throws IOException, InvalidRangeException {
        final File file = getAquaFile();

        try {
            reader.open(file);
            final List<Variable> variables = reader.getVariables();
            assertEquals(112, variables.size());

            Variable variable = variables.get(1);
            assertEquals("Longitude", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());

            variable = variables.get(5);
            assertEquals("Solar_Zenith_Night", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(9);
            assertEquals("Sensor_Zenith", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(16);
            assertEquals("Surface_Pressure", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(22);
            assertEquals("Cloud_Top_Pressure", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(33);
            assertEquals("Cloud_Top_Temperature_Nadir_Day", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(44);
            assertEquals("Cloud_Effective_Emissivity_Nadir_Night", variable.getShortName());
            assertEquals(DataType.BYTE, variable.getDataType());

            variable = variables.get(55);
            assertEquals("cloud_top_pressure_1km", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(66);
            assertEquals("Cloud_Effective_Radius_PCL", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(77);
            assertEquals("Cloud_Effective_Radius_1621", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(88);
            assertEquals("Cloud_Water_Path_37_PCL", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(99);
            assertEquals("Cloud_Water_Path_Uncertainty_16", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(108);
            assertEquals("Quality_Assurance_5km_03", variable.getShortName());
            assertEquals(DataType.BYTE, variable.getDataType());

            variable = variables.get(111);
            assertEquals("Quality_Assurance_5km_09", variable.getShortName());
            assertEquals(DataType.BYTE, variable.getDataType());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_5km_Aqua() throws IOException, InvalidRangeException {
        final File file = getAquaFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(45, 88, new Interval(3, 3), "Scan_Start_Time");
            NCTestUtils.assertValueAt(5.16364571722655E8, 0, 0, array);
            NCTestUtils.assertValueAt(5.16364571722655E8, 1, 0, array);

            NCTestUtils.assertValueAt(5.16364573199765E8, 1, 1, array);
            NCTestUtils.assertValueAt(5.16364573199765E8, 2, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_5km_Terra() throws IOException, InvalidRangeException {
        final File file = getTerraFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(46, 89, new Interval(3, 3), "Solar_Zenith");
            NCTestUtils.assertValueAt(6372, 0, 0, array);
            NCTestUtils.assertValueAt(6368, 1, 0, array);

            NCTestUtils.assertValueAt(6360, 1, 2, array);
            NCTestUtils.assertValueAt(6356, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_5km_Terra_upperLeft() throws IOException, InvalidRangeException {
        final File file = getTerraFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(0, 0, new Interval(3, 3), "Solar_Zenith_Day");
            NCTestUtils.assertValueAt(-32768, 0, 0, array);
            NCTestUtils.assertValueAt(-32768, 1, 0, array);

            NCTestUtils.assertValueAt(-32768, 0, 1, array);
            NCTestUtils.assertValueAt(7009, 1, 1, array);
            NCTestUtils.assertValueAt(6998, 2, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_5km_Aqua_upperRight() throws IOException, InvalidRangeException {
        final File file = getAquaFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(269, 0, new Interval(3, 3), "Solar_Zenith_Night");
            NCTestUtils.assertValueAt(-32768, 0, 0, array);
            NCTestUtils.assertValueAt(-32768, 1, 0, array);
            NCTestUtils.assertValueAt(-32768, 2, 0, array);

            NCTestUtils.assertValueAt(9284, 0, 1, array);
            NCTestUtils.assertValueAt(9276, 1, 1, array);
            NCTestUtils.assertValueAt(-32768, 2, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_5km_Terra_lowerRight() throws IOException, InvalidRangeException {
        final File file = getTerraFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(269, 405, new Interval(3, 3), "Solar_Azimuth");
            NCTestUtils.assertValueAt(16553, 0, 0, array);
            NCTestUtils.assertValueAt(16581, 1, 0, array);
            NCTestUtils.assertValueAt(-32768, 2, 0, array);

            NCTestUtils.assertValueAt(16547, 0, 1, array);
            NCTestUtils.assertValueAt(16576, 1, 1, array);
            NCTestUtils.assertValueAt(-32768, 2, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_5km_Aqua_lowerLeft() throws IOException, InvalidRangeException {
        final File file = getAquaFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(0, 405, new Interval(3, 3), "Solar_Azimuth_Day");
            NCTestUtils.assertValueAt(-32768, 0, 0, array);
            NCTestUtils.assertValueAt(-5067, 1, 0, array);
            NCTestUtils.assertValueAt(-5041, 2, 0, array);

            NCTestUtils.assertValueAt(-32768, 0, 1, array);
            NCTestUtils.assertValueAt(-5062, 1, 1, array);
            NCTestUtils.assertValueAt(-5036, 2, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_1km_Terra() throws IOException, InvalidRangeException {
        final File file = getTerraFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(1, 1, new Interval(3, 3), "Cloud_Effective_Radius");
            NCTestUtils.assertValueAt(2563, 0, 0, array);
            NCTestUtils.assertValueAt(3277, 1, 0, array);

            NCTestUtils.assertValueAt(-9999, 1, 2, array);
            NCTestUtils.assertValueAt(4151, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_1km_Aqua() throws IOException, InvalidRangeException {
        final File file = getAquaFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(174, 389, new Interval(3, 3), "Cloud_Water_Path");
            NCTestUtils.assertValueAt(266, 0, 0, array);
            NCTestUtils.assertValueAt(-9999, 1, 0, array);

            NCTestUtils.assertValueAt(17, 1, 2, array);
            NCTestUtils.assertValueAt(-9999, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_1km_Aqua_upperLeft() throws IOException, InvalidRangeException {
        final File file = getAquaFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(0, 0, new Interval(3, 3), "Cloud_Phase_Infrared_1km");
            NCTestUtils.assertValueAt(127, 0, 0, array);
            NCTestUtils.assertValueAt(127, 1, 0, array);
            NCTestUtils.assertValueAt(127, 2, 0, array);

            NCTestUtils.assertValueAt(127, 0, 1, array);
            NCTestUtils.assertValueAt(2, 1, 1, array);
            NCTestUtils.assertValueAt(2, 2, 1, array);

            NCTestUtils.assertValueAt(127, 0, 2, array);
            NCTestUtils.assertValueAt(2, 1, 2, array);
            NCTestUtils.assertValueAt(2, 2, 2, array);
        } finally {
            reader.close();
        }
    }


    @Test
    public void testReadRaw_1km_Terra_upperRight() throws IOException, InvalidRangeException {
        final File file = getTerraFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(269, 0, new Interval(3, 3), "IRP_CTH_Consistency_Flag_1km");
            NCTestUtils.assertValueAt(127, 0, 0, array);
            NCTestUtils.assertValueAt(127, 1, 0, array);
            NCTestUtils.assertValueAt(127, 2, 0, array);

            NCTestUtils.assertValueAt(0, 0, 1, array);
            NCTestUtils.assertValueAt(0, 1, 1, array);
            NCTestUtils.assertValueAt(0, 2, 1, array);  // due to subsampling geometries, this one is also inside the raster tb 2017-08-30

            NCTestUtils.assertValueAt(0, 0, 2, array);
            NCTestUtils.assertValueAt(0, 1, 2, array);
            NCTestUtils.assertValueAt(0, 2, 2, array);  // due to subsampling geometries, this one is also inside the raster tb 2017-08-30
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_1km_Aqua_lowerRight() throws IOException, InvalidRangeException {
        final File file = getAquaFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(269, 405, new Interval(3, 3), "os_top_flag_1km");
            NCTestUtils.assertValueAt(0, 0, 0, array);
            NCTestUtils.assertValueAt(0, 1, 0, array);
            NCTestUtils.assertValueAt(0, 2, 0, array);  // due to subsampling geometries, this one is also inside the raster tb 2017-08-30

            NCTestUtils.assertValueAt(0, 0, 1, array);
            NCTestUtils.assertValueAt(0, 1, 1, array);
            NCTestUtils.assertValueAt(0, 2, 1, array);  // due to subsampling geometries, this one is also inside the raster tb 2017-08-30

            NCTestUtils.assertValueAt(127, 0, 2, array);
            NCTestUtils.assertValueAt(127, 1, 2, array);
            NCTestUtils.assertValueAt(127, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_1km_Terra_lowerLeft() throws IOException, InvalidRangeException {
        final File file = getTerraFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(0, 405, new Interval(3, 3), "cloud_top_pressure_1km");
            NCTestUtils.assertValueAt(-999, 0, 0, array);
            NCTestUtils.assertValueAt(5550, 1, 0, array);
            NCTestUtils.assertValueAt(2250, 2, 0, array);

            NCTestUtils.assertValueAt(-999, 0, 1, array);
            NCTestUtils.assertValueAt(4600, 1, 1, array);
            NCTestUtils.assertValueAt(4800, 2, 1, array);

            NCTestUtils.assertValueAt(-999, 0, 2, array);
            NCTestUtils.assertValueAt(-999, 1, 2, array);
            NCTestUtils.assertValueAt(-999, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_5km_Terra() throws IOException, InvalidRangeException {
        final File file = getTerraFile();

        try {
            reader.open(file);

            final Array array = reader.readScaled(122, 372, new Interval(3, 3), "Cloud_Effective_Emissivity_Nadir_Day");
            NCTestUtils.assertValueAt(0.9999999776482582, 0, 0, array);
            NCTestUtils.assertValueAt(0.9999999776482582, 1, 0, array);
            NCTestUtils.assertValueAt(0.9999999776482582, 2, 0, array);

            NCTestUtils.assertValueAt(0.9999999776482582, 0, 1, array);
            NCTestUtils.assertValueAt(0.9999999776482582, 1, 1, array);
            NCTestUtils.assertValueAt(0.9599999785423279, 2, 1, array);

            NCTestUtils.assertValueAt(0.9199999794363976, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_5km_Aqua() throws IOException, InvalidRangeException {
        final File file = getAquaFile();

        try {
            reader.open(file);

            final Array array = reader.readScaled(163, 388, new Interval(3, 3), "Sensor_Zenith_Day");
            NCTestUtils.assertValueAt(12.309999724850059, 0, 0, array);
            NCTestUtils.assertValueAt(12.759999714791775, 1, 0, array);
            NCTestUtils.assertValueAt(13.219999704509974, 2, 0, array);

            NCTestUtils.assertValueAt(12.309999724850059, 0, 1, array);
            NCTestUtils.assertValueAt(12.759999714791775, 1, 1, array);
            NCTestUtils.assertValueAt(13.219999704509974, 2, 1, array);

            NCTestUtils.assertValueAt(13.209999704733491, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_1km_Terra() throws IOException, InvalidRangeException {
        final File file = getTerraFile();

        try {
            reader.open(file);

            final Array array = reader.readScaled(131, 323, new Interval(3, 3), "cloud_top_pressure_1km");
            NCTestUtils.assertValueAt(315.0000046938658, 0, 0, array);
            NCTestUtils.assertValueAt(360.00000536441803, 1, 0, array);
            NCTestUtils.assertValueAt(905.0000134855509, 2, 0, array);

            NCTestUtils.assertValueAt(390.00000581145287, 0, 1, array);
            NCTestUtils.assertValueAt(960.0000143051147, 1, 1, array);
            NCTestUtils.assertValueAt(985.0000146776438, 2, 1, array);

            NCTestUtils.assertValueAt(985.0000146776438, 0, 2, array);
            NCTestUtils.assertValueAt(985.0000146776438, 1, 2, array);
            NCTestUtils.assertValueAt(-99.900001488626, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_1km_Aqua() throws IOException, InvalidRangeException {
        final File file = getAquaFile();

        try {
            reader.open(file);

            final Array array = reader.readScaled(101, 182, new Interval(3, 3), "cloud_top_temperature_1km");
            NCTestUtils.assertValueAt(-14901.29000220634, 0, 0, array);
            NCTestUtils.assertValueAt(-15009.989999776706, 1, 0, array);
            NCTestUtils.assertValueAt(-15009.989999776706, 2, 0, array);

            NCTestUtils.assertValueAt(-14899.67000224255, 0, 1, array);
            NCTestUtils.assertValueAt(-14899.050002256408, 1, 1, array);
            NCTestUtils.assertValueAt(-15009.989999776706, 2, 1, array);

            NCTestUtils.assertValueAt(-14902.700002174824, 0, 2, array);
            NCTestUtils.assertValueAt(-14901.8600021936, 1, 2, array);
            NCTestUtils.assertValueAt(-14900.850002216175, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_Cloud_Mask_5km_Aqua() throws IOException, InvalidRangeException {
        final File file = getAquaFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(102, 183, new Interval(3, 3), "Cloud_Mask_5km");
            NCTestUtils.assertValueAt(4889, 0, 0, array);
            NCTestUtils.assertValueAt(5913, 1, 0, array);
            NCTestUtils.assertValueAt(5888, 2, 0, array);

            NCTestUtils.assertValueAt(4377, 0, 1, array);
            NCTestUtils.assertValueAt(4889, 1, 1, array);
            NCTestUtils.assertValueAt(4889, 2, 1, array);

            NCTestUtils.assertValueAt(4889, 0, 2, array);
            NCTestUtils.assertValueAt(4377, 1, 2, array);
            NCTestUtils.assertValueAt(4377, 2, 2, array);

            // test that the shifting is correct
            final Index index = array.getIndex();
            final short firstValue = array.getShort(index);
            assertEquals(4889, firstValue);
            assertEquals(19, (byte)((firstValue & 0xFF00) >> 8));
            assertEquals(25, (byte)(firstValue & 0x00FF));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_Cloud_Mask_5km_Terra() throws IOException, InvalidRangeException {
        final File file = getTerraFile();

        try {
            reader.open(file);

            final Array array = reader.readRaw(103, 284, new Interval(3, 3), "Cloud_Mask_5km");
            NCTestUtils.assertValueAt(14741, 0, 0, array);
            NCTestUtils.assertValueAt(14741, 1, 0, array);
            NCTestUtils.assertValueAt(14741, 2, 0, array);

            NCTestUtils.assertValueAt(14741, 0, 1, array);
            NCTestUtils.assertValueAt(14741, 1, 1, array);
            NCTestUtils.assertValueAt(14741, 2, 1, array);

            NCTestUtils.assertValueAt(14741, 0, 2, array);
            NCTestUtils.assertValueAt(14741, 1, 2, array);
            NCTestUtils.assertValueAt(14741, 2, 2, array);

            // test that the shifting is correct
            final Index index = array.getIndex();
            final short firstValue = array.getShort(index);
            assertEquals(14741, firstValue);
            assertEquals(57, (byte)((firstValue & 0xFF00) >> 8));
            assertEquals(-107, (byte)(firstValue & 0x00FF));
        } finally {
            reader.close();
        }
    }

    private File getTerraFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"mod06-te", "v006", "2013", "037", "MOD06_L2.A2013037.1435.006.2015066015540.hdf"}, false);
        return getFileAsserted(testFilePath);
    }

    private File getAquaFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"myd06-aq", "v006", "2009", "133", "MYD06_L2.A2009133.1035.006.2014062050327.hdf"}, false);
        return getFileAsserted(testFilePath);
    }

    private File getFileAsserted(String testFilePath) {
        final File file = new File(dataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}

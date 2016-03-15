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

package com.bc.fiduceo.reader;


import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.TimeAxis;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.Index;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class AVHRR_GAC_Reader_IO_Test {

    private File testDataDirectory;
    private AVHRR_GAC_Reader reader;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory();
        reader = new AVHRR_GAC_Reader();
    }

    @Test
    public void testReadAcquisitionInfo_NOAA17() throws IOException {
        final File file = createAvhrrNOAA17Path();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 3, 34, 54, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 5, 28, 48, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) boundingGeometry;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);

            Point[] coordinates = geometries[0].getCoordinates();
            assertEquals(161, coordinates.length);
            assertEquals(-66.97299194335938, coordinates[0].getLon(), 1e-8);
            assertEquals(-5.238999843597412, coordinates[0].getLat(), 1e-8);

            assertEquals(-74.2349853515625, coordinates[23].getLon(), 1e-8);
            assertEquals(61.316001892089844, coordinates[23].getLat(), 1e-8);

            coordinates = geometries[1].getCoordinates();
            assertEquals(161, coordinates.length);
            assertEquals(69.81500244140625, coordinates[0].getLon(), 1e-8);
            assertEquals(-12.913000106811522, coordinates[0].getLat(), 1e-8);

            assertEquals(7.164999961853029, coordinates[23].getLon(), 1e-8);
            assertEquals(-67.44300079345703, coordinates[23].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = geometries[0].getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 3, 34, 54, 0, time);

            coordinates = geometries[1].getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 4, 32, 4, 754, time);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_NOAA17() throws IOException {
        final File file = createAvhrrNOAA17Path();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(17, variables.size());

            Variable variable = variables.get(0);
            assertEquals("lat", variable.getShortName());

            variable = variables.get(7);
            assertEquals("ch4", variable.getShortName());

            variable = variables.get(16);
            assertEquals("l1b_line_number", variable.getShortName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_NOAA18() throws IOException {
        final File file = createAvhrrNOAA18Path();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 8, 4, 12, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 9, 46, 11, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) boundingGeometry;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);

            Point[] coordinates = geometries[0].getCoordinates();
            assertEquals(147, coordinates.length);
            assertEquals(-110.22799682617189, coordinates[17].getLon(), 1e-8);
            assertEquals(38.00899887084961, coordinates[17].getLat(), 1e-8);

            assertEquals(177.14199829101562, coordinates[58].getLon(), 1e-8);
            assertEquals(-66.05000305175781, coordinates[58].getLat(), 1e-8);

            coordinates = geometries[1].getCoordinates();
            assertEquals(147, coordinates.length);
            assertEquals(153.96200561523438, coordinates[0].getLon(), 1e-8);
            assertEquals(-66.73899841308594, coordinates[0].getLat(), 1e-8);

            assertEquals(83.5780029296875, coordinates[23].getLon(), 1e-8);
            assertEquals(-16.940000534057617, coordinates[23].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = geometries[0].getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 8, 4, 13, 616, time);

            coordinates = geometries[1].getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 8, 55, 11, 500, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_NOAA18() throws IOException {
        final File file = createAvhrrNOAA18Path();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(17, variables.size());

            Variable variable = variables.get(1);
            assertEquals("lon", variable.getShortName());

            variable = variables.get(8);
            assertEquals("ch5", variable.getShortName());

            variable = variables.get(15);
            assertEquals("cloud_probability", variable.getShortName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testWindowCenter() throws Exception {
        final File file = createAvhrrNOAA18Path();

        reader.open(file);
        try {
            Array array = reader.readRaw(4, 4, new Interval(3, 3), "lon");
            assertNotNull(array);
            assertEquals(9, array.getSize());

            final Index index = array.getIndex();
            index.set(0, 0);
            assertEquals(-152.41099548339844, array.getDouble(index), 1e-8);

            assertEqualValue(-152.41099548339844,0,0,array);

            index.set(0, 1);
            assertEquals(-151.1510009765625, array.getDouble(index), 1e-8);

            index.set(0, 2);
            assertEquals(-149.8489990234375, array.getDouble(index), 1e-8);

            index.set(1, 0);
            assertEquals(-152.1490020751953, array.getDouble(index), 1e-8);

            index.set(1, 1);
            assertEquals(-150.88499450683594, array.getDouble(index), 1e-8);

            index.set(1, 2);
            assertEquals(-149.57899475097656, array.getDouble(index), 1e-8);

            index.set(2, 0);
            assertEquals(-151.88999938964844, array.getDouble(index), 1e-8);

            index.set(2, 1);
            assertEquals(-150.62100219726562, array.getDouble(index), 1e-8);

            index.set(2, 2);
            assertEquals(-149.31100463867188, array.getDouble(index), 1e-8);
        } finally {
            reader.close();
        }
    }



    private void assertEqualValue(double value, int i, int j, Array array) {
        Index index = array.getIndex();
        index.set(i, j);
        assertEquals(value, array.getDouble(index), 1e-8);
    }

    @Test
    public void testBottomWindowOut() throws Exception {
        try {
            File avhrrNOAA18Path = createAvhrrNOAA18Path();
            reader.open(avhrrNOAA18Path);
            Array array = reader.readRaw(5, 12235, new Interval(3, 13), "lon");
            assertNotNull(array);
            assertEquals(39, array.getSize());

            final Index index = array.getIndex();
            index.set(0, 0);
            assertEquals(174.82699584960938, array.getDouble(index), 1e-8);

            index.set(0, 1);
            assertEquals(175.9340057373047, array.getDouble(index), 1e-8);

            index.set(0, 2);
            assertEquals(177.09300231933594, array.getDouble(index), 1e-8);

            index.set(12, 0);
            assertEquals(-32768.0, array.getDouble(index), 1e-8);

            index.set(12, 1);
            assertEquals(-32768.0, array.getDouble(index), 1e-8);

            index.set(12, 1);
            assertEquals(-32768.0, array.getDouble(index), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testTopWindowOut() throws Exception {

        try {
            File avhrrNOAA18Path = createAvhrrNOAA18Path();
            reader.open(avhrrNOAA18Path);
            Array array = reader.readRaw(2, 1, new Interval(3, 5), "lon");
            assertNotNull(array);
            assertEquals(15, array.getSize());

            final Index index = array.getIndex();
            index.set(0, 0);
            assertEquals(-32768.0, array.getDouble(index), 1e-8);

            index.set(0, 1);
            assertEquals(-32768.0, array.getDouble(index), 1e-8);

            index.set(0, 2);
            assertEquals(-32768.0, array.getDouble(index), 1e-8);


            index.set(1, 0);
            assertEquals(-155.5709991455078, array.getDouble(index), 1e-8);
            index.set(1, 1);
            assertEquals(-154.40899658203125, array.getDouble(index), 1e-8);
            index.set(1, 2);
            assertEquals(-153.20599365234375, array.getDouble(index), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testLeftWindowOut() throws Exception {
        try {
            File avhrrNOAA18Path = createAvhrrNOAA18Path();
            reader.open(avhrrNOAA18Path);

            ArrayFloat.D2 array = (ArrayFloat.D2) reader.readRaw(2, 10, new Interval(9, 9), "lon");
            assertNotNull(array);
            assertEquals(81, array.getSize());

            final Index index = array.getIndex();
            System.out.println("array = " + array);

            index.set(0, 0);
            assertEquals(-32768.0, array.getDouble(index), 1e-8);
            index.set(0, 1);
            assertEquals(-32768.0, array.getDouble(index), 1e-8);
            index.set(0, 2);
            assertEquals(-155.2050018310547, array.getDouble(index), 1e-8);
            index.set(0, 3);
            assertEquals(-154.05299377441406, array.getDouble(index), 1e-8);
            index.set(0, 4);
            assertEquals(-152.86199951171875, array.getDouble(index), 1e-8);
            index.set(0, 5);
            assertEquals(-151.63099670410156, array.getDouble(index), 1e-8);
            index.set(0, 6);
            assertEquals(-150.35899353027344, array.getDouble(index), 1e-8);
            index.set(0, 7);
            assertEquals(-149.0449981689453, array.getDouble(index), 1e-8);
            index.set(0, 8);
            assertEquals(-147.68899536132812, array.getDouble(index), 1e-8);


            assertEquals(-32768.0, array.get(1, 0), 1e-8);
            assertEquals(-32768.0, array.get(2, 0), 1e-8);
            assertEquals(-32768.0, array.get(3, 0), 1e-8);
            assertEquals(-32768.0, array.get(4, 0), 1e-8);
            assertEquals(-32768.0, array.get(5, 0), 1e-8);
            assertEquals(-32768.0, array.get(6, 0), 1e-8);
        } finally {
            reader.close();

        }
    }

    @Test
    public void testRightWindowOut() throws Exception {
        try {
            File avhrrNOAA18Path = createAvhrrNOAA18Path();
            reader.open(avhrrNOAA18Path);

            ArrayFloat.D2 array = (ArrayFloat.D2) reader.readRaw(407, 10, new Interval(9, 9), "lon");
            assertNotNull(array);
            assertEquals(81, array.getSize());
            assertEquals(-14.597991943359375, array.get(0, 0), 1e-8);
            assertEquals(-14.52899169921875, array.get(0, 1), 1e-8);
            assertEquals(-14.386993408203125, array.get(0, 3), 1e-8);
            assertEquals(-14.31500244140625, array.get(0, 4), 1e-8);
            assertEquals(-14.24200439453125, array.get(0, 5), 1e-8);

            assertEquals(-32768.0, array.get(0, 8), 1e-8);
            assertEquals(-32768.0, array.get(1, 8), 1e-8);
            assertEquals(-32768.0, array.get(2, 8), 1e-8);
            assertEquals(-32768.0, array.get(3, 8), 1e-8);
            assertEquals(-32768.0, array.get(4, 8), 1e-8);
            assertEquals(-32768.0, array.get(5, 8), 1e-8);
            assertEquals(-32768.0, array.get(6, 8), 1e-8);
            assertEquals(-32768.0, array.get(7, 8), 1e-8);
            assertEquals(-32768.0, array.get(8, 8), 1e-8);
        } finally {

            reader.close();
        }
    }


    @Test
    public void testTopLeftWindowInOut() throws Exception {
        try {
            File avhrrNOAA18Path = createAvhrrNOAA18Path();
            reader.open(avhrrNOAA18Path);

            ArrayFloat.D2 array = (ArrayFloat.D2) reader.readRaw(2, 3, new Interval(9, 9), "lon");
            assertNotNull(array);
            assertEquals(81, array.getSize());


            assertEquals(-32768.0, array.get(0, 0), 1e-8);
            assertEquals(-32768.0, array.get(0, 1), 1e-8);
            assertEquals(-32768.0, array.get(0, 2), 1e-8);
            assertEquals(-32768.0, array.get(0, 3), 1e-8);
            assertEquals(-32768.0, array.get(0, 4), 1e-8);
            assertEquals(-32768.0, array.get(0, 5), 1e-8);
            assertEquals(-32768.0, array.get(0, 6), 1e-8);
            assertEquals(-32768.0, array.get(0, 7), 1e-8);
            assertEquals(-32768.0, array.get(0, 8), 1e-8);

            assertEquals(-155.5709991455078, array.get(1, 3), 1e-8);
            assertEquals(-154.40899658203125, array.get(1, 4), 1e-8);
            assertEquals(-153.20599365234375, array.get(1, 5), 1e-8);

            assertEquals(-32768.0, array.get(0, 0), 1e-8);
            assertEquals(-32768.0, array.get(1, 0), 1e-8);
            assertEquals(-32768.0, array.get(2, 0), 1e-8);
            assertEquals(-32768.0, array.get(3, 0), 1e-8);
            assertEquals(-32768.0, array.get(4, 0), 1e-8);
            assertEquals(-32768.0, array.get(5, 0), 1e-8);
            assertEquals(-32768.0, array.get(6, 0), 1e-8);
            assertEquals(-32768.0, array.get(7, 0), 1e-8);
            assertEquals(-32768.0, array.get(8, 0), 1e-8);

            assertEquals(-32768.0, array.get(0, 1), 1e-8);
            assertEquals(-32768.0, array.get(1, 1), 1e-8);
            assertEquals(-32768.0, array.get(2, 1), 1e-8);
            assertEquals(-32768.0, array.get(3, 1), 1e-8);
            assertEquals(-32768.0, array.get(4, 1), 1e-8);
            assertEquals(-32768.0, array.get(5, 1), 1e-8);
            assertEquals(-32768.0, array.get(6, 1), 1e-8);
            assertEquals(-32768.0, array.get(7, 1), 1e-8);
            assertEquals(-32768.0, array.get(8, 1), 1e-8);
        } finally {

            reader.close();
        }
    }


    @Test
    public void testBottomRightWindowInOut() throws Exception {
        try {
            File avhrrNOAA18Path = createAvhrrNOAA18Path();
            reader.open(avhrrNOAA18Path);

            ArrayFloat.D2 array = (ArrayFloat.D2) reader.readRaw(405, 12235, new Interval(9, 9), "lon");
            assertNotNull(array);
            assertEquals(81, array.getSize());


            assertEquals(-32768.0, array.get(8, 0), 1e-8);
            assertEquals(-32768.0, array.get(8, 1), 1e-8);
            assertEquals(-32768.0, array.get(8, 2), 1e-8);
            assertEquals(-32768.0, array.get(8, 3), 1e-8);
            assertEquals(-32768.0, array.get(8, 4), 1e-8);
            assertEquals(-32768.0, array.get(8, 5), 1e-8);
            assertEquals(-32768.0, array.get(8, 6), 1e-8);
            assertEquals(-32768.0, array.get(8, 7), 1e-8);
            assertEquals(-32768.0, array.get(8, 8), 1e-8);

            assertEquals(-37.912994384765625, array.get(1, 3), 1e-8);
            assertEquals(-37.860992431640625, array.get(1, 4), 1e-8);
            assertEquals(-37.808013916015625, array.get(1, 5), 1e-8);

            assertEquals(-32768.0, array.get(0, 8), 1e-8);
            assertEquals(-32768.0, array.get(1, 8), 1e-8);
            assertEquals(-32768.0, array.get(2, 8), 1e-8);
            assertEquals(-32768.0, array.get(3, 8), 1e-8);
            assertEquals(-32768.0, array.get(4, 8), 1e-8);
            assertEquals(-32768.0, array.get(5, 8), 1e-8);
            assertEquals(-32768.0, array.get(6, 8), 1e-8);
            assertEquals(-32768.0, array.get(7, 8), 1e-8);
            assertEquals(-32768.0, array.get(8, 8), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    @Ignore
    public void testBottomLeftWindowInOut() throws Exception {
        try {
            File avhrrNOAA18Path = createAvhrrNOAA18Path();
            reader.open(avhrrNOAA18Path);

            ArrayFloat.D2 array = (ArrayFloat.D2) reader.readRaw(2, 12235, new Interval(9, 9), "lon");
            assertNotNull(array);
            assertEquals(81, array.getSize());


            assertEquals(-32768.0, array.get(8, 0), 1e-8);
            assertEquals(-32768.0, array.get(8, 1), 1e-8);
            assertEquals(-32768.0, array.get(8, 2), 1e-8);
            assertEquals(-32768.0, array.get(8, 3), 1e-8);
            assertEquals(-32768.0, array.get(8, 4), 1e-8);
            assertEquals(-32768.0, array.get(8, 5), 1e-8);
            assertEquals(-32768.0, array.get(8, 6), 1e-8);
            assertEquals(-32768.0, array.get(8, 7), 1e-8);
            assertEquals(-32768.0, array.get(8, 8), 1e-8);

            assertEquals(172.6439971923828, array.get(1, 3), 1e-8);
            assertEquals(173.63999938964844, array.get(1, 4), 1e-8);
            assertEquals(174.6790008544922, array.get(1, 5), 1e-8);

            assertEquals(-32768.0, array.get(0, 0), 1e-8);
            assertEquals(-32768.0, array.get(1, 0), 1e-8);
            assertEquals(-32768.0, array.get(2, 0), 1e-8);
            assertEquals(-32768.0, array.get(3, 0), 1e-8);
            assertEquals(-32768.0, array.get(4, 0), 1e-8);
            assertEquals(-32768.0, array.get(5, 0), 1e-8);
            assertEquals(-32768.0, array.get(6, 0), 1e-8);
            assertEquals(-32768.0, array.get(7, 0), 1e-8);
            assertEquals(-32768.0, array.get(8, 0), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testTopRightWindowInOut() throws Exception {
        try {
            File avhrrNOAA18Path = createAvhrrNOAA18Path();
            reader.open(avhrrNOAA18Path);

            ArrayFloat.D2 array = (ArrayFloat.D2) reader.readRaw(407, 3, new Interval(9, 9), "lat");
            assertNotNull(array);
            assertEquals(81, array.getSize());


            assertEquals(-32768.0, array.get(0, 0), 1e-8);
            assertEquals(-32768.0, array.get(0, 1), 1e-8);
            assertEquals(-32768.0, array.get(0, 2), 1e-8);
            assertEquals(-32768.0, array.get(0, 3), 1e-8);
            assertEquals(-32768.0, array.get(0, 4), 1e-8);
            assertEquals(-32768.0, array.get(0, 5), 1e-8);
            assertEquals(-32768.0, array.get(0, 6), 1e-8);
            assertEquals(-32768.0, array.get(0, 7), 1e-8);
            assertEquals(-32768.0, array.get(0, 8), 1e-8);

            assertEquals(68.71700286865234, array.get(1, 0), 1e-8);
            assertEquals(68.54100036621094, array.get(1, 1), 1e-8);
            assertEquals(68.1729965209961, array.get(1, 3), 1e-8);
            assertEquals(67.98200225830078, array.get(1, 4), 1e-8);
            assertEquals(67.78500366210938, array.get(1, 5), 1e-8);

            assertEquals(-32768.0, array.get(0, 8), 1e-8);
            assertEquals(-32768.0, array.get(1, 8), 1e-8);
            assertEquals(-32768.0, array.get(2, 8), 1e-8);
            assertEquals(-32768.0, array.get(3, 8), 1e-8);
            assertEquals(-32768.0, array.get(4, 8),

                    1e-8);
            assertEquals(-32768.0, array.get(5, 8), 1e-8);
            assertEquals(-32768.0, array.get(6, 8), 1e-8);
            assertEquals(-32768.0, array.get(7, 8), 1e-8);
            assertEquals(-32768.0, array.get(8, 8), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testWindowWindowInside() throws Exception {
        try {
            File avhrrNOAA18Path = createAvhrrNOAA18Path();
            reader.open(avhrrNOAA18Path);

            ArrayFloat.D2 array = (ArrayFloat.D2) reader.readRaw(109, 100, new Interval(5, 7), "lon");
            assertEquals(35, array.getSize());
            assertEquals("-56.211 -55.936005 -55.664 -55.397003 -55.132996 -56.360992 -56.086 -55.813995 -55.546997 -55.28299 -56.509003 -56.23401 -55.963013 -55.696014 -55.432007 -56.657013 -56.38199 -56.110992 -55.843994 -55.580994 -56.803986 -56.52899 -56.259003 -55.992004 -55.727997 -56.950012 -56.675995 -56.405 -56.138 -55.875 -57.095 -56.821014 -56.550995 -56.283997 -56.020996 ", array.toString());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testCloud_Mask() throws Exception {
        try {
            File avhrrNOAA18Path = createAvhrrNOAA18Path();
            reader.open(avhrrNOAA18Path);
            Array array = reader.readRaw(3, 0, new Interval(5, 3), "cloud_mask");
            System.out.println("array = " + array);
            fail();
        } catch (RuntimeException expeated) {

        } finally {
            reader.close();
        }
    }

    private File createAvhrrNOAA17Path() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n17", "1.01", "2007", "04", "01", "20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }

    private File createAvhrrNOAA18Path() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n18", "1.02", "2007", "04", "01", "20070401080400-ESACCI-L1C-AVHRR18_G-fv01.0.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}

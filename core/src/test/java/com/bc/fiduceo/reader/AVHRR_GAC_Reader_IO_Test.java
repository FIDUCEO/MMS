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
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.Index;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    public void testWindowCenter() throws Exception {
        final File file = createAvhrrNOAA18Path();
        reader.open(file);
        try {
            Array array = reader.readRaw(4, 4, new Interval(3, 3), "lon");
            assertNotNull(array);
            assertEquals(9, array.getSize());

            assertEqualDoubleValueFromArray(-152.41099548339844, 0, 0,array);
            assertEqualDoubleValueFromArray(-151.1510009765625, 0, 1, array);
            assertEqualDoubleValueFromArray(-149.8489990234375, 0, 2, array);

            assertEqualDoubleValueFromArray(-150.88499450683594, 1, 1, array);
            assertEqualDoubleValueFromArray(-149.57899475097656, 1, 2, array);
            assertEqualDoubleValueFromArray(-149.57899475097656, 1, 2, array);
            assertEqualDoubleValueFromArray(-151.88999938964844, 2, 0, array);
            assertEqualDoubleValueFromArray(-150.62100219726562, 2, 1, array);
            assertEqualDoubleValueFromArray(-149.31100463867188, 2, 2, array);
        } finally {
            reader.close();
        }
    }


    private void assertEqualDoubleValueFromArray(double value, int i, int j, Array array) {
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

            assertEqualDoubleValueFromArray(174.82699584960938, 0, 0, array);
            assertEqualDoubleValueFromArray(175.9340057373047, 0, 1, array);
            assertEqualDoubleValueFromArray(177.09300231933594, 0, 2, array);
            assertEqualDoubleValueFromArray(177.09300231933594, 0, 2, array);
            assertEqualDoubleValueFromArray(-32768.0, 12, 1, array);
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

            assertEqualDoubleValueFromArray(-32768.0, 0, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 1, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 2, array);

            assertEqualDoubleValueFromArray(-155.5709991455078, 1, 0, array);
            assertEqualDoubleValueFromArray(-154.40899658203125, 1, 1, array);
            assertEqualDoubleValueFromArray(-153.20599365234375, 1, 2, array);
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

            assertEqualDoubleValueFromArray(-32768.0, 0, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 1, array);

            assertEqualDoubleValueFromArray(-155.2050018310547, 0, 2, array);
            assertEqualDoubleValueFromArray(-154.05299377441406, 0, 3, array);
            assertEqualDoubleValueFromArray(-152.86199951171875, 0, 4, array);
            assertEqualDoubleValueFromArray(-151.63099670410156, 0, 5, array);
            assertEqualDoubleValueFromArray(-150.35899353027344, 0, 6, array);
            assertEqualDoubleValueFromArray(-149.0449981689453, 0, 7, array);
            assertEqualDoubleValueFromArray(-147.68899536132812, 0, 8, array);

            assertEqualDoubleValueFromArray(-32768.0, 1, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 2, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 3, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 4, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 5, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 6, 0, array);
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
            assertEqualDoubleValueFromArray(-14.597991943359375, 0, 0, array);
            assertEqualDoubleValueFromArray(-14.52899169921875, 0, 1, array);
            assertEqualDoubleValueFromArray(-14.386993408203125, 0, 3, array);
            assertEqualDoubleValueFromArray(-14.31500244140625, 0, 4, array);
            assertEqualDoubleValueFromArray(-14.24200439453125, 0, 5, array);

            assertEqualDoubleValueFromArray(-32768.0, 0, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 1, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 2, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 3, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 4, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 5, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 6, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 7, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 8, array);
        } finally {

            reader.close();
        }
    }


    @Test
    public void testTopLeftWindowOut() throws Exception {
        try {
            File avhrrNOAA18Path = createAvhrrNOAA18Path();
            reader.open(avhrrNOAA18Path);

            ArrayFloat.D2 array = (ArrayFloat.D2) reader.readRaw(2, 3, new Interval(9, 9), "lon");
            assertNotNull(array);
            assertEquals(81, array.getSize());

            assertEqualDoubleValueFromArray(-32768.0, 0, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 1, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 2, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 3, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 4, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 5, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 6, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 7, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 8, array);

            assertEqualDoubleValueFromArray(-155.5709991455078, 1, 3, array);
            assertEqualDoubleValueFromArray(-154.40899658203125, 1, 4, array);
            assertEqualDoubleValueFromArray(-153.20599365234375, 1, 5, array);

            assertEqualDoubleValueFromArray(-32768.0, 0, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 1, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 2, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 3, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 4, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 5, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 6, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 7, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 1, array);
            assertEqualDoubleValueFromArray(-32768.0, 1, 1, array);
            assertEqualDoubleValueFromArray(-32768.0, 2, 1, array);
            assertEqualDoubleValueFromArray(-32768.0, 3, 1, array);
            assertEqualDoubleValueFromArray(-32768.0, 4, 1, array);
            assertEqualDoubleValueFromArray(-32768.0, 5, 1, array);
            assertEqualDoubleValueFromArray(-32768.0, 6, 1, array);
            assertEqualDoubleValueFromArray(-32768.0, 7, 1, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 1, array);
        } finally {

            reader.close();
        }
    }


    @Test
    public void testBottomRightWindowOut() throws Exception {
        try {
            File avhrrNOAA18Path = createAvhrrNOAA18Path();
            reader.open(avhrrNOAA18Path);

            ArrayFloat.D2 array = (ArrayFloat.D2) reader.readRaw(405, 12235, new Interval(9, 9), "lon");
            assertNotNull(array);
            assertEquals(81, array.getSize());


            assertEqualDoubleValueFromArray(-32768.0, 8, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 1, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 2, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 3, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 4, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 5, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 6, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 7, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 8, array);

            assertEqualDoubleValueFromArray(-37.912994384765625, 1, 3, array);
            assertEqualDoubleValueFromArray(-37.860992431640625, 1, 4, array);
            assertEqualDoubleValueFromArray(-37.808013916015625, 1, 5, array);

            assertEqualDoubleValueFromArray(-32768.0, 0, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 1, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 2, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 3, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 4, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 5, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 6, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 7, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 8, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testBottomLeftWindowOut() throws Exception {
        try {
            File avhrrNOAA18Path = createAvhrrNOAA18Path();
            reader.open(avhrrNOAA18Path);

            ArrayFloat.D2 array = (ArrayFloat.D2) reader.readRaw(2, 12235, new Interval(9, 9), "lon");
            assertNotNull(array);
            assertEquals(81, array.getSize());


            assertEqualDoubleValueFromArray(-32768.0, 8, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 1, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 2, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 3, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 4, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 5, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 6, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 7, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 8, array);

            assertEqualDoubleValueFromArray(172.6439971923828, 1, 3, array);
            assertEqualDoubleValueFromArray(173.63999938964844, 1, 4, array);
            assertEqualDoubleValueFromArray(174.6790008544922, 1, 5, array);

            assertEqualDoubleValueFromArray(-32768.0, 0, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 1, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 2, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 3, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 4, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 5, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 6, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 7, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 0, array);
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

            assertEqualDoubleValueFromArray(-32768.0, 0, 0, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 1, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 2, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 3, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 4, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 5, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 6, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 7, array);
            assertEqualDoubleValueFromArray(-32768.0, 0, 8, array);

            assertEqualDoubleValueFromArray(68.71700286865234, 1, 0, array);
            assertEqualDoubleValueFromArray(68.54100036621094, 1, 1, array);
            assertEqualDoubleValueFromArray(68.1729965209961, 1, 3, array);
            assertEqualDoubleValueFromArray(67.98200225830078, 1, 4, array);
            assertEqualDoubleValueFromArray(67.78500366210938, 1, 5, array);

            assertEqualDoubleValueFromArray(-32768.0, 0, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 1, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 2, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 3, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 4, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 5, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 6, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 7, 8, array);
            assertEqualDoubleValueFromArray(-32768.0, 8, 8, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testWindowInside() throws Exception {
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
}

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

package com.bc.fiduceo.reader.hirs;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;
import ucar.nc2.iosp.netcdf3.N3iosp;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class HIRS_L1C_Reader_IO_Test {

    private File dataDirectory;
    private HIRS_L1C_Reader reader;

    @Before
    public void setUp() throws IOException {
        dataDirectory = TestUtil.getTestDataDirectory();
        reader = new HIRS_L1C_Reader(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testReadAcquisitionInfo_TIROSN() throws IOException {
        final File file = getTirosNFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1979, 10, 14, 16, 23, 59, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1979, 10, 14, 18, 7, 33, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) boundingGeometry;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);

            Point[] coordinates = geometries[0].getCoordinates();
            assertEquals(127, coordinates.length);
            assertEquals(-47.484375, coordinates[1].getLon(), 1e-8);
            assertEquals(56.96875, coordinates[1].getLat(), 1e-8);

            assertEquals(111.625, coordinates[63].getLon(), 1e-8);
            assertEquals(-59.0703125, coordinates[63].getLat(), 1e-8);

            coordinates = geometries[1].getCoordinates();
            assertEquals(127, coordinates.length);
            assertEquals(144.9609375, coordinates[2].getLon(), 1e-8);
            assertEquals(-65.484375, coordinates[2].getLat(), 1e-8);

            assertEquals(-43.484375, coordinates[64].getLon(), 1e-8);
            assertEquals(67.3671875, coordinates[64].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = geometries[0].getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1979, 10, 14, 16, 24, 1, 110, time);

            coordinates = geometries[1].getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1979, 10, 14, 17, 15, 46, 0, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_NOAA10() throws IOException {
        final File file = getNOAA10File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1989, 3, 17, 6, 12, 16, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1989, 3, 17, 8, 2, 2, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) boundingGeometry;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);

            Point[] coordinates = geometries[0].getCoordinates();
            assertEquals(133, coordinates.length);
            assertEquals(-175.1171875, coordinates[1].getLon(), 1e-8);
            assertEquals(34.8984375, coordinates[1].getLat(), 1e-8);

            assertEquals(-12.171875, coordinates[63].getLon(), 1e-8);
            assertEquals(-38.74999999999999, coordinates[63].getLat(), 1e-8);

            coordinates = geometries[1].getCoordinates();
            assertEquals(133, coordinates.length);
            assertEquals(5.1328125, coordinates[2].getLon(), 1e-8);
            assertEquals(-53.2109375, coordinates[2].getLat(), 1e-8);

            assertEquals(177.96875, coordinates[65].getLon(), 1e-8);
            assertEquals(64.234375, coordinates[65].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = geometries[0].getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1989, 3, 17, 6, 12, 21, 347, time);

            coordinates = geometries[1].getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1989, 3, 17, 7, 7, 9, 0, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_METOPA() throws IOException {
        final File file = getMetopAFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2011, 8, 23, 16, 41, 52, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2011, 8, 23, 18, 22, 40, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) boundingGeometry;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);

            Point[] coordinates = geometries[0].getCoordinates();
            assertEquals(125, coordinates.length);
            assertEquals(2.4777, coordinates[4].getLon(), 1e-8);
            assertEquals(77.4248, coordinates[4].getLat(), 1e-8);

            assertEquals(174.73, coordinates[61].getLon(), 1e-8);
            assertEquals(-69.4306, coordinates[61].getLat(), 1e-8);

            coordinates = geometries[1].getCoordinates();
            assertEquals(125, coordinates.length);
            assertEquals(-172.6181, coordinates[5].getLon(), 1e-8);
            assertEquals(-81.6821, coordinates[5].getLat(), 1e-8);

            assertEquals(52.7125, coordinates[60].getLon(), 1e-8);
            assertEquals(76.5637, coordinates[60].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = geometries[0].getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2011, 8, 23, 16, 41, 52, 0, time);

            coordinates = geometries[1].getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2011, 8, 23, 17, 32, 16, 0, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_TIROSN() throws IOException {
        final File file = getTirosNFile();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(308766239000L, timeLocator.getTimeFor(10, 0));
            assertEquals(308769746000L, timeLocator.getTimeFor(12, 548));
            assertEquals(308772453000L, timeLocator.getTimeFor(14, 978));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_NOAA10() throws IOException {
        final File file = getNOAA10File();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(606118336000L, timeLocator.getTimeFor(12, 0));
            assertEquals(606121933000L, timeLocator.getTimeFor(14, 562));
            assertEquals(606124922000L, timeLocator.getTimeFor(16, 1029));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_METOPA() throws IOException {
        final File file = getMetopAFile();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(1314117712000L, timeLocator.getTimeFor(14, 0));
            assertEquals(1314120541000L, timeLocator.getTimeFor(16, 442));
            assertEquals(1314123760000L, timeLocator.getTimeFor(18, 945));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_TIROSN() throws IOException, InvalidRangeException {
        final File file = getTirosNFile();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(66, variables.size());

            Variable variable = variables.get(0);
            assertEquals("time", variable.getShortName());

            variable = variables.get(1);
            assertEquals("lat", variable.getShortName());

            variable = variables.get(2);
            assertEquals("lon", variable.getShortName());

            variable = variables.get(3);
            assertEquals("bt_ch01", variable.getShortName());

            variable = variables.get(4);
            assertEquals("bt_ch02", variable.getShortName());

            variable = variables.get(14);
            assertEquals("bt_ch12", variable.getShortName());

            variable = variables.get(22);
            assertEquals("lza", variable.getShortName());

            variable = variables.get(23);
            assertEquals("radiance_ch01", variable.getShortName());

            variable = variables.get(34);
            assertEquals("radiance_ch12", variable.getShortName());

            variable = variables.get(42);
            assertEquals("radiance_ch20", variable.getShortName());

            variable = variables.get(43);
            assertEquals("counts_ch01", variable.getShortName());

            variable = variables.get(52);
            assertEquals("counts_ch10", variable.getShortName());

            variable = variables.get(62);
            assertEquals("counts_ch20", variable.getShortName());

            variable = variables.get(63);
            assertEquals("scanline", variable.getShortName());

            variable = variables.get(64);
            assertEquals("scanpos", variable.getShortName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_NOAA10() throws IOException, InvalidRangeException {
        final File file = getNOAA10File();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(66, variables.size());

            Variable variable = variables.get(0);
            assertEquals("time", variable.getShortName());

            variable = variables.get(3);
            assertEquals("bt_ch01", variable.getShortName());

            variable = variables.get(4);
            assertEquals("bt_ch02", variable.getShortName());

            variable = variables.get(21);
            assertEquals("bt_ch19", variable.getShortName());

            variable = variables.get(22);
            assertEquals("lza", variable.getShortName());

            variable = variables.get(23);
            assertEquals("radiance_ch01", variable.getShortName());

            variable = variables.get(42);
            assertEquals("radiance_ch20", variable.getShortName());

            variable = variables.get(43);
            assertEquals("counts_ch01", variable.getShortName());

            variable = variables.get(62);
            assertEquals("counts_ch20", variable.getShortName());

            variable = variables.get(63);
            assertEquals("scanline", variable.getShortName());

            variable = variables.get(64);
            assertEquals("scanpos", variable.getShortName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_METOPA() throws IOException, InvalidRangeException {
        final File file = getMetopAFile();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(66, variables.size());

            Variable variable = variables.get(0);
            assertEquals("time", variable.getShortName());

            variable = variables.get(4);
            assertEquals("bt_ch02", variable.getShortName());

            variable = variables.get(22);
            assertEquals("lza", variable.getShortName());

            variable = variables.get(23);
            assertEquals("radiance_ch01", variable.getShortName());

            variable = variables.get(42);
            assertEquals("radiance_ch20", variable.getShortName());

            variable = variables.get(43);
            assertEquals("counts_ch01", variable.getShortName());

            variable = variables.get(62);
            assertEquals("counts_ch20", variable.getShortName());

            variable = variables.get(63);
            assertEquals("scanline", variable.getShortName());

            variable = variables.get(64);
            assertEquals("scanpos", variable.getShortName());

            variable = variables.get(65);
            assertEquals("scanline_type", variable.getShortName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_TIROSN() throws IOException, InvalidRangeException {
        final File file = getTirosNFile();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertEquals(56, productSize.getNx());
            assertEquals(979, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_NOAA10() throws IOException, InvalidRangeException {
        final File file = getNOAA10File();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertEquals(56, productSize.getNx());
            assertEquals(1030, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_METOPA() throws IOException, InvalidRangeException {
        final File file = getMetopAFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(22, 124, interval);
            assertNotNull(acquisitionTime);
            final Index index = acquisitionTime.getIndex();     // index takes arguments as (y, x) tb 2016-08-03

            index.set(0, 0);
            assertEquals(1314118493, acquisitionTime.getInt(index));

            index.set(1, 1);
            assertEquals(1314118499, acquisitionTime.getInt(index));

            index.set(2, 2);
            assertEquals(1314118506, acquisitionTime.getInt(index));

            index.set(3, 1);
            assertEquals(1314118512, acquisitionTime.getInt(index));

            index.set(4, 0);
            assertEquals(1314118518, acquisitionTime.getInt(index));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_TIROSN() throws IOException, InvalidRangeException {
        final File file = getTirosNFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readScaled(13, 231, interval, "bt_ch01");
            NCTestUtils.assertValueAt(222.73716735839844, 0, 0, array);
            NCTestUtils.assertValueAt(233.08924865722656, 1, 0, array);

            array = reader.readScaled(14, 232, interval, "bt_ch02");
            NCTestUtils.assertValueAt(216.99270629882812, 2, 0, array);
            NCTestUtils.assertValueAt(215.75791931152344, 3, 0, array);

            array = reader.readScaled(15, 233, interval, "counts_ch03");
            NCTestUtils.assertValueAt(-863, 4, 0, array);
            NCTestUtils.assertValueAt(-868, 0, 1, array);

            array = reader.readScaled(16, 234, interval, "counts_ch04");
            NCTestUtils.assertValueAt(-584, 1, 1, array);
            NCTestUtils.assertValueAt(-588, 2, 1, array);

            array = reader.readScaled(17, 235, interval, "lat");
            NCTestUtils.assertValueAt(29.71875, 3, 1, array);
            NCTestUtils.assertValueAt(29.765625, 4, 1, array);

            array = reader.readScaled(18, 236, interval, "lon");
            NCTestUtils.assertValueAt(164.796875, 0, 2, array);
            NCTestUtils.assertValueAt(164.4765625, 1, 2, array);

            array = reader.readScaled(19, 237, interval, "lza");
            NCTestUtils.assertValueAt(17.467435836791992, 2, 2, array);
            NCTestUtils.assertValueAt(15.402369499206543, 3, 2, array);

            array = reader.readScaled(20, 238, interval, "radiance_ch05");
            NCTestUtils.assertValueAt(70.4374771118164, 4, 2, array);
            NCTestUtils.assertValueAt(69.71855163574219, 0, 3, array);

            array = reader.readScaled(21, 239, interval, "radiance_ch06");
            NCTestUtils.assertValueAt(-999.0, 1, 3, array);
            NCTestUtils.assertValueAt(-999.0, 2, 3, array);

            array = reader.readScaled(22, 240, interval, "scanline");
            NCTestUtils.assertValueAt(242, 3, 3, array);
            NCTestUtils.assertValueAt(242, 4, 3, array);

            final Interval nonSquareInterval = new Interval(3, 5);
            array = reader.readScaled(23, 241, nonSquareInterval, "scanpos");
            NCTestUtils.assertValueAt(22, 0, 2, array);
            NCTestUtils.assertValueAt(23, 1, 2, array);

            array = reader.readScaled(0, 242, nonSquareInterval, "scanpos");
            NCTestUtils.assertValueAt(N3iosp.NC_FILL_BYTE, 0, 2, array);
            NCTestUtils.assertValueAt(0, 1, 2, array);
            NCTestUtils.assertValueAt(1, 2, 2, array);

            array = reader.readScaled(55, 242, nonSquareInterval, "scanpos");
            NCTestUtils.assertValueAt(54, 0, 2, array);
            NCTestUtils.assertValueAt(55, 1, 2, array);
            NCTestUtils.assertValueAt(N3iosp.NC_FILL_BYTE, 2, 2, array);

            array = reader.readScaled(24, 242, interval, "time");
            NCTestUtils.assertValueAt(308767794, 0, 3, array);
            NCTestUtils.assertValueAt(308767801, 1, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_NOAA10() throws IOException, InvalidRangeException {
        final File file = getNOAA10File();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(25, 243, interval, "bt_ch07");
            NCTestUtils.assertValueAt(251.18798828125, 2, 4, array);
            NCTestUtils.assertValueAt(251.59432983398438, 3, 4, array);

            array = reader.readRaw(26, 244, interval, "bt_ch08");
            NCTestUtils.assertValueAt(261.6963195800781, 4, 4, array);
            NCTestUtils.assertValueAt(242.42616271972656, 0, 0, array);

            array = reader.readRaw(27, 245, interval, "counts_ch09");
            NCTestUtils.assertValueAt(-2, 1, 0, array);
            NCTestUtils.assertValueAt(-43, 2, 0, array);

            array = reader.readRaw(28, 246, interval, "counts_ch10");
            NCTestUtils.assertValueAt(-782, 3, 0, array);
            NCTestUtils.assertValueAt(-687, 4, 0, array);

            array = reader.readRaw(29, 247, interval, "lat");
            NCTestUtils.assertValueAt(48.7734375, 0, 1, array);
            NCTestUtils.assertValueAt(48.8203125, 1, 1, array);

            array = reader.readRaw(30, 248, interval, "lon");
            NCTestUtils.assertValueAt(22.9453125, 2, 1, array);
            NCTestUtils.assertValueAt(22.6015625, 3, 1, array);

            array = reader.readRaw(31, 249, interval, "lza");
            NCTestUtils.assertValueAt(11.476059913635254, 4, 1, array);
            NCTestUtils.assertValueAt(3.1275672912597656, 0, 2, array);

            array = reader.readRaw(32, 250, interval, "radiance_ch11");
            NCTestUtils.assertValueAt(12.17858600616455, 1, 2, array);
            NCTestUtils.assertValueAt(12.456483840942383, 2, 2, array);

            array = reader.readRaw(33, 251, interval, "radiance_ch12");
            NCTestUtils.assertValueAt(4.776543617248535, 3, 2, array);
            NCTestUtils.assertValueAt(4.776543617248535, 4, 2, array);

            array = reader.readRaw(34, 252, interval, "scanline");
            NCTestUtils.assertValueAt(287, 0, 3, array);
            NCTestUtils.assertValueAt(287, 1, 3, array);

            array = reader.readRaw(35, 253, interval, "scanpos");
            NCTestUtils.assertValueAt(35, 2, 3, array);
            NCTestUtils.assertValueAt(36, 3, 3, array);

            array = reader.readRaw(36, 254, interval, "time");
            NCTestUtils.assertValueAt(606119968, 4, 3, array);
            NCTestUtils.assertValueAt(606119975, 0, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_METOPA() throws IOException, InvalidRangeException {
        final File file = getMetopAFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(37, 4, interval, "scanline_type");
            NCTestUtils.assertValueAt(0, 1, 0, array);
            NCTestUtils.assertValueAt(0, 1, 1, array);
            NCTestUtils.assertValueAt(0, 1, 2, array);
            NCTestUtils.assertValueAt(0, 1, 3, array);
            NCTestUtils.assertValueAt(0, 1, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator_MetopA() throws IOException {
        final File file = getMetopAFile();

        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            Point2D geoLocation = pixelLocator.getGeoLocation(0.5, 0.5, null);
            assertEquals(-4.352700233459473, geoLocation.getX(), 1e-8);
            assertEquals(71.04930114746094, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(29.5, 696.5, null);
            assertEquals(55.05039978027344, geoLocation.getX(), 1e-8);
            assertEquals(-10.665200233459473, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(55.5, 782.5, null);
            assertEquals(57.537899017333984, geoLocation.getX(), 1e-8);
            assertEquals(23.64889907836914, geoLocation.getY(), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator_Singleton_MetopA() throws IOException {
        final File file = getMetopAFile();

        try {
            reader.open(file);

            final PixelLocator first = reader.getPixelLocator();
            final PixelLocator second = reader.getPixelLocator();

            assertSame(first, second);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testSubscenePixelLocator_TIROSN() throws IOException {
        final File file = getTirosNFile();

        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Polygon polygon = (Polygon) geometryFactory.parse("POLYGON((-135.2109 68.8125, 150.625 80.695, 153.5 62.328, -167.132 56.39, -135.2109 68.8125))");

        try {
            reader.open(file);

            final PixelLocator subScenePixelLocator = reader.getSubScenePixelLocator(polygon);
            assertNotNull(subScenePixelLocator);

            Point2D geoLocation = subScenePixelLocator.getGeoLocation(0.5, 0.5, null);
            assertEquals(-50.6875, geoLocation.getX(), 1e-8);
            assertEquals(55.8515625, geoLocation.getY(), 1e-8);
        } finally {
            reader.close();
        }
    }

    private File getMetopAFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-ma", "1.0", "2011", "08", "23", "190583863.NSS.HIRX.M2.D11235.S1641.E1823.B2513233.SV.nc"}, false);
        return getFileAsserted(testFilePath);
    }

    private File getTirosNFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-tn", "1.0", "1979", "10", "14", "NSS.HIRX.TN.D79287.S1623.E1807.B0516566.GC.nc"}, false);
        return getFileAsserted(testFilePath);
    }

    private File getNOAA10File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-n10", "1.0", "1989", "03", "17", "NSS.HIRX.NG.D89076.S0608.E0802.B1296162.WI.nc"}, false);
        return getFileAsserted(testFilePath);
    }

    private File getFileAsserted(String testFilePath) {
        final File file = new File(dataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}

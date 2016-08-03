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
            TestUtil.assertCorrectUTCDate(1989, 3, 17, 6, 8, 45, 0, sensingStart);

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
            assertEquals(137, coordinates.length);
            assertEquals(-170.703125, coordinates[1].getLon(), 1e-8);
            assertEquals(22.859375, coordinates[1].getLat(), 1e-8);

            assertEquals(-6.3515625, coordinates[63].getLon(), 1e-8);
            assertEquals(-25.4765625, coordinates[63].getLat(), 1e-8);

            coordinates = geometries[1].getCoordinates();
            assertEquals(137, coordinates.length);
            assertEquals(6.6640625, coordinates[2].getLon(), 1e-8);
            assertEquals(-46.84375, coordinates[2].getLat(), 1e-8);

            assertEquals(178.5625, coordinates[65].getLon(), 1e-8);
            assertEquals(56.78125, coordinates[65].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = geometries[0].getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1989, 3, 17, 6, 8, 52, 2, time);

            coordinates = geometries[1].getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1989, 3, 17, 7, 5, 23, 500, time);
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
            TestUtil.assertCorrectUTCDate(2011, 8, 23, 16, 41, 20, 0, sensingStart);

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
            assertEquals(10.2899, coordinates[4].getLon(), 1e-8);
            assertEquals(76.641, coordinates[4].getLat(), 1e-8);

            assertEquals(177.6307, coordinates[61].getLon(), 1e-8);
            assertEquals(-68.9219, coordinates[61].getLat(), 1e-8);

            coordinates = geometries[1].getCoordinates();
            assertEquals(125, coordinates.length);
            assertEquals(-166.8919, coordinates[5].getLon(), 1e-8);
            assertEquals(-80.9488, coordinates[5].getLat(), 1e-8);

            assertEquals(52.7125, coordinates[60].getLon(), 1e-8);
            assertEquals(76.5637, coordinates[60].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = geometries[0].getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2011, 8, 23, 16, 41, 20, 0, time);

            coordinates = geometries[1].getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2011, 8, 23, 17, 32, 0, 0, time);
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

            assertEquals(606118125000L, timeLocator.getTimeFor(12, 0));
            assertEquals(606121722000L, timeLocator.getTimeFor(14, 562));
            assertEquals(606124922000L, timeLocator.getTimeFor(16, 1062));
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

            assertEquals(1314117680000L, timeLocator.getTimeFor(14, 0));
            assertEquals(1314120509000L, timeLocator.getTimeFor(16, 442));
            assertEquals(1314123760000L, timeLocator.getTimeFor(18, 950));
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
            assertEquals(65, variables.size());

            Variable variable = variables.get(0);
            assertEquals("time", variable.getShortName());

            variable = variables.get(1);
            assertEquals("lat", variable.getShortName());

            variable = variables.get(2);
            assertEquals("lon", variable.getShortName());

            variable = variables.get(3);
            assertEquals("lza", variable.getShortName());

            variable = variables.get(4);
            assertEquals("bt_ch01", variable.getShortName());

            variable = variables.get(14);
            assertEquals("bt_ch11", variable.getShortName());

            variable = variables.get(22);
            assertEquals("bt_ch19", variable.getShortName());

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
            assertEquals(65, variables.size());

            Variable variable = variables.get(0);
            assertEquals("time", variable.getShortName());

            variable = variables.get(4);
            assertEquals("bt_ch01", variable.getShortName());

            variable = variables.get(22);
            assertEquals("bt_ch19", variable.getShortName());

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
            assertEquals(65, variables.size());

            Variable variable = variables.get(0);
            assertEquals("time", variable.getShortName());

            variable = variables.get(4);
            assertEquals("bt_ch01", variable.getShortName());

            variable = variables.get(22);
            assertEquals("bt_ch19", variable.getShortName());

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
            assertEquals(1063, productSize.getNy());
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
            assertEquals(1314118461, acquisitionTime.getInt(index));

            index.set(1, 1);
            assertEquals(1314118467, acquisitionTime.getInt(index));

            index.set(2, 2);
            assertEquals(1314118474, acquisitionTime.getInt(index));

            index.set(3, 1);
            assertEquals(1314118480, acquisitionTime.getInt(index));

            index.set(4, 0);
            assertEquals(1314118486, acquisitionTime.getInt(index));
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
            NCTestUtils.assertValueAt(222.7375946044922, 0, 0, array);
            NCTestUtils.assertValueAt(233.08966064453125, 1, 0, array);

            array = reader.readScaled(14, 232, interval, "bt_ch02");
            NCTestUtils.assertValueAt(216.99314880371094, 2, 0, array);
            NCTestUtils.assertValueAt(215.75836181640625, 3, 0, array);

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
            NCTestUtils.assertValueAt(-0.5326722860336304, 1, 3, array);
            NCTestUtils.assertValueAt(0.2421426773071289, 2, 3, array);

            array = reader.readScaled(22, 240, interval, "scanline");
            NCTestUtils.assertValueAt(242, 3, 3, array);
            NCTestUtils.assertValueAt(242, 4, 3, array);

            final Interval nonSquareInterval = new Interval(3, 5);
            array = reader.readScaled(23, 241, nonSquareInterval, "scanpos");
            NCTestUtils.assertValueAt(22, 0, 2, array);
            NCTestUtils.assertValueAt(23, 1, 2, array);

            array = reader.readScaled(0, 242, nonSquareInterval, "scanpos");
            NCTestUtils.assertValueAt(-128, 0, 2, array);
            NCTestUtils.assertValueAt(0, 1, 2, array);
            NCTestUtils.assertValueAt(1, 2, 2, array);

            array = reader.readScaled(55, 242, nonSquareInterval, "scanpos");
            NCTestUtils.assertValueAt(54, 0, 2, array);
            NCTestUtils.assertValueAt(55, 1, 2, array);
            NCTestUtils.assertValueAt(-128, 2, 2, array);

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
            NCTestUtils.assertValueAt(252.1149139404297, 2, 4, array);
            NCTestUtils.assertValueAt(252.6898193359375, 3, 4, array);

            array = reader.readRaw(26, 244, interval, "bt_ch08");
            NCTestUtils.assertValueAt(266.6014709472656, 4, 4, array);
            NCTestUtils.assertValueAt(253.4669647216797, 0, 0, array);

            array = reader.readRaw(27, 245, interval, "counts_ch09");
            NCTestUtils.assertValueAt(266, 1, 0, array);
            NCTestUtils.assertValueAt(285, 2, 0, array);

            array = reader.readRaw(28, 246, interval, "counts_ch10");
            NCTestUtils.assertValueAt(-654, 3, 0, array);
            NCTestUtils.assertValueAt(-649, 4, 0, array);

            array = reader.readRaw(29, 247, interval, "lat");
            NCTestUtils.assertValueAt(60.84375, 0, 1, array);
            NCTestUtils.assertValueAt(60.90625, 1, 1, array);

            array = reader.readRaw(30, 248, interval, "lon");
            NCTestUtils.assertValueAt(29.2421875, 2, 1, array);
            NCTestUtils.assertValueAt(28.7890625, 3, 1, array);

            array = reader.readRaw(31, 249, interval, "lza");
            NCTestUtils.assertValueAt(11.521845817565918, 4, 1, array);
            NCTestUtils.assertValueAt(3.1400468349456787, 0, 2, array);

            array = reader.readRaw(32, 250, interval, "radiance_ch11");
            NCTestUtils.assertValueAt(11.918828964233398, 1, 2, array);
            NCTestUtils.assertValueAt(11.980561256408691, 2, 2, array);

            array = reader.readRaw(33, 251, interval, "radiance_ch12");
            NCTestUtils.assertValueAt(4.9098429679870605, 3, 2, array);
            NCTestUtils.assertValueAt(4.83237886428833, 4, 2, array);

            array = reader.readRaw(34, 252, interval, "scanline");
            NCTestUtils.assertValueAt(254, 0, 3, array);
            NCTestUtils.assertValueAt(254, 1, 3, array);

            array = reader.readRaw(35, 253, interval, "scanpos");
            NCTestUtils.assertValueAt(35, 2, 3, array);
            NCTestUtils.assertValueAt(36, 3, 3, array);

            array = reader.readRaw(36, 254, interval, "time");
            NCTestUtils.assertValueAt(606119757, 4, 3, array);
            NCTestUtils.assertValueAt(606119763, 0, 4, array);
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
            assertEquals(1.253499984741211, geoLocation.getX(), 1e-8);
            assertEquals(70.50540161132812, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(29.5, 696.5, null);
            assertEquals(55.48429870605469, geoLocation.getX(), 1e-8);
            assertEquals(-12.545900344848633, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(55.5, 782.5, null);
            assertEquals(57.860198974609375, geoLocation.getX(), 1e-8);
            assertEquals(21.78820037841797, geoLocation.getY(), 1e-8);
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

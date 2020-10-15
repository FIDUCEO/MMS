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

package com.bc.fiduceo.reader.iasi;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.time.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class IASI_Reader_IO_Test {

    private IASI_Reader reader;

    @Before
    public void setUp() throws IOException {
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new IASI_Reader(readerContext);
    }

    @Test
    public void testOpen_alreadyOpenedStreamThrows() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v5();

        try {
            reader.open(iasiFile);

            try {
                reader.open(iasiFile);
                fail("RuntimeException expected");
            } catch (RuntimeException expected) {
            }

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_MA_v5() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v5();

        try {
            reader.open(iasiFile);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 12, 47, 54, 870, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 14, 26, 58, 414, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(166, coordinates.length);
            assertEquals(-21.02281951904297, coordinates[3].getLon(), 1e-8);
            assertEquals(66.62924194335938, coordinates[3].getLat(), 1e-8);

            assertEquals(-26.05643653869629, coordinates[79].getLon(), 1e-8);
            assertEquals(48.4214973449707, coordinates[79].getLat(), 1e-8);

            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = polygons.get(0).getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 12, 47, 54, 870, time);

            coordinates = polygons.get(1).getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 13, 37, 26, 642, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_MA_v4() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v4();

        try {
            reader.open(iasiFile);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2009, 4, 6, 1, 14, 56, 495, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2009, 4, 6, 2, 59, 52, 38, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(174, coordinates.length);
            assertEquals(179.3275146484375, coordinates[4].getLon(), 1e-8);
            assertEquals(77.74348449707031, coordinates[4].getLat(), 1e-8);

            assertEquals(144.5364990234375, coordinates[80].getLon(), 1e-8);
            assertEquals(45.13542175292969, coordinates[80].getLat(), 1e-8);

            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = polygons.get(0).getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2009, 4, 6, 1, 14, 56, 495, time);

            coordinates = polygons.get(1).getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2009, 4, 6, 2, 7, 24, 266, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_MB() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MB();

        try {
            reader.open(iasiFile);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2014, 4, 25, 12, 47, 56, 879, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2014, 4, 25, 14, 26, 52, 463, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(166, coordinates.length);
            assertEquals(-19.0395393371582, coordinates[2].getLon(), 1e-8);
            assertEquals(64.17292785644531, coordinates[2].getLat(), 1e-8);

            assertEquals(-29.8023567199707, coordinates[78].getLon(), 1e-8);
            assertEquals(41.484779357910156, coordinates[78].getLat(), 1e-8);

            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = polygons.get(0).getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2014, 4, 25, 12, 47, 56, 879, time);

            coordinates = polygons.get(1).getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2014, 4, 25, 13, 37, 24, 671, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_MA_v5() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v5();

        try {
            reader.open(iasiFile);

            final Dimension productSize = reader.getProductSize();
            assertNotNull(productSize);
            assertEquals(60, productSize.getNx());
            assertEquals(1486, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_MA_v4() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v4();

        try {
            reader.open(iasiFile);

            final Dimension productSize = reader.getProductSize();
            assertNotNull(productSize);
            assertEquals(60, productSize.getNx());
            assertEquals(1574, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_MA_v5() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v5();

        try {
            reader.open(iasiFile);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);
            assertTrue(timeLocator instanceof IASI_TimeLocator);

            long time = timeLocator.getTimeFor(0, 0);
            assertEquals(1451652474870L, time);
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 12, 47, 54, 870, new Date(time));

            time = timeLocator.getTimeFor(34, 744);
            assertEquals(1451655454535L, time);
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 13, 37, 34, 535, new Date(time));

            time = timeLocator.getTimeFor(35, 744); // this pixel is within the same EFOV than the one before, acquired at the same time tb 2017-04-27
            assertEquals(1451655454535L, time);
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 13, 37, 34, 535, new Date(time));

            time = timeLocator.getTimeFor(36, 744); // one EFOV further - approx 200 ms later tb 2017-04-27
            assertEquals(1451655454749L, time);
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 13, 37, 34, 749, new Date(time));

            time = timeLocator.getTimeFor(59, 1485);
            assertEquals(1451658417117L, time);
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 14, 26, 57, 117, new Date(time));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_MA_v4() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v4();

        try {
            reader.open(iasiFile);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);
            assertTrue(timeLocator instanceof IASI_TimeLocator);

            long time = timeLocator.getTimeFor(0, 0);
            assertEquals(1238980496495L, time);
            TestUtil.assertCorrectUTCDate(2009, 4, 6, 1, 14, 56, 495, new Date(time));

            time = timeLocator.getTimeFor(34, 744);
            assertEquals(1238983476163L, time);
            TestUtil.assertCorrectUTCDate(2009, 4, 6, 2, 4, 36, 163, new Date(time));

            time = timeLocator.getTimeFor(35, 744); // this pixel is within the same EFOV than the one before, acquired at the same time tb 2017-04-27
            assertEquals(1238983476163L, time);
            TestUtil.assertCorrectUTCDate(2009, 4, 6, 2, 4, 36, 163, new Date(time));

            time = timeLocator.getTimeFor(36, 744); // one EFOV further - approx 200 ms later tb 2017-04-27
            assertEquals(1238983476378L, time);
            TestUtil.assertCorrectUTCDate(2009, 4, 6, 2, 4, 36, 378, new Date(time));

            time = timeLocator.getTimeFor(59, 1485);
            assertEquals(1238986438749L, time);
            TestUtil.assertCorrectUTCDate(2009, 4, 6, 2, 53, 58, 749, new Date(time));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocater_MA_v5() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v5();

        try {
            reader.open(iasiFile);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            Point2D geoLocation = pixelLocator.getGeoLocation(0.5, 0.5, null);
            assertEquals(-8.481149673461914, geoLocation.getX(), 1e-8);
            assertEquals(62.769412994384766, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(59.5, 0.5, null);
            assertEquals(-53.7663688659668, geoLocation.getX(), 1e-8);
            assertEquals(69.9629135131836, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(21.5, 367.5, null);
            assertEquals(-56.68997573852539, geoLocation.getX(), 1e-8);
            assertEquals(-17.64765167236328, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(24.5, 628.5, null);
            assertEquals(-94.60089111328125, geoLocation.getX(), 1e-8);
            assertEquals(-76.91805267333984, geoLocation.getY(), 1e-8);

            Point2D[] pixelLocation = pixelLocator.getPixelLocation(-8.481149673461914, 62.769412994384766);
            assertEquals(1, pixelLocation.length);
            assertEquals(0.5f, pixelLocation[0].getX(), 1e-8);
            assertEquals(0.5f, pixelLocation[0].getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(-53.7663688659668, 69.9629135131836);
            assertEquals(1, pixelLocation.length);
            assertEquals(59.5f, pixelLocation[0].getX(), 1e-8);
            assertEquals(0.5f, pixelLocation[0].getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(-56.68997573852539, -17.64765167236328);
            assertEquals(1, pixelLocation.length);
            assertEquals(21.5f, pixelLocation[0].getX(), 1e-8);
            assertEquals(367.5f, pixelLocation[0].getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(-94.60089111328125, -76.91805267333984);
            assertEquals(1, pixelLocation.length);
            assertEquals(24.5, pixelLocation[0].getX(), 1e-8);
            assertEquals(628.5, pixelLocation[0].getY(), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocater_MA_v4() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v4();

        try {
            reader.open(iasiFile);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            Point2D geoLocation = pixelLocator.getGeoLocation(0.5, 0.5, null);
            assertEquals(-166.61215209960938, geoLocation.getX(), 1e-8);
            assertEquals(70.62594604492188, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(59.5, 0.5, null);
            assertEquals(116.59491729736328, geoLocation.getX(), 1e-8);
            assertEquals(82.99984741210938, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(21.5, 367.5, null);
            assertEquals(119.2708740234375, geoLocation.getX(), 1e-8);
            assertEquals(-4.314527988433838, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(24.5, 628.5, null);
            assertEquals(97.68930053710938, geoLocation.getX(), 1e-8);
            assertEquals(-64.88347625732422, geoLocation.getY(), 1e-8);

            Point2D[] pixelLocation = pixelLocator.getPixelLocation(-166.61215209960938, 70.62594604492188);
            assertEquals(1, pixelLocation.length);
            assertEquals(0.5f, pixelLocation[0].getX(), 1e-8);
            assertEquals(0.5f, pixelLocation[0].getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(116.59491729736328, 82.99984741210938);
            assertEquals(2, pixelLocation.length);
            assertEquals(59.5f, pixelLocation[0].getX(), 1e-8);
            assertEquals(0.5f, pixelLocation[0].getY(), 1e-8);
            assertEquals(53.5f, pixelLocation[1].getX(), 1e-8);
            assertEquals(1519.5f, pixelLocation[1].getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(119.2708740234375, -4.314527988433838);
            assertEquals(1, pixelLocation.length);
            assertEquals(21.5f, pixelLocation[0].getX(), 1e-8);
            assertEquals(367.5f, pixelLocation[0].getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(97.68930053710938, -64.88347625732422);
            assertEquals(1, pixelLocation.length);
            assertEquals(24.5, pixelLocation[0].getX(), 1e-8);
            assertEquals(628.5, pixelLocation[0].getY(), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocater_MB() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MB();

        try {
            reader.open(iasiFile);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            Point2D geoLocation = pixelLocator.getGeoLocation(0.5, 0.5, null);
            assertEquals(-10.291940689086914, geoLocation.getX(), 1e-8);
            assertEquals(61.35856628417969, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(59.5, 0.5, null);
            assertEquals(-53.05204772949219, geoLocation.getX(), 1e-8);
            assertEquals(68.13693237304688, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(21.5, 367.5, null);
            assertEquals(-56.1817512512207, geoLocation.getX(), 1e-8);
            assertEquals(-19.48737144470215, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(19.5, 628.5, null);
            assertEquals(-93.76776885986328, geoLocation.getX(), 1e-8);
            assertEquals(-79.11643981933594, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(18.5, 744.5, null);
            assertEquals(135.94346618652344, geoLocation.getX(), 1e-8);
            assertEquals(-70.93910217285156, geoLocation.getY(), 1e-8);

            Point2D[] pixelLocation = pixelLocator.getPixelLocation(-10.291940689086914, 61.35856628417969);
            assertEquals(1, pixelLocation.length);
            assertEquals(0.5f, pixelLocation[0].getX(), 1e-8);
            assertEquals(0.5f, pixelLocation[0].getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(-53.05204772949219, 68.13693237304688);
            assertEquals(1, pixelLocation.length);
            assertEquals(59.5f, pixelLocation[0].getX(), 1e-8);
            assertEquals(0.5f, pixelLocation[0].getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(-56.1817512512207, -19.48737144470215);
            assertEquals(1, pixelLocation.length);
            assertEquals(21.5f, pixelLocation[0].getX(), 1e-8);
            assertEquals(367.5f, pixelLocation[0].getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(-93.76776885986328, -79.11643981933594);
            assertEquals(1, pixelLocation.length);
            assertEquals(19.5, pixelLocation[0].getX(), 1e-8);
            assertEquals(628.5, pixelLocation[0].getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(135.94346618652344, -70.93910217285156);
            assertEquals(1, pixelLocation.length);
            assertEquals(18.5, pixelLocation[0].getX(), 1e-8);
            assertEquals(744.5, pixelLocation[0].getY(), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocater_MA_twiceReturnsSameObject() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v5();

        try {
            reader.open(iasiFile);

            final PixelLocator pixelLocator_1 = reader.getPixelLocator();
            assertNotNull(pixelLocator_1);

            final PixelLocator pixelLocator_2 = reader.getPixelLocator();
            assertNotNull(pixelLocator_2);

            assertSame(pixelLocator_1, pixelLocator_2);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testSubsceneGetPixelLocater_MB() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MB();

        try {
            reader.open(iasiFile);

            final PixelLocator subScenePixelLocator = reader.getSubScenePixelLocator(null);// we ignore the geometry tb 2017-05-03
            assertNotNull(subScenePixelLocator);
            // assertTrue(subScenePixelLocator instanceof IASI_PixelLocator);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_MB() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MB();

        try {
            reader.open(iasiFile);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);
            assertTrue(timeLocator instanceof IASI_TimeLocator);

            long time = timeLocator.getTimeFor(0, 0);
            assertEquals(1398430076879L, time);
            TestUtil.assertCorrectUTCDate(2014, 4, 25, 12, 47, 56, 879, new Date(time));

            time = timeLocator.getTimeFor(54, 844);
            assertEquals(1398433458733L, time);
            TestUtil.assertCorrectUTCDate(2014, 4, 25, 13, 44, 18, 733, new Date(time));

            time = timeLocator.getTimeFor(55, 844); // this pixel is within the same EFOV than the one before, acquired at the same time tb 2017-04-28
            assertEquals(1398433458733L, time);
            TestUtil.assertCorrectUTCDate(2014, 4, 25, 13, 44, 18, 733, new Date(time));

            time = timeLocator.getTimeFor(56, 844); // one EFOV further - approx 200 ms later tb 2017-04-27
            assertEquals(1398433458948L, time);
            TestUtil.assertCorrectUTCDate(2014, 4, 25, 13, 44, 18, 948, new Date(time));

            time = timeLocator.getTimeFor(59, 1483);
            assertEquals(1398436011167L, time);
            TestUtil.assertCorrectUTCDate(2014, 4, 25, 14, 26, 51, 167, new Date(time));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_MB_twiceReturnsSameObject() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MB();

        try {
            reader.open(iasiFile);
            final TimeLocator timeLocator_1 = reader.getTimeLocator();
            assertNotNull(timeLocator_1);

            final TimeLocator timeLocator_2 = reader.getTimeLocator();
            assertNotNull(timeLocator_2);

            assertSame(timeLocator_1, timeLocator_2);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_MA_v5_perScan_byte() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v5();

        try {
            reader.open(iasiFile);

            final Array array = reader.readRaw(22, 108, new Interval(1, 1), "DEGRADED_INST_MDR");
            assertNotNull(array);
            final int[] shape = array.getShape();
            assertEquals(2, shape.length);
            assertEquals(1, shape[0]);
            assertEquals(1, shape[1]);

            NCTestUtils.assertValueAt(0, 0, 0, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_MA_v4_perScan_byte() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v4();

        try {
            reader.open(iasiFile);

            final Array array = reader.readRaw(23, 109, new Interval(1, 1), "DEGRADED_INST_MDR");
            assertNotNull(array);
            final int[] shape = array.getShape();
            assertEquals(2, shape.length);
            assertEquals(1, shape[0]);
            assertEquals(1, shape[1]);

            NCTestUtils.assertValueAt(0, 0, 0, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_MB_perScan_int() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MB();

        try {
            reader.open(iasiFile);

            final Array array = reader.readRaw(23, 109, new Interval(3, 1), "GEPSIasiMode");
            assertNotNull(array);
            final int[] shape = array.getShape();
            assertEquals(2, shape.length);
            assertEquals(1, shape[0]);
            assertEquals(3, shape[1]);

            NCTestUtils.assertValueAt(161, 0, 0, array);
            NCTestUtils.assertValueAt(161, 1, 0, array);
            NCTestUtils.assertValueAt(161, 2, 0, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_MA_v5_perScan_utc() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v5();

        try {
            reader.open(iasiFile);

            final Array array = reader.readRaw(24, 110, new Interval(3, 3), "GEPSDatIasi");
            assertNotNull(array);
            final int[] shape = array.getShape();
            assertEquals(2, shape.length);
            assertEquals(3, shape[0]);
            assertEquals(3, shape[1]);

            NCTestUtils.assertValueAt(1451652909236L, 0, 0, array);
            NCTestUtils.assertValueAt(1451652917236L, 0, 1, array);
            NCTestUtils.assertValueAt(1451652917236L, 0, 2, array);
            NCTestUtils.assertValueAt(1451652909451L, 1, 0, array);
            NCTestUtils.assertValueAt(1451652917451L, 1, 1, array);
            NCTestUtils.assertValueAt(1451652917451L, 1, 2, array);
            NCTestUtils.assertValueAt(1451652909451L, 2, 0, array);
            NCTestUtils.assertValueAt(1451652917451L, 2, 1, array);
            NCTestUtils.assertValueAt(1451652917451L, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_MA_v4_perScan_utc() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v4();

        try {
            reader.open(iasiFile);

            final Array array = reader.readRaw(25, 111, new Interval(3, 3), "GEPSDatIasi");
            assertNotNull(array);
            final int[] shape = array.getShape();
            assertEquals(2, shape.length);
            assertEquals(3, shape[0]);
            assertEquals(3, shape[1]);

            NCTestUtils.assertValueAt(1238980939079L, 0, 0, array);
            NCTestUtils.assertValueAt(1238980939079L, 0, 1, array);
            NCTestUtils.assertValueAt(1238980947079L, 0, 2, array);
            NCTestUtils.assertValueAt(1238980939079L, 1, 0, array);
            NCTestUtils.assertValueAt(1238980939079L, 1, 1, array);
            NCTestUtils.assertValueAt(1238980947079L, 1, 2, array);
            NCTestUtils.assertValueAt(1238980939294L, 2, 0, array);
            NCTestUtils.assertValueAt(1238980939294L, 2, 1, array);
            NCTestUtils.assertValueAt(1238980947294L, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_MB_perPixel_short_upperBorder() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MB();

        try {
            reader.open(iasiFile);

            final Array array = reader.readRaw(24, 0, new Interval(3, 3), "GQisFlagQualDetailed");
            assertNotNull(array);
            final int[] shape = array.getShape();
            assertEquals(2, shape.length);
            assertEquals(3, shape[0]);
            assertEquals(3, shape[1]);

            NCTestUtils.assertValueAt(-32767, 0, 0, array);
            NCTestUtils.assertValueAt(-32767, 1, 0, array);
            NCTestUtils.assertValueAt(-32767, 2, 0, array);
            NCTestUtils.assertValueAt(0, 0, 1, array);
            NCTestUtils.assertValueAt(0, 1, 1, array);
            NCTestUtils.assertValueAt(0, 2, 1, array);
            NCTestUtils.assertValueAt(0, 0, 2, array);
            NCTestUtils.assertValueAt(0, 1, 2, array);
            NCTestUtils.assertValueAt(0, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_MA_v5_perScan_dualInt_rightBorder() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v5();

        try {
            reader.open(iasiFile);

            final Array array = reader.readRaw(60, 111, new Interval(3, 3), "GGeoSondLoc_Lon");
            assertNotNull(array);
            final int[] shape = array.getShape();
            assertEquals(2, shape.length);
            assertEquals(3, shape[0]);
            assertEquals(3, shape[1]);

            NCTestUtils.assertValueAt(-56386416, 0, 0, array);
            NCTestUtils.assertValueAt(-2147483647, 1, 0, array);
            NCTestUtils.assertValueAt(-2147483647, 2, 0, array);
            NCTestUtils.assertValueAt(-56426833, 0, 1, array);
            NCTestUtils.assertValueAt(-2147483647, 1, 1, array);
            NCTestUtils.assertValueAt(-2147483647, 2, 1, array);
            NCTestUtils.assertValueAt(-56450797, 0, 2, array);
            NCTestUtils.assertValueAt(-2147483647, 1, 2, array);
            NCTestUtils.assertValueAt(-2147483647, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_MA_v4_perScan_dualInt_rightBorder() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v4();

        try {
            reader.open(iasiFile);

            final Array array = reader.readRaw(60, 112, new Interval(3, 3), "GGeoSondLoc_Lon");
            assertNotNull(array);
            final int[] shape = array.getShape();
            assertEquals(2, shape.length);
            assertEquals(3, shape[0]);
            assertEquals(3, shape[1]);

            NCTestUtils.assertValueAt(118209298, 0, 0, array);
            NCTestUtils.assertValueAt(-2147483647, 1, 0, array);
            NCTestUtils.assertValueAt(-2147483647, 2, 0, array);
            NCTestUtils.assertValueAt(118192818, 0, 1, array);
            NCTestUtils.assertValueAt(-2147483647, 1, 1, array);
            NCTestUtils.assertValueAt(-2147483647, 2, 1, array);
            NCTestUtils.assertValueAt(118159131, 0, 2, array);
            NCTestUtils.assertValueAt(-2147483647, 1, 2, array);
            NCTestUtils.assertValueAt(-2147483647, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_MB_perScan_perPixelInt_bottomBorder() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v5();

        try {
            reader.open(iasiFile);

            final Array array = reader.readRaw(16, 1485, new Interval(3, 3), "GCcsRadAnalNbClass");
            assertNotNull(array);
            final int[] shape = array.getShape();
            assertEquals(2, shape.length);
            assertEquals(3, shape[0]);
            assertEquals(3, shape[1]);

            NCTestUtils.assertValueAt(5, 0, 0, array);
            NCTestUtils.assertValueAt(4, 1, 0, array);
            NCTestUtils.assertValueAt(4, 2, 0, array);
            NCTestUtils.assertValueAt(3, 0, 1, array);
            NCTestUtils.assertValueAt(3, 1, 1, array);
            NCTestUtils.assertValueAt(4, 2, 1, array);
            NCTestUtils.assertValueAt(-2147483647, 0, 2, array);
            NCTestUtils.assertValueAt(-2147483647, 1, 2, array);
            NCTestUtils.assertValueAt(-2147483647, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_MB_perScan_perEVOFshort_leftBorder() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MB();

        try {
            reader.open(iasiFile);

            final Array array = reader.readRaw(0, 634, new Interval(3, 3), "GCcsImageClassifiedNbLin");
            assertNotNull(array);
            final int[] shape = array.getShape();
            assertEquals(2, shape.length);
            assertEquals(3, shape[0]);
            assertEquals(3, shape[1]);

            NCTestUtils.assertValueAt(-32767, 0, 0, array);
            NCTestUtils.assertValueAt(90, 1, 0, array);
            NCTestUtils.assertValueAt(90, 2, 0, array);
            NCTestUtils.assertValueAt(-32767, 0, 1, array);
            NCTestUtils.assertValueAt(90, 1, 1, array);
            NCTestUtils.assertValueAt(90, 2, 1, array);
            NCTestUtils.assertValueAt(-32767, 0, 2, array);
            NCTestUtils.assertValueAt(90, 1, 2, array);
            NCTestUtils.assertValueAt(90, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_MB_perScan_perEVOFbyte_leftUpperCorner() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MB();

        try {
            reader.open(iasiFile);

            final Array array = reader.readRaw(0, 0, new Interval(3, 3), "GEUMAvhrr1BLandFrac");
            assertNotNull(array);
            final int[] shape = array.getShape();
            assertEquals(2, shape.length);
            assertEquals(3, shape[0]);
            assertEquals(3, shape[1]);

            NCTestUtils.assertValueAt(-127, 0, 0, array);
            NCTestUtils.assertValueAt(-127, 1, 0, array);
            NCTestUtils.assertValueAt(-127, 2, 0, array);
            NCTestUtils.assertValueAt(-127, 0, 1, array);
            NCTestUtils.assertValueAt(0, 1, 1, array);
            NCTestUtils.assertValueAt(0, 2, 1, array);
            NCTestUtils.assertValueAt(-127, 0, 2, array);
            NCTestUtils.assertValueAt(0, 1, 2, array);
            NCTestUtils.assertValueAt(0, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_MB_perScan_int_noScaling() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MB();

        try {
            reader.open(iasiFile);

            final Array array = reader.readScaled(23, 109, new Interval(3, 3), "GQisSysTecSondQual");
            assertNotNull(array);
            final int[] shape = array.getShape();
            assertEquals(2, shape.length);
            assertEquals(3, shape[0]);
            assertEquals(3, shape[1]);

            NCTestUtils.assertValueAt(1, 0, 0, array);
            NCTestUtils.assertValueAt(1, 1, 0, array);
            NCTestUtils.assertValueAt(1, 2, 0, array);
            NCTestUtils.assertValueAt(1, 0, 1, array);
            NCTestUtils.assertValueAt(1, 1, 1, array);
            NCTestUtils.assertValueAt(1, 2, 1, array);
            NCTestUtils.assertValueAt(1, 0, 2, array);
            NCTestUtils.assertValueAt(1, 1, 2, array);
            NCTestUtils.assertValueAt(1, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_MB_perPixel_dualInt_scaling() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MB();

        try {
            reader.open(iasiFile);

            final Array array = reader.readScaled(16, 854, new Interval(3, 3), "GGeoSondAnglesMETOP_Azimuth");
            assertNotNull(array);
            final int[] shape = array.getShape();
            assertEquals(2, shape.length);
            assertEquals(3, shape[0]);
            assertEquals(3, shape[1]);

            NCTestUtils.assertValueAt(79.946678, 0, 0, array);
            NCTestUtils.assertValueAt(79.714484, 1, 0, array);
            NCTestUtils.assertValueAt(79.620301, 2, 0, array);
            NCTestUtils.assertValueAt(76.956385, 0, 1, array);
            NCTestUtils.assertValueAt(76.48884, 1, 1, array);
            NCTestUtils.assertValueAt(76.217804, 2, 1, array);
            NCTestUtils.assertValueAt(79.96817, 0, 2, array);
            NCTestUtils.assertValueAt(79.766999, 1, 2, array);
            NCTestUtils.assertValueAt(79.677393, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_MB_() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MB();

        try {
            reader.open(iasiFile);

            final Array array = reader.readAcquisitionTime(23, 109, new Interval(3, 3));
            assertNotNull(array);
            final int[] shape = array.getShape();
            assertEquals(2, shape.length);
            assertEquals(3, shape[0]);
            assertEquals(3, shape[1]);

            NCTestUtils.assertValueAt(1398430511, 0, 0, array);
            NCTestUtils.assertValueAt(1398430511, 1, 0, array);
            NCTestUtils.assertValueAt(1398430512, 2, 0, array);
            NCTestUtils.assertValueAt(1398430511, 0, 1, array);
            NCTestUtils.assertValueAt(1398430511, 1, 1, array);
            NCTestUtils.assertValueAt(1398430512, 2, 1, array);
            NCTestUtils.assertValueAt(1398430519, 0, 2, array);
            NCTestUtils.assertValueAt(1398430519, 1, 2, array);
            NCTestUtils.assertValueAt(1398430520, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadSpectrum_MA_v5() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v5();

        try {
            reader.open(iasiFile);

            final Array array = reader.readSpectrum(24, 110);
            assertNotNull(array);
            final int[] shape = array.getShape();
            assertEquals(1, shape.length);
            assertEquals(8700, shape[0]);

            NCTestUtils.assertValueAt(4.1179999243468046E-4, 0, array);
            NCTestUtils.assertValueAt(4.447000101208687E-4, 101, array);
            NCTestUtils.assertValueAt(4.807999939657748E-4, 1101, array);
            NCTestUtils.assertValueAt(4.6380001003853977E-4, 1208, array);
            NCTestUtils.assertValueAt(4.616999940481037E-4, 1209, array);
            NCTestUtils.assertValueAt(4.5510000200010836E-4, 1210, array);
            NCTestUtils.assertValueAt(9.969209968386869E36, 8698, array);
            NCTestUtils.assertValueAt(9.969209968386869E36, 8699, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadSpectrum_MA_v4() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA_v4();

        try {
            reader.open(iasiFile);

            final Array array = reader.readSpectrum(25, 111);
            assertNotNull(array);
            final int[] shape = array.getShape();
            assertEquals(1, shape.length);
            assertEquals(8700, shape[0]);

            NCTestUtils.assertValueAt(4.780000017490238E-4, 0, array);
            NCTestUtils.assertValueAt(5.212000105530024E-4, 101, array);
            NCTestUtils.assertValueAt(5.335999885573983E-4, 1101, array);
            NCTestUtils.assertValueAt(5.08100027218461E-4, 1208, array);
            NCTestUtils.assertValueAt(5.093999789096415E-4, 1209, array);
            NCTestUtils.assertValueAt(5.041999975219369E-4, 1210, array);
            NCTestUtils.assertValueAt(9.969209968386869E36, 8698, array);
            NCTestUtils.assertValueAt(9.969209968386869E36, 8699, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadSpectrum_MB() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MB();

        try {
            reader.open(iasiFile);

            final Array array = reader.readSpectrum(25, 111);
            assertNotNull(array);
            final int[] shape = array.getShape();
            assertEquals(1, shape.length);
            assertEquals(8700, shape[0]);

            NCTestUtils.assertValueAt(4.555000050459057E-4, 0, array);
            NCTestUtils.assertValueAt(4.650999908335507E-4, 102, array);
            NCTestUtils.assertValueAt(9.07899986486882E-4, 1102, array);
            NCTestUtils.assertValueAt(8.700999896973372E-4, 1209, array);
            NCTestUtils.assertValueAt(8.578000124543905E-4, 1210, array);
            NCTestUtils.assertValueAt(8.409000001847744E-4, 1211, array);
            NCTestUtils.assertValueAt(9.969209968386869E36, 8698, array);
            NCTestUtils.assertValueAt(9.969209968386869E36, 8699, array);
        } finally {
            reader.close();
        }
    }
}

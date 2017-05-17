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
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class IASI_Reader_IO_Test {

    private IASI_Reader reader;

    @Before
    public void setUp() throws IOException {
        reader = new IASI_Reader(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testOpen_alreadyOpenedStreamThrows() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA();

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
    public void testReadAcquisitionInfo_MA() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA();

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
            assertTrue(boundingGeometry instanceof GeometryCollection);
            Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(166, coordinates.length);
            assertEquals(-21.02281951904297, coordinates[3].getLon(), 1e-8);
            assertEquals(66.62924194335938, coordinates[3].getLat(), 1e-8);

            assertEquals(-26.05643653869629, coordinates[79].getLon(), 1e-8);
            assertEquals(48.4214973449707, coordinates[79].getLat(), 1e-8);

            final GeometryCollection collection = (GeometryCollection) boundingGeometry;
            final Geometry[] geometries = collection.getGeometries();
            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = geometries[0].getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 12, 47, 54, 870, time);

            coordinates = geometries[1].getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 13, 37, 26, 642, time);
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
            assertTrue(boundingGeometry instanceof GeometryCollection);
            Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(166, coordinates.length);
            assertEquals(-19.0395393371582, coordinates[2].getLon(), 1e-8);
            assertEquals(64.17292785644531, coordinates[2].getLat(), 1e-8);

            assertEquals(-29.8023567199707, coordinates[78].getLon(), 1e-8);
            assertEquals(41.484779357910156, coordinates[78].getLat(), 1e-8);

            final GeometryCollection collection = (GeometryCollection) boundingGeometry;
            final Geometry[] geometries = collection.getGeometries();
            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = geometries[0].getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2014, 4, 25, 12, 47, 56, 879, time);

            coordinates = geometries[1].getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2014, 4, 25, 13, 37, 24, 671, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_MA() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA();

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
    public void testGetTimeLocator_MA() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA();

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
    public void testGetPixelLocater_MA() throws IOException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA();

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
        final File iasiFile = IASI_TestUtil.getIasiFile_MA();

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
            assertTrue(subScenePixelLocator instanceof IASI_PixelLocator);
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
    public void testReadRaw_MA_perScan_byte() throws IOException, InvalidRangeException {
        final File iasiFile = IASI_TestUtil.getIasiFile_MA();

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
    public void testReadRaw_MB_perScan_int() throws IOException, InvalidRangeException {
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
}

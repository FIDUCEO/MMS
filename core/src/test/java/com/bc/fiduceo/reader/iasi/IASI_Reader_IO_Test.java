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
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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

    private File testDataDirectory;
    private IASI_Reader reader;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory();
        reader = new IASI_Reader(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testOpen_alreadyOpenedStreamThrows() throws IOException {
        final File iasiFile = getIasiFile_MA();

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
        final File iasiFile = getIasiFile_MA();

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
            final Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(166, coordinates.length);
            assertEquals(-21.02281951904297, coordinates[3].getLon(), 1e-8);
            assertEquals(66.62924194335938, coordinates[3].getLat(), 1e-8);

            assertEquals(-26.05643653869629, coordinates[79].getLon(), 1e-8);
            assertEquals(48.4214973449707, coordinates[79].getLat(), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_MB() throws IOException {
        final File iasiFile = getIasiFile_MB();

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
            final Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(166, coordinates.length);
            assertEquals(-19.0395393371582, coordinates[2].getLon(), 1e-8);
            assertEquals(64.17292785644531, coordinates[2].getLat(), 1e-8);

            assertEquals(-29.8023567199707, coordinates[78].getLon(), 1e-8);
            assertEquals(41.484779357910156, coordinates[78].getLat(), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_MA() throws IOException {
        final File iasiFile = getIasiFile_MA();

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
        final File iasiFile = getIasiFile_MA();

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
    public void testGetTimeLocator_MB() throws IOException {
        final File iasiFile = getIasiFile_MB();

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
        final File iasiFile = getIasiFile_MB();

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

    private File getIasiFile_MA() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"iasi-ma", "v3-6N", "2016", "01", "IASI_xxx_1C_M02_20160101124754Z_20160101142658Z_N_O_20160101142620Z.nat"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }

    private File getIasiFile_MB() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"iasi-mb", "v7-0N", "2014", "04", "IASI_xxx_1C_M01_20140425124756Z_20140425142652Z_N_O_20140425133911Z.nat"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}

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

package com.bc.fiduceo.reader.atsr;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
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

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class ATSR_L1B_Reader_IO_Test {

    private File dataDirectory;
    private ATSR_L1B_Reader reader;

    @Before
    public void setUp() throws IOException {
        dataDirectory = TestUtil.getTestDataDirectory();
        reader = new ATSR_L1B_Reader(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testReadAcquisitionInfo_ATSR1() throws IOException {
        final File file = getAtsr1File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1993, 8, 5, 21, 0, 30, 240, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1993, 8, 5, 22, 41, 8, 490, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);
            final Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(137, coordinates.length);
            assertEquals(24.824844360351562, coordinates[0].getLon(), 1e-8);
            assertEquals(0.4836359918117523, coordinates[0].getLat(), 1e-8);

            assertEquals(-5.181085109710694, coordinates[68].getLon(), 1e-8);
            assertEquals(-0.4404639899730683, coordinates[68].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1993, 8, 5, 21, 0, 30, 365, time);
            time = timeAxes[0].getTime(coordinates[68]);
            TestUtil.assertCorrectUTCDate(1993, 8, 5, 22, 41, 8, 367, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_ATSR2() throws IOException {
        final File file = getAtsr2File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1998, 4, 24, 5, 57, 54, 720, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1998, 4, 24, 7, 38, 32, 970, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);
            final Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(137, coordinates.length);
            assertEquals(-110.77146911621094, coordinates[1].getLon(), 1e-8);
            assertEquals(6.13779592514038, coordinates[1].getLat(), 1e-8);

            assertEquals(-138.29689025878906, coordinates[69].getLon(), 1e-8);
            assertEquals(-6.176464080810547, coordinates[69].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1998, 4, 24, 5, 57, 54, 845, time);
            time = timeAxes[0].getTime(coordinates[68]);
            TestUtil.assertCorrectUTCDate(1998, 4, 24, 7, 38, 32, 846, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_AATSR() throws IOException {
        final File file = getAatsrFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2006, 2, 15, 7, 8, 52, 812, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2006, 2, 15, 8, 57, 40, 662, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);
            final Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(147, coordinates.length);
            assertEquals(-135.95962524414062, coordinates[2].getLon(), 1e-8);
            assertEquals(3.0329780578613286, coordinates[2].getLat(), 1e-8);

            assertEquals(-170.1296844482422, coordinates[73].getLon(), 1e-8);
            assertEquals(19.727405548095703, coordinates[73].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2006, 2, 15, 7, 8, 52, 892, time);
            time = timeAxes[0].getTime(coordinates[73]);
            TestUtil.assertCorrectUTCDate(2006, 2, 15, 8, 57, 40, 644, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_ATSR1() throws IOException {
        final File file = getAtsr1File();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(744584430240L, timeLocator.getTimeFor(15, 0));
            assertEquals(744584445240L, timeLocator.getTimeFor(16, 100));
            assertEquals(744585930240L, timeLocator.getTimeFor(20, 10000));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_ATSR2() throws IOException {
        final File file = getAtsr2File();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(893397474720L, timeLocator.getTimeFor(15, 0));
            assertEquals(893400484770L, timeLocator.getTimeFor(18, 20067));
            assertEquals(893403512970L, timeLocator.getTimeFor(20, 40255));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_AATSR() throws IOException {
        final File file = getAatsrFile();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(1139987332812L, timeLocator.getTimeFor(16, 0));
            assertEquals(1139990490912L, timeLocator.getTimeFor(198, 21054));
            assertEquals(1139993860662L, timeLocator.getTimeFor(22, 43519));
        } finally {
            reader.close();
        }
    }

    private File getAtsr1File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"atsr-e1", "v3", "1993", "08", "05", "AT1_TOA_1PURAL19930805_210030_000000004015_00085_10751_0000.E1"}, false);
        return getFileAsserted(testFilePath);
    }

    private File getAtsr2File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"atsr-e2", "v3", "1998", "04", "24", "AT2_TOA_1PURAL19980424_055754_000000001031_00348_15733_0000.E2"}, false);
        return getFileAsserted(testFilePath);
    }

    private File getAatsrFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"aatsr-en", "v3", "2006", "02", "15", "ATS_TOA_1PUUPA20060215_070852_000065272045_00120_20715_4282.N1"}, false);
        return getFileAsserted(testFilePath);
    }

    private File getFileAsserted(String testFilePath) {
        final File file = new File(dataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}

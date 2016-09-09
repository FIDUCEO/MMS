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

package com.bc.fiduceo.reader.ssmt2;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.reader.AcquisitionInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class SSMT2_Reader_IO_Test {

    private SSMT2_Reader reader;
    private File testDataDirectory;

    @Before
    public void setUp() throws IOException {
        reader = new SSMT2_Reader(new GeometryFactory(GeometryFactory.Type.S2));
        testDataDirectory = TestUtil.getTestDataDirectory();
    }

    @Test
    public void testReadAcquisitionInfo_F11() throws IOException, ParseException {
        final File f11File = createSSMT2_F11_File();

        try {
            reader.open(f11File);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1994, 1, 28, 4, 12, 22, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1994, 1, 28, 5, 54, 16, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.ASCENDING, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);

            Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(75, coordinates.length);

            assertEquals(-149.76181030273438, coordinates[0].getLon(), 1e-8);
            assertEquals(1.3309934139251711, coordinates[0].getLat(), 1e-8);

            assertEquals(-126.34518432617186, coordinates[24].getLon(), 1e-8);
            assertEquals(-69.82559204101562, coordinates[24].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            coordinates = timeAxes[0].getGeometry().getCoordinates();
            final Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1994, 1, 28, 4, 12, 22, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_F14() throws IOException, ParseException {
        final File f14File = createSSMT2_F14_File();

        try {
            reader.open(f14File);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2001, 6, 14, 12, 29, 4, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2001, 6, 14, 14, 10, 58, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.ASCENDING, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);

            Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(75, coordinates.length);

            assertEquals(128.3829803466797, coordinates[0].getLon(), 1e-8);
            assertEquals(1.6132754087448118, coordinates[0].getLat(), 1e-8);

            assertEquals(150.22152709960938, coordinates[24].getLon(), 1e-8);
            assertEquals(-69.72442626953125, coordinates[24].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            coordinates = timeAxes[0].getGeometry().getCoordinates();
            final Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2001, 6, 14, 12, 29, 4, time);
        } finally {
            reader.close();
        }
    }

    private File createSSMT2_F11_File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"ssmt2-f11", "v01", "1994", "01", "28", "F11199401280412.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }

    private File createSSMT2_F14_File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"ssmt2-f14", "v01", "2001", "06", "14", "F14200106141229.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}

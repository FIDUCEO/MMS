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

package com.bc.fiduceo.reader.amsre;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class AMSRE_Reader_IO_Test {

    private File testDataDirectory;
    private AMSRE_Reader reader;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory();
        reader = new AMSRE_Reader(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File file = createAmsreFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2005, 2, 17, 5, 36, 6, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2005, 2, 17, 6, 25, 56, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.DESCENDING, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);

            Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(67, coordinates.length);

            assertEquals(111.96620178222658, coordinates[0].getLon(), 1e-8);
            assertEquals(83.54039001464844, coordinates[0].getLat(), 1e-8);

            assertEquals(-159.9243621826172, coordinates[23].getLon(), 1e-8);
            assertEquals(-77.13606262207031, coordinates[23].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);

            final TimeAxis timeAxis = timeAxes[0];
            coordinates = timeAxis.getGeometry().getCoordinates();
            final Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2005, 2, 17, 5, 36, 6, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator() throws IOException {
        final File file = createAmsreFile();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);
            assertTrue(timeLocator instanceof AMSRE_TimeLocator);

            long time = timeLocator.getTimeFor(67, 0);
            assertEquals(1108618539107L, time);
            TestUtil.assertCorrectUTCDate(2005, 2, 17, 5, 35, 39, new Date(time));

            time = timeLocator.getTimeFor(68, 1000);
            assertEquals(1108620038998L, time);

            time = timeLocator.getTimeFor(68, 1994);
            assertEquals(1108621529890L, time);
            TestUtil.assertCorrectUTCDate(2005, 2, 17, 6, 25, 29, new Date(time));
        } finally {
            reader.close();
        }
    }

    private File createAmsreFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"amsre-aq", "v12", "2005", "02", "17", "AMSR_E_L2A_BrightnessTemperatures_V12_200502170536_D.hdf"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}

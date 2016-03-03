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
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.GeometryFactory;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class AVHRR_GAC_Reader_IO_Test {

    @Test
    public void testReadAcquisitionInfo_NOAA17() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File file = new File(testDataDirectory, "20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc");
        assertTrue(file.isFile());

        final AVHRR_GAC_Reader reader = new AVHRR_GAC_Reader();
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
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_NOAA18() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File file = new File(testDataDirectory, "20070401080400-ESACCI-L1C-AVHRR18_G-fv01.0.nc");
        assertTrue(file.isFile());

        final AVHRR_GAC_Reader reader = new AVHRR_GAC_Reader();
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
        } finally {
            reader.close();
        }
    }
}

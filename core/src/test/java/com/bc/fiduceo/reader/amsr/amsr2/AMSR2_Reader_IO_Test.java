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

package com.bc.fiduceo.reader.amsr.amsr2;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.GeometryUtil;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.reader.AcquisitionInfo;
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
public class AMSR2_Reader_IO_Test {

    private File testDataDirectory;
    private AMSR2_Reader reader;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory();
        reader = new AMSR2_Reader(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File amsr2File = getAmsr2File();

        try {
            reader.open(amsr2File);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2013, 7, 1, 9, 42, 53, 154, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2013, 7, 1, 10, 32, 27, 365, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.ASCENDING, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);

            Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(57, coordinates.length);

            assertEquals(176.48370361328125, coordinates[0].getLon(), 1e-8);
            assertEquals(-72.13760375976562, coordinates[0].getLat(), 1e-8);

            assertEquals(-64.33760070800781, coordinates[28].getLon(), 1e-8);
            assertEquals(81.37730407714844, coordinates[28].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);

            final TimeAxis timeAxis = timeAxes[0];
            coordinates = timeAxis.getGeometry().getCoordinates();
            assertEquals(22, coordinates.length);
            assertEquals(127.08951568603516, coordinates[1].getLon(), 1e-8);
            assertEquals(-76.40843963623047, coordinates[1].getLat(), 1e-8);

            assertEquals(52.38159942626954, coordinates[12].getLon(), 1e-8);
            assertEquals(14.875041007995609, coordinates[12].getLat(), 1e-8);


            Date time = timeAxis.getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2013, 7, 1, 9, 42, 53, time);

            time = timeAxis.getTime(coordinates[12]);
            TestUtil.assertCorrectUTCDate(2013, 7, 1, 10, 12, 1, time);

        } finally {
            reader.close();
        }
    }

    private File getAmsr2File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"amsr2-gcw1", "v220", "2013", "07", "01", "GW1AM2_201307010942_035A_L1SGRTBR_2220220.h5"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}

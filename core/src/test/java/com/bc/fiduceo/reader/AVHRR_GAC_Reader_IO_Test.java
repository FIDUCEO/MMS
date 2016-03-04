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
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.TimeAxis;
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
public class AVHRR_GAC_Reader_IO_Test {

    private File testDataDirectory;
    private GeometryFactory geometryFactory;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory();
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
    }

    @Test
    public void testReadAcquisitionInfo_NOAA17() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n17", "1.01", "2007", "04", "01", "20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
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
            final GeometryCollection geometryCollection = (GeometryCollection) boundingGeometry;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);

            Point[] coordinates = geometries[0].getCoordinates();
            assertEquals(161, coordinates.length);
            assertEquals(-66.97299194335938, coordinates[0].getLon(), 1e-8);
            assertEquals(-5.238999843597412, coordinates[0].getLat(), 1e-8);

            assertEquals(-74.2349853515625, coordinates[23].getLon(), 1e-8);
            assertEquals(61.316001892089844, coordinates[23].getLat(), 1e-8);

            coordinates = geometries[1].getCoordinates();
            assertEquals(161, coordinates.length);
            assertEquals(69.81500244140625, coordinates[0].getLon(), 1e-8);
            assertEquals(-12.913000106811522, coordinates[0].getLat(), 1e-8);

            assertEquals(7.164999961853029, coordinates[23].getLon(), 1e-8);
            assertEquals(-67.44300079345703, coordinates[23].getLat(), 1e-8);

            final TimeAxis timeAxis = acquisitionInfo.getTimeAxis();
            assertNotNull(timeAxis);
            final Date time = timeAxis.getTime(geometryFactory.createPoint(-66.97299194335938, -5.238999843597412));
            TestUtil.assertCorrectUTCDate(2007, 4, 1, 3, 34, 54, 0, time);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_NOAA18() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n18", "1.02", "2007", "04", "01", "20070401080400-ESACCI-L1C-AVHRR18_G-fv01.0.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
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
            final GeometryCollection geometryCollection = (GeometryCollection) boundingGeometry;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);

            Point[] coordinates = geometries[0].getCoordinates();
            assertEquals(147, coordinates.length);
            assertEquals(-110.22799682617189, coordinates[17].getLon(), 1e-8);
            assertEquals(38.00899887084961, coordinates[17].getLat(), 1e-8);

            assertEquals(177.14199829101562, coordinates[58].getLon(), 1e-8);
            assertEquals(-66.05000305175781, coordinates[58].getLat(), 1e-8);

            coordinates = geometries[1].getCoordinates();
            assertEquals(147, coordinates.length);
            assertEquals(153.96200561523438, coordinates[0].getLon(), 1e-8);
            assertEquals(-66.73899841308594, coordinates[0].getLat(), 1e-8);

            assertEquals(83.5780029296875, coordinates[23].getLon(), 1e-8);
            assertEquals(-16.940000534057617, coordinates[23].getLat(), 1e-8);

            final TimeAxis timeAxis = acquisitionInfo.getTimeAxis();
            assertNotNull(timeAxis);
            coordinates = geometries[0].getCoordinates();
            // @todo 1 tb/tb continue here 2016-03-04
//            final Date time = timeAxis.getTime(coordinates[2]);
//            TestUtil.assertCorrectUTCDate(2007, 4, 1, 8, 4, 12, 0, time);
        } finally {
            reader.close();
        }
    }
}

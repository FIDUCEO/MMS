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

/*
 * Copyright (C) 2015 Brockmann Consult GmbH
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
package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;


@RunWith(IOTestRunner.class)
public class AMSUB_MHS_L1C_Reader_IO_Test {

    private AMSUB_MHS_L1C_Reader reader;
    private File testDataDirectory;

    @Before
    public void setUp() throws IOException {
        reader = new AMSUB_MHS_L1C_Reader();
        testDataDirectory = TestUtil.getTestDataDirectory();
    }

    @Test
    public void testReadAcquisitionInfo_AMSUB_NOAA15() throws IOException, ParseException {
        final File amsubFile = createAmsubNOAA15Path("L0496703.NSS.AMBX.NK.D07234.S0630.E0824.B4821011.WI.h5");

        try {
            reader.open(amsubFile);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 6, 30, 29, 119, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 8, 24, 23, 785, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertSplittedBoundingGeometry(boundingGeometry);

            final GeometryCollection geometryCollection = (GeometryCollection) boundingGeometry;
            final Geometry[] geometries = geometryCollection.getGeometries();
            Point[] coordinates = geometries[0].getCoordinates();
            assertEquals(85, coordinates.length);
            assertCorrectCoordinate(134.5867, 51.186, coordinates[0]);
            assertCorrectCoordinate(-30.2003, 36.4925, coordinates[24]);

            coordinates = geometries[1].getCoordinates();
            assertEquals(85, coordinates.length);
            assertCorrectCoordinate(-26.8742, -78.5383, coordinates[0]);
            assertCorrectCoordinate(152.2549, -11.3641, coordinates[24]);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = geometries[0].getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 6, 30, 33, 808, time);

            coordinates = geometries[1].getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 7, 27, 26, 452, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_AMSUB_NOAA15_laterAcquisition() throws IOException, ParseException {
        final File amsubFile = createAmsubNOAA15Path("L0522933.NSS.AMBX.NK.D07234.S1640.E1824.B4821617.GC.h5");

        try {
            reader.open(amsubFile);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 16, 40, 37, 120, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 18, 24, 53, 119, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertSplittedBoundingGeometry(boundingGeometry);

            final GeometryCollection geometryCollection = (GeometryCollection) boundingGeometry;
            final Geometry[] geometries = geometryCollection.getGeometries();
            Point[] coordinates = geometries[0].getCoordinates();
            assertEquals(77, coordinates.length);
            assertCorrectCoordinate(-29.0474, 61.1013, coordinates[0]);
            assertCorrectCoordinate(174.4584, 14.7534, coordinates[24]);

            coordinates = geometries[1].getCoordinates();
            assertEquals(77, coordinates.length);
            assertCorrectCoordinate(-179.2796, -75.2729, coordinates[0]);
            assertCorrectCoordinate(-0.1681, -8.6179, coordinates[24]);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = geometries[0].getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 16, 40, 39, 757, time);

            coordinates = geometries[1].getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 17, 32, 45, 119, time);
        } finally {
            reader.close();
        }
    }


    @Test
    public void testReadAcquisitionInfo_MHS_NOAA18() throws IOException, ParseException {
        final File mhsFile = createMhsNOAA18Path("NSS.MHSX.NN.D07234.S1010.E1156.B1161920.GC.h5");

        try {
            reader.open(mhsFile);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 10, 10, 7, 277, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 11, 56, 28, 610, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertSplittedBoundingGeometry(boundingGeometry);

            final GeometryCollection geometryCollection = (GeometryCollection) boundingGeometry;
            final Geometry[] geometries = geometryCollection.getGeometries();
            Point[] coordinates = geometries[0].getCoordinates();

            assertEquals(79, coordinates.length);
            assertCorrectCoordinate(6.8728, 63.4891, coordinates[0]);
            assertCorrectCoordinate(-144.1532, 9.9494, coordinates[25]);

            coordinates = geometries[1].getCoordinates();
            assertEquals(79, coordinates.length);
            assertCorrectCoordinate(-131.5143, -78.9114, coordinates[0]);
            assertCorrectCoordinate(40.2073, 5.992, coordinates[26]);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = geometries[0].getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 10, 10, 8, 839, time);

            coordinates = geometries[1].getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2007, 8, 22, 11, 3, 17, 943, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_AMSUB_NOAA15() throws IOException {
        final File amsubFile = createAmsubNOAA15Path("L0496703.NSS.AMBX.NK.D07234.S0630.E0824.B4821011.WI.h5");

        try {
            reader.open(amsubFile);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(1187764229119L, timeLocator.getTimeFor(1, 0));
            assertEquals(1187764570452L, timeLocator.getTimeFor(2, 128));
            assertEquals(1187766986453L, timeLocator.getTimeFor(3, 1034));
            assertEquals(1187771063785L, timeLocator.getTimeFor(4, 2563));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_AMSUB_NOAA15_callingTwiceReturnsSameObject() throws IOException {
        final File amsubFile = createAmsubNOAA15Path("L0496703.NSS.AMBX.NK.D07234.S0630.E0824.B4821011.WI.h5");

        try {
            reader.open(amsubFile);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            final TimeLocator timeLocator_2 = reader.getTimeLocator();
            assertNotNull(timeLocator_2);

            assertSame(timeLocator, timeLocator_2);

        } finally {
            reader.close();
        }
    }

    private File createAmsubNOAA15Path(String fileName) {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"amsub-n15", "v1.0", "2007", "08", "22", fileName}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }

    private File createMhsNOAA18Path(String fileName) {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"mhs-n18", "v1.0", "2007", "08", "22", fileName}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }

    private void assertSplittedBoundingGeometry(Geometry boundingGeometry) {
        assertNotNull(boundingGeometry);
        assertTrue(boundingGeometry instanceof GeometryCollection);
        final GeometryCollection geometryCollection = (GeometryCollection) boundingGeometry;
        final Geometry[] geometries_2 = geometryCollection.getGeometries();
        assertEquals(2, geometries_2.length);
    }

    private void assertCorrectCoordinate(double expectedLon, double expectedLat, Point coordinate) {
        assertEquals(expectedLon, coordinate.getLon(), 1e-8);
        assertEquals(expectedLat, coordinate.getLat(), 1e-8);
    }
}

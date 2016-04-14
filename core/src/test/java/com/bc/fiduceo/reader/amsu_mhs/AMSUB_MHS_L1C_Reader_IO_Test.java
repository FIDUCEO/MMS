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
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.awt.geom.Point2D;
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
            assertCorrectCoordinate(134.58669660005398, 51.18599870693288, coordinates[0]);
            assertCorrectCoordinate(-30.20029923707625, 36.49249907812191, coordinates[24]);

            coordinates = geometries[1].getCoordinates();
            assertEquals(85, coordinates.length);
            assertCorrectCoordinate(-26.874199321100605, -78.53829801595566, coordinates[0]);
            assertCorrectCoordinate(152.2548961537177, -11.364099712918687, coordinates[24]);

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
            assertCorrectCoordinate(-29.04739926620095, 61.10129845645133, coordinates[0]);
            assertCorrectCoordinate(174.45839559281012, 14.75339962729777, coordinates[24]);

            coordinates = geometries[1].getCoordinates();
            assertEquals(77, coordinates.length);
            assertCorrectCoordinate(-179.27959547101636, -75.27289809844662, coordinates[0]);
            assertCorrectCoordinate(-0.1681, -8.617899782293533, coordinates[24]);

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
            assertCorrectCoordinate(6.872799826378468, 63.48909839613043, coordinates[0]);
            assertCorrectCoordinate(-144.15319635838387, 9.949399748657015, coordinates[25]);

            coordinates = geometries[1].getCoordinates();
            assertEquals(79, coordinates.length);
            assertCorrectCoordinate(-131.51429667766934, -78.91139800653036, coordinates[0]);
            assertCorrectCoordinate(40.20729898427817, 5.991999848629348, coordinates[26]);

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

    @Test
    public void testGetTimeLocator_MHS_NOAA18() throws IOException {
        final File mhsFile = createMhsNOAA18Path("NSS.MHSX.NN.D07234.S1332.E1518.B1162122.GC.h5");

        try {
            reader.open(mhsFile);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(1187789535277L, timeLocator.getTimeFor(1, 0));
            assertEquals(1187789876610L, timeLocator.getTimeFor(2, 128));
            assertEquals(1187792292609L, timeLocator.getTimeFor(3, 1034));
            assertEquals(1187795892609L, timeLocator.getTimeFor(4, 2384));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator_MHS_NOAA18() throws IOException, ParseException {
        final File mhsFile = createMhsNOAA18Path("NSS.MHSX.NN.D07234.S1010.E1156.B1161920.GC.h5");

        try {
            reader.open(mhsFile);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            Point2D geoLocation = pixelLocator.getGeoLocation(0.5, 0.5, null);
            assertEquals(6.872799873352051, geoLocation.getX(), 1e-8);  // original 6.8728
            assertEquals(63.489097595214844, geoLocation.getY(), 1e-8); // original 63.4891

            Point2D[] pixelLocation = pixelLocator.getPixelLocation(6.8728, 63.4891);
            assertEquals(2, pixelLocation.length);  // is part of self intersecting area
            assertEquals(0.48546158480835666, pixelLocation[0].getX(), 1e-8);    // original 0.5
            assertEquals(0.606753391875949, pixelLocation[0].getY(), 1e-8);    // original 0.5

            assertEquals(40.26355242809091, pixelLocation[1].getX(), 1e-8);
            assertEquals(2258.6934166055794, pixelLocation[1].getY(), 1e-8);

            //-------------------------------------------------------------
            geoLocation = pixelLocator.getGeoLocation(10.5, 246.5, null);
            assertEquals(-94.14899444580078, geoLocation.getX(), 1e-8);  // original -94.149
            assertEquals(65.11270141601562, geoLocation.getY(), 1e-8); // original 65.3599

            pixelLocation = pixelLocator.getPixelLocation(-94.149, 65.3599);
            assertEquals(1, pixelLocation.length);
            assertEquals(11.056689038123926, pixelLocation[0].getX(), 1e-8);    // original 10.5
            assertEquals(245.3098948236201, pixelLocation[0].getY(), 1e-8);    // original 246.5

            //-------------------------------------------------------------
            geoLocation = pixelLocator.getGeoLocation(20.5, 484.5, null);
            assertEquals(-123.38299560546875, geoLocation.getX(), 1e-8);  // original -123.383
            assertEquals(31.553499221801758, geoLocation.getY(), 1e-8); // original 31.5535

            pixelLocation = pixelLocator.getPixelLocation(-123.383, 31.5535);
            assertEquals(1, pixelLocation.length);  // is part of self intersecting area
            assertEquals(20.51628831614342, pixelLocation[0].getX(), 1e-8);    // original 20.5
            assertEquals(484.4601792290086, pixelLocation[0].getY(), 1e-8);    // original 484.5

            //-------------------------------------------------------------
            geoLocation = pixelLocator.getGeoLocation(30.5, 727.5, null);
            assertEquals(-134.95899963378906, geoLocation.getX(), 1e-8);  // original -134.959
            assertEquals(-5.781399726867676, geoLocation.getY(), 1e-8); // original -5.7814

            pixelLocation = pixelLocator.getPixelLocation(-134.959, -5.7814);
            assertEquals(1, pixelLocation.length);  // is part of self intersecting area
            assertEquals(30.45967510530061, pixelLocation[0].getX(), 1e-8);    // original 30.5
            assertEquals(727.5172845630435, pixelLocation[0].getY(), 1e-8);    // original 727.5

            //-------------------------------------------------------------
            geoLocation = pixelLocator.getGeoLocation(40.5, 1024.5, null);
            assertEquals(-149.65489196777344, geoLocation.getX(), 1e-8);  // original -149.6549
            assertEquals(-51.36819839477539, geoLocation.getY(), 1e-8); // original -51.3682

            pixelLocation = pixelLocator.getPixelLocation(-149.6549, -51.3682);
            assertEquals(1, pixelLocation.length);  // is part of self intersecting area
            assertEquals(40.58351743748158, pixelLocation[0].getX(), 1e-8);    // original 40.5
            assertEquals(1024.4990507940652, pixelLocation[0].getY(), 1e-8);    // original 1024.5

            //-------------------------------------------------------------
            geoLocation = pixelLocator.getGeoLocation(50.5, 1301.5, null);
            assertEquals(99.39009857177734, geoLocation.getX(), 1e-8);  // original 99.3901
            assertEquals(-79.1156997680664, geoLocation.getY(), 1e-8); // original -79.1157

            pixelLocation = pixelLocator.getPixelLocation(99.3901, -79.1157);
            assertEquals(1, pixelLocation.length);  // is part of self intersecting area
            assertEquals(50.484098850555775, pixelLocation[0].getX(), 1e-8);    // original 50.5
            assertEquals(1301.4747256336116, pixelLocation[0].getY(), 1e-8);    // original 1301.5

            //-------------------------------------------------------------
            geoLocation = pixelLocator.getGeoLocation(60.5, 1504.5, null);
            assertEquals(50.50239944458008, geoLocation.getX(), 1e-8);  // original 50.5024
            assertEquals(-51.80769729614258, geoLocation.getY(), 1e-8); // original -51.8077

            pixelLocation = pixelLocator.getPixelLocation(50.5024, -51.8077);
            assertEquals(1, pixelLocation.length);  // is part of self intersecting area
            assertEquals(60.49520617819131, pixelLocation[0].getX(), 1e-8);    // original 60.5
            assertEquals(1504.5986111709697, pixelLocation[0].getY(), 1e-8);    // original 1504.5

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

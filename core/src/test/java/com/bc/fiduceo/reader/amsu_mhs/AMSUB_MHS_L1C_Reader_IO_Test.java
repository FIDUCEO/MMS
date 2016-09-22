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
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.util.TimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;
import ucar.nc2.iosp.netcdf3.N3iosp;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;


@SuppressWarnings("ThrowFromFinallyBlock")
@RunWith(IOTestRunner.class)
public class AMSUB_MHS_L1C_Reader_IO_Test {

    private AMSUB_MHS_L1C_Reader reader;
    private File testDataDirectory;

    @Before
    public void setUp() throws IOException {
        reader = new AMSUB_MHS_L1C_Reader(new GeometryFactory(GeometryFactory.Type.S2));
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
            assertEquals(1, pixelLocation.length);
            assertEquals(20.51628831614342, pixelLocation[0].getX(), 1e-8);    // original 20.5
            assertEquals(484.4601792290086, pixelLocation[0].getY(), 1e-8);    // original 484.5

            //-------------------------------------------------------------
            geoLocation = pixelLocator.getGeoLocation(30.5, 727.5, null);
            assertEquals(-134.95899963378906, geoLocation.getX(), 1e-8);  // original -134.959
            assertEquals(-5.781399726867676, geoLocation.getY(), 1e-8); // original -5.7814

            pixelLocation = pixelLocator.getPixelLocation(-134.959, -5.7814);
            assertEquals(1, pixelLocation.length);
            assertEquals(30.45967510530061, pixelLocation[0].getX(), 1e-8);    // original 30.5
            assertEquals(727.5172845630435, pixelLocation[0].getY(), 1e-8);    // original 727.5

            //-------------------------------------------------------------
            geoLocation = pixelLocator.getGeoLocation(40.5, 1024.5, null);
            assertEquals(-149.65489196777344, geoLocation.getX(), 1e-8);  // original -149.6549
            assertEquals(-51.36819839477539, geoLocation.getY(), 1e-8); // original -51.3682

            pixelLocation = pixelLocator.getPixelLocation(-149.6549, -51.3682);
            assertEquals(1, pixelLocation.length);
            assertEquals(40.58351743748158, pixelLocation[0].getX(), 1e-8);    // original 40.5
            assertEquals(1024.4990507940652, pixelLocation[0].getY(), 1e-8);    // original 1024.5

            //-------------------------------------------------------------
            geoLocation = pixelLocator.getGeoLocation(50.5, 1301.5, null);
            assertEquals(99.39009857177734, geoLocation.getX(), 1e-8);  // original 99.3901
            assertEquals(-79.1156997680664, geoLocation.getY(), 1e-8); // original -79.1157

            pixelLocation = pixelLocator.getPixelLocation(99.3901, -79.1157);
            assertEquals(1, pixelLocation.length);
            assertEquals(50.484098850555775, pixelLocation[0].getX(), 1e-8);    // original 50.5
            assertEquals(1301.4747256336116, pixelLocation[0].getY(), 1e-8);    // original 1301.5

            //-------------------------------------------------------------
            geoLocation = pixelLocator.getGeoLocation(60.5, 1504.5, null);
            assertEquals(50.50239944458008, geoLocation.getX(), 1e-8);  // original 50.5024
            assertEquals(-51.80769729614258, geoLocation.getY(), 1e-8); // original -51.8077

            pixelLocation = pixelLocator.getPixelLocation(50.5024, -51.8077);
            assertEquals(1, pixelLocation.length);
            assertEquals(60.49520617819131, pixelLocation[0].getX(), 1e-8);    // original 60.5
            assertEquals(1504.5986111709697, pixelLocation[0].getY(), 1e-8);    // original 1504.5

        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator_AMSUB_NOAA15() throws IOException, ParseException {
        final File amsubFile = createAmsubNOAA15Path("L0496703.NSS.AMBX.NK.D07234.S0630.E0824.B4821011.WI.h5");

        try {
            reader.open(amsubFile);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            Point2D geoLocation = pixelLocator.getGeoLocation(0.5, 0.5, null);
            assertEquals(134.58670043945312, geoLocation.getX(), 1e-8);  // original 134.5867
            assertEquals(51.185997009277344, geoLocation.getY(), 1e-8); // original 51.186

            Point2D[] pixelLocation = pixelLocator.getPixelLocation(134.5867, 51.186);
            assertEquals(2, pixelLocation.length);  // we are in the overlapping area
            assertEquals(0.4715504690469804, pixelLocation[0].getX(), 1e-8);    // original 0.5
            assertEquals(0.8111074622107415, pixelLocation[0].getY(), 1e-8);    // original 0.5

            assertEquals(77.69149200396195, pixelLocation[1].getX(), 1e-8);
            assertEquals(2247.573633028767, pixelLocation[1].getY(), 1e-8);

            //-------------------------------------------------------------
            geoLocation = pixelLocator.getGeoLocation(10.5, 246.5, null);
            assertEquals(52.30149841308594, geoLocation.getX(), 1e-8);  // original 52.3015
            assertEquals(74.90239715576172, geoLocation.getY(), 1e-8); // original 74.9024

            pixelLocation = pixelLocator.getPixelLocation(52.3015, 74.9024);
            assertEquals(2, pixelLocation.length);  // we are in the overlapping area
            assertEquals(10.518601337554388, pixelLocation[0].getX(), 1e-8);    // original 10.5
            assertEquals(246.49994190945807, pixelLocation[0].getY(), 1e-8);    // original 246.5

            assertEquals(8.909629189213087, pixelLocation[1].getX(), 1e-8);
            assertEquals(2481.1179758565668, pixelLocation[1].getY(), 1e-8);

            //-------------------------------------------------------------
            geoLocation = pixelLocator.getGeoLocation(20.5, 484.5, null);
            assertEquals(-9.354100227355957, geoLocation.getX(), 1e-8);  // original -9.3541
            assertEquals(46.54669952392578, geoLocation.getY(), 1e-8); // original 46.5467

            pixelLocation = pixelLocator.getPixelLocation(-9.3541, 46.5467);
            assertEquals(1, pixelLocation.length);
            assertEquals(20.549521097468954, pixelLocation[0].getX(), 1e-8);    // original 20.5
            assertEquals(484.47231359716176, pixelLocation[0].getY(), 1e-8);    // original 484.5

            //-------------------------------------------------------------
            geoLocation = pixelLocator.getGeoLocation(30.5, 727.5, null);
            assertEquals(-23.289798736572266, geoLocation.getX(), 1e-8);  // original -23.2898
            assertEquals(9.112299919128418, geoLocation.getY(), 1e-8); // original 9.1123

            pixelLocation = pixelLocator.getPixelLocation(-23.2898, 9.1123);
            assertEquals(1, pixelLocation.length);
            assertEquals(30.589228290151972, pixelLocation[0].getX(), 1e-8);    // original 30.5
            assertEquals(727.4296637874371, pixelLocation[0].getY(), 1e-8);    // original 727.5

            //-------------------------------------------------------------
            geoLocation = pixelLocator.getGeoLocation(40.5, 1024.5, null);
            assertEquals(-35.8302001953125, geoLocation.getX(), 1e-8);  // original -35.8302
            assertEquals(-37.38059997558594, geoLocation.getY(), 1e-8); // original -37.3806

            pixelLocation = pixelLocator.getPixelLocation(-35.8302, -37.3806);
            assertEquals(1, pixelLocation.length);
            assertEquals(40.47791010604271, pixelLocation[0].getX(), 1e-8);    // original 40.5
            assertEquals(1024.3933174972674, pixelLocation[0].getY(), 1e-8);    // original 1024.5

            //-------------------------------------------------------------
            geoLocation = pixelLocator.getGeoLocation(50.5, 1301.5, null);
            assertEquals(-80.20600128173828, geoLocation.getX(), 1e-8);  // original -80.206
            assertEquals(-77.39289855957031, geoLocation.getY(), 1e-8); // original -77.3929

            pixelLocation = pixelLocator.getPixelLocation(-80.206, -77.3929);
            assertEquals(1, pixelLocation.length);
            assertEquals(50.48494718955499, pixelLocation[0].getX(), 1e-8);    // original 50.5
            assertEquals(1301.4961686392523, pixelLocation[0].getY(), 1e-8);    // original 1301.5

            //-------------------------------------------------------------
            geoLocation = pixelLocator.getGeoLocation(60.5, 1504.5, null);
            assertEquals(168.4783935546875, geoLocation.getX(), 1e-8);  // original 168.4784
            assertEquals(-64.35199737548828, geoLocation.getY(), 1e-8); // original -64.352

            pixelLocation = pixelLocator.getPixelLocation(168.4784, -64.352);
            assertEquals(1, pixelLocation.length);  // is part of self intersecting area
            assertEquals(60.49976819818859, pixelLocation[0].getX(), 1e-8);    // original 60.5
            assertEquals(1504.4747569343847, pixelLocation[0].getY(), 1e-8);    // original 1504.5

        } finally {
            reader.close();
        }
    }

    @Test
    public void testSubScenePixelLocator_MHS_NOAA18() throws IOException, ParseException {
        final File mhsFile = createMhsNOAA18Path("NSS.MHSX.NN.D07234.S1151.E1337.B1162021.GC.h5");
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);

        final Polygon polygon = (Polygon) geometryFactory.parse("POLYGON((-157.6385 -36.6767, 177.5199 -32.9176, 150.1497 -62.1028, -159.8103 -69.9033, -157.6385 -36.6767))");

        try {
            reader.open(mhsFile);

            final PixelLocator subScenePixelLocator = reader.getSubScenePixelLocator(polygon);
            assertNotNull(subScenePixelLocator);

            Point2D geoLocation = subScenePixelLocator.getGeoLocation(18.5, 1061.5, null);
            assertEquals(-169.9741973876953, geoLocation.getX(), 1e-8);  // original -169.9742
            assertEquals(-55.09589767456055, geoLocation.getY(), 1e-8); // original -55.0959

            Point2D[] pixelLocation = subScenePixelLocator.getPixelLocation(-169.9742, -55.0959);
            assertEquals(1, pixelLocation.length);
            assertEquals(18.4837931639385, pixelLocation[0].getX(), 1e-8);    // original 18.5
            assertEquals(1061.542564721166, pixelLocation[0].getY(), 1e-8);    // original 1061.5

        } finally {
            reader.close();
        }
    }

    @Test
    public void testSubScenePixelLocator_AMSUB_NOAA15() throws IOException, ParseException {
        final File mhsFile = createAmsubNOAA15Path("L0522933.NSS.AMBX.NK.D07234.S1640.E1824.B4821617.GC.h5");
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);

        final Polygon polygon = (Polygon) geometryFactory.parse("POLYGON((-12.2705 -57.3754, 20.1677 -52.4447, 1.9342 -16.1438, -17.6123 -19.269, -12.2705 -57.3754))");

        try {
            reader.open(mhsFile);

            final PixelLocator subScenePixelLocator = reader.getSubScenePixelLocator(polygon);
            assertNotNull(subScenePixelLocator);

            Point2D geoLocation = subScenePixelLocator.getGeoLocation(26.5, 1563.5, null);
            assertEquals(-5.109499931335449, geoLocation.getX(), 1e-8);  // original -5.1095
            assertEquals(-41.448699951171875, geoLocation.getY(), 1e-8); // original -41.4487

            Point2D[] pixelLocation = subScenePixelLocator.getPixelLocation(-5.1095, -41.4487);
            assertEquals(1, pixelLocation.length);
            assertEquals(26.40093136888642, pixelLocation[0].getX(), 1e-8);    // original 26.5
            assertEquals(1563.4830863678947, pixelLocation[0].getY(), 1e-8);    // original 1563.5

        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_MHS_NOAA18() throws IOException, InvalidRangeException {
        final File mhsFile = createMhsNOAA18Path("NSS.MHSX.NN.D07234.S1332.E1518.B1162122.GC.h5");

        try {
            reader.open(mhsFile);

            final List<Variable> variables = reader.getVariables();
            assertEquals(23, variables.size());
            Variable variable = variables.get(0);
            assertEquals("btemps_ch1", variable.getShortName());
            variable = variables.get(1);
            assertEquals("btemps_ch2", variable.getShortName());
            variable = variables.get(2);
            assertEquals("btemps_ch3", variable.getShortName());
            variable = variables.get(3);
            assertEquals("btemps_ch4", variable.getShortName());
            variable = variables.get(4);
            assertEquals("btemps_ch5", variable.getShortName());

            variable = variables.get(5);
            assertEquals("chanqual_ch1", variable.getShortName());
            variable = variables.get(6);
            assertEquals("chanqual_ch2", variable.getShortName());
            variable = variables.get(7);
            assertEquals("chanqual_ch3", variable.getShortName());
            variable = variables.get(8);
            assertEquals("chanqual_ch4", variable.getShortName());
            variable = variables.get(9);
            assertEquals("chanqual_ch5", variable.getShortName());

            variable = variables.get(10);
            assertEquals("instrtemp", variable.getShortName());

            variable = variables.get(17);
            assertEquals("Latitude", variable.getShortName());

            variable = variables.get(19);
            assertEquals("Satellite_azimuth_angle", variable.getShortName());
            variable = variables.get(21);
            assertEquals("Solar_azimuth_angle", variable.getShortName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_AMSUB_NOAA15() throws IOException, InvalidRangeException {
        final File amsubFile = createAmsubNOAA15Path("L0522933.NSS.AMBX.NK.D07234.S1640.E1824.B4821617.GC.h5");

        try {
            reader.open(amsubFile);

            final List<Variable> variables = reader.getVariables();
            assertEquals(23, variables.size());
            Variable variable = variables.get(0);
            assertEquals("btemps_ch16", variable.getShortName());
            variable = variables.get(1);
            assertEquals("btemps_ch17", variable.getShortName());
            variable = variables.get(2);
            assertEquals("btemps_ch18", variable.getShortName());
            variable = variables.get(3);
            assertEquals("btemps_ch19", variable.getShortName());
            variable = variables.get(4);
            assertEquals("btemps_ch20", variable.getShortName());

            variable = variables.get(5);
            assertEquals("chanqual_ch16", variable.getShortName());
            variable = variables.get(6);
            assertEquals("chanqual_ch17", variable.getShortName());
            variable = variables.get(7);
            assertEquals("chanqual_ch18", variable.getShortName());
            variable = variables.get(8);
            assertEquals("chanqual_ch19", variable.getShortName());
            variable = variables.get(9);
            assertEquals("chanqual_ch20", variable.getShortName());

            variable = variables.get(11);
            assertEquals("qualind", variable.getShortName());

            variable = variables.get(18);
            assertEquals("Longitude", variable.getShortName());

            variable = variables.get(19);
            assertEquals("Satellite_azimuth_angle", variable.getShortName());
            variable = variables.get(21);
            assertEquals("Solar_azimuth_angle", variable.getShortName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_AMSUB_NOAA15_windowCenter() throws Exception {
        final File amsubFile = createAmsubNOAA15Path("L0496703.NSS.AMBX.NK.D07234.S0630.E0824.B4821011.WI.h5");

        try {
            reader.open(amsubFile);

            Array array = reader.readRaw(7, 56, new Interval(3, 3), "btemps_ch16");
            NCTestUtils.assertValueAt(28237.0, 1, 1, array);

            array = reader.readRaw(8, 57, new Interval(3, 3), "btemps_ch17");
            NCTestUtils.assertValueAt(27872.0, 2, 1, array);

            array = reader.readRaw(9, 58, new Interval(3, 3), "chanqual_ch18");
            NCTestUtils.assertValueAt(0.0, 0, 2, array);

            array = reader.readRaw(10, 59, new Interval(3, 3), "chanqual_ch19");
            NCTestUtils.assertValueAt(0.0, 1, 2, array);

            array = reader.readRaw(11, 60, new Interval(3, 3), "instrtemp");
            NCTestUtils.assertValueAt(29285.0, 2, 2, array);

            array = reader.readRaw(12, 61, new Interval(3, 3), "qualind");
            NCTestUtils.assertValueAt(0.0, 0, 0, array);

            array = reader.readRaw(13, 62, new Interval(3, 3), "scanqual");
            NCTestUtils.assertValueAt(0.0, 1, 0, array);

            array = reader.readRaw(14, 63, new Interval(3, 3), "scnlin");
            NCTestUtils.assertValueAt(63.0, 1, 0, array);

            array = reader.readRaw(15, 64, new Interval(3, 3), "scnlindy");
            NCTestUtils.assertValueAt(234.0, 2, 0, array);

            array = reader.readRaw(16, 65, new Interval(3, 3), "scnlintime");
            NCTestUtils.assertValueAt(23602452.0, 0, 1, array);

            array = reader.readRaw(17, 66, new Interval(3, 3), "scnlinyr");
            NCTestUtils.assertValueAt(2007, 1, 1, array);

            array = reader.readRaw(18, 67, new Interval(3, 3), "Latitude");
            NCTestUtils.assertValueAt(629951.0, 2, 1, array);

            array = reader.readRaw(19, 68, new Interval(3, 3), "Longitude");
            NCTestUtils.assertValueAt(1331347.0, 0, 2, array);

            array = reader.readRaw(20, 69, new Interval(3, 3), "Satellite_azimuth_angle");
            NCTestUtils.assertValueAt(18317.0, 1, 2, array);

            array = reader.readRaw(21, 70, new Interval(3, 3), "Satellite_zenith_angle");
            NCTestUtils.assertValueAt(2821.0, 2, 2, array);

            array = reader.readRaw(22, 71, new Interval(3, 3), "Solar_azimuth_angle");
            NCTestUtils.assertValueAt(23955.0, 0, 0, array);

            array = reader.readRaw(23, 72, new Interval(3, 3), "Solar_zenith_angle");
            NCTestUtils.assertValueAt(6344.0, 1, 0, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_MHS_NOAA18_windowCenter() throws Exception {
        final File mhsFile = createMhsNOAA18Path("NSS.MHSX.NN.D07234.S1332.E1518.B1162122.GC.h5");

        try {
            reader.open(mhsFile);

            Array array = reader.readRaw(24, 73, new Interval(3, 3), "btemps_ch1");
            NCTestUtils.assertValueAt(17881.0, 1, 1, array);

            array = reader.readRaw(25, 74, new Interval(3, 3), "btemps_ch3");
            NCTestUtils.assertValueAt(23870.0, 2, 1, array);

            array = reader.readRaw(26, 75, new Interval(3, 3), "chanqual_ch4");
            NCTestUtils.assertValueAt(0.0, 1, 0, array);

            array = reader.readRaw(27, 76, new Interval(3, 3), "chanqual_ch5");
            NCTestUtils.assertValueAt(0.0, 1, 1, array);

            array = reader.readRaw(28, 77, new Interval(3, 3), "instrtemp");
            NCTestUtils.assertValueAt(29376.0, 2, 1, array);

            array = reader.readRaw(29, 78, new Interval(3, 3), "qualind");
            NCTestUtils.assertValueAt(0.0, 0, 2, array);

            array = reader.readRaw(30, 79, new Interval(3, 3), "scanqual");
            NCTestUtils.assertValueAt(0.0, 1, 2, array);

            array = reader.readRaw(31, 80, new Interval(3, 3), "scnlin");
            NCTestUtils.assertValueAt(82.0, 2, 2, array);

            array = reader.readRaw(32, 81, new Interval(3, 3), "scnlindy");
            NCTestUtils.assertValueAt(234.0, 0, 0, array);

            array = reader.readRaw(33, 82, new Interval(3, 3), "scnlintime");
            NCTestUtils.assertValueAt(48951277.0, 1, 0, array);

            array = reader.readRaw(34, 83, new Interval(3, 3), "scnlinyr");
            NCTestUtils.assertValueAt(2007, 1, 1, array);

            array = reader.readRaw(35, 84, new Interval(3, 3), "Latitude");
            NCTestUtils.assertValueAt(737050.0, 2, 1, array);

            array = reader.readRaw(36, 85, new Interval(3, 3), "Longitude");
            NCTestUtils.assertValueAt(-375704.0, 2, 2, array);

            array = reader.readRaw(37, 86, new Interval(3, 3), "Satellite_azimuth_angle");
            NCTestUtils.assertValueAt(24717.0, 0, 0, array);

            array = reader.readRaw(38, 87, new Interval(3, 3), "Satellite_zenith_angle");
            NCTestUtils.assertValueAt(820.0, 1, 0, array);

            array = reader.readRaw(39, 88, new Interval(3, 3), "Solar_azimith_angle");
            NCTestUtils.assertValueAt(16533.0, 2, 0, array);

            array = reader.readRaw(40, 89, new Interval(3, 3), "Solar_zenith_angle");
            NCTestUtils.assertValueAt(6333.0, 0, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomWindowOut() throws Exception {
        final File mhsFile = createMhsNOAA18Path("NSS.MHSX.NN.D07234.S1332.E1518.B1162122.GC.h5");
        try {
            reader.open(mhsFile);
            final Array array = reader.readRaw(55, 2384, new Interval(3, 3), "Solar_zenith_angle");
            assertNotNull(array);
            assertEquals(9, array.getSize());

            NCTestUtils.assertValueAt(6376, 0, 0, array);
            NCTestUtils.assertValueAt(6383, 1, 0, array);
            NCTestUtils.assertValueAt(6389, 2, 0, array);

            NCTestUtils.assertValueAt(6391, 0, 1, array);
            NCTestUtils.assertValueAt(6397, 1, 1, array);
            NCTestUtils.assertValueAt(6404, 2, 1, array);

            NCTestUtils.assertValueAt(-999999.0, 0, 2, array);
            NCTestUtils.assertValueAt(-999999.0, 1, 2, array);
            NCTestUtils.assertValueAt(-999999.0, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topWindowOut() throws Exception {
        final File mhsFile = createMhsNOAA18Path("NSS.MHSX.NN.D07234.S1332.E1518.B1162122.GC.h5");
        try {
            reader.open(mhsFile);
            final Array array = reader.readRaw(56, 0, new Interval(3, 3), "Solar_azimuth_angle");
            assertNotNull(array);
            assertEquals(9, array.getSize());

            NCTestUtils.assertValueAt(-999999.0, 0, 0, array);
            NCTestUtils.assertValueAt(-999999.0, 1, 0, array);
            NCTestUtils.assertValueAt(-999999.0, 2, 0, array);

            NCTestUtils.assertValueAt(19180.0, 0, 1, array);
            NCTestUtils.assertValueAt(19220.0, 1, 1, array);
            NCTestUtils.assertValueAt(19261.0, 2, 1, array);

            NCTestUtils.assertValueAt(19165.0, 0, 2, array);
            NCTestUtils.assertValueAt(19206.0, 1, 2, array);
            NCTestUtils.assertValueAt(19247.0, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_leftWindowOut() throws Exception {
        final File mhsFile = createMhsNOAA18Path("NSS.MHSX.NN.D07234.S1332.E1518.B1162122.GC.h5");
        try {
            reader.open(mhsFile);
            final Array array = reader.readRaw(0, 1684, new Interval(3, 3), "Satellite_zenith_angle");
            assertNotNull(array);
            assertEquals(9, array.getSize());

            NCTestUtils.assertValueAt(-999999.0, 0, 0, array);
            NCTestUtils.assertValueAt(5972.0, 1, 0, array);
            NCTestUtils.assertValueAt(5811.0, 2, 0, array);

            NCTestUtils.assertValueAt(-999999.0, 0, 1, array);
            NCTestUtils.assertValueAt(5972.0, 1, 1, array);
            NCTestUtils.assertValueAt(5811.0, 2, 1, array);

            NCTestUtils.assertValueAt(-999999.0, 0, 2, array);
            NCTestUtils.assertValueAt(5972.0, 1, 2, array);
            NCTestUtils.assertValueAt(5811.0, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_rightWindowOut() throws Exception {
        final File mhsFile = createMhsNOAA18Path("NSS.MHSX.NN.D07234.S1332.E1518.B1162122.GC.h5");
        try {
            reader.open(mhsFile);
            final Array array = reader.readRaw(89, 1876, new Interval(3, 3), "Satellite_azimuth_angle");
            assertNotNull(array);
            assertEquals(9, array.getSize());

            NCTestUtils.assertValueAt(32888.0, 0, 0, array);
            NCTestUtils.assertValueAt(32923.0, 1, 0, array);
            NCTestUtils.assertValueAt(-999999.0, 2, 0, array);

            NCTestUtils.assertValueAt(32909.0, 0, 1, array);
            NCTestUtils.assertValueAt(32944.0, 1, 1, array);
            NCTestUtils.assertValueAt(-999999.0, 2, 1, array);

            NCTestUtils.assertValueAt(32931.0, 0, 2, array);
            NCTestUtils.assertValueAt(32965.0, 1, 2, array);
            NCTestUtils.assertValueAt(-999999.0, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topLeftWindowOut() throws Exception {
        final File mhsFile = createMhsNOAA18Path("NSS.MHSX.NN.D07234.S1332.E1518.B1162122.GC.h5");
        try {
            reader.open(mhsFile);
            final Array array = reader.readRaw(0, 0, new Interval(3, 3), "Longitude");
            assertNotNull(array);
            assertEquals(9, array.getSize());

            NCTestUtils.assertValueAt(-999999.0, 0, 0, array);
            NCTestUtils.assertValueAt(-999999.0, 1, 0, array);
            NCTestUtils.assertValueAt(-999999.0, 2, 0, array);

            NCTestUtils.assertValueAt(-999999.0, 0, 1, array);
            NCTestUtils.assertValueAt(-341819.0, 1, 1, array);
            NCTestUtils.assertValueAt(-334336.0, 2, 1, array);

            NCTestUtils.assertValueAt(-999999.0, 0, 2, array);
            NCTestUtils.assertValueAt(-343612.0, 1, 2, array);
            NCTestUtils.assertValueAt(-336118.0, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topRightWindowOut() throws Exception {
        final File mhsFile = createMhsNOAA18Path("NSS.MHSX.NN.D07234.S1332.E1518.B1162122.GC.h5");
        try {
            reader.open(mhsFile);
            final Array array = reader.readRaw(89, 0, new Interval(3, 3), "Latitude");
            assertNotNull(array);
            assertEquals(9, array.getSize());

            NCTestUtils.assertValueAt(-999999.0, 0, 0, array);
            NCTestUtils.assertValueAt(-999999.0, 1, 0, array);
            NCTestUtils.assertValueAt(-999999.0, 2, 0, array);

            NCTestUtils.assertValueAt(643634.0, 0, 1, array);
            NCTestUtils.assertValueAt(643640.0, 1, 1, array);
            NCTestUtils.assertValueAt(-999999.0, 2, 1, array);

            NCTestUtils.assertValueAt(645180.0, 0, 2, array);
            NCTestUtils.assertValueAt(645184.0, 1, 2, array);
            NCTestUtils.assertValueAt(-999999.0, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomRightWindowOut() throws Exception {
        final File mhsFile = createMhsNOAA18Path("NSS.MHSX.NN.D07234.S1332.E1518.B1162122.GC.h5");
        try {
            reader.open(mhsFile);
            final Array array = reader.readRaw(89, 2384, new Interval(3, 3), "scnlinyr");
            assertNotNull(array);
            assertEquals(9, array.getSize());

            NCTestUtils.assertValueAt(2007.0, 0, 0, array);
            NCTestUtils.assertValueAt(2007.0, 1, 0, array);
            NCTestUtils.assertValueAt(N3iosp.NC_FILL_INT, 2, 0, array);

            NCTestUtils.assertValueAt(2007.0, 0, 1, array);
            NCTestUtils.assertValueAt(2007.0, 1, 1, array);
            NCTestUtils.assertValueAt(N3iosp.NC_FILL_INT, 2, 1, array);

            NCTestUtils.assertValueAt(N3iosp.NC_FILL_INT, 0, 2, array);
            NCTestUtils.assertValueAt(N3iosp.NC_FILL_INT, 1, 2, array);
            NCTestUtils.assertValueAt(N3iosp.NC_FILL_INT, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomLeftWindowOut() throws Exception {
        final File mhsFile = createMhsNOAA18Path("NSS.MHSX.NN.D07234.S1332.E1518.B1162122.GC.h5");
        try {
            reader.open(mhsFile);
            final Array array = reader.readRaw(0, 2384, new Interval(3, 3), "scnlintime");
            assertNotNull(array);
            assertEquals(9, array.getSize());

            NCTestUtils.assertValueAt(N3iosp.NC_FILL_INT, 0, 0, array);
            NCTestUtils.assertValueAt(55089942.0, 1, 0, array);
            NCTestUtils.assertValueAt(55089942.0, 2, 0, array);

            NCTestUtils.assertValueAt(N3iosp.NC_FILL_INT, 0, 1, array);
            NCTestUtils.assertValueAt(55092609.0, 1, 1, array);
            NCTestUtils.assertValueAt(55092609.0, 2, 1, array);

            NCTestUtils.assertValueAt(N3iosp.NC_FILL_INT, 0, 2, array);
            NCTestUtils.assertValueAt(N3iosp.NC_FILL_INT, 1, 2, array);
            NCTestUtils.assertValueAt(N3iosp.NC_FILL_INT, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_windowCenter() throws Exception {
        final File mhsFile = createAmsubNOAA15Path("L0496703.NSS.AMBX.NK.D07234.S0630.E0824.B4821011.WI.h5");
        try {
            reader.open(mhsFile);
            final Array array = reader.readScaled(78, 2100, new Interval(3, 3), "Solar_zenith_angle");
            assertNotNull(array);
            assertEquals(9, array.getSize());

            NCTestUtils.assertValueAt(75.65999830886722, 0, 0, array);
            NCTestUtils.assertValueAt(75.8999983035028, 1, 0, array);
            NCTestUtils.assertValueAt(76.14999829791486, 2, 0, array);

            NCTestUtils.assertValueAt(75.62999830953777, 0, 1, array);
            NCTestUtils.assertValueAt(75.86999830417335, 1, 1, array);
            NCTestUtils.assertValueAt(76.10999829880893, 2, 1, array);

            NCTestUtils.assertValueAt(75.58999831043184, 0, 2, array);
            NCTestUtils.assertValueAt(75.82999830506742, 1, 2, array);
            NCTestUtils.assertValueAt(76.07999829947948, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_upperRightWindowOut() throws Exception {
        final File mhsFile = createAmsubNOAA15Path("L0496703.NSS.AMBX.NK.D07234.S0630.E0824.B4821011.WI.h5");
        try {
            reader.open(mhsFile);
            final Array array = reader.readScaled(89, 0, new Interval(3, 3), "btemps_ch17");
            assertNotNull(array);
            assertEquals(9, array.getSize());

            NCTestUtils.assertValueAt(-9999.989776482806, 0, 0, array);
            NCTestUtils.assertValueAt(-9999.989776482806, 1, 0, array);
            NCTestUtils.assertValueAt(-9999.989776482806, 2, 0, array);

            NCTestUtils.assertValueAt(270.979993943125, 0, 1, array);
            NCTestUtils.assertValueAt(271.00999394245446, 1, 1, array);
            NCTestUtils.assertValueAt(-9999.989776482806, 2, 1, array);

            NCTestUtils.assertValueAt(271.2599939368665, 0, 2, array);
            NCTestUtils.assertValueAt(271.82999392412603, 1, 2, array);
            NCTestUtils.assertValueAt(-9999.989776482806, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_AMSUB_NOAA15_centerWindow() throws IOException, InvalidRangeException {
        final File amsubFile = createAmsubNOAA15Path("L0522933.NSS.AMBX.NK.D07234.S1640.E1824.B4821617.GC.h5");

        try {
            reader.open(amsubFile);

            final Array acquisitionTime = reader.readAcquisitionTime(32, 1098, new Interval(3, 3));
            assertNotNull(acquisitionTime);
            assertEquals(9, acquisitionTime.getSize());

            final int upperLineTime = (int) (TimeUtils.getDate(2007, 234, 63031787).getTime() / 1000);
            final int centerLineTime = (int) (TimeUtils.getDate(2007, 234, 63034454).getTime() / 1000);
            final int lowerLineTime = (int) (TimeUtils.getDate(2007, 234, 63037121).getTime() / 1000);

            NCTestUtils.assertValueAt(upperLineTime, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(upperLineTime, 1, 0, acquisitionTime);
            NCTestUtils.assertValueAt(upperLineTime, 2, 0, acquisitionTime);

            NCTestUtils.assertValueAt(centerLineTime, 0, 1, acquisitionTime);
            NCTestUtils.assertValueAt(centerLineTime, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(centerLineTime, 2, 1, acquisitionTime);

            NCTestUtils.assertValueAt(lowerLineTime, 0, 2, acquisitionTime);
            NCTestUtils.assertValueAt(lowerLineTime, 1, 2, acquisitionTime);
            NCTestUtils.assertValueAt(lowerLineTime, 2, 2, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_AMSUB_NOAA15_upperLeftOutside() throws IOException, InvalidRangeException {
        final File amsubFile = createAmsubNOAA15Path("L0522933.NSS.AMBX.NK.D07234.S1640.E1824.B4821617.GC.h5");

        try {
            reader.open(amsubFile);

            final Array acquisitionTime = reader.readAcquisitionTime(0, 0, new Interval(3, 3));
            assertNotNull(acquisitionTime);
            assertEquals(9, acquisitionTime.getSize());

            final int centerLineTime = (int) (TimeUtils.getDate(2007, 234, 60037120).getTime() / 1000);
            final int lowerLineTime = (int) (TimeUtils.getDate(2007, 234, 60039787).getTime() / 1000);

            NCTestUtils.assertValueAt(N3iosp.NC_FILL_INT, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(N3iosp.NC_FILL_INT, 1, 0, acquisitionTime);
            NCTestUtils.assertValueAt(N3iosp.NC_FILL_INT, 2, 0, acquisitionTime);

            NCTestUtils.assertValueAt(N3iosp.NC_FILL_INT, 0, 1, acquisitionTime);
            NCTestUtils.assertValueAt(centerLineTime, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(centerLineTime, 2, 1, acquisitionTime);

            NCTestUtils.assertValueAt(N3iosp.NC_FILL_INT, 0, 2, acquisitionTime);
            NCTestUtils.assertValueAt(lowerLineTime, 1, 2, acquisitionTime);
            NCTestUtils.assertValueAt(lowerLineTime, 2, 2, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_MHS_NOAA18() throws IOException, InvalidRangeException {
        final File mhsFile = createMhsNOAA18Path("NSS.MHSX.NN.D07234.S1332.E1518.B1162122.GC.h5");

        try {
            reader.open(mhsFile);

            final Dimension productSize = reader.getProductSize();
            assertNotNull(productSize);
            assertEquals(90, productSize.getNx());
            assertEquals(2385, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_AMSUB_NOAA15() throws IOException, InvalidRangeException {
        final File amsubFile = createAmsubNOAA15Path("L0522933.NSS.AMBX.NK.D07234.S1640.E1824.B4821617.GC.h5");

        try {
            reader.open(amsubFile);

            final Dimension productSize = reader.getProductSize();
            assertNotNull(productSize);
            assertEquals(90, productSize.getNx());
            assertEquals(2321, productSize.getNy());
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

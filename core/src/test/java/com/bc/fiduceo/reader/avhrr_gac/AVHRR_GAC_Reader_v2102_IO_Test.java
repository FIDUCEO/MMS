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

package com.bc.fiduceo.reader.avhrr_gac;


import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class AVHRR_GAC_Reader_v2102_IO_Test {

    private AVHRR_GAC_Reader reader;

    @Before
    public void setUp() {
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new AVHRR_GAC_Reader(readerContext);
    }

    @Test
    public void testReadAcquisitionInfo_N19() throws IOException {
        final File file = createAvhrrN19File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2011, 3, 15, 2, 10, 17, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2011, 3, 15, 3, 53, 10, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            assertEquals(7, polygons.size());

            Point[] coordinates = polygons.get(0).getCoordinates();
            assertEquals(149, coordinates.length);
            assertEquals(-177.197265625, coordinates[0].getLon(), 1e-8);
            assertEquals(56.23046875, coordinates[0].getLat(), 1e-8);

            coordinates = polygons.get(1).getCoordinates();
            assertEquals(29, coordinates.length);
            assertEquals(-62.802734375, coordinates[1].getLon(), 1e-8);
            assertEquals(-55.416015625, coordinates[1].getLat(), 1e-8);

            coordinates = polygons.get(2).getCoordinates();
            assertEquals(25, coordinates.length);
            assertEquals(-60.16406250000001, coordinates[2].getLon(), 1e-8);
            assertEquals(-62.32519531250001, coordinates[2].getLat(), 1e-8);

            coordinates = polygons.get(3).getCoordinates();
            assertEquals(25, coordinates.length);
            assertEquals(-57.37988281250001, coordinates[3].getLon(), 1e-8);
            assertEquals(-65.4912109375, coordinates[3].getLat(), 1e-8);

            coordinates = polygons.get(4).getCoordinates();
            assertEquals(27, coordinates.length);
            assertEquals(-63.6435546875, coordinates[4].getLon(), 1e-8);
            assertEquals(-68.4716796875, coordinates[4].getLat(), 1e-8);

            coordinates = polygons.get(5).getCoordinates();
            assertEquals(27, coordinates.length);
            assertEquals(-76.0791015625, coordinates[5].getLon(), 1e-8);
            assertEquals(-74.796875, coordinates[5].getLat(), 1e-8);

            coordinates = polygons.get(6).getCoordinates();
            assertEquals(131, coordinates.length);
            assertEquals(-136.3818359375, coordinates[6].getLon(), 1e-8);
            assertEquals(-65.6455078125, coordinates[6].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(7, timeAxes.length);
            coordinates = polygons.get(0).getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2011, 3, 15, 2, 10, 17, 0, time);

            coordinates = polygons.get(1).getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2011, 3, 15, 2, 25, 46, 710, time);

            coordinates = polygons.get(2).getCoordinates();
            time = timeAxes[2].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2011, 3, 15, 2, 47, 42, 727, time);

            coordinates = polygons.get(3).getCoordinates();
            time = timeAxes[3].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2011, 3, 15, 3, 0, 1, 468, time);

            coordinates = polygons.get(4).getCoordinates();
            time = timeAxes[4].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2011, 3, 15, 3, 9, 56, 988, time);

            coordinates = polygons.get(5).getCoordinates();
            time = timeAxes[5].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2011, 3, 15, 3, 24, 6, 761, time);

            coordinates = polygons.get(6).getCoordinates();
            time = timeAxes[6].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2011, 3, 15, 3, 38, 28, 301, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_MA() throws IOException {
        final File file = createAvhrrMAFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2010, 1, 1, 11, 37, 16, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2010, 1, 1, 13, 17, 57, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            assertEquals(2, polygons.size());

            Point[] coordinates = polygons.get(0).getCoordinates();
            assertEquals(145, coordinates.length);
            assertEquals(-55.71679687500001, coordinates[0].getLon(), 1e-8);
            assertEquals(79.0263671875, coordinates[0].getLat(), 1e-8);

            assertEquals(-48.1142578125, coordinates[24].getLon(), 1e-8);
            assertEquals(10.916015625, coordinates[24].getLat(), 1e-8);

            coordinates = polygons.get(1).getCoordinates();
            assertEquals(145, coordinates.length);
            assertEquals(-157.2783203125, coordinates[0].getLon(), 1e-8);
            assertEquals(-66.1435546875, coordinates[0].getLat(), 1e-8);

            assertEquals(144.923828125, coordinates[24].getLon(), 1e-8);
            assertEquals(-6.693359375000002, coordinates[24].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = polygons.get(0).getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2010, 1, 1, 11, 37, 16, 666, time);

            coordinates = polygons.get(1).getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2010, 1, 1, 12, 27, 36, 500, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_N19() throws IOException {
        final File file = createAvhrrN19File();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            final long referenceTime = 1300155017000L;
            assertEquals(referenceTime, timeLocator.getTimeFor(169, 0));
            assertEquals(referenceTime + 500, timeLocator.getTimeFor(168, 1));
            assertEquals(referenceTime + 7000, timeLocator.getTimeFor(169, 14));
            assertEquals(referenceTime + 507500, timeLocator.getTimeFor(170, 1015));
            assertEquals(referenceTime + 1008000, timeLocator.getTimeFor(171, 2016));
            assertEquals(referenceTime + 5623000, timeLocator.getTimeFor(172, 11246));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_MA() throws IOException {
        final File file = createAvhrrMAFile();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            final long referenceTime = 1262345836000L;
            assertEquals(referenceTime, timeLocator.getTimeFor(169, 0));
            assertEquals(referenceTime + 500, timeLocator.getTimeFor(168, 1));
            assertEquals(referenceTime + 7000, timeLocator.getTimeFor(169, 14));
            assertEquals(referenceTime + 507500, timeLocator.getTimeFor(170, 1015));
            assertEquals(referenceTime + 1008000, timeLocator.getTimeFor(171, 2016));
            assertEquals(referenceTime + 5623002, timeLocator.getTimeFor(172, 11246));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_N19() throws IOException {
        final File file = createAvhrrN19File();

        try {
            reader.open(file);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(37, 212, new Interval(3, 3));
            NCTestUtils.assertValueAt(1300155123, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1300155123, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1300155124, 2, 2, acquisitionTime);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_MA() throws IOException {
        final File file = createAvhrrMAFile();

        try {
            reader.open(file);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(37, 212, new Interval(3, 3));
            NCTestUtils.assertValueAt(1262345942, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1262345942, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1262345943, 2, 2, acquisitionTime);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_N19() throws IOException {
        final File file = createAvhrrN19File();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(40, variables.size());
            Variable variable = variables.get(2);
            assertEquals("dtime", variable.getFullName());

            variable = variables.get(12);
            assertEquals("ch3b_nedt", variable.getFullName());

            variable = variables.get(19);
            assertEquals("ict_temp", variable.getFullName());

            variable = variables.get(27);
            assertEquals("ch5_space_counts", variable.getFullName());

            variable = variables.get(32);
            assertEquals("ch1_earth_counts", variable.getFullName());

            variable = variables.get(39);
            assertEquals("orbital_temperature_nlines", variable.getFullName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_MA() throws IOException {
        final File file = createAvhrrMAFile();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(40, variables.size());
            Variable variable = variables.get(1);
            assertEquals("lon", variable.getFullName());

            variable = variables.get(11);
            assertEquals("ch3a_noise", variable.getFullName());

            variable = variables.get(17);
            assertEquals("satellite_azimuth_angle", variable.getFullName());

            variable = variables.get(26);
            assertEquals("ch4_space_counts", variable.getFullName());

            variable = variables.get(31);
            assertEquals("prt_4", variable.getFullName());

            variable = variables.get(39);
            assertEquals("orbital_temperature_nlines", variable.getFullName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_windowCenter_N19() throws Exception {
        final File file = createAvhrrN19File();
        reader.open(file);
        try {
            final Array array = reader.readRaw(42, 668, new Interval(3, 3), "ch2");
            assertNotNull(array);

            NCTestUtils.assertValueAt(1370, 0, 0, array);
            NCTestUtils.assertValueAt(1075, 1, 0, array);
            NCTestUtils.assertValueAt(1219, 2, 0, array);
            NCTestUtils.assertValueAt(1477, 0, 1, array);
            NCTestUtils.assertValueAt(1307, 1, 1, array);
            NCTestUtils.assertValueAt(1157, 2, 1, array);
            NCTestUtils.assertValueAt(1609, 0, 2, array);
            NCTestUtils.assertValueAt(1446, 1, 2, array);
            NCTestUtils.assertValueAt(1106, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_windowCenter_MA() throws Exception {
        final File file = createAvhrrMAFile();
        reader.open(file);
        try {
            final Array array = reader.readRaw(41, 667, new Interval(3, 3), "ch1_earth_counts");
            assertNotNull(array);

            NCTestUtils.assertValueAt(60, 0, 0, array);
            NCTestUtils.assertValueAt(58, 1, 0, array);
            NCTestUtils.assertValueAt(61, 2, 0, array);
            NCTestUtils.assertValueAt(59, 0, 1, array);
            NCTestUtils.assertValueAt(58, 1, 1, array);
            NCTestUtils.assertValueAt(61, 2, 1, array);
            NCTestUtils.assertValueAt(61, 0, 2, array);
            NCTestUtils.assertValueAt(57, 1, 2, array);
            NCTestUtils.assertValueAt(61, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomWindowOut_N19() throws Exception {
        final File file = createAvhrrN19File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(406, 12347, new Interval(3, 3), "ch2_earth_counts");
            assertNotNull(array);

            NCTestUtils.assertValueAt(302, 0, 0, array);
            NCTestUtils.assertValueAt(329, 1, 0, array);
            NCTestUtils.assertValueAt(382, 2, 0, array);
            NCTestUtils.assertValueAt(342, 0, 1, array);
            NCTestUtils.assertValueAt(356, 1, 1, array);
            NCTestUtils.assertValueAt(390, 2, 1, array);
            NCTestUtils.assertValueAt(-1, 0, 2, array);
            NCTestUtils.assertValueAt(-1, 1, 2, array);
            NCTestUtils.assertValueAt(-1, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomWindowOut_MA() throws Exception {
        final File file = createAvhrrMAFile();
        reader.open(file);

        try {
            final Array array = reader.readRaw(405, 12082, new Interval(3, 3), "ch2");
            assertNotNull(array);

            NCTestUtils.assertValueAt(41, 0, 0, array);
            NCTestUtils.assertValueAt(47, 1, 0, array);
            NCTestUtils.assertValueAt(41, 2, 0, array);
            NCTestUtils.assertValueAt(47, 0, 1, array);
            NCTestUtils.assertValueAt(47, 1, 1, array);
            NCTestUtils.assertValueAt(41, 2, 1, array);
            NCTestUtils.assertValueAt(-32768, 0, 2, array);
            NCTestUtils.assertValueAt(-32768, 1, 2, array);
            NCTestUtils.assertValueAt(-32768, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topWindowOut_N19() throws Exception {
        final File file = createAvhrrN19File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(1, 0, new Interval(3, 5), "ch2_noise");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-32768, 0, 0, array);
            NCTestUtils.assertValueAt(-32768, 1, 0, array);
            NCTestUtils.assertValueAt(-32768, 2, 0, array);

            NCTestUtils.assertValueAt(24, 0, 2, array);
            NCTestUtils.assertValueAt(24, 1, 3, array);
            NCTestUtils.assertValueAt(24, 2, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topWindowOut_MA() throws Exception {
        final File file = createAvhrrMAFile();
        reader.open(file);

        try {
            final Array array = reader.readRaw(1, 0, new Interval(3, 5), "ch3b_bbody_counts");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-32768, 0, 0, array);
            NCTestUtils.assertValueAt(-32768, 1, 0, array);
            NCTestUtils.assertValueAt(-32768, 2, 0, array);

            NCTestUtils.assertValueAt(16794, 0, 2, array);
            NCTestUtils.assertValueAt(16794, 1, 3, array);
            NCTestUtils.assertValueAt(16794, 2, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_leftWindowOut_N19() throws Exception {
        final File file = createAvhrrN19File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(0, 124, new Interval(3, 3), "ch3b");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-32768, 0, 0, array);
            NCTestUtils.assertValueAt(-177, 1, 0, array);
            NCTestUtils.assertValueAt(-177, 2, 0, array);

            NCTestUtils.assertValueAt(-32768, 0, 2, array);
            NCTestUtils.assertValueAt(-208, 1, 2, array);
            NCTestUtils.assertValueAt(-208, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_leftWindowOut_MA() throws Exception {
        final File file = createAvhrrMAFile();
        reader.open(file);

        try {
            final Array array = reader.readRaw(2, 122, new Interval(8, 8), "ch3b_nedt");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-32768, 0, 0, array);
            NCTestUtils.assertValueAt(-32768, 1, 0, array);
            NCTestUtils.assertValueAt(1246, 2, 0, array);
            NCTestUtils.assertValueAt(827, 3, 0, array);
            NCTestUtils.assertValueAt(885, 4, 0, array);

            NCTestUtils.assertValueAt(-32768, 0, 7, array);
            NCTestUtils.assertValueAt(-32768, 1, 7, array);
            NCTestUtils.assertValueAt(1128, 2, 7, array);
            NCTestUtils.assertValueAt(1032, 3, 7, array);
            NCTestUtils.assertValueAt(952, 4, 7, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_rightWindowOut_N19() throws Exception {
        final File file = createAvhrrN19File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(408, 786, new Interval(3, 3), "ch3b_bbody_counts");
            assertNotNull(array);

            NCTestUtils.assertValueAt(14773, 0, 0, array);
            NCTestUtils.assertValueAt(14773, 1, 0, array);
            NCTestUtils.assertValueAt(-32768, 2, 0, array);

            NCTestUtils.assertValueAt(14774, 0, 2, array);
            NCTestUtils.assertValueAt(14774, 1, 2, array);
            NCTestUtils.assertValueAt(-32768, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_rightWindowOut_MA() throws Exception {
        final File file = createAvhrrMAFile();
        reader.open(file);

        try {
            final Array array = reader.readRaw(407, 248, new Interval(9, 9), "ch4_space_counts");
            assertNotNull(array);

            NCTestUtils.assertValueAt(4264, 4, 0, array);
            NCTestUtils.assertValueAt(4264, 5, 0, array);
            NCTestUtils.assertValueAt(-32768, 6, 0, array);
            NCTestUtils.assertValueAt(-32768, 7, 0, array);
            NCTestUtils.assertValueAt(-32768, 8, 0, array);

            NCTestUtils.assertValueAt(4265, 4, 7, array);
            NCTestUtils.assertValueAt(4265, 5, 7, array);
            NCTestUtils.assertValueAt(-32768, 6, 7, array);
            NCTestUtils.assertValueAt(-32768, 7, 7, array);
            NCTestUtils.assertValueAt(-32768, 8, 7, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topLeftWindowOut_N19() throws Exception {
        final File file = createAvhrrN19File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(2, 3, new Interval(9, 9), "ch5_earth_counts");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-1, 0, 0, array);
            NCTestUtils.assertValueAt(-1, 4, 0, array);
            NCTestUtils.assertValueAt(-1, 8, 0, array);

            NCTestUtils.assertValueAt(-1, 0, 4, array);
            NCTestUtils.assertValueAt(649, 4, 4, array);
            NCTestUtils.assertValueAt(648, 8, 4, array);

            NCTestUtils.assertValueAt(-1, 0, 8, array);
            NCTestUtils.assertValueAt(646, 4, 8, array);
            NCTestUtils.assertValueAt(650, 8, 8, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topLeftWindowOut_MA() throws Exception {
        final File file = createAvhrrMAFile();
        reader.open(file);

        try {
            final Array array = reader.readRaw(2, 3, new Interval(9, 9), "ch5_earth_counts");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-1, 0, 0, array);
            NCTestUtils.assertValueAt(-1, 4, 0, array);
            NCTestUtils.assertValueAt(-1, 8, 0, array);

            NCTestUtils.assertValueAt(-1, 0, 4, array);
            NCTestUtils.assertValueAt(739, 4, 4, array);
            NCTestUtils.assertValueAt(741, 8, 4, array);

            NCTestUtils.assertValueAt(-1, 0, 8, array);
            NCTestUtils.assertValueAt(739, 4, 8, array);
            NCTestUtils.assertValueAt(741, 8, 8, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_scalingAndOffset_N19() throws IOException {
        final File file = createAvhrrN19File();
        reader.open(file);

        try {
            final Array array = reader.readScaled(95, 7363, new Interval(3, 3), "prt_1");
            assertNotNull(array);

            NCTestUtils.assertValueAt(286.4599935989827, 0, 0, array);
            NCTestUtils.assertValueAt(286.4599935989827, 1, 0, array);
            NCTestUtils.assertValueAt(286.4599935989827, 2, 0, array);

            NCTestUtils.assertValueAt(286.4599935989827, 0, 1, array);
            NCTestUtils.assertValueAt(286.4599935989827, 1, 1, array);
            NCTestUtils.assertValueAt(286.4599935989827, 2, 1, array);

            NCTestUtils.assertValueAt(286.4599935989827, 0, 2, array);
            NCTestUtils.assertValueAt(286.4599935989827, 1, 2, array);
            NCTestUtils.assertValueAt(286.4599935989827, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_scalingAndOffset_MA() throws IOException {
        final File file = createAvhrrMAFile();
        reader.open(file);

        try {
            final Array array = reader.readScaled(96, 7364, new Interval(3, 3), "prt_2");
            assertNotNull(array);

            NCTestUtils.assertValueAt(286.31999360211194, 1, 0, array);
            NCTestUtils.assertValueAt(286.31999360211194, 0, 0, array);
            NCTestUtils.assertValueAt(286.31999360211194, 2, 0, array);

            NCTestUtils.assertValueAt(286.31999360211194, 0, 1, array);
            NCTestUtils.assertValueAt(286.31999360211194, 1, 1, array);
            NCTestUtils.assertValueAt(286.31999360211194, 2, 1, array);

            NCTestUtils.assertValueAt(286.31999360211194, 0, 2, array);
            NCTestUtils.assertValueAt(286.31999360211194, 1, 2, array);
            NCTestUtils.assertValueAt(286.31999360211194, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_onlyScaling_onePixel_N19() throws IOException {
        final File file = createAvhrrN19File();
        reader.open(file);

        try {
            final Array array = reader.readScaled(85, 4606, new Interval(1, 1), "lat");
            assertNotNull(array);
            assertEquals(1, array.getSize());

            final Index index = array.getIndex();
            assertEquals(-10.2841796875, array.getFloat(index), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_onlyScaling_onePixel_MA() throws IOException {
        final File file = createAvhrrMAFile();
        reader.open(file);

        try {
            final Array array = reader.readScaled(84, 4605, new Interval(1, 1), "ch5_nedt");
            assertNotNull(array);
            assertEquals(1, array.getSize());

            final Index index = array.getIndex();
            assertEquals(0.125, array.getFloat(index), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_onlyScaling_N19() throws IOException {
        final File file = createAvhrrN19File();
        reader.open(file);

        try {
            final Array array = reader.readScaled(118, 8091, new Interval(3, 3), "lon");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-179.2587890625, 0, 0, array);
            NCTestUtils.assertValueAt(-179.349609375, 1, 0, array);
            NCTestUtils.assertValueAt(-179.44140625, 2, 0, array);

            NCTestUtils.assertValueAt(-179.291015625, 0, 1, array);
            NCTestUtils.assertValueAt(-179.3828125, 1, 1, array);
            NCTestUtils.assertValueAt(-179.4736328125, 2, 1, array);

            NCTestUtils.assertValueAt(-179.32421875, 0, 2, array);
            NCTestUtils.assertValueAt(-179.4150390625, 1, 2, array);
            NCTestUtils.assertValueAt(-179.505859375, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_onlyScaling_MA() throws IOException {
        final File file = createAvhrrMAFile();
        reader.open(file);

        try {
            final Array array = reader.readScaled(118, 8091, new Interval(3, 3), "ch5_nedt");
            assertNotNull(array);

            NCTestUtils.assertValueAt(0.09400000446476042, 0, 0, array);
            NCTestUtils.assertValueAt(0.09300000441726297, 1, 0, array);
            NCTestUtils.assertValueAt(0.09000000427477062, 2, 0, array);

            NCTestUtils.assertValueAt(0.08800000417977571, 0, 1, array);
            NCTestUtils.assertValueAt(0.09100000432226807, 1, 1, array);
            NCTestUtils.assertValueAt(0.09100000432226807, 2, 1, array);

            NCTestUtils.assertValueAt(0.08800000417977571, 0, 2, array);
            NCTestUtils.assertValueAt(0.08900000422727317, 1, 2, array);
            NCTestUtils.assertValueAt(0.09000000427477062, 2, 2, array);
        } finally {
            reader.close();
        }
    }


    @Test
    public void testReadScaled_noScale_noOffset_N19() throws IOException {
        final File file = createAvhrrN19File();
        reader.open(file);

        try {
            final Array array = reader.readScaled(359, 10237, new Interval(3, 3), "orbital_temperature");
            assertNotNull(array);

            NCTestUtils.assertValueAt(287.7126770019531, 0, 0, array);
            NCTestUtils.assertValueAt(287.7126770019531, 1, 0, array);
            NCTestUtils.assertValueAt(287.7126770019531, 2, 0, array);

            NCTestUtils.assertValueAt(287.7126770019531, 0, 1, array);
            NCTestUtils.assertValueAt(287.7126770019531, 1, 1, array);
            NCTestUtils.assertValueAt(287.7126770019531, 2, 1, array);

            NCTestUtils.assertValueAt(287.7126770019531, 0, 2, array);
            NCTestUtils.assertValueAt(287.7126770019531, 1, 2, array);
            NCTestUtils.assertValueAt(287.7126770019531, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_noScale_noOffset_MA() throws IOException {
        final File file = createAvhrrMAFile();
        reader.open(file);

        try {
            final Array array = reader.readScaled(360, 10238, new Interval(3, 3), "orbital_temperature");
            assertNotNull(array);

            NCTestUtils.assertValueAt(286.16259765625, 0, 0, array);
            NCTestUtils.assertValueAt(286.16259765625, 1, 0, array);
            NCTestUtils.assertValueAt(286.16259765625, 2, 0, array);

            NCTestUtils.assertValueAt(286.16259765625, 0, 1, array);
            NCTestUtils.assertValueAt(286.16259765625, 1, 1, array);
            NCTestUtils.assertValueAt(286.16259765625, 2, 1, array);

            NCTestUtils.assertValueAt(286.16259765625, 0, 2, array);
            NCTestUtils.assertValueAt(286.16259765625, 1, 2, array);
            NCTestUtils.assertValueAt(286.16259765625, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_N19() throws Exception {
        final File file = createAvhrrN19File();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertNotNull(productSize);
            assertEquals(409, productSize.getNx());
            assertEquals(12348, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_MA() throws Exception {
        final File file = createAvhrrMAFile();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertNotNull(productSize);
            assertEquals(409, productSize.getNx());
            assertEquals(12083, productSize.getNy());
        } finally {
            reader.close();
        }
    }


    @Test
    public void testGetPixelLocator_N19() throws IOException {
        final File file = createAvhrrN19File();

        try {
            reader.open(file);
            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            Point2D[] pixelLocation = pixelLocator.getPixelLocation(142.80371, -41.773438);
            assertNotNull(pixelLocation);
            assertEquals(1, pixelLocation.length);
            assertEquals(405.42875271789677, pixelLocation[0].getX(), 1e-8);
            assertEquals(8957.82570099859, pixelLocation[0].getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(-29.305664, -44.881836);
            assertNotNull(pixelLocation);
            assertEquals(1, pixelLocation.length);
            assertEquals(216.5820714371418, pixelLocation[0].getX(), 1e-8);
            assertEquals(5765.501763328423, pixelLocation[0].getY(), 1e-8);

            Point2D geoLocation = pixelLocator.getGeoLocation(405.5, 8957.5, null);
            assertNotNull(geoLocation);
            assertEquals(142.7802734375, geoLocation.getX(), 1e-8);
            assertEquals(-41.7822265625, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(216.5, 5765.5, null);
            assertNotNull(geoLocation);
            assertEquals(-29.3056640625, geoLocation.getX(), 1e-8);
            assertEquals(-44.8818359375, geoLocation.getY(), 1e-8);

            // request from a gap
            geoLocation = pixelLocator.getGeoLocation(214.5, 6750.0, null);
            assertNull(geoLocation);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator_MA() throws IOException {
        final File file = createAvhrrMAFile();

        try {
            reader.open(file);
            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            Point2D[] pixelLocation = pixelLocator.getPixelLocation(-38.927734, 49.15332);
            assertNotNull(pixelLocation);
            assertEquals(1, pixelLocation.length);
            assertEquals(20.508135728313565, pixelLocation[0].getX(), 1e-8);
            assertEquals(1063.4976170465015, pixelLocation[0].getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(142.34961, -48.006836);
            assertNotNull(pixelLocation);
            assertEquals(1, pixelLocation.length);
            assertEquals(221.48154605314505, pixelLocation[0].getX(), 1e-8);
            assertEquals(7135.491916055188, pixelLocation[0].getY(), 1e-8);

            Point2D geoLocation = pixelLocator.getGeoLocation(20.5, 1063.5, null);
            assertNotNull(geoLocation);
            assertEquals(-38.927734375, geoLocation.getX(), 1e-8);
            assertEquals(49.1533203125, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(221.5, 7135.5, null);
            assertNotNull(geoLocation);
            assertEquals(142.349609375, geoLocation.getX(), 1e-8);
            assertEquals(-48.0068359375, geoLocation.getY(), 1e-8);
        } finally {
            reader.close();
        }
    }

    private File createAvhrrN19File() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n19", "v2.10.2", "2011", "03", "15", "20110315021017-ESACCI-L1C-AVHRR19_G-v1.5-fv02.0.nc"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File createAvhrrMAFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-ma", "v2.10.2", "2010", "01", "01", "20100101113716-ESACCI-L1C-AVHRRMTA_G-v1.5-fv02.0.nc"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}

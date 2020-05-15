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
import com.bc.fiduceo.reader.time.TimeLocator;
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
public class AVHRR_GAC_Reader_v015_IO_Test {

    private AVHRR_GAC_Reader reader;

    @Before
    public void setUp() {
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new AVHRR_GAC_Reader(readerContext);
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File file = createAvhrrNOAA08File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1984, 3, 13, 16, 20, 41, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1984, 3, 13, 18, 2, 44, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            assertEquals(2, polygons.size());

            Point[] coordinates = polygons.get(0).getCoordinates();
            assertEquals(147, coordinates.length);
            assertEquals(72.10600280761719, coordinates[0].getLon(), 1e-8);
            assertEquals(78.06099700927734, coordinates[0].getLat(), 1e-8);

            assertEquals(-145.95700073242188, coordinates[24].getLon(), 1e-8);
            assertEquals(31.524000167846683, coordinates[24].getLat(), 1e-8);

            coordinates = polygons.get(1).getCoordinates();
            assertEquals(147, coordinates.length);
            assertEquals(151.36700439453125, coordinates[0].getLon(), 1e-8);
            assertEquals(-65.83399963378906, coordinates[0].getLat(), 1e-8);

            assertEquals(50.32600021362305, coordinates[24].getLon(), 1e-8);
            assertEquals(-26.034000396728516, coordinates[24].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = polygons.get(0).getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1984, 3, 13, 16, 20, 41, 0, time);

            coordinates = polygons.get(1).getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1984, 3, 13, 17, 11, 44, 634, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator() throws IOException {
        final File file = createAvhrrNOAA08File();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            final long referenceTime = 448042841000L;
            assertEquals(referenceTime, timeLocator.getTimeFor(169, 0));
            assertEquals(referenceTime + 494, timeLocator.getTimeFor(168, 1));
            assertEquals(referenceTime + 6997, timeLocator.getTimeFor(169, 14));
            assertEquals(referenceTime + 507493, timeLocator.getTimeFor(170, 1015));
            assertEquals(referenceTime + 1007996, timeLocator.getTimeFor(171, 2016));
            assertEquals(referenceTime + 6122997, timeLocator.getTimeFor(172, 12246));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime() throws IOException {
        final File file = createAvhrrNOAA08File();

        try {
            reader.open(file);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(36, 211, new Interval(3, 3));
            NCTestUtils.assertValueAt(448042946, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(448042946, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(448042947, 2, 2, acquisitionTime);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables() throws IOException {
        final File file = createAvhrrNOAA08File();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(19, variables.size());
            Variable variable = variables.get(1);
            assertEquals("lon", variable.getFullName());

            variable = variables.get(9);
            assertEquals("ch3b_nedt", variable.getFullName());

            variable = variables.get(15);
            assertEquals("qual_flags", variable.getFullName());

            variable = variables.get(18);
            assertEquals("l1b_line_number", variable.getFullName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_windowCenter() throws Exception {
        final File file = createAvhrrNOAA08File();
        reader.open(file);
        try {
            final Array array = reader.readRaw(28, 230, new Interval(3, 3), "ch1_noise");
            assertNotNull(array);

            NCTestUtils.assertValueAt(45, 0, 0, array);
            NCTestUtils.assertValueAt(45, 1, 0, array);
            NCTestUtils.assertValueAt(45, 2, 0, array);
            NCTestUtils.assertValueAt(-32768, 0, 1, array);
            NCTestUtils.assertValueAt(45, 1, 1, array);
            NCTestUtils.assertValueAt(45, 2, 1, array);
            NCTestUtils.assertValueAt(45, 0, 2, array);
            NCTestUtils.assertValueAt(45, 1, 2, array);
            NCTestUtils.assertValueAt(45, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomWindowOut() throws Exception {
        final File file = createAvhrrNOAA08File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(77, 12246, new Interval(3, 3), "ch2_noise");
            assertNotNull(array);

            NCTestUtils.assertValueAt(73, 0, 0, array);
            NCTestUtils.assertValueAt(73, 1, 0, array);
            NCTestUtils.assertValueAt(73, 2, 0, array);
            NCTestUtils.assertValueAt(73, 0, 1, array);
            NCTestUtils.assertValueAt(73, 1, 1, array);
            NCTestUtils.assertValueAt(73, 2, 1, array);
            NCTestUtils.assertValueAt(-32768, 0, 2, array);
            NCTestUtils.assertValueAt(-32768, 1, 2, array);
            NCTestUtils.assertValueAt(-32768, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topWindowOut() throws Exception {
        final File file = createAvhrrNOAA08File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(4, 1, new Interval(3, 4), "ch3b");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-32768, 0, 0, array);
            NCTestUtils.assertValueAt(-32768, 1, 0, array);
            NCTestUtils.assertValueAt(-32768, 2, 0, array);

            NCTestUtils.assertValueAt(-2130, 0, 1, array);
            NCTestUtils.assertValueAt(-2470, 1, 1, array);
            NCTestUtils.assertValueAt(-1879, 2, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_leftWindowOut() throws Exception {
        final File file = createAvhrrNOAA08File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(2, 121, new Interval(8, 8), "ch3b_nedt");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-32768, 0, 0, array);
            NCTestUtils.assertValueAt(-32768, 1, 0, array);
            NCTestUtils.assertValueAt(1362, 2, 0, array);
            NCTestUtils.assertValueAt(-32768, 3, 0, array);
            NCTestUtils.assertValueAt(2883, 4, 0, array);

            NCTestUtils.assertValueAt(-32768, 0, 7, array);
            NCTestUtils.assertValueAt(-32768, 1, 7, array);
            NCTestUtils.assertValueAt(1700, 2, 7, array);
            NCTestUtils.assertValueAt(5000, 3, 7, array);
            NCTestUtils.assertValueAt(8632, 4, 7, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_rightWindowOut() throws Exception {
        final File file = createAvhrrNOAA08File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(407, 243, new Interval(9, 9), "ch4");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-1208, 4, 0, array);
            NCTestUtils.assertValueAt(-1236, 5, 0, array);
            NCTestUtils.assertValueAt(-32768, 6, 0, array);
            NCTestUtils.assertValueAt(-32768, 7, 0, array);
            NCTestUtils.assertValueAt(-32768, 8, 0, array);

            NCTestUtils.assertValueAt(-1363, 4, 7, array);
            NCTestUtils.assertValueAt(-1178, 5, 7, array);
            NCTestUtils.assertValueAt(-32768, 6, 7, array);
            NCTestUtils.assertValueAt(-32768, 7, 7, array);
            NCTestUtils.assertValueAt(-32768, 8, 7, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topLeftWindowOut() throws Exception {
        final File file = createAvhrrNOAA08File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(2, 3, new Interval(9, 9), "ch4_nedt");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-32768, 0, 0, array);
            NCTestUtils.assertValueAt(-32768, 4, 0, array);
            NCTestUtils.assertValueAt(-32768, 8, 0, array);

            NCTestUtils.assertValueAt(-32768, 0, 4, array);
            NCTestUtils.assertValueAt(104, 4, 4, array);
            NCTestUtils.assertValueAt(105, 8, 4, array);

            NCTestUtils.assertValueAt(-32768, 0, 8, array);
            NCTestUtils.assertValueAt(108, 4, 8, array);
            NCTestUtils.assertValueAt(104, 8, 8, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomRightWindowOut() throws Exception {
        final File file = createAvhrrNOAA08File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(405, 12243, new Interval(9, 9), "relative_azimuth_angle");
            assertNotNull(array);

            NCTestUtils.assertValueAt(15678, 3, 3, array);
            NCTestUtils.assertValueAt(15676, 4, 4, array);
            NCTestUtils.assertValueAt(15670, 6, 6, array);
            NCTestUtils.assertValueAt(-32768, 8, 8, array);

            NCTestUtils.assertValueAt(15677, 2, 2, array);
            NCTestUtils.assertValueAt(15674, 4, 2, array);
            NCTestUtils.assertValueAt(-32768, 6, 8, array);
            NCTestUtils.assertValueAt(-32768, 8, 8, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_scalingAndOffset() throws IOException {
        final File file = createAvhrrNOAA08File();
        reader.open(file);

        try {
            final Array array = reader.readScaled(94, 7329, new Interval(3, 3), "cloud_probability");
            assertNotNull(array);

            NCTestUtils.assertValueAt(0.9960619928315282, 0, 0, array);
            NCTestUtils.assertValueAt(0.9960619928315282, 1, 0, array);
            NCTestUtils.assertValueAt(0.9960619928315282, 2, 0, array);

            NCTestUtils.assertValueAt(0.9960619928315282, 0, 1, array);
            NCTestUtils.assertValueAt(0.9960619928315282, 1, 1, array);
            NCTestUtils.assertValueAt(0.9960619928315282, 2, 1, array);

            NCTestUtils.assertValueAt(0.9960619928315282, 0, 2, array);
            NCTestUtils.assertValueAt(0.9960619928315282, 1, 2, array);
            NCTestUtils.assertValueAt(0.9960619928315282, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_onlyScaling_onePixel() throws IOException {
        final File file = createAvhrrNOAA08File();
        reader.open(file);

        try {
            final Array array = reader.readScaled(83, 4604, new Interval(1, 1), "ch4_nedt");
            assertNotNull(array);
            assertEquals(1, array.getSize());

            final Index index = array.getIndex();
            assertEquals(0.07500000298023224, array.getFloat(index), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_onlyScaling() throws IOException {
        final File file = createAvhrrNOAA08File();
        reader.open(file);

        try {
            final Array array = reader.readScaled(118, 8091, new Interval(3, 3), "satellite_zenith_angle");
            assertNotNull(array);

            NCTestUtils.assertValueAt(26.159999415278435, 0, 0, array);
            NCTestUtils.assertValueAt(25.849999422207475, 1, 0, array);
            NCTestUtils.assertValueAt(25.539999429136515, 2, 0, array);

            NCTestUtils.assertValueAt(26.159999415278435, 0, 1, array);
            NCTestUtils.assertValueAt(25.849999422207475, 1, 1, array);
            NCTestUtils.assertValueAt(25.539999429136515, 2, 1, array);

            NCTestUtils.assertValueAt(26.169999415054917, 0, 2, array);
            NCTestUtils.assertValueAt(25.859999421983957, 1, 2, array);
            NCTestUtils.assertValueAt(25.55999942868948, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_noScale_noOffset() throws IOException {
        final File file = createAvhrrNOAA08File();
        reader.open(file);

        try {
            final Array array = reader.readScaled(359, 12237, new Interval(3, 3), "cloud_mask");
            assertNotNull(array);

            NCTestUtils.assertValueAt(7, 0, 0, array);
            NCTestUtils.assertValueAt(7, 1, 0, array);
            NCTestUtils.assertValueAt(7, 2, 0, array);

            NCTestUtils.assertValueAt(7, 0, 1, array);
            NCTestUtils.assertValueAt(7, 1, 1, array);
            NCTestUtils.assertValueAt(7, 2, 1, array);

            NCTestUtils.assertValueAt(7, 0, 2, array);
            NCTestUtils.assertValueAt(7, 1, 2, array);
            NCTestUtils.assertValueAt(7, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topRightWindowOut() throws Exception {
        final File file = createAvhrrNOAA08File();

        try {
            reader.open(file);
            Array array = reader.readRaw(407, 3, new Interval(9, 9), "solar_zenith_angle");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-32768, 4, 0, array);
            NCTestUtils.assertValueAt(-32768, 5, 0, array);
            NCTestUtils.assertValueAt(-32768, 6, 0, array);
            NCTestUtils.assertValueAt(-32768, 7, 0, array);
            NCTestUtils.assertValueAt(-32768, 8, 0, array);

            NCTestUtils.assertValueAt(7739, 4, 1, array);
            NCTestUtils.assertValueAt(7722, 5, 1, array);
            NCTestUtils.assertValueAt(-32768, 6, 1, array);
            NCTestUtils.assertValueAt(-32768, 7, 1, array);
            NCTestUtils.assertValueAt(-32768, 8, 1, array);

            NCTestUtils.assertValueAt(7731, 4, 8, array);
            NCTestUtils.assertValueAt(7713, 5, 8, array);
            NCTestUtils.assertValueAt(-32768, 6, 8, array);
            NCTestUtils.assertValueAt(-32768, 7, 8, array);
            NCTestUtils.assertValueAt(-32768, 8, 8, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize() throws Exception {
        final File file = createAvhrrNOAA08File();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertNotNull(productSize);
            assertEquals(409, productSize.getNx());
            assertEquals(12247, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator() throws IOException {
        final File file = createAvhrrNOAA08File();

        try {
            reader.open(file);
            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            final Point2D[] pixelLocation = pixelLocator.getPixelLocation(-167.222, -57.508);
            assertNotNull(pixelLocation);
            assertEquals(1, pixelLocation.length);

            assertEquals(74.2102281980777, pixelLocation[0].getX(), 1e-8);
            assertEquals(5475.744973612564, pixelLocation[0].getY(), 1e-8);

            final Point2D geoLocation = pixelLocator.getGeoLocation(74.5, 5475.5, null);
            assertNotNull(geoLocation);
            assertEquals(-167.2030029296875, geoLocation.getX(), 1e-8);
            assertEquals(-57.513999938964844, geoLocation.getY(), 1e-8);
        } finally {
            reader.close();
        }
    }

    private File createAvhrrNOAA08File() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n08", "v01.5", "1984", "03", "13", "19840313161900-ESACCI-L1C-AVHRR08_G-fv01.0.nc"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}

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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
@Ignore
public class AVHRR_GAC_Reader_v0152_IO_Test {

    private File testDataDirectory;
    private AVHRR_GAC_Reader reader;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory();

        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new AVHRR_GAC_Reader(readerContext);
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File file = createAvhrrMAFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2010, 1, 1, 11, 37, 46, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2010, 1, 1, 13, 17, 27, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            assertEquals(2, polygons.size());

            Point[] coordinates = polygons.get(0).getCoordinates();
            assertEquals(143, coordinates.length);
            assertEquals(-52.91210937500001, coordinates[0].getLon(), 1e-8);
            assertEquals(77.4375, coordinates[0].getLat(), 1e-8);

            assertEquals(-48.447265625, coordinates[24].getLon(), 1e-8);
            assertEquals(9.1904296875, coordinates[24].getLat(), 1e-8);

            coordinates = polygons.get(1).getCoordinates();
            assertEquals(143, coordinates.length);
            assertEquals(-157.2783203125, coordinates[0].getLon(), 1e-8);
            assertEquals(-66.1435546875, coordinates[0].getLat(), 1e-8);

            assertEquals(144.923828125, coordinates[24].getLon(), 1e-8);
            assertEquals(-6.693359375000002, coordinates[24].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = polygons.get(0).getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2010, 1, 1, 11, 37, 46, 663, time);

            coordinates = polygons.get(1).getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2010, 1, 1, 12, 27, 36, 500, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator() throws IOException {
        final File file = createAvhrrMAFile();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            final long referenceTime = 1262345866000L;
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
    public void testReadAcquisitionTime() throws IOException, InvalidRangeException {
        final File file = createAvhrrMAFile();

        try {
            reader.open(file);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(37, 212, new Interval(3, 3));
            NCTestUtils.assertValueAt(1262345972, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1262345972, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1262345973, 2, 2, acquisitionTime);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables() throws IOException {
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
    public void testReadRaw_windowCenter() throws Exception {
        final File file = createAvhrrMAFile();
        reader.open(file);
        try {
            final Array array = reader.readRaw(41, 667, new Interval(3, 3), "ch1_earth_counts");
            assertNotNull(array);

            NCTestUtils.assertValueAt(52, 0, 0, array);
            NCTestUtils.assertValueAt(52, 1, 0, array);
            NCTestUtils.assertValueAt(54, 2, 0, array);
            NCTestUtils.assertValueAt(52, 0, 1, array);
            NCTestUtils.assertValueAt(53, 1, 1, array);
            NCTestUtils.assertValueAt(54, 2, 1, array);
            NCTestUtils.assertValueAt(53, 0, 2, array);
            NCTestUtils.assertValueAt(54, 1, 2, array);
            NCTestUtils.assertValueAt(55, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomWindowOut() throws Exception {
        final File file = createAvhrrMAFile();
        reader.open(file);

        try {
            final Array array = reader.readRaw(406, 11962, new Interval(3, 3), "ch2");
            assertNotNull(array);

            NCTestUtils.assertValueAt(7, 0, 0, array);
            NCTestUtils.assertValueAt(7, 1, 0, array);
            NCTestUtils.assertValueAt(14, 2, 0, array);
            NCTestUtils.assertValueAt(7, 0, 1, array);
            NCTestUtils.assertValueAt(14, 1, 1, array);
            NCTestUtils.assertValueAt(14, 2, 1, array);
            NCTestUtils.assertValueAt(-32768, 0, 2, array);
            NCTestUtils.assertValueAt(-32768, 1, 2, array);
            NCTestUtils.assertValueAt(-32768, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topWindowOut() throws Exception {
        final File file = createAvhrrMAFile();
        reader.open(file);

        try {
            final Array array = reader.readRaw(1, 0, new Interval(3, 5), "ch3b_bbody_counts");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-32768, 0, 0, array);
            NCTestUtils.assertValueAt(-32768, 1, 0, array);
            NCTestUtils.assertValueAt(-32768, 2, 0, array);

            NCTestUtils.assertValueAt(16801, 0, 2, array);
            NCTestUtils.assertValueAt(16801, 1, 3, array);
            NCTestUtils.assertValueAt(16800, 2, 4, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_leftWindowOut() throws Exception {
        final File file = createAvhrrMAFile();
        reader.open(file);

        try {
            final Array array = reader.readRaw(2, 122, new Interval(8, 8), "ch3b_nedt");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-32768, 0, 0, array);
            NCTestUtils.assertValueAt(-32768, 1, 0, array);
            NCTestUtils.assertValueAt(528, 2, 0, array);
            NCTestUtils.assertValueAt(528, 3, 0, array);
            NCTestUtils.assertValueAt(693, 4, 0, array);

            NCTestUtils.assertValueAt(-32768, 0, 7, array);
            NCTestUtils.assertValueAt(-32768, 1, 7, array);
            NCTestUtils.assertValueAt(508, 2, 7, array);
            NCTestUtils.assertValueAt(732, 3, 7, array);
            NCTestUtils.assertValueAt(732, 4, 7, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_rightWindowOut() throws Exception {
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
    public void testReadRaw_topLeftWindowOut() throws Exception {
        final File file = createAvhrrMAFile();
        reader.open(file);

        try {
            final Array array = reader.readRaw(2, 3, new Interval(9, 9), "ch5_earth_counts");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-32767, 0, 0, array);
            NCTestUtils.assertValueAt(-32767, 4, 0, array);
            NCTestUtils.assertValueAt(-32767, 8, 0, array);

            NCTestUtils.assertValueAt(-32767, 0, 4, array);
            NCTestUtils.assertValueAt(798, 4, 4, array);
            NCTestUtils.assertValueAt(751, 8, 4, array);

            NCTestUtils.assertValueAt(-32767, 0, 8, array);
            NCTestUtils.assertValueAt(796, 4, 8, array);
            NCTestUtils.assertValueAt(775, 8, 8, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomRightWindowOut() throws Exception {
        final File file = createAvhrrMAFile();
        reader.open(file);

        try {
            final Array array = reader.readRaw(405, 11961, new Interval(9, 9), "ch5");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-976, 3, 3, array);
            NCTestUtils.assertValueAt(-817, 4, 4, array);
            NCTestUtils.assertValueAt(-32768, 6, 6, array);
            NCTestUtils.assertValueAt(-32768, 8, 8, array);

            NCTestUtils.assertValueAt(-1433, 2, 2, array);
            NCTestUtils.assertValueAt(-1284, 4, 2, array);
            NCTestUtils.assertValueAt(-32768, 6, 8, array);
            NCTestUtils.assertValueAt(-32768, 8, 8, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomLeftWindowOut() throws Exception {
        final File file = createAvhrrMAFile();
        reader.open(file);

        try {
            final Array array = reader.readRaw(0, 11962, new Interval(3, 3), "orbital_temperature_nlines");
            assertNotNull(array);

            NCTestUtils.assertValueAt(-32767, 0, 0, array);
            NCTestUtils.assertValueAt(12146, 1, 0, array);
            NCTestUtils.assertValueAt(12146, 2, 0, array);

            NCTestUtils.assertValueAt(-32767, 0, 1, array);
            NCTestUtils.assertValueAt(12146, 1, 1, array);
            NCTestUtils.assertValueAt(12146, 2, 1, array);

            NCTestUtils.assertValueAt(-32767, 0, 2, array);
            NCTestUtils.assertValueAt(-32767, 1, 2, array);
            NCTestUtils.assertValueAt(-32767, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_scalingAndOffset() throws IOException, InvalidRangeException {
        final File file = createAvhrrMAFile();
        reader.open(file);

        try {
            final Array array = reader.readScaled(95, 7363, new Interval(3, 3), "prt_1");
            assertNotNull(array);

            NCTestUtils.assertValueAt(286.14999360591173, 0, 0, array);
            NCTestUtils.assertValueAt(286.14999360591173, 1, 0, array);
            NCTestUtils.assertValueAt(286.14999360591173, 2, 0, array);

            NCTestUtils.assertValueAt(286.14999360591173, 0, 1, array);
            NCTestUtils.assertValueAt(286.14999360591173, 1, 1, array);
            NCTestUtils.assertValueAt(286.14999360591173, 2, 1, array);

            NCTestUtils.assertValueAt(286.13999360613525, 0, 2, array);
            NCTestUtils.assertValueAt(286.13999360613525, 1, 2, array);
            NCTestUtils.assertValueAt(286.13999360613525, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_onlyScaling_onePixel() throws IOException, InvalidRangeException {
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
    public void testReadScaled_onlyScaling() throws IOException, InvalidRangeException {
        final File file = createAvhrrMAFile();
        reader.open(file);

        try {
            final Array array = reader.readScaled(118, 8091, new Interval(3, 3), "ch5_nedt");
            assertNotNull(array);

            NCTestUtils.assertValueAt(0.14200000674463809, 0, 0, array);
            NCTestUtils.assertValueAt(0.14200000674463809, 1, 0, array);
            NCTestUtils.assertValueAt(0.14700000698212534, 2, 0, array);

            NCTestUtils.assertValueAt(0.14900000707712024, 0, 1, array);
            NCTestUtils.assertValueAt(0.14900000707712024, 1, 1, array);
            NCTestUtils.assertValueAt(0.1500000071246177, 2, 1, array);

            NCTestUtils.assertValueAt(0.15100000717211515, 0, 2, array);
            NCTestUtils.assertValueAt(0.15100000717211515, 1, 2, array);
            NCTestUtils.assertValueAt(0.1500000071246177, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_noScale_noOffset() throws IOException, InvalidRangeException {
        final File file = createAvhrrMAFile();
        reader.open(file);

        try {
            final Array array = reader.readScaled(359, 10237, new Interval(3, 3), "orbital_temperature");
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
    public void testGetProductSize() throws Exception {
        final File file = createAvhrrMAFile();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertNotNull(productSize);
            assertEquals(409, productSize.getNx());
            assertEquals(11963, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator() throws IOException {
        final File file = createAvhrrMAFile();

        try {
            reader.open(file);
            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            final Point2D[] pixelLocation = pixelLocator.getPixelLocation(142.80371, -41.773438);
            assertNotNull(pixelLocation);
            assertEquals(1, pixelLocation.length);

            assertEquals(166.52272838344527, pixelLocation[0].getX(), 1e-8);
            assertEquals(7273.488198427849, pixelLocation[0].getY(), 1e-8);

            final Point2D geoLocation = pixelLocator.getGeoLocation(166.5, 7273.5, null);
            assertNotNull(geoLocation);
            assertEquals(142.8037109375, geoLocation.getX(), 1e-8);
            assertEquals(-41.7734375, geoLocation.getY(), 1e-8);
        } finally {
            reader.close();
        }
    }

    private File createAvhrrMAFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-ma", "v1.5.2", "2010", "01", "01", "20100101113746-ESACCI-L1C-AVHRRMTA_G-v1.5-fv02.0.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}

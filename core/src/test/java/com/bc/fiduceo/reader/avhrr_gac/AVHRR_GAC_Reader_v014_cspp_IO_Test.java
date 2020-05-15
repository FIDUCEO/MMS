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
import com.bc.fiduceo.util.NetCDFUtils;
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
public class AVHRR_GAC_Reader_v014_cspp_IO_Test {

    private AVHRR_GAC_Reader reader;

    @Before
    public void setUp() {
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new AVHRR_GAC_Reader(readerContext);
    }

    @Test
    public void testReadAcquisitionInfo_NOAA11() throws IOException {
        final File file = createAvhrrNOAA11File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1994, 3, 7, 2, 33, 57, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1994, 3, 7, 4, 25, 58, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            assertEquals(2, polygons.size());

            Point[] coordinates = polygons.get(0).getCoordinates();
            assertEquals(159, coordinates.length);
            assertEquals(-140.05999755859375, coordinates[0].getLon(), 1e-8);
            assertEquals(24.733999252319336, coordinates[0].getLat(), 1e-8);

            assertEquals(-50.0830078125, coordinates[23].getLon(), 1e-8);
            assertEquals(85.55500030517578, coordinates[23].getLat(), 1e-8);

            coordinates = polygons.get(1).getCoordinates();
            assertEquals(159, coordinates.length);
            assertEquals(-9.368011474609375, coordinates[0].getLon(), 1e-8);
            assertEquals(-36.70800018310547, coordinates[0].getLat(), 1e-8);

            assertEquals(-118.2819976806641, coordinates[23].getLon(), 1e-8);
            assertEquals(-60.51599884033203, coordinates[23].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = polygons.get(0).getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1994, 3, 7, 2, 33, 57, 0, time);

            coordinates = polygons.get(1).getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1994, 3, 7, 3, 30, 8, 956, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_NOAA17() throws IOException {
        final File file = createAvhrrNOAA17File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2009, 10, 25, 8, 7, 39, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2009, 10, 25, 10, 0, 39, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            assertEquals(3, polygons.size());

            Point[] coordinates = polygons.get(0).getCoordinates();
            assertEquals(91, coordinates.length);
            assertEquals(-148.8730010986328, coordinates[17].getLon(), 1e-8);
            assertEquals(74.93000030517578, coordinates[17].getLat(), 1e-8);

            assertEquals(113.49700164794922, coordinates[58].getLon(), 1e-8);
            assertEquals(68.74299621582031, coordinates[58].getLat(), 1e-8);

            coordinates = polygons.get(1).getCoordinates();
            assertEquals(29, coordinates.length);
            assertEquals(0.0010000000474974513, coordinates[0].getLon(), 1e-8);
            assertEquals(55.702999114990234, coordinates[0].getLat(), 1e-8);

            assertEquals(19.881999969482422, coordinates[23].getLon(), 1e-8);
            assertEquals(54.77199935913086, coordinates[23].getLat(), 1e-8);

            coordinates = polygons.get(2).getCoordinates();
            assertEquals(223, coordinates.length);
            assertEquals(-0.0020141601562500004, coordinates[0].getLon(), 1e-8);
            assertEquals(47.821998596191406, coordinates[0].getLat(), 1e-8);

            assertEquals(-148.05999755859375, coordinates[57].getLon(), 1e-8);
            assertEquals(-51.93899917602539, coordinates[57].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(3, timeAxes.length);
            coordinates = polygons.get(0).getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2009, 10, 25, 8, 7, 39, 0, time);

            coordinates = polygons.get(1).getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2009, 10, 25, 8, 47, 50, 345, time);

            coordinates = polygons.get(2).getCoordinates();
            time = timeAxes[2].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2009, 10, 25, 9, 23, 3, 766, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_NOAA18() throws IOException {
        final File file = createAvhrrNOAA18File();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            final long referenceTime = 1479845292000L;
            assertEquals(referenceTime, timeLocator.getTimeFor(168, 0));
            assertEquals(referenceTime + 508, timeLocator.getTimeFor(168, 1));
            assertEquals(referenceTime + 6997, timeLocator.getTimeFor(169, 14));
            assertEquals(referenceTime + 507507, timeLocator.getTimeFor(170, 1015));
            assertEquals(referenceTime + 1007996, timeLocator.getTimeFor(171, 2016));
            assertEquals(referenceTime + 6777507, timeLocator.getTimeFor(172, 13555));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_NOAA19() throws IOException {
        final File file = createAvhrrNOAA19File();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            final long referenceTime = 1271141360000L;
            assertEquals(referenceTime, timeLocator.getTimeFor(301, 0));
            assertEquals(referenceTime + 7500, timeLocator.getTimeFor(304, 15));
            assertEquals(referenceTime + 1008001, timeLocator.getTimeFor(304, 2016));
            assertEquals(referenceTime + 2008500, timeLocator.getTimeFor(305, 4017));
            assertEquals(referenceTime + 5119499, timeLocator.getTimeFor(172, 10239));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_NOAA10() throws IOException {
        final File file = createAvhrrNOAA10File();

        try {
            reader.open(file);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(35, 210, new Interval(3, 3));
            NCTestUtils.assertValueAt(634188255, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(634188256, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(634188256, 2, 2, acquisitionTime);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_NOAA11_singlePixel() throws IOException {
        final File file = createAvhrrNOAA11File();

        try {
            reader.open(file);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(40, 215, new Interval(1, 1));
            NCTestUtils.assertValueAt(763007637, 0, 0, acquisitionTime);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_NOAA17_borderPixel() throws IOException {
        final File file = createAvhrrNOAA17File();

        try {
            reader.open(file);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(408, 211, new Interval(3, 3));
            NCTestUtils.assertValueAt(1256458164, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1256458164, 1, 0, acquisitionTime);
            NCTestUtils.assertValueAt(NetCDFUtils.getDefaultFillValue(int.class).intValue(), 2, 2, acquisitionTime);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_NOAA18() throws IOException {
        final File file = createAvhrrNOAA18File();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(23, variables.size());
            Variable variable = variables.get(1);
            assertEquals("lon", variable.getFullName());

            variable = variables.get(9);
            assertEquals("ch1_noise", variable.getFullName());

            variable = variables.get(15);
            assertEquals("satellite_zenith_angle", variable.getFullName());

            variable = variables.get(22);
            assertEquals("l1b_line_number", variable.getFullName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_windowCenter_NOAA19() throws Exception {
        final File file = createAvhrrNOAA19File();
        reader.open(file);
        try {
            final Array array = reader.readRaw(5, 15, new Interval(3, 3), "ch1");
            assertNotNull(array);
            assertEquals(9, array.getSize());

            NCTestUtils.assertValueAt(118, 0, 0, array);
            NCTestUtils.assertValueAt(112, 1, 0, array);
            NCTestUtils.assertValueAt(107, 2, 0, array);
            NCTestUtils.assertValueAt(118, 0, 1, array);
            NCTestUtils.assertValueAt(112, 1, 1, array);
            NCTestUtils.assertValueAt(118, 2, 1, array);
            NCTestUtils.assertValueAt(118, 0, 2, array);
            NCTestUtils.assertValueAt(112, 1, 2, array);
            NCTestUtils.assertValueAt(118, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomWindowOut_NOAA10() throws Exception {
        final File file = createAvhrrNOAA10File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(6, 12446, new Interval(3, 3), "ch1_noise");
            assertNotNull(array);
            assertEquals(9, array.getSize());

            NCTestUtils.assertValueAt(0, 0, 0, array);
            NCTestUtils.assertValueAt(0, 1, 0, array);
            NCTestUtils.assertValueAt(0, 2, 0, array);
            NCTestUtils.assertValueAt(0, 0, 1, array);
            NCTestUtils.assertValueAt(0, 1, 1, array);
            NCTestUtils.assertValueAt(0, 2, 1, array);
            NCTestUtils.assertValueAt(-32768, 0, 2, array);
            NCTestUtils.assertValueAt(-32768, 1, 2, array);
            NCTestUtils.assertValueAt(-32768, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topWindowOut_NOAA11() throws Exception {
        final File file = createAvhrrNOAA11File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(4, 1, new Interval(3, 4), "ch2");
            assertNotNull(array);
            assertEquals(12, array.getSize());

            NCTestUtils.assertValueAt(-32768, 0, 0, array);
            NCTestUtils.assertValueAt(-32768, 1, 0, array);
            NCTestUtils.assertValueAt(-32768, 2, 0, array);

            NCTestUtils.assertValueAt(961, 0, 1, array);
            NCTestUtils.assertValueAt(1210, 1, 1, array);
            NCTestUtils.assertValueAt(1223, 2, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_leftWindowOut_NOAA17() throws Exception {
        final File file = createAvhrrNOAA17File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(2, 120, new Interval(8, 8), "ch2_noise");
            assertNotNull(array);
            assertEquals(64, array.getSize());

            NCTestUtils.assertValueAt(-32768, 0, 0, array);
            NCTestUtils.assertValueAt(-32768, 1, 0, array);
            NCTestUtils.assertValueAt(0, 2, 0, array);
            NCTestUtils.assertValueAt(0, 3, 0, array);
            NCTestUtils.assertValueAt(0, 4, 0, array);

            NCTestUtils.assertValueAt(-32768, 0, 7, array);
            NCTestUtils.assertValueAt(-32768, 1, 7, array);
            NCTestUtils.assertValueAt(0, 2, 7, array);
            NCTestUtils.assertValueAt(0, 3, 7, array);
            NCTestUtils.assertValueAt(0, 4, 7, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_rightWindowOut_NOAA18() throws Exception {
        final File file = createAvhrrNOAA18File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(407, 242, new Interval(9, 9), "ch3b");
            assertNotNull(array);
            assertEquals(81, array.getSize());

            NCTestUtils.assertValueAt(-1948, 4, 0, array);
            NCTestUtils.assertValueAt(-1807, 5, 0, array);
            NCTestUtils.assertValueAt(-32768, 6, 0, array);
            NCTestUtils.assertValueAt(-32768, 7, 0, array);
            NCTestUtils.assertValueAt(-32768, 8, 0, array);

            NCTestUtils.assertValueAt(-2236, 4, 7, array);
            NCTestUtils.assertValueAt(-2713, 5, 7, array);
            NCTestUtils.assertValueAt(-32768, 6, 7, array);
            NCTestUtils.assertValueAt(-32768, 7, 7, array);
            NCTestUtils.assertValueAt(-32768, 8, 7, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topLeftWindowOut_NOAA19() throws Exception {
        final File file = createAvhrrNOAA19File();
        reader.open(file);

        try {
            final Array array = reader.readRaw(2, 3, new Interval(9, 9), "ch3b_nedt");
            assertNotNull(array);
            assertEquals(81, array.getSize());

            NCTestUtils.assertValueAt(-32768, 0, 0, array);
            NCTestUtils.assertValueAt(-32768, 4, 0, array);
            NCTestUtils.assertValueAt(-32768, 8, 0, array);

            NCTestUtils.assertValueAt(-32768, 0, 4, array);
            NCTestUtils.assertValueAt(-32768, 4, 4, array);
            NCTestUtils.assertValueAt(-32768, 8, 4, array);

            NCTestUtils.assertValueAt(-32768, 0, 8, array);
            NCTestUtils.assertValueAt(-32768, 4, 8, array);
            NCTestUtils.assertValueAt(-32768, 8, 8, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomRightWindowOut_NOAA10() throws Exception {
        final File file = createAvhrrNOAA10File();
        reader.open(file);

        try {
            Array array = reader.readRaw(405, 12446, new Interval(9, 9), "ch4");
            assertNotNull(array);

            NCTestUtils.assertValueAt(134, 3, 3, array);
            NCTestUtils.assertValueAt(-26, 4, 4, array);
            NCTestUtils.assertValueAt(-32768, 6, 6, array);
            NCTestUtils.assertValueAt(-32768, 8, 8, array);

            NCTestUtils.assertValueAt(415, 2, 2, array);
            NCTestUtils.assertValueAt(356, 4, 2, array);
            NCTestUtils.assertValueAt(-32768, 6, 8, array);
            NCTestUtils.assertValueAt(-32768, 8, 8, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_scalingAndOffset_NOAA11() throws IOException {
        final File file = createAvhrrNOAA11File();
        reader.open(file);

        try {
            final Array array = reader.readScaled(93, 7328, new Interval(3, 3), "cloud_probability");
            assertNotNull(array);

            NCTestUtils.assertValueAt(0.9960619928315282, 0, 0, array);
            NCTestUtils.assertValueAt(0.9921249928884208, 1, 0, array);
            NCTestUtils.assertValueAt(0.9921249928884208, 2, 0, array);

            NCTestUtils.assertValueAt(0.9960619928315282, 0, 1, array);
            NCTestUtils.assertValueAt(0.9960619928315282, 1, 1, array);
            NCTestUtils.assertValueAt(0.9921249928884208, 2, 1, array);

            NCTestUtils.assertValueAt(0.9921249928884208, 0, 2, array);
            NCTestUtils.assertValueAt(0.9921249928884208, 1, 2, array);
            NCTestUtils.assertValueAt(0.9921249928884208, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_onlyScaling_onePixel_NOAA17() throws IOException {
        final File file = createAvhrrNOAA17File();
        reader.open(file);

        try {
            final Array array = reader.readScaled(82, 4603, new Interval(1, 1), "ch4_nedt");
            assertNotNull(array);
            assertEquals(1, array.getSize());

            final Index index = array.getIndex();
            assertEquals(0.01900000125169754, array.getFloat(index), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_onlyScaling_NOAA18() throws IOException {
        final File file = createAvhrrNOAA18File();
        reader.open(file);

        try {
            final Array array = reader.readScaled(117, 8090, new Interval(3, 3), "relative_azimuth_angle");
            assertNotNull(array);

            NCTestUtils.assertValueAt(13.649999694675207, 0, 0, array);
            NCTestUtils.assertValueAt(13.649999694675207, 1, 0, array);
            NCTestUtils.assertValueAt(13.649999694898725, 2, 0, array);

            NCTestUtils.assertValueAt(13.659999694675207, 0, 1, array);
            NCTestUtils.assertValueAt(13.659999694675207, 1, 1, array);
            NCTestUtils.assertValueAt(13.659999694675207, 2, 1, array);

            NCTestUtils.assertValueAt(13.659999694675207, 0, 2, array);
            NCTestUtils.assertValueAt(13.659999694675207, 1, 2, array);
            NCTestUtils.assertValueAt(13.659999694675207, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_noScale_noOffset_NOAA19() throws IOException {
        final File file = createAvhrrNOAA19File();
        reader.open(file);

        try {
            final Array array = reader.readScaled(358, 12236, new Interval(3, 3), "cloud_mask");
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
    public void testReadRaw_topRightWindowOut_NOAA10() throws Exception {
        final File file = createAvhrrNOAA10File();

        try {
            reader.open(file);
            Array array = reader.readRaw(407, 3, new Interval(9, 9), "dtime");
            assertNotNull(array);

            NCTestUtils.assertValueAt(2147483647, 4, 0, array);
            NCTestUtils.assertValueAt(2147483647, 5, 0, array);
            NCTestUtils.assertValueAt(2147483647, 6, 0, array);
            NCTestUtils.assertValueAt(2147483647, 7, 0, array);
            NCTestUtils.assertValueAt(2147483647, 8, 0, array);

            NCTestUtils.assertValueAt(0, 4, 1, array);
            NCTestUtils.assertValueAt(0, 5, 1, array);
            NCTestUtils.assertValueAt(2147483647, 6, 1, array);
            NCTestUtils.assertValueAt(2147483647, 7, 1, array);
            NCTestUtils.assertValueAt(2147483647, 8, 1, array);

            NCTestUtils.assertValueAt(3, 4, 8, array);
            NCTestUtils.assertValueAt(3, 5, 8, array);
            NCTestUtils.assertValueAt(2147483647, 6, 8, array);
            NCTestUtils.assertValueAt(2147483647, 7, 8, array);
            NCTestUtils.assertValueAt(2147483647, 8, 8, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_NOAA11() throws Exception {
        final File file = createAvhrrNOAA11File();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertNotNull(productSize);
            assertEquals(409, productSize.getNx());
            assertEquals(13443, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator_NOAA17() throws IOException {
        final File file = createAvhrrNOAA17File();

        try {
            reader.open(file);
            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            final Point2D[] pixelLocation = pixelLocator.getPixelLocation(-167.222, -57.508);
            assertNotNull(pixelLocation);
            assertEquals(1, pixelLocation.length);

            assertEquals(180.35479940164305, pixelLocation[0].getX(), 1e-8);
            assertEquals(9329.483038106515, pixelLocation[0].getY(), 1e-8);

            final Point2D geoLocation = pixelLocator.getGeoLocation(180.5, 9329.5, null);
            assertNotNull(geoLocation);
            assertEquals(-167.2220001220703, geoLocation.getX(), 1e-8);
            assertEquals(-57.507999420166016, geoLocation.getY(), 1e-8);
        } finally {
            reader.close();
        }
    }

    private File createAvhrrNOAA10File() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n10", "v01.4-cspp", "1990", "02", "05", "19900205032100-ESACCI-L1C-AVHRR10_G-fv01.0.nc"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File createAvhrrNOAA11File() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n11", "v01.4-cspp", "1994", "03", "07", "19940307023300-ESACCI-L1C-AVHRR11_G-fv01.0.nc"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File createAvhrrNOAA17File() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n17", "v01.4-cspp", "2009", "10", "25", "20091025080600-ESACCI-L1C-AVHRR17_G-fv01.0.nc"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File createAvhrrNOAA18File() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n18", "v01.4-cspp", "2016", "11", "22", "20161122200700-ESACCI-L1C-AVHRR18_G-fv01.0.nc"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File createAvhrrNOAA19File() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n19", "v01.4-cspp", "2010", "04", "13", "20100413064800-ESACCI-L1C-AVHRR19_G-fv01.0.nc"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}

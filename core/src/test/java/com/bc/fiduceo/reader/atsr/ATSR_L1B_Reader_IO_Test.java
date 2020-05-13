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

package com.bc.fiduceo.reader.atsr;

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
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class ATSR_L1B_Reader_IO_Test {

    private ATSR_L1B_Reader reader;

    @Before
    public void setUp() {
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new ATSR_L1B_Reader(readerContext);
    }

    @Test
    public void testReadAcquisitionInfo_ATSR1() throws IOException {
        final File file = getAtsr1File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1993, 8, 5, 21, 0, 30, 240, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1993, 8, 5, 22, 41, 8, 490, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);
            final Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(137, coordinates.length);
            assertEquals(24.824844360351562, coordinates[0].getLon(), 1e-8);
            assertEquals(0.4836359918117523, coordinates[0].getLat(), 1e-8);

            assertEquals(-5.181085109710694, coordinates[68].getLon(), 1e-8);
            assertEquals(-0.4404639899730683, coordinates[68].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1993, 8, 5, 21, 0, 30, 365, time);
            time = timeAxes[0].getTime(coordinates[68]);
            TestUtil.assertCorrectUTCDate(1993, 8, 5, 22, 41, 8, 367, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_ATSR2() throws IOException {
        final File file = getAtsr2File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1998, 4, 24, 5, 57, 54, 720, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1998, 4, 24, 7, 38, 32, 970, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);
            final Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(137, coordinates.length);
            assertEquals(-110.77146911621094, coordinates[1].getLon(), 1e-8);
            assertEquals(6.13779592514038, coordinates[1].getLat(), 1e-8);

            assertEquals(-138.29689025878906, coordinates[69].getLon(), 1e-8);
            assertEquals(-6.176464080810547, coordinates[69].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1998, 4, 24, 5, 57, 54, 845, time);
            time = timeAxes[0].getTime(coordinates[68]);
            TestUtil.assertCorrectUTCDate(1998, 4, 24, 7, 38, 32, 846, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_AATSR() throws IOException {
        final File file = getAatsrFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2006, 2, 15, 7, 8, 52, 812, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2006, 2, 15, 8, 57, 40, 662, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);
            final Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(147, coordinates.length);
            assertEquals(-135.95962524414062, coordinates[2].getLon(), 1e-8);
            assertEquals(3.0329780578613286, coordinates[2].getLat(), 1e-8);

            assertEquals(-170.1296844482422, coordinates[73].getLon(), 1e-8);
            assertEquals(19.727405548095703, coordinates[73].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2006, 2, 15, 7, 8, 52, 892, time);
            time = timeAxes[0].getTime(coordinates[73]);
            TestUtil.assertCorrectUTCDate(2006, 2, 15, 8, 57, 40, 644, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_ATSR1() throws IOException {
        final File file = getAtsr1File();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(744584430240L, timeLocator.getTimeFor(15, 0));
            assertEquals(744584445240L, timeLocator.getTimeFor(16, 100));
            assertEquals(744585930240L, timeLocator.getTimeFor(20, 10000));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_ATSR2() throws IOException {
        final File file = getAtsr2File();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(893397474720L, timeLocator.getTimeFor(15, 0));
            assertEquals(893400484770L, timeLocator.getTimeFor(18, 20067));
            assertEquals(893403512970L, timeLocator.getTimeFor(20, 40255));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_AATSR() throws IOException {
        final File file = getAatsrFile();

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            assertEquals(1139987332812L, timeLocator.getTimeFor(16, 0));
            assertEquals(1139990490912L, timeLocator.getTimeFor(198, 21054));
            assertEquals(1139993860662L, timeLocator.getTimeFor(22, 43519));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_ATSR1() throws IOException {
        final File file = getAtsr1File();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(33, variables.size());

            Variable variable = variables.get(0);
            assertEquals("btemp_nadir_1200", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(12);
            assertEquals("reflec_fward_0670", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(23);
            assertEquals("lon_corr_fward", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());

            variable = variables.get(32);
            assertEquals("view_azimuth_fward", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_ATSR2() throws IOException {
        final File file = getAtsr2File();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(33, variables.size());

            Variable variable = variables.get(1);
            assertEquals("btemp_nadir_1100", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(13);
            assertEquals("reflec_fward_0550", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(24);
            assertEquals("altitude", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());

            variable = variables.get(32);
            assertEquals("view_azimuth_fward", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_AATSR() throws IOException {
        final File file = getAatsrFile();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(33, variables.size());

            Variable variable = variables.get(2);
            assertEquals("btemp_nadir_0370", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(14);
            assertEquals("confid_flags_nadir", variable.getShortName());
            assertEquals(DataType.SHORT, variable.getDataType());

            variable = variables.get(25);
            assertEquals("sun_elev_nadir", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());

            variable = variables.get(32);
            assertEquals("view_azimuth_fward", variable.getShortName());
            assertEquals(DataType.FLOAT, variable.getDataType());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_ATSR1() throws IOException {
        final File file = getAtsr1File();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertEquals(512, productSize.getNx());
            assertEquals(40256, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize_AATSR() throws IOException {
        final File file = getAatsrFile();

        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertEquals(512, productSize.getNx());
            assertEquals(43520, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_ATSR2() throws IOException {
        final File file = getAtsr2File();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(108, 2538, interval);
            assertNotNull(acquisitionTime);

            NCTestUtils.assertValueAt(893397855, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(893397855, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(893397855, 2, 2, acquisitionTime);
            NCTestUtils.assertValueAt(893397856, 1, 3, acquisitionTime);
            NCTestUtils.assertValueAt(893397856, 0, 4, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_ATSR1_singlePixel() throws IOException {
        final File file = getAtsr1File();

        try {
            reader.open(file);

            final Interval interval = new Interval(1, 1);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(110, 2542, interval);
            assertNotNull(acquisitionTime);

            NCTestUtils.assertValueAt(744584812, 0, 0, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_AATSR_borderPixel_X() throws IOException {
        final File file = getAatsrFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(1, 268, interval);
            assertNotNull(acquisitionTime);

            NCTestUtils.assertValueAt(-2147483647, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1139987373, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(1139987373, 3, 4, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_AATSR_borderPixel_Y() throws IOException {
        final File file = getAatsrFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(278, 43519, interval);
            assertNotNull(acquisitionTime);

            NCTestUtils.assertValueAt(1139993860, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1139993861, 1, 1, acquisitionTime);
            NCTestUtils.assertValueAt(-2147483647, 2, 4, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_ATSR1() throws IOException {
        final File file = getAtsr1File();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readScaled(14, 232, interval, "btemp_nadir_1200");
            NCTestUtils.assertValueAt(287.3299865722656, 0, 0, array);
            NCTestUtils.assertValueAt(287.2799987792969, 1, 0, array);

            array = reader.readScaled(0, 233, interval, "reflec_nadir_1600");
            NCTestUtils.assertValueAt(-0.019999999552965164, 0, 1, array);
            NCTestUtils.assertValueAt(-0.019999999552965164, 1, 1, array);
            NCTestUtils.assertValueAt(0.0, 2, 1, array);

            array = reader.readScaled(511, 235, interval, "reflec_nadir_0550");
            NCTestUtils.assertValueAt(0.0, 2, 2, array);
            NCTestUtils.assertValueAt(-0.019999999552965164, 3, 2, array);
            NCTestUtils.assertValueAt(-0.019999999552965164, 4, 2, array);

            array = reader.readScaled(145, 0, interval, "btemp_fward_1200");
            NCTestUtils.assertValueAt(-0.019999999552965164, 3, 0, array);
            NCTestUtils.assertValueAt(-0.019999999552965164, 3, 1, array);
            NCTestUtils.assertValueAt(226.5500030517578, 3, 2, array);
            NCTestUtils.assertValueAt(223.33999633789062, 3, 3, array);

            array = reader.readScaled(155, 40255, interval, "reflec_fward_1600");
            NCTestUtils.assertValueAt(0.0, 2, 0, array);
            NCTestUtils.assertValueAt(0.0, 2, 1, array);
            NCTestUtils.assertValueAt(0.0, 2, 2, array);
            NCTestUtils.assertValueAt(-0.019999999552965164, 2, 3, array);

            array = reader.readScaled(157, 4023, interval, "reflec_fward_0550");
            NCTestUtils.assertValueAt(0.0, 2, 0, array);
            NCTestUtils.assertValueAt(0.0, 3, 0, array);

            array = reader.readScaled(196, 4153, interval, "cloud_flags_fward");
            NCTestUtils.assertValueAt(0, 4, 0, array);
            NCTestUtils.assertValueAt(0, 0, 1, array);
            NCTestUtils.assertValueAt(4098, 1, 1, array);

            array = reader.readScaled(198, 4155, interval, "lat_corr_nadir");
            NCTestUtils.assertValueAt(0.0, 1, 1, array);
            NCTestUtils.assertValueAt(0.0, 2, 1, array);

            array = reader.readScaled(200, 4157, interval, "view_elev_nadir");
            NCTestUtils.assertValueAt(85.38400268554688, 1, 1, array);
            NCTestUtils.assertValueAt(85.46600341796875, 2, 1, array);

            array = reader.readScaled(202, 4159, interval, "latitude");
            NCTestUtils.assertValueAt(36.974647521972656, 3, 1, array);
            NCTestUtils.assertValueAt(36.972557067871094, 4, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_ATSR2() throws IOException {
        final File file = getAtsr2File();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readScaled(15, 233, interval, "btemp_nadir_1100");
            NCTestUtils.assertValueAt(285.239990234375, 1, 0, array);
            NCTestUtils.assertValueAt(286.5799865722656, 2, 0, array);

            array = reader.readScaled(0, 234, interval, "reflec_nadir_0870");
            NCTestUtils.assertValueAt(-0.019999999552965164, 3, 1, array);
            NCTestUtils.assertValueAt(-0.019999999552965164, 4, 1, array);
            NCTestUtils.assertValueAt(-0.019999999552965164, 0, 2, array);

            array = reader.readScaled(511, 236, interval, "confid_flags_nadir");
            NCTestUtils.assertValueAt(8, 1, 2, array);
            NCTestUtils.assertValueAt(8, 2, 2, array);
            NCTestUtils.assertValueAt(65534, 3, 2, array);
            NCTestUtils.assertValueAt(65534, 4, 2, array);

            array = reader.readScaled(146, 0, interval, "btemp_fward_0370");
            NCTestUtils.assertValueAt(-0.019999999552965164, 4, 0, array);
            NCTestUtils.assertValueAt(-0.019999999552965164, 4, 1, array);
            NCTestUtils.assertValueAt(283.92999267578125, 4, 2, array);
            NCTestUtils.assertValueAt(284.70001220703125, 4, 3, array);

            array = reader.readScaled(155, 40255, interval, "reflec_fward_0870");
            NCTestUtils.assertValueAt(-0.019999999552965164, 2, 0, array);
            NCTestUtils.assertValueAt(-0.019999999552965164, 2, 1, array);
            NCTestUtils.assertValueAt(-0.019999999552965164, 2, 2, array);
            NCTestUtils.assertValueAt(-0.019999999552965164, 2, 3, array);

            array = reader.readScaled(158, 4024, interval, "confid_flags_fward");
            NCTestUtils.assertValueAt(2, 3, 0, array);
            NCTestUtils.assertValueAt(12, 4, 0, array);

            array = reader.readScaled(197, 4154, interval, "lat_corr_nadir");
            NCTestUtils.assertValueAt(0.0, 0, 1, array);
            NCTestUtils.assertValueAt(0.0, 1, 1, array);
            NCTestUtils.assertValueAt(0.0, 2, 1, array);

            array = reader.readScaled(199, 4156, interval, "lon_corr_nadir");
            NCTestUtils.assertValueAt(0.0, 3, 1, array);
            NCTestUtils.assertValueAt(0.0, 4, 1, array);

            array = reader.readScaled(201, 4158, interval, "view_elev_nadir");
            NCTestUtils.assertValueAt(85.33837127685547, 0, 2, array);
            NCTestUtils.assertValueAt(85.42138671875, 1, 2, array);

            array = reader.readScaled(203, 4160, interval, "longitude");
            NCTestUtils.assertValueAt(-120.4134292602539, 2, 2, array);
            NCTestUtils.assertValueAt(-120.42435455322266, 3, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_AATSR_borderPixel() throws IOException {
        final File file = getAatsrFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readScaled(511, 232, interval, "btemp_nadir_1200");
            NCTestUtils.assertValueAt(268.45001220703125, 0, 0, array);
            NCTestUtils.assertValueAt(267.67999267578125, 1, 0, array);
            NCTestUtils.assertValueAt(267.29998779296875, 2, 0, array);
            NCTestUtils.assertValueAt(-0.02, 3, 0, array);
            NCTestUtils.assertValueAt(-0.019999999552965164, 4, 0, array);


            array = reader.readScaled(511, 2845, interval, "cloud_flags_nadir");
            NCTestUtils.assertValueAt(2402, 0, 0, array);
            NCTestUtils.assertValueAt(2402, 1, 0, array);
            NCTestUtils.assertValueAt(354, 2, 0, array);
            NCTestUtils.assertValueAt(65534, 3, 0, array);
            NCTestUtils.assertValueAt(65534, 4, 0, array);

            array = reader.readScaled(106, 43519, interval, "cloud_flags_nadir");
            NCTestUtils.assertValueAt(34, 0, 0, array);
            NCTestUtils.assertValueAt(34, 0, 1, array);
            NCTestUtils.assertValueAt(34, 0, 2, array);
            NCTestUtils.assertValueAt(65534, 0, 3, array);
            NCTestUtils.assertValueAt(65534, 0, 4, array);

            array = reader.readScaled(1, 2845, interval, "cloud_flags_nadir");
            NCTestUtils.assertValueAt(65534, 0, 0, array);
            NCTestUtils.assertValueAt(3554, 1, 0, array);
            NCTestUtils.assertValueAt(3554, 2, 0, array);
            NCTestUtils.assertValueAt(3554, 3, 0, array);
            NCTestUtils.assertValueAt(3554, 4, 0, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_AATSR() throws IOException, InvalidRangeException {
        final File file = getAatsrFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(25, 333, interval, "btemp_nadir_0370");
            NCTestUtils.assertValueAt(29697, 3, 0, array);
            NCTestUtils.assertValueAt(29698, 4, 0, array);

            array = reader.readRaw(0, 335, interval, "reflec_nadir_0670");
            NCTestUtils.assertValueAt(-2, 0, 1, array);
            NCTestUtils.assertValueAt(-2, 1, 1, array);
            NCTestUtils.assertValueAt(0, 2, 1, array);

            array = reader.readRaw(511, 337, interval, "cloud_flags_nadir");
            NCTestUtils.assertValueAt(98, 2, 1, array);
            NCTestUtils.assertValueAt(-2, 3, 1, array);
            NCTestUtils.assertValueAt(-2, 4, 1, array);

            array = reader.readRaw(217, 0, interval, "btemp_fward_1100");
            NCTestUtils.assertValueAt(-2, 2, 0, array);
            NCTestUtils.assertValueAt(-2, 2, 1, array);
            NCTestUtils.assertValueAt(-1, 2, 2, array);
            NCTestUtils.assertValueAt(-1, 2, 3, array);

            array = reader.readRaw(219, 43519, interval, "reflec_fward_0870");
            NCTestUtils.assertValueAt(-1, 2, 0, array);
            NCTestUtils.assertValueAt(-1, 2, 1, array);
            NCTestUtils.assertValueAt(-1, 2, 2, array);
            NCTestUtils.assertValueAt(-2, 2, 3, array);

            array = reader.readRaw(244, 21770, interval, "reflec_fward_0550");
            NCTestUtils.assertValueAt(3254, 0, 2, array);
            NCTestUtils.assertValueAt(2581, 1, 2, array);

            array = reader.readRaw(246, 21772, interval, "sun_elev_nadir");
            NCTestUtils.assertValueAt(55.33750534057617, 2, 2, array);
            NCTestUtils.assertValueAt(55.34658432006836, 3, 2, array);

            array = reader.readRaw(248, 21774, interval, "altitude");
            NCTestUtils.assertValueAt(757.2406005859375, 4, 2, array);
            NCTestUtils.assertValueAt(744.5018920898438, 0, 3, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_AATSR_corner_pixels() throws IOException, InvalidRangeException {
        final File file = getAatsrFile();

        try {
            reader.open(file);

            final Interval interval = new Interval(5, 5);
            Array array = reader.readRaw(0, 0, interval, "confid_flags_nadir");
            NCTestUtils.assertValueAt(-2, 0, 2, array);
            NCTestUtils.assertValueAt(-2, 1, 2, array);
            NCTestUtils.assertValueAt(4, 2, 2, array);
            NCTestUtils.assertValueAt(4, 3, 2, array);
            NCTestUtils.assertValueAt(4, 4, 2, array);

            array = reader.readRaw(511, 0, interval, "confid_flags_fward");
            NCTestUtils.assertValueAt(4, 0, 2, array);
            NCTestUtils.assertValueAt(4, 1, 2, array);
            NCTestUtils.assertValueAt(4, 2, 2, array);
            NCTestUtils.assertValueAt(-2, 3, 2, array);
            NCTestUtils.assertValueAt(-2, 4, 2, array);

            array = reader.readRaw(511, 43519, interval, "cloud_flags_nadir");
            NCTestUtils.assertValueAt(34, 0, 2, array);
            NCTestUtils.assertValueAt(34, 1, 2, array);
            NCTestUtils.assertValueAt(34, 2, 2, array);
            NCTestUtils.assertValueAt(-2, 3, 2, array);
            NCTestUtils.assertValueAt(-2, 4, 2, array);

            array = reader.readRaw(0, 43519, interval, "cloud_flags_fward");
            NCTestUtils.assertValueAt(-2, 0, 2, array);
            NCTestUtils.assertValueAt(-2, 1, 2, array);
            NCTestUtils.assertValueAt(34, 2, 2, array);
            NCTestUtils.assertValueAt(34, 3, 2, array);
            NCTestUtils.assertValueAt(34, 4, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator_ATSR1() throws IOException {
        final File file = getAtsr1File();

        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull(pixelLocator);

            Point2D geoLocation = pixelLocator.getGeoLocation(144.5, 20044.5, null);
            assertEquals(-171.0328446073869, geoLocation.getX(), 1e-8);
            assertEquals(0.6732450926862658, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(67.5, 25804.5, null);
            assertEquals(173.32764437329226, geoLocation.getX(), 1e-8);
            assertEquals(-49.92663087487221, geoLocation.getY(), 1e-8);

            Point2D[] pixelLocation = pixelLocator.getPixelLocation(29.54, -69.286);
            assertEquals(1, pixelLocation.length);
            assertEquals(115.56233317921996, pixelLocation[0].getX(), 1e-8);
            assertEquals(32231.612908326668, pixelLocation[0].getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(172, -89);
            assertEquals(0, pixelLocation.length);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetSubscenePixelLocator_ATSR2() throws IOException {
        final File file = getAtsr2File();

        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getSubScenePixelLocator(null); // polygon is ignored, we do not split geometries tb 2016-08-11
            assertNotNull(pixelLocator);

            Point2D geoLocation = pixelLocator.getGeoLocation(446.5, 5794.5, null);
            assertEquals(-128.86650544024937, geoLocation.getX(), 1e-8);
            assertEquals(50.66542253494263, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(470.5, 21176.5, null);
            assertEquals(55.2723475388128, geoLocation.getX(), 1e-8);
            assertEquals(-9.990966130197048, geoLocation.getY(), 1e-8);

            Point2D[] pixelLocation = pixelLocator.getPixelLocation(-112.569, -62.085);
            assertEquals(1, pixelLocation.length);
            assertEquals(74.4803862445492, pixelLocation[0].getX(), 1e-8);
            assertEquals(33108.46804440283, pixelLocation[0].getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(74.5, 33108.5, null);
            assertEquals(-112.56937827919018, geoLocation.getX(), 1e-8);
            assertEquals(-62.08462750196457, geoLocation.getY(), 1e-8);

            pixelLocation = pixelLocator.getPixelLocation(172, -89);
            assertEquals(0, pixelLocation.length);
        } finally {
            reader.close();
        }
    }

    private File getAtsr1File() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"atsr-e1", "v3", "1993", "08", "05", "AT1_TOA_1PURAL19930805_210030_000000004015_00085_10751_0000.E1"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File getAtsr2File() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"atsr-e2", "v3", "1998", "04", "24", "AT2_TOA_1PURAL19980424_055754_000000001031_00348_15733_0000.E2"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File getAatsrFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"aatsr-en", "v3", "2006", "02", "15", "ATS_TOA_1PUUPA20060215_070852_000065272045_00120_20715_4282.N1"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}

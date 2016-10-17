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

package com.bc.fiduceo.reader.ssmt2;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.reader.WindowReader;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class SSMT2_Reader_IO_Test {

    private SSMT2_Reader reader;
    private File testDataDirectory;

    @Before
    public void setUp() throws IOException {
        reader = new SSMT2_Reader(new GeometryFactory(GeometryFactory.Type.S2));
        testDataDirectory = TestUtil.getTestDataDirectory();
    }

    @Test
    public void testReadAcquisitionInfo_F11() throws IOException, ParseException {
        final File f11File = createSSMT2_F11_File();

        try (SSMT2_Reader r = reader) {
            r.open(f11File);

            final AcquisitionInfo acquisitionInfo = r.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1994, 1, 28, 4, 12, 22, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1994, 1, 28, 5, 54, 16, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.ASCENDING, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);

            Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(75, coordinates.length);

            assertEquals(-149.76181030273438, coordinates[0].getLon(), 1e-8);
            assertEquals(1.3309934139251711, coordinates[0].getLat(), 1e-8);

            assertEquals(-126.34518432617186, coordinates[24].getLon(), 1e-8);
            assertEquals(-69.82559204101562, coordinates[24].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            coordinates = timeAxes[0].getGeometry().getCoordinates();
            final Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1994, 1, 28, 4, 12, 22, time);
        }
    }

    @Test
    public void testReadAcquisitionInfo_F14() throws IOException, ParseException {
        final File f14File = createSSMT2_F14_File();

        try (SSMT2_Reader r = reader) {
            r.open(f14File);

            final AcquisitionInfo acquisitionInfo = r.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2001, 6, 14, 12, 29, 4, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2001, 6, 14, 14, 10, 58, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.ASCENDING, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof Polygon);

            Point[] coordinates = boundingGeometry.getCoordinates();
            assertEquals(75, coordinates.length);

            assertEquals(128.3829803466797, coordinates[0].getLon(), 1e-8);
            assertEquals(1.6132754087448118, coordinates[0].getLat(), 1e-8);

            assertEquals(150.22152709960938, coordinates[24].getLon(), 1e-8);
            assertEquals(-69.72442626953125, coordinates[24].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(1, timeAxes.length);
            coordinates = timeAxes[0].getGeometry().getCoordinates();
            final Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(2001, 6, 14, 12, 29, 4, time);
        }
    }

    @Test
    public void testGetProductSize() throws IOException, InvalidRangeException {
        final File file = createSSMT2_F14_File();

        try (SSMT2_Reader r = reader) {
            r.open(file);

            final Dimension productSize = r.getProductSize();
            assertEquals(28, productSize.getNx());
            assertEquals(763, productSize.getNy());
        }
    }

    @Test
    public void testReadFrom1dVariable() throws IOException, InvalidRangeException {
        final File file = createSSMT2_F14_File();

        try (SSMT2_Reader r = reader) {
            r.open(file);
            final Array array = r.readRaw(2, 197, new Interval(5, 5), "thermal_reference");
            assertNotNull(array);
            assertTrue(array instanceof ArrayFloat.D2);
            final ArrayFloat.D2 d2 = (ArrayFloat.D2) array;
            float[] f = (float[]) d2.get1DJavaArray(Float.class);
            final float[] expected = new float[]{
                    0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0,
                    2575, 2575, 2575, 2575, 2575,
                    2575, 2575, 2575, 2575, 2575,
                    2575, 2575, 2575, 2575, 2575
            };
            assertArrayEquals(f, expected, 1e-8f);
        }
    }

    @Test
    public void testReadFrom2dVariable() throws IOException, InvalidRangeException {
        final File file = createSSMT2_F14_File();

        try (SSMT2_Reader r = reader) {
            r.open(file);
            final Array array = r.readRaw(2, 2, new Interval(5, 5), "lon");
            assertNotNull(array);
            assertTrue(array instanceof ArrayFloat.D2);
            final ArrayFloat.D2 d2 = (ArrayFloat.D2) array;
            float[] f = (float[]) d2.get1DJavaArray(Float.class);
            final float[] expected = new float[]{
                    128.38298f, 127.622314f, 126.94211f, 126.32476f, 125.757286f,
                    128.2796f, 127.518654f, 126.83821f, 126.22068f, 125.65306f,
                    128.1766f, 127.41533f, 126.73462f, 126.11687f, 125.54906f,
                    128.07399f, 127.31234f, 126.63133f, 126.01331f, 125.44529f,
                    127.97173f, 127.20968f, 126.52832f, 125.91001f, 125.34173f
            };
            assertArrayEquals(f, expected, 1e-8f);
        }
    }

    @Test
    public void testRead1dFrom2dVariable_with_channel() throws IOException, InvalidRangeException {
        final File file = createSSMT2_F14_File();

        try (SSMT2_Reader r = reader) {
            r.open(file);
            final Array array = r.readRaw(0, 4, new Interval(5, 5), "counts_to_tb_gain_ch2");
            assertNotNull(array);
            assertTrue(array instanceof ArrayFloat.D2);
            final ArrayFloat.D2 d2 = (ArrayFloat.D2) array;
            float[] f = (float[]) d2.get1DJavaArray(Float.class);
            final float[] expected = new float[]{
                    0.12364213f, 0.12364213f, 0.12364213f, 0.12364213f, 0.12364213f,
                    0.12347869f, 0.12347869f, 0.12347869f, 0.12347869f, 0.12347869f,
                    0.123529024f, 0.123529024f, 0.123529024f, 0.123529024f, 0.123529024f,
                    0.12290908f, 0.12290908f, 0.12290908f, 0.12290908f, 0.12290908f,
                    0.12332391f, 0.12332391f, 0.12332391f, 0.12332391f, 0.12332391f
            };
            assertArrayEquals(f, expected, 1e-8f);
        }
    }

    @Test
    public void testRead2dFrom3dVariable_with_channel() throws IOException, InvalidRangeException {
        final File file = createSSMT2_F14_File();

        try (SSMT2_Reader r = reader) {
            r.open(file);
            final Array array = r.readRaw(4, 4, new Interval(5, 5), "tb_ch3");
            assertNotNull(array);
            assertTrue(array instanceof ArrayFloat.D2);
            final ArrayFloat.D2 d2 = (ArrayFloat.D2) array;
            float[] f = (float[]) d2.get1DJavaArray(Float.class);
            final float[] expected = new float[]{
                    267.4799f, 267.72684f, 268.34415f, 268.71454f, 268.59106f,
                    268.293f, 268.293f, 270.27148f, 270.5188f, 270.5188f,
                    268.7834f, 268.04242f, 270.01834f, 270.01834f, 269.77136f,
                    267.58524f, 267.58524f, 268.69617f, 268.94305f, 268.4493f,
                    268.0838f, 267.8365f, 268.20746f, 268.20746f, 268.82574f
            };
            assertArrayEquals(f, expected, 1e-8f);
        }
    }

    @Test
    public void testRead1dFrom3dVariable_with_channel_and_calibrations() throws IOException, InvalidRangeException {
        final File file = createSSMT2_F14_File();

        try (SSMT2_Reader r = reader) {
            r.open(file);
            final Array array = r.readRaw(4, 4, new Interval(5, 5), "warm_counts_ch3_cal3");
            assertNotNull(array);
            assertTrue(array instanceof ArrayFloat.D2);
            final ArrayFloat.D2 d2 = (ArrayFloat.D2) array;
            float[] f = (float[]) d2.get1DJavaArray(Float.class);
            final float[] expected = new float[]{
                    3532.0f, 3532.0f, 3532.0f, 3532.0f, 3532.0f,
                    3527.0f, 3527.0f, 3527.0f, 3527.0f, 3527.0f,
                    3526.0f, 3526.0f, 3526.0f, 3526.0f, 3526.0f,
                    3543.0f, 3543.0f, 3543.0f, 3543.0f, 3543.0f,
                    3534.0f, 3534.0f, 3534.0f, 3534.0f, 3534.0f
            };
            assertArrayEquals(f, expected, 1e-8f);
        }
    }

    @Test
    public void testGetVariables() throws Exception {
        final File file = createSSMT2_F11_File();

        try (SSMT2_Reader r = reader) {
            r.open(file);
            final List<Variable> variables = r.getVariables();

            assertThat(variables, is(not(nullValue())));
            assertThat(variables.size(), is(97));

            Variable variable;

            variable = variables.get(0);
            assertThat(variable.getShortName(), equalTo("ancil_data_Year_1"));
            assertThat(variable.getDataType(), equalTo(DataType.DOUBLE));

            variable = variables.get(1);
            assertThat(variable.getShortName(), equalTo("ancil_data_DayofYear_1"));
            assertThat(variable.getDataType(), equalTo(DataType.DOUBLE));

            variable = variables.get(2);
            assertThat(variable.getShortName(), equalTo("ancil_data_SecondsofDay_1"));
            assertThat(variable.getDataType(), equalTo(DataType.DOUBLE));

            variable = variables.get(3);
            assertThat(variable.getShortName(), equalTo("ancil_data_SatLat"));
            assertThat(variable.getDataType(), equalTo(DataType.DOUBLE));

            variable = variables.get(4);
            assertThat(variable.getShortName(), equalTo("ancil_data_SatLong"));
            assertThat(variable.getDataType(), equalTo(DataType.DOUBLE));

            variable = variables.get(5);
            assertThat(variable.getShortName(), equalTo("ancil_data_SatAlt"));
            assertThat(variable.getDataType(), equalTo(DataType.DOUBLE));

            variable = variables.get(6);
            assertThat(variable.getShortName(), equalTo("ancil_data_SatHeading"));
            assertThat(variable.getDataType(), equalTo(DataType.DOUBLE));

            variable = variables.get(7);
            assertThat(variable.getShortName(), equalTo("ancil_data_Year_2"));
            assertThat(variable.getDataType(), equalTo(DataType.DOUBLE));

            variable = variables.get(8);
            assertThat(variable.getShortName(), equalTo("ancil_data_DayofYear_2"));
            assertThat(variable.getDataType(), equalTo(DataType.DOUBLE));

            variable = variables.get(9);
            assertThat(variable.getShortName(), equalTo("ancil_data_SecondsofDay_2"));
            assertThat(variable.getDataType(), equalTo(DataType.DOUBLE));

            variable = variables.get(10);
            assertThat(variable.getShortName(), equalTo("tb_ch1"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(11);
            assertThat(variable.getShortName(), equalTo("tb_ch2"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(12);
            assertThat(variable.getShortName(), equalTo("tb_ch3"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(13);
            assertThat(variable.getShortName(), equalTo("tb_ch4"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(14);
            assertThat(variable.getShortName(), equalTo("tb_ch5"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(15);
            assertThat(variable.getShortName(), equalTo("lon"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(16);
            assertThat(variable.getShortName(), equalTo("lat"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(17);
            assertThat(variable.getShortName(), equalTo("channel_quality_flag_ch1"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(18);
            assertThat(variable.getShortName(), equalTo("channel_quality_flag_ch2"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(19);
            assertThat(variable.getShortName(), equalTo("channel_quality_flag_ch3"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(20);
            assertThat(variable.getShortName(), equalTo("channel_quality_flag_ch4"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(21);
            assertThat(variable.getShortName(), equalTo("channel_quality_flag_ch5"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(22);
            assertThat(variable.getShortName(), equalTo("gain_control_ch1"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(23);
            assertThat(variable.getShortName(), equalTo("gain_control_ch2"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(24);
            assertThat(variable.getShortName(), equalTo("gain_control_ch3"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(25);
            assertThat(variable.getShortName(), equalTo("gain_control_ch4"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(26);
            assertThat(variable.getShortName(), equalTo("gain_control_ch5"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(27);
            assertThat(variable.getShortName(), equalTo("counts_to_tb_gain_ch1"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(28);
            assertThat(variable.getShortName(), equalTo("counts_to_tb_gain_ch2"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(29);
            assertThat(variable.getShortName(), equalTo("counts_to_tb_gain_ch3"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(30);
            assertThat(variable.getShortName(), equalTo("counts_to_tb_gain_ch4"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(31);
            assertThat(variable.getShortName(), equalTo("counts_to_tb_gain_ch5"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(32);
            assertThat(variable.getShortName(), equalTo("counts_to_tb_offset_ch1"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(33);
            assertThat(variable.getShortName(), equalTo("counts_to_tb_offset_ch2"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(34);
            assertThat(variable.getShortName(), equalTo("counts_to_tb_offset_ch3"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(35);
            assertThat(variable.getShortName(), equalTo("counts_to_tb_offset_ch4"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(36);
            assertThat(variable.getShortName(), equalTo("counts_to_tb_offset_ch5"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(37);
            assertThat(variable.getShortName(), equalTo("thermal_reference"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(38);
            assertThat(variable.getShortName(), equalTo("Temperature_misc_housekeeping_thermistorcount01"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(39);
            assertThat(variable.getShortName(), equalTo("Temperature_misc_housekeeping_thermistorcount02"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(40);
            assertThat(variable.getShortName(), equalTo("Temperature_misc_housekeeping_thermistorcount03"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(41);
            assertThat(variable.getShortName(), equalTo("Temperature_misc_housekeeping_thermistorcount04"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(42);
            assertThat(variable.getShortName(), equalTo("Temperature_misc_housekeeping_thermistorcount05"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(43);
            assertThat(variable.getShortName(), equalTo("Temperature_misc_housekeeping_thermistorcount06"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(44);
            assertThat(variable.getShortName(), equalTo("Temperature_misc_housekeeping_thermistorcount07"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(45);
            assertThat(variable.getShortName(), equalTo("Temperature_misc_housekeeping_thermistorcount08"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(46);
            assertThat(variable.getShortName(), equalTo("Temperature_misc_housekeeping_thermistorcount09"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(47);
            assertThat(variable.getShortName(), equalTo("Temperature_misc_housekeeping_thermistorcount10"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(48);
            assertThat(variable.getShortName(), equalTo("Temperature_misc_housekeeping_thermistorcount11"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(49);
            assertThat(variable.getShortName(), equalTo("Temperature_misc_housekeeping_thermistorcount12"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(50);
            assertThat(variable.getShortName(), equalTo("Temperature_misc_housekeeping_thermistorcount13"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(51);
            assertThat(variable.getShortName(), equalTo("Temperature_misc_housekeeping_thermistorcount14"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(52);
            assertThat(variable.getShortName(), equalTo("Temperature_misc_housekeeping_thermistorcount15"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(53);
            assertThat(variable.getShortName(), equalTo("Temperature_misc_housekeeping_thermistorcount16"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(54);
            assertThat(variable.getShortName(), equalTo("Temperature_misc_housekeeping_thermistorcount17"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(55);
            assertThat(variable.getShortName(), equalTo("Temperature_misc_housekeeping_thermistorcount18"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(56);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch1_cal1"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(57);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch1_cal2"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(58);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch1_cal3"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(59);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch1_cal4"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(60);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch2_cal1"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(61);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch2_cal2"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(62);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch2_cal3"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(63);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch2_cal4"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(64);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch3_cal1"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(65);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch3_cal2"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(66);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch3_cal3"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(67);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch3_cal4"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(68);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch4_cal1"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(69);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch4_cal2"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(70);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch4_cal3"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(71);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch4_cal4"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(72);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch5_cal1"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(73);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch5_cal2"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(74);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch5_cal3"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(75);
            assertThat(variable.getShortName(), equalTo("warm_counts_ch5_cal4"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(76);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch1_cal1"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(77);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch1_cal2"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(78);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch1_cal3"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(79);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch1_cal4"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(80);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch2_cal1"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(81);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch2_cal2"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(82);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch2_cal3"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(83);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch2_cal4"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(84);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch3_cal1"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(85);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch3_cal2"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(86);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch3_cal3"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(87);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch3_cal4"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(88);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch4_cal1"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(89);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch4_cal2"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(90);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch4_cal3"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(91);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch4_cal4"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(92);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch5_cal1"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(93);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch5_cal2"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(94);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch5_cal3"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(95);
            assertThat(variable.getShortName(), equalTo("cold_counts_ch5_cal4"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));

            variable = variables.get(96);
            assertThat(variable.getShortName(), equalTo("Satellite_zenith_angle"));
            assertThat(variable.getDataType(), equalTo(DataType.FLOAT));
        }
    }

    @Test
    public void testGetPixelLocator() throws Exception {
        final File file = createSSMT2_F14_File();

        try (SSMT2_Reader r = reader) {
            r.open(file);

            final PixelLocator pixelLocator = r.getPixelLocator();

            assertNotNull(pixelLocator);

            final Point2D geoLocation = pixelLocator.getGeoLocation(2.5, 4.5, null);
            final double lonVal = geoLocation.getX();
            final double latVal = geoLocation.getY();
            assertEquals(126.52832f, lonVal, 1e-128);
            assertEquals(3.2110612f, latVal, 1e-128);

            final Point2D[] pixelLocation = pixelLocator.getPixelLocation(lonVal, latVal);
            assertNotNull(pixelLocation);
            assertEquals(1, pixelLocation.length);
            assertEquals(2.5, pixelLocation[0].getX(), 0.2);
            assertEquals(4.5, pixelLocation[0].getY(), 0.1);
        }
    }

    @Test
    public void testNewObjectsAfterClose() throws Exception {
        final File file = createSSMT2_F14_File();

        SSMT2_Reader r = reader;
        try {
            r.open(file);

            final PixelLocator pixelLocator1 = r.getPixelLocator();
            assertNotNull(pixelLocator1);
            final TimeLocator timeLocator1 = r.getTimeLocator();
            assertNotNull(timeLocator1);
            final List<Variable> variables1 = r.getVariables();
            assertNotNull(variables1);
            final HashMap<String, WindowReader> readersMap1 = r.getReadersMap();
            assertNotNull(readersMap1);

            r.close();
            r.open(file);

            final PixelLocator pixelLocator2 = r.getPixelLocator();
            assertNotNull(pixelLocator2);
            final TimeLocator timeLocator2 = r.getTimeLocator();
            assertNotNull(timeLocator2);
            final List<Variable> variables2 = r.getVariables();
            assertNotNull(variables2);
            final HashMap<String, WindowReader> readersMap2 = r.getReadersMap();
            assertNotNull(readersMap2);

            assertNotSame(pixelLocator1, pixelLocator2);
            assertNotSame(timeLocator1, timeLocator2);
            assertNotSame(variables1, variables2);
            assertNotSame(readersMap1, readersMap2);
        } finally {
            r.close();
        }
    }

    @Test
    public void testGetAcquisitionTime() throws Exception {
        final File file = createSSMT2_F14_File();
        final Interval win5_5 = new Interval(5, 5);
        final int fillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();

        try (SSMT2_Reader r = reader) {
            r.open(file);
            final Dimension productSize = r.getProductSize();
            final int width = productSize.getNx();
            final int height = productSize.getNy();
            final int minX = 0;
            final int minY = 0;
            final int maxX = width - 1;
            final int maxY = height - 1;

            ArrayInt.D2 times;
            int[] storage;
            int[] expecteds;

            // at the upper left border
            times = r.readAcquisitionTime(minX, minY, win5_5);
            storage = (int[]) times.getStorage();
            expecteds = new int[]{
                    fillValue, fillValue, fillValue, fillValue, fillValue,
                    fillValue, fillValue, fillValue, fillValue, fillValue,
                    fillValue, fillValue, 992521749, 992521749, 992521749,
                    fillValue, fillValue, 992521757, 992521757, 992521757,
                    fillValue, fillValue, 992521765, 992521765, 992521765
            };
            assertArrayEquals(expecteds, storage);

            // at the upper right border
            times = r.readAcquisitionTime(maxX, minY, win5_5);
            storage = (int[]) times.getStorage();
            expecteds = new int[]{
                    fillValue, fillValue, fillValue, fillValue, fillValue,
                    fillValue, fillValue, fillValue, fillValue, fillValue,
                    992521749, 992521749, 992521749, fillValue, fillValue,
                    992521757, 992521757, 992521757, fillValue, fillValue,
                    992521765, 992521765, 992521765, fillValue, fillValue
            };
            assertArrayEquals(expecteds, storage);

            // at the lower right border
            times = r.readAcquisitionTime(maxX, maxY, win5_5);
            storage = (int[]) times.getStorage();
            expecteds = new int[]{
                    992527835, 992527835, 992527835, fillValue, fillValue,
                    992527843, 992527843, 992527843, fillValue, fillValue,
                    992527851, 992527851, 992527851, fillValue, fillValue,
                    fillValue, fillValue, fillValue, fillValue, fillValue,
                    fillValue, fillValue, fillValue, fillValue, fillValue
            };
            assertArrayEquals(expecteds, storage);

            // at the lower left border
            times = r.readAcquisitionTime(minX, maxY, win5_5);
            storage = (int[]) times.getStorage();
            expecteds = new int[]{
                    fillValue, fillValue, 992527835, 992527835, 992527835,
                    fillValue, fillValue, 992527843, 992527843, 992527843,
                    fillValue, fillValue, 992527851, 992527851, 992527851,
                    fillValue, fillValue, fillValue, fillValue, fillValue,
                    fillValue, fillValue, fillValue, fillValue, fillValue
            };
            assertArrayEquals(expecteds, storage);

            // entirely inside of the product
            times = r.readAcquisitionTime(9, 11, win5_5);
            storage = (int[]) times.getStorage();
            expecteds = new int[]{
                    992521822, 992521822, 992521822, 992521822, 992521822,
                    992521830, 992521830, 992521830, 992521830, 992521830,
                    992521838, 992521838, 992521838, 992521838, 992521838,
                    992521846, 992521846, 992521846, 992521846, 992521846,
                    992521854, 992521854, 992521854, 992521854, 992521854
            };
            assertArrayEquals(expecteds, storage);
        }
    }

    @Test
    public void testReadRaw_zenithAngle() throws IOException, InvalidRangeException {
        final File file = createSSMT2_F14_File();
        final Interval interval = new Interval(3, 3);

        try (SSMT2_Reader r = reader) {
            r.open(file);

            Array array = r.readRaw(5, 178, interval, "Satellite_zenith_angle");
            NCTestUtils.assertValueAt(32.75, 0, 0, array);
            NCTestUtils.assertValueAt(29.209999084472656, 1, 1, array);
            NCTestUtils.assertValueAt(25.709999084472656, 2, 2, array);


            array = r.readRaw(27, 179, interval, "Satellite_zenith_angle");
            NCTestUtils.assertValueAt(43.63999938964844, 0, 0, array);
            NCTestUtils.assertValueAt(47.41999816894531, 1, 1, array);
            NCTestUtils.assertValueAt(9.969209968386869E36, 2, 2, array);
        }
    }

    private File createSSMT2_F11_File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"ssmt2-f11", "v01", "1994", "01", "28", "F11199401280412.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }

    private File createSSMT2_F14_File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"ssmt2-f14", "v01", "2001", "06", "14", "F14200106141229.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}

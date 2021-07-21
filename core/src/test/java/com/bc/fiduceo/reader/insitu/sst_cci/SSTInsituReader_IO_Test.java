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

package com.bc.fiduceo.reader.insitu.sst_cci;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.time.TimeLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


@RunWith(IOTestRunner.class)
public class SSTInsituReader_IO_Test {

    private static final Interval _3x3 = new Interval(3, 3);

    private SSTInsituReader insituReader;

    @Before
    public void setUp() throws Exception {
        insituReader = new SSTInsituReader();
    }

    @After
    public void tearDown() throws Exception {
        insituReader.close();
    }

    @Test
    public void testReadAcquisitionInfo_drifter_v33() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_51993_20040402_20060207.nc", "v03.3");

        final AcquisitionInfo info = insituReader.read();
        assertNotNull(info);

        TestUtil.assertCorrectUTCDate(2004, 4, 2, 18, 43, 47, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2006, 2, 7, 5, 17, 59, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    @Test
    public void testReadAcquisitionInfo_drifter_v40() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_42531_19960904_19960909.nc", "v04.0");

        final AcquisitionInfo info = insituReader.read();
        assertNotNull(info);

        TestUtil.assertCorrectUTCDate(1996, 9, 4, 22, 0, 0, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(1996, 9, 9, 13, 19, 47, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    @Test
    public void testReadAcquisitionInfo_ship_v33() throws Exception {
        openFile("ship-sst", "insitu_2_WMOID_DBBH_19780118_20151025.nc", "v03.3");

        final AcquisitionInfo info = insituReader.read();
        assertNotNull(info);

        TestUtil.assertCorrectUTCDate(1978, 1, 18, 9, 0, 0, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2015, 10, 25, 11, 0, 0, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    @Test
    public void testReadAcquisitionInfo_gtmba_v33() throws Exception {
        openFile("gtmba-sst", "insitu_3_WMOID_51019_19910722_20120610.nc", "v03.3");

        final AcquisitionInfo info = insituReader.read();
        assertNotNull(info);

        TestUtil.assertCorrectUTCDate(1991, 7, 22, 16, 0, 0, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2012, 6, 10, 0, 10, 0, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    @Test
    public void testReadAcquisitionInfo_gtmba_v40() throws Exception {
        openFile("gtmba-sst", "insitu_3_WMOID_31007_19990306_20160920.nc", "v04.0");

        final AcquisitionInfo info = insituReader.read();
        assertNotNull(info);

        TestUtil.assertCorrectUTCDate(1999, 3, 6, 14, 0, 0, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2016, 9, 20, 12, 0, 0, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    @Test
    public void testGetVariables_drifter_v33() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_46942_19951026_19951027.nc", "v03.3");
        final List<Variable> variables = insituReader.getVariables();

        assertNotNull(variables);
        assertEquals(10, variables.size());
        assertEquals("insitu.time", variables.get(0).getShortName());
        assertEquals("insitu.lat", variables.get(1).getShortName());
        assertEquals("insitu.lon", variables.get(2).getShortName());
        assertEquals("insitu.sea_surface_temperature", variables.get(3).getShortName());
        assertEquals("insitu.sst_uncertainty", variables.get(4).getShortName());
        assertEquals("insitu.sst_depth", variables.get(5).getShortName());
        assertEquals("insitu.sst_qc_flag", variables.get(6).getShortName());
        assertEquals("insitu.sst_track_flag", variables.get(7).getShortName());
        assertEquals("insitu.mohc_id", variables.get(8).getShortName());
        assertEquals("insitu.id", variables.get(9).getShortName());
    }

    @Test
    public void testGetVariables_drifter_v40() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_42531_19960904_19960909.nc", "v04.0");
        final List<Variable> variables = insituReader.getVariables();

        assertNotNull(variables);
        assertEquals(20, variables.size());
        assertEquals("insitu.time", variables.get(0).getShortName());
        assertEquals("insitu.lat", variables.get(1).getShortName());
        assertEquals("insitu.lon", variables.get(2).getShortName());
        assertEquals("insitu.sea_surface_temperature", variables.get(3).getShortName());
        assertEquals("insitu.sst_uncertainty", variables.get(4).getShortName());
        assertEquals("insitu.sst_random_uncertainty", variables.get(5).getShortName());
        assertEquals("insitu.sst_depth", variables.get(6).getShortName());
        assertEquals("insitu.sst_depth_corr", variables.get(7).getShortName());
        assertEquals("insitu.mohc_id", variables.get(8).getShortName());
        assertEquals("insitu.collection", variables.get(9).getShortName());
        assertEquals("insitu.subcol1", variables.get(10).getShortName());
        assertEquals("insitu.subcol2", variables.get(11).getShortName());
        assertEquals("insitu.prof_id", variables.get(12).getShortName());
        assertEquals("insitu.sst_type_corr", variables.get(13).getShortName());
        assertEquals("insitu.sst_type_corr_unc", variables.get(14).getShortName());
        assertEquals("insitu.sst_plat_corr", variables.get(15).getShortName());
        assertEquals("insitu.sst_plat_corr_unc", variables.get(16).getShortName());
        assertEquals("insitu.qc1", variables.get(17).getShortName());
        assertEquals("insitu.qc2", variables.get(18).getShortName());
        assertEquals("insitu.id", variables.get(19).getShortName());
    }

    @Test
    public void testGetVariables_ship() throws Exception {
        openFile("ship-sst", "insitu_2_WMOID_DBBH_19780118_20151025.nc", "v03.3");
        final List<Variable> variables = insituReader.getVariables();

        assertNotNull(variables);
        assertEquals(10, variables.size());
        assertEquals("insitu.time", variables.get(0).getShortName());
        assertEquals("insitu.lat", variables.get(1).getShortName());
        assertEquals("insitu.lon", variables.get(2).getShortName());
        assertEquals("insitu.sea_surface_temperature", variables.get(3).getShortName());
        assertEquals("insitu.sst_uncertainty", variables.get(4).getShortName());
        assertEquals("insitu.sst_depth", variables.get(5).getShortName());
        assertEquals("insitu.sst_qc_flag", variables.get(6).getShortName());
        assertEquals("insitu.sst_track_flag", variables.get(7).getShortName());
        assertEquals("insitu.mohc_id", variables.get(8).getShortName());
        assertEquals("insitu.id", variables.get(9).getShortName());
    }

    @Test
    public void testGetSourceArray_v33() throws IOException {
        openFile("ship-sst", "insitu_2_WMOID_DBBH_19780118_20151025.nc", "v03.3");

        final Array array = insituReader.getSourceArray("insitu.sst_track_flag");
        assertNotNull(array);
        assertEquals(136607, array.getSize());
        assertEquals(3, array.getInt(0));
        assertEquals(1, array.getInt(22987));
        assertEquals(3, array.getInt(136606));
    }

    @Test
    public void testGetSourceArray_v40() throws IOException {
        openFile("gtmba-sst", "insitu_3_WMOID_31007_19990306_20160920.nc", "v04.0");

        final Array array = insituReader.getSourceArray("insitu.sst_type_corr_unc");
        assertNotNull(array);
        assertEquals(663513, array.getSize());
        assertEquals(-9999, array.getInt(0));
        assertEquals(-9999, array.getInt(315664));
        assertEquals(-9999, array.getInt(663512));
    }

    @Test
    public void testInsituType_drifter_v33() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_51993_20040402_20060207.nc", "v03.3");
        assertEquals("drifter", insituReader.getInsituType());
    }

    @Test
    public void testInsituType_drifter_v40() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_42531_19960904_19960909.nc", "v04.0");
        assertEquals("drifter", insituReader.getInsituType());
    }

    @Test
    public void testInsituType_ship_v33() throws Exception {
        openFile("ship-sst", "insitu_2_WMOID_DBBH_19780118_20151025.nc", "v03.3");
        assertEquals("ship", insituReader.getInsituType());
    }

    @Test
    public void testGetTime_drifter_v33() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_51993_20040402_20060207.nc", "v03.3");
        final int numObservations = insituReader.getNumObservations();

        assertEquals(8969, numObservations);
        assertEquals(828470627, insituReader.getTime(0));
        assertEquals(828701820, insituReader.getTime(24));
        assertEquals(886828679, insituReader.getTime(numObservations - 1));

        try {
            insituReader.getTime(numObservations);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void testGetTime_drifter_v40() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_42531_19960904_19960909.nc", "v04.0");
        final int numObservations = insituReader.getNumObservations();

        assertEquals(90, numObservations);
        assertEquals(589413600, insituReader.getTime(0));
        assertEquals(589639212, insituReader.getTime(44));
        assertEquals(589814387, insituReader.getTime(numObservations - 1));

        try {
            insituReader.getTime(numObservations);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void testGetTime_gtmba_v33() throws Exception {
        openFile("gtmba-sst", "insitu_3_WMOID_51019_19910722_20120610.nc", "v03.3");
        final int numObservations = insituReader.getNumObservations();

        assertEquals(590706, numObservations);
        assertEquals(427737600, insituReader.getTime(0));
        assertEquals(847325400, insituReader.getTime(250656));
        assertEquals(1086826200, insituReader.getTime(numObservations - 1));
        try {
            insituReader.getTime(numObservations);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void testGetTime_gtmba_v40() throws Exception {
        openFile("gtmba-sst", "insitu_3_WMOID_31007_19990306_20160920.nc", "v04.0");
        final int numObservations = insituReader.getNumObservations();

        assertEquals(663513, numObservations);
        assertEquals(668268000, insituReader.getTime(0));
        assertEquals(818933400, insituReader.getTime(250656));
        assertEquals(1221912000, insituReader.getTime(numObservations - 1));
        try {
            insituReader.getTime(numObservations);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void testReadRaw_0_v33() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_46942_19951026_19951027.nc", "v03.3");
        final Array array = insituReader.readRaw(0, 0, new Interval(1, 1), "insitu.lat");

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(41.7f, array.getFloat(0), 1e-8);
    }

    @Test
    public void testReadRaw_0_v40() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_42531_19960904_19960909.nc", "v04.0");
        final Array array = insituReader.readRaw(0, 0, new Interval(1, 1), "insitu.lat");

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(30.58f, array.getFloat(0), 1e-8);
    }

    @Test
    public void testReadRaw_1_v33() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_46942_19951026_19951027.nc", "v03.3");
        final Array array = insituReader.readRaw(0, 1, new Interval(1, 1), "insitu.lat");

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(41.7f, array.getFloat(0), 0);
    }

    @Test
    public void testReadRaw_1_v40() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_42531_19960904_19960909.nc", "v04.0");
        final Array array = insituReader.readRaw(0, 1, new Interval(1, 1), "insitu.lat");

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(30.58f, array.getFloat(0), 0);
    }

    @Test
    public void testReadRaw_1_3x3_v33() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_46942_19951026_19951027.nc", "v03.3");
        final Array array = insituReader.readRaw(0, 1, _3x3, "insitu.lat");

        assertNotNull(array);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());

        NCTestUtils.assertValueAt(-32768, 0, 0, array);
        NCTestUtils.assertValueAt(-32768, 1, 0, array);
        NCTestUtils.assertValueAt(-32768, 2, 0, array);
        NCTestUtils.assertValueAt(-32768, 0, 1, array);
        NCTestUtils.assertValueAt(41.70000076293945, 1, 1, array);
        NCTestUtils.assertValueAt(-32768, 2, 1, array);
        NCTestUtils.assertValueAt(-32768, 0, 2, array);
        NCTestUtils.assertValueAt(-32768, 1, 2, array);
        NCTestUtils.assertValueAt(-32768, 2, 2, array);
    }

    @Test
    public void testReadRaw_1_3x3_v40() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_42531_19960904_19960909.nc", "v04.0");
        final Array array = insituReader.readRaw(0, 1, _3x3, "insitu.lat");

        assertNotNull(array);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());

        NCTestUtils.assertValueAt(-9999, 0, 0, array);
        NCTestUtils.assertValueAt(-9999, 1, 0, array);
        NCTestUtils.assertValueAt(-9999, 2, 0, array);
        NCTestUtils.assertValueAt(-9999, 0, 1, array);
        NCTestUtils.assertValueAt(30.579999923706055, 1, 1, array);
        NCTestUtils.assertValueAt(-9999, 2, 1, array);
        NCTestUtils.assertValueAt(-9999, 0, 2, array);
        NCTestUtils.assertValueAt(-9999, 1, 2, array);
        NCTestUtils.assertValueAt(-9999, 2, 2, array);
    }

    @Test
    public void testReadScaled_1_3x3_drifter_v33() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_51993_20040402_20060207.nc", "v03.3");
        final Array array = insituReader.readScaled(0, 24, _3x3, "insitu.sea_surface_temperature");

        assertNotNull(array);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());

        NCTestUtils.assertValueAt(-32768, 0, 0, array);
        NCTestUtils.assertValueAt(-32768, 1, 0, array);
        NCTestUtils.assertValueAt(-32768, 2, 0, array);
        NCTestUtils.assertValueAt(-32768, 0, 1, array);
        NCTestUtils.assertValueAt(27.399999618530273, 1, 1, array);
        NCTestUtils.assertValueAt(-32768, 2, 1, array);
        NCTestUtils.assertValueAt(-32768, 0, 2, array);
        NCTestUtils.assertValueAt(-32768, 1, 2, array);
        NCTestUtils.assertValueAt(-32768, 2, 2, array);
    }

    @Test
    public void testReadScaled_1_3x3_drifter_v40() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_42531_19960904_19960909.nc", "v04.0");
        final Array array = insituReader.readScaled(0, 24, _3x3, "insitu.sea_surface_temperature");

        assertNotNull(array);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());

        NCTestUtils.assertValueAt(-9999, 0, 0, array);
        NCTestUtils.assertValueAt(-9999, 1, 0, array);
        NCTestUtils.assertValueAt(-9999, 2, 0, array);
        NCTestUtils.assertValueAt(-9999, 0, 1, array);
        NCTestUtils.assertValueAt(27.600000381469727, 1, 1, array);
        NCTestUtils.assertValueAt(-9999, 2, 1, array);
        NCTestUtils.assertValueAt(-9999, 0, 2, array);
        NCTestUtils.assertValueAt(-9999, 1, 2, array);
        NCTestUtils.assertValueAt(-9999, 2, 2, array);
    }

    @Test
    public void testReadRaw_Id_v33() throws Exception {
        openFile("gtmba-sst", "insitu_3_WMOID_51019_19910722_20120610.nc", "v03.3");
        final Array idArray = insituReader.readRaw(0, 11, _3x3, "insitu.id");

        assertNotNull(idArray);
        assertEquals(DataType.LONG, idArray.getDataType());

        NCTestUtils.assertValueAt(-32768, 0, 0, idArray);
        NCTestUtils.assertValueAt(-32768, 1, 0, idArray);
        NCTestUtils.assertValueAt(-32768, 2, 0, idArray);
        NCTestUtils.assertValueAt(-32768, 0, 1, idArray);
        NCTestUtils.assertValueAt(1991070000000011L, 1, 1, idArray);
        NCTestUtils.assertValueAt(-32768, 2, 1, idArray);
        NCTestUtils.assertValueAt(-32768, 0, 2, idArray);
        NCTestUtils.assertValueAt(-32768, 1, 2, idArray);
        NCTestUtils.assertValueAt(-32768, 2, 2, idArray);
    }

    @Test
    public void testReadRaw_Id_v40() throws Exception {
        openFile("gtmba-sst", "insitu_3_WMOID_31007_19990306_20160920.nc", "v04.0");
        final Array idArray = insituReader.readRaw(0, 11, _3x3, "insitu.id");

        assertNotNull(idArray);
        assertEquals(DataType.LONG, idArray.getDataType());

        NCTestUtils.assertValueAt(-9999, 0, 0, idArray);
        NCTestUtils.assertValueAt(-9999, 1, 0, idArray);
        NCTestUtils.assertValueAt(-9999, 2, 0, idArray);
        NCTestUtils.assertValueAt(-9999, 0, 1, idArray);
        NCTestUtils.assertValueAt(1999030002317407L, 1, 1, idArray);
        NCTestUtils.assertValueAt(-9999, 2, 1, idArray);
        NCTestUtils.assertValueAt(-9999, 0, 2, idArray);
        NCTestUtils.assertValueAt(-9999, 1, 2, idArray);
        NCTestUtils.assertValueAt(-9999, 2, 2, idArray);
    }

    @Test
    public void testReadAcquisitionTime_v33() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_51993_20040402_20060207.nc", "v03.3");
        final ArrayInt.D2 array = insituReader.readAcquisitionTime(0, 3, _3x3);

        assertNotNull(array);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(DataType.INT, array.getDataType());

        NCTestUtils.assertValueAt(-2147483647, 0, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 1, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 0, 1, array);
        NCTestUtils.assertValueAt(1080945900, 1, 1, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 1, array);
        NCTestUtils.assertValueAt(-2147483647, 0, 2, array);
        NCTestUtils.assertValueAt(-2147483647, 1, 2, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 2, array);
    }

    @Test
    public void testReadAcquisitionTime_v40() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_42531_19960904_19960909.nc", "v04.0");
        final ArrayInt.D2 array = insituReader.readAcquisitionTime(0, 3, _3x3);

        assertNotNull(array);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(DataType.INT, array.getDataType());

        NCTestUtils.assertValueAt(-2147483647, 0, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 1, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 0, 1, array);
        NCTestUtils.assertValueAt(841914000, 1, 1, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 1, array);
        NCTestUtils.assertValueAt(-2147483647, 0, 2, array);
        NCTestUtils.assertValueAt(-2147483647, 1, 2, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 2, array);
    }

    @Test
    public void testGetProductSize_drifter_v33() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_51993_20040402_20060207.nc", "v03.3");
        final Dimension productSize = insituReader.getProductSize();

        assertNotNull(productSize);
        assertEquals("product_size", productSize.getName());
        assertEquals(1, productSize.getNx());
        assertEquals(8969, productSize.getNy());
    }

    @Test
    public void testGetProductSize_drifter_v40() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_42531_19960904_19960909.nc", "v04.0");
        final Dimension productSize = insituReader.getProductSize();

        assertNotNull(productSize);
        assertEquals("product_size", productSize.getName());
        assertEquals(1, productSize.getNx());
        assertEquals(90, productSize.getNy());
    }

    @Test
    public void testGetTimeLocator_drifter_v33() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_51993_20040402_20060207.nc", "v03.3");
        final TimeLocator timeLocator = insituReader.getTimeLocator();

        assertNotNull(timeLocator);
        assertEquals(1080943451000L, timeLocator.getTimeFor(2, 2));
        assertEquals(1080945900000L, timeLocator.getTimeFor(2, 3));
        assertEquals(1080952091000L, timeLocator.getTimeFor(2, 4));
    }

    @Test
    public void testGetTimeLocator_drifter_v40() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_42531_19960904_19960909.nc", "v04.0");
        final TimeLocator timeLocator = insituReader.getTimeLocator();

        assertNotNull(timeLocator);
        assertEquals(841914000000L, timeLocator.getTimeFor(2, 3));
        assertEquals(841917600000L, timeLocator.getTimeFor(2, 4));
        assertEquals(841921200000L, timeLocator.getTimeFor(2, 5));
    }

    @Test
    public void testGetPixelLocator() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_46942_19951026_19951027.nc", "v03.3");

        try {
            insituReader.getPixelLocator();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetSubScenePixelLocator() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_46942_19951026_19951027.nc", "v03.3");
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Polygon polygon = geometryFactory.createPolygon(Arrays.asList(
                geometryFactory.createPoint(4, 5),
                geometryFactory.createPoint(5, 6),
                geometryFactory.createPoint(6, 5)
        ));

        try {
            insituReader.getSubScenePixelLocator(polygon);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    private void openFile(String dataType, String fileName, String version) throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", dataType, version, fileName}, false);
        final File insituDataFile = TestUtil.getTestDataFileAsserted(testFilePath);

        insituReader.open(insituDataFile);
    }
}
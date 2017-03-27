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
 */

package com.bc.fiduceo.reader.insitu;

import static org.junit.Assert.*;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.TimeLocator;
import org.junit.*;
import org.junit.runner.*;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@RunWith(IOTestRunner.class)
public class SSTInsituReader_IO_Test {

    private static final Interval _3x3 = new Interval(3, 3);

    private SSTInsituReader insituReader;
    private File testDataDirectory;

    @Before
    public void setUp() throws Exception {
        insituReader = new SSTInsituReader();
        testDataDirectory = TestUtil.getTestDataDirectory();
    }

    @After
    public void tearDown() throws Exception {
        insituReader.close();
    }

    @Test
    public void testReadAcquisitionInfo_drifter() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_51993_20040402_20060207.nc");

        final AcquisitionInfo info = insituReader.read();
        assertNotNull(info);

        TestUtil.assertCorrectUTCDate(2004, 4, 2, 18, 43, 47, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2006, 2, 7, 5, 17, 59, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    @Test
    public void testReadAcquisitionInfo_ship() throws Exception {
        openFile("ship-sst", "insitu_2_WMOID_DBBH_19780118_20151025.nc");

        final AcquisitionInfo info = insituReader.read();
        assertNotNull(info);

        TestUtil.assertCorrectUTCDate(1978, 1, 18, 9, 0, 0, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2015, 10, 25, 11, 0, 0, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    @Test
    public void testReadAcquisitionInfo_gtmba() throws Exception {
        openFile("gtmba-sst", "insitu_3_WMOID_51019_19910722_20120610.nc");

        final AcquisitionInfo info = insituReader.read();
        assertNotNull(info);

        TestUtil.assertCorrectUTCDate(1991, 7, 22, 16, 0, 0, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2012, 6, 10, 0, 10, 0, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    @Test
    public void testGetVariables_drifter() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_46942_19951026_19951027.nc");
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
    public void testGetVariables_ship() throws Exception {
        openFile("ship-sst", "insitu_2_WMOID_DBBH_19780118_20151025.nc");
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
    public void testGetSourceArray() throws IOException {
        openFile("ship-sst", "insitu_2_WMOID_DBBH_19780118_20151025.nc");

        final Array array = insituReader.getSourceArray("insitu.sst_track_flag");
        assertNotNull(array);
        assertEquals(3, array.getInt(0));
    }

    @Test
    public void testInsituType_drifter() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_51993_20040402_20060207.nc");
        assertEquals("drifter", insituReader.getInsituType());
    }

    @Test
    public void testInsituType_ship() throws Exception {
        openFile("ship-sst", "insitu_2_WMOID_DBBH_19780118_20151025.nc");
        assertEquals("ship", insituReader.getInsituType());
    }

    @Test
    public void testGetTime_drifter() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_51993_20040402_20060207.nc");
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
    public void testGetTime_gtmba() throws Exception {
        openFile("gtmba-sst", "insitu_3_WMOID_51019_19910722_20120610.nc");
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
    public void testReadRaw_0() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_46942_19951026_19951027.nc");
        final Array array = insituReader.readRaw(0, 0, new Interval(1, 1), "insitu.lat");

        assertNotNull(array);
        assertEquals(2, array.getShape().length);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(1, array.getSize());
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals("41.7", array.getObject(0).toString());
        assertEquals(41.7f, array.getFloat(0), 0);
    }

    @Test
    public void testReadRaw_1() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_46942_19951026_19951027.nc");
        final Array array = insituReader.readRaw(0, 1, new Interval(1, 1), "insitu.lat");

        assertNotNull(array);
        assertEquals(2, array.getShape().length);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(1, array.getSize());
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals("41.7", array.getObject(0).toString());
        assertEquals(41.7f, array.getFloat(0), 0);
    }

    @Test
    public void testReadRaw_1_3x3() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_46942_19951026_19951027.nc");
        final Array array = insituReader.readRaw(0, 1, _3x3, "insitu.lat");

        assertNotNull(array);
        assertEquals(2, array.getShape().length);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(9, array.getSize());
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals("-32768.0", array.getObject(0).toString());
        assertEquals("-32768.0", array.getObject(1).toString());
        assertEquals("-32768.0", array.getObject(2).toString());
        assertEquals("-32768.0", array.getObject(3).toString());
        assertEquals("41.7", array.getObject(4).toString());
        assertEquals("-32768.0", array.getObject(5).toString());
        assertEquals("-32768.0", array.getObject(6).toString());
        assertEquals("-32768.0", array.getObject(7).toString());
        assertEquals("-32768.0", array.getObject(8).toString());
    }

    @Test
    public void testReadScaled_1_3x3_drifter() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_51993_20040402_20060207.nc");
        final Array array = insituReader.readScaled(0, 24, _3x3, "insitu.sea_surface_temperature");

        assertNotNull(array);
        assertEquals(2, array.getShape().length);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(9, array.getSize());
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals("-32768.0", array.getObject(0).toString());
        assertEquals("-32768.0", array.getObject(1).toString());
        assertEquals("-32768.0", array.getObject(2).toString());
        assertEquals("-32768.0", array.getObject(3).toString());
        assertEquals("27.4", array.getObject(4).toString());
        assertEquals("-32768.0", array.getObject(5).toString());
        assertEquals("-32768.0", array.getObject(6).toString());
        assertEquals("-32768.0", array.getObject(7).toString());
        assertEquals("-32768.0", array.getObject(8).toString());
    }

    @Test
    public void testReadRaw_Id() throws Exception {
        openFile("gtmba-sst", "insitu_3_WMOID_51019_19910722_20120610.nc");
        final Array idArray = insituReader.readRaw(0, 11, _3x3, "insitu.id");

        assertNotNull(idArray);
        assertEquals(DataType.LONG, idArray.getDataType());
        assertEquals(-32768, idArray.getLong(0));
        assertEquals(-32768, idArray.getLong(1));
        assertEquals(-32768, idArray.getLong(2));
        assertEquals(-32768, idArray.getLong(3));
        assertEquals(1991070000000011L, idArray.getLong(4));
        assertEquals(-32768, idArray.getLong(5));
        assertEquals(-32768, idArray.getLong(6));
        assertEquals(-32768, idArray.getLong(7));
        assertEquals(-32768, idArray.getLong(8));
    }

    @Test
    public void testReadAcquisitionTime() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_51993_20040402_20060207.nc");
        final ArrayInt.D2 array = insituReader.readAcquisitionTime(0, 3, _3x3);

        assertNotNull(array);
        assertEquals(2, array.getShape().length);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(9, array.getSize());
        assertEquals(DataType.INT, array.getDataType());
        assertEquals("-32768", array.getObject(0).toString());
        assertEquals("-32768", array.getObject(1).toString());
        assertEquals("-32768", array.getObject(2).toString());
        assertEquals("-32768", array.getObject(3).toString());
        assertEquals("1080945900", array.getObject(4).toString());
        assertEquals("-32768", array.getObject(5).toString());
        assertEquals("-32768", array.getObject(6).toString());
        assertEquals("-32768", array.getObject(7).toString());
        assertEquals("-32768", array.getObject(8).toString());
    }

    @Test
    public void testGetProductSize_drifter() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_51993_20040402_20060207.nc");
        final Dimension productSize = insituReader.getProductSize();

        assertNotNull(productSize);
        assertEquals("product_size", productSize.getName());
        assertEquals(1, productSize.getNx());
        assertEquals(8969, productSize.getNy());
    }

    @Test
    public void testGetTimeLocator_drifter() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_51993_20040402_20060207.nc");
        final TimeLocator timeLocator = insituReader.getTimeLocator();

        assertNotNull(timeLocator);
        assertEquals(1080943451000L, timeLocator.getTimeFor(2, 2));
    }

    @Test
    public void testGetPixelLocator() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_46942_19951026_19951027.nc");

        try {
            insituReader.getPixelLocator();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }

    }

    @Test
    public void testGetSubScenePixelLocator() throws Exception {
        openFile("drifter-sst", "insitu_0_WMOID_46942_19951026_19951027.nc");
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

    private void openFile(String dataType, String fileName) throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", dataType, "v03.3", fileName}, false);
        final File insituDataFile = new File(testDataDirectory, testFilePath);
        assertTrue(insituDataFile.isFile());

        insituReader.open(insituDataFile);
    }
}
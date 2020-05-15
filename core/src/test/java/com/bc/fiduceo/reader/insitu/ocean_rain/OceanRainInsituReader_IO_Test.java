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

package com.bc.fiduceo.reader.insitu.ocean_rain;

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
import com.bc.fiduceo.util.NetCDFUtils;
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

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_STANDARD_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_UNITS_NAME;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class OceanRainInsituReader_IO_Test {

    private OceanRainInsituReader reader;

    @Before
    public void setUp() throws IOException {
        reader = new OceanRainInsituReader();

        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "ocean-rain-sst", "v1.0", "OceanRAIN_allships_2010-2017_SST.ascii"}, false);
        final File testFile = TestUtil.getTestDataFileAsserted(relativePath);
        reader.open(testFile);
    }

    @After
    public void tearDown() throws IOException {
        reader.close();
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final AcquisitionInfo info = reader.read();
        assertNotNull(info);

        TestUtil.assertCorrectUTCDate(2010, 6, 10, 21, 0, 0, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2016, 9, 16, 22, 59, 0, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    @Test
    public void testGetVariables() {
        final List<Variable> variables = reader.getVariables();

        assertNotNull(variables);
        assertEquals(4, variables.size());

        Variable variable = variables.get(0);
        assertEquals("lon", variable.getShortName());
        assertEquals("longitude", NetCDFUtils.getAttributeString(variable, CF_STANDARD_NAME, "default"));
        assertEquals("degree_east", NetCDFUtils.getAttributeString(variable, CF_UNITS_NAME, "default"));
        assertEquals(9.969209968386869E36f, NetCDFUtils.getAttributeFloat(variable, CF_FILL_VALUE_NAME, -0.8f), 1e-8);

        variable = variables.get(1);
        assertEquals("lat", variable.getShortName());
        assertEquals("latitude", NetCDFUtils.getAttributeString(variable, CF_STANDARD_NAME, "default"));
        assertEquals("degree_north", NetCDFUtils.getAttributeString(variable, CF_UNITS_NAME, "default"));
        assertEquals(9.969209968386869E36f, NetCDFUtils.getAttributeFloat(variable, CF_FILL_VALUE_NAME, -0.8f), 1e-8);

        variable = variables.get(2);
        assertEquals("time", variable.getShortName());
        assertEquals("time", NetCDFUtils.getAttributeString(variable, CF_STANDARD_NAME, "default"));
        assertEquals("Seconds since 1970-01-01", NetCDFUtils.getAttributeString(variable, CF_UNITS_NAME, "default"));
        assertEquals(-2147483647, NetCDFUtils.getAttributeInt(variable, CF_FILL_VALUE_NAME, 100));

        variable = variables.get(3);
        assertEquals("sst", variable.getShortName());
        assertEquals("sea_surface_temperature", NetCDFUtils.getAttributeString(variable, CF_STANDARD_NAME, "default"));
        assertEquals("celsius", NetCDFUtils.getAttributeString(variable, CF_UNITS_NAME, "default"));
        assertEquals(9.969209968386869E36f, NetCDFUtils.getAttributeFloat(variable, CF_FILL_VALUE_NAME, -0.8f), 1e-8);
    }

    @Test
    public void testReadRaw_1x1_lat() throws Exception {
        final Array array = reader.readRaw(0, 0, new Interval(1, 1), "lat");

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(53.5733f, array.getFloat(0), 1e-8);
    }

    @Test
    public void testReadRaw_1x1_lon() throws Exception {
        final Array array = reader.readRaw(1, 1, new Interval(1, 1), "lon");

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(8.55049991607666f, array.getFloat(0), 1e-8);
    }

    @Test
    public void testReadRaw_3x3_time() throws Exception {
        final Array array = reader.readRaw(2, 2, new Interval(3, 3), "time");

        assertNotNull(array);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(DataType.INT, array.getDataType());

        NCTestUtils.assertValueAt(-2147483647, 0, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 1, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 0, 1, array);
        NCTestUtils.assertValueAt(1276203720, 1, 1, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 1, array);
        NCTestUtils.assertValueAt(-2147483647, 0, 2, array);
        NCTestUtils.assertValueAt(-2147483647, 1, 2, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 2, array);
    }

    @Test
    public void testReadRaw_3x3_sst() throws Exception {
        final Array array = reader.readRaw(3, 3, new Interval(3, 3), "sst");

        assertNotNull(array);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());

        NCTestUtils.assertValueAt(9.969209968386869E36f, 0, 0, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 1, 0, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 2, 0, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 0, 1, array);
        NCTestUtils.assertValueAt(16.7f, 1, 1, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 2, 1, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 0, 2, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 1, 2, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 2, 2, array);
    }

    @Test
    public void testReadScaled_3x3_lon() throws Exception {
        final Array array = reader.readScaled(4, 4, new Interval(3, 3), "lon");

        assertNotNull(array);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());

        NCTestUtils.assertValueAt(9.969209968386869E36f, 0, 0, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 1, 0, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 2, 0, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 0, 1, array);
        NCTestUtils.assertValueAt(8.550399780273438f, 1, 1, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 2, 1, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 0, 2, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 1, 2, array);
        NCTestUtils.assertValueAt(9.969209968386869E36f, 2, 2, array);
    }

    @Test
    public void testReadScaled_1x1_lat() throws Exception {
        final Array array = reader.readScaled(5, 5, new Interval(1, 1), "lat");

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());

        assertEquals(53.572898864746094f, array.getFloat(0), 1e-8);
    }

    @Test
    public void testReadAcquisitionTime_3x3() throws Exception {
        final ArrayInt.D2 array = reader.readAcquisitionTime(0, 5129099, new Interval(3, 3));

        assertNotNull(array);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(DataType.INT, array.getDataType());

        NCTestUtils.assertValueAt(-2147483647, 0, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 1, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 0, 1, array);
        NCTestUtils.assertValueAt(1474066740, 1, 1, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 1, array);
        NCTestUtils.assertValueAt(-2147483647, 0, 2, array);
        NCTestUtils.assertValueAt(-2147483647, 1, 2, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 2, array);
    }

    @Test
    public void testReadAcquisitionTime_1x1() throws Exception {
        final ArrayInt.D2 array = reader.readAcquisitionTime(0, 5129098, new Interval(1, 1));

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.INT, array.getDataType());

        assertEquals(1474066680, array.getInt(0));
    }

    @Test
    public void testGetProductSize() {
        final Dimension productSize = reader.getProductSize();

        assertNotNull(productSize);
        assertEquals("product_size", productSize.getName());
        assertEquals(1, productSize.getNx());
        assertEquals(5129100, productSize.getNy());
    }

    @Test
    public void testGetTimeLocator() {
        final TimeLocator timeLocator = reader.getTimeLocator();

        assertNotNull(timeLocator);
        assertEquals(1276204020000L, timeLocator.getTimeFor(2, 7));
        assertEquals(1276204080000L, timeLocator.getTimeFor(2, 8));
        assertEquals(1276204140000L, timeLocator.getTimeFor(2, 9));
    }

    @Test
    public void testGetPixelLocator() {
        try {
            reader.getPixelLocator();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetSubScenePixelLocator() {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Polygon polygon = geometryFactory.createPolygon(Arrays.asList(
                geometryFactory.createPoint(5, 5),
                geometryFactory.createPoint(6, 6),
                geometryFactory.createPoint(7, 5)
        ));

        try {
            reader.getSubScenePixelLocator(polygon);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}

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

package com.bc.fiduceo.reader;

import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;
import ucar.nc2.iosp.netcdf3.N3iosp;

import static org.junit.Assert.*;

public class ReaderUtilsTest {

    @Test
    public void testGetDefaultFillValue_ProductData_double() {
        final Number fillValue = ReaderUtils.getDefaultFillValue(ProductData.TYPE_FLOAT64);
        assertEquals(N3iosp.NC_FILL_DOUBLE, fillValue.doubleValue(), 1e-8);
    }

    @Test
    public void testGetDefaultFillValue_ProductData_float() {
        final Number fillValue = ReaderUtils.getDefaultFillValue(ProductData.TYPE_FLOAT32);
        assertEquals(N3iosp.NC_FILL_FLOAT, fillValue.floatValue(), 1e-8);
    }

    @Test
    public void testGetDefaultFillValue_ProductData_int() {
        final Number fillValue = ReaderUtils.getDefaultFillValue(ProductData.TYPE_INT32);
        assertEquals(N3iosp.NC_FILL_INT, fillValue.intValue(), 1e-8);
    }

    @Test
    public void testGetDefaultFillValue_ProductData_short() {
        final Number fillValue = ReaderUtils.getDefaultFillValue(ProductData.TYPE_INT16);
        assertEquals(N3iosp.NC_FILL_SHORT, fillValue.shortValue(), 1e-8);
    }

    @Test
    public void testGetDefaultFillValue_ProductData_byte() {
        final Number fillValue = ReaderUtils.getDefaultFillValue(ProductData.TYPE_INT8);
        assertEquals(N3iosp.NC_FILL_BYTE, fillValue.byteValue(), 1e-8);
    }

    @Test
    public void testGetDefaultFillValue_ProductData_unsigned_byte() {
        final Number fillValue = ReaderUtils.getDefaultFillValue(ProductData.TYPE_UINT8);
        assertEquals(N3iosp.NC_FILL_UBYTE, fillValue.byteValue(), 1e-8);
    }

    @Test
    public void testGetDefaultFillValue_ProductData_invalidType() {
        try {
            ReaderUtils.getDefaultFillValue(ProductData.TYPE_UTC);
            fail("RuntimeException expected");
        } catch(RuntimeException expected) {
        }
    }

    @Test
    public void testMustScale() {
        assertTrue(ReaderUtils.mustScale(1.2, 0.45));
        assertTrue(ReaderUtils.mustScale(1.2, 0.0));
        assertTrue(ReaderUtils.mustScale(1.0, 0.45));

        assertFalse(ReaderUtils.mustScale(1.0, 0.0));
    }

    @Test
    public void testStripChannelSuffix() {
        assertEquals("btemps", ReaderUtils.stripChannelSuffix("btemps_ch17"));
        assertEquals("chanqual", ReaderUtils.stripChannelSuffix("chanqual_ch4"));
        assertEquals("quality_channel_bitmask", ReaderUtils.stripChannelSuffix("quality_channel_bitmask_ch04"));

        assertEquals("Latitude", ReaderUtils.stripChannelSuffix("Latitude"));
        assertEquals("scnlindy", ReaderUtils.stripChannelSuffix("scnlindy"));
    }

    @Test
    public void testGetChannelIndex() {
        assertEquals(17, ReaderUtils.getChannelIndex("btemps_ch18"));
        assertEquals(4, ReaderUtils.getChannelIndex("chanqual_ch5"));

        assertEquals(0, ReaderUtils.getChannelIndex("lon"));
        assertEquals(0, ReaderUtils.getChannelIndex("a_strange_channel"));
    }
}

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

package com.bc.fiduceo.reader.insitu;

import org.junit.Before;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;

import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_LONG_NAME;
import static org.junit.Assert.assertEquals;

public class UniqueIdVariableTest {

    private UniqueIdVariable variable;

    @Before
    public void setUp() {
        variable = new UniqueIdVariable("insitu.id");
    }

    @Test
    public void testGetFullName() {
        assertEquals("insitu.id", variable.getFullName());
    }

    @Test
    public void testGetShortName() {
        assertEquals("insitu.id", variable.getShortName());
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.LONG, variable.getDataType());
    }

    @Test
    public void testGetAttributes() {
        final List<Attribute> attributes = variable.getAttributes();
        assertEquals(3, attributes.size());
        assertEquals(CF_FILL_VALUE_NAME, attributes.get(0).getShortName());
        assertEquals(-32768L, attributes.get(0).getNumericValue());
        assertEquals(CF_LONG_NAME, attributes.get(1).getShortName());
        assertEquals("comment", attributes.get(2).getShortName());
    }
}

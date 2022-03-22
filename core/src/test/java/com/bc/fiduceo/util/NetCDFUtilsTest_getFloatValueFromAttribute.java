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

package com.bc.fiduceo.util;

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.*;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

public class NetCDFUtilsTest_getFloatValueFromAttribute {

    private Variable variable;
    private Attribute attribute;

    @Before
    public void setUp() throws Exception {
        variable = mock(Variable.class);
        attribute = mock(Attribute.class);
    }

    @Test
    public void testGetFloatValueFromAttribute() {
        when(variable.findAttribute(CF_FILL_VALUE_NAME)).thenReturn(attribute);
        when(attribute.getNumericValue()).thenReturn(-999.0);

        final float fillValue = NetCDFUtils.getFloatValueFromAttribute(variable, CF_FILL_VALUE_NAME, 1);

        assertThat(fillValue, is(equalTo(-999.0F)));
    }

    @Test
    public void testGetFloatValueFromAttribute_defaultValue() {
        final double scaleFactor = NetCDFUtils.getFloatValueFromAttribute(variable, null, 16.5F);

        assertEquals("16.5", "" + scaleFactor);

        verifyNoInteractions(variable);
    }

    @Test
    public void testGetFloatValueFromAttribute_noAttributeWithNameScale() {
        when(variable.findAttribute("scale")).thenReturn(null);

        try {
            NetCDFUtils.getFloatValueFromAttribute(variable, "scale", 1);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("No attribute with name 'scale'.", expected.getMessage());
        }
    }

    @Test
    public void testGetFloatValueFromAttribute_noNumberValue() {
        when(variable.findAttribute("scale")).thenReturn(attribute);

        try {
            NetCDFUtils.getFloatValueFromAttribute(variable, "scale", 1);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Attribute 'scale' does not own a number value.", expected.getMessage());
        }
    }
}

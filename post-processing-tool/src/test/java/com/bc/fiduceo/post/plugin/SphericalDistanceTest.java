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

package com.bc.fiduceo.post.plugin;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.*;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class SphericalDistanceTest {

    @Test
    public void testGetValueFromAttribute() {
        final Variable variable = mock(Variable.class);
        final Attribute attribute = mock(Attribute.class);

        when(variable.findAttribute("scale")).thenReturn(attribute);
        when(attribute.getNumericValue()).thenReturn(24.4);

        final double scaleFactor = SphericalDistance.getValueFromAttribute(variable, "scale", 1);

        assertEquals("24.4", "" + scaleFactor);
    }

    @Test
    public void testGetValueFromAttribute_defaultValue() {
        final Variable variable = mock(Variable.class);

        final double scaleFactor = SphericalDistance.getValueFromAttribute(variable, null, 1);

        assertEquals("1.0", "" + scaleFactor);

        verifyZeroInteractions(variable);
    }

    @Test
    public void testGetValueFromAttribute_noAttributeWithNameScale() {
        final Variable variable = mock(Variable.class);
        when(variable.findAttribute("scale")).thenReturn(null);

        try {
            SphericalDistance.getValueFromAttribute(variable, "scale", 1);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("No attribute with name 'scale'.", expected.getMessage());
        }
    }

    @Test
    public void testGetValueFromAttribute_noNumberValue() {
        final Variable variable = mock(Variable.class);
        final Attribute attribute = mock(Attribute.class);
        when(variable.findAttribute("scale")).thenReturn(attribute);

        try {
            SphericalDistance.getValueFromAttribute(variable, "scale", 1);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Attribute 'scale' does not own a number value.", expected.getMessage());
        }
    }

    @Test
    public void testGetCountDimension() throws Exception {
        final SphericalDistance sphericalDistance = new SphericalDistance("tvar", "tType", "tDim", null, null, null, null,
                                                                          null, null, null, null, null, null, null, null);
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        try {
            sphericalDistance.getCountDimension(netcdfFile);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Dimension 'tDim' expected", expected.getMessage());
        }
    }
}

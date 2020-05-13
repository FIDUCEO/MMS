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

package com.bc.fiduceo.reader.amsr.amsr2;

import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PixelDataQualityVariableTest {

    @Test
    public void testGetShortName() {
        final Variable variable = mock(Variable.class);
        when(variable.getShortName()).thenReturn("The_name");

        final PixelDataQualityVariable qualityVariable = new PixelDataQualityVariable(variable);

        assertEquals("The_name", qualityVariable.getShortName());
    }

    @Test
    public void testGetDataType() {
        final Variable variable = mock(Variable.class);

        final PixelDataQualityVariable qualityVariable = new PixelDataQualityVariable(variable);

        assertEquals(DataType.SHORT, qualityVariable.getDataType());
    }

    @Test
    public void testRead() throws IOException {
        final byte[][] byteData = {{0, 1, 2, 3}, {4, 5, 6, 7}, {8, 9, 10, 11}};
        final Array originalArray = NetCDFUtils.create(byteData);
        final Variable variable = mock(Variable.class);
        when(variable.read()).thenReturn(originalArray);

        final PixelDataQualityVariable qualityVariable = new PixelDataQualityVariable(variable);

        final Array mergedData = qualityVariable.read();
        final int[] shape = mergedData.getShape();
        assertEquals(3, shape[0]);
        assertEquals(2, shape[1]);
        assertEquals(256, mergedData.getShort(0));
        assertEquals(1284, mergedData.getShort(2));
        assertEquals(1798, mergedData.getShort(3));
    }

    @Test
    public void testGetAttributes() {
        final Variable variable = mock(Variable.class);

        final ArrayList<Attribute> originalAttributes = new ArrayList<>();
        originalAttributes.add(new Attribute("the_old_one", "ancient_value"));

        when(variable.getAttributes()).thenReturn(originalAttributes);

        final PixelDataQualityVariable qualityVariable = new PixelDataQualityVariable(variable);

        final List<Attribute> attributes = qualityVariable.getAttributes();
        assertEquals(3, attributes.size());

        Attribute attribute = attributes.get(1);
        assertEquals("flag_values", attribute.getShortName());
        assertEquals("2, 3, 8, 12, 32, 48, 128, 192, 32768", attribute.getStringValue());

        attribute = attributes.get(2);
        assertEquals("flag_meanings", attribute.getShortName());
        assertEquals("rfi_pos_6V rfi_cont_6V rfi_pos_6H rfi_cont_6H rfi_pos_7V rfi_cont_7V rfi_pos_7H rfi_cont_7H data_drop_7H", attribute.getStringValue());
    }
}

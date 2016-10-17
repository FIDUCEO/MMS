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

package com.bc.fiduceo.matchup.writer;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.RawDataSource;
import org.junit.*;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VariablePrototypeTest {

    private VariablePrototype prototype;

    @Before
    public void setUp() throws Exception {
        prototype = new VariablePrototype();
    }

    @Test
    public void testSetGetName() {
        final String name = "the_variable_name";

        prototype.setTargetVariableName(name);
        assertEquals(name, prototype.getTargetVariableName());
    }

    @Test
    public void testSetGetDimensionNames() {
        final String dimensionNames = "matchup ny ny";

        prototype.setDimensionNames(dimensionNames);
        assertEquals(dimensionNames, prototype.getDimensionNames());
    }

    @Test
    public void testSetGetDataType() {
        final String dataType = "float";

        prototype.setDataType(dataType);
        assertEquals(dataType, prototype.getDataType());
    }

    @Test
    public void testSetGetAttributes() throws Exception {
        final ArrayList<Attribute> attributes = new ArrayList<>();

        prototype.setAttributes(attributes);

        assertSame(attributes, prototype.getAttributes());
    }

    @Test
    public void testGetEmptyListAfterInitialisation() throws Exception {
        final List<Attribute> attributes = prototype.getAttributes();
        assertNotNull(attributes);
        assertEquals(0, attributes.size());
    }

    @Test
    public void testReadRaw() throws IOException, InvalidRangeException {
        final Array data = Array.factory(new int[]{1, 2, 3, 4});
        final RawDataSource rawDataSource = mock(RawDataSource.class);
        when(rawDataSource.readRaw(anyInt(), anyInt(), anyObject(), anyString())).thenReturn(data);
        final RawDataSourceContainer sourceContainer = new RawDataSourceContainer();
        sourceContainer.setSource(rawDataSource);

        final VariablePrototype variablePrototype = new VariablePrototype(sourceContainer);

        final Interval interval = new Interval(3, 3);
        final Array hans_wurst = variablePrototype.readRaw(3, 4, interval, "hans_wurst");
        assertNotNull(hans_wurst);

        verify(rawDataSource, times(1)).readRaw(3, 4, interval, "hans_wurst");
        verifyNoMoreInteractions(rawDataSource);
    }

}

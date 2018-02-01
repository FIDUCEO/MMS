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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.Reader;
import org.junit.*;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WindowReadingIOVariableTest {

    private WindowReadingIOVariable ioVariable;

    @Before
    public void setUp() throws Exception {
        ioVariable = new WindowReadingIOVariable(null);
    }

    @Test
    public void testSetGetName() {
        final String name = "the_variable_name";

        ioVariable.setTargetVariableName(name);
        assertEquals(name, ioVariable.getTargetVariableName());
    }

    @Test
    public void testSetGetDimensionNames() {
        final String dimensionNames = "matchup ny ny";

        ioVariable.setDimensionNames(dimensionNames);
        assertEquals(dimensionNames, ioVariable.getDimensionNames());
    }

    @Test
    public void testSetGetDataType() {
        final String dataType = "float";

        ioVariable.setDataType(dataType);
        assertEquals(dataType, ioVariable.getDataType());
    }

    @Test
    public void testSetGetAttributes() throws Exception {
        final ArrayList<Attribute> attributes = new ArrayList<>();

        ioVariable.setAttributes(attributes);

        assertSame(attributes, ioVariable.getAttributes());
    }

    @Test
    public void testGetEmptyListAfterInitialisation() {
        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(0, attributes.size());
    }

    @Test
    public void testWriteData() throws IOException, InvalidRangeException {
        final Target target = mock(Target.class);
        final Reader readerMock = mock(Reader.class);

        final Array data = Array.factory(new int[]{1, 2, 3, 4});
        when(readerMock.readRaw(anyInt(), anyInt(), any(), anyString())).thenReturn(data);
        final ReaderContainer sourceContainer = new ReaderContainer();
        sourceContainer.setReader(readerMock);

        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(sourceContainer);
        ioVariable.setTarget(target);
        ioVariable.setSourceVariableName("hans_wurst");
        ioVariable.setTargetVariableName("target_hans_wurst");

        final Interval interval = new Interval(3, 3);
        ioVariable.writeData(3, 4, interval, 4);

        verify(readerMock, times(1)).readRaw(3, 4, interval, "hans_wurst");
        verify(target,times(1)).write(data, "target_hans_wurst", 4);

        verifyNoMoreInteractions(readerMock);
        verifyNoMoreInteractions(target);
    }
}

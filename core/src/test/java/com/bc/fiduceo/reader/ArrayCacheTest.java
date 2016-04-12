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


import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ArrayCacheTest {

    private ArrayCache arrayCache;
    private NetcdfFile netcdfFile;
    private Variable variable;
    private Group group;

    @Before
    public void setUp() throws IOException {
        netcdfFile = mock(NetcdfFile.class);
        variable = mock(Variable.class);
        when(netcdfFile.findVariable(null, "a_variable")).thenReturn(variable);

        group = mock(Group.class);
        when(netcdfFile.findGroup("a_group")).thenReturn(group);
        when(netcdfFile.findVariable(group, "a_group_variable")).thenReturn(variable);

        final Array array = mock(Array.class);
        when(variable.read()).thenReturn(array);

        arrayCache = new ArrayCache(netcdfFile);
    }

    @Test
    public void testRequestedArrayIsRead() throws IOException {
        final Array resultArray =  arrayCache.get("a_variable");
        assertNotNull(resultArray);

        verify(netcdfFile, times(1)).findVariable(null, "a_variable");
        verify(variable, times(1)).read();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testRequestedArrayInGroupIsRead() throws IOException {
        final Array resultArray =  arrayCache.get("a_group", "a_group_variable");
        assertNotNull(resultArray);

        verify(netcdfFile, times(1)).findGroup("a_group");
        verify(netcdfFile, times(1)).findVariable(group, "a_group_variable");
        verify(variable, times(1)).read();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testRequestedArrayIsTakenFromCacheWhenRequestedASecondTime() throws IOException {
        final Array resultArray =  arrayCache.get("a_variable");
        assertNotNull(resultArray);

        final Array secondResultArray =  arrayCache.get("a_variable");
        assertNotNull(secondResultArray);

        assertSame(resultArray, secondResultArray);


        verify(netcdfFile, times(1)).findVariable(null, "a_variable");
        verify(variable, times(1)).read();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testRequestedArrayInGroupIsTakenFromCacheWhenRequestedASecondTime() throws IOException {
        final Array resultArray =  arrayCache.get("a_group", "a_group_variable");
        assertNotNull(resultArray);

        final Array secondResultArray =  arrayCache.get("a_group", "a_group_variable");
        assertNotNull(secondResultArray);

        assertSame(resultArray, secondResultArray);

        verify(netcdfFile, times(1)).findGroup("a_group");
        verify(netcdfFile, times(1)).findVariable(group, "a_group_variable");
        verify(variable, times(1)).read();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testArrayThrowsWhenVariableIsNotPresent() throws IOException {
        try {
            arrayCache.get("not_present_variable");
            fail("IOException expected");
        } catch (IOException expected) {
        }

        verify(netcdfFile, times(1)).findVariable(null, "not_present_variable");
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testArrayThrowsWhenVariableInGroupIsNotPresent() throws IOException {
        try {
            arrayCache.get("a_group", "not_present_variable");
            fail("IOException expected");
        } catch (IOException expected) {
        }

        verify(netcdfFile, times(1)).findGroup("a_group");
        verify(netcdfFile, times(1)).findVariable(group, "not_present_variable");
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testArrayThrowsWhenGroupIsNotPresent() throws IOException {
        try {
            arrayCache.get("a_shitty_group", "not_present_variable");
            fail("IOException expected");
        } catch (IOException expected) {
        }

        verify(netcdfFile, times(1)).findGroup("a_shitty_group");
        verifyNoMoreInteractions(netcdfFile, variable);
    }
}

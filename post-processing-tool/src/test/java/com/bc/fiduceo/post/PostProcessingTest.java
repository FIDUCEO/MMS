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

package com.bc.fiduceo.post;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.*;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import java.io.IOException;

public class PostProcessingTest {

    private PostProcessing postProcessing;

    @Before
    public void setUp() throws Exception {
        postProcessing = new PostProcessing() {
            @Override
            protected void prepareImpl(NetcdfFile reader, NetcdfFileWriter writer) {

            }

            @Override
            protected void computeImpl(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {

            }
        };
    }

    @Test
    public void testPrepare() {
        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        when(writer.isDefineMode()).thenReturn(true);
        postProcessing.prepare(null, writer);

        verify(writer, times(1)).isDefineMode();
        verifyNoMoreInteractions(writer);
    }

    @Test
    public void testPrepare_wrongMode() {
        final NetcdfFileWriter netcdfFileWriter = mock(NetcdfFileWriter.class);
        when(netcdfFileWriter.isDefineMode()).thenReturn(false);
        try {
            postProcessing.prepare(null, netcdfFileWriter);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("NetcdfFileWriter has to be in 'define' mode.", expected.getMessage());
        }

        verify(netcdfFileWriter, times(1)).isDefineMode();
        verifyNoMoreInteractions(netcdfFileWriter);
    }

    @Test
    public void testCompute() throws IOException, InvalidRangeException {
        final NetcdfFileWriter netcdfFileWriter = mock(NetcdfFileWriter.class);
        when(netcdfFileWriter.isDefineMode()).thenReturn(false);
        postProcessing.compute(null, netcdfFileWriter);

        verify(netcdfFileWriter, times(1)).isDefineMode();
        verifyNoMoreInteractions(netcdfFileWriter);
    }

    @Test
    public void testCompute_wrongMode() throws IOException, InvalidRangeException {
        final NetcdfFileWriter netcdfFileWriter = mock(NetcdfFileWriter.class);
        when(netcdfFileWriter.isDefineMode()).thenReturn(true);
        try {
            postProcessing.compute(null, netcdfFileWriter);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("NetcdfFileWriter has NOT to be in 'define' mode.", expected.getMessage());
        }

        verify(netcdfFileWriter, times(1)).isDefineMode();
        verifyNoMoreInteractions(netcdfFileWriter);
    }
}

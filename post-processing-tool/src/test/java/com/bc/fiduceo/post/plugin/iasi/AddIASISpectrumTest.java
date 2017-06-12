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

package com.bc.fiduceo.post.plugin.iasi;

import org.junit.Before;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class AddIASISpectrumTest {

    private NetcdfFile reader;
    private NetcdfFileWriter writer;

    @Before
    public void setUp() {
        reader = mock(NetcdfFile.class);
        writer = mock(NetcdfFileWriter.class);
    }

    // qtodo 1 tb/tb modify test code 2017-06-12
//    @Test
//    public void testPrepare() throws IOException, InvalidRangeException {
//        final AddIASISpectrum.Configuration configuration = new AddIASISpectrum.Configuration();
//        configuration.targetVariableName = "GS1cSpect";
//        configuration.referenceVariableName = "ref_var";
//
//        final Variable referenceVariable = mock(Variable.class);
//        final ArrayList<Dimension> dimensions = new ArrayList<>();
//        dimensions.add(new Dimension("matchup_count", 142));
//        dimensions.add(new Dimension("height", 1));
//        dimensions.add(new Dimension("width", 1));
//        when(referenceVariable.getDimensions()).thenReturn(dimensions);
//        when(reader.findVariable(null, "ref_var")).thenReturn(referenceVariable);
//
//        final AddIASISpectrum plugin = new AddIASISpectrum(configuration);
//
//        plugin.prepare(reader, writer);
//
//        final ArrayList<Dimension> targetDimensions = new ArrayList<>();
//        targetDimensions.add(new Dimension("matchup_count", 142));
//        targetDimensions.add(new Dimension("height", 1));
//        targetDimensions.add(new Dimension("width", 1));
//        targetDimensions.add(new Dimension("iasi_ss", 8700));
//
//        verify(reader, times(1)).findVariable(null, "ref_var");
//        verify(writer, times(1)).addVariable(null, "GS1cSpect", DataType.FLOAT, targetDimensions);
//        verify(writer, times(1)).addDimension(null, "iasi_ss", 8700);
//        verifyNoMoreInteractions(reader, writer);
//    }

    @Test
    public void testPrepare_missingReferenceVariable() throws IOException, InvalidRangeException {
        final AddIASISpectrum.Configuration configuration = new AddIASISpectrum.Configuration();
        configuration.targetVariableName = "GS1cSpect";
        configuration.referenceVariableName = "ref_var";

        final AddIASISpectrum plugin = new AddIASISpectrum(configuration);

        try {
            plugin.prepare(reader, writer);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }

        verify(reader, times(1)).findVariable(null, "ref_var");
        verifyNoMoreInteractions(reader, writer);
    }

    @Test
    public void testGetSensorKey() {
         assertEquals("iasi-ma", AddIASISpectrum.getSensorKey("IASI_xxx_1C_M02_20160101124754Z_20160101142658Z_N_O_20160101142620Z.nat"));
         assertEquals("iasi-mb", AddIASISpectrum.getSensorKey("IASI_xxx_1C_M01_20140425124756Z_20140425142652Z_N_O_20140425133911Z.nat"));
    }

    @Test
    public void testGetSensorKey_invalidFileName() {
        try {
            AddIASISpectrum.getSensorKey("NSS.HIRX.TN.D79287.S1623.E1807.B0516566.GC.nc");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testExtracYMDFromFileName() {
        int[] ymd = AddIASISpectrum.extractYMDfromFileName("IASI_xxx_1C_M02_20160101124754Z_20160101142658Z_N_O_20160101142620Z.nat");
        assertEquals(2016, ymd[0]);
        assertEquals(1, ymd[1]);
        assertEquals(1, ymd[2]);

        ymd = AddIASISpectrum.extractYMDfromFileName("IASI_xxx_1C_M01_20140425124756Z_20140425142652Z_N_O_20140425133911Z.nat");
        assertEquals(2014, ymd[0]);
        assertEquals(4, ymd[1]);
        assertEquals(25, ymd[2]);
    }
}

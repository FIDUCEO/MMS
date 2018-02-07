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

package com.bc.fiduceo.post.plugin;

import org.junit.Before;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class AddAmsr2ScanDataQualityTest {

    private AddAmsr2ScanDataQuality plugin;
    private NetcdfFile reader;
    private NetcdfFileWriter writer;

    @Before
    public void setUp(){
        final AddAmsr2ScanDataQuality.Configuration configuration = new AddAmsr2ScanDataQuality.Configuration();
        configuration.referenceVariableName = "amsr2-gcw1_lat";
        configuration.targetVariableName = "amsr2-gcw1_Scan_Data_Quality";

        plugin = new AddAmsr2ScanDataQuality();
        plugin.configure(configuration);

        reader = mock(NetcdfFile.class);
        writer = mock(NetcdfFileWriter.class);
    }

    @Test
    public void testPrepare() throws IOException, InvalidRangeException {
        final Dimension dimension = mock(Dimension.class);
        when(dimension.getLength()).thenReturn(107);
        when(reader.findDimension("matchup_count")).thenReturn(dimension);

        final Variable targetVariable = mock(Variable.class);
        when(writer.addVariable(eq(null), eq("amsr2-gcw1_Scan_Data_Quality"), eq(DataType.BYTE), anyList())).thenReturn(targetVariable);

        plugin.prepare(reader, writer);

        verify(reader, times(1)).findDimension("matchup_count");
        verifyNoMoreInteractions(reader);

        verify(writer, times(1)).addVariable(eq(null), eq("amsr2-gcw1_Scan_Data_Quality"), eq(DataType.BYTE), anyList());
        verifyNoMoreInteractions(writer);
    }

    @Test
    public void testPrepare_missingInputVariable() throws IOException, InvalidRangeException {
        try {
            plugin.prepare(reader, writer);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}

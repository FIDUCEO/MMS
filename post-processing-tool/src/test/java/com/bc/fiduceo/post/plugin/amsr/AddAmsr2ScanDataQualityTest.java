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

package com.bc.fiduceo.post.plugin.amsr;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.post.plugin.amsr.AddAmsr2ScanDataQuality;
import org.junit.*;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

public class AddAmsr2ScanDataQualityTest {

    private AddAmsr2ScanDataQuality plugin;
    private NetcdfFile reader;
    private NetcdfFileWriter writer;

    @Before
    public void setUp() {
        final AddAmsr2ScanDataQuality.Configuration configuration = new AddAmsr2ScanDataQuality.Configuration();
        configuration.filenameVariableName = "amsr2-gcw1_file_name";
        configuration.targetVariableName = "amsr2-gcw1_Scan_Data_Quality";

        plugin = new AddAmsr2ScanDataQuality();
        plugin.configure(configuration);

        reader = mock(NetcdfFile.class);
        writer = mock(NetcdfFileWriter.class);
    }

    @Test
    public void testPrepare() {
        final Dimension dimension = mock(Dimension.class);
        when(dimension.getLength()).thenReturn(107);
        when(reader.findDimension(FiduceoConstants.MATCHUP_COUNT)).thenReturn(dimension);

        final Variable targetVariable = mock(Variable.class);
        when(writer.addVariable(eq(null), eq("amsr2-gcw1_Scan_Data_Quality"), eq(DataType.BYTE), anyList())).thenReturn(targetVariable);

        plugin.prepare(reader, writer);

        verify(reader, times(1)).findDimension(FiduceoConstants.MATCHUP_COUNT);
        verifyNoMoreInteractions(reader);

        verify(writer, times(1)).addDimension(null, "scan_data_quality", 512);
        verify(writer, times(1)).addVariable(eq(null), eq("amsr2-gcw1_Scan_Data_Quality"), eq(DataType.BYTE), anyList());
        verifyNoMoreInteractions(writer);
    }

    @Test
    public void testPrepare_missingInputVariable() {
        try {
            plugin.prepare(reader, writer);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}

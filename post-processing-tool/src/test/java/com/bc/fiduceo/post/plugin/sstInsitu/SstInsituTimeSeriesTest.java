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

package com.bc.fiduceo.post.plugin.sstInsitu;

import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeries.INSITU_NTIME;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.post.Constants;
import com.bc.fiduceo.reader.Reader;
import com.beust.jcommander.internal.Lists;
import org.junit.*;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.util.Arrays;
import java.util.Collections;

public class SstInsituTimeSeriesTest {

    @Test
    public void extractSensorType_Success() throws Exception {
        final Variable variable = mock(Variable.class);
        when(variable.getShortName()).thenReturn("sensor-name_insitu.sonstwas");

        final NetcdfFile reader = mock(NetcdfFile.class);
        when(reader.getVariables()).thenReturn(Collections.singletonList(variable));

        final String sensorType = SstInsituTimeSeries.extractSensorType(reader);

        assertEquals("sensor-name", sensorType);
    }

    @Test
    public void extractSensorType_DoesNotContainVariablesWithNameContainig_insituDot() throws Exception {
        final Variable v1 = mock(Variable.class);
        when(v1.getShortName()).thenReturn("DontContainInsituWithUnderscoreAndDot_1");

        final Variable v2 = mock(Variable.class);
        when(v2.getShortName()).thenReturn("DontContainInsituWithUnderscoreAndDot_2");

        final NetcdfFile reader = mock(NetcdfFile.class);
        when(reader.getVariables()).thenReturn(Arrays.asList(v1, v2));

        try {
            SstInsituTimeSeries.extractSensorType(reader);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Unable to extract sensor type.", expected.getMessage());
        }
    }


    @Test
    public void addInsituVariables() throws Exception {
        final SstInsituTimeSeries insituTimeSeries = new SstInsituTimeSeries("v123", 234, 34, "matchupTimeVarName");

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);

        final Reader insituReader = mock(Reader.class);

        final Variable v1 = mock(Variable.class);
        when(v1.getShortName()).thenReturn("insitu.lat");
        when(v1.getDataType()).thenReturn(DataType.FLOAT);
        when(v1.getAttributes()).thenReturn(Lists.newArrayList());
        final Variable v2 = mock(Variable.class);
        when(v2.getShortName()).thenReturn("insitu.time");
        when(v2.getDataType()).thenReturn(DataType.INT);
        when(v2.getAttributes()).thenReturn(Lists.newArrayList());
        final Variable v3 = mock(Variable.class);
        when(v3.getShortName()).thenReturn("insitu.lon");
        when(v3.getDataType()).thenReturn(DataType.FLOAT);
        when(v3.getAttributes()).thenReturn(Lists.newArrayList());


        when(insituReader.getVariables()).thenReturn(Arrays.asList(v1, v2, v3));
        final Variable newVar = mock(Variable.class);
        when(writer.addVariable(any(Group.class), any(String.class), any(DataType.class), any(String.class))).thenReturn(newVar);

        insituTimeSeries.addInsituVariables(writer, insituReader);

        final String dimString = Constants.MATCHUP_COUNT + " " + INSITU_NTIME;

        verify(writer, times(1)).addDimension(null, "insitu.ntime", 34);
        verify(writer, times(1)).addVariable(null, "insitu.lat", DataType.FLOAT, dimString);
        verify(writer, times(1)).addVariable(null, "insitu.time", DataType.INT, dimString);
        verify(writer, times(1)).addVariable(null, "insitu.lon", DataType.FLOAT, dimString);
        verify(writer, times(1)).addVariable(null, "insitu.y", DataType.INT, dimString);
        verify(writer, times(1)).addVariable(null, "insitu.dtime", DataType.INT, dimString);
        verify(insituReader, times(1)).getVariables();
        verify(newVar, times(3)).addAll(isA(Iterable.class));
        verify(v3, times(1)).getShortName();
        verify(v3, times(1)).getDataType();
        verify(v3, times(1)).getAttributes();
        verify(v2, times(1)).getShortName();
        verify(v2, times(1)).getDataType();
        verify(v2, times(1)).getAttributes();
        verify(newVar, times(3)).addAttribute(any(Attribute.class));
        verifyNoMoreInteractions(writer, insituReader, newVar, v3, v2);
    }
}

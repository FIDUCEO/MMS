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


import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.ReaderFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(IOTestRunner.class)
public class IOVariableList_IO_Test {

    private IOVariablesList ioVariablesList;
    private String sensorName;
    private Path noaa17Path;
    private VariablesConfiguration variablesConfiguration;

    @Before
    public void setUp() throws Exception {
        sensorName = "avhrr-n17";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n17", "v01.3", "2007", "04", "01", "20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;

        noaa17Path = Paths.get(absolutePath);

        final ReaderFactory readerFactory = ReaderFactory.create(new GeometryFactory(GeometryFactory.Type.S2), null); // we don't need temp file support here tb 2018-01-23
        ioVariablesList = new IOVariablesList(readerFactory);

        variablesConfiguration = new VariablesConfiguration();
    }

    @Test
    public void testExtractVariables_AVHRR_NOAA_17() throws IOException {

        ioVariablesList.extractVariables(sensorName, noaa17Path, new Dimension(sensorName, 5, 5), variablesConfiguration);

        final List<IOVariable> ioVariables = ioVariablesList.get();
        assertEquals(17, ioVariables.size());

        IOVariable ioVariable = ioVariables.get(0);
        assertEquals("avhrr-n17_lat", ioVariable.getTargetVariableName());
        assertEquals("float", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

        ioVariable = ioVariables.get(1);
        assertEquals("avhrr-n17_lon", ioVariable.getTargetVariableName());
        assertEquals("float", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

        ioVariable = ioVariables.get(2);
        assertEquals("avhrr-n17_dtime", ioVariable.getTargetVariableName());
        assertEquals("float", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

        ioVariable = ioVariables.get(3);
        assertEquals("avhrr-n17_ch1", ioVariable.getTargetVariableName());
        assertEquals("short", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

        ioVariable = ioVariables.get(4);
        assertEquals("avhrr-n17_ch2", ioVariable.getTargetVariableName());
        assertEquals("short", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

        ioVariable = ioVariables.get(5);
        assertEquals("avhrr-n17_ch3a", ioVariable.getTargetVariableName());
        assertEquals("short", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

        ioVariable = ioVariables.get(6);
        assertEquals("avhrr-n17_ch3b", ioVariable.getTargetVariableName());
        assertEquals("short", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

        ioVariable = ioVariables.get(7);
        assertEquals("avhrr-n17_ch4", ioVariable.getTargetVariableName());
        assertEquals("short", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

        ioVariable = ioVariables.get(8);
        assertEquals("avhrr-n17_ch5", ioVariable.getTargetVariableName());
        assertEquals("short", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

        ioVariable = ioVariables.get(9);
        assertEquals("avhrr-n17_satellite_zenith_angle", ioVariable.getTargetVariableName());
        assertEquals("short", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

        ioVariable = ioVariables.get(10);
        assertEquals("avhrr-n17_solar_zenith_angle", ioVariable.getTargetVariableName());
        assertEquals("short", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

        ioVariable = ioVariables.get(11);
        assertEquals("avhrr-n17_relative_azimuth_angle", ioVariable.getTargetVariableName());
        assertEquals("short", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

        ioVariable = ioVariables.get(12);
        assertEquals("avhrr-n17_ict_temp", ioVariable.getTargetVariableName());
        assertEquals("short", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

        ioVariable = ioVariables.get(13);
        assertEquals("avhrr-n17_qual_flags", ioVariable.getTargetVariableName());
        assertEquals("byte", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

        ioVariable = ioVariables.get(14);
        assertEquals("avhrr-n17_cloud_mask", ioVariable.getTargetVariableName());
        assertEquals("byte", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

        ioVariable = ioVariables.get(15);
        assertEquals("avhrr-n17_cloud_probability", ioVariable.getTargetVariableName());
        assertEquals("byte", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

        ioVariable = ioVariables.get(16);
        assertEquals("avhrr-n17_l1b_line_number", ioVariable.getTargetVariableName());
        assertEquals("short", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());
    }

    @Test
    public void testExtractVariables_AVHRR_NOAA_17_mostOfThemExcluded() throws IOException {
        variablesConfiguration.addExcludes(sensorName, Arrays.asList(
                "lat",
                "lon",
                "dtime",
                "ch1",
                "ch2",
                "ch3a",
                "ch3b",
                "ch4",
                "ch5",
                "relative_azimuth_angle",
                "ict_temp",
                "qual_flags",
                "cloud_mask",
                "cloud_probability",
                "l1b_line_number"
        ));

        ioVariablesList.extractVariables(sensorName, noaa17Path, new Dimension(sensorName, 5, 5), variablesConfiguration);

        final List<IOVariable> ioVariables = ioVariablesList.get();
        assertEquals(2, ioVariables.size());

        IOVariable ioVariable;


        ioVariable = ioVariables.get(0);
        assertEquals("avhrr-n17_satellite_zenith_angle", ioVariable.getTargetVariableName());
        assertEquals("short", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

        ioVariable = ioVariables.get(1);
        assertEquals("avhrr-n17_solar_zenith_angle", ioVariable.getTargetVariableName());
        assertEquals("short", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

    }

    @Test
    public void testExtractVariables_AVHRR_NOAA_17_mostOfThemExcluded_restIsRenamed() throws IOException {
        variablesConfiguration.addExcludes(sensorName, Arrays.asList(
                "lat",
                "lon",
                "dtime",
                "ch1",
                "ch2",
                "ch3a",
                "ch3b",
                "ch4",
                "ch5",
                "relative_azimuth_angle",
                "ict_temp",
                "qual_flags",
                "cloud_mask",
                "cloud_probability",
                "l1b_line_number"
        ));
        Map<String, String> renames = new HashMap<>();
        renames.put("satellite_zenith_angle", "satza");
        renames.put("solar_zenith_angle", "solza");
        variablesConfiguration.addRenames(sensorName, renames);
        variablesConfiguration.addSensorRename("avhrr-n17", "xxxxxx");
        variablesConfiguration.setSeparator("avhrr-n17", ".");

        ioVariablesList.extractVariables(sensorName, noaa17Path, new Dimension(sensorName, 5, 5), variablesConfiguration);

        final List<IOVariable> ioVariables = ioVariablesList.get();
        assertEquals(2, ioVariables.size());

        IOVariable ioVariable;


        ioVariable = ioVariables.get(0);
        assertEquals("xxxxxx.satza", ioVariable.getTargetVariableName());
        assertEquals("short", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

        ioVariable = ioVariables.get(1);
        assertEquals("xxxxxx.solza", ioVariable.getTargetVariableName());
        assertEquals("short", ioVariable.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", ioVariable.getDimensionNames());

    }
}

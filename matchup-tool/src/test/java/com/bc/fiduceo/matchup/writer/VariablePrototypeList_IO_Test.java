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

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.ReaderFactory;
import org.junit.*;
import org.junit.runner.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RunWith(IOTestRunner.class)
public class VariablePrototypeList_IO_Test {

    @Test
    public void testExtractPrototypes_AVHRR_NOAA_17() throws IOException {
        final Sensor sensor = new Sensor("avhrr-n17");
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n17", "1.01", "2007", "04", "01", "20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;

        final Path noaa17Path = Paths.get(absolutePath);

        final ReaderFactory readerFactory = ReaderFactory.get(new GeometryFactory(GeometryFactory.Type.S2));
        final VariablePrototypeList variablePrototypeList = new VariablePrototypeList(readerFactory);
        variablePrototypeList.extractPrototypes(sensor, noaa17Path, new Dimension("avhrr-n17", 5, 5));

        final List<VariablePrototype> variablePrototypes = variablePrototypeList.get();
        assertEquals(17, variablePrototypes.size());

        VariablePrototype prototype = variablePrototypes.get(0);
        assertEquals("avhrr-n17_lat", prototype.getTargetVariableName());
        assertEquals("float", prototype.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", prototype.getDimensionNames());

        prototype = variablePrototypes.get(1);
        assertEquals("avhrr-n17_lon", prototype.getTargetVariableName());
        assertEquals("float", prototype.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", prototype.getDimensionNames());

        prototype = variablePrototypes.get(2);
        assertEquals("avhrr-n17_dtime", prototype.getTargetVariableName());
        assertEquals("float", prototype.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", prototype.getDimensionNames());

        prototype = variablePrototypes.get(3);
        assertEquals("avhrr-n17_ch1", prototype.getTargetVariableName());
        assertEquals("short", prototype.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", prototype.getDimensionNames());

        prototype = variablePrototypes.get(9);
        assertEquals("avhrr-n17_satellite_zenith_angle", prototype.getTargetVariableName());
        assertEquals("short", prototype.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", prototype.getDimensionNames());

        prototype = variablePrototypes.get(10);
        assertEquals("avhrr-n17_solar_zenith_angle", prototype.getTargetVariableName());
        assertEquals("short", prototype.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", prototype.getDimensionNames());

        prototype = variablePrototypes.get(11);
        assertEquals("avhrr-n17_relative_azimuth_angle", prototype.getTargetVariableName());
        assertEquals("short", prototype.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", prototype.getDimensionNames());

        prototype = variablePrototypes.get(12);
        assertEquals("avhrr-n17_ict_temp", prototype.getTargetVariableName());
        assertEquals("short", prototype.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", prototype.getDimensionNames());

        prototype = variablePrototypes.get(13);
        assertEquals("avhrr-n17_qual_flags", prototype.getTargetVariableName());
        assertEquals("byte", prototype.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", prototype.getDimensionNames());

        prototype = variablePrototypes.get(14);
        assertEquals("avhrr-n17_cloud_mask", prototype.getTargetVariableName());
        assertEquals("byte", prototype.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", prototype.getDimensionNames());

        prototype = variablePrototypes.get(15);
        assertEquals("avhrr-n17_cloud_probability", prototype.getTargetVariableName());
        assertEquals("byte", prototype.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", prototype.getDimensionNames());

        prototype = variablePrototypes.get(16);
        assertEquals("avhrr-n17_l1b_line_number", prototype.getTargetVariableName());
        assertEquals("short", prototype.getDataType());
        assertEquals("matchup_count avhrr-n17_ny avhrr-n17_nx", prototype.getDimensionNames());
    }
}

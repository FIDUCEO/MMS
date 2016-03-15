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
import com.bc.fiduceo.core.Sensor;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(IOTestRunner.class)
public class VariablesConfigurationTest {

    @Test
    public void testExtractPrototypes_AVHRR_NOAA_17() throws IOException {
        final Sensor sensor = new Sensor("avhrr-n17");
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n17", "1.01", "2007", "04", "01", "20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;

        final VariablesConfiguration variablesConfiguration = new VariablesConfiguration();
        variablesConfiguration.extractPrototypes(sensor, absolutePath);

//        final List<VariablePrototype> variablePrototypes = variablesConfiguration.get();
//        assertEquals(18, variablePrototypes.size());
//
//        final VariablePrototype prototype = variablePrototypes.get(0);
//        assertEquals("avhrr-n17_ch1", prototype.getName());
    }
}

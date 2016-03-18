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

package com.bc.fiduceo.matchup;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.matchup.writer.VariablePrototype;
import com.bc.fiduceo.matchup.writer.VariablesConfiguration;
import com.bc.fiduceo.tool.ToolContext;
import org.junit.*;
import org.junit.runner.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@RunWith(IOTestRunner.class)
public class MatchupToolTest_IO {

    @Test
    public void testVariableConfiguration() throws Exception {
        //preparation
        final Sensor primarySensor = createSensor("avhrr-n17", true);
        final Sensor secondarySensor = createSensor("avhrr-n18", false);
        final Dimension primaryWindowDimension = new Dimension("avhrr-n17", 5, 4);
        final Dimension secondaryWindowDimension = new Dimension("avhrr-n18", 5, 4);
        final String[] primary = {"avhrr-n17", "1.01", "2007", "04", "01", "20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc"};
        final String[] secondary = {"avhrr-n18", "1.02", "2007", "04", "01", "20070401080400-ESACCI-L1C-AVHRR18_G-fv01.0.nc"};

        final UseCaseConfig useCaseConfig = new UseCaseConfig();
        useCaseConfig.setDimensions(Arrays.asList(primaryWindowDimension, secondaryWindowDimension));
        useCaseConfig.setSensors(Arrays.asList(primarySensor, secondarySensor));

        final ToolContext toolContext = mock(ToolContext.class);
        when(toolContext.getUseCaseConfig()).thenReturn(useCaseConfig);

        final MatchupSet matchupSet = new MatchupSet();
        matchupSet.setPrimaryObservationPath(createPath(primary));
        matchupSet.setSecondaryObservationPath(createPath(secondary));

        final MatchupCollection matchupCollection = new MatchupCollection();
        matchupCollection.add(matchupSet);

        // test execution
        final VariablesConfiguration variablesConfiguration = MatchupTool.createVariablesConfiguration(matchupCollection, toolContext);

        // validation
        assertNotNull(variablesConfiguration);

        final List<VariablePrototype> variablePrototypeList = variablesConfiguration.get();
        assertEquals(34, variablePrototypeList.size());

        final List<VariablePrototype> prototypesFor = variablesConfiguration.getPrototypesFor("avhrr-n17");
        assertEquals(17, prototypesFor.size());

        final VariablePrototype variablePrototype = prototypesFor.get(0);
        // @todo 1 se/** continue implementing this test  2016-03-18
    }

    private Sensor createSensor(String name, boolean isPrimary) {
        final Sensor primarySensor = new Sensor();
        primarySensor.setPrimary(isPrimary);
        primarySensor.setName(name);
        return primarySensor;
    }

    private Path createPath(final String[] strings) throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(strings, false);
        return TestUtil.getTestDataDirectoryPath().resolve(testFilePath);
    }
}

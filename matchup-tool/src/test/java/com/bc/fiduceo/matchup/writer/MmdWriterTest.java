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
import static org.mockito.Mockito.*;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.tool.ToolContext;
import com.bc.fiduceo.util.TimeUtils;
import org.junit.*;
import org.mockito.InOrder;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFileWriter;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MmdWriterTest {

    @Test
    public void testCreateMMDFileName() {
        final UseCaseConfig useCaseConfig = new UseCaseConfig();
        useCaseConfig.setName("mmd-12");

        final List<Sensor> sensors = new ArrayList<>();
        sensors.add(new Sensor("avhrr-n07"));
        final Sensor primary = new Sensor("avhrr-n08");
        primary.setPrimary(true);
        sensors.add(primary);
        useCaseConfig.setSensors(sensors);

        final Date startDate = TimeUtils.parseDOYBeginOfDay("2011-245");
        final Date endDate = TimeUtils.parseDOYEndOfDay("2011-251");

        final String fileName = MmdWriter.createMMDFileName(useCaseConfig, startDate, endDate);
        assertEquals("mmd-12_avhrr-n08_avhrr-n07_2011-245_2011-251.nc", fileName);
    }

    @Test
    public void testCreateUseCaseAttributesGroupInMmdFile() throws Exception {
        final UseCaseConfig useCaseConfig = new UseCaseConfig();
        useCaseConfig.setName("NameOfTheUseCase");
        useCaseConfig.setTimeDeltaSeconds(234);
        useCaseConfig.setMaxPixelDistanceKm(12.34f);

        final Sensor primarySensor = new Sensor("SensorName1");
        primarySensor.setPrimary(true);
        useCaseConfig.setSensors(Arrays.asList(
                    primarySensor,
                    new Sensor("SensorName2"),
                    new Sensor("SensorName3")
        ));
        useCaseConfig.setDimensions(Arrays.asList(
                    new Dimension("SensorName1", 1, 2),
                    new Dimension("SensorName2", 3, 4),
                    new Dimension("SensorName3", 5, 6)
        ));

        final NetcdfFileWriter mockWriter = mock(NetcdfFileWriter.class);

        //test
        MmdWriter.createUseCaseAttributes(mockWriter, useCaseConfig);

        //verification
        final String useCaseAttributeName = "use-case-configuration";
        // @todo 1 se/tb please check english!! 2016-03-22
        final String expectedCommentText = "The MMD file is created based on the use case configuration " +
                                           "documented in the attribute '" + useCaseAttributeName + "'.";
        verify(mockWriter).addGroupAttribute(isNull(Group.class), eq(new Attribute("comment", expectedCommentText)));
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        useCaseConfig.store(outputStream);
        verify(mockWriter).addGroupAttribute(isNull(Group.class), eq(new Attribute(useCaseAttributeName, outputStream.toString())));
        verifyNoMoreInteractions(mockWriter);
    }


    @Test
    public void testThrowAway() throws Exception {
        final Array stack = Array.factory(DataType.INT, new int[]{4, 3, 3});
        final Array dataToBeWritten = Array.factory(new int[][]{
                    new int[]{1, 2, 3},
                    new int[]{4, 5, 6},
                    new int[]{7, 8, 9}
        });
        final Index index = stack.getIndex().set(1);
        Array.arraycopy(dataToBeWritten, 0, stack, index.currentElement(), (int) dataToBeWritten.getSize());
        final int[] storage = (int[]) stack.getStorage();
        final int[] expecteds = new int[]{
                    0, 0, 0, 0, 0, 0, 0, 0, 0,
                    1, 2, 3, 4, 5, 6, 7, 8, 9,
                    0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        assertArrayEquals(expecteds, storage);
    }

    @Test
    public void testGetFirstMatchupSet_emptyList() {
        final MatchupCollection matchupCollection = new MatchupCollection();

        try {
            MmdWriter.getFirstMatchupSet(matchupCollection);
            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testGetFirstMatchupSet() {
        final MatchupCollection collection = new MatchupCollection();
        final MatchupSet first = new MatchupSet();
        final MatchupSet second = new MatchupSet();
        collection.add(first);
        collection.add(second);

        final MatchupSet set = MmdWriter.getFirstMatchupSet(collection);

        assertSame(first, set);
    }

    @Test
    public void testVariableConfiguration() throws Exception {
        //preparation
        final Sensor primarySensor = createSensor("avhrr-n17", true);
        final Sensor secondarySensor = createSensor("avhrr-n18", false);
        final Dimension primaryWindowDimension = new Dimension("avhrr-n17", 5, 4);
        final Dimension secondaryWindowDimension = new Dimension("avhrr-n18", 5, 4);
        final Path mockingPrimaryPath = Paths.get("mockingPrimaryPath");
        final Path mockingSecondaryPath = Paths.get("mockingSecondaryPath");

        final UseCaseConfig useCaseConfig = new UseCaseConfig();
        useCaseConfig.setDimensions(Arrays.asList(primaryWindowDimension, secondaryWindowDimension));
        useCaseConfig.setSensors(Arrays.asList(primarySensor, secondarySensor));

        final ToolContext toolContext = mock(ToolContext.class);
        when(toolContext.getUseCaseConfig()).thenReturn(useCaseConfig);

        final MatchupSet matchupSet = new MatchupSet();
        matchupSet.setPrimaryObservationPath(mockingPrimaryPath);
        matchupSet.setSecondaryObservationPath(mockingSecondaryPath);

        final MatchupCollection matchupCollection = new MatchupCollection();
        matchupCollection.add(matchupSet);

        final VariablesConfiguration configuration = mock(VariablesConfiguration.class);

        // test execution
        MmdWriter.extractPrototypes(configuration, matchupCollection, toolContext);

        // validation
        verify(configuration).extractPrototypes(primarySensor, mockingPrimaryPath, primaryWindowDimension);
        verify(configuration).extractPrototypes(secondarySensor, mockingSecondaryPath, secondaryWindowDimension);
        verifyNoMoreInteractions(configuration);
    }

    private Sensor createSensor(String name, boolean isPrimary) {
        final Sensor primarySensor = new Sensor();
        primarySensor.setPrimary(isPrimary);
        primarySensor.setName(name);
        return primarySensor;
    }
}


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


import static org.mockito.Mockito.*;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.matchup.MatchupToolUseCaseConfigBuilder;
import org.junit.*;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFileWriter;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

public class AbstractMmdWriterTest {

    @Test
    public void testCreateUseCaseAttributesGroupInMmdFile() throws Exception {
        final Sensor primarySensor = new Sensor("SensorName2");
        primarySensor.setPrimary(true);
        final List<Sensor> sensorList = Arrays.asList(
                primarySensor,
                new Sensor("SensorName1"),
                new Sensor("SensorName3")
        );

        final UseCaseConfig useCaseConfig = new MatchupToolUseCaseConfigBuilder("NameOfTheUseCase")
                .withTimeDeltaSeconds(234, null)
                .withMaxPixelDistanceKm(12.34f, null)
                .withSensors(sensorList)
                .withDimensions(Arrays.asList(
                        new Dimension("SensorName1", 1, 2),
                        new Dimension("SensorName2", 3, 4),
                        new Dimension("SensorName3", 5, 6)
                ))
                .createConfig();

        final NetcdfFileWriter mockWriter = mock(NetcdfFileWriter.class);

        //test
        AbstractMmdWriter.createUseCaseAttributes(mockWriter, useCaseConfig);

        //verification
        final String useCaseAttributeName = "use-case-configuration";
        final String expectedCommentText = "This MMD file is created based on the use case configuration " +
                "documented in the attribute '" + useCaseAttributeName + "'.";
        verify(mockWriter).addGroupAttribute(isNull(Group.class), eq(new Attribute("comment", expectedCommentText)));
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        useCaseConfig.store(outputStream);
        verify(mockWriter).addGroupAttribute(isNull(Group.class), eq(new Attribute(useCaseAttributeName, outputStream.toString())));
        verify(mockWriter).addGroupAttribute(isNull(Group.class), eq(new Attribute("sensor-names", "SensorName2,SensorName1,SensorName3")));
        verifyNoMoreInteractions(mockWriter);
    }
}

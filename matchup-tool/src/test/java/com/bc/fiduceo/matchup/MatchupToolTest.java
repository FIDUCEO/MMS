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


import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.core.UseCaseConfigBuilder;
import com.bc.fiduceo.core.ValidationResult;
import com.bc.fiduceo.db.QueryParameter;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.tool.ToolContext;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

public class MatchupToolTest {

    private String ls;

    @Before
    public void SetUp() {
        ls = System.lineSeparator();
    }

    @Test
    public void testPrintUsageTo() {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final MatchupTool matchupTool = new MatchupTool();

        matchupTool.printUsageTo(outputStream);

        assertEquals("matchup-tool version 1.0.3" + ls +
                ls +
                "usage: matchup-tool <options>" + ls +
                "Valid options are:" + ls +
                "   -c,--config <arg>           Defines the configuration directory. Defaults to './config'." + ls +
                "   -end,--end-time <arg>       Defines the processing end-date, format 'yyyy-DDD'" + ls +
                "   -h,--help                   Prints the tool usage." + ls +
                "   -start,--start-time <arg>   Defines the processing start-date, format 'yyyy-DDD'" + ls +
                "   -u,--usecase <arg>          Defines the path to the use-case configuration file. Path is relative to the" + ls +
                "                               configuration directory." + ls, outputStream.toString());
    }

    @Test
    public void testGetOptions() {
        final Options options = MatchupTool.getOptions();
        assertNotNull(options);

        final Option helpOption = options.getOption("h");
        assertNotNull(helpOption);
        assertEquals("h", helpOption.getOpt());
        assertEquals("help", helpOption.getLongOpt());
        assertEquals("Prints the tool usage.", helpOption.getDescription());
        assertFalse(helpOption.hasArg());

        final Option configOption = options.getOption("config");
        assertNotNull(configOption);
        assertEquals("c", configOption.getOpt());
        assertEquals("config", configOption.getLongOpt());
        assertEquals("Defines the configuration directory. Defaults to './config'.", configOption.getDescription());
        assertTrue(configOption.hasArg());

        final Option startOption = options.getOption("start");
        assertNotNull(startOption);
        assertEquals("start", startOption.getOpt());
        assertEquals("start-time", startOption.getLongOpt());
        assertEquals("Defines the processing start-date, format 'yyyy-DDD'", startOption.getDescription());
        assertTrue(startOption.hasArg());

        final Option endOption = options.getOption("end");
        assertNotNull(endOption);
        assertEquals("end", endOption.getOpt());
        assertEquals("end-time", endOption.getLongOpt());
        assertEquals("Defines the processing end-date, format 'yyyy-DDD'", endOption.getDescription());
        assertTrue(endOption.hasArg());

        final Option useCaseOption = options.getOption("usecase");
        assertNotNull(useCaseOption);
        assertEquals("u", useCaseOption.getOpt());
        assertEquals("usecase", useCaseOption.getLongOpt());
        assertEquals("Defines the path to the use-case configuration file. Path is relative to the configuration directory.", useCaseOption.getDescription());
        assertTrue(useCaseOption.hasArg());
    }

    @Test
    public void testGetEndDate() {
        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("end")).thenReturn("1998-345");

        final Date endDate = MatchupTool.getEndDate(commandLine);
        TestUtil.assertCorrectUTCDate(1998, 12, 11, 23, 59, 59, 999, endDate);
    }

    @Test
    public void testGetEndDate_missingValue() {
        final CommandLine commandLine = mock(CommandLine.class);

        try {
            MatchupTool.getEndDate(commandLine);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetStartDate() {
        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("start")).thenReturn("1999-346");

        final Date startDate = MatchupTool.getStartDate(commandLine);
        TestUtil.assertCorrectUTCDate(1999, 12, 12, 0, 0, 0, 0, startDate);
    }

    @Test
    public void testGetStartDate_missingValue() {
        final CommandLine commandLine = mock(CommandLine.class);

        try {
            MatchupTool.getStartDate(commandLine);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetPrimarySensorParameter() {
        final ToolContext context = new ToolContext();
        context.setStartDate(TimeUtils.parseDOYBeginOfDay("2002-23"));
        context.setEndDate(TimeUtils.parseDOYEndOfDay("2002-23"));

        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor sensor = new Sensor("amsub-n16");
        sensor.setPrimary(true);
        sensorList.add(sensor);
        final UseCaseConfig useCaseConfig = UseCaseConfigBuilder.build("name")
                .withSensors(sensorList)
                .createConfig();
        context.setUseCaseConfig(useCaseConfig);

        final QueryParameter parameter = MatchupTool.getPrimarySensorParameter(context);
        assertNotNull(parameter);
        assertEquals("amsub-n16", parameter.getSensorName());
        assertNull(parameter.getVersion());
        TestUtil.assertCorrectUTCDate(2002, 1, 23, 0, 0, 0, 0, parameter.getStartTime());
        TestUtil.assertCorrectUTCDate(2002, 1, 23, 23, 59, 59, 999, parameter.getStopTime());
    }

    @Test
    public void testGetPrimarySensorParameter_withDataVersion() {
        final ToolContext context = new ToolContext();

        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor sensor = new Sensor("amsub-n16");
        sensor.setPrimary(true);
        sensor.setDataVersion("v23.5");
        sensorList.add(sensor);
        final UseCaseConfig useCaseConfig = UseCaseConfigBuilder.build("name")
                .withSensors(sensorList)
                .createConfig();
        context.setUseCaseConfig(useCaseConfig);

        final QueryParameter parameter = MatchupTool.getPrimarySensorParameter(context);
        assertNotNull(parameter);
        assertEquals("amsub-n16", parameter.getSensorName());
        assertEquals("v23.5", parameter.getVersion());
    }

    @Test
    public void testGetPrimarySensorParameter_missingPrimarySensor() {
        final ToolContext context = new ToolContext();

        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor sensor = new Sensor("amsub-n16");
        sensorList.add(sensor);

        final UseCaseConfig useCaseConfig = UseCaseConfigBuilder.build("testName")
                .withSensors(sensorList)
                .createConfig();
        context.setUseCaseConfig(useCaseConfig);

        try {
            MatchupTool.getPrimarySensorParameter(context);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetPixelLocator_notSegmented() throws Exception {
        final Reader reader = mock(Reader.class);
        final PixelLocator locator = mock(PixelLocator.class);
        when(reader.getPixelLocator()).thenReturn(locator);
        final Polygon polygon = mock(Polygon.class);
        final boolean segmented = false;

        final PixelLocator pixelLocator = MatchupTool.getPixelLocator(reader, segmented, polygon);

        verify(reader, times(1)).getPixelLocator();
        verifyNoMoreInteractions(reader);
        verifyNoMoreInteractions(polygon);
        assertNotNull(pixelLocator);
        assertSame(locator, pixelLocator);
    }

    @Test
    public void testGetPixelLocator_segmented() throws Exception {
        final Reader reader = mock(Reader.class);
        final PixelLocator locator = mock(PixelLocator.class);
        final Polygon polygon = mock(Polygon.class);
        when(reader.getSubScenePixelLocator(polygon)).thenReturn(locator);
        final boolean segmented = true;

        final PixelLocator pixelLocator = MatchupTool.getPixelLocator(reader, segmented, polygon);

        verify(reader, times(1)).getSubScenePixelLocator(same(polygon));
        verifyNoMoreInteractions(reader);
        verifyNoMoreInteractions(polygon);
        assertNotNull(pixelLocator);
        assertSame(locator, pixelLocator);
    }

    @Test
    public void testIsSegmented() throws Exception {
        final GeometryCollection collection = mock(GeometryCollection.class);

        when(collection.getGeometries()).thenReturn(new Geometry[1]);
        assertEquals(false, MatchupTool.isSegmented(collection));

        when(collection.getGeometries()).thenReturn(new Geometry[2]);
        assertEquals(true, MatchupTool.isSegmented(collection));

        verify(collection, times(2)).getGeometries();
        verifyNoMoreInteractions(collection);
    }

    @Test
    public void testGetSecondarySensor() {
        final UseCaseConfig config = mock(UseCaseConfig.class);

        final List<Sensor> additionalSensors = new ArrayList<>();
        additionalSensors.add(new Sensor("nasenmann"));
        when(config.getAdditionalSensors()).thenReturn(additionalSensors);

        final Sensor secondarySensor = MatchupTool.getSecondarySensor(config);
        assertNotNull(secondarySensor);
        assertEquals("nasenmann", secondarySensor.getName());
    }

    @Test
    public void testGetSecondarySensor_emptyList() {
        final UseCaseConfig config = mock(UseCaseConfig.class);

        final List<Sensor> additionalSensors = new ArrayList<>();
        when(config.getAdditionalSensors()).thenReturn(additionalSensors);

        try {
            MatchupTool.getSecondarySensor(config);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetSecondarySensorParameter() {
        final UseCaseConfig config = mock(UseCaseConfig.class);

        final List<Sensor> additionalSensors = new ArrayList<>();
        additionalSensors.add(new Sensor("the sensor"));
        when(config.getAdditionalSensors()).thenReturn(additionalSensors);

        final Date startDate = TimeUtils.parseDOYBeginOfDay("1997-34");
        final Date endDate = TimeUtils.parseDOYEndOfDay("1997-34");

        final QueryParameter parameter = MatchupTool.getSecondarySensorParameter(config, startDate, endDate);
        assertNotNull(parameter);
        assertEquals("the sensor", parameter.getSensorName());
        assertNull(parameter.getVersion());
        TestUtil.assertCorrectUTCDate(1997, 2, 3, 0, 0, 0, parameter.getStartTime());
        TestUtil.assertCorrectUTCDate(1997, 2, 3, 23, 59, 59, parameter.getStopTime());
    }

    @Test
    public void testGetSecondarySensorParameter_withDataVersion() {
        final UseCaseConfig config = mock(UseCaseConfig.class);

        final List<Sensor> additionalSensors = new ArrayList<>();
        additionalSensors.add(new Sensor("the sensor", "version_string"));
        when(config.getAdditionalSensors()).thenReturn(additionalSensors);

        final Date startDate = TimeUtils.parseDOYBeginOfDay("1997-35");
        final Date endDate = TimeUtils.parseDOYEndOfDay("1997-35");

        final QueryParameter parameter = MatchupTool.getSecondarySensorParameter(config, startDate, endDate);
        assertNotNull(parameter);
        assertEquals("the sensor", parameter.getSensorName());
        assertEquals("version_string", parameter.getVersion());
        TestUtil.assertCorrectUTCDate(1997, 2, 4, 0, 0, 0, parameter.getStartTime());
        TestUtil.assertCorrectUTCDate(1997, 2, 4, 23, 59, 59, parameter.getStopTime());
    }

    @Test
    public void testCreateErrorMessage_noErrors() {
        final StringBuilder errorMessage = MatchupTool.createErrorMessage(new ValidationResult());
        assertEquals("", errorMessage.toString());
    }

    @Test
    public void testCreateErrorMessage_oneError() {
        final ValidationResult validationResult = new ValidationResult();
        validationResult.getMessages().add("error happened, woho");

        final StringBuilder errorMessage = MatchupTool.createErrorMessage(validationResult);
        assertEquals("error happened, woho\n", errorMessage.toString());
    }

    @Test
    public void testCreateErrorMessage_twoErrors() {
        final ValidationResult validationResult = new ValidationResult();
        validationResult.getMessages().add("error happened, woho");
        validationResult.getMessages().add("phew, another one");

        final StringBuilder errorMessage = MatchupTool.createErrorMessage(validationResult);
        assertEquals("error happened, woho\nphew, another one\n", errorMessage.toString());
    }

    @Test
    public void testCalculateDistance() {
        final MatchupSet matchupSet = new MatchupSet();
        final SampleSet first = new SampleSet();
        first.setPrimary(new Sample(3, 4, -128.446, -38.056, 5));
        first.setSecondary(new Sample(6, 7, -128.438, -38.062, 8));

        final SampleSet second = new SampleSet();
        second.setPrimary(new Sample(9, 10, 55.306, 1.0887, 11));
        second.setSecondary(new Sample(12, 13, 55.299, 1.092, 14));

        matchupSet.getSampleSets().add(first);
        matchupSet.getSampleSets().add(second);

        MatchupTool.calculateDistance(matchupSet);

        SampleSet sampleSet = matchupSet.getSampleSets().get(0);
        assertEquals(0.9673157930374146f, sampleSet.getSphericalDistance(), 1e-8);

        sampleSet = matchupSet.getSampleSets().get(1);
        assertEquals(0.8603944182395935f, sampleSet.getSphericalDistance(), 1e-8);
    }
}

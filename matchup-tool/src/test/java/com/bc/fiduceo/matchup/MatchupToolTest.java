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


import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.core.UseCaseConfigBuilder;
import com.bc.fiduceo.core.ValidationResult;
import com.bc.fiduceo.matchup.writer.IOVariable;
import com.bc.fiduceo.matchup.writer.IOVariablesList;
import com.bc.fiduceo.matchup.writer.ReaderContainer;
import com.bc.fiduceo.matchup.writer.VariablesConfiguration;
import com.bc.fiduceo.matchup.writer.WindowReadingIOVariable;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_UNITS_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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

        assertEquals("matchup-tool version 1.5.7" + ls +
                ls +
                "usage: matchup-tool <options>" + ls +
                "Valid options are:" + ls +
                "   -c,--config <arg>           Defines the configuration directory. Defaults to './config'." + ls +
                "   -end,--end-date <arg>       Defines the processing end-date, format 'yyyy-DDD'" + ls +
                "   -h,--help                   Prints the tool usage." + ls +
                "   -start,--start-date <arg>   Defines the processing start-date, format 'yyyy-DDD'" + ls +
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
        assertEquals("start-date", startOption.getLongOpt());
        assertEquals("Defines the processing start-date, format 'yyyy-DDD'", startOption.getDescription());
        assertTrue(startOption.hasArg());

        final Option endOption = options.getOption("end");
        assertNotNull(endOption);
        assertEquals("end", endOption.getOpt());
        assertEquals("end-date", endOption.getLongOpt());
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
    public void testGetVariable() {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setSourceVariableName("the_source_name");
        ioVariable.setTargetVariableName("we_don_t_care");

        final List<IOVariable> ioVariables = new ArrayList<>();
        ioVariables.add(ioVariable);

        final IOVariable resultVariable = MatchupTool.getVariable("the_source_name", ioVariables);
        assertNotNull(resultVariable);
        assertEquals("the_source_name", resultVariable.getSourceVariableName());
    }

    @Test
    public void testGetVariable_notPresentInList() {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setSourceVariableName("the_source_name");
        ioVariable.setTargetVariableName("we_don_t_care");

        final List<IOVariable> ioVariables = new ArrayList<>();
        ioVariables.add(ioVariable);

        final IOVariable resultVariable = MatchupTool.getVariable("this-does-not-exist", ioVariables);
        assertNull(resultVariable);
    }

    @Test
    public void testApplyExcludesAndRenames_emptyConfig() {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setSourceVariableName("the_source_name");
        ioVariable.setTargetVariableName("the_wrong_name");

        final IOVariablesList ioVariablesList = new IOVariablesList(null);// we don't need a ReaderFactory for this test tb 2016-10-05
        ioVariablesList.add(ioVariable, "a_sensor");

        final VariablesConfiguration variablesConfiguration = new VariablesConfiguration();

        MatchupTool.applyExcludesAndRenames(ioVariablesList, variablesConfiguration);

        final List<IOVariable> ioVariables = ioVariablesList.getVariablesFor("a_sensor");
        assertEquals(1, ioVariables.size());
        assertEquals("the_wrong_name", ioVariables.get(0).getTargetVariableName());
    }

    @Test
    public void testApplyExcludesAndRenames_rename() {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setSourceVariableName("the_source_name");
        ioVariable.setTargetVariableName("the_wrong_name");

        final IOVariablesList ioVariablesList = new IOVariablesList(null);// we don't need a ReaderFactory for this test tb 2016-10-05
        ioVariablesList.add(ioVariable, "another_sensor");

        final VariablesConfiguration variablesConfiguration = new VariablesConfiguration();
        final Map<String, String> renamesMap = new HashMap<>();
        renamesMap.put("the_source_name", "correct_name");
        variablesConfiguration.addRenames("another_sensor", renamesMap);

        MatchupTool.applyExcludesAndRenames(ioVariablesList, variablesConfiguration);

        final List<IOVariable> ioVariables = ioVariablesList.getVariablesFor("another_sensor");
        assertEquals(1, ioVariables.size());
        assertEquals("correct_name", ioVariables.get(0).getTargetVariableName());
    }

    @Test
    public void testApplyExcludesAndRenames_exclude() {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setSourceVariableName("the_source_name");
        ioVariable.setTargetVariableName("we_don_t_care");

        final WindowReadingIOVariable remove_ioVariable = new WindowReadingIOVariable(null);
        remove_ioVariable.setSourceVariableName("kick_me_off");
        remove_ioVariable.setTargetVariableName("we_don_t_care");

        final IOVariablesList ioVariablesList = new IOVariablesList(null);// we don't need a ReaderFactory for this test tb 2016-10-05
        ioVariablesList.add(remove_ioVariable, "the_sensor");
        ioVariablesList.add(ioVariable, "the_sensor");

        final VariablesConfiguration variablesConfiguration = new VariablesConfiguration();
        final ArrayList<String> excludeList = new ArrayList<>();
        excludeList.add("kick_me_off");
        variablesConfiguration.addExcludes("the_sensor", excludeList);

        MatchupTool.applyExcludesAndRenames(ioVariablesList, variablesConfiguration);

        final List<IOVariable> ioVariables = ioVariablesList.getVariablesFor("the_sensor");
        assertEquals(1, ioVariables.size());
        assertEquals("the_source_name", ioVariables.get(0).getSourceVariableName());
    }

    @Test
    public void testCreateIOVariablesPerSensor() throws Exception {
        //preparation
        final String primarySensorName = "avhrr-n17";
        final String secondarySensorName = "avhrr-n18";
        final Sensor primarySensor = createSensor(primarySensorName, true);
        final Sensor secondarySensor = createSensor(secondarySensorName, false);
        final Dimension primaryWindowDimension = new Dimension(primarySensorName, 5, 4);
        final Dimension secondaryWindowDimension = new Dimension(secondarySensorName, 5, 4);
        final Path mockingPrimaryPath = Paths.get("mockingPrimaryPath");
        final Path mockingSecondaryPath = Paths.get("mockingSecondaryPath");

        final UseCaseConfig useCaseConfig = UseCaseConfigBuilder.build("testName")
                .withDimensions(Arrays.asList(primaryWindowDimension, secondaryWindowDimension))
                .withSensors(Arrays.asList(primarySensor, secondarySensor))
                .createConfig();

        final MatchupSet matchupSet = new MatchupSet();
        matchupSet.setPrimaryObservationPath(mockingPrimaryPath);
        matchupSet.setSecondaryObservationPath(secondarySensorName, mockingSecondaryPath);

        final MatchupCollection matchupCollection = new MatchupCollection();
        matchupCollection.add(matchupSet);

        final WindowReadingIOVariable ioVariable = mock(WindowReadingIOVariable.class);
        final List<IOVariable> ioVariables = Arrays.asList(ioVariable, ioVariable);

        final IOVariablesList ioVariablesList = mock(IOVariablesList.class);
        when(ioVariablesList.get()).thenReturn(ioVariables);

        final VariablesConfiguration variablesConfiguration = new VariablesConfiguration();

        // test execution
        MatchupTool.createIOVariablesPerSensor(ioVariablesList, matchupCollection, useCaseConfig, variablesConfiguration);

        // validation
        verify(ioVariablesList, times(1)).extractVariables(eq(primarySensorName), eq(mockingPrimaryPath), eq(primaryWindowDimension), eq(variablesConfiguration));
        verify(ioVariablesList, times(1)).extractVariables(eq(secondarySensorName), eq(mockingSecondaryPath), eq(secondaryWindowDimension), eq(variablesConfiguration));
        verify(ioVariablesList, times(5)).add(any(IOVariable.class), eq(primarySensorName));
        verify(ioVariablesList, times(5)).add(any(IOVariable.class), eq(secondarySensorName));
        verify(ioVariablesList, times(1)).getReaderContainer(primarySensorName);
        verify(ioVariablesList, times(1)).getReaderContainer(secondarySensorName);
        verifyNoMoreInteractions(ioVariablesList);

        verifyNoMoreInteractions(ioVariable);
    }

    @Test
    public void testCreateExtraVariables() {
        final String sensorName = "sensorName";
        final IOVariablesList ioVariablesList = new IOVariablesList(null);
        ioVariablesList.setReaderContainer(sensorName, new ReaderContainer());

        MatchupTool.createExtraVariables(sensorName, ioVariablesList, new VariablesConfiguration());

        final List<IOVariable> ioVariables = ioVariablesList.get();
        assertEquals(5, ioVariables.size());

        IOVariable variable;
        List<Attribute> attributes;
        Attribute attribute;

        variable = ioVariables.get(0);
        assertEquals("sensorName_x", variable.getTargetVariableName());
        assertEquals("int", variable.getDataType());
        assertEquals(FiduceoConstants.MATCHUP_COUNT, variable.getDimensionNames());
        attributes = variable.getAttributes();
        assertEquals(2, attributes.size());
        attribute = attributes.get(0);
        assertEquals("description", attribute.getShortName());
        assertEquals("pixel original x location in satellite raster", attribute.getStringValue());
        attribute = attributes.get(1);
        assertEquals(CF_FILL_VALUE_NAME, attribute.getShortName());
        assertEquals(-2147483647, attribute.getNumericValue());

        variable = ioVariables.get(1);
        assertEquals("sensorName_y", variable.getTargetVariableName());
        assertEquals("int", variable.getDataType());
        assertEquals(FiduceoConstants.MATCHUP_COUNT, variable.getDimensionNames());
        attributes = variable.getAttributes();
        assertEquals(2, attributes.size());
        attribute = attributes.get(0);
        assertEquals("description", attribute.getShortName());
        assertEquals("pixel original y location in satellite raster", attribute.getStringValue());
        attribute = attributes.get(1);
        assertEquals(CF_FILL_VALUE_NAME, attribute.getShortName());
        assertEquals(-2147483647, attribute.getNumericValue());

        variable = ioVariables.get(2);
        assertEquals("sensorName_file_name", variable.getTargetVariableName());
        assertEquals("char", variable.getDataType());
        assertEquals("matchup_count file_name", variable.getDimensionNames());
        attributes = variable.getAttributes();
        assertEquals(1, attributes.size());
        attribute = attributes.get(0);
        assertEquals("description", attribute.getShortName());
        assertEquals("file name of the original data file", attribute.getStringValue());

        variable = ioVariables.get(3);
        assertEquals("sensorName_processing_version", variable.getTargetVariableName());
        assertEquals("char", variable.getDataType());
        assertEquals("matchup_count processing_version", variable.getDimensionNames());
        attributes = variable.getAttributes();
        assertEquals(1, attributes.size());
        attribute = attributes.get(0);
        assertEquals("description", attribute.getShortName());
        assertEquals("the processing version of the original data file", attribute.getStringValue());

        variable = ioVariables.get(4);
        assertEquals("sensorName_acquisition_time", variable.getTargetVariableName());
        assertEquals("int", variable.getDataType());
        assertEquals("matchup_count sensorName_ny sensorName_nx", variable.getDimensionNames());
        attributes = variable.getAttributes();
        assertEquals(3, attributes.size());
        attribute = attributes.get(0);
        assertEquals("description", attribute.getShortName());
        assertEquals("acquisition time of original pixel", attribute.getStringValue());
        attribute = attributes.get(1);
        assertEquals(CF_UNITS_NAME, attribute.getShortName());
        assertEquals("seconds since 1970-01-01", attribute.getStringValue());
        attribute = attributes.get(2);
        assertEquals(CF_FILL_VALUE_NAME, attribute.getShortName());
        assertEquals(DataType.INT, attribute.getDataType());
        assertEquals(1, attribute.getValues().getSize());
        assertEquals(-2147483647, attribute.getValues().getInt(0));
    }

    @Test
    public void testCreateExtraVariables_sensorRename() {
        final String sensorName = "sensorName";
        final IOVariablesList ioVariablesList = new IOVariablesList(null);
        ioVariablesList.setReaderContainer(sensorName, new ReaderContainer());
        final VariablesConfiguration configuration = new VariablesConfiguration();
        configuration.addSensorRename(sensorName, "renamed");

        MatchupTool.createExtraVariables(sensorName, ioVariablesList, configuration);

        final List<IOVariable> ioVariables = ioVariablesList.get();
        assertEquals(5, ioVariables.size());

        final IOVariable xVariable = ioVariables.get(0);
        assertEquals("renamed_x", xVariable.getTargetVariableName());

        final IOVariable yVariable = ioVariables.get(1);
        assertEquals("renamed_y", yVariable.getTargetVariableName());

        final IOVariable fileNameVariable = ioVariables.get(2);
        assertEquals("renamed_file_name", fileNameVariable.getTargetVariableName());

        final IOVariable versionVariable = ioVariables.get(3);
        assertEquals("renamed_processing_version", versionVariable.getTargetVariableName());

        final IOVariable timeVariable = ioVariables.get(4);
        assertEquals("renamed_acquisition_time", timeVariable.getTargetVariableName());
    }

    @Test
    public void testCreateExtraVariables_variableRenaming() {
        final String sensorName = "sensorName";
        final IOVariablesList ioVariablesList = new IOVariablesList(null);
        ioVariablesList.setReaderContainer(sensorName, new ReaderContainer());
        final VariablesConfiguration configuration = new VariablesConfiguration();
        final HashMap<String, String> renames = new HashMap<>();
        renames.put("x", "XcenterX");
        renames.put("y", "YcenterY");
        renames.put(FiduceoConstants.FILE_NAME, "SourceProductFileName");
        renames.put("processing_version", "versionOfProcessing");
        renames.put("acquisition_time", "time");
        configuration.addRenames(sensorName, renames);

        MatchupTool.createExtraVariables(sensorName, ioVariablesList, configuration);

        final List<IOVariable> ioVariables = ioVariablesList.get();
        assertEquals(5, ioVariables.size());

        final IOVariable xVariable = ioVariables.get(0);
        assertEquals("sensorName_XcenterX", xVariable.getTargetVariableName());

        final IOVariable yVariable = ioVariables.get(1);
        assertEquals("sensorName_YcenterY", yVariable.getTargetVariableName());

        final IOVariable pathVariable = ioVariables.get(2);
        assertEquals("sensorName_SourceProductFileName", pathVariable.getTargetVariableName());

        final IOVariable versionVariable = ioVariables.get(3);
        assertEquals("sensorName_versionOfProcessing", versionVariable.getTargetVariableName());

        final IOVariable timeVariable = ioVariables.get(4);
        assertEquals("sensorName_time", timeVariable.getTargetVariableName());
    }

    @Test
    public void testCreateExtraVariables_AttibuteRenaming() {
        final String sensorName = "sensorName";
        final IOVariablesList ioVariablesList = new IOVariablesList(null);
        ioVariablesList.setReaderContainer(sensorName, new ReaderContainer());
        final VariablesConfiguration configuration = new VariablesConfiguration();
        configuration.setAttributeRename(sensorName, "x", "description", "desc_r");
        configuration.setAttributeRename(sensorName, FiduceoConstants.FILE_NAME, "description", "desc_r");
        configuration.setAttributeRename(sensorName, "processing_version", "description", "desc_r_2");
        configuration.setAttributeRename(sensorName, "acquisition_time", CF_UNITS_NAME, "unit_r");
        configuration.setAttributeRename(sensorName, null, CF_FILL_VALUE_NAME, "_fill_value");

        MatchupTool.createExtraVariables(sensorName, ioVariablesList, configuration);

        final List<IOVariable> ioVariables = ioVariablesList.get();
        assertEquals(5, ioVariables.size());

        final IOVariable xVariable = ioVariables.get(0);
        assertEquals(2, xVariable.getAttributes().size());
        assertEquals("desc_r", xVariable.getAttributes().get(0).getShortName());
        assertEquals(CF_FILL_VALUE_NAME, xVariable.getAttributes().get(1).getShortName());

        final IOVariable yVariable = ioVariables.get(1);
        assertEquals(2, yVariable.getAttributes().size());
        assertEquals("description", yVariable.getAttributes().get(0).getShortName());
        assertEquals(CF_FILL_VALUE_NAME, yVariable.getAttributes().get(1).getShortName());

        final IOVariable pathVariable = ioVariables.get(2);
        assertEquals(1, pathVariable.getAttributes().size());
        assertEquals("desc_r", pathVariable.getAttributes().get(0).getShortName());

        final IOVariable versionVariable = ioVariables.get(3);
        assertEquals(1, versionVariable.getAttributes().size());
        assertEquals("desc_r_2", versionVariable.getAttributes().get(0).getShortName());

        final IOVariable timeVariable = ioVariables.get(4);
        assertEquals(3, timeVariable.getAttributes().size());
        assertEquals("description", timeVariable.getAttributes().get(0).getShortName());
        assertEquals("unit_r", timeVariable.getAttributes().get(1).getShortName());
        assertEquals("_fill_value", timeVariable.getAttributes().get(2).getShortName());
    }

    @Test
    public void testCreateExtraVariables_excludes___x_y_fileName_aquisitionTime() {
        final String sensorName = "sensorName";
        final IOVariablesList ioVariablesList = new IOVariablesList(null);
        ioVariablesList.setReaderContainer(sensorName, new ReaderContainer());
        final VariablesConfiguration configuration = new VariablesConfiguration();
        configuration.addExcludes(sensorName, Arrays.asList("x", "y", FiduceoConstants.FILE_NAME, FiduceoConstants.PROCESSING_VERSION, "acquisition_time"));

        MatchupTool.createExtraVariables(sensorName, ioVariablesList, configuration);

        final List<IOVariable> ioVariables = ioVariablesList.get();
        assertEquals(0, ioVariables.size());
    }

    @Test
    public void testGetFirstMatchupSet_emptyList() {
        final MatchupCollection matchupCollection = new MatchupCollection();

        try {
            MatchupTool.getFirstMatchupSet(matchupCollection);
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

        final MatchupSet set = MatchupTool.getFirstMatchupSet(collection);

        assertSame(first, set);
    }

    @Test
    public void testCreateDistanceVariableName_noRenames() {
        final VariablesConfiguration variablesConfiguration = new VariablesConfiguration();
        final String primary = "primus";
        final String secondary = "sick";

        final String distanceVariableName = MatchupTool.createDistanceVariableName(variablesConfiguration, primary, secondary);
        assertEquals("primus_sick_matchup_spherical_distance", distanceVariableName);
    }

    @Test
    public void testCreateDistanceVariableName_primaryRename() {
        final VariablesConfiguration variablesConfiguration = new VariablesConfiguration();
        final String primary = "prince";
        final String secondary = "sick";

        variablesConfiguration.addSensorRename(primary, "renamed_wurst");

        final String distanceVariableName = MatchupTool.createDistanceVariableName(variablesConfiguration, primary, secondary);
        assertEquals("renamed_wurst_sick_matchup_spherical_distance", distanceVariableName);
    }

    @Test
    public void testCreateDistanceVariableName_secondaryRename() {
        final VariablesConfiguration variablesConfiguration = new VariablesConfiguration();
        final String primary = "prince";
        final String secondary = "sick";

        variablesConfiguration.addSensorRename(secondary, "the-seco-one");

        final String distanceVariableName = MatchupTool.createDistanceVariableName(variablesConfiguration, primary, secondary);
        assertEquals("prince_the-seco-one_matchup_spherical_distance", distanceVariableName);
    }

    private Sensor createSensor(String name, boolean isPrimary) {
        final Sensor primarySensor = new Sensor();
        primarySensor.setPrimary(isPrimary);
        primarySensor.setName(name);
        return primarySensor;
    }
}

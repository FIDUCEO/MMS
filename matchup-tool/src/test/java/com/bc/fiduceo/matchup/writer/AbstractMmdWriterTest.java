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


import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.core.UseCaseConfigBuilder;
import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.MatchupToolUseCaseConfigBuilder;
import com.bc.fiduceo.tool.ToolContext;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFileWriter;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.*;

public class AbstractMmdWriterTest {

    private static final String fillValueName = "_FillValue";

    @Test
    public void testCreateUseCaseAttributesGroupInMmdFile() throws Exception {
        final Sensor primarySensor = new Sensor("SensorName1");
        primarySensor.setPrimary(true);
        final List<Sensor> sensorList = Arrays.asList(
                primarySensor,
                new Sensor("SensorName2"),
                new Sensor("SensorName3")
        );

        final UseCaseConfig useCaseConfig = new MatchupToolUseCaseConfigBuilder("NameOfTheUseCase")
                .withTimeDeltaSeconds(234)
                .withMaxPixelDistanceKm(12.34f)
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
        verifyNoMoreInteractions(mockWriter);
    }


    @Test
    public void testGetFirstMatchupSet_emptyList() {
        final MatchupCollection matchupCollection = new MatchupCollection();

        try {
            AbstractMmdWriter.getFirstMatchupSet(matchupCollection);
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

        final MatchupSet set = AbstractMmdWriter.getFirstMatchupSet(collection);

        assertSame(first, set);
    }

    @Test
    public void testEnsureFillValue_Double() throws Exception {
        final IOVariable ioVariable = new IOVariable();
        ioVariable.setDataType(DataType.DOUBLE.name());

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(Double.MIN_VALUE, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Float() throws Exception {
        final IOVariable ioVariable = new IOVariable();
        ioVariable.setDataType(DataType.FLOAT.name());

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(Float.MIN_VALUE, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Long() throws Exception {
        final IOVariable ioVariable = new IOVariable();
        ioVariable.setDataType(DataType.LONG.name());

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(Long.MIN_VALUE, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Integer() throws Exception {
        final IOVariable ioVariable = new IOVariable();
        ioVariable.setDataType(DataType.INT.name());

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(Integer.MIN_VALUE, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Short() throws Exception {
        final IOVariable ioVariable = new IOVariable();
        ioVariable.setDataType(DataType.SHORT.name());

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(Short.MIN_VALUE, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Byte() throws Exception {
        final IOVariable ioVariable = new IOVariable();
        ioVariable.setDataType(DataType.BYTE.name());

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(Byte.MIN_VALUE, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Double_existing() throws Exception {
        final IOVariable ioVariable = new IOVariable();
        ioVariable.setDataType(DataType.DOUBLE.name());
        final double fillValue = 1234.5678;
        ioVariable.setAttributes(Collections.singletonList(new Attribute(fillValueName, fillValue)));

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(fillValue, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Float_existing() throws Exception {
        final IOVariable ioVariable = new IOVariable();
        ioVariable.setDataType(DataType.FLOAT.name());
        final float fillValue = 1234.5678f;
        ioVariable.setAttributes(Collections.singletonList(new Attribute(fillValueName, fillValue)));

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(fillValue, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Long_existing() throws Exception {
        final IOVariable ioVariable = new IOVariable();
        ioVariable.setDataType(DataType.LONG.name());
        final long fillValue = 12345678912345678L;
        ioVariable.setAttributes(Collections.singletonList(new Attribute(fillValueName, fillValue)));

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(fillValue, attribute.getValue(0));
    }

    @Test
    public void testEnsureFillValue_Integer_existing() throws Exception {
        final IOVariable ioVariable = new IOVariable();
        ioVariable.setDataType(DataType.INT.name());
        final int fillValue = 123456789;
        ioVariable.setAttributes(Collections.singletonList(new Attribute(fillValueName, fillValue)));

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(fillValue, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Short_existing() throws Exception {
        final IOVariable ioVariable = new IOVariable();
        ioVariable.setDataType(DataType.SHORT.name());
        final short fillValue = 12345;
        ioVariable.setAttributes(Collections.singletonList(new Attribute(fillValueName, fillValue)));

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(fillValue, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Byte_existing() throws Exception {
        final IOVariable ioVariable = new IOVariable();
        ioVariable.setDataType(DataType.BYTE.name());
        final byte fillValue = 123;
        ioVariable.setAttributes(Collections.singletonList(new Attribute(fillValueName, fillValue)));

        AbstractMmdWriter.ensureFillValue(ioVariable);

        final List<Attribute> attributes = ioVariable.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(fillValue, attribute.getNumericValue());
    }

    @Test
    public void testExtractPrototypes() throws Exception {
        //preparation
        final Sensor primarySensor = createSensor("avhrr-n17", true);
        final Sensor secondarySensor = createSensor("avhrr-n18", false);
        final Dimension primaryWindowDimension = new Dimension("avhrr-n17", 5, 4);
        final Dimension secondaryWindowDimension = new Dimension("avhrr-n18", 5, 4);
        final Path mockingPrimaryPath = Paths.get("mockingPrimaryPath");
        final Path mockingSecondaryPath = Paths.get("mockingSecondaryPath");

        final UseCaseConfig useCaseConfig = UseCaseConfigBuilder.build("testName")
                .withDimensions(Arrays.asList(primaryWindowDimension, secondaryWindowDimension))
                .withSensors(Arrays.asList(primarySensor, secondarySensor))
                .createConfig();

        final ToolContext toolContext = mock(ToolContext.class);
        when(toolContext.getUseCaseConfig()).thenReturn(useCaseConfig);

        final MatchupSet matchupSet = new MatchupSet();
        matchupSet.setPrimaryObservationPath(mockingPrimaryPath);
        matchupSet.setSecondaryObservationPath(mockingSecondaryPath);

        final MatchupCollection matchupCollection = new MatchupCollection();
        matchupCollection.add(matchupSet);

        final IOVariable ioVariable = mock(IOVariable.class);
        final List<IOVariable> ioVariables = Arrays.asList(ioVariable, ioVariable);

        final IOVariablesList configuration = mock(IOVariablesList.class);
        when(configuration.get()).thenReturn(ioVariables);

        final Target target = mock(Target.class);
        // test execution
        AbstractMmdWriter.extractPrototypes(configuration, matchupCollection, toolContext, target);

        // validation
        verify(configuration, times(1)).extractVariables(refEq(primarySensor), refEq(mockingPrimaryPath), refEq(primaryWindowDimension));
        verify(configuration, times(1)).extractVariables(refEq(secondarySensor), refEq(mockingSecondaryPath), refEq(secondaryWindowDimension));
        verify(configuration, times(1)).get();
        verifyNoMoreInteractions(configuration);

        verify(ioVariable, times(2)).setTarget(target);
        verifyNoMoreInteractions(ioVariable);

        verify(toolContext, times(1)).getUseCaseConfig();
        verifyNoMoreInteractions(toolContext);

        verifyNoMoreInteractions(target);
    }

    @Test
    public void testApplyExcludesAndRenames_emptyConfig() {
        final IOVariable ioVariable = new IOVariable();
        ioVariable.setSourceVariableName("the_source_name");
        ioVariable.setTargetVariableName("the_wrong_name");

        final IOVariablesList ioVariablesList = new IOVariablesList(null);// we don't need a ReaderFactory for this test tb 2016-10-05
        ioVariablesList.add(ioVariable, "a_sensor");

        final VariablesConfiguration variablesConfiguration = new VariablesConfiguration();

        AbstractMmdWriter.applyExcludesAndRenames(ioVariablesList, variablesConfiguration);

        final List<IOVariable> ioVariables = ioVariablesList.getVariablesFor("a_sensor");
        assertEquals(1, ioVariables.size());
        assertEquals("the_wrong_name", ioVariables.get(0).getTargetVariableName());
    }

    @Test
    public void testApplyExcludesAndRenames_rename() {
        final IOVariable ioVariable = new IOVariable();
        ioVariable.setSourceVariableName("the_source_name");
        ioVariable.setTargetVariableName("the_wrong_name");

        final IOVariablesList ioVariablesList = new IOVariablesList(null);// we don't need a ReaderFactory for this test tb 2016-10-05
        ioVariablesList.add(ioVariable, "another_sensor");

        final VariablesConfiguration variablesConfiguration = new VariablesConfiguration();
        final ArrayList<VariableRename> renamesList = new ArrayList<>();
        renamesList.add(new VariableRename("the_source_name", "correct_name"));
        variablesConfiguration.addRenames("another_sensor", renamesList);

        AbstractMmdWriter.applyExcludesAndRenames(ioVariablesList, variablesConfiguration);

        final List<IOVariable> ioVariables = ioVariablesList.getVariablesFor("another_sensor");
        assertEquals(1, ioVariables.size());
        assertEquals("correct_name", ioVariables.get(0).getTargetVariableName());
    }

    @Test
    public void testApplyExcludesAndRenames_exclude() {
        final IOVariable ioVariable = new IOVariable();
        ioVariable.setSourceVariableName("the_source_name");
        ioVariable.setTargetVariableName("we_don_t_care");

        final IOVariable remove_ioVariable = new IOVariable();
        remove_ioVariable.setSourceVariableName("kick_me_off");
        remove_ioVariable.setTargetVariableName("we_don_t_care");

        final IOVariablesList ioVariablesList = new IOVariablesList(null);// we don't need a ReaderFactory for this test tb 2016-10-05
        ioVariablesList.add(remove_ioVariable, "the_sensor");
        ioVariablesList.add(ioVariable, "the_sensor");

        final VariablesConfiguration variablesConfiguration = new VariablesConfiguration();
        final ArrayList<VariableExclude> excludeList = new ArrayList<>();
        excludeList.add(new VariableExclude("kick_me_off"));
        variablesConfiguration.addExcludes("the_sensor", excludeList);

        AbstractMmdWriter.applyExcludesAndRenames(ioVariablesList, variablesConfiguration);

        final List<IOVariable> ioVariables = ioVariablesList.getVariablesFor("the_sensor");
        assertEquals(1, ioVariables.size());
        assertEquals("the_source_name", ioVariables.get(0).getSourceVariableName());
    }

    @Test
    public void testGetPrototype() {
        final IOVariable ioVariable = new IOVariable();
        ioVariable.setSourceVariableName("the_source_name");
        ioVariable.setTargetVariableName("we_don_t_care");

        final List<IOVariable> ioVariables = new ArrayList<>();
        ioVariables.add(ioVariable);

        final IOVariable resultVariable = AbstractMmdWriter.getVariable("the_source_name", ioVariables);
        assertNotNull(resultVariable);
        assertEquals("the_source_name", resultVariable.getSourceVariableName());
    }

    @Test
    public void testGetPrototype_notPresentInList() {
        final IOVariable ioVariable = new IOVariable();
        ioVariable.setSourceVariableName("the_source_name");
        ioVariable.setTargetVariableName("we_don_t_care");

        final List<IOVariable> ioVariables = new ArrayList<>();
        ioVariables.add(ioVariable);

        final IOVariable resultVariable = AbstractMmdWriter.getVariable("this-does-not-exist", ioVariables);
        assertNull(resultVariable);
    }

    @Test
    public void testGetExclude() {
        final List<VariableExclude> excludes = new ArrayList<>();
        excludes.add(new VariableExclude("wrong"));
        excludes.add(new VariableExclude("remove"));

        final VariableExclude exclude = AbstractMmdWriter.getExclude("wrong", excludes);
        assertNotNull(exclude);
        assertEquals("wrong", exclude.getSourceName());
    }

    @Test
    public void testGetExclude_notPresent() {
        final List<VariableExclude> excludes = new ArrayList<>();
        excludes.add(new VariableExclude("yo"));
        excludes.add(new VariableExclude("man"));

        final VariableExclude exclude = AbstractMmdWriter.getExclude("not-there", excludes);
        assertNull(exclude);
    }

    @Test
    public void tstGetRename() {
        final List<VariableRename> renames = new ArrayList<>();
        renames.add(new VariableRename("bla", "blubb"));
        renames.add(new VariableRename("schnick", "schnack"));

        final VariableRename rename = AbstractMmdWriter.getRename("schnick", renames);
        assertNotNull(rename);
        assertEquals("schnick", rename.getSourceName());
    }

    @Test
    public void tstGetRename_notPresent() {
        final List<VariableRename> renames = new ArrayList<>();
        renames.add(new VariableRename("jekyll", "hyde"));
        renames.add(new VariableRename("dit", "dat"));

        final VariableRename rename = AbstractMmdWriter.getRename("herman", renames);
        assertNull(rename);
    }

    private Sensor createSensor(String name, boolean isPrimary) {
        final Sensor primarySensor = new Sensor();
        primarySensor.setPrimary(isPrimary);
        primarySensor.setName(name);
        return primarySensor;
    }
}

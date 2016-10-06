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
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setDataType(DataType.DOUBLE.name());

        AbstractMmdWriter.ensureFillValue(prototype);

        final List<Attribute> attributes = prototype.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(Double.MIN_VALUE, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Float() throws Exception {
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setDataType(DataType.FLOAT.name());

        AbstractMmdWriter.ensureFillValue(prototype);

        final List<Attribute> attributes = prototype.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(Float.MIN_VALUE, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Long() throws Exception {
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setDataType(DataType.LONG.name());

        AbstractMmdWriter.ensureFillValue(prototype);

        final List<Attribute> attributes = prototype.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(Long.MIN_VALUE, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Integer() throws Exception {
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setDataType(DataType.INT.name());

        AbstractMmdWriter.ensureFillValue(prototype);

        final List<Attribute> attributes = prototype.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(Integer.MIN_VALUE, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Short() throws Exception {
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setDataType(DataType.SHORT.name());

        AbstractMmdWriter.ensureFillValue(prototype);

        final List<Attribute> attributes = prototype.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(Short.MIN_VALUE, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Byte() throws Exception {
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setDataType(DataType.BYTE.name());

        AbstractMmdWriter.ensureFillValue(prototype);

        final List<Attribute> attributes = prototype.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(Byte.MIN_VALUE, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Double_existing() throws Exception {
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setDataType(DataType.DOUBLE.name());
        final double fillValue = 1234.5678;
        prototype.setAttributes(Collections.singletonList(new Attribute(fillValueName, fillValue)));

        AbstractMmdWriter.ensureFillValue(prototype);

        final List<Attribute> attributes = prototype.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(fillValue, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Float_existing() throws Exception {
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setDataType(DataType.FLOAT.name());
        final float fillValue = 1234.5678f;
        prototype.setAttributes(Collections.singletonList(new Attribute(fillValueName, fillValue)));

        AbstractMmdWriter.ensureFillValue(prototype);

        final List<Attribute> attributes = prototype.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(fillValue, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Long_existing() throws Exception {
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setDataType(DataType.LONG.name());
        final long fillValue = 12345678912345678L;
        prototype.setAttributes(Collections.singletonList(new Attribute(fillValueName, fillValue)));

        AbstractMmdWriter.ensureFillValue(prototype);

        final List<Attribute> attributes = prototype.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(fillValue, attribute.getValue(0));
    }

    @Test
    public void testEnsureFillValue_Integer_existing() throws Exception {
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setDataType(DataType.INT.name());
        final int fillValue = 123456789;
        prototype.setAttributes(Collections.singletonList(new Attribute(fillValueName, fillValue)));

        AbstractMmdWriter.ensureFillValue(prototype);

        final List<Attribute> attributes = prototype.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(fillValue, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Short_existing() throws Exception {
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setDataType(DataType.SHORT.name());
        final short fillValue = 12345;
        prototype.setAttributes(Collections.singletonList(new Attribute(fillValueName, fillValue)));

        AbstractMmdWriter.ensureFillValue(prototype);

        final List<Attribute> attributes = prototype.getAttributes();
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals(fillValueName, attribute.getShortName());
        assertEquals(fillValue, attribute.getNumericValue());
    }

    @Test
    public void testEnsureFillValue_Byte_existing() throws Exception {
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setDataType(DataType.BYTE.name());
        final byte fillValue = 123;
        prototype.setAttributes(Collections.singletonList(new Attribute(fillValueName, fillValue)));

        AbstractMmdWriter.ensureFillValue(prototype);

        final List<Attribute> attributes = prototype.getAttributes();
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

        final VariablePrototypeList configuration = mock(VariablePrototypeList.class);

        // test execution
        AbstractMmdWriter.extractPrototypes(configuration, matchupCollection, toolContext);

        // validation
        verify(configuration).extractPrototypes(refEq(primarySensor), refEq(mockingPrimaryPath), refEq(primaryWindowDimension));
        verify(configuration).extractPrototypes(refEq(secondarySensor), refEq(mockingSecondaryPath), refEq(secondaryWindowDimension));
        verifyNoMoreInteractions(configuration);
    }

    @Test
    public void testApplyExcludesAndRenames_emptyConfig() {
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setSourceVariableName("the_source_name");
        prototype.setTargetVariableName("the_wrong_name");

        final VariablePrototypeList variablePrototypeList = new VariablePrototypeList(null);// we don't need a ReaderFactory for this test tb 2016-10-05
        variablePrototypeList.add(prototype, "a_sensor");

        final VariablesConfiguration variablesConfiguration = new VariablesConfiguration();

        AbstractMmdWriter.applyExcludesAndRenames(variablePrototypeList, variablesConfiguration);

        final List<VariablePrototype> prototypes = variablePrototypeList.getPrototypesFor("a_sensor");
        assertEquals(1, prototypes.size());
        assertEquals("the_wrong_name", prototypes.get(0).getTargetVariableName());
    }

    @Test
    public void testApplyExcludesAndRenames_rename() {
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setSourceVariableName("the_source_name");
        prototype.setTargetVariableName("the_wrong_name");

        final VariablePrototypeList variablePrototypeList = new VariablePrototypeList(null);// we don't need a ReaderFactory for this test tb 2016-10-05
        variablePrototypeList.add(prototype, "another_sensor");

        final VariablesConfiguration variablesConfiguration = new VariablesConfiguration();
        final ArrayList<VariableRename> renamesList = new ArrayList<>();
        renamesList.add(new VariableRename("the_source_name", "correct_name"));
        variablesConfiguration.addRenames("another_sensor", renamesList);

        AbstractMmdWriter.applyExcludesAndRenames(variablePrototypeList, variablesConfiguration);

        final List<VariablePrototype> prototypes = variablePrototypeList.getPrototypesFor("another_sensor");
        assertEquals(1, prototypes.size());
        assertEquals("correct_name", prototypes.get(0).getTargetVariableName());
    }

    @Test
    public void testApplyExcludesAndRenames_exclude() {
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setSourceVariableName("the_source_name");
        prototype.setTargetVariableName("we_don_t_care");

        final VariablePrototype remove_prototype = new VariablePrototype();
        remove_prototype.setSourceVariableName("kick_me_off");
        remove_prototype.setTargetVariableName("we_don_t_care");

        final VariablePrototypeList variablePrototypeList = new VariablePrototypeList(null);// we don't need a ReaderFactory for this test tb 2016-10-05
        variablePrototypeList.add(remove_prototype, "the_sensor");
        variablePrototypeList.add(prototype, "the_sensor");

        final VariablesConfiguration variablesConfiguration = new VariablesConfiguration();
        final ArrayList<VariableExclude> excludeList = new ArrayList<>();
        excludeList.add(new VariableExclude("kick_me_off"));
        variablesConfiguration.addExcludes("the_sensor", excludeList);

        AbstractMmdWriter.applyExcludesAndRenames(variablePrototypeList, variablesConfiguration);

        final List<VariablePrototype> prototypes = variablePrototypeList.getPrototypesFor("the_sensor");
        assertEquals(1, prototypes.size());
        assertEquals("the_source_name", prototypes.get(0).getSourceVariableName());
    }

    @Test
    public void testGetPrototype(){
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setSourceVariableName("the_source_name");
        prototype.setTargetVariableName("we_don_t_care");

       final List<VariablePrototype> prototypeList = new ArrayList<>();
        prototypeList.add(prototype);

        final VariablePrototype resultPrototype = AbstractMmdWriter.getPrototype("the_source_name", prototypeList);
        assertNotNull(resultPrototype);
        assertEquals("the_source_name", resultPrototype.getSourceVariableName());
    }

    @Test
    public void testGetPrototype_notPresentInList(){
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setSourceVariableName("the_source_name");
        prototype.setTargetVariableName("we_don_t_care");

        final List<VariablePrototype> prototypeList = new ArrayList<>();
        prototypeList.add(prototype);

        final VariablePrototype resultPrototype = AbstractMmdWriter.getPrototype("this-does-not-exist", prototypeList);
        assertNull(resultPrototype);
    }

    private Sensor createSensor(String name, boolean isPrimary) {
        final Sensor primarySensor = new Sensor();
        primarySensor.setPrimary(isPrimary);
        primarySensor.setName(name);
        return primarySensor;
    }
}

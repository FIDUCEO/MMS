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
import com.bc.fiduceo.reader.Reader;
import org.junit.*;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayLong;
import ucar.ma2.ArrayObject;
import ucar.ma2.ArrayShort;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class IOVariableListTest {

    @Test
    public void testCloneAllTheAttributesFromAVariable() throws Exception {
        //preparation
        final String sensorName = "sensorName";
        final String variableName = "varName";

        final Variable mock = mock(Variable.class);
        when(mock.getShortName()).thenReturn(variableName);

        final ArrayList<Attribute> attributes = new ArrayList<>();
        final ArrayDouble.D1 expectedDouble = (ArrayDouble.D1) Array.factory(new double[]{1});
        final ArrayFloat.D1 expectedFloat = (ArrayFloat.D1) Array.factory(new float[]{2});
        final ArrayLong.D1 expectedLong = (ArrayLong.D1) Array.factory(new long[]{3});
        final ArrayInt.D1 expectedInt = (ArrayInt.D1) Array.factory(new int[]{4});
        final ArrayShort.D1 expectedShort = (ArrayShort.D1) Array.factory(new short[]{5});
        final ArrayByte.D1 expectedByte = (ArrayByte.D1) Array.factory(new byte[]{6});
        final ArrayObject.D1 expectedString = (ArrayObject.D1) Array.factory(DataType.STRING, new int[]{1});
        expectedString.set(0, "7");
        final ArrayFloat.D1 expectedFloats = (ArrayFloat.D1) Array.factory(new float[]{8, 9, 10});

        attributes.add(new Attribute("name1", expectedDouble));
        attributes.add(new Attribute("name2", expectedFloat));  // Attribute to be renamed
        attributes.add(new Attribute("name3", expectedLong));
        attributes.add(new Attribute("name4", expectedInt));
        attributes.add(new Attribute("name5", expectedShort));
        attributes.add(new Attribute("_Chunk_and_so_on", expectedInt)); // Attribute "_Chunk" should be ignored
        attributes.add(new Attribute("name6", expectedByte));
        attributes.add(new Attribute("name7", expectedString));
        attributes.add(new Attribute("name8", expectedFloats));
        when(mock.getAttributes()).thenReturn(attributes);

        final VariablesConfiguration variablesConfiguration = new VariablesConfiguration();
        variablesConfiguration.setAttributeRename(sensorName, variableName, "name2", "renamed");

        //test
        final List<Attribute> attributeClones = IOVariablesList.getAttributeClones(mock, sensorName, variablesConfiguration);

        //validation
        assertNotNull(attributeClones);
        assertEquals(8, attributeClones.size());

        Attribute expectedAttrib;
        Attribute actualAttrib;
        Array expectedAttribValues;
        Array actualAttribValues;
        int attribIndex;

        // template
        attribIndex = 0;

        expectedAttrib = attributes.get(attribIndex);
        actualAttrib = attributeClones.get(attribIndex);

        assertEquals(expectedAttrib, actualAttrib);
        assertNotSame(expectedAttrib, actualAttrib);
        assertEquals(expectedAttrib.getShortName(), actualAttrib.getShortName());

        expectedAttribValues = expectedAttrib.getValues();
        actualAttribValues = actualAttrib.getValues();

        assertNotSame(expectedAttribValues, actualAttribValues);
        assertEquals(expectedAttribValues.getRank(), actualAttribValues.getRank());
        assertEquals(expectedAttribValues.getElementType(), actualAttribValues.getElementType());
        assertEquals(1, actualAttribValues.getSize());
        assertEquals(expectedAttribValues.getDouble(0), actualAttribValues.getDouble(0), 1e-8);


        attribIndex = 1;
        expectedAttrib = attributes.get(attribIndex);
        actualAttrib = attributeClones.get(attribIndex);

        assertNotSame(expectedAttrib, actualAttrib);
        assertEquals("renamed", actualAttrib.getShortName());

        expectedAttribValues = expectedAttrib.getValues();
        actualAttribValues = actualAttrib.getValues();

        assertNotSame(expectedAttribValues, actualAttribValues);
        assertEquals(expectedAttribValues.getRank(), actualAttribValues.getRank());
        assertEquals(expectedAttribValues.getElementType(), actualAttribValues.getElementType());
        assertEquals(1, actualAttribValues.getSize());
        assertEquals(expectedAttribValues.getFloat(0), actualAttribValues.getFloat(0), 1e-8f);


        attribIndex = 2;
        expectedAttrib = attributes.get(attribIndex);
        actualAttrib = attributeClones.get(attribIndex);

        assertEquals(expectedAttrib, actualAttrib);
        assertNotSame(expectedAttrib, actualAttrib);
        assertEquals(expectedAttrib.getShortName(), actualAttrib.getShortName());

        expectedAttribValues = expectedAttrib.getValues();
        actualAttribValues = actualAttrib.getValues();

        assertNotSame(expectedAttribValues, actualAttribValues);
        assertEquals(expectedAttribValues.getRank(), actualAttribValues.getRank());
        assertEquals(expectedAttribValues.getElementType(), actualAttribValues.getElementType());
        assertEquals(1, actualAttribValues.getSize());
        assertEquals(expectedAttribValues.getLong(0), actualAttribValues.getLong(0));


        attribIndex = 3;
        expectedAttrib = attributes.get(attribIndex);
        actualAttrib = attributeClones.get(attribIndex);

        assertEquals(expectedAttrib, actualAttrib);
        assertNotSame(expectedAttrib, actualAttrib);
        assertEquals(expectedAttrib.getShortName(), actualAttrib.getShortName());

        expectedAttribValues = expectedAttrib.getValues();
        actualAttribValues = actualAttrib.getValues();

        assertNotSame(expectedAttribValues, actualAttribValues);
        assertEquals(expectedAttribValues.getRank(), actualAttribValues.getRank());
        assertEquals(expectedAttribValues.getElementType(), actualAttribValues.getElementType());
        assertEquals(1, actualAttribValues.getSize());
        assertEquals(expectedAttribValues.getInt(0), actualAttribValues.getInt(0));


        attribIndex = 4;
        expectedAttrib = attributes.get(attribIndex);
        actualAttrib = attributeClones.get(attribIndex);

        assertEquals(expectedAttrib, actualAttrib);
        assertNotSame(expectedAttrib, actualAttrib);
        assertEquals(expectedAttrib.getShortName(), actualAttrib.getShortName());

        expectedAttribValues = expectedAttrib.getValues();
        actualAttribValues = actualAttrib.getValues();

        assertNotSame(expectedAttribValues, actualAttribValues);
        assertEquals(expectedAttribValues.getRank(), actualAttribValues.getRank());
        assertEquals(expectedAttribValues.getElementType(), actualAttribValues.getElementType());
        assertEquals(1, actualAttribValues.getSize());
        assertEquals(expectedAttribValues.getShort(0), actualAttribValues.getShort(0));


        attribIndex = 5;
        expectedAttrib = attributes.get(attribIndex + 1);
        actualAttrib = attributeClones.get(attribIndex);

        assertEquals(expectedAttrib, actualAttrib);
        assertNotSame(expectedAttrib, actualAttrib);
        assertEquals(expectedAttrib.getShortName(), actualAttrib.getShortName());

        expectedAttribValues = expectedAttrib.getValues();
        actualAttribValues = actualAttrib.getValues();

        assertNotSame(expectedAttribValues, actualAttribValues);
        assertEquals(expectedAttribValues.getRank(), actualAttribValues.getRank());
        assertEquals(expectedAttribValues.getElementType(), actualAttribValues.getElementType());
        assertEquals(1, actualAttribValues.getSize());
        assertEquals(expectedAttribValues.getByte(0), actualAttribValues.getByte(0));


        attribIndex = 6;
        expectedAttrib = attributes.get(attribIndex +1);
        actualAttrib = attributeClones.get(attribIndex);

        assertEquals(expectedAttrib, actualAttrib);
        assertNotSame(expectedAttrib, actualAttrib);
        assertEquals(expectedAttrib.getShortName(), actualAttrib.getShortName());

        expectedAttribValues = expectedAttrib.getValues();
        actualAttribValues = actualAttrib.getValues();

        assertNotSame(expectedAttribValues, actualAttribValues);
        assertEquals(expectedAttribValues.getRank(), actualAttribValues.getRank());
        assertEquals(expectedAttribValues.getElementType(), actualAttribValues.getElementType());
        assertEquals(1, actualAttribValues.getSize());
        assertEquals(expectedAttribValues.getObject(0), actualAttribValues.getObject(0));


        attribIndex = 7;
        expectedAttrib = attributes.get(attribIndex + 1);
        actualAttrib = attributeClones.get(attribIndex);

        assertEquals(expectedAttrib, actualAttrib);
        assertNotSame(expectedAttrib, actualAttrib);
        assertEquals(expectedAttrib.getShortName(), actualAttrib.getShortName());

        expectedAttribValues = expectedAttrib.getValues();
        actualAttribValues = actualAttrib.getValues();

        assertNotSame(expectedAttribValues, actualAttribValues);
        assertEquals(expectedAttribValues.getRank(), actualAttribValues.getRank());
        assertEquals(expectedAttribValues.getElementType(), actualAttribValues.getElementType());
        assertEquals(3, actualAttribValues.getSize());
        assertEquals(expectedAttribValues.getFloat(0), actualAttribValues.getFloat(0), 1e-8f);
        assertEquals(expectedAttribValues.getFloat(1), actualAttribValues.getFloat(1), 1e-8f);
        assertEquals(expectedAttribValues.getFloat(2), actualAttribValues.getFloat(2), 1e-8f);
    }

    @Test
    public void testCreateDimensionNames() {
        final Dimension dimension = new Dimension("mhs-ma", 7, 9);

        final String dimensionNames = IOVariablesList.createDimensionNames(dimension);
        assertEquals("matchup_count mhs-ma_ny mhs-ma_nx", dimensionNames);
    }

    @Test
    public void testAdd_newSensor() {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setSourceVariableName("Yo!");

        final IOVariablesList ioVariablesList = new IOVariablesList(null);// we don't need a ReaderFactory for this test tb 2016-10-05
        ioVariablesList.add(ioVariable, "sensor_name");

        final List<IOVariable> ioVariables = ioVariablesList.getVariablesFor("sensor_name");
        assertNotNull(ioVariables);
        assertEquals(1, ioVariables.size());
        assertEquals("Yo!", ioVariables.get(0).getSourceVariableName());
    }

    @Test
    public void testGetSensorNames() {
        final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(null);
        ioVariable.setSourceVariableName("what?");

        final IOVariablesList ioVariablesList = new IOVariablesList(null);// we don't need a ReaderFactory for this test tb 2016-10-05
        ioVariablesList.add(ioVariable, "sensor_name_1");
        ioVariablesList.add(ioVariable, "sensor_name_2");

        final List<String> sensorNames = ioVariablesList.getSensorNames();
        assertEquals(2, sensorNames.size());
        assertTrue(sensorNames.contains("sensor_name_1"));
        assertTrue(sensorNames.contains("sensor_name_2"));
    }

    @Test
    public void testAddGetProcessingReader() {
        final IOVariablesList ioVariablesList = new IOVariablesList(null);// we don't need a ReaderFactory for this test tb 2016-10-05

        final ReaderContainer container = new ReaderContainer();
        ioVariablesList.setReaderContainer("theFirst", container);

        final ReaderContainer resultCont = ioVariablesList.getReaderContainer("theFirst");
        assertNotNull(resultCont);
        assertSame(resultCont, container);
    }

    @Test
    public void testAddGetProcessingReader_twoReader() {
        final IOVariablesList ioVariablesList = new IOVariablesList(null);// we don't need a ReaderFactory for this test tb 2016-10-05
        final ReaderContainer container_1 = new ReaderContainer();
        final ReaderContainer container_2 = new ReaderContainer();

        ioVariablesList.setReaderContainer("theFirst", container_1);
        ioVariablesList.setReaderContainer("theSecond", container_2);

        ReaderContainer container;

        container = ioVariablesList.getReaderContainer("theFirst");
        assertNotNull(container);
        assertSame(container, container_1);

        container = ioVariablesList.getReaderContainer("theSecond");
        assertNotNull(container);
        assertSame(container, container_2);
    }

    @Test
    public void testAddGetProcessingReader_notPresent() {
        final IOVariablesList ioVariablesList = new IOVariablesList(null);// we don't need a ReaderFactory for this test tb 2016-10-05
        final ReaderContainer container = new ReaderContainer();

        ioVariablesList.setReaderContainer("theFirst", container);

        try {
            ioVariablesList.getReaderContainer("stupid");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    // @todo 1 tb/tb add tests for set reader - must be propagated to container 2016-12-19

    @Test
    public void testClose() throws IOException {
        final IOVariablesList ioVariablesList = new IOVariablesList(null);// we don't need a ReaderFactory for this test tb 2016-10-05
        final ReaderContainer container = new ReaderContainer();
        final Reader readerMock = mock(Reader.class);
        container.setReader(readerMock);
        ioVariablesList.setReaderContainer("theFirst", container);

        ioVariablesList.close();

        verify(readerMock, times(1)).close();
        verifyNoMoreInteractions(readerMock);
    }
}

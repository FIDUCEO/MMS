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
import org.junit.Test;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VariablePrototypeListTest {

    @Test
    public void testCloneAllTheAttributesFromAVariable() throws Exception {
        //preparation
        final Variable mock = mock(Variable.class);
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
        attributes.add(new Attribute("name2", expectedFloat));
        attributes.add(new Attribute("name3", expectedLong));
        attributes.add(new Attribute("name4", expectedInt));
        attributes.add(new Attribute("name5", expectedShort));
        attributes.add(new Attribute("name6", expectedByte));
        attributes.add(new Attribute("name7", expectedString));
        attributes.add(new Attribute("name8", expectedFloats));
        when(mock.getAttributes()).thenReturn(attributes);

        //test
        final List<Attribute> attributeClones = VariablePrototypeList.getAttributeClones(mock);

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

        assertEquals(expectedAttrib, actualAttrib);
        assertNotSame(expectedAttrib, actualAttrib);
        assertEquals(expectedAttrib.getShortName(), actualAttrib.getShortName());

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
        assertEquals(expectedAttribValues.getByte(0), actualAttribValues.getByte(0));


        attribIndex = 6;
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
        assertEquals(expectedAttribValues.getObject(0), actualAttribValues.getObject(0));


        attribIndex = 7;
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
        assertEquals(3, actualAttribValues.getSize());
        assertEquals(expectedAttribValues.getFloat(0), actualAttribValues.getFloat(0), 1e-8f);
        assertEquals(expectedAttribValues.getFloat(1), actualAttribValues.getFloat(1), 1e-8f);
        assertEquals(expectedAttribValues.getFloat(2), actualAttribValues.getFloat(2), 1e-8f);
    }

    @Test
    public void testCreateDimensionNames() {
        final Dimension dimension = new Dimension("mhs-ma", 7, 9);

        final String dimensionNames = VariablePrototypeList.createDimensionNames(dimension);
        assertEquals("matchup_count mhs-ma_ny mhs-ma_nx", dimensionNames);
    }

    @Test
    public void testAdd_newSensor() {
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setSourceVariableName("Yo!");

        final VariablePrototypeList variablePrototypeList = new VariablePrototypeList(null);// we don't need a ReaderFactory for this test tb 2016-10-05
        variablePrototypeList.add(prototype, "sensor_name");

        final List<VariablePrototype> prototypeList = variablePrototypeList.getPrototypesFor("sensor_name");
        assertNotNull(prototypeList);
        assertEquals(1, prototypeList.size());
        assertEquals("Yo!", prototypeList.get(0).getSourceVariableName());
    }

    @Test
    public void testGetSensorNames() {
        final VariablePrototype prototype = new VariablePrototype();
        prototype.setSourceVariableName("what?");

        final VariablePrototypeList variablePrototypeList = new VariablePrototypeList(null);// we don't need a ReaderFactory for this test tb 2016-10-05
        variablePrototypeList.add(prototype, "sensor_name_1");
        variablePrototypeList.add(prototype, "sensor_name_2");

        final List<String> sensorNames = variablePrototypeList.getSensorNames();
        assertEquals(2, sensorNames.size());
        assertTrue(sensorNames.contains("sensor_name_1"));
        assertTrue(sensorNames.contains("sensor_name_2"));
    }
}

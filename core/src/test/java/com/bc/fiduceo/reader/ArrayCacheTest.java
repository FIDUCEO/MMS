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

package com.bc.fiduceo.reader;


import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.nc2.*;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ArrayCacheTest {

    private ArrayCache arrayCache;
    private NetcdfFile netcdfFile;
    private Variable variable;
    private Group group;

    @Before
    public void setUp() throws IOException {
        netcdfFile = mock(NetcdfFile.class);
        variable = mock(Variable.class);
        when(netcdfFile.findVariable(null, "a_variable")).thenReturn(variable);

        group = mock(Group.class);
        when(netcdfFile.findGroup("a_group")).thenReturn(group);
        when(netcdfFile.findVariable(group, "a_group_variable")).thenReturn(variable);

        final Array array = NetCDFUtils.create(new int[]{1, 2, 3, 4});
        when(variable.read()).thenReturn(array);

        arrayCache = new ArrayCache(netcdfFile);
    }

    @Test
    public void testRequestedArrayIsRead() throws IOException {
        final Array resultArray = arrayCache.get("a_variable");
        assertNotNull(resultArray);

        verify(netcdfFile, times(1)).findVariable(null, "a_variable");
        verify(variable, times(1)).read();
        verify(variable, times(1)).attributes();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testRequestedArrayInGroupIsRead() throws IOException {
        final Array resultArray = arrayCache.get("a_group", "a_group_variable");
        assertNotNull(resultArray);

        verify(netcdfFile, times(1)).findGroup("a_group");
        verify(netcdfFile, times(1)).findVariable(group, "a_group_variable");
        verify(variable, times(1)).read();
        verify(variable, times(1)).attributes();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testRequestedArrayIsReadScaled_onlyScale() throws IOException {
        final Attribute attribute = new Attribute("scaleFac", 2.1);
        final AttributeContainerMutable attributes = new AttributeContainerMutable("test");
        attributes.addAttribute(attribute);
        when(variable.attributes()).thenReturn(attributes);

        final Array resultArray = arrayCache.getScaled("a_variable", "scaleFac", null);
        assertNotNull(resultArray);
        assertEquals(2.1, resultArray.getFloat(0), 1e-6);
        assertEquals(4.2, resultArray.getFloat(1), 1e-6);
        assertEquals(6.3, resultArray.getFloat(2), 1e-6);
        assertEquals(8.4, resultArray.getFloat(3), 1e-6);

        verify(netcdfFile, times(1)).findVariable(null, "a_variable");
        verify(variable, times(1)).read();
        verify(variable, times(1)).attributes();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testRequestedArrayIsReadScaled_onlyOffset() throws IOException {
        final Attribute attribute = new Attribute("offset", 1.8);
        final AttributeContainerMutable attributes = new AttributeContainerMutable("test");
        attributes.addAttribute(attribute);
        when(variable.attributes()).thenReturn(attributes);

        final Array resultArray = arrayCache.getScaled("a_variable", null, "offset");
        assertNotNull(resultArray);
        assertEquals(2.8, resultArray.getFloat(0), 1e-6);
        assertEquals(3.8, resultArray.getFloat(1), 1e-6);
        assertEquals(4.8, resultArray.getFloat(2), 1e-6);
        assertEquals(5.8, resultArray.getFloat(3), 1e-6);

        verify(netcdfFile, times(1)).findVariable(null, "a_variable");
        verify(variable, times(1)).read();
        verify(variable, times(1)).attributes();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testRequestedArrayIsReadScaled_scaleAndOffset() throws IOException {
        final Attribute scaleAttribute = new Attribute("scale", 0.5);
        final Attribute offsetAttribute = new Attribute("offset", 1.4);
        final AttributeContainerMutable attributes = new AttributeContainerMutable("test");
        attributes.addAttribute(scaleAttribute);
        attributes.addAttribute(offsetAttribute);
        when(variable.attributes()).thenReturn(attributes);

        final Array resultArray = arrayCache.getScaled("a_variable", "scale", "offset");
        assertNotNull(resultArray);
        assertEquals(1.9, resultArray.getFloat(0), 1e-6);
        assertEquals(2.4, resultArray.getFloat(1), 1e-6);
        assertEquals(2.9, resultArray.getFloat(2), 1e-6);
        assertEquals(3.4, resultArray.getFloat(3), 1e-6);

        verify(netcdfFile, times(1)).findVariable(null, "a_variable");
        verify(variable, times(1)).read();
        verify(variable, times(1)).attributes();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testRequestedArrayIsReadScaled_scaleIsNotAvailable_RuntimeException() throws IOException {
        final Attribute offsetAttribute = new Attribute("offset", 1.4);
        final AttributeContainerMutable attributes = new AttributeContainerMutable("test");
        attributes.addAttribute(offsetAttribute);
        when(variable.attributes()).thenReturn(attributes);

        try {
            arrayCache.getScaled("a_variable", "scale", "offset");
            fail("should not come here.");
        } catch (RuntimeException e) {
            assertEquals("Scale attribute with name 'scale' is not available.", e.getMessage());
        }

        verify(netcdfFile, times(1)).findVariable(null, "a_variable");
        verify(variable, times(1)).read();
        verify(variable, times(1)).attributes();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testRequestedArrayIsReadScaled_offsetIsNotAvailable_RuntionException() throws IOException {
        final Attribute scaleAttribute = new Attribute("scale", 0.5);
        final AttributeContainerMutable attributes = new AttributeContainerMutable("test");
        attributes.addAttribute(scaleAttribute);
        when(variable.attributes()).thenReturn(attributes);

        try {
            arrayCache.getScaled("a_variable", "scale", "offset");
            fail("should not come here.");
        } catch (RuntimeException e) {
            assertEquals("Offset attribute with name 'offset' is not available.", e.getMessage());
        }

        verify(netcdfFile, times(1)).findVariable(null, "a_variable");
        verify(variable, times(1)).read();
        verify(variable, times(1)).attributes();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testRequestedArrayIsReadScaled_fromCacheIfRequestedTwice() throws IOException {
        final Attribute attribute = new Attribute("scaleFac", 2.1);
        final AttributeContainerMutable attributes = new AttributeContainerMutable("test");
        attributes.addAttribute(attribute);
        when(variable.attributes()).thenReturn(attributes);

        final Array resultArray = arrayCache.getScaled("a_variable", "scaleFac", null);
        assertNotNull(resultArray);

        final Array resultArray_2 = arrayCache.getScaled("a_variable", "scaleFac", null);
        assertNotNull(resultArray_2);

        assertSame(resultArray, resultArray_2);

        verify(netcdfFile, times(1)).findVariable(null, "a_variable");
        verify(variable, times(1)).read();
        verify(variable, times(1)).attributes();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testRequestedArrayIsTakenFromCacheWhenRequestedASecondTime() throws IOException {
        final Array resultArray = arrayCache.get("a_variable");
        assertNotNull(resultArray);

        final Array secondResultArray = arrayCache.get("a_variable");
        assertNotNull(secondResultArray);

        assertSame(resultArray, secondResultArray);

        verify(netcdfFile, times(1)).findVariable(null, "a_variable");
        verify(variable, times(1)).read();
        verify(variable, times(1)).attributes();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testRequestedArrayInGroupIsTakenFromCacheWhenRequestedASecondTime() throws IOException {
        final Array resultArray = arrayCache.get("a_group", "a_group_variable");
        assertNotNull(resultArray);

        final Array secondResultArray = arrayCache.get("a_group", "a_group_variable");
        assertNotNull(secondResultArray);

        assertSame(resultArray, secondResultArray);

        verify(netcdfFile, times(1)).findGroup("a_group");
        verify(netcdfFile, times(1)).findVariable(group, "a_group_variable");
        verify(variable, times(1)).read();
        verify(variable, times(1)).attributes();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testRequestedArrayIsReadScaled_fromGroup_scaleAndOffset() throws IOException {
        final Attribute scaleAttribute = new Attribute("scale", 0.5);
        final Attribute offsetAttribute = new Attribute("offset", 1.4);
        final AttributeContainerMutable attributes = new AttributeContainerMutable("test");
        attributes.addAttribute(scaleAttribute);
        attributes.addAttribute(offsetAttribute);
        when(variable.attributes()).thenReturn(attributes);

        final Array resultArray = arrayCache.getScaled("a_group", "a_group_variable", "scale", "offset");
        assertNotNull(resultArray);
        assertEquals(1.9, resultArray.getFloat(0), 1e-6);
        assertEquals(2.4, resultArray.getFloat(1), 1e-6);
        assertEquals(2.9, resultArray.getFloat(2), 1e-6);
        assertEquals(3.4, resultArray.getFloat(3), 1e-6);

        verify(netcdfFile, times(1)).findGroup("a_group");
        verify(netcdfFile, times(1)).findVariable(group, "a_group_variable");
        verify(variable, times(1)).read();
        verify(variable, times(1)).attributes();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testArrayThrowsWhenVariableIsNotPresent() {
        try {
            arrayCache.get("not_present_variable");
            fail("IOException expected");
        } catch (IOException expected) {
        }

        verify(netcdfFile, times(1)).findVariable(null, "not_present_variable");
        verify(netcdfFile, times(1)).getLocation();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testArrayThrowsWhenVariableInGroupIsNotPresent() {
        try {
            arrayCache.get("a_group", "not_present_variable");
            fail("IOException expected");
        } catch (IOException expected) {
        }

        verify(netcdfFile, times(1)).findGroup("a_group");
        verify(netcdfFile, times(1)).findVariable(group, "not_present_variable");
        verify(netcdfFile, times(1)).getLocation();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testArrayThrowsWhenGroupIsNotPresent() {
        try {
            arrayCache.get("a_shitty_group", "not_present_variable");
            fail("IOException expected");
        } catch (IOException expected) {
        }

        verify(netcdfFile, times(1)).findGroup("a_shitty_group");
        verify(netcdfFile, times(1)).getLocation();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testGetGroupedName() {
        assertEquals("nasen_mann", ArrayCache.createGroupedName("nasen", "mann"));
    }

    @Test
    public void testGetStringAttribute_variableNotPresent() {
        try {
            arrayCache.getStringAttributeValue("attribute_name", "not_present_variable");
            fail("IOException expected");
        } catch (IOException expected) {
        }

        verify(netcdfFile, times(1)).findVariable(null, "not_present_variable");
        verify(netcdfFile, times(1)).getLocation();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testGetStringAttribute_attributeNotPresent() throws IOException {
        final String attributeValue = arrayCache.getStringAttributeValue("attribute_name", "a_variable");
        assertNull(attributeValue);

        verify(netcdfFile, times(1)).findVariable(null, "a_variable");
        verify(variable, times(1)).read();
        verify(variable, times(1)).attributes();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testGetStringAttribute() throws IOException {
        final Attribute numberAttribute = new Attribute("attribute_number", -99999);
        final Attribute stringAttribute = new Attribute("attribute_string", "the_value");
        final AttributeContainerMutable attributes = new AttributeContainerMutable("test");
        attributes.addAttribute(numberAttribute);
        attributes.addAttribute(stringAttribute);
        when(variable.attributes()).thenReturn(attributes);

        String attributeValue = arrayCache.getStringAttributeValue("attribute_number", "a_variable");
        assertEquals("-99999", attributeValue);

        attributeValue = arrayCache.getStringAttributeValue("attribute_string", "a_variable");
        assertEquals("the_value", attributeValue);

        verify(netcdfFile, times(1)).findVariable(null, "a_variable");
        verify(variable, times(1)).read();
        verify(variable, times(1)).attributes();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testGetStringAttributeFromGroup_groupNotPresent() {
        try {
            arrayCache.getStringAttributeValue("attribute_name", "a_mean_group", "a_group_variable");
            fail("IOException expected");
        } catch (IOException expected) {
        }

        verify(netcdfFile, times(1)).findGroup("a_mean_group");
        verify(netcdfFile, times(1)).getLocation();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testGetStringAttributeFromGroup_variableNotPresent() {
        try {
            arrayCache.getStringAttributeValue("attribute_name", "a_group", "not_present_variable");
            fail("IOException expected");
        } catch (IOException expected) {
        }

        verify(netcdfFile, times(1)).findGroup("a_group");
        verify(netcdfFile, times(1)).findVariable(group, "not_present_variable");
        verify(netcdfFile, times(1)).getLocation();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testGetStringAttributeFromGroup_attributeNotPresent() throws IOException {
        final String attributeValue = arrayCache.getStringAttributeValue("attribute_name", "a_group", "a_group_variable");
        assertNull(attributeValue);

        verify(netcdfFile, times(1)).findGroup("a_group");
        verify(netcdfFile, times(1)).findVariable(group, "a_group_variable");
        verify(variable, times(1)).read();
        verify(variable, times(1)).attributes();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testGetStringAttributeFromGroup() throws IOException {
        final Attribute numberAttribute = new Attribute("attribute_number", -99999);
        final Attribute stringAttribute = new Attribute("attribute_string", "the_value");
        final AttributeContainerMutable attributes = new AttributeContainerMutable("test");
        attributes.addAttribute(numberAttribute);
        attributes.addAttribute(stringAttribute);
        when(variable.attributes()).thenReturn(attributes);

        String attributeValue = arrayCache.getStringAttributeValue("attribute_number", "a_group", "a_group_variable");
        assertEquals("-99999", attributeValue);

        attributeValue = arrayCache.getStringAttributeValue("attribute_string", "a_group", "a_group_variable");
        assertEquals("the_value", attributeValue);

        verify(netcdfFile, times(1)).findGroup("a_group");
        verify(netcdfFile, times(1)).findVariable(group, "a_group_variable");
        verify(variable, times(1)).read();
        verify(variable, times(1)).attributes();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testGetNumberAttribute() throws IOException {
        final Attribute numberAttribute = new Attribute("attribute_number", -99999);
        final AttributeContainerMutable attributes = new AttributeContainerMutable("test");
        attributes.addAttribute(numberAttribute);
        when(variable.attributes()).thenReturn(attributes);

        final Number attributeValue = arrayCache.getNumberAttributeValue("attribute_number", "a_variable");
        assertEquals(-99999, attributeValue.intValue());

        verify(netcdfFile, times(1)).findVariable(null, "a_variable");
        verify(variable, times(1)).read();
        verify(variable, times(1)).attributes();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testGetNumberAttributeFromGroup() throws IOException {
        final Attribute numberAttribute = new Attribute("attribute_number", 1.887635);
        final AttributeContainerMutable attributes = new AttributeContainerMutable("test");
        attributes.addAttribute(numberAttribute);
        when(variable.attributes()).thenReturn(attributes);

        Number attributeValue = arrayCache.getNumberAttributeValue("attribute_number", "a_group", "a_group_variable");
        assertEquals(1.887635, attributeValue.doubleValue(), 1e-8);

        verify(netcdfFile, times(1)).findGroup("a_group");
        verify(netcdfFile, times(1)).findVariable(group, "a_group_variable");
        verify(variable, times(1)).read();
        verify(variable, times(1)).attributes();
        verifyNoMoreInteractions(netcdfFile, variable);
    }

    @Test
    public void testInjectVariable() throws IOException {
        final Variable variable = mock(Variable.class);
        when(variable.getShortName()).thenReturn("injected");

        final Array array = NetCDFUtils.create(new int[]{2, 3, 4, 5});
        when(variable.read()).thenReturn(array);

        arrayCache.inject(variable);

        final Array injectedArray = arrayCache.get("injected");
        assertNotNull(injectedArray);
        assertEquals(2, injectedArray.getInt(0));
    }

    @Test
    public void testGetInjectedVariables_empty() {
        final List<Variable> injectedVariables = arrayCache.getInjectedVariables();
        assertNotNull(injectedVariables);
        assertEquals(0, injectedVariables.size());
    }

    @Test
    public void testGetInjectedVariables_twoOfThem() {
        Variable variable = mock(Variable.class);
        when(variable.getShortName()).thenReturn("first");
        arrayCache.inject(variable);

        variable = mock(Variable.class);
        when(variable.getShortName()).thenReturn("second");
        arrayCache.inject(variable);

        final List<Variable> injectedVariables = arrayCache.getInjectedVariables();
        assertNotNull(injectedVariables);
        assertEquals(2, injectedVariables.size());

        assertEquals("first", injectedVariables.get(0).getShortName());
        assertEquals("second", injectedVariables.get(1).getShortName());
    }
}

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

package com.bc.fiduceo.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.core.UseCaseConfig;
import org.esa.snap.core.util.Debug;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.junit.*;

import java.util.List;

public class JDomUtilsTest {

    @Test
    public void testGetMandatoryText() throws Exception {
        final Element testElem1 = new Element("Elem");
        testElem1.setText("  abcdef  ");

        final String textFromElem = JDomUtils.getMandatoryText(testElem1);
        assertEquals("abcdef", textFromElem);
    }

    @Test
    public void testGetMandatoryText_Exception() throws Exception {
        final Element testElem1 = new Element("testElem");
        testElem1.setText("   ");

        try {
            JDomUtils.getMandatoryText(testElem1);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element 'testElem' expected", expected.getMessage());
        }
    }

    @Test
    public void testGetMandatoryAttribute() {
        final Attribute attribute = new Attribute("mandatory", "value");
        final Element element = new Element("element");
        element.setAttribute(attribute);

        final Attribute mandatoryAttribute = JDomUtils.getMandatoryAttribute(element, "mandatory");
        assertNotNull(mandatoryAttribute);
        assertSame(attribute, mandatoryAttribute);
    }

    @Test
    public void testGetMandatoryAttribute_notPresent() {
        final Element element = new Element("element");

        try {
            JDomUtils.getMandatoryAttribute(element, "mandatory");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Attribute 'mandatory' expected", expected.getMessage());
        }
    }

    @Test
    public void testGetMandatoryChild() {
        final Element elem = new Element("elem");
        final Element childElement = new Element("mandatory");
        elem.addContent(childElement);

        final Element mandatoryChild = JDomUtils.getMandatoryChild(elem, "mandatory");
        assertNotNull(mandatoryChild);
        assertSame(childElement, mandatoryChild);
    }

    @Test
    public void testGetMandatoryChild_notPresent() {
        final Element element = new Element("elem");

        try {
            JDomUtils.getMandatoryChild(element, "mandatory");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element 'mandatory' expected", expected.getMessage());
        }
    }

    @Test
    public void testGetMandatoryChildren() {
        final Element elem = new Element("elem");
        final Element childElement1 = new Element("mandatory");
        final Element childElement2 = new Element("mandatory");
        elem.addContent(childElement1);
        elem.addContent(childElement2);

        final List<Element> children = JDomUtils.getMandatoryChildren(elem, "mandatory");
        assertNotNull(children);
        assertEquals(2, children.size());
        assertTrue(children.contains(childElement1));
        assertTrue(children.contains(childElement2));
    }

    @Test
    public void testGetMandatoryChildren_notPresent() {
        final Element elem = new Element("elem");
        final Element child1 = new Element("otherChild");
        final Element child2 = new Element("otherChild");
        elem.addContent(child1);
        elem.addContent(child2);

        try {
            JDomUtils.getMandatoryChildren(elem, "mandatory");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("At least one child element 'mandatory' expected", expected.getMessage());
        }
    }

    @Test
    public void testGetMandatoryChildTextTrim() {
        final Element element = mock(Element.class);
        final Element childElement = mock(Element.class);

        when(element.getChild("mandatory")).thenReturn(childElement);
        when(childElement.getTextTrim()).thenReturn("trimmedText");

        final String textTrim = JDomUtils.getMandatoryChildTextTrim(element, "mandatory");
        assertEquals("trimmedText", textTrim);
    }

    @Test
    public void testGetMandatoryChildTextTrim_notPresent() {
        final Element element = mock(Element.class);

        when(element.getChild("mandatory")).thenReturn(null);

        try {
            JDomUtils.getMandatoryChildTextTrim(element, "mandatory");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetMandatoryRootElement() {
        final Document document = mock(Document.class);
        final Element rootElement = mock(Element.class);

        when(document.getRootElement()).thenReturn(rootElement);
        when(rootElement.getName()).thenReturn(UseCaseConfig.TAG_NAME_ROOT);

        final Element mandatoryRootElement = JDomUtils.getMandatoryRootElement(UseCaseConfig.TAG_NAME_ROOT, document);
        assertNotNull(mandatoryRootElement);
        assertSame(rootElement, mandatoryRootElement);
    }

    @Test
    public void testGetMandatoryRootElement_wrongTag() {
        final Document document = mock(Document.class);
        final Element rootElement = mock(Element.class);

        when(document.getRootElement()).thenReturn(rootElement);
        when(rootElement.getName()).thenReturn("my_name_is_renate");

        try {
            JDomUtils.getMandatoryRootElement(UseCaseConfig.TAG_NAME_ROOT, document);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetMandatoryChildMandatoryTextTrimm() throws Exception {
        final Element root = new Element("root");
        root.addContent(new Element("childName").setText("     blah    "));

        final String trimmedChildText = JDomUtils.getMandatoryChildMandatoryTextTrim(root, "childName");

        assertEquals("blah", trimmedChildText);
    }

    @Test
    public void testGetMandatoryChildMandatoryTextTrimm_NoTextSet() throws Exception {
        final Element root = new Element("root");
        root.addContent(new Element("childName").setText("    "));

        try {
            JDomUtils.getMandatoryChildMandatoryTextTrim(root, "childName");
        } catch (RuntimeException expected) {
            assertEquals("Value of element 'childName' expected", expected.getMessage());
        }
    }

    @Test
    public void testGetMandatoryChildMandatoryTextTrimm_NoChildSet() throws Exception {
        final Element root = new Element("root");

        try {
            JDomUtils.getMandatoryChildMandatoryTextTrim(root, "childName");
        } catch (RuntimeException expected) {
            assertEquals("Child element 'childName' expected", expected.getMessage());
        }
    }

    @Test
    public void testSetNameAttribute() throws Exception {
        final Element element = new Element("elem");
        JDomUtils.setNameAttribute(element, "valueForAttribute");

        assertEquals("valueForAttribute", element.getAttribute(JDomUtils.ATTRIBUTE_NAME__NAME).getValue());
    }

    @Test
    public void testGetValueFromNameAttribute() throws Exception {
        //preparation
        final Element element = new Element("elem");

        //execution //verification
        assertNull(JDomUtils.getValueFromNameAttribute(element));

        //preparation
        JDomUtils.setNameAttribute(element, "textValue");

        //execution //verification
        assertEquals("textValue", JDomUtils.getValueFromNameAttribute(element));
    }

    @Test
    public void testGetValueFromNameAttributeMandatory() throws Exception {
        //preparation
        final Element element = new Element("elem");

        //execution //verification
        try {
            JDomUtils.getValueFromNameAttributeMandatory(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Attribute 'name' expected", expected.getMessage());
        }

        //preparation
        JDomUtils.setNameAttribute(element, "");

        //execution //verification
        try {
            JDomUtils.getValueFromNameAttributeMandatory(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value expected for attribute 'name'", expected.getMessage());
        }

        //preparation
        JDomUtils.setNameAttribute(element, "textValue");

        //execution //verification
        assertEquals("textValue", JDomUtils.getValueFromNameAttribute(element));
    }

    @Test
    public void testSetNamesAttribute() throws Exception {
        final Element element = new Element("elem");
        JDomUtils.setNamesAttribute(element, "valueForAttribute");

        assertEquals("valueForAttribute", element.getAttribute(JDomUtils.ATTRIBUTE_NAME__NAMES).getValue());
    }

    @Test
    public void testGetValueFromNamesAttribute() throws Exception {
        //preparation
        final Element element = new Element("elem");

        //execution //verification
        assertNull(JDomUtils.getValueFromNamesAttribute(element));

        //preparation
        JDomUtils.setNamesAttribute(element, "textValue");

        //execution //verification
        assertEquals("textValue", JDomUtils.getValueFromNamesAttribute(element));
    }

    @Test
    public void testGetValueFromNamesAttributeMandatory() throws Exception {
        //preparation
        final Element element = new Element("elem");

        //execution //verification
        try {
            JDomUtils.getValueFromNamesAttributeMandatory(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Attribute 'names' expected", expected.getMessage());
        }

        //preparation
        JDomUtils.setNamesAttribute(element, "");

        //execution //verification
        try {
            JDomUtils.getValueFromNamesAttributeMandatory(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value expected for attribute 'names'", expected.getMessage());
        }

        //preparation
        JDomUtils.setNamesAttribute(element, "textValue");

        //execution //verification
        assertEquals("textValue", JDomUtils.getValueFromNamesAttribute(element));
    }
}

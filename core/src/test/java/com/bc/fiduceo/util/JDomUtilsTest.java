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

import com.bc.fiduceo.core.UseCaseConfig;
import org.jdom.Attribute;
import org.jdom.Document;
import org.junit.Test;
import org.jdom.Element;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JDomUtilsTest {

    @Test
    public void testGetMandatoryAttribute() {
        final Element element = mock(Element.class);
        final Attribute attribute = mock(Attribute.class);

        when(element.getAttribute("mandatory")).thenReturn(attribute);

        final Attribute mandatoryAttribute = JDomUtils.getMandatoryAttribute(element, "mandatory");
        assertNotNull(mandatoryAttribute);
        assertSame(attribute, mandatoryAttribute);
    }

    @Test
    public void testGetMandatoryAttribute_notPresent() {
        final Element element = mock(Element.class);

        when(element.getAttribute("mandatory")).thenReturn(null);

        try {
            JDomUtils.getMandatoryAttribute(element, "mandatory");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetMandatoryChild() {
        final Element element = mock(Element.class);
        final Element childElement = mock(Element.class);

        when(element.getChild("mandatory")).thenReturn(childElement);

        final Element mandatoryChild = JDomUtils.getMandatoryChild(element, "mandatory");
        assertNotNull(mandatoryChild);
        assertSame(childElement, mandatoryChild);
    }

    @Test
    public void testGetMandatoryChild_notPresent() {
        final Element element = mock(Element.class);

        when(element.getChild("mandatory")).thenReturn(null);

        try {
            JDomUtils.getMandatoryChild(element, "mandatory");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
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
}

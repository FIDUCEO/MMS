/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.bc.fiduceo.util;

import static org.esa.snap.core.util.StringUtils.isNullOrEmpty;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import java.util.List;

public class JDomUtils {

    public final static String ATTRIBUTE_NAME__NAME = "name";
    public final static String ATTRIBUTE_NAME__NAMES = "names";
    public static final String VALUE = "Value";
    public static final String ATTRIBUTE = "Attribute";

    public static Attribute getMandatoryAttribute(final Element element, final String name) {
        final Attribute attribute = element.getAttribute(name);
        if (attribute == null) {
            throw new RuntimeException(ATTRIBUTE + " '" + name + "' expected");
        }
        return attribute;
    }

    public static String getMandatoryText(final Element element) {
        final String textTrim = element.getTextTrim();
        if (textTrim.length() == 0) {
            throw new RuntimeException(VALUE + " of element '" + element.getName() + "' expected");
        }
        return textTrim;
    }

    public static Element getMandatoryChild(final Element element, final String name) {
        final Element child = element.getChild(name);
        if (child == null) {
            throw new RuntimeException("Child element '" + name + "' expected");
        }
        return child;
    }

    public static List<Element> getMandatoryChildren(final Element element, final String name) {
        final List<Element> children = element.getChildren(name);
        if (children.size() == 0) {
            throw new RuntimeException("At least one child element '" + name + "' expected");
        }
        return children;
    }

    public static String getMandatoryChildTextTrim(final Element element, final String name) {
        final Element child = getMandatoryChild(element, name);
        return child.getTextTrim();
    }

    public static String getMandatoryChildMandatoryTextTrim(final Element element, final String name) {
        final Element child = getMandatoryChild(element, name);
        return getMandatoryText(child);
    }

    public static Element getMandatoryRootElement(String elementName, Document document) {
        final Element rootElement = document.getRootElement();
        final String name = rootElement.getName();
        if (!elementName.equals(name)) {
            throw new RuntimeException("Root tag name '" + elementName + "' expected");
        }
        return rootElement;
    }

    public static Attribute setNameAttribute(Element element, String textvalue) {
        return setAttribute(element, ATTRIBUTE_NAME__NAME, textvalue);
    }

    public static Attribute setNamesAttribute(Element element, String textValue) {
        return setAttribute(element, ATTRIBUTE_NAME__NAMES, textValue);
    }

    public static String getValueFromNameAttribute(Element element) {
        return getValueFromAttribute(element, ATTRIBUTE_NAME__NAME);
    }

    public static String getValueFromNamesAttribute(Element element) {
        return getValueFromAttribute(element, ATTRIBUTE_NAME__NAMES);
    }

    public static String getValueFromNameAttributeMandatory(Element element) {
        return getValueFromAttributeMandatory(element, ATTRIBUTE_NAME__NAME);
    }

    public static String getValueFromNamesAttributeMandatory(Element element) {
        return getValueFromAttributeMandatory(element, ATTRIBUTE_NAME__NAMES);
    }

    private static Attribute setAttribute(Element element, String attributeName, String textvalue) {
        element.setAttribute(attributeName, textvalue);
        return element.getAttribute(attributeName);
    }

    private static String getValueFromAttribute(Element element, String attributeName) {
        final Attribute attribute = element.getAttribute(attributeName);
        if (attribute != null) {
            return attribute.getValue();
        }
        return null;
    }

    private static String getValueFromAttributeMandatory(Element element, String attributeName) {
        final Attribute mandatoryAttribute = getMandatoryAttribute(element, attributeName);
        final String value = mandatoryAttribute.getValue();
        if (isNullOrEmpty(value)) {
            throw new RuntimeException(VALUE + " expected for attribute '" + attributeName + "'");
        }
        return value;
    }
}

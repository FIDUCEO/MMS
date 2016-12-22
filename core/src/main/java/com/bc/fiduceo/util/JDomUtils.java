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

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

public class JDomUtils {

    public static Attribute getMandatoryAttribute(final Element element, final String name) {
        final Attribute attribute = element.getAttribute(name);
        if (attribute == null) {
            throw new RuntimeException("Attribute '" + name + "' expected");
        }
        return attribute;
    }

    public static String getMandatoryText(final Element element) {
        final String textTrim = element.getTextTrim();
        if (textTrim.length() == 0) {
            throw new RuntimeException("Value of element '" + element.getName() + "' expected");
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
}

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

import com.bc.fiduceo.core.UseCaseConfig;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

public class JDomUtils {

    public static Attribute mandatory_getAttribute(final Element element, final String name) {
        final Attribute attribute = element.getAttribute(name);
        if (attribute == null) {
            throw new RuntimeException("Attribute '" + name + "' expected");
        }
        return attribute;
    }

    public static Element mandatory_getChild(final Element element, final String name) {
        final Element child = element.getChild(name);
        if (child == null) {
            throw new RuntimeException("Children '" + name + "' expected");
        }
        return child;
    }

    public static String mandatory_getChildTextTrim(final Element element, final String name) {
        final Element child = mandatory_getChild(element, name);
        return child.getTextTrim();
    }

    public static Element mandatory_getRootElement(Document document) {
        final Element rootElement = document.getRootElement();
        final String name = rootElement.getName();
        if (!UseCaseConfig.TAG_NAME_ROOT.equals(name)) {
            throw new RuntimeException("Root tag name '" + UseCaseConfig.TAG_NAME_ROOT + "' expected");
        }
        return rootElement;
    }
}

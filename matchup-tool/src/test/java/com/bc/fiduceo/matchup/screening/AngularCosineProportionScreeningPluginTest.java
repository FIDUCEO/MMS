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

package com.bc.fiduceo.matchup.screening;

import com.bc.fiduceo.util.JDomUtils;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

import static com.bc.fiduceo.util.JDomUtils.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class AngularCosineProportionScreeningPluginTest {

    public static final String PRIMARY_VARIABLE = "primary-variable";
    public static final String SECONDARY_VARIABLE = "secondary-variable";
    public static final String THRESHOLD = "threshold";

    private AngularCosineProportionScreeningPlugin plugin;

    private Element rootElement;

    @Before
    public void setUp() {
        plugin = new AngularCosineProportionScreeningPlugin();

        rootElement = new Element("angular-cosine-proportion");
        rootElement.addContent(new Element(PRIMARY_VARIABLE).setAttribute("name", "prim_angle"));
        rootElement.addContent(new Element(SECONDARY_VARIABLE).setAttribute("name", "sec_angle"));
        rootElement.addContent(new Element(THRESHOLD).setText("0.028"));
    }

    @Test
    public void testGetScreeningName() {
        assertEquals("angular-cosine-proportion", plugin.getScreeningName());
    }

    @Test
    public void testCreateScreening() {
        final Screening screening = plugin.createScreening(rootElement);

        assertNotNull(screening);
    }

    @Test
    public void testCreateConfiguration() {
        final AngularCosineProportionScreening.Configuration configuration = AngularCosineProportionScreeningPlugin.createConfiguration(rootElement);

        assertNotNull(configuration);
        assertEquals("prim_angle", configuration.primaryVariableName);
        assertEquals("sec_angle", configuration.secondaryVariableName);
        assertEquals(0.028, configuration.threshold, 1e-8);
    }

    @Test
    public void testCreateConfiguration_NoPrimaryVariable() {
        rootElement.removeChild(PRIMARY_VARIABLE);

        try {
            AngularCosineProportionScreeningPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertThat(expected.getMessage(), containsString(PRIMARY_VARIABLE));
        }
    }

    @Test
    public void testCreateConfiguration_PrimaryVariable_EmptyNameAttribute() {
        final Element child = rootElement.getChild(PRIMARY_VARIABLE);
        JDomUtils.setNameAttribute(child, "");

        try {
            AngularCosineProportionScreeningPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String message = expected.getMessage();
            assertThat(message, containsString(ATTRIBUTE_NAME__NAME));
            assertThat(message, containsString(VALUE));
        }
    }

    @Test
    public void testCreateConfiguration_PrimaryVariable_NoNameAttribute() {
        rootElement.getChild(PRIMARY_VARIABLE).removeAttribute(ATTRIBUTE_NAME__NAME);

        try {
            AngularCosineProportionScreeningPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String message = expected.getMessage();
            assertThat(message, containsString(ATTRIBUTE_NAME__NAME));
            assertThat(message, containsString(ATTRIBUTE));
        }
    }

    @Test
    public void testCreateConfiguration_NoSecondaryVariable() {
        rootElement.removeChild(SECONDARY_VARIABLE);

        try {
            AngularCosineProportionScreeningPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertThat(expected.getMessage(), containsString(SECONDARY_VARIABLE));
        }
    }


    @Test
    public void testCreateConfiguration_SecondaryVariable_EmptyNameAttribute() {
        final Element child = rootElement.getChild(SECONDARY_VARIABLE);
        JDomUtils.setNameAttribute(child, "");

        try {
            AngularCosineProportionScreeningPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String message = expected.getMessage();
            assertThat(message, containsString(ATTRIBUTE_NAME__NAME));
            assertThat(message, containsString(VALUE));
        }
    }

    @Test
    public void testCreateConfiguration_SecondaryVariable_NoNameAttribute() {
        rootElement.getChild(SECONDARY_VARIABLE).removeAttribute(ATTRIBUTE_NAME__NAME);

        try {
            AngularCosineProportionScreeningPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String message = expected.getMessage();
            assertThat(message, containsString(ATTRIBUTE_NAME__NAME));
            assertThat(message, containsString(ATTRIBUTE));
        }
    }

    @Test
    public void testCreateConfiguration_NoThreshold() {
        rootElement.removeChild(THRESHOLD);

        try {
            AngularCosineProportionScreeningPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertThat(expected.getMessage(), containsString(THRESHOLD));
        }
    }


    @Test
    public void testCreateConfiguration_Threshold_EmptyText() {
        rootElement.getChild(THRESHOLD).setText("");

        try {
            AngularCosineProportionScreeningPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String message = expected.getMessage();
            assertThat(message, equalTo("empty String"));
        }
    }
}

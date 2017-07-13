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
 */

package com.bc.fiduceo.matchup.screening;

import static com.bc.fiduceo.matchup.screening.WindowValueScreening.Evaluate.*;
import static com.bc.fiduceo.matchup.screening.WindowValueScreeningPlugin.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.matchup.SampleSet;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.*;

import java.io.IOException;
import java.util.Arrays;

public class WindowValueScreeningPluginTest {

    private ScreeningPlugin plugin;

    @Before
    public void setUp() {
        plugin = new WindowValueScreeningPlugin();
    }

    @Test
    public void testGetScreeningName() {
        assertEquals("window-value", plugin.getScreeningName());
    }

    @Test
    public void testCreateScreening() throws JDOMException, IOException {
        final Element rootElement = new Element(ROOT_TAG_NAME).addContent(Arrays.asList(
                    new Element(TAG_NAME_PRIMARY).addContent(Arrays.asList(
                                new Element(TAG_NAME_EXPRESSION).addContent("radiance_10 > 13.678"),
                                new Element(TAG_NAME_PERCENTAGE).addContent("72")
                    )),
                    new Element(TAG_NAME_SECONDARY).addContent(Arrays.asList(
                                new Element(TAG_NAME_EXPRESSION).addContent("flags != 26"),
                                new Element(TAG_NAME_PERCENTAGE).addContent("82")
                    ))
        ));
        final Screening screening = plugin.createScreening(rootElement);
        assertNotNull(screening);
        assertThat(screening, is(instanceOf(WindowValueScreening.class)));
    }

    @Test
    public void testCreateConfiguration_oneSecondary_withoutName() throws JDOMException, IOException {
        final Element rootElement = new Element(ROOT_TAG_NAME).addContent(Arrays.asList(
                    new Element(TAG_NAME_PRIMARY).addContent(Arrays.asList(
                                new Element(TAG_NAME_EXPRESSION).addContent("radiance_10 > 13.678"),
                                new Element(TAG_NAME_PERCENTAGE).addContent("72")
                    )),
                    new Element(TAG_NAME_SECONDARY).addContent(Arrays.asList(
                                new Element(TAG_NAME_EXPRESSION).addContent("flags != 26"),
                                new Element(TAG_NAME_PERCENTAGE).addContent("82")
                    ))
        ));


        final WindowValueScreening.Configuration configuration = WindowValueScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals("radiance_10 > 13.678", configuration.primaryExpression);
        assertThat(configuration.primaryPercentage, is(72.0));
        assertThat(configuration.primaryEvaluate, is(EntireWindow));

        assertThat(configuration.secondaryConfigurations, is(notNullValue()));
        assertThat(configuration.secondaryConfigurations.length, is(1));

        final WindowValueScreening.SecondaryConfiguration secConfiguration = configuration.secondaryConfigurations[0];
        assertEquals(SampleSet.getOnlyOneSecondaryKey(), secConfiguration.secondarySensorName);
        assertEquals("flags != 26", secConfiguration.secondaryExpression);
        assertThat(secConfiguration.secondaryPercentage, is(82.0));
        assertThat(secConfiguration.secondaryEvaluate, is(EntireWindow));
    }


    @Test
    public void testCreateConfiguration_onlyPrimary() throws JDOMException, IOException {
        final Element rootElement = new Element(ROOT_TAG_NAME).addContent(
                    new Element(TAG_NAME_PRIMARY).addContent(Arrays.asList(
                                new Element(TAG_NAME_EXPRESSION).addContent("radiance_10 > 13.678"),
                                new Element(TAG_NAME_PERCENTAGE).addContent("72"),
                                new Element(TAG_NAME_EVALUATE).addContent(IgnoreNoData.name())
                    ))
        );

        final WindowValueScreening.Configuration configuration = WindowValueScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals("radiance_10 > 13.678", configuration.primaryExpression);
        assertThat(configuration.primaryPercentage, is(72.0));
        assertThat(configuration.primaryEvaluate, is(IgnoreNoData));

        assertThat(configuration.secondaryConfigurations, is(notNullValue()));
        assertThat(configuration.secondaryConfigurations.length, is(0));
    }

    @Test
    public void testCreateConfiguration_onlyOneSecondary_withoutName() throws JDOMException, IOException {
        final Element rootElement = new Element(ROOT_TAG_NAME).addContent(
                    new Element(TAG_NAME_SECONDARY).addContent(Arrays.asList(
                                new Element(TAG_NAME_EXPRESSION).addContent("flags != 26"),
                                new Element(TAG_NAME_PERCENTAGE).addContent("82"),
                                new Element(TAG_NAME_EVALUATE).addContent(IgnoreNoData.name())
                    ))
        );

        final WindowValueScreening.Configuration configuration = WindowValueScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertNull(configuration.primaryExpression);
        assertNull(configuration.primaryPercentage);

        assertThat(configuration.secondaryConfigurations, is(notNullValue()));
        assertThat(configuration.secondaryConfigurations.length, is(1));

        final WindowValueScreening.SecondaryConfiguration secConfiguration = configuration.secondaryConfigurations[0];
        assertEquals(SampleSet.getOnlyOneSecondaryKey(), secConfiguration.secondarySensorName);
        assertEquals("flags != 26", secConfiguration.secondaryExpression);
        assertThat(secConfiguration.secondaryPercentage, is(82.0));
        assertThat(secConfiguration.secondaryEvaluate, is(IgnoreNoData));
    }

    @Test
    public void testCreateConfiguration_onlyOneSecondary_withThreeNames() throws JDOMException, IOException {
        final Element rootElement = new Element(ROOT_TAG_NAME).addContent(
                    new Element(TAG_NAME_SECONDARY).addContent(Arrays.asList(
                                new Element(TAG_NAME_EXPRESSION).addContent("flags != 26"),
                                new Element(TAG_NAME_PERCENTAGE).addContent("82"),
                                new Element(TAG_NAME_EVALUATE).addContent(IgnoreNoData.name())
                    )).setAttribute("names", "nameA , nameB , , , name C")  // empty names will be removed
        );

        final WindowValueScreening.Configuration configuration = WindowValueScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertNull(configuration.primaryExpression);
        assertNull(configuration.primaryPercentage);

        assertThat(configuration.secondaryConfigurations, is(notNullValue()));
        assertThat(configuration.secondaryConfigurations.length, is(3));

        String[] expectedNames = {"nameA", "nameB", "name C"};

        for (int i = 0; i < expectedNames.length; i++) {
            String expectedName = expectedNames[i];
            String message = "Test failed at index: " + i;
            final WindowValueScreening.SecondaryConfiguration secConfiguration = configuration.secondaryConfigurations[i];
            assertEquals(message, expectedName, secConfiguration.secondarySensorName);
            assertEquals(message, "flags != 26", secConfiguration.secondaryExpression);
            assertThat(message, secConfiguration.secondaryPercentage, is(82.0));
            assertThat(message, secConfiguration.secondaryEvaluate, is(IgnoreNoData));
        }
    }

    @Test
    public void testCreateConfiguration_twoSecondary_withThreeNames() throws JDOMException, IOException {
        final Element rootElement = new Element(ROOT_TAG_NAME).addContent(Arrays.asList(
                    new Element(TAG_NAME_SECONDARY).addContent(Arrays.asList(
                                new Element(TAG_NAME_EXPRESSION).addContent("flags != 26"),
                                new Element(TAG_NAME_PERCENTAGE).addContent("82"),
                                new Element(TAG_NAME_EVALUATE).addContent(EntireWindow.name())
                    )).setAttribute("names", "nameA"),
                    new Element(TAG_NAME_SECONDARY).addContent(Arrays.asList(
                                new Element(TAG_NAME_EXPRESSION).addContent("second expr"),
                                new Element(TAG_NAME_PERCENTAGE).addContent("77"),
                                new Element(TAG_NAME_EVALUATE).addContent(IgnoreNoData.name())
                    )).setAttribute("names", " , nameB , , , name C"))
        );

        final WindowValueScreening.Configuration configuration = WindowValueScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertNull(configuration.primaryExpression);
        assertNull(configuration.primaryPercentage);

        assertThat(configuration.secondaryConfigurations, is(notNullValue()));
        assertThat(configuration.secondaryConfigurations.length, is(3));

        String[] expectedNames = {"nameA", "nameB", "name C"};

        WindowValueScreening.SecondaryConfiguration secConfiguration = configuration.secondaryConfigurations[0];
        assertEquals(expectedNames[0], secConfiguration.secondarySensorName);
        assertEquals("flags != 26", secConfiguration.secondaryExpression);
        assertThat(secConfiguration.secondaryPercentage, is(82.0));
        assertThat(secConfiguration.secondaryEvaluate, is(EntireWindow));

        for (int i = 1; i < expectedNames.length; i++) {
            String expectedName = expectedNames[i];
            String message = "Test failed at index: " + i;
            secConfiguration = configuration.secondaryConfigurations[i];
            assertEquals(message, expectedName, secConfiguration.secondarySensorName);
            assertEquals(message, "second expr", secConfiguration.secondaryExpression);
            assertThat(message, secConfiguration.secondaryPercentage, is(77.0));
            assertThat(message, secConfiguration.secondaryEvaluate, is(IgnoreNoData));
        }
    }

    @Test
    public void testCreateConfiguration_twoSecondary_mixed_WithAndWithoutName() throws JDOMException, IOException {
        final Element rootElement = new Element(ROOT_TAG_NAME).addContent(Arrays.asList(
                    new Element(TAG_NAME_SECONDARY).addContent(Arrays.asList(
                                new Element(TAG_NAME_EXPRESSION).addContent("expr 1"),
                                new Element(TAG_NAME_PERCENTAGE).addContent("82"),
                                new Element(TAG_NAME_EVALUATE).addContent(EntireWindow.name())
                    )), //  <<<--- without "names" attribute declaration
                    new Element(TAG_NAME_SECONDARY).addContent(Arrays.asList(
                                new Element(TAG_NAME_EXPRESSION).addContent("expr 2"),
                                new Element(TAG_NAME_PERCENTAGE).addContent("77"),
                                new Element(TAG_NAME_EVALUATE).addContent(IgnoreNoData.name())
                    )).setAttribute("names", "nameA"))
        );

        try {
            WindowValueScreeningPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String msg = "It is not allowed to mix '" + TAG_NAME_SECONDARY + "' tags with and without 'names' attribute.";
            assertEquals(msg, expected.getMessage());
            assertEquals(RuntimeException.class.getTypeName(), expected.getClass().getTypeName());
        }
    }


    @Test
    public void testCreateConfiguration_wrongRootElementName() throws JDOMException, IOException {
        final Element rootElement = new Element("wrong-name");

        try {
            WindowValueScreeningPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String msg = "Illegal root element name 'wrong-name'. Expected root element name is 'window-value'.";
            assertEquals(msg, expected.getMessage());
        }
    }

    @Test
    public void testCreateConfiguration_onlyPrimary_missingPercentage() throws JDOMException, IOException {
        final Element rootElement = new Element(ROOT_TAG_NAME).addContent(
                    new Element(TAG_NAME_PRIMARY).addContent(
                                new Element(TAG_NAME_EXPRESSION).addContent("radiance_10 > 13.678")
                    )
        );

        try {
            WindowValueScreeningPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String msg = "Primary percentage is missing.";
            assertEquals(msg, expected.getMessage());
        }
    }

    @Test
    public void testCreateConfiguration_onlyPrimary_missingExpression() throws JDOMException, IOException {
        final Element rootElement = new Element(ROOT_TAG_NAME).addContent(
                    new Element(TAG_NAME_PRIMARY).addContent(
                                new Element(TAG_NAME_PERCENTAGE).addContent("72")
                    )
        );

        try {
            WindowValueScreeningPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String msg = "Primary expression is missing.";
            assertEquals(msg, expected.getMessage());
        }
    }

    @Test
    public void testCreateConfiguration_onlySecondary_missingPercentage() throws JDOMException, IOException {
        final Element rootElement = new Element(ROOT_TAG_NAME).addContent(
                    new Element(TAG_NAME_SECONDARY).addContent(
                                new Element(TAG_NAME_EXPRESSION).addContent("radiance_10 > 13.678")
                    )
        );

        try {
            WindowValueScreeningPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String msg = "Secondary percentage is missing.";
            assertEquals(msg, expected.getMessage());
        }
    }

    @Test
    public void testCreateConfiguration_onlySecondary_missingExpression() throws JDOMException, IOException {
        final Element rootElement = new Element(ROOT_TAG_NAME).addContent(
                    new Element(TAG_NAME_SECONDARY).addContent(
                                new Element(TAG_NAME_PERCENTAGE).addContent("72")
                    )
        );

        try {
            WindowValueScreeningPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String msg = "Secondary expression is missing.";
            assertEquals(msg, expected.getMessage());
        }
    }

    @Test
    public void testCreateConfiguration_noPrimaryAndNoSecondary() throws JDOMException, IOException {
        final Element rootElement = new Element(ROOT_TAG_NAME);

        try {
            WindowValueScreeningPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String msg = "At least primary or secondary expression must be implemented.";
            assertEquals(msg, expected.getMessage());
        }
    }

    @Test
    public void testCreateConfiguration_primaryPercentageNotParseable() throws JDOMException, IOException {
        final Element rootElement = new Element(ROOT_TAG_NAME).addContent(
                    new Element(TAG_NAME_PRIMARY).addContent(
                                new Element(TAG_NAME_PERCENTAGE).addContent("not parseable")
                    )
        );

        try {
            WindowValueScreeningPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String msg = "Invalid primary percentage 'not parseable'";
            assertEquals(msg, expected.getMessage());
        }
    }

    @Test
    public void testCreateConfiguration_secondaryPercentageNotParseable() throws JDOMException, IOException {
        final Element rootElement = new Element(ROOT_TAG_NAME).addContent(
                    new Element(TAG_NAME_SECONDARY).addContent(
                                new Element(TAG_NAME_PERCENTAGE).addContent("secondary not parseable")
                    )
        );

        try {
            WindowValueScreeningPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String msg = "Invalid secondary percentage 'secondary not parseable'";
            assertEquals(msg, expected.getMessage());
        }
    }
}
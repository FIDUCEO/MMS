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

package com.bc.fiduceo.post.plugin.sstInsitu;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_FILE_NAME_VARIABLE;
import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_INSITU_SENSOR;
import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_SECONDARY_SENSOR_MATCHUP_TIME_VARIABLE;
import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_SST_INSITU_TIME_SERIES;
import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_TIME_RANGE_SECONDS;
import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_TIME_SERIES_SIZE;
import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_VERSION;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class SstInsituTimeSeriesPluginTest {

    private SstInsituTimeSeriesPlugin plugin;
    private Element element;

    @Before
    public void setUp() {
        plugin = new SstInsituTimeSeriesPlugin();
        element = new Element(TAG_NAME_SST_INSITU_TIME_SERIES).addContent(Arrays.asList(
                new Element(TAG_NAME_VERSION).addContent("v03.3"),
                new Element(TAG_NAME_TIME_RANGE_SECONDS).addContent("" + 36 * 60 * 60),
                new Element(TAG_NAME_TIME_SERIES_SIZE).addContent("96"),
                new Element(TAG_NAME_SECONDARY_SENSOR_MATCHUP_TIME_VARIABLE).addContent("amsre.acquisition_time")
        ));
    }

    @Test
    public void testThatPluginImplementsInterfacePostProcessingPlugin() {
        assertThat(plugin, instanceOf(PostProcessingPlugin.class));
    }

    @Test
    public void testThatPluginReturnsASstInsituTimeSeriesPostProcessing() {
        final PostProcessing postProcessing = plugin.createPostProcessing(element);

        assertThat(postProcessing, is(not(nullValue())));
        assertThat(postProcessing, instanceOf(PostProcessing.class));
        assertThat(postProcessing, instanceOf(SstInsituTimeSeries.class));
    }

    @Test
    public void createPostProcessing_throwsExceptionIfTheNameOfTheRootElementIsWrong() {
        try {
            plugin.createPostProcessing(new Element("wrongName"));
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertThat(expected.getMessage(), is(equalTo("Illegal XML Element. Tagname '" + plugin.getPostProcessingName() + "' expected.")));
        }
    }

    @Test
    public void testParseConfiguration() {
        final SstInsituTimeSeries.Configuration configuration = SstInsituTimeSeriesPlugin.parseConfiguration(element);
        assertEquals("v03.3", configuration.processingVersion);
        assertEquals(36 * 60 * 60, configuration.timeRangeSeconds);
        assertEquals(96, configuration.timeSeriesSize);
        assertEquals("amsre.acquisition_time", configuration.matchupTimeVarName);
        assertNull(configuration.insituSensorName);
    }

    @Test
    public void testParseConfiguration_withSensorName() {
        element.addContent(new Element(TAG_NAME_INSITU_SENSOR).addContent("bottle-sst"));

        final SstInsituTimeSeries.Configuration configuration = SstInsituTimeSeriesPlugin.parseConfiguration(element);
        assertEquals("v03.3", configuration.processingVersion);
        assertEquals(36 * 60 * 60, configuration.timeRangeSeconds);
        assertEquals(96, configuration.timeSeriesSize);
        assertEquals("amsre.acquisition_time", configuration.matchupTimeVarName);
        assertEquals("bottle-sst", configuration.insituSensorName);
    }

    @Test
    public void testParseConfiguration_withSensorName_emptyTag() {
        element.addContent(new Element(TAG_NAME_INSITU_SENSOR));

        try {
            SstInsituTimeSeriesPlugin.parseConfiguration(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testParseConfiguration_withFileName() {
        element.addContent(new Element(TAG_NAME_FILE_NAME_VARIABLE).addContent("the_data_file"));

        final SstInsituTimeSeries.Configuration configuration = SstInsituTimeSeriesPlugin.parseConfiguration(element);
        assertEquals("the_data_file", configuration.fileNameVariableName);
    }

    @Test
    public void testParseConfiguration_withFileName_emptyTag() {
        element.addContent(new Element(TAG_NAME_FILE_NAME_VARIABLE));

        try {
            SstInsituTimeSeriesPlugin.parseConfiguration(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}
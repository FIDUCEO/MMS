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

import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_SECONDARY_SENSOR_MATCHUP_TIME_VARIABLE;
import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_SST_INSITU_TIME_SERIES;
import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_TIME_RANGE_SECONDS;
import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_TIME_SERIES_SIZE;
import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_VERSION;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import org.jdom.Element;
import org.junit.*;

import java.util.Arrays;

public class SstInsituTimeSeriesPluginTest {

    private SstInsituTimeSeriesPlugin plugin;
    private Element element;

    @Before
    public void setUp() throws Exception {
        plugin = new SstInsituTimeSeriesPlugin();
        element = new Element(TAG_NAME_SST_INSITU_TIME_SERIES).addContent(Arrays.asList(
                new Element(TAG_NAME_VERSION).addContent("v03.3"),
                new Element(TAG_NAME_TIME_RANGE_SECONDS).addContent("" + 36 * 60 * 60),
                new Element(TAG_NAME_TIME_SERIES_SIZE).addContent("96"),
                new Element(TAG_NAME_SECONDARY_SENSOR_MATCHUP_TIME_VARIABLE).addContent("amsre.acquisition_time")
        ));
    }

    @Test
    public void testThatPluginImplementsInterfacePostProcessingPlugin() throws Exception {
        assertThat(plugin, instanceOf(PostProcessingPlugin.class));
    }

    @Test
    public void testThatPluginReturnsASstInsituTimeSeriesPostProcessing() throws Exception {
        final PostProcessing postProcessing = plugin.createPostProcessing(element);

        assertThat(postProcessing, is(not(nullValue())));
        assertThat(postProcessing, instanceOf(PostProcessing.class));
        assertThat(postProcessing, instanceOf(SstInsituTimeSeries.class));
        final SstInsituTimeSeries insituTimeSeries = (SstInsituTimeSeries) postProcessing;
        // @todo 3 tb/** use configuration class instead of checking fields of prost-processing 2016-12-23
        assertThat(insituTimeSeries.processingVersion, is(equalTo("v03.3")));
        assertThat(insituTimeSeries.timeRangeSeconds, is(equalTo(36 * 60 * 60)));
        assertThat(insituTimeSeries.timeSeriesSize, is(equalTo(96)));
        assertThat(insituTimeSeries.matchupTimeVarName, is(equalTo("amsre.acquisition_time")));
    }

    @Test
    public void chreatePostProcessing_throwsExceptionIfTheNameOfTheRootElementIsWrong() throws Exception {
        try {
            plugin.createPostProcessing(new Element("wrongName"));
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertThat(expected.getMessage(), is(equalTo("Illegal XML Element. Tagname '" + plugin.getPostProcessingName() + "' expected.")));
        }

    }
}
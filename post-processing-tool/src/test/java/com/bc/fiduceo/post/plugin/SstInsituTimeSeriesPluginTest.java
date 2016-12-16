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

package com.bc.fiduceo.post.plugin;

import static com.bc.fiduceo.post.plugin.SstInsituTimeSeriesPlugin.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import org.esa.snap.core.util.Debug;
import org.jdom.Element;
import org.junit.*;

import java.util.Arrays;

/**
 * Created by Sabine on 08.12.2016.
 */
public class SstInsituTimeSeriesPluginTest {

    private SstInsituTimeSeriesPlugin plugin;
    private Element element;

    @Before
    public void setUp() throws Exception {
        plugin = new SstInsituTimeSeriesPlugin();
        element = new Element(TAG_NAME_SST_INSITU_TIME_SERIES).addContent(Arrays.asList(
                    new Element(TAG_NAME_VERSION).addContent("v03.3"),
                    new Element(TAG_NAME_TIME_RANGE_SECONDS).addContent("" + 36 * 60 * 60),
                    new Element(TAG_NAME_TIME_SERIES_SIZE).addContent("96")
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
        assertThat(insituTimeSeries.processingVersion, is(equalTo("v03.3")));
        assertThat(insituTimeSeries.timeRangeSeconds, is(equalTo(36 * 60 * 60)));
        assertThat(insituTimeSeries.timeSeriesSize, is(equalTo(96)));
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
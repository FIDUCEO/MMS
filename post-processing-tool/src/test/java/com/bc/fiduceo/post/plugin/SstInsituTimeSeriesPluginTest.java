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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import com.sun.corba.se.impl.presentation.rmi.DynamicMethodMarshallerImpl;
import org.jdom.Element;
import org.junit.*;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.rewrite.Rewrite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Created by Sabine on 08.12.2016.
 */
public class SstInsituTimeSeriesPluginTest {

    private SstInsituTimeSeriesPlugin plugin;

    @Before
    public void setUp() throws Exception {
        plugin = new SstInsituTimeSeriesPlugin();
    }

    @Test
    public void testThatPluginImplementsInterfacePostProcessingPlugin() throws Exception {
        assertThat(plugin, instanceOf(PostProcessingPlugin.class));
    }

    @Test
    public void testThatPluginReturnsASstInsituTimeSeriesPostProcessing() throws Exception {
        final Element jDomElement = new Element("name");
        final PostProcessing postProcessing = plugin.createPostProcessing(jDomElement);

        assertThat(postProcessing, is(not(nullValue())));
        assertThat(postProcessing, instanceOf(PostProcessing.class));
        assertThat(postProcessing, instanceOf(SstInsituTimeSeries.class));
    }
}
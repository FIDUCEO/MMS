/*
 * Copyright (C) 2018 Brockmann Consult GmbH
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

package com.bc.fiduceo.reader.airs;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;
import org.junit.*;

public class AIRS_L1B_ReaderPluginTest {

    private AIRS_L1B_ReaderPlugin plugin;

    @Before
    public void setUp() throws Exception {
        plugin = new AIRS_L1B_ReaderPlugin();
    }

    @Test
    public void testImplementsReaderPluginInterface() {
        assertThat(plugin, instanceOf(ReaderPlugin.class));
    }

    @Test
    public void testGetSupportedSensorKeys() throws Exception {
        final String[] expected = new String[]{"airs-aq"};
        final String[] keys = plugin.getSupportedSensorKeys();
        assertArrayEquals(expected, keys);
    }

    @Test
    public void testCreateReaderInstance() throws Exception {
        //preparation
        final ReaderContext readerContext = new ReaderContext();

        //execution
        final Reader reader = plugin.createReader(readerContext);

        //verification
        assertThat(reader, is(notNullValue()));
        assertThat(reader, is(instanceOf(AIRS_L1B_Reader.class)));
        final AIRS_L1B_Reader airs_reader = (AIRS_L1B_Reader) reader;
        assertThat(airs_reader.readerContext, is(sameInstance(readerContext)));
    }

    @Test
    public void testDataType() {
        final DataType dataType = plugin.getDataType();

        assertThat(dataType, is(equalTo(DataType.POLAR_ORBITING_SATELLITE)));
    }
}

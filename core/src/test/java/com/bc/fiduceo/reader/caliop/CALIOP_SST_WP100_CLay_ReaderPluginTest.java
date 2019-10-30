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
package com.bc.fiduceo.reader.caliop;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CALIOP_SST_WP100_CLay_ReaderPluginTest {

    private CALIOP_SST_WP100_CLay_ReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new CALIOP_SST_WP100_CLay_ReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKeys() {
        //execution
        final String[] supportedSensorKeys = plugin.getSupportedSensorKeys();

        //verification
        final String[] expected = {"caliop_clay-cal"};
        assertThat(supportedSensorKeys, is(equalTo(expected)));
    }

    @Test
    public void testGetDataType() {
        //execution
        final DataType dataType = plugin.getDataType();

        //verification
        final DataType expected = DataType.POLAR_ORBITING_SATELLITE;
        assertThat(dataType, is(equalTo(expected)));
    }

    @Test
    public void testCreateReader() {
        //preparation
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        //execution
        final Reader reader = plugin.createReader(readerContext);

        //verification
        final Class<CALIOP_SST_WP100_CLay_Reader> expectedType = CALIOP_SST_WP100_CLay_Reader.class;
        assertThat(reader, is(instanceOf(expectedType)));
        final CALIOP_SST_WP100_CLay_Reader clayReader = (CALIOP_SST_WP100_CLay_Reader) reader;
        assertThat(clayReader.geometryFactory, is(sameInstance(readerContext.getGeometryFactory())));
        assertThat(clayReader.caliopUtils, is(notNullValue()));
        assertThat(clayReader.caliopUtils, is(instanceOf(CaliopUtils.class)));
    }
}
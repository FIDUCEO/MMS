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

package com.bc.fiduceo.reader.atsr;

import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.junit.Before;
import org.junit.Test;
import ucar.nc2.Attribute;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VariableProxyTest {

    private RasterDataNode rasterDataNode;
    private VariableProxy proxy;

    @Before
    public void setUp() {
        rasterDataNode = mock(RasterDataNode.class);
        proxy = new VariableProxy(rasterDataNode);
    }

    @Test
    public void testGetShortName() {
        when(rasterDataNode.getName()).thenReturn("The_node");

        assertEquals("The_node", proxy.getShortName());
    }

    @Test
    public void testGetDataType() {
        when(rasterDataNode.getDataType()).thenReturn(ProductData.TYPE_FLOAT32);

        assertEquals("float", proxy.getDataType().toString());

        when(rasterDataNode.getDataType()).thenReturn(ProductData.TYPE_INT16);

        assertEquals("short", proxy.getDataType().toString());
    }

    @Test
    public void testGetAttributes_noScaling() {
        final Band band = new Band("test", 12, 3, 3);
        band.setScalingOffset(0.0);
        band.setScalingFactor(1.0);

        proxy = new VariableProxy(band);

        final List<Attribute> attributes = proxy.getAttributes();
        assertEquals(0, attributes.size());
    }

    @Test
    public void testGetAttributes_scalingAndOffset() {
        final Band band = new Band("test", 12, 3, 3);
        band.setScalingOffset(0.86);
        band.setScalingFactor(1.23);

        proxy = new VariableProxy(band);

        final List<Attribute> attributes = proxy.getAttributes();
        assertEquals(2, attributes.size());

        Attribute attribute = attributes.get(0);
        assertEquals("scale_factor", attribute.getShortName());
        assertEquals("1.23", attribute.getStringValue());

        attribute = attributes.get(1);
        assertEquals("add_offset", attribute.getShortName());
        assertEquals("0.86", attribute.getStringValue());
    }

    @Test
    public void testGetAttributes_onlyScaling() {
        final Band band = new Band("test", 12, 3, 3);
        band.setScalingFactor(0.01);

        proxy = new VariableProxy(band);

        final List<Attribute> attributes = proxy.getAttributes();
        assertEquals(1, attributes.size());

        final Attribute attribute = attributes.get(0);
        assertEquals("scale_factor", attribute.getShortName());
        assertEquals("0.01", attribute.getStringValue());
    }

    @Test
    public void testGetAttributes_onlyOffset() {
        final Band band = new Band("test", 12, 3, 3);
        band.setScalingOffset(273.15);

        proxy = new VariableProxy(band);

        final List<Attribute> attributes = proxy.getAttributes();
        assertEquals(1, attributes.size());

        final Attribute attribute = attributes.get(0);
        assertEquals("add_offset", attribute.getShortName());
        assertEquals("273.15", attribute.getStringValue());
    }

    @Test
    public void testGetAttributes_fillValue() {
        when(rasterDataNode.isNoDataValueUsed()).thenReturn(true);
        when(rasterDataNode.getNoDataValue()).thenReturn(-32768.0);

        final List<Attribute> attributes = proxy.getAttributes();
        assertEquals(1, attributes.size());

        final Attribute attribute = attributes.get(0);
        assertEquals("_FillValue", attribute.getShortName());
        assertEquals("-32768.0", attribute.getStringValue());
    }
}

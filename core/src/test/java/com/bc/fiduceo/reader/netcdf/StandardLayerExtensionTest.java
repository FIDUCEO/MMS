package com.bc.fiduceo.reader.netcdf;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StandardLayerExtensionTest {

    @Test
    public void testGetExtension_noOffset() {
        final LayerExtension layerExtension = new StandardLayerExtension();

        assertEquals("_ch01", layerExtension.getExtension(0));
        assertEquals("_ch05", layerExtension.getExtension(4));
        assertEquals("_ch11", layerExtension.getExtension(10));
    }

    @Test
    public void testGetExtension_withOffset() {
        final LayerExtension layerExtension = new StandardLayerExtension(4);

        assertEquals("_ch05", layerExtension.getExtension(0));
        assertEquals("_ch09", layerExtension.getExtension(4));
        assertEquals("_ch15", layerExtension.getExtension(10));
    }
}

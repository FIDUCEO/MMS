package com.bc.fiduceo.reader.netcdf;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StandardLayerExtensionTest {

    @Test
    public void testGetExtension_noOffset() {
        final LayerExtension layerExtension = new StandardLayerExtension();

        assertEquals("01", layerExtension.getExtension(0));
        assertEquals("05", layerExtension.getExtension(4));
        assertEquals("11", layerExtension.getExtension(10));
    }

    @Test
    public void testGetExtension_withOffset() {
        final LayerExtension layerExtension = new StandardLayerExtension(4);

        assertEquals("05", layerExtension.getExtension(0));
        assertEquals("09", layerExtension.getExtension(4));
        assertEquals("15", layerExtension.getExtension(10));
    }
}

package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.reader.netcdf.LayerExtension;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ModisL1ReflectiveExtensionTest {

    @Test
    public void testGetExtension_1km_reflective() {
        final LayerExtension layerExtension = new ModisL1ReflectiveExtension();
        assertEquals("_ch08", layerExtension.getExtension(0));
        assertEquals("_ch12", layerExtension.getExtension(4));
        assertEquals("_ch13L", layerExtension.getExtension(5));
        assertEquals("_ch13H", layerExtension.getExtension(6));
        assertEquals("_ch14L", layerExtension.getExtension(7));
        assertEquals("_ch14H", layerExtension.getExtension(8));
        assertEquals("_ch15", layerExtension.getExtension(9));
        assertEquals("_ch19", layerExtension.getExtension(13));
        assertEquals("_ch26", layerExtension.getExtension(14));
    }
}

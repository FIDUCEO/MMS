package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.reader.netcdf.LayerExtension;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ModisL1ReflectiveExtensionTest {

    @Test
    public void testGetExtension_1km_reflective() {
        final LayerExtension layerExtension = new ModisL1ReflectiveExtension();
        assertEquals("08", layerExtension.getExtension(0));
        assertEquals("12", layerExtension.getExtension(4));
        assertEquals("13L", layerExtension.getExtension(5));
        assertEquals("13H", layerExtension.getExtension(6));
        assertEquals("14L", layerExtension.getExtension(7));
        assertEquals("14H", layerExtension.getExtension(8));
        assertEquals("15", layerExtension.getExtension(9));
        assertEquals("19", layerExtension.getExtension(13));
        assertEquals("26", layerExtension.getExtension(14));
    }
}

package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.reader.netcdf.LayerExtension;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ModisL1EmissiveExtensionTest {

    @Test
    public void testGetExtension_emissive() {
        final LayerExtension layerExtension = new ModisL1EmissiveExtension();

        assertEquals("20", layerExtension.getExtension(0));
        assertEquals("25", layerExtension.getExtension(5));
        assertEquals("27", layerExtension.getExtension(6));
        assertEquals("36", layerExtension.getExtension(15));
    }
}

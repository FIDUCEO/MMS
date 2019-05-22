package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Nadir1kmTransformTest {

    @Test
    public void testGetRasterSize() {
        final Nadir1kmTransform transform = new Nadir1kmTransform(200, 180);

        final Dimension rasterSize = transform.getRasterSize();
        assertEquals(100, rasterSize.getNx());
        assertEquals(90, rasterSize.getNy());
    }
}

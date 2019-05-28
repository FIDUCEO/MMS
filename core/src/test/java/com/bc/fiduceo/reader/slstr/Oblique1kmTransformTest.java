package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Oblique1kmTransformTest {

    @Test
    public void testGetRasterSize() {
        final Oblique1kmTransform transform = new Oblique1kmTransform(2000, 1800, 256);

        final Dimension rasterSize = transform.getRasterSize();
        assertEquals(400, rasterSize.getNx());
        assertEquals(900, rasterSize.getNy());
    }

    // @todo 1 tb/tb continue here 2019-05-28
//    @Test
//    public void testMapCoordinate() {
//        final Oblique1kmTransform transform = new Oblique1kmTransform(2000, 1800, 548);
//
//        assertEquals(123, transform.mapCoordinate(123));
//        assertEquals(4000, transform.mapCoordinate(4000));
//    }
}

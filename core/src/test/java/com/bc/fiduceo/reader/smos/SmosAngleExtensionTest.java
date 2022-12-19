package com.bc.fiduceo.reader.smos;

import com.bc.fiduceo.reader.netcdf.LayerExtension;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SmosAngleExtensionTest {

    private LayerExtension extension;

    @Before
    public void setUp() {
        extension = new SmosAngleExtension();
    }

    @Test
    public void testGetExtension() {
        assertEquals("_025", extension.getExtension(0));
        assertEquals("_275", extension.getExtension(5));
        assertEquals("_525", extension.getExtension(11));
    }

    @Test
    public void testGetExtension_outsideRange() {
        try {
            extension.getExtension(-1);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }

        try {
            extension.getExtension(14);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetIndex() {
        assertEquals(0, extension.getIndex("_025"));
        assertEquals(5, extension.getIndex("_275"));
        assertEquals(11, extension.getIndex("_525"));
    }

    @Test
    public void testGetIndex_invalid() {
        try {
            extension.getIndex("heffalump");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }

        try {
            extension.getIndex("");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}

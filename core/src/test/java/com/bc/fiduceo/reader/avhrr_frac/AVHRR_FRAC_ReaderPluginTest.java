package com.bc.fiduceo.reader.avhrr_frac;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AVHRR_FRAC_ReaderPluginTest {

    private AVHRR_FRAC_ReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new AVHRR_FRAC_ReaderPlugin();
    }

    @Test
    public void testCreateReader() {
        final Reader reader = plugin.createReader(new ReaderContext());
        assertNotNull(reader);
        assertTrue(reader instanceof AVHRR_FRAC_Reader);
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.POLAR_ORBITING_SATELLITE, plugin.getDataType());
    }

    @Test
    public void testGetSupportedSensorKeys(){
        final String[] expected = {"avhrr-frac-ma", "avhrr-frac-mb"};
        final String[] keys = plugin.getSupportedSensorKeys();

        assertArrayEquals(expected, keys);
    }
}

package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.reader.DataType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SlstrRegriddedSubsetReaderPluginTest {

    private SlstrRegriddedSubsetReaderPlugin plugin;

    @Before
    public void setUp() throws Exception {
        plugin = new SlstrRegriddedSubsetReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKeys() {
        final String[] keys = plugin.getSupportedSensorKeys();
        assertEquals(2, keys.length);
        assertEquals("slstr-s3a-uor", keys[0]);
        assertEquals("slstr-s3b-uor", keys[1]);
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.POLAR_ORBITING_SATELLITE, plugin.getDataType());
    }

    @Test
    public void testCreateReader() {
        final SlstrRegriddedSubsetReader reader = (SlstrRegriddedSubsetReader) plugin.createReader(null);
        assertNotNull(reader);
    }
}

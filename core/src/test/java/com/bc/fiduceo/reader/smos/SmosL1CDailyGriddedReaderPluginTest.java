package com.bc.fiduceo.reader.smos;

import com.bc.fiduceo.reader.DataType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SmosL1CDailyGriddedReaderPluginTest {

    private SmosL1CDailyGriddedReaderPlugin plugin;

    @Before
    public void setUp() throws Exception {
        plugin = new SmosL1CDailyGriddedReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKeys() {
        final String[] keys = plugin.getSupportedSensorKeys();
        assertEquals(1, keys.length);
        assertEquals("miras-smos-daily-gridded", keys[0]);
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.POLAR_ORBITING_SATELLITE, plugin.getDataType());
    }

    @Test
    public void testCreateReader() {
        final SmosL1CDailyGriddedReader reader = (SmosL1CDailyGriddedReader) plugin.createReader(null);
        assertNotNull(reader);
    }
}

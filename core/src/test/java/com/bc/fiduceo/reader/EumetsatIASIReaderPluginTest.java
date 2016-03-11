package com.bc.fiduceo.reader;

import org.junit.*;

import static org.junit.Assert.*;

public class EumetsatIASIReaderPluginTest {

    private EumetsatIASIReaderPlugin plugin;

    @Before
    public void setUp() throws Exception {
        plugin = new EumetsatIASIReaderPlugin();
    }


    @Test
    public void testGetSensorKeys() {
        final String[] expected = new String[]{"iasi-ma", "iasi-mb"};
        final String[] keys = plugin.getSupportedSensorKeys();
        assertArrayEquals(expected, keys);
    }

    @Test
    public void testCreateReader() throws Exception {
        final Reader reader = plugin.createReader();
        assertNotNull(reader);
        assertTrue(reader instanceof EumetsatIASIReader);

    }
}

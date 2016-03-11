package com.bc.fiduceo.reader;

import org.junit.*;

import static org.junit.Assert.*;

public class AIRS_L1B_ReaderPluginTest {

    private AIRS_L1B_ReaderPlugin plugin;

    @Before
    public void setUp() throws Exception {
        plugin = new AIRS_L1B_ReaderPlugin();
    }

    @Test
    public void testImplementsReaderPluginInterface() {
        assertTrue(plugin instanceof ReaderPlugin);
    }

    @Test
    public void testGetSupportedSensorKeys() throws Exception {
        final String[] expected = new String[]{"airs-aq"};
        final String[] keys = plugin.getSupportedSensorKeys();
        assertArrayEquals(expected, keys);
    }

    @Test
    public void testCreateReaderInstance() throws Exception {
        final Reader reader = plugin.createReader();
        assertNotNull(reader);
        assertTrue(reader instanceof AIRS_L1B_Reader);
    }
}

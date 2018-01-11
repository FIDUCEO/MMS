package com.bc.fiduceo.reader.amsr2;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AMSR2_ReaderPluginTest {

    private AMSR2_ReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new AMSR2_ReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKeys() {
        final String[] keys = plugin.getSupportedSensorKeys();

        assertArrayEquals(new String[]{"amsr2-gcw1"}, keys);
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.POLAR_ORBITING_SATELLITE, plugin.getDataType());
    }

    @Test
    public void testCreateReader() {
        final Reader reader = plugin.createReader(null);
        assertNotNull(reader);
        assertTrue(reader instanceof AMSR2_Reader);
    }
}

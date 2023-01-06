package com.bc.fiduceo.reader.windsat;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class WindsatReaderPluginTest {

    private WindsatReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new WindsatReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKey() {
        final String[] keys = plugin.getSupportedSensorKeys();

        assertEquals(1, keys.length);
        assertEquals("windsat-coriolis", keys[0]);
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.POLAR_ORBITING_SATELLITE, plugin.getDataType());
    }

    @Test
    public void testCreateReader() {
        final Reader reader = plugin.createReader(new ReaderContext());
        assertNotNull(reader);
        assertTrue(reader instanceof WindsatReader);
    }
}

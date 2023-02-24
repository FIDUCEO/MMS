package com.bc.fiduceo.reader.insitu.ndbc;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class NdbcCWReaderPluginTest {

    private NdbcCWReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new NdbcCWReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKeys() throws Exception {
        final String[] expected = {"ndbc-cw-ob", "ndbc-cw-cb", "ndbc-cw-lb", "ndbc-cw-os", "ndbc-cw-cs", "ndbc-cw-ls"};

        final String[] sensorKeys = plugin.getSupportedSensorKeys();
        assertArrayEquals(expected, sensorKeys);
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.INSITU, plugin.getDataType());
    }

    @Test
    public void testCreateReader() {
        final Reader reader = plugin.createReader(null);
        assertNotNull(reader);
        assertTrue(reader instanceof NdbcCWReader);
    }
}

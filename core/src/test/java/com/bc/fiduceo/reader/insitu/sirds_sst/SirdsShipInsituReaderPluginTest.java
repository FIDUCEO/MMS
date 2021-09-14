package com.bc.fiduceo.reader.insitu.sirds_sst;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SirdsShipInsituReaderPluginTest {

    private SirdsShipInsituReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new SirdsShipInsituReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKeys() throws Exception {
        final String[] expected = {"ship-sirds"};

        final String[] sensorKeys = plugin.getSupportedSensorKeys();
        assertArrayEquals(expected, sensorKeys);
    }

    @Test
    public void testCreateReader() {
        final Reader reader = plugin.createReader(null);
        assertNotNull(reader);
        assertTrue(reader instanceof SirdsInsituReader);
        assertEquals("ship-sirds", ((SirdsInsituReader) reader).getSensorKey());
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.INSITU, plugin.getDataType());
    }
}

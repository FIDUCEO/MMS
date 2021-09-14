package com.bc.fiduceo.reader.insitu.sirds_sst;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class SirdsAnimalInsituReaderPluginTest {

    private SirdsAnimalInsituReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new SirdsAnimalInsituReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKeys() throws Exception {
        final String[] expected = {"animal-sirds"};

        final String[] sensorKeys = plugin.getSupportedSensorKeys();
        assertArrayEquals(expected, sensorKeys);
    }

    @Test
    public void testCreateReader() {
        final Reader reader = plugin.createReader(null);
        assertNotNull(reader);
        assertTrue(reader instanceof SirdsInsituReader);
        assertEquals("animal-sirds", ((SirdsInsituReader) reader).getSensorKey());
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.INSITU, plugin.getDataType());
    }
}

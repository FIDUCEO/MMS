package com.bc.fiduceo.reader.insitu.gruan_uleic;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GruanUleicInsituReaderPluginTest {

    private GruanUleicInsituReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new GruanUleicInsituReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKeys() {
        final String[] expected = {"gruan-uleic"};

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
        assertTrue(reader instanceof GruanUleicInsituReader);
    }

}

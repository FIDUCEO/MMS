package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DTUSIC1SicInsituReaderPluginTest {

    private DTUSIC1SicInsituReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new DTUSIC1SicInsituReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKeys() {
        final String[] expected = {"DTUSIC1-sic-cci"};

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
        assertTrue(reader instanceof SicCciInsituReader);

        assertEquals(".*DTUSIC1.*.text", reader.getRegEx());
    }
}

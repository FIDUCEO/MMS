package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class SciCciInsituReaderPluginTest {

    private SicCciInsituReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new SicCciInsituReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKeys() {
        final String[] expected = {"sic-cci-dmisic0"};

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
    }
}

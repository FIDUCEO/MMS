package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SlstrNRReaderPluginTest {

    private SlstrNRReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new SlstrNRReaderPlugin();
    }

    @Test
    public void testCreateReader() {
        final Reader reader = plugin.createReader(new ReaderContext());
        assertNotNull(reader);
        assertTrue(reader instanceof SlstrReader);
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.POLAR_ORBITING_SATELLITE, plugin.getDataType());
    }

    @Test
    public void testGetSupportedSensorKeys(){
        final String[] expected = {"slstr-s3a-nr", "slstr-s3b-nr"};
        final String[] keys = plugin.getSupportedSensorKeys();

        assertArrayEquals(expected, keys);
    }
}

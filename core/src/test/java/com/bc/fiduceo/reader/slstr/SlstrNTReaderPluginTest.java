package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SlstrNTReaderPluginTest {

    private SlstrNTReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new SlstrNTReaderPlugin();
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
        final String[] expected = {"slstr-s3a-nt", "slstr-s3b-nt"};
        final String[] keys = plugin.getSupportedSensorKeys();

        assertArrayEquals(expected, keys);
    }
}

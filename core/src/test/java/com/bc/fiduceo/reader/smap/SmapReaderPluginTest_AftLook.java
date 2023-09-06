package com.bc.fiduceo.reader.smap;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SmapReaderPluginTest_AftLook {

    private SmapReaderPluginAftLook plugin;

    @Before
    public void setUp() {
        plugin = new SmapReaderPluginAftLook();
    }

    @Test
    public void testGetSupportedSensorKey() {
        final String[] keys = plugin.getSupportedSensorKeys();

        assertEquals(1, keys.length);
        assertEquals("smap-sss-aft", keys[0]);
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.POLAR_ORBITING_SATELLITE, plugin.getDataType());
    }

    @Test
    public void testCreateReader() {
        final Reader reader = plugin.createReader(new ReaderContext());
        assertNotNull(reader);
        assertTrue(reader instanceof SmapReader);
        final SmapReader smapReader = (SmapReader) reader;
        assertEquals(1, smapReader.lookValue);
    }
}

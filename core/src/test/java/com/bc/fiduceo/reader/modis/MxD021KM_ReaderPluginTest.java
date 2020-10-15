package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MxD021KM_ReaderPluginTest {

    private MxD021KM_ReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new MxD021KM_ReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKeys() {
        final String[] expected = {"mod021km-te", "myd021km-aq"};

        final String[] keys = plugin.getSupportedSensorKeys();
        assertArrayEquals(expected, keys);
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.POLAR_ORBITING_SATELLITE, plugin.getDataType());
    }

    @Test
    public void testCreateReader() {
        final Reader reader = plugin.createReader(new ReaderContext());// we don't need a geometry factory for this test tb 2020-05-13
        assertNotNull(reader);
        assertTrue(reader instanceof MxD021KM_Reader);
    }
}

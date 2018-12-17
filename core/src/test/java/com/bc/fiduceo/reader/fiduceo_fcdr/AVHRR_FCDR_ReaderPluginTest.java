package com.bc.fiduceo.reader.fiduceo_fcdr;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AVHRR_FCDR_ReaderPluginTest {

    private AVHRR_FCDR_ReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new AVHRR_FCDR_ReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKeys() {
        final String[] expected = {"avhrr-n11-fcdr", "avhrr-n12-fcdr", "avhrr-n14-fcdr", "avhrr-n15-fcdr", "avhrr-n16-fcdr", "avhrr-n17-fcdr", "avhrr-n18-fcdr", "avhrr-n19-fcdr", "avhrr-ma-fcdr"};
        final String[] keys = plugin.getSupportedSensorKeys();

        assertArrayEquals(expected, keys);
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.POLAR_ORBITING_SATELLITE, plugin.getDataType());
    }

    @Test
    public void testCreateReader() {
        final Reader reader = plugin.createReader(new ReaderContext());
        assertNotNull(reader);
        assertTrue(reader instanceof AVHRR_FCDR_Reader);
    }
}

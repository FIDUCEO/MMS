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

public class HIRS_FCDR_ReaderPluginTest {

    private HIRS_FCDR_ReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new HIRS_FCDR_ReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKeys() {
        final String[] expected = {"hirs-n06-fcdr", "hirs-n07-fcdr", "hirs-n08-fcdr", "hirs-n09-fcdr", "hirs-n10-fcdr", "hirs-n11-fcdr", "hirs-n12-fcdr", "hirs-n14-fcdr", "hirs-n15-fcdr", "hirs-n16-fcdr", "hirs-n17-fcdr", "hirs-n18-fcdr", "hirs-n19-fcdr", "hirs-ma-fcdr", "hirs-mp-fcdr"};
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

        assertTrue(reader instanceof HIRS_FCDR_Reader);
    }
}

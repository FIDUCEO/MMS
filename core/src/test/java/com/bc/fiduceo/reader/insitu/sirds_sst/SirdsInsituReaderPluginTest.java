package com.bc.fiduceo.reader.insitu.sirds_sst;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SirdsInsituReaderPluginTest {

    private SirdsInsituReaderPlugin plugin;

    @Before
    public void setUp() throws Exception {
        plugin = new SirdsInsituReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKeys() throws Exception {
        final String[] expected = {"animal-sirds", "argo-sirds", "argo_surf-sirds", "bottle-sirds", "ctd-sirds", "drifter-sirds", "drifter_cmems-sirds", "gtmba-sirds", "mbt-sirds", "mooring-sirds", "ship-sirds", "xbt-sirds"};

        final String[] sensorKeys = plugin.getSupportedSensorKeys();
        assertArrayEquals(expected, sensorKeys);
    }

    @Test
    public void testCreateReader() {
        final Reader reader = plugin.createReader(null);
        assertNotNull(reader);
        assertTrue(reader instanceof SirdsInsituReader);
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.INSITU, plugin.getDataType());
    }
}

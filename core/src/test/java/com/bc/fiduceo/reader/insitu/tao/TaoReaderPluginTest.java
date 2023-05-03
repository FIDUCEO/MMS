package com.bc.fiduceo.reader.insitu.tao;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TaoReaderPluginTest {

    private TaoReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new TaoReaderPlugin();
    }

    @Test
    public void testGetSupportedKeys() {
        final String[] expected = {"tao-sss"};

        assertArrayEquals(expected, plugin.getSupportedSensorKeys());
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.INSITU, plugin.getDataType());
    }

    @Test
    public void testCreateReader(){
        final Reader reader = plugin.createReader(null);
        assertTrue(reader instanceof TaoReader);
    }
}

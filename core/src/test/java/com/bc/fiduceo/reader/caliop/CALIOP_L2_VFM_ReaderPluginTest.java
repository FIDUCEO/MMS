package com.bc.fiduceo.reader.caliop;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import org.junit.*;

public class CALIOP_L2_VFM_ReaderPluginTest {

    private CALIOP_L2_VFM_ReaderPlugin plugin;

    @Before
    public void setUp() throws Exception {
        plugin = new CALIOP_L2_VFM_ReaderPlugin();
    }

    @Test
    public void createReader() throws Exception {
        //execution
        final Reader reader = plugin.createReader(null);

        //verification
        final Class<CALIOP_L2_VFM_Reader> expectedType = CALIOP_L2_VFM_Reader.class;
        assertThat(reader, is(instanceOf(expectedType)));
    }

    @Test
    public void getSupportedSensorKeys() throws Exception {
        //execution
        final String[] supportedSensorKeys = plugin.getSupportedSensorKeys();

        //verification
        final String[] expected = {"caliop_vfm-cal"};
        assertThat(supportedSensorKeys, is(equalTo(expected)));
    }

    @Test
    public void getDataType() throws Exception {
        //execution
        final DataType dataType = plugin.getDataType();

        //verification
        final DataType expected = DataType.POLAR_ORBITING_SATELLITE;
        assertThat(dataType, is(equalTo(expected)));
    }

}
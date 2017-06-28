package com.bc.fiduceo.reader.calipso;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import org.junit.*;

/**
 * Created by Sabine on 28.06.2017.
 */
public class CALIPSO_L2_VFM_ReaderPluginTest {

    private CALIPSO_L2_VFM_ReaderPlugin plugin;

    @Before
    public void setUp() throws Exception {
        plugin = new CALIPSO_L2_VFM_ReaderPlugin();
    }

    @Test
    public void createReader() throws Exception {
        //execution
        final Reader reader = plugin.createReader(null);

        //verification
        final Class<CALIPSO_L2_VFM_Reader> expectedType = CALIPSO_L2_VFM_Reader.class;
        assertThat(reader, is(instanceOf(expectedType)));
    }

    @Test
    public void getSupportedSensorKeys() throws Exception {
        //execution
        final String[] supportedSensorKeys = plugin.getSupportedSensorKeys();

        //verification
        final String[] expected = {"CALIPSO_VFM"};
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
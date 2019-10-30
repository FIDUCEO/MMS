package com.bc.fiduceo.reader.caliop;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.*;

public class CALIOP_L2_VFM_ReaderPluginTest {

    private CALIOP_L2_VFM_ReaderPlugin plugin;

    @Before
    public void setUp()  {
        plugin = new CALIOP_L2_VFM_ReaderPlugin();
    }

    @Test
    public void testCreateReader() {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);

        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(geometryFactory);

        final Reader reader = plugin.createReader(readerContext);

        final Class<CALIOP_L2_VFM_Reader> expectedType = CALIOP_L2_VFM_Reader.class;
        assertThat(reader, is(instanceOf(expectedType)));
        final CALIOP_L2_VFM_Reader vfmReader = (CALIOP_L2_VFM_Reader) reader;
        assertThat(vfmReader.geometryFactory, is(sameInstance(geometryFactory)));
        assertThat(vfmReader.caliopUtils, is(notNullValue()));
        assertThat(vfmReader.caliopUtils, is(instanceOf(CaliopUtils.class)));
    }

    @Test
    public void testGetSupportedSensorKeys() {
        //execution
        final String[] supportedSensorKeys = plugin.getSupportedSensorKeys();

        //verification
        final String[] expected = {"caliop_vfm-cal"};
        assertThat(supportedSensorKeys, is(equalTo(expected)));
    }

    @Test
    public void testGetDataType() {
        //execution
        final DataType dataType = plugin.getDataType();

        //verification
        final DataType expected = DataType.POLAR_ORBITING_SATELLITE;
        assertThat(dataType, is(equalTo(expected)));
    }
}
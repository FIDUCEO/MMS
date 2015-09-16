package com.bc.fiduceo.reader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("OctalInteger")
@RunWith(ProductReaderTestRunner.class)
public class EumetsatIASIReader_IO_Test {

    //--- Polygon with the interval of 6 at the
    private EumetsatIASIReader eumetSatIASIReader;
    private AcquisitionInfo acquisitionInfo;

    @Before
    public void setUp() throws IOException {
        final File dataDirectory = TestUtil.getTestDataDirectory();

        File file = new File(dataDirectory, "W_XX-EUMETSAT-Darmstadt,HYPERSPECT+SOUNDING,MetOpA+IASI_C_EUMP_20130528172543_34281_eps_o_l1.nc");
        eumetSatIASIReader = new EumetsatIASIReader();
        eumetSatIASIReader.open(file);

        acquisitionInfo = eumetSatIASIReader.read();
        assertNotNull(acquisitionInfo);
    }

    @After
    public void tearDown() throws IOException {
        eumetSatIASIReader.close();
    }


    @Test
    public void testGeoCoordinate() throws com.vividsolutions.jts.io.ParseException {
       acquisitionInfo.getCoordinates();


    }

    @Test
    public void testDateTime() {
        Date date = acquisitionInfo.getSensingStart();
        TestUtil.assertCorrectUTCDate(date, 2013, 5, 28, 17, 25, 43);

        date = acquisitionInfo.getSensingStop();
        TestUtil.assertCorrectUTCDate(date, 2013, 5, 28, 17, 44, 54);
    }
}

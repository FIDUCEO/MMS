package com.bc.fiduceo.reader.amsr.amsr2;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.reader.ReaderContext;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;
import ucar.nc2.Attribute;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AMSR2_ReaderTest {

    private AMSR2_Reader reader;

    @Before
    public void setUp() {
        reader = new AMSR2_Reader(new ReaderContext()); // we do not need a geometry factory for this test tb 2018-01-11
    }

    @Test
    public void testGetRegEx() {
        final String expected = "GW1AM2_\\d{12}_\\d{3}[AD]_L1SGRTBR_\\d{7}.h5(.gz)?";

        assertEquals(expected, reader.getRegEx());

        final Pattern pattern = Pattern.compile(expected);

        Matcher matcher = pattern.matcher("GW1AM2_201307010942_035A_L1SGRTBR_2220220.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("GW1AM2_201307161446_092D_L1SGRTBR_2220220.h5.gz");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("GW1AM2_201609101037_044A_L1SGRTBR_2220220.h5.gz");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("AMSR_E_L2A_BrightnessTemperatures_V12_200607211944_A.hdf");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("AMSR_E_L2A_BrightnessTemperatures_V12_201011230036_D.hdf");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("L0496703.NSS.AMBX.NK.D07234.S0630.E0824.B4821011.WI.h5");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("AT2_TOA_1PURAL19980424_055754_000000001031_00348_15733_0000.E2");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetLongitudeVariableName() {
        assertEquals("Longitude_of_Observation_Point_for_89A", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        assertEquals("Latitude_of_Observation_Point_for_89A", reader.getLatitudeVariableName());
    }

    @Test
    public void testGetUtcData() throws IOException {
        final Attribute timeAttribute = new Attribute("whatever", "2013-07-01T09:42:53.154Z");
        final ProductData.UTC utcDate = AMSR2_Reader.getUtcDate(timeAttribute);
        assertNotNull(utcDate);
        TestUtil.assertCorrectUTCDate(2013, 7, 1, 9, 42, 53, utcDate.getAsDate());
    }

    @Test
    public void testGetUtcData_wrongStringRaisesIOException() {
        final Attribute timeAttribute = new Attribute("not_correct", "2013-07-01:early_afternoon");
        try {
            AMSR2_Reader.getUtcDate(timeAttribute);
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }
    @Test
    public void testExtractYearMonthDayFromFilename() {
        final int[] ymd = reader.extractYearMonthDayFromFilename("GW1AM2_201307010942_035A_L1SGRTBR_2220220.h5");

        assertEquals(2013, ymd[0]);
        assertEquals(7, ymd[1]);
        assertEquals(1, ymd[2]);
    }

    @Test
    public void testIsCompressed() {
        assertTrue(AMSR2_Reader.isCompressed(new File("/home/tom/GW1AM2_201707160510_232D_L1SGRTBR_2220220.h5.gz")));
        assertFalse(AMSR2_Reader.isCompressed(new File("/home/tom/GW1AM2_201707160510_232D_L1SGRTBR_2220220.h5")));
    }
}

package com.bc.fiduceo.reader.amsr2;

import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class AMSR2_ReaderTest {

    private AMSR2_Reader reader;

    @Before
    public void setUp() {
        reader = new AMSR2_Reader(null); // we do not need a geometry factory for this test tb 2018-01-11
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

    //Latitude of Observation Point for 89A

    @Test
    public void testGetLongitudeVariableName() {
        assertEquals("Longitude of Observation Point for 89A", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        assertEquals("Latitude of Observation Point for 89A", reader.getLatitudeVariableName());
    }
}

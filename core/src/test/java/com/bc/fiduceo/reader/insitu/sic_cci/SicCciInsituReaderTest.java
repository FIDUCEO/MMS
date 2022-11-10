package com.bc.fiduceo.reader.insitu.sic_cci;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

public class SicCciInsituReaderTest {

    private SicCciInsituReader reader;

    @Before
    public void setUp() {
        reader = new SicCciInsituReader();
    }

    @Test
    public void testGetRegEx() {
        final String expected = "ASCAT-vs-AMSR2-vs-ERA5-vs-\\p{Upper}{6}\\d{1}-\\d{4}-[N|S].text";

        assertEquals(expected, reader.getRegEx());

        final Pattern pattern = Pattern.compile(expected);

        Matcher matcher = pattern.matcher("ASCAT-vs-AMSR2-vs-ERA5-vs-DMISIC0-2017-N.text");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("ASCAT-vs-AMSR2-vs-ERA5-vs-DTUSIC1-2017-S.text");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("insitu_0_WMOID_42531_19960904_19960909.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("190546533.NSS.HIRX.NL.D11235.S1235.E1422.B5628788.WI.nc");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetLongitudeVariableName() {
        assertEquals("longitude", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        assertEquals("latitude", reader.getLatitudeVariableName());
    }

    @Test
    public void testExtractYearMonthDayFromFilename() {
        int[] ymd = reader.extractYearMonthDayFromFilename("whatever");
        assertEquals(3, ymd.length);
        assertEquals(0, ymd[0]);
        assertEquals(0, ymd[1]);
        assertEquals(0, ymd[2]);
    }

    @Test
    public void testCreateReferenceParser() throws IOException {
        ReferenceSectionParser parser = SicCciInsituReader.createReferenceParser("ASCAT-vs-AMSR2-vs-ERA5-vs-DMISIC0-2017-N.text");
        assertTrue(parser instanceof DMISIC0SectionParser);

        parser = SicCciInsituReader.createReferenceParser("ASCAT-vs-AMSR2-vs-ERA5-vs-DTUSIC1-2016-S.text");
        assertTrue(parser instanceof DTUSIC1SectionParser);

        parser = SicCciInsituReader.createReferenceParser("ASCAT-vs-AMSR2-vs-ERA-vs-ANTXXXI_2_FROSN_SeaIceObservations_reformatted.txt");
        assertTrue(parser instanceof ANTXXXISectionParser);
    }

    @Test
    public void testCreateReferenceParser_invalid() {
        try {
            SicCciInsituReader.createReferenceParser("OceanRAIN_allships_2010-2017_SST.ascii");
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }
}

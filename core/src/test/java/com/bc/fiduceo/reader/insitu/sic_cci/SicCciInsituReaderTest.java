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
        reader = new SicCciInsituReader("whatever");
    }

    @Test
    public void testGetRegEx() {
        final String antxxxiPattern = ".*ANTXXXI.*.text";
        final SicCciInsituReader antxxxiReader = new SicCciInsituReader(antxxxiPattern);
        assertEquals(antxxxiPattern, antxxxiReader.getRegEx());

        Pattern pattern = Pattern.compile(antxxxiPattern);

        Matcher matcher = pattern.matcher("ASCAT-vs-AMSR2-vs-ERA-vs-ANTXXXI_2_FROSN_SeaIceObservations_reformatted.text");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("ASCAT-vs-AMSR2-vs-ERA5-vs-DTUSIC1-2017-S.text");
        assertFalse(matcher.matches());

        final String dmisic0Pattern = ".*DMISIC0.*.text";
        final SicCciInsituReader dmisic0Reader = new SicCciInsituReader(dmisic0Pattern);
        assertEquals(dmisic0Pattern, dmisic0Reader.getRegEx());

        pattern = Pattern.compile(dmisic0Pattern);

        matcher = pattern.matcher("ASCAT-vs-AMSR2-vs-ERA5-vs-DMISIC0-2016-N.text");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("ASCAT-vs-AMSR2-vs-ERA5-vs-DTUSIC1-2017-S.text");
        assertFalse(matcher.matches());

        final String dtusic1Pattern = ".*DTUSIC1.*.text";
        final SicCciInsituReader dtusic10Reader = new SicCciInsituReader(dtusic1Pattern);
        assertEquals(dtusic1Pattern, dtusic10Reader.getRegEx());

        pattern = Pattern.compile(dtusic1Pattern);

        matcher = pattern.matcher("ASCAT-vs-AMSR2-vs-ERA5-vs-DTUSIC1-2017-S.text");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("ASCAT-vs-AMSR2-vs-ERA5-vs-DMISIC0-2016-N.text");
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

package com.bc.fiduceo.reader.fiduceo_fcdr;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AVHRR_FCDR_ReaderTest {

    private AVHRR_FCDR_Reader reader;

    @Before
    public void setUp() {
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new AVHRR_FCDR_Reader(readerContext);
    }

    @Test
    public void testGetRegEx() {
        final String regEx = reader.getRegEx();
        assertEquals("FIDUCEO_FCDR_L1C_AVHRR_(METOPA|NOAA[0-9]{2})_[0-9]{14}_[0-9]{14}_EASY_vBeta_fv\\d\\.\\d\\.\\d\\.nc", regEx);

        final Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher("FIDUCEO_FCDR_L1C_AVHRR_METOPA_20090115151549_20090115160636_EASY_vBeta_fv2.0.0.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("FIDUCEO_FCDR_L1C_AVHRR_NOAA15_20011205154420_20011205172532_EASY_vBeta_fv2.0.0.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("FIDUCEO_FCDR_L1C_AVHRR_NOAA11_19920327133425_19920327151625_EASY_vBeta_fv2.0.0.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("NSS.AMBX.NK.D15365.S1249.E1420.B9169697.GC");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("L8912163.NSS.AMBX.NK.D08001.S0000.E0155.B5008586.GC.gz.l1c.h5");
        assertFalse(matcher.matches());
    }

    @Test
    public void test_parseStartDate() {
        final Date startDate = AVHRR_FCDR_Reader.parseStartDate("FIDUCEO_FCDR_L1C_AVHRR_NOAA11_19920327133425_19920327151625_EASY_vBeta_fv2.0.0.nc");

        TestUtil.assertCorrectUTCDate(1992, 3, 27, 13, 34, 25, startDate);
    }

    @Test
    public void test_parseStopDate() {
        final Date startDate = AVHRR_FCDR_Reader.parseStopDate("FIDUCEO_FCDR_L1C_AVHRR_NOAA15_20011205154420_20011205172532_EASY_vBeta_fv2.0.0.nc");

        TestUtil.assertCorrectUTCDate(2001, 12, 5, 17, 25, 32, startDate);
    }

    @Test
    public void testExtractYearMonthDayFromFilename() {
        int[] ymd = reader.extractYearMonthDayFromFilename("FIDUCEO_FCDR_L1C_AVHRR_NOAA15_20011205154420_20011205172532_EASY_vBeta_fv2.0.0.nc");

        assertEquals(2001, ymd[0]);
        assertEquals(12, ymd[1]);
        assertEquals(5, ymd[2]);

        ymd = reader.extractYearMonthDayFromFilename("FIDUCEO_FCDR_L1C_AVHRR_NOAA11_19920327133425_19920327151625_EASY_vBeta_fv2.0.0.nc");

        assertEquals(1992, ymd[0]);
        assertEquals(3, ymd[1]);
        assertEquals(27, ymd[2]);
    }

    @Test
    public void testGetLongitudeVariableName() {
        assertEquals("longitude", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        assertEquals("latitude", reader.getLatitudeVariableName());
    }
}

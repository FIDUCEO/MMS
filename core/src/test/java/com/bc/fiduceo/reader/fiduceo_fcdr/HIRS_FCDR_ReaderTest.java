package com.bc.fiduceo.reader.fiduceo_fcdr;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HIRS_FCDR_ReaderTest {

    private HIRS_FCDR_Reader reader;

    @Before
    public void setUp() {
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new HIRS_FCDR_Reader(readerContext);
    }

    @Test
    public void testGetRegEx() {
        final String regEx = reader.getRegEx();
        assertEquals("FIDUCEO_FCDR_L1C_HIRS(2|3|4)_(METOPA|NOAA[0-9]{2})_[0-9]{14}_[0-9]{14}_EASY_v0.8rc1_fv\\d\\.\\d\\.\\d\\.nc", regEx);

        final Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher("FIDUCEO_FCDR_L1C_HIRS4_METOPA_20150326173656_20150326191810_EASY_v0.8rc1_fv2.0.0.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("FIDUCEO_FCDR_L1C_HIRS2_NOAA07_19831004162422_19831004180614_EASY_v0.8rc1_fv2.0.0.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("NSS.AMBX.NK.D15365.S1249.E1420.B9169697.GC");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("L8912163.NSS.AMBX.NK.D08001.S0000.E0155.B5008586.GC.gz.l1c.h5");
        assertFalse(matcher.matches());
    }

    @Test
    public void testExtractYearMonthDayFromFilename() {
        int[] ymd = reader.extractYearMonthDayFromFilename("FIDUCEO_FCDR_L1C_HIRS2_NOAA07_19831004162422_19831004180614_EASY_v0.8rc1_fv2.0.0.nc");

        assertEquals(1983, ymd[0]);
        assertEquals(10, ymd[1]);
        assertEquals(4, ymd[2]);

        ymd = reader.extractYearMonthDayFromFilename("FIDUCEO_FCDR_L1C_HIRS4_METOPA_20150326173656_20150326191810_EASY_v0.8rc1_fv2.0.0");

        assertEquals(2015, ymd[0]);
        assertEquals(3, ymd[1]);
        assertEquals(26, ymd[2]);
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

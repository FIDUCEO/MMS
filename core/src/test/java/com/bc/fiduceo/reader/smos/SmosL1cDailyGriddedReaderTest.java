package com.bc.fiduceo.reader.smos;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.MultiLineString;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.util.TimeUtils;
import org.junit.Test;
import ucar.ma2.Array;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

@SuppressWarnings("resource")
public class SmosL1cDailyGriddedReaderTest {

    @Test
    public void testGetRegEx() {
        final Reader reader = new SmosL1CDailyGriddedReader(null); // this test does not require a context class tb 2022-09-13

        final String expected = "SM_RE07_MIR_CDF3T[AD]_(\\d{8}T\\d{6}_){2}\\d{3}_\\d{3}_\\d{1}.tgz";
        assertEquals(expected, reader.getRegEx());

        final Pattern pattern = Pattern.compile(expected);
        Matcher matcher = pattern.matcher("SM_RE07_MIR_CDF3TA_20160610T000000_20160610T235959_330_001_7.tgz");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("SM_RE07_MIR_CDF3TD_20171120T000000_20171120T235959_330_001_7.tgz");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("S3A_SL_1_RBT____20200522T231202_20200522T231502_20200524T053503_0179_058_286_5580_LN2_O_NT_004.zip");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("FIDUCEO_FCDR_L1C_AVHRR_N19ALL_20110705055721_20110705073927_EASY_v0.2Bet_fv2.0.0.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("190583863.NSS.HIRX.M2.D11235.S1641.E1823.B2513233.SV.nc");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetLongitudeVariableName() {
        final Reader reader = new SmosL1CDailyGriddedReader(null); // this test does not require a context class tb 2022-09-15

        assertEquals("lon", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        final Reader reader = new SmosL1CDailyGriddedReader(null); // this test does not require a context class tb 2022-09-15

        assertEquals("lat", reader.getLatitudeVariableName());
    }

    @Test
    public void testExtractYearMonthDayFromFilename() {
        final SmosL1CDailyGriddedReader reader = new SmosL1CDailyGriddedReader(null); // this test does not require a context class tb 2022-09-15

        int[] ymd = reader.extractYearMonthDayFromFilename("SM_RE07_MIR_CDF3TA_20160610T000000_20160610T235959_330_001_7.tgz");
        assertEquals(3, ymd.length);
        assertEquals(2016, ymd[0]);
        assertEquals(6, ymd[1]);
        assertEquals(10, ymd[2]);

        ymd = reader.extractYearMonthDayFromFilename("SM_RE07_MIR_CDF3TD_20171120T000000_20171120T235959_330_001_7.tgz");
        assertEquals(3, ymd.length);
        assertEquals(2017, ymd[0]);
        assertEquals(11, ymd[1]);
        assertEquals(20, ymd[2]);
    }

    @Test
    public void testCreatePolygonFromMinMax() {
        final double[] geoMinMax = {-170.0, 170.0, -80.0, 80.0};

        final Polygon polygon = SmosL1CDailyGriddedReader.createPolygonFromMinMax(geoMinMax, new GeometryFactory(GeometryFactory.Type.S2));
        final Point[] coordinates = polygon.getCoordinates();
        assertEquals(5, coordinates.length);
        assertEquals(-170, coordinates[0].getLon(), 1e-8);
        assertEquals(-80, coordinates[0].getLat(), 1e-8);

        assertEquals(170, coordinates[2].getLon(), 1e-8);
        assertEquals(80, coordinates[2].getLat(), 1e-8);

        assertEquals(-170, coordinates[4].getLon(), 1e-8);
        assertEquals(-80, coordinates[4].getLat(), 1e-8);
    }

    @Test
    public void testExtractMinMax() {
        final double[] lons = new double[]{-171.0, -81.0, 0.0, 81.0, 171.0};
        final double[] lats = new double[]{-79.0, -39.0, 0.0, 39.0, 79.0};

        final Array lonArray = Array.makeFromJavaArray(lons);
        final Array latArray = Array.makeFromJavaArray(lats);

        final double[] minMax = SmosL1CDailyGriddedReader.extractMinMax(lonArray, latArray);
        assertEquals(4, minMax.length);
        assertEquals(-171.0, minMax[0], 1e-8);
        assertEquals(171.0, minMax[1], 1e-8);
        assertEquals(-79.0, minMax[2], 1e-8);
        assertEquals(79.0, minMax[3], 1e-8);
    }

    @Test
    public void testCreateMultiLineStringFromMinMax() {
        final double[] geoMinMax = {-172.0, 172.0, -78.0, 87.0};

        final MultiLineString lineString = SmosL1CDailyGriddedReader.createMultiLineStringFromMinMax(geoMinMax, new GeometryFactory(GeometryFactory.Type.S2));
        assertNotNull(lineString);
        final Point[] coordinates = lineString.getCoordinates();
        assertEquals(4, coordinates.length);
        assertEquals("POINT(-172.0 0.0)", coordinates[0].toString());
        assertEquals("POINT(172.0 0.0)", coordinates[1].toString());
        assertEquals("POINT(0.0 87.0)", coordinates[2].toString());
        assertEquals("POINT(0.0 -78.0)", coordinates[3].toString());
    }

    @Test
    public void testCfiDateToUtc() {
        Date date = SmosL1CDailyGriddedReader.cfiDateToUtc(0, 0, 0);
        assertEquals("01-Jan-2000 00:00:00", TimeUtils.format(date));

        date = SmosL1CDailyGriddedReader.cfiDateToUtc(1, 0, 0);
        assertEquals("02-Jan-2000 00:00:00", TimeUtils.format(date));

        date = SmosL1CDailyGriddedReader.cfiDateToUtc(1, 10, 0);
        long timeWithoutMillis = date.getTime();
        assertEquals("02-Jan-2000 00:00:10", TimeUtils.format(date));

        date = SmosL1CDailyGriddedReader.cfiDateToUtc(1, 10, 100000);    // last argument is microsecond, date can only handle millis ...
        assertEquals("02-Jan-2000 00:00:10", TimeUtils.format(date));
        assertEquals(timeWithoutMillis + 100, date.getTime());
    }
}

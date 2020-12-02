package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.util.TimeUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;

public class Era5ArchiveTest {

    private static final String SEP = File.separator;

    @Test
    public void testConstructAndGet() throws IOException {
        final Era5Archive era5Archive = new Era5Archive("archive" + SEP + "era5");

        // Friday, 30. May 2008 11:00:00
        // 1212145200
        String expected = assemblePath("archive", "era5", "an_ml", "2008", "05", "30", "ecmwf-era5_oper_an_ml_200805301100.q.nc");
        assertEquals(expected, era5Archive.get("an_ml_q", 1212145200));

        expected = assemblePath("archive", "era5", "an_sfc", "2008", "05", "30", "ecmwf-era5_oper_an_sfc_200805301100.2t.nc");
        assertEquals(expected, era5Archive.get("an_sfc_t2m", 1212145200));

        expected = assemblePath("archive", "era5", "fc_sfc", "2008", "05", "30", "ecmwf-era5_oper_fc_sfc_2008053006005.msnlwrf.nc");
        assertEquals(expected, era5Archive.get("fc_sfc_msnlwrf", 1212145200));

        // Monday, 2. June 2008 10:00:00
        // 1212400800
        expected = assemblePath("archive", "era5", "an_ml", "2008", "06", "02", "ecmwf-era5_oper_an_ml_200806021000.t.nc");
        assertEquals(expected, era5Archive.get("an_ml_t", 1212400800));

        expected = assemblePath("archive", "era5", "fc_sfc", "2008", "06", "02", "ecmwf-era5_oper_fc_sfc_2008060206004.mslhf.nc");
        assertEquals(expected, era5Archive.get("fc_sfc_mslhf", 1212400800));
    }

    @Test
    public void testGetFileName() {
        assertEquals("ecmwf-era5_oper_an_ml_201108231900.q.nc", Era5Archive.getFileName("an_ml", "q", "201108231900"));
    }

    @Test
    public void testMapVariable() {
        assertEquals("q", Era5Archive.mapVariable("q"));
        assertEquals("2t", Era5Archive.mapVariable("t2m"));
        assertEquals("10u", Era5Archive.mapVariable("u10"));
        assertEquals("10v", Era5Archive.mapVariable("v10"));
        assertEquals("ci", Era5Archive.mapVariable("siconc"));
        assertEquals("msl", Era5Archive.mapVariable("msl"));
    }

    @Test
    public void testGetTimeString_an(){
        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.setTimeInMillis(1212500800000L);

        final String o3Time = Era5Archive.getTimeString("an_ml_o3", utcCalendar);
        assertEquals("200806031300", o3Time);

        final String mslTime = Era5Archive.getTimeString("an_sfc_msl", utcCalendar);
        assertEquals("200806031300", mslTime);
    }

    @Test
    public void testGetTimeString_fc(){
        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.setTimeInMillis(1212600800000L);
        utcCalendar.set(Calendar.HOUR_OF_DAY, 3);

        String metssTime = Era5Archive.getTimeString("fc_sfc_metss", utcCalendar);
        assertEquals("2008060318009", metssTime);

        utcCalendar.setTimeInMillis(1212600800000L);
        utcCalendar.set(Calendar.HOUR_OF_DAY, 11);
        metssTime = Era5Archive.getTimeString("fc_sfc_metss", utcCalendar);
        assertEquals("2008060406005", metssTime);

        utcCalendar.setTimeInMillis(1212600800000L);
        utcCalendar.set(Calendar.HOUR_OF_DAY, 21);
        metssTime = Era5Archive.getTimeString("fc_sfc_metss", utcCalendar);
        assertEquals("2008060418003", metssTime);
    }

    @Test
    public void testGetTimeString_invalidCollection(){
        final Calendar utcCalendar = TimeUtils.getUTCCalendar();

        try {
            Era5Archive.getTimeString("heffalump", utcCalendar);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    private String assemblePath(String... elements) {
        StringBuilder path = new StringBuilder();

        for (String element : elements) {
            path.append(element);
            path.append(SEP);
        }

        // strip last separator tb 2020-11-23
        return path.substring(0, path.length() - 1);
    }
}

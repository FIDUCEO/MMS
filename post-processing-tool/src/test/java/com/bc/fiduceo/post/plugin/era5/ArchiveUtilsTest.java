package com.bc.fiduceo.post.plugin.era5;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

public class ArchiveUtilsTest {

    private static final String SEP = File.separator;

    @Test
    public void testConstructAndGet() throws IOException {
        final ArchiveUtils archiveUtils = new ArchiveUtils("archive" + SEP + "era5");

        // Friday, 30. May 2008 11:00:00
        // 1212145200
        String expected = assemblePath("archive", "era5", "an_ml", "2008", "05", "30", "ecmwf-era5_oper_an_ml_200805301100.q.nc");
        assertEquals(expected, archiveUtils.get("an_ml_q", 1212145200));

        expected = assemblePath("archive", "era5", "an_sfc", "2008", "05", "30", "ecmwf-era5_oper_an_sfc_200805301100.2t.nc");
        assertEquals(expected, archiveUtils.get("an_sfc_t2m", 1212145200));

        expected = assemblePath("archive", "era5", "fc_sfc", "2008", "05", "30", "ecmwf-era5_oper_fc_sfc_2008053006005.msnlwrf.nc");
        assertEquals(expected, archiveUtils.get("fc_sfc_msnlwrf", 1212145200));

        // Monday, 2. June 2008 10:00:00
        // 1212400800
        expected = assemblePath("archive", "era5", "an_ml", "2008", "06", "02", "ecmwf-era5_oper_an_ml_200806021000.t.nc");
        assertEquals(expected, archiveUtils.get("an_ml_t", 1212400800));

        expected = assemblePath("archive", "era5", "fc_sfc", "2008", "06", "02", "ecmwf-era5_oper_fc_sfc_2008060206004.mslhf.nc");
        assertEquals(expected, archiveUtils.get("fc_sfc_mslhf", 1212400800));


    }

    @Test
    public void testGetFileName() {
        assertEquals("ecmwf-era5_oper_an_ml_201108231900.q.nc", ArchiveUtils.getFileName("an_ml", "q", "201108231900"));
    }

    @Test
    public void testMapVariable() {
        assertEquals("q", ArchiveUtils.mapVariable("q"));
        assertEquals("2t", ArchiveUtils.mapVariable("t2m"));
        assertEquals("10u", ArchiveUtils.mapVariable("u10"));
        assertEquals("10v", ArchiveUtils.mapVariable("v10"));
        assertEquals("ci", ArchiveUtils.mapVariable("siconc"));
        assertEquals("msl", ArchiveUtils.mapVariable("msl"));
    }

    private String assemblePath(String... elements) {
        String path = "";

        for (String element : elements) {
            path += element;
            path += SEP;
        }

        // strip last separator tb 2020-11-23
        return path.substring(0, path.length() - 1);
    }
}

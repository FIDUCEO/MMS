package com.bc.fiduceo.post.plugin.era5;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ArchiveUtilsTest {

    @Test
    public void testConstructAndGet() {
        final ArchiveUtils archiveUtils = new ArchiveUtils("/archive/era5");

        assertEquals("/archive/era5\\an_ml\\2008\\05\\30\\ecmwf-era5_oper_an_ml_200805301100.q.nc", archiveUtils.get("an_ml_q", 1212145200));
        assertEquals("/archive/era5\\an_ml\\2008\\06\\02\\ecmwf-era5_oper_an_ml_200806021000.t.nc", archiveUtils.get("an_ml_t", 1212400800));
        // Friday, 30. May 2008 11:00:00
        // 1212145200
        // Monday, 2. June 2008 10:00:00
        //1212400800

        // an_ml/2008/05/30/ecmwf-era5_oper_an_ml_200805301100.q.nc
        // an_ml/2008/06/02/ecmwf-era5_oper_an_ml_200806021000.q.nc
    }

    @Test
    public void testGetFileName() {
        assertEquals("ecmwf-era5_oper_an_ml_201108231900.q.nc", ArchiveUtils.getFileName("an_ml", "q", "20110823", "19"));
    }
}

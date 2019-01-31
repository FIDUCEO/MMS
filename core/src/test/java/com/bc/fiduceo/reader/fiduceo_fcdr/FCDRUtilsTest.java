package com.bc.fiduceo.reader.fiduceo_fcdr;

import com.bc.fiduceo.TestUtil;
import org.junit.Test;

import java.util.Date;

public class FCDRUtilsTest {

    @Test
    public void testParseStartDate() {
        Date startDate = FCDRUtils.parseStartDate("FIDUCEO_FCDR_L1C_AMSUB_NOAA17_20040904095327_20040904113437_EASY_v4.1_fv2.0.1.nc");
        TestUtil.assertCorrectUTCDate(2004, 9, 4, 9, 53, 27, startDate);

        startDate = FCDRUtils.parseStartDate("FIDUCEO_FCDR_L1C_AVHRR_NOAA11_19920327133425_19920327151625_EASY_vBeta_fv2.0.0.nc");
        TestUtil.assertCorrectUTCDate(1992, 3, 27, 13, 34, 25, startDate);

        startDate = FCDRUtils.parseStartDate("FIDUCEO_FCDR_L1C_HIRS3_NOAA17_20061003211526_20061003225633_EASY_v0.8rc1_fv2.0.0.nc");
        TestUtil.assertCorrectUTCDate(2006, 10, 3, 21, 15, 26, startDate);

        startDate = FCDRUtils.parseStartDate("FIDUCEO_FCDR_L1C_SSMT2_F15_20010522050622_20010522064805_EASY_v4.1_fv2.0.1.nc");
        TestUtil.assertCorrectUTCDate(2001, 5, 22, 5, 6, 22, startDate);
    }

    @Test
    public void testParseStopDate() {
        Date startDate = FCDRUtils.parseStopDate("FIDUCEO_FCDR_L1C_AMSUB_NOAA17_20040904095327_20040904113437_EASY_v4.1_fv2.0.1.nc");
        TestUtil.assertCorrectUTCDate(2004, 9, 4, 11, 34, 37, startDate);

        startDate = FCDRUtils.parseStopDate("FIDUCEO_FCDR_L1C_AVHRR_NOAA11_19920327133425_19920327151625_EASY_vBeta_fv2.0.0.nc");
        TestUtil.assertCorrectUTCDate(1992, 3, 27, 15, 16, 25, startDate);

        startDate = FCDRUtils.parseStopDate("FIDUCEO_FCDR_L1C_HIRS3_NOAA17_20061003211526_20061003225633_EASY_v0.8rc1_fv2.0.0.nc");
        TestUtil.assertCorrectUTCDate(2006, 10, 3, 22, 56, 33, startDate);

        startDate = FCDRUtils.parseStopDate("FIDUCEO_FCDR_L1C_SSMT2_F15_20010522050622_20010522064805_EASY_v4.1_fv2.0.1.nc");
        TestUtil.assertCorrectUTCDate(2001, 5, 22, 6, 48, 5, startDate);
    }
}

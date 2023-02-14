package com.bc.fiduceo.util;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MMDUtilTest {

    @Test
    public void testFileNamePattern() {
        final Pattern pattern = MMDUtil.getMMDFileNamePattern();

        assertTrue(pattern.matcher("mmd08_aatsr-en_avhrr-frac-ma_2012-092_2012-092.nc").matches());
        assertTrue(pattern.matcher("mmd09_slstr-s3a-nt_avhrr-frac-ma_2019-115_2019-115.nc").matches());
        assertTrue(pattern.matcher("coo_2_slstr-s3a-nt_avhrr-frac-mb_2020-241_2020-241.nc").matches());

        assertFalse(pattern.matcher("NSS.FRAC.M3.D19261.S1708.E1849.B0448586.SV").matches());
        assertFalse(pattern.matcher("20161122200700-ESACCI-L1C-AVHRR18_G-fv01.0.nc").matches());
    }
}

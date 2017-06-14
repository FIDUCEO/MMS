/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader.iasi;

import com.bc.fiduceo.IOTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

@RunWith(IOTestRunner.class)
public class MDR_1C_v4_IOTest {

    private ImageInputStream iis;
    private HashMap<String, ReadProxy> readProxies;

    @Before
    public void setUp() throws Exception {
        readProxies = MDR_1C_v4.getReadProxies();
    }

    @After
    public void tearDown() throws IOException {
        if (iis != null) {
            iis.close();
        }
    }

    @Test
    public void testAccessFields_MA() throws IOException {
        final File file = IASI_TestUtil.getIasiFile_MA_v4();

        iis = new FileImageInputStream(file);

        final MDR_1C_v4 mdr_1C = new MDR_1C_v4();
        iis.seek(IASI_TestUtil.MDR_OFFSET_MA + 198 * mdr_1C.getMdrSize());
        iis.read(mdr_1C.getRaw_record());

        // general L1 data -----------------------------------
        ReadProxy proxy = readProxies.get("DEGRADED_INST_MDR");
        assertEquals(1, (byte) proxy.read(0, 0, mdr_1C));

        proxy = readProxies.get("DEGRADED_PROC_MDR");
        assertEquals(1, (byte) proxy.read(1, 1, mdr_1C));

        proxy = readProxies.get("GEPSIasiMode");
        assertEquals(161, (int) proxy.read(2, 0, mdr_1C));

        proxy = readProxies.get("GEPSOPSProcessingMode");
        assertEquals(0, (int) proxy.read(3, 1, mdr_1C));

        // skipping GEPSIdConf tb 2017-06-14
        // skipping GEPSLocIasiAvhrr_IASI tb 2017-06-14
        // skipping GEPSLocIasiAvhrr_IIS tb 2017-06-14

        proxy = readProxies.get("OBT");
        assertEquals(695914947584L, (long) proxy.read(6, 0, mdr_1C));

        proxy = readProxies.get("OnboardUTC");
        assertEquals(1238982081135L, (long) proxy.read(7, 1, mdr_1C));

        proxy = readProxies.get("GEPSDatIasi");
        assertEquals(1238982081354L, (long) proxy.read(8, 0, mdr_1C));

        // skipping GIsfLinOrigin tb 2017-06-07
        // skipping GIsfColOrigin tb 2017-06-07
        // skipping GIsfPds1 tb 2017-06-07
        // skipping GIsfPds2 tb 2017-06-07
        // skipping GIsfPds3 tb 2017-06-07
        // skipping GIsfPds4 tb 2017-06-07

        proxy = readProxies.get("GEPS_CCD");
        assertEquals(0, (byte) proxy.read(9, 1, mdr_1C));

        proxy = readProxies.get("GEPS_SP");
        assertEquals(6, (int) proxy.read(10, 0, mdr_1C));

        // skipping GIrcImage tb 2017-06-07
        // @todo 3 tb/tb GQisFlagQual 2017-05-04

        // @todo 1 tb/tb continue here 2017-06-14
//        proxy = readProxies.get("GQisFlagQualDetailed");
//        assertEquals(0, (short) proxy.read(11, 1, mdr_1C));
//
//        proxy = readProxies.get("GQisQualIndex");
//        assertEquals(1.0000486373901367, (float) proxy.read(12, 0, mdr_1C), 1e-8);
//
//        proxy = readProxies.get("GQisQualIndexIIS");
//        assertEquals(0.9339925646781921, (float) proxy.read(13, 1, mdr_1C), 1e-8);
//
//        proxy = readProxies.get("GQisQualIndexLoc");
//        assertEquals(0.06710358709096909, (float) proxy.read(14, 0, mdr_1C), 1e-8);
//
//        proxy = readProxies.get("GQisQualIndexRad");
//        assertEquals(1.0000312328338623, (float) proxy.read(15, 1, mdr_1C), 1e-8);
//
//        proxy = readProxies.get("GQisQualIndexSpect");
//        assertEquals(1.0000174045562744, (float) proxy.read(16, 0, mdr_1C), 1e-8);
//
//        proxy = readProxies.get("GQisSysTecIISQual");
//        assertEquals(1, (int) proxy.read(12, 0, mdr_1C));
//
//        proxy = readProxies.get("GQisSysTecSondQual");
//        assertEquals(1, (int) proxy.read(13, 1, mdr_1C));
//
//        proxy = readProxies.get("GGeoSondLoc_Lon");
//        assertEquals(-47930637, (int) proxy.read(14, 0, mdr_1C));
//        assertEquals(-48133239, (int) proxy.read(15, 0, mdr_1C));
//
//        proxy = readProxies.get("GGeoSondLoc_Lat");
//        assertEquals(13088441, (int) proxy.read(15, 1, mdr_1C));
//
//        proxy = readProxies.get("GGeoSondAnglesMETOP_Zenith");
//        assertEquals(25283361, (int) proxy.read(16, 0, mdr_1C));
//
//        proxy = readProxies.get("GGeoSondAnglesMETOP_Azimuth");
//        assertEquals(285331513, (int) proxy.read(17, 1, mdr_1C));
//
//        proxy = readProxies.get("GGeoSondAnglesSUN_Zenith");
//        assertEquals(49310599, (int) proxy.read(18, 0, mdr_1C));
//
//        proxy = readProxies.get("GGeoSondAnglesSUN_Azimuth");
//        assertEquals(137090083, (int) proxy.read(19, 1, mdr_1C));
//
//        // skipping GGeoIISLoc tb 2017-06-07
//
//        proxy = readProxies.get("EARTH_SATELLITE_DISTANCE");
//        assertEquals(7199344, (int) proxy.read(20, 0, mdr_1C));
//
//        // l1c specific --------------------------------------------
//        proxy = readProxies.get("IDefSpectDWn1b");
//        assertEquals(25.0, (float) proxy.read(21, 1, mdr_1C), 1e-8);
//
//        proxy = readProxies.get("IDefNsfirst1b");
//        assertEquals(2581, (int) proxy.read(21, 1, mdr_1C));
//
//        proxy = readProxies.get("IDefNslast1b");
//        assertEquals(11041, (int) proxy.read(22, 0, mdr_1C));
//
//        final short[] l1c_spec = mdr_1C.get_GS1cSpect(23, 0);
//        assertEquals(8700, l1c_spec.length);
//        assertEquals(4089, l1c_spec[0]);
//        assertEquals(6022, l1c_spec[4267]);
//
//        proxy = readProxies.get("GCcsRadAnalNbClass");
//        assertEquals(6, (int) proxy.read(24, 1, mdr_1C));
//
//        proxy = readProxies.get("IDefCcsMode");
//        assertEquals(0, (int) proxy.read(25, 0, mdr_1C));
//
//        proxy = readProxies.get("GCcsImageClassifiedNbLin");
//        assertEquals(48, (short) proxy.read(26, 1, mdr_1C));
//
//        proxy = readProxies.get("GCcsImageClassifiedNbCol");
//        assertEquals(69, (short) proxy.read(27, 0, mdr_1C));
//
//        proxy = readProxies.get("GCcsImageClassifiedFirstLin");
//        assertEquals(-1213.0, (float) proxy.read(28, 0, mdr_1C), 1e-8);
//
//        proxy = readProxies.get("GCcsImageClassifiedFirstCol");
//        assertEquals(1023.0, (float) proxy.read(29, 1, mdr_1C), 1e-8);
//
//        // skipping GCcsRadAnalType tb 2017-06-07
//
//        proxy = readProxies.get("GIacVarImagIIS");
//        assertEquals(1.3744753232458606E-5, (float) proxy.read(30, 0, mdr_1C), 1e-8);
//
//        proxy = readProxies.get("GIacAvgImagIIS");
//        assertEquals(0.0011099015828222036, (float) proxy.read(31, 1, mdr_1C), 1e-8);
//
//        proxy = readProxies.get("GEUMAvhrr1BCldFrac");
//        assertEquals(2, (byte) proxy.read(28, 1, mdr_1C));
//
//        proxy = readProxies.get("GEUMAvhrr1BLandFrac");
//        assertEquals(0, (byte) proxy.read(29, 0, mdr_1C));
//
//        proxy = readProxies.get("GEUMAvhrr1BQual");
//        assertEquals(0, (byte) proxy.read(30, 1, mdr_1C));
    }
}

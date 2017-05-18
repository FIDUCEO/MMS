package com.bc.fiduceo.reader.iasi;

import com.bc.fiduceo.IOTestRunner;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(IOTestRunner.class)
public class MDR_1C_IOTest {

    private ImageInputStream iis;

    @After
    public void tearDown() throws IOException {
        if (iis != null) {
            iis.close();
        }
    }

    @Test
    public void testAccessFields_MA() throws IOException {
        final File file = IASI_TestUtil.getIasiFile_MA();

        iis = new FileImageInputStream(file);

        final MDR_1C mdr_1C = new MDR_1C();
        iis.seek(IASI_TestUtil.MDR_OFFSET_MA + 117 * MDR_1C.RECORD_SIZE);
        iis.read(mdr_1C.getRaw_record());

        // general L1 data -----------------------------------
        assertEquals(0, mdr_1C.get_DEGRADED_INST_MDR(0, 0));
        assertEquals(0, mdr_1C.get_DEGRADED_PROC_MDR(1, 1));
        assertEquals(161, mdr_1C.get_GEPSIasiMode(2, 0));
        assertEquals(0, mdr_1C.get_GEPSOPSProcessingMode(3, 1));
        // @todo 1 tb/tb GEPSLocIasiAvhrr_IASI 2017-05-04
        assertEquals(339926409216L, mdr_1C.get_OBT(6, 0));
        assertEquals(1451653411496L, mdr_1C.get_OnboardUTC(7, 1));
        assertEquals(1451653411707L, mdr_1C.get_GEPSDatIasi(8, 0));
        assertEquals(0, mdr_1C.get_GEPS_CCD(9, 1));
        assertEquals(6, mdr_1C.get_GEPS_SP(10, 0));
        // @todo 1 tb/tb GQisFlagQual 2017-05-04
        assertEquals(0, mdr_1C.get_GQisFlagQualDetailed(11, 1));
        // @todo 1 tb/tb GQisQualIndex 2017-05-04
        // @todo 1 tb/tb GQisQualIndexIIS 2017-05-04
        // @todo 1 tb/tb GQisQualIndexLoc 2017-05-04
        // @todo 1 tb/tb GQisQualIndexRad 2017-05-04
        // @todo 1 tb/tb GQisQualIndexSpect 2017-05-04
        assertEquals(1, mdr_1C.get_GQisSysTecIISQual(12, 0));
        assertEquals(1, mdr_1C.get_GQisSysTecSondQual(13, 1));
        assertEquals(-47.93063735961914, mdr_1C.get_GGeoSondLoc_Lon(14, 0), 1e-8);
        assertEquals(-48.13323974609375, mdr_1C.get_GGeoSondLoc_Lon(15, 0), 1e-8);
        assertEquals(-48.46881866455078, mdr_1C.get_GGeoSondLoc_Lon(16, 0), 1e-8);
        assertEquals(-48.6592903137207, mdr_1C.get_GGeoSondLoc_Lon(17, 0), 1e-8);
        assertEquals(-48.976593017578125, mdr_1C.get_GGeoSondLoc_Lon(18, 0), 1e-8);
        assertEquals(13.088440895080566, mdr_1C.get_GGeoSondLoc_Lat(15, 1), 1e-8);
        assertEquals(25.28335952758789, mdr_1C.get_GGeoSondAnglesMETOP_Zenith(16, 0), 1e-8);
        assertEquals(285.3315124511719, mdr_1C.get_GGeoSondAnglesMETOP_Azimuth(17, 1), 1e-8);
        assertEquals(49.31060028076172, mdr_1C.get_GGeoSondAnglesSUN_Zenith(18, 0), 1e-8);
        assertEquals(137.09007263183594, mdr_1C.get_GGeoSondAnglesSUN_Azimuth(19, 1), 1e-8);
        assertEquals(7199344, mdr_1C.get_EARTH_SATELLITE_DISTANCE(20, 0));

        // l1c specific --------------------------------------------
        // @todo 1 tb/tb IDefSpectDWn1b 2017-05-05
        assertEquals(2581, mdr_1C.get_IDefNsfirst1b(21, 1));
        assertEquals(11041, mdr_1C.get_IDefNslast1b(22, 0));

        final short[] l1c_spec =  mdr_1C.get_GS1cSpect(23, 0);
        assertEquals(8700, l1c_spec.length);
        assertEquals(4089, l1c_spec[0]);
        assertEquals(6022, l1c_spec[4267]);

        assertEquals(6, mdr_1C.get_GCcsRadAnalNbClass(24, 1));
        assertEquals(0, mdr_1C.get_IDefCcsMode(25, 0));
        assertEquals(48, mdr_1C.get_GCcsImageClassifiedNbLin(26, 1));
        assertEquals(69, mdr_1C.get_GCcsImageClassifiedNbCol(27, 0));
        // @todo 1 tb/tb GIacVarImagIIS 2017-05-05
        // @todo 1 tb/tb GIacAvgImagIIS 2017-05-05
        assertEquals(2, mdr_1C.get_GEUMAvhrr1BCldFrac(28, 1));
        assertEquals(0, mdr_1C.get_GEUMAvhrr1BLandFrac(29, 0));
        assertEquals(0, mdr_1C.get_GEUMAvhrr1BQual(30, 1));
    }

    @Test
    public void testAccessFields_MB() throws IOException {
        final File file = IASI_TestUtil.getIasiFile_MB();

        iis = new FileImageInputStream(file);
        final MDR_1C mdr_1C = new MDR_1C();
        iis.seek(IASI_TestUtil.MDR_OFFSET_MB + 608 * MDR_1C.RECORD_SIZE);
        iis.read(mdr_1C.getRaw_record());

        // general L1 data -----------------------------------
        assertEquals(0, mdr_1C.get_DEGRADED_INST_MDR(0, 1));
        assertEquals(0, mdr_1C.get_DEGRADED_PROC_MDR(1, 0));
        assertEquals(161, mdr_1C.get_GEPSIasiMode(2, 1));
        assertEquals(0, mdr_1C.get_GEPSOPSProcessingMode(3, 0));
        // @todo 1 tb/tb GEPSLocIasiAvhrr_IASI 2017-05-04
        assertEquals(14729503744L, mdr_1C.get_OBT(6, 1));
        assertEquals(1398434941538L, mdr_1C.get_OnboardUTC(7, 0));
        assertEquals(1398434941754L, mdr_1C.get_GEPSDatIasi(8, 1));
        assertEquals(1, mdr_1C.get_GEPS_CCD(9, 0));
        assertEquals(6, mdr_1C.get_GEPS_SP(10, 1));
        // @todo 1 tb/tb GQisFlagQual 2017-05-04
        assertEquals(0, mdr_1C.get_GQisFlagQualDetailed(11, 0));
        // @todo 1 tb/tb GQisQualIndex 2017-05-04
        // @todo 1 tb/tb GQisQualIndexIIS 2017-05-04
        // @todo 1 tb/tb GQisQualIndexLoc 2017-05-04
        // @todo 1 tb/tb GQisQualIndexRad 2017-05-04
        // @todo 1 tb/tb GQisQualIndexSpect 2017-05-04
        assertEquals(1, mdr_1C.get_GQisSysTecIISQual(12, 1));
        assertEquals(1, mdr_1C.get_GQisSysTecSondQual(13, 0));
        assertEquals(98.93218231201172, mdr_1C.get_GGeoSondLoc_Lon(14, 1), 1e-8);
        assertEquals(99.1828384399414, mdr_1C.get_GGeoSondLoc_Lon(15, 1), 1e-8);
        assertEquals(99.58789825439453, mdr_1C.get_GGeoSondLoc_Lon(16, 1), 1e-8);
        assertEquals(38.758026123046875, mdr_1C.get_GGeoSondLoc_Lat(15, 0), 1e-8);
        assertEquals(25.284067153930664, mdr_1C.get_GGeoSondAnglesMETOP_Zenith(16, 1), 1e-8);
        assertEquals(71.94691467285156, mdr_1C.get_GGeoSondAnglesMETOP_Azimuth(17, 0), 1e-8);
        assertEquals(111.65995025634766, mdr_1C.get_GGeoSondAnglesSUN_Zenith(18, 1), 1e-8);
        assertEquals(310.1988220214844, mdr_1C.get_GGeoSondAnglesSUN_Azimuth(19, 0), 1e-8);
        assertEquals(7194027, mdr_1C.get_EARTH_SATELLITE_DISTANCE(20, 1));

        // l1c specific --------------------------------------------
        // @todo 1 tb/tb IDefSpectDWn1b 2017-05-05
        assertEquals(2581, mdr_1C.get_IDefNsfirst1b(21, 0));
        assertEquals(11041, mdr_1C.get_IDefNslast1b(22, 1));

        final short[] l1c_spec =  mdr_1C.get_GS1cSpect(23, 1);
        assertEquals(8700, l1c_spec.length);
        assertEquals(4436, l1c_spec[0]);
        assertEquals(3326, l1c_spec[4268]);

        assertEquals(3, mdr_1C.get_GCcsRadAnalNbClass(24, 0));
        assertEquals(0, mdr_1C.get_IDefCcsMode(25, 1));
        assertEquals(49, mdr_1C.get_GCcsImageClassifiedNbLin(26, 0));
        assertEquals(69, mdr_1C.get_GCcsImageClassifiedNbCol(27, 1));
        // @todo 1 tb/tb GIacVarImagIIS 2017-05-05
        // @todo 1 tb/tb GIacAvgImagIIS 2017-05-05
        assertEquals(0, mdr_1C.get_GEUMAvhrr1BCldFrac(28, 0));
        assertEquals(100, mdr_1C.get_GEUMAvhrr1BLandFrac(29, 1));
        assertEquals(0, mdr_1C.get_GEUMAvhrr1BQual(30, 0));
    }
}

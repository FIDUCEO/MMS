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
    }


    @Test
    public void testAccessFields_MB() throws IOException {
        final File file = IASI_TestUtil.getIasiFile_MB();

        iis = new FileImageInputStream(file);
        final MDR_1C mdr_1C = new MDR_1C();
        iis.seek(IASI_TestUtil.MDR_OFFSET_MB + 608 * MDR_1C.RECORD_SIZE);
        iis.read(mdr_1C.getRaw_record());

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
    }
}

package com.bc.fiduceo.reader.insitu.tao.preproc;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(IOTestRunner.class)
public class POSProvider_IO_Test {

    @Test
    public void testOpenAndGet_matchTime() throws IOException {
        final POSProvider posProvider = new POSProvider();
        final File testFile = TestUtil.getTestDataFileAsserted("insitu/tao/TAO_T2N165E_R_POS.ascii");

        posProvider.open(testFile, 165.f, 2.f);

        POSRecord posRecord = posProvider.get(1451952099);
        assertEquals(1451952099, posRecord.date);
        assertEquals(165.15f, posRecord.lon, 1e-8);
        assertEquals(2.02f, posRecord.lat, 1e-8);

        posRecord = posProvider.get(1452304908);
        assertEquals(1452304908, posRecord.date);
        assertEquals(165.14f, posRecord.lon, 1e-8);
        assertEquals(2.f, posRecord.lat, 1e-8);
    }

    @Test
    public void testOpenAndGet_interpolate() throws IOException {
        final POSProvider posProvider = new POSProvider();
        final File testFile = TestUtil.getTestDataFileAsserted("insitu/tao/TAO_T2N165E_R_POS.ascii");

        posProvider.open(testFile,165.f, 2.f);

        POSRecord posRecord = posProvider.get(1451952199);
        assertEquals(1451952199, posRecord.date);
        assertEquals(165.15f, posRecord.lon, 1e-8);
        assertEquals(2.02f, posRecord.lat, 1e-8);

        posRecord = posProvider.get(1533970700);
        assertEquals(1533970700, posRecord.date);
        assertEquals(164.77195739746094f, posRecord.lon, 1e-8);
        assertEquals(2.248743772506714f, posRecord.lat, 1e-8);
    }

    @Test
    public void testOpenAndGet_noTimeMatch() throws IOException {
        final POSProvider posProvider = new POSProvider();
        final File testFile = TestUtil.getTestDataFileAsserted("insitu/tao/TAO_T2N165E_R_POS.ascii");

        posProvider.open(testFile,165.f, 2.f);

        POSRecord posRecord = posProvider.get(1651606499);
        assertEquals(165.f, posRecord.lon, 1e-8);
        assertEquals(2.f, posRecord.lat, 1e-8);
    }

    @Test
    public void testOpenWithNullFileAndGet() throws IOException {
        final POSProvider posProvider = new POSProvider();

        posProvider.open(null,165.f, 2.f);

        POSRecord posRecord = posProvider.get(1651606499);
        assertEquals(165.f, posRecord.lon, 1e-8);
        assertEquals(2.f, posRecord.lat, 1e-8);
    }
}

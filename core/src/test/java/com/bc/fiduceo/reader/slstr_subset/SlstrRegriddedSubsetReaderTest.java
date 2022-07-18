package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(IOTestRunner.class)
public class SlstrRegriddedSubsetReaderTest {

    private SlstrRegriddedSubsetReader reader;

    @Before
    public void setUp() throws Exception {
        reader = new SlstrRegriddedSubsetReader(new ReaderContext(), true);
        reader.open(getSlstrA_File());
    }

    @After
    public void tearDown() throws Exception {
        reader.close();
    }

    @Test
    public void extractName() {
        assertEquals("name.nc", SlstrRegriddedSubsetReader.extractName("name.nc"));
        assertEquals("name.nc", SlstrRegriddedSubsetReader.extractName("egal\\welcher\\pfad\\name.nc"));
        assertEquals("name.nc", SlstrRegriddedSubsetReader.extractName("egal/welcher/pfad/name.nc"));
    }

    private static File getSlstrA_File() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"slstr.a", "1.0", "2020", "05", "22", "S3A_SL_1_RBT____20200522T231202_20200522T231502_20200524T053503_0179_058_286_5580_LN2_O_NT_004.zip"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

}
package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.TestUtil;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SlstrRegriddedSubsetReader_slstrA_Zip_IOTest extends AbstractRegriddedSubsetReader_SlstrA_IOTest {

    @Override
    protected File getSlstrFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"slstr-s3a-uor-n", "1.0", "2020", "05", "22", "S3A_SL_1_RBT____20200522T231202_20200522T231502_20200524T053503_0179_058_286_5580_LN2_O_NT_004.zip"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
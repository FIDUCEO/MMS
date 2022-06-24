package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.TestUtil;

import java.io.File;
import java.io.IOException;

public class SlstrRegriddedSubsetReader_slstrA_unpacked_manifest_IOTest extends AbstractRegriddedSubsetReader_SlstrA_IOTest {

    @Override
    protected File getSlstrFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"slstr.a", "1.0", "2020", "05", "22", "S3A_SL_1_RBT____20200522T231202_20200522T231502_20200524T053503_0179_058_286_5580_LN2_O_NT_004.SEN3", "xfdumanifest.xml"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
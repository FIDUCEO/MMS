package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.TestData;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

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
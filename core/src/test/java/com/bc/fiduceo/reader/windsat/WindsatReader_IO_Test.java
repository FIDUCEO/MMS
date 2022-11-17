package com.bc.fiduceo.reader.windsat;


import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

@RunWith(IOTestRunner.class)
public class WindsatReader_IO_Test {

    private WindsatReader reader;

    @Before
    public void setUp() throws IOException {
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new WindsatReader(readerContext);
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File file = getWindsatFile();

        try {
            reader.open(file);

            // @todo 1 tb/tb continue here 2022-11-17
        } finally {
            reader.close();
        }
    }

    private File getWindsatFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"windsat-coriolis", "v1.0", "2018", "04", "29", "RSS_WindSat_TB_L1C_r79285_20180429T174238_2018119_V08.0.nc"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}

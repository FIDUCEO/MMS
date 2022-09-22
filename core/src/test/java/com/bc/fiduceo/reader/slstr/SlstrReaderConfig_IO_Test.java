package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class SlstrReaderConfig_IO_Test {

    private File testDir;

    @Before
    public void setUp() {
        testDir = TestUtil.getTestDir();
        if (!testDir.mkdirs()) {
            fail("unable to create test directory");
        }
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testLoadFrom() throws IOException {
        final File configFile = new File(testDir, "slstr-reader-config.xml");
        if (!configFile.createNewFile()) {
            fail("unable to create test file");
        }

        final String configXml = "<slstr-reader-config>" +
                "    <use-pixel-geocoding>true</use-pixel-geocoding>" +
                "</slstr-reader-config>";

        TestUtil.writeStringTo(configFile, configXml);

        final SlstrReaderConfig slstrReaderConfig = SlstrReaderConfig.loadFrom(testDir);

        assertTrue(slstrReaderConfig.usePixelGeoCoding());
    }

    @Test
    public void testLoadFrom_noConfigFile() throws IOException {
        final SlstrReaderConfig slstrReaderConfig = SlstrReaderConfig.loadFrom(testDir);

        assertFalse(slstrReaderConfig.usePixelGeoCoding());
    }
}

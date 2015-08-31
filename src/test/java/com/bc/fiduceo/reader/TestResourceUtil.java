package com.bc.fiduceo.reader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.fail;

class TestResourceUtil {

    static File getTestDataDirectory() throws IOException {
        final InputStream resourceStream = TestResourceUtil.class.getResourceAsStream("dataDirectory.properties");
        final Properties properties = new Properties();
        properties.load(resourceStream);
        final String dataDirectoryProperty = properties.getProperty("dataDirectory");
        if (dataDirectoryProperty == null) {
            fail("Property 'dataDirectory' is not set.");
        }
        final File dataDirectory = new File(dataDirectoryProperty);
        if (!dataDirectory.isDirectory()) {
            fail("Property 'dataDirectory' supplied does not exist: '" + dataDirectoryProperty + "'");
        }
        return dataDirectory;
    }
}

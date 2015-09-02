package com.bc.fiduceo.reader;

import org.esa.snap.framework.datamodel.ProductData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

class TestUtil {

    static File getTestDataDirectory() throws IOException {
        final InputStream resourceStream = TestUtil.class.getResourceAsStream("dataDirectory.properties");
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

    public static void assertCorrectUTCDate(Date utcDate, int year, int month, int day, int hour, int minute, int second) {
        final Calendar calendar = ProductData.UTC.createCalendar();
        calendar.setTime(utcDate);

        assertEquals(year, calendar.get(Calendar.YEAR));
        assertEquals(month - 1, calendar.get(Calendar.MONTH));
        assertEquals(day, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(hour, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(minute, calendar.get(Calendar.MINUTE));
        assertEquals(second, calendar.get(Calendar.SECOND));
    }
}

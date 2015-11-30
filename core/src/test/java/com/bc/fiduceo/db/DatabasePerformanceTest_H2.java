package com.bc.fiduceo.db;

import com.bc.fiduceo.TestUtil;
import org.apache.commons.httpclient.util.DateUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.fail;

@Ignore
public class DatabasePerformanceTest_H2 {

    private static final String MERIS_ALL_LIST = "meris_all.list";
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Before
    public void setUp() throws IOException, ParseException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File merisGeometries = new File(testDataDirectory, MERIS_ALL_LIST);
        if (!merisGeometries.isFile()) {
            fail("test file '" + MERIS_ALL_LIST + "' not found");
        }

        final FileInputStream fileInputStream = new FileInputStream(merisGeometries);
        final InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        int numLines = 0;
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            final String[] splits = line.split("\t");

            final String productName = splits[0];
            final Date startDate = dateFormat.parse(splits[1]);
            final Date stopDate = dateFormat.parse(splits[2]);
            final String geometryWKT = splits[3];

        }
        bufferedReader.close();


    }

    @Test
    public void testDummy() {

    }
}

/*
 * Copyright (C) 2015 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.db;

import com.bc.fiduceo.TestUtil;
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

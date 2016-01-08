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
package com.bc.fiduceo.reader;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

/**
 * @author muhammad.bc
 */
public class FilterReadersTest {
    Pattern pattern = Pattern.compile("'?[A-Z].+[MHSX|AMBX].NK.D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.d5");

    @Test
    public void getReaderTest() {
        FilterReaders filterReaders = new FilterReaders();

        Reader reader = filterReaders.getReader("AMSU-B");
        String readerName = reader.getReaderName();
        Assert.assertTrue(readerName.equals("AMSU-B"));


        Reader airs = filterReaders.getReader("AIRS");
        String readerAirs = airs.getReaderName();
        Assert.assertTrue(readerAirs.equals("AIRS"));

        Reader eumesatReader = filterReaders.getReader("EUMETASAT");
        String readerEum = eumesatReader.getReaderName();
        Assert.assertTrue(readerEum.equals("EUMETASAT"));
    }
}

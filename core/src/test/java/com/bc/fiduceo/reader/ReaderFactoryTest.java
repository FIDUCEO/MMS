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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class ReaderFactoryTest {

    @Test
    public void testGetAVHHRReaderKey() throws Exception {
        ReaderFactory readerFactory = new ReaderFactory();
        Reader reader = readerFactory.getReader("avhrr-n06");

        assertNotNull(reader);
        assertEquals("[0-9]{14}-ESACCI-L1C-AVHRR([0-9]{2}|MTA)_G-fv\\d\\d.\\d.nc", reader.getRegEx());
        assertTrue(reader.toString().contains("AVHRR_GAC_Reader"));
    }


    @Test
    public void testGetReaderNullKey() throws Exception {
        ReaderFactory readerFactory = new ReaderFactory();
        try {
            readerFactory.getReader(null);
            fail("The key is null");
        } catch (NullPointerException expect) {
        }
    }

    @Test
    public void testGetReaderEmptyKey() throws Exception {
        ReaderFactory readerFactory = new ReaderFactory();
        try {
            readerFactory.getReader("");
            fail("The key is empty");
        } catch (IllegalArgumentException expect) {
        }
    }

    @Test
    public void testGetReaderKeyNonExist() throws Exception {
        ReaderFactory readerFactory = new ReaderFactory();
        try {
            readerFactory.getReader("uztierter");
            fail("The key doest not exist");
        } catch (NullPointerException expect) {
        }
    }
}
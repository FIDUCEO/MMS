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
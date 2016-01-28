package com.bc.fiduceo.ingest;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.reader.ReadersPlugin;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class InjectionParameterTest {

    @Test
    public void satelliteTypeTest() {
        Assert.assertEquals("NP", ReadersPlugin.valueOf("N19").getType());
        Assert.assertEquals("NN", ReadersPlugin.valueOf("N18").getType());
        Assert.assertEquals("NM", ReadersPlugin.valueOf("N17").getType());


        Assert.assertEquals("NL", ReadersPlugin.valueOf("N16").getType());
        Assert.assertEquals("NK", ReadersPlugin.valueOf("N15").getType());
        Assert.assertEquals("NJ", ReadersPlugin.valueOf("N14").getType());
        Assert.assertEquals("NH", ReadersPlugin.valueOf("N11").getType());
    }

    @Test
    public void searchTermTest() {
        String[] split = "AMSU-B NOAA-19".split("'* ");
        Assert.assertTrue(split[0].equals("AMSU-B"));
        Assert.assertTrue(split[1].equals("NOAA-19"));


        split = "AMSU_B_NOAA19".split("_B_");
        Assert.assertTrue(split[0].equals("AMSU"));
        Assert.assertTrue(split[1].equals("NOAA19"));

        split = "AMSU_B_NOAA-19".split("_B_");
        Assert.assertTrue(split[0].equals("AMSU"));
        Assert.assertTrue(split[1].equals("NOAA-19"));

        split = "AMSU-B_NOAA-19".split("-B_");
        Assert.assertTrue(split[0].equals("AMSU"));
        Assert.assertTrue(split[1].equals("NOAA-19"));


    }

    @Test
    public void satelliteProductTest() throws IOException {
        ReaderFactory readerFactory = new ReaderFactory();
        File testDataDirectory = TestUtil.getTestDataDirectory();

        File[] searchResult = readerFactory.getSearchResult(testDataDirectory, "amsu N15");
        Assert.assertTrue(searchResult.length > 0);

        String noaa_15 = ReadersPlugin.valueOf("N15").getType();
        Assert.assertTrue(searchResult[0].getName().contains(noaa_15));

        searchResult = readerFactory.getSearchResult(testDataDirectory, "mhs METOP_B");
        noaa_15 = ReadersPlugin.valueOf("METOP_B").getType();
        Assert.assertTrue(searchResult[0].getName().contains(noaa_15));
    }
}

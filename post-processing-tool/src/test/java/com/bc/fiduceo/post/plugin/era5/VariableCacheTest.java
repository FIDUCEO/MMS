package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class VariableCacheTest {

    private VariableCache variableCache;

    @Before
    public void setUp() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File era5RootDir = new File(testDataDirectory, "era-5" + File.separator + "v1");
        assertTrue(era5RootDir.isDirectory());

        final Era5Archive era5Archive = new Era5Archive(era5RootDir.getAbsolutePath());

        variableCache = new VariableCache(era5Archive, 3);
    }

    @After
    public void tearDown() throws IOException {
        variableCache.close();
    }

    @Test
    public void testGet() throws IOException {
        Variable variable = variableCache.get("an_ml_lnsp", 1212145200);
        assertEquals("time latitude longitude", variable.getDimensionsString());
        assertEquals("Logarithm of surface pressure", NetCDFUtils.getAttributeString(variable, "long_name", null));

        variable = variableCache.get("an_ml_q", 1212145200);
        assertEquals("time level latitude longitude", variable.getDimensionsString());
        assertEquals(3.786489628510026E-7, NetCDFUtils.getAttributeFloat(variable, "scale_factor", Float.NaN), 1e-8);
    }

    @Test
    public void testCallGetTwice() throws IOException {
        Variable variable_1 = variableCache.get("an_ml_o3", 1212400800);

        Variable variable_2 = variableCache.get("an_ml_o3", 1212400800);
        assertSame(variable_1, variable_2);
    }

    @Test
    public void testCallGetTwice_closeInbetween() throws IOException {
        Variable variable_1 = variableCache.get("an_sfc_t2m", 1212145200);

        variableCache.close();

        Variable variable_2 = variableCache.get("an_sfc_t2m", 1212145200);
        assertNotSame(variable_1, variable_2);
    }

    @Test
    public void testGet_removeFunctionalityOnFullCache() throws IOException, InterruptedException {
        final Variable varU10 = variableCache.get("an_sfc_u10", 1212400800);
        sleep(50);
        variableCache.get("an_sfc_v10", 1212400800);
        sleep(50);
        variableCache.get("an_sfc_siconc", 1212400800);
        sleep(50);
        // now the cache is full tb 2020-11-25
        variableCache.get("an_sfc_msl", 1212400800);
        sleep(50);
        // now the first u10 variable is removed from cache and opening it again must result in a new object tb 2020-11-25
        final Variable varU10New = variableCache.get("an_sfc_u10", 1212400800);

        assertNotSame(varU10, varU10New);
    }
}

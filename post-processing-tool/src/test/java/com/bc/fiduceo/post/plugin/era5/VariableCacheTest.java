package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class VariableCacheTest {

    private VariableCache variableCache;

    @Before
    public void setUp() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File era5RootDir = new File(testDataDirectory, "era-5" + File.separator + "v1");
        assertTrue(era5RootDir.isDirectory());

        final Era5Archive era5Archive = new Era5Archive(era5RootDir.getAbsolutePath());

        variableCache = new VariableCache(era5Archive);
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
}

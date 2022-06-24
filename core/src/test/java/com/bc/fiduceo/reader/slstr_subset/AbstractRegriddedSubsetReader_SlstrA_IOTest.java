package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.reader.ReaderContext;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;


public abstract class AbstractRegriddedSubsetReader_SlstrA_IOTest {
    private SlstrRegriddedSubsetReader reader;

    @Before
    public void setUp() throws Exception {
        reader = new SlstrRegriddedSubsetReader(new ReaderContext(), true);
        final File slstrFile = getSlstrFile();
        reader.open(slstrFile);
    }

    @After
    public void tearDown() throws Exception {
        reader.close();
    }

    @Test
    public void getVariables() throws IOException, InvalidRangeException {
        final List<Variable> variables = reader.getVariables();

        assertEquals(31, variables.size());
        final List<String> names = variables.stream().map(v -> v.getShortName()).collect(Collectors.toList());
        assertThat(names, Matchers.contains(
                "S1_radiance_in", "S2_radiance_in", "S3_radiance_in", "S4_radiance_in", "S5_radiance_in",
                "S6_radiance_in", "S7_BT_in", "S7_exception_in", "S8_BT_in", "S8_exception_in", "S9_BT_in",
                "S9_exception_in", "bayes_in", "cloud_in", "confidence_in", "pointing_in", "probability_cloud_dual_in",
                "probability_cloud_single_in", "latitude_in", "longitude_in", "latitude_tx", "longitude_tx",
                "sat_azimuth_tn", "sat_path_tn", "sat_zenith_tn", "solar_azimuth_tn", "solar_path_tn",
                "solar_zenith_tn", "detector_in", "pixel_in", "scan_in"));
    }

    protected abstract File getSlstrFile() throws IOException;
}

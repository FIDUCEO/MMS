package com.bc.fiduceo.reader.slstr;

import org.junit.Test;

import static com.bc.fiduceo.reader.slstr.VariableType.*;
import static org.junit.Assert.*;

public class VariableNamesTest {

    @Test
    public void testIsValidName() {
        final VariableNames variableNames = new VariableNames();

        assertTrue(variableNames.isValidName("S2_exception_an"));
        assertTrue(variableNames.isValidName("S8_BT_in"));

        assertFalse(variableNames.isValidName("Heffalump"));
        assertFalse(variableNames.isValidName("time_cn"));
    }

    @Test
    public void testGetVariableType() {
        final VariableNames variableNames = new VariableNames();
        assertEquals(NADIR_500m, variableNames.getVariableType("longitude_tx"));
        assertEquals(NADIR_500m, variableNames.getVariableType("solar_azimuth_tn"));

        assertEquals(NADIR_1km, variableNames.getVariableType("S7_BT_in"));
        assertEquals(NADIR_1km, variableNames.getVariableType("S9_exception_in"));

        assertEquals(OBLIQUE_500m, variableNames.getVariableType("solar_zenith_to"));
        assertEquals(OBLIQUE_500m, variableNames.getVariableType("S4_exception_ao"));

        assertEquals(OBLIQUE_1km, variableNames.getVariableType("S9_BT_io"));
        assertEquals(OBLIQUE_1km, variableNames.getVariableType("bayes_io"));
    }
}

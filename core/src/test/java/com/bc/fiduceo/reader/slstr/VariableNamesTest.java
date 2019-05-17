package com.bc.fiduceo.reader.slstr;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    }
}

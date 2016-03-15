/*
 * Copyright (C) 2016 Brockmann Consult GmbH
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

package com.bc.fiduceo.matchup.writer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VariablePrototypeTest {

    private VariablePrototype config;

    @Before
    public void setUp() throws Exception {
        config = new VariablePrototype();
    }

    @Test
    public void testSetGetName() {
        final String name = "the_variable_name";

        config.setName(name);
        assertEquals(name, config.getName());
    }

    @Test
    public void testSetGetDimensionNames() {
        final String dimensionNames = "matchup ny ny";

        config.setDimensionNames(dimensionNames);
        assertEquals(dimensionNames, config.getDimensionNames());
    }

    @Test
    public void testSetGetDataType() {
         final String dataType = "float";

        config.setDataType(dataType);
        assertEquals(dataType, config.getDataType());
    }
}

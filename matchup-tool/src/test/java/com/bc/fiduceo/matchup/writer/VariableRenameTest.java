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

public class VariableRenameTest {

    private VariableRename variableRename;

    @Before
    public void setUp() {
        variableRename = new VariableRename();
    }

    @Test
    public void testSetGetSourceName() {
        final String source_1 = "the_variable";
        final String source_2 = "to-be-renamed";

        variableRename.setSourceName(source_1);
        assertEquals(source_1, variableRename.getSourceName());

        variableRename.setSourceName(source_2);
        assertEquals(source_2, variableRename.getSourceName());
    }

    @Test
    public void testSetGetTargetName(){
        final String target_1 = "where do";
        final String target_2 = "you go";

        variableRename.setTargetName(target_1);
        assertEquals(target_1, variableRename.getTargetName());

        variableRename.setTargetName(target_2);
        assertEquals(target_2, variableRename.getTargetName());
    }

    @Test
    public void testConstruction() {
        final VariableRename variableRename = new VariableRename("original", "renamed");

        assertEquals("original", variableRename.getSourceName());
        assertEquals("renamed", variableRename.getTargetName());
    }
}

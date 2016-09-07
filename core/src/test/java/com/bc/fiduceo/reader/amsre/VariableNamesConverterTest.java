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

package com.bc.fiduceo.reader.amsre;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VariableNamesConverterTest {

    private VariableNamesConverter converter;

    @Before
    public void setUp() {
        converter = new VariableNamesConverter();
    }

    @Test
    public void testToMMS() {
        assertEquals("10_7H_Res_1_TB", converter.toMms("10.7H_Res.1_TB"));
        assertEquals("36_5H_Res_1_TB", converter.toMms("36.5H_Res.1_TB"));

        assertEquals("Res1_Surf", converter.toMms("Res1_Surf"));
        assertEquals("Latitude", converter.toMms("Latitude"));
    }

    @Test
    public void testToHDF() {
        assertEquals("18.7H_Res.1_TB", converter.toHdf("18_7H_Res_1_TB"));
        assertEquals("89.0V_Res.1_TB", converter.toHdf("89_0V_Res_1_TB"));

        assertEquals("Earth_Incidence", converter.toHdf("Earth_Incidence"));
        assertEquals("Scan_Quality_Flag", converter.toHdf("Scan_Quality_Flag"));
    }
}

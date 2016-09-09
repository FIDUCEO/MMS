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

package com.bc.fiduceo.reader.ssmt2;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SSMT2_ReaderTest {

    @Test
    public void testAssembleDateString() {
         assertEquals("2006-08-22T14:22:45", SSMT2_Reader.assembleDateString("2006-08-22", "14:22:45.012345"));
         assertEquals("1993-11-02T03:39:22", SSMT2_Reader.assembleDateString("1993-11-02", "03:39:22.654321"));
    }
}

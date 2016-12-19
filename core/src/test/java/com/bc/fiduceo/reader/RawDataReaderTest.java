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

package com.bc.fiduceo.reader;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RawDataReaderTest {

    @Test
    public void testGetInputDimensions() {
        assertEquals(RawDataReader.InputDimension.ONE_D, RawDataReader.getInputDimension(1, new int[]{67}));
        assertEquals(RawDataReader.InputDimension.ONE_D, RawDataReader.getInputDimension(1, new int[]{12}));

        assertEquals(RawDataReader.InputDimension.TWO_D_FALSE_DIMENSION, RawDataReader.getInputDimension(2, new int[]{1, 12}));
        assertEquals(RawDataReader.InputDimension.TWO_D_FALSE_DIMENSION, RawDataReader.getInputDimension(2, new int[]{1, 2689}));

        assertEquals(RawDataReader.InputDimension.TWO_D, RawDataReader.getInputDimension(2, new int[]{450, 2689}));
        assertEquals(RawDataReader.InputDimension.TWO_D, RawDataReader.getInputDimension(2, new int[]{2, 5}));

        assertEquals(RawDataReader.InputDimension.THREE_D_FALSE_DIMENSION, RawDataReader.getInputDimension(3, new int[]{1, 409, 4443}));
        assertEquals(RawDataReader.InputDimension.THREE_D_FALSE_DIMENSION, RawDataReader.getInputDimension(3, new int[]{1, 10, 10}));
    }
}

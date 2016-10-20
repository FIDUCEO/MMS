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

package com.bc.fiduceo.matchup;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SphericalDistanceCalculatorTest {

    @Test
    public void testCalculate() {
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(0, 1, 12.0876, 22.562, 3));
        sampleSet.setSecondary(new Sample(4, 5, 12.0886, 22.572, 3));

        final double km = SphericalDistanceCalculator.calculateKm(sampleSet);

        assertEquals(1.1166796684265137f, (float) km, 1e-8);
    }
}

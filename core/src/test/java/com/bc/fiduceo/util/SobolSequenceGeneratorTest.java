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

package com.bc.fiduceo.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SobolSequenceGeneratorTest {

    @Test
    public void testSkipOneResultsInSameVectorThanCallingNextVectorTwice() {
        SobolSequenceGenerator generator = new SobolSequenceGenerator(1);

        generator.nextVector();
        final double[] secondOutput = generator.nextVector();

        generator = new SobolSequenceGenerator(1);
        generator.skip(1);

        final double[] skippedOutput = generator.nextVector();
        assertArrayEquals(secondOutput, skippedOutput, 0.0);
    }

    @Test
    public void testSkipZeroResultsInSameVector() {
        SobolSequenceGenerator generator = new SobolSequenceGenerator(1);

        final double[] firstOutput = generator.nextVector();

        generator = new SobolSequenceGenerator(1);
        generator.skip(0);

        final double[] skippedOutput = generator.nextVector();
        assertArrayEquals(firstOutput, skippedOutput, 0.0);
    }

    @Test
    public void testConstructionFailsOnIllegalDimension() {
        try {
            new SobolSequenceGenerator(0);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal input dimension: 0", expected.getMessage());
        }

        try {
            new SobolSequenceGenerator(1001);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertEquals("Illegal input dimension: 1001", expected.getMessage());
        }
    }
}

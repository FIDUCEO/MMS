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

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RawDataReaderTest {

    @Test
    public void testGetInputDimensions() {
        assertEquals(RawDataReader.InputDimension.ONE_D, RawDataReader.getInputDimension(1, new int[]{67}));
        assertEquals(RawDataReader.InputDimension.ONE_D, RawDataReader.getInputDimension(1, new int[]{12}));

        assertEquals(RawDataReader.InputDimension.SKALAR, RawDataReader.getInputDimension(1, new int[]{1}));

        assertEquals(RawDataReader.InputDimension.TWO_D_FALSE_DIMENSION, RawDataReader.getInputDimension(2, new int[]{1, 12}));
        assertEquals(RawDataReader.InputDimension.TWO_D_FALSE_DIMENSION, RawDataReader.getInputDimension(2, new int[]{1, 2689}));

        assertEquals(RawDataReader.InputDimension.TWO_D, RawDataReader.getInputDimension(2, new int[]{450, 2689}));
        assertEquals(RawDataReader.InputDimension.TWO_D, RawDataReader.getInputDimension(2, new int[]{2, 5}));

        assertEquals(RawDataReader.InputDimension.THREE_D_FALSE_DIMENSION, RawDataReader.getInputDimension(3, new int[]{1, 409, 4443}));
        assertEquals(RawDataReader.InputDimension.THREE_D_FALSE_DIMENSION, RawDataReader.getInputDimension(3, new int[]{1, 10, 10}));
    }

    @Test
    public void testGetInsideWindow() {
        // Assumptions:
        // Given is a source array with a width of 200 and a height of 100
        // It is to be read from different areas from the array
        // Mostly somewhere inside the array. But sometimes also areas that are partially outside.

        Rectangle2D insideWindow;

        // case: inside
        insideWindow = RawDataReader.getInsideWindow(50, 40, 4, 3, 200, 100);
        assertThat(insideWindow, is(not(nullValue())));
        assertThat(insideWindow, is(instanceOf(Rectangle.class)));
        assertThat(insideWindow, is(equalTo(new Rectangle(50, 40, 4, 3))));

        // case: partly outside top left
        insideWindow = RawDataReader.getInsideWindow(-1, -1, 4, 3, 200, 100);
        assertThat(insideWindow, is(not(nullValue())));
        assertThat(insideWindow, is(instanceOf(Rectangle.class)));
        assertThat(insideWindow, is(equalTo(new Rectangle(0, 0, 3, 2))));

        // case: partly outside lower right
        insideWindow = RawDataReader.getInsideWindow(197, 98, 4, 3, 200, 100);
        assertThat(insideWindow, is(not(nullValue())));
        assertThat(insideWindow, is(instanceOf(Rectangle.class)));
        assertThat(insideWindow, is(equalTo(new Rectangle(197, 98, 3, 2))));
    }
}

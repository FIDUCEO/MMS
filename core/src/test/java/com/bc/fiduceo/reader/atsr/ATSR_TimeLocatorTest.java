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

package com.bc.fiduceo.reader.atsr;


import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.TimeCoding;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ATSR_TimeLocatorTest {

    @Test
    public void testGetTimeFor() {
        final Product product = mock(Product.class);
        final TimeCoding timeCoding = mock(TimeCoding.class);
        when(timeCoding.getMJD(any())).thenReturn(100.0);

        when(product.getSceneTimeCoding()).thenReturn(timeCoding);

        final ATSR_TimeLocator timeLocator = new ATSR_TimeLocator(product);
        assertEquals(955324800000L, timeLocator.getTimeFor(2,5));
    }
}

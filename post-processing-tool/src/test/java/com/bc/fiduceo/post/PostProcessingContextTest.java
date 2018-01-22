/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.post;

import com.bc.fiduceo.util.TempFileUtils;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class PostProcessingContextTest {

    @Test
    public void testSetGetTempFileUtils() {
        final PostProcessingContext context = new PostProcessingContext();
        final TempFileUtils tempFileUtils = new TempFileUtils();

        context.setTempFileUtils(tempFileUtils);
        assertSame(tempFileUtils, context.getTempFileUtils());
    }
}

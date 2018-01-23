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

package com.bc.fiduceo.reader.caliop;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.reader.ReaderContext;
import org.junit.*;

public class CALIOP_SST_WP100_CLay_ReaderTest {

    @Test
    public void testExtractYearMonthDayFromFilename() {
        final CaliopUtils spyCaliopUtils = spy(new CaliopUtils());
        final CALIOP_SST_WP100_CLay_Reader reader = new CALIOP_SST_WP100_CLay_Reader(new ReaderContext(), spyCaliopUtils);
        int[] ymd = reader.extractYearMonthDayFromFilename("CAL_LID_L2_05kmCLay-Standard-V4-10.2011-09-13T00-27-29ZD.hdf");
        assertArrayEquals(new int[]{2011, 9, 13}, ymd);
        verify(spyCaliopUtils, times(1)).extractYearMonthDayFromFilename(anyString());
    }

    @Test
    public void testGetLongitudeVariableName() {
        final CALIOP_SST_WP100_CLay_Reader reader = new CALIOP_SST_WP100_CLay_Reader(new ReaderContext(), new CaliopUtils()); // we do not need a geometry factory for this test tb 2017-08-10

        assertEquals("Longitude", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        final CALIOP_SST_WP100_CLay_Reader reader = new CALIOP_SST_WP100_CLay_Reader(new ReaderContext(), new CaliopUtils()); // we do not need a geometry factory for this test tb 2017-08-10

        assertEquals("Latitude", reader.getLatitudeVariableName());
    }

}
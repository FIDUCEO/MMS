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


import com.bc.fiduceo.TestUtil;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;
import ucar.nc2.Attribute;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AMSRE_ReaderTest {

    @Test
    public void testAssembleUtcString() {
        final String dateString = "2005-02-17";
        final String timeString = "04:46:34.83Z";

        assertEquals("2005-02-17T04:46:34", AMSRE_Reader.assembleUTCString(dateString, timeString));
    }

    @Test
    public void testGetUtcDate() throws IOException {
        final String rangeStartDate = "2005-02-17";
        final String rangeStartTime = "06:25:56.89Z";

        final Attribute startDateAttribute = mock(Attribute.class);
        when(startDateAttribute.getStringValue()).thenReturn(rangeStartDate);

        final Attribute startTimeAttribute = mock(Attribute.class);
        when(startTimeAttribute.getStringValue()).thenReturn(rangeStartTime);

        final ProductData.UTC sensingStart = AMSRE_Reader.getUtcData(startDateAttribute, startTimeAttribute);
        TestUtil.assertCorrectUTCDate(2005, 2, 17, 6, 25, 56, sensingStart.getAsDate());
    }

    @Test
    public void testGetUtcDate_wrongFormat() throws IOException {
        final String rangeStartDate = "you_cant";
        final String rangeStartTime = "parse_this";

        final Attribute startDateAttribute = mock(Attribute.class);
        when(startDateAttribute.getStringValue()).thenReturn(rangeStartDate);

        final Attribute startTimeAttribute = mock(Attribute.class);
        when(startTimeAttribute.getStringValue()).thenReturn(rangeStartTime);

        try {
            AMSRE_Reader.getUtcData(startDateAttribute, startTimeAttribute);
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }
}

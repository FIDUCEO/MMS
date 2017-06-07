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

package com.bc.fiduceo.reader.iasi;

import org.esa.snap.core.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EpsMetopUtilTest {

    private ImageInputStream imageInputStream;

    @Before
    public void setUp() {
        imageInputStream = mock(ImageInputStream.class);
    }

    @Test
    public void testReadShortCdsTime() throws IOException {
        when(imageInputStream.readUnsignedShort()).thenReturn(5732);
        when(imageInputStream.readUnsignedInt()).thenReturn(46074870L);

        final ProductData.UTC utc = EpsMetopUtil.readShortCdsTime(imageInputStream);

        final Calendar calendar = utc.getAsCalendar();
        assertEquals(2015, calendar.get(Calendar.YEAR));
        assertEquals(8, calendar.get(Calendar.MONTH));
        assertEquals(11, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(12, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(47, calendar.get(Calendar.MINUTE));
        assertEquals(54, calendar.get(Calendar.SECOND));
        assertEquals(870, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    public void testReadOBT() throws IOException {
        // upper short is zero
        when(imageInputStream.readUnsignedShort()).thenReturn(0);
        when(imageInputStream.readUnsignedInt()).thenReturn(46074870L);

        long obt = EpsMetopUtil.readOBT(imageInputStream);
        assertEquals(46074870L, obt);

        // lower int is zero, upper one
        when(imageInputStream.readUnsignedShort()).thenReturn(1);
        when(imageInputStream.readUnsignedInt()).thenReturn(0L);

        obt = EpsMetopUtil.readOBT(imageInputStream);
        assertEquals(4294967296L, obt);

        // both
        when(imageInputStream.readUnsignedShort()).thenReturn(128);
        when(imageInputStream.readUnsignedInt()).thenReturn(2048L);

        obt = EpsMetopUtil.readOBT(imageInputStream);
        assertEquals(549755815936L, obt);
    }

    @Test
    public void testReadVInt4() throws IOException {
        when(imageInputStream.readByte()).thenReturn((byte) 2);
        when(imageInputStream.readInt()).thenReturn(200);

        assertEquals(2.0, EpsMetopUtil.readVInt4(imageInputStream), 1e-8);

        when(imageInputStream.readByte()).thenReturn((byte) -2);
        when(imageInputStream.readInt()).thenReturn(200);

        assertEquals(20000.0, EpsMetopUtil.readVInt4(imageInputStream), 1e-8);

        when(imageInputStream.readByte()).thenReturn((byte) 0);
        when(imageInputStream.readInt()).thenReturn(200);

        assertEquals(200.0, EpsMetopUtil.readVInt4(imageInputStream), 1e-8);

        when(imageInputStream.readByte()).thenReturn((byte) 3);
        when(imageInputStream.readInt()).thenReturn(16);

        assertEquals(0.01600000075995922, EpsMetopUtil.readVInt4(imageInputStream), 1e-8);

        when(imageInputStream.readByte()).thenReturn((byte) -3);
        when(imageInputStream.readInt()).thenReturn(16);

        assertEquals(16000.0, EpsMetopUtil.readVInt4(imageInputStream), 1e-8);
    }
}

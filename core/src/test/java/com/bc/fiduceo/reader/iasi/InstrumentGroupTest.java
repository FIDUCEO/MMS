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

import org.junit.Test;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InstrumentGroupTest {

    @Test
    public void testReadInstrumentGroup() throws IOException {
        final ImageInputStream imageInputStream = mock(ImageInputStream.class);
        when(imageInputStream.readByte()).thenReturn((byte) 4);

        final InstrumentGroup instrumentGroup = InstrumentGroup.readInstrumentGroup(imageInputStream);
        assertEquals(InstrumentGroup.AVHRR_3, instrumentGroup);
    }

    @Test
    public void testReadInstrumentGroup_valueOutOfRange() throws IOException {
        final ImageInputStream imageInputStream = mock(ImageInputStream.class);

        when(imageInputStream.readByte()).thenReturn((byte)-1);
        try {
            InstrumentGroup.readInstrumentGroup(imageInputStream);
            fail("IOException expected");
        } catch (IOException expected) {
        }

        when(imageInputStream.readByte()).thenReturn((byte)16);
        try {
            InstrumentGroup.readInstrumentGroup(imageInputStream);
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }
}

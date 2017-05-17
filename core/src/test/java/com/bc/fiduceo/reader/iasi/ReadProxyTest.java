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

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ReadProxyTest {

    @Test
    public void testBytesPerScan() throws IOException {
        final ReadProxy.bytePerScan bytePerScan = new ReadProxy.bytePerScan(12);

        assertEquals(byte.class, bytePerScan.getDataType());

        final MDR_1C mdr_1C = mock(MDR_1C.class);
        when(mdr_1C.readPerScan_byte(12)).thenReturn((byte) 44);

        final Object data = bytePerScan.read(3, 4, mdr_1C);
        assertEquals((byte)44, (byte)(data));

        verify(mdr_1C, times(1)).readPerScan_byte(12);
        verifyNoMoreInteractions(mdr_1C);
    }

    @Test
    public void testIntPerScan() throws IOException {
        final ReadProxy.intPerScan intPerScan = new ReadProxy.intPerScan(13);

        assertEquals(int.class, intPerScan.getDataType());

        final MDR_1C mdr_1C = mock(MDR_1C.class);
        when(mdr_1C.readPerScan_int(13)).thenReturn(45);

        final Object data = intPerScan.read(4, 1, mdr_1C);
        assertEquals(45, (int)(data));

        verify(mdr_1C, times(1)).readPerScan_int(13);
        verifyNoMoreInteractions(mdr_1C);
    }

    @Test
    public void testObtPerEVOF() throws IOException {
        final ReadProxy.obtPerEVOF obtPerEVOF = new ReadProxy.obtPerEVOF(14);

        assertEquals(long.class, obtPerEVOF.getDataType());

        final MDR_1C mdr_1C = mock(MDR_1C.class);
        when(mdr_1C.get_OBT(5, 0)).thenReturn(46L);

        final Object data = obtPerEVOF.read(5, 0, mdr_1C);
        assertEquals(46L, (long)(data));

        verify(mdr_1C, times(1)).get_OBT(5, 0);
        verifyNoMoreInteractions(mdr_1C);
    }

    @Test
    public void testUtcPerEVOF() throws IOException {
        final ReadProxy.utcPerEVOF utcPerEVOF = new ReadProxy.utcPerEVOF(15);

        assertEquals(long.class, utcPerEVOF.getDataType());

        final MDR_1C mdr_1C = mock(MDR_1C.class);
        when(mdr_1C.readPerEFOV_utc(6, 15)).thenReturn(47L);     // notabene: the second argument is the offset into the record

        final Object data = utcPerEVOF.read(6, 1, mdr_1C);
        assertEquals(47L, (long)(data));

        verify(mdr_1C, times(1)).readPerEFOV_utc(6, 15);
        verifyNoMoreInteractions(mdr_1C);
    }

    @Test
    public void testBytePerEVOF() throws IOException {
        final ReadProxy.bytePerEVOF bytePerEVOF = new ReadProxy.bytePerEVOF(16);

        assertEquals(byte.class, bytePerEVOF.getDataType());

        final MDR_1C mdr_1C = mock(MDR_1C.class);
        when(mdr_1C.readPerEFOV_byte(7, 16)).thenReturn((byte)48);     // notabene: the second argument is the offset into the record

        final Object data = bytePerEVOF.read(7, 1, mdr_1C);
        assertEquals((byte)48, (byte)(data));

        verify(mdr_1C, times(1)).readPerEFOV_byte(7, 16);
        verifyNoMoreInteractions(mdr_1C);
    }

    @Test
    public void testShortPerEVOF() throws IOException {
        final ReadProxy.shortPerEVOF shortPerEVOF = new ReadProxy.shortPerEVOF(17);

        assertEquals(short.class, shortPerEVOF.getDataType());

        final MDR_1C mdr_1C = mock(MDR_1C.class);
        when(mdr_1C.readPerEFOV_short(8, 17)).thenReturn((short)49);     // notabene: the second argument is the offset into the record

        final Object data = shortPerEVOF.read(8, 8, mdr_1C);
        assertEquals((short)49, (short)(data));

        verify(mdr_1C, times(1)).readPerEFOV_short(8, 17);
        verifyNoMoreInteractions(mdr_1C);
    }

    @Test
    public void testIntPerEVOF() throws IOException {
        final ReadProxy.intPerEVOF intPerEVOF = new ReadProxy.intPerEVOF(17);

        assertEquals(int.class, intPerEVOF.getDataType());

        final MDR_1C mdr_1C = mock(MDR_1C.class);
        when(mdr_1C.readPerEFOV_int(8, 17)).thenReturn(49);     // notabene: the second argument is the offset into the record

        final Object data = intPerEVOF.read(8, 0, mdr_1C);
        assertEquals(49, (int)(data));

        verify(mdr_1C, times(1)).readPerEFOV_int(8, 17);
        verifyNoMoreInteractions(mdr_1C);
    }

    @Test
    public void testBytePerPixel() throws IOException {
        final ReadProxy.bytePerPixel bytePerPixel = new ReadProxy.bytePerPixel(19);

        assertEquals(byte.class, bytePerPixel.getDataType());

        final MDR_1C mdr_1C = mock(MDR_1C.class);
        when(mdr_1C.readPerPixel_byte(10, 1, 19)).thenReturn((byte) 51);     // notabene: the third argument is the offset into the record

        final Object data = bytePerPixel.read(10, 1, mdr_1C);
        assertEquals((byte)51, (byte)(data));

        verify(mdr_1C, times(1)).readPerPixel_byte(10, 1, 19);
        verifyNoMoreInteractions(mdr_1C);
    }

    @Test
    public void testShortPerPixel() throws IOException {
        final ReadProxy.shortPerPixel shortPerPixel = new ReadProxy.shortPerPixel(18);

        assertEquals(short.class, shortPerPixel.getDataType());

        final MDR_1C mdr_1C = mock(MDR_1C.class);
        when(mdr_1C.readPerPixel_short(9, 0, 18)).thenReturn((short) 50);     // notabene: the third argument is the offset into the record

        final Object data = shortPerPixel.read(9, 0, mdr_1C);
        assertEquals((short)50, (short)(data));

        verify(mdr_1C, times(1)).readPerPixel_short(9, 0, 18);
        verifyNoMoreInteractions(mdr_1C);
    }

    @Test
    public void testIntPerPixel() throws IOException {
        final ReadProxy.intPerPixel intPerPixel = new ReadProxy.intPerPixel(20);

        assertEquals(int.class, intPerPixel.getDataType());

        final MDR_1C mdr_1C = mock(MDR_1C.class);
        when(mdr_1C.readPerPixel_int(11, 1, 20)).thenReturn(52);     // notabene: the third argument is the offset into the record

        final Object data = intPerPixel.read(11, 1, mdr_1C);
        assertEquals(52, (int)(data));

        verify(mdr_1C, times(1)).readPerPixel_int(11, 1, 20);
        verifyNoMoreInteractions(mdr_1C);
    }

    @Test
    public void testDualIntPerPixel() throws IOException {
        final ReadProxy.dualIntPerPixel dualIntPerPixel = new ReadProxy.dualIntPerPixel(19, 1);

        assertEquals(int.class, dualIntPerPixel.getDataType());

        final MDR_1C mdr_1C = mock(MDR_1C.class);
        when(mdr_1C.readPerPixel_angle(10, 1, 19, 1)).thenReturn(51);     // notabene: the third argument is the offset into the record

        final Object data = dualIntPerPixel.read(10, 1, mdr_1C);
        assertEquals(51, (int)(data));

        verify(mdr_1C, times(1)).readPerPixel_angle(10, 1, 19, 1);
        verifyNoMoreInteractions(mdr_1C);
    }
}

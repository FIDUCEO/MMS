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

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

abstract class MDR_1C {

    static final long DEGRADED_INST_MDR_OFFSET = 20;
    static final long DEGRADED_PROC_MDR_OFFSET = 21;
    static final long GEPS_IASI_MODE_OFFSET = 22;
    static final long GEPS_OPS_PROC_MODE_OFFSET = 26;
    static final long OBT_OFFSET = 8762;
    static final long ONBOARD_UTC_OFFSET = 8942;
    static final long GEPS_DAT_IASI_OFFSET = 9122;
    static final long GEPS_CCD_OFFSET = 9350;
    static final long GEPS_SP_OFFSET = 9380;

    private static final int OBT_SIZE = 6;
    private static final int UTC_SIZE = 6;

    private final byte[] raw_record;

    ImageInputStream iis;

    MDR_1C(byte[] raw_record) {
        this.raw_record = raw_record;
    }

    static int getEFOVIndex(int x, int line) {
        final int xOff = x % 2;
        if (xOff == 0) {
            return 3 - line;
        }
        return line;
    }

    byte[] getRaw_record() {
        return raw_record;
    }

    abstract int getMdrSize();

    long get_OBT(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);

        stream.seek(OBT_OFFSET + mdrPos * OBT_SIZE);

        return EpsMetopUtil.readOBT(stream);
    }

    abstract short[] get_GS1cSpect(int x, int line) throws IOException;

    byte readPerScan_byte(long position) throws IOException {
        final ImageInputStream stream = getStream();
        stream.seek(position);

        return stream.readByte();
    }

    int readPerScan_int(long position) throws IOException {
        final ImageInputStream stream = getStream();
        stream.seek(position);

        return stream.readInt();
    }

    abstract float readPerScan_vInt4(long position) throws IOException;

    byte readPerEFOV_byte(int x, long position) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);

        stream.seek(position + mdrPos);
        return stream.readByte();
    }

    abstract short readPerEFOV_short(int x, long position) throws IOException;

    int readPerEFOV_int(int x, long position) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);

        stream.seek(position + mdrPos * 4);
        return stream.readInt();
    }

    long readPerEFOV_utc(int x, long position) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);

        stream.seek(position + mdrPos * UTC_SIZE);

        return EpsMetopUtil.readShortCdsTime(stream).getAsDate().getTime();
    }

    abstract float readPerEFOV_vInt4(int x, long position) throws IOException;

    abstract byte readPerPixel_byte(int x, int line, long position) throws IOException;

    abstract short readPerPixel_short(int x, int line, long position) throws IOException;

    abstract int readPerPixel_int(int x, int line, long position) throws IOException;

    abstract int readPerPixel_oneOfDualInt(int x, int line, long position, int offset) throws IOException;

    // @todo 1 tb/tb reanimate this 2017-06-14
    //abstract HashMap<String, ReadProxy> getReadProxies();

    int getMdrPos(int x) {
        return x / 2;
    }

    ImageInputStream getStream() {
        if (iis == null) {
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(raw_record);
            iis = new MemoryCacheImageInputStream(byteArrayInputStream);
        }

        return iis;
    }
}

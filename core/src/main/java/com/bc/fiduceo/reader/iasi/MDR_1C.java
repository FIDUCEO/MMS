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

class MDR_1C {

    static final int RECORD_SIZE = 2728908;

    private static final long DEGRADED_INST_MDR_OFFSET = 20;
    private static final long DEGRADED_PROC_MDR_OFFSET = 21;
    private static final long GEPS_IASI_MODE_OFFSET = 22;
    private static final long GEPS_OPS_PROC_MODE_OFFSET = 26;
    private static final long OBT_OFFSET = 8762;
    private static final long ONBOARD_UTC_OFFSET = 8942;
    private static final long GEPS_DAT_IASI_OFFSET = 9122;
    private static final long GEPS_CCD_OFFSET = 9350;
    private static final long GEPS_SP_OFFSET = 9380;
    private static final long GQIS_FLAG_QUAL_DET_OFFSET = 255620;
    private static final long GQIS_SYS_TEC_IIS_QUAL_OFFSET = 255885;
    private static final long GQIS_SYS_TEC_SOND_QUAL_OFFSET = 255889;

    private static final int OBT_SIZE = 6;
    private static final int ONBOARD_UTC_SIZE = 6;
    private static final int GEPS_DAT_IASI_SIZE = 6;
    private static final int GEPS_SP_SIZE = 4;
    private static final int GQIS_FLAG_QUAL_DET_SIZE = 2;

    private final byte[] raw_record;

    private ImageInputStream iis;

    MDR_1C() {
        raw_record = new byte[RECORD_SIZE];
    }

    byte[] getRaw_record() {
        return raw_record;
    }

    byte get_DEGRADED_INST_MDR(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        stream.seek(DEGRADED_INST_MDR_OFFSET);

        return stream.readByte();
    }

    byte get_DEGRADED_PROC_MDR(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        stream.seek(DEGRADED_PROC_MDR_OFFSET);

        return stream.readByte();
    }

    int get_GEPSIasiMode(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        stream.seek(GEPS_IASI_MODE_OFFSET);

        return stream.readInt();
    }

    int get_GEPSOPSProcessingMode(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        stream.seek(GEPS_OPS_PROC_MODE_OFFSET);

        return stream.readInt();
    }

    long get_OBT(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);

        stream.seek(OBT_OFFSET + mdrPos * OBT_SIZE);

        return EpsMetopUtil.readOBT(stream);
    }

    long get_OnboardUTC(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);

        stream.seek(ONBOARD_UTC_OFFSET + mdrPos * ONBOARD_UTC_SIZE);

        return EpsMetopUtil.readShortCdsTime(stream).getAsDate().getTime();
    }

    long get_GEPSDatIasi(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);

        stream.seek(GEPS_DAT_IASI_OFFSET + mdrPos * GEPS_DAT_IASI_SIZE);

        return EpsMetopUtil.readShortCdsTime(stream).getAsDate().getTime();
    }

    byte get_GEPS_CCD(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);

        stream.seek(GEPS_CCD_OFFSET + mdrPos);

        return stream.readByte();
    }

    int get_GEPS_SP(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);

        stream.seek(GEPS_SP_OFFSET + mdrPos * GEPS_SP_SIZE);

        return stream.readInt();
    }

    short get_GQisFlagQualDetailed(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);
        final int efovIndex = getEFOVIndex(x, line);

        stream.seek(GQIS_FLAG_QUAL_DET_OFFSET + mdrPos * 4 * GQIS_FLAG_QUAL_DET_SIZE + efovIndex);

        return stream.readShort();
    }

    int get_GQisSysTecIISQual(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();

        stream.seek(GQIS_SYS_TEC_IIS_QUAL_OFFSET);
        return (int) stream.readUnsignedInt();
    }

    int get_GQisSysTecSondQual(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();

        stream.seek(GQIS_SYS_TEC_SOND_QUAL_OFFSET);
        return (int) stream.readUnsignedInt();
    }

    private int getMdrPos(int x) {
        return x / 2;
    }

    private ImageInputStream getStream() {
        if (iis == null) {
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(raw_record);
            iis = new MemoryCacheImageInputStream(byteArrayInputStream);
        }

        return iis;
    }

    // package access for testing only tb 2017-05-04
    static int getEFOVIndex(int x, int line) {
        final int xOff = x % 2;
        if (xOff == 0) {
            return 3 - line;
        }
        return line;
    }
}

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

import static com.bc.fiduceo.reader.iasi.EpsMetopConstants.G_GEO_SOND_LOC_SCALING_FACTOR;
import static com.bc.fiduceo.reader.iasi.EpsMetopConstants.PN;
import static com.bc.fiduceo.reader.iasi.EpsMetopConstants.SS;

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
    private static final long GGEO_SOND_LOC_OFFSET = 255893;
    private static final long GGEO_SOND_ANGLES_METOP_OFFSET = 256853;
    private static final long GGEO_SOND_ANGLES_SUN_OFFSET = 263813;
    private static final long EARTH_SATELLITE_DISTANCE_OFFSET = 276773;
    private static final long IDEF_NS_FIRST_1B_OFFSET = 276782;
    private static final long IDEF_NS_LAST_1B_OFFSET = 276786;
    private static final long G1S_SPECT_OFFSET = 276790;
    private static final long GCS_RAD_ANAL_NB_OFFSET = 2365814;
    private static final long IDEF_CS_MODE_OFFSET = 2727614;
    private static final long GCS_IMG_CLASS_LIN_OFFSET = 2727618;
    private static final long GCS_IMG_CLASS_COL_OFFSET = 2727678;
    private static final long GEUM_AVHRR_CLOUD_FRAC_OFFSET = 2728548;
    private static final long GEUM_AVHRR_LAND_FRAC_OFFSET = 2728668;
    private static final long GEUM_AVHRR_QUAL_OFFSET = 2728788;

    private static final int OBT_SIZE = 6;
    private static final int ONBOARD_UTC_SIZE = 6;
    private static final int GEPS_DAT_IASI_SIZE = 6;
    private static final int GEPS_SP_SIZE = 4;
    private static final int GQIS_FLAG_QUAL_DET_SIZE = 2;
    private static final int GGEO_SOND_LOC_SIZE = 8;    // two integers, lon + lat tb 2015-05-05
    private static final int GGEO_SOND_ANGLES_METOP_SIZE = 8;    // two integers, zenith + azimuth tb 2015-05-05
    private static final int GGEO_SOND_ANGLES_SUN_SIZE = 8;    // two integers, zenith + azimuth tb 2015-05-05
    private static final int G1S_SPECT_SIZE = 17400;    // 8700 shorts tb 2015-05-05
    private static final int GCS_RAD_ANAL_NB_SIZE = 4;
    private static final int GCS_IMG_CLASS_LIN_SIZE = 2;
    private static final int GCS_IMG_CLASS_COL_SIZE = 2;

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

        stream.seek(GQIS_FLAG_QUAL_DET_OFFSET + (mdrPos * PN + efovIndex) * GQIS_FLAG_QUAL_DET_SIZE);

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

    float get_GGeoSondLoc_Lon(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);
        final int efovIndex = getEFOVIndex(x, line);

        stream.seek(GGEO_SOND_LOC_OFFSET + (mdrPos * PN + efovIndex) * GGEO_SOND_LOC_SIZE);

        final int lonInt = stream.readInt();
        return G_GEO_SOND_LOC_SCALING_FACTOR * lonInt;
    }

    float get_GGeoSondLoc_Lat(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);
        final int efovIndex = getEFOVIndex(x, line);

        stream.seek(GGEO_SOND_LOC_OFFSET + (mdrPos * PN + efovIndex) * GGEO_SOND_LOC_SIZE + 4);

        final int latInt = stream.readInt();
        return G_GEO_SOND_LOC_SCALING_FACTOR * latInt;
    }

    float get_GGeoSondAnglesMETOP_Zenith(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);
        final int efovIndex = getEFOVIndex(x, line);

        stream.seek(GGEO_SOND_ANGLES_METOP_OFFSET + (mdrPos * PN + efovIndex) * GGEO_SOND_ANGLES_METOP_SIZE);

        final int zenithInt = stream.readInt();
        return G_GEO_SOND_LOC_SCALING_FACTOR * zenithInt;
    }

    float get_GGeoSondAnglesMETOP_Azimuth(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);
        final int efovIndex = getEFOVIndex(x, line);

        stream.seek(GGEO_SOND_ANGLES_METOP_OFFSET + (mdrPos * PN + efovIndex) * GGEO_SOND_ANGLES_METOP_SIZE + 4);

        final int azimuthInt = stream.readInt();
        return G_GEO_SOND_LOC_SCALING_FACTOR * azimuthInt;
    }

    float get_GGeoSondAnglesSUN_Zenith(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);
        final int efovIndex = getEFOVIndex(x, line);

        stream.seek(GGEO_SOND_ANGLES_SUN_OFFSET + (mdrPos * PN + efovIndex) * GGEO_SOND_ANGLES_SUN_SIZE);

        final int zenithInt = stream.readInt();
        return G_GEO_SOND_LOC_SCALING_FACTOR * zenithInt;
    }

    float get_GGeoSondAnglesSUN_Azimuth(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);
        final int efovIndex = getEFOVIndex(x, line);

        stream.seek(GGEO_SOND_ANGLES_SUN_OFFSET + (mdrPos * PN + efovIndex) * GGEO_SOND_ANGLES_SUN_SIZE + 4);

        final int azimuthInt = stream.readInt();
        return G_GEO_SOND_LOC_SCALING_FACTOR * azimuthInt;
    }

    int get_EARTH_SATELLITE_DISTANCE(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();

        stream.seek(EARTH_SATELLITE_DISTANCE_OFFSET);
        return (int) stream.readUnsignedInt();
    }

    int get_IDefNsfirst1b(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();

        stream.seek(IDEF_NS_FIRST_1B_OFFSET);
        return stream.readInt();
    }

    int get_IDefNslast1b(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();

        stream.seek(IDEF_NS_LAST_1B_OFFSET);
        return stream.readInt();
    }

    short[] get_GS1cSpect(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);
        final int efovIndex = getEFOVIndex(x, line);

        stream.seek(G1S_SPECT_OFFSET + (mdrPos * PN + efovIndex) * G1S_SPECT_SIZE);

        final short[] spectrum = new short[SS];
        for (int i = 0; i < SS; i++) {
            spectrum[i] = stream.readShort();
        }
        return spectrum;
    }

    int get_GCcsRadAnalNbClass(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);
        final int efovIndex = getEFOVIndex(x, line);

        stream.seek(GCS_RAD_ANAL_NB_OFFSET + (mdrPos * PN + efovIndex) * GCS_RAD_ANAL_NB_SIZE);

        return stream.readInt();
    }

    int get_IDefCcsMode(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();

        stream.seek(IDEF_CS_MODE_OFFSET);

        return stream.readInt();
    }

    short get_GCcsImageClassifiedNbLin(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);

        stream.seek(GCS_IMG_CLASS_LIN_OFFSET + mdrPos * GCS_IMG_CLASS_LIN_SIZE);

        return stream.readShort();
    }

    short get_GCcsImageClassifiedNbCol(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);

        stream.seek(GCS_IMG_CLASS_COL_OFFSET + mdrPos * GCS_IMG_CLASS_COL_SIZE);

        return stream.readShort();
    }

    byte get_GEUMAvhrr1BCldFrac(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);
        final int efovIndex = getEFOVIndex(x, line);

        stream.seek(GEUM_AVHRR_CLOUD_FRAC_OFFSET + mdrPos * PN + efovIndex);

        return (byte) stream.readUnsignedByte();
    }

    byte get_GEUMAvhrr1BLandFrac(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);
        final int efovIndex = getEFOVIndex(x, line);

        stream.seek(GEUM_AVHRR_LAND_FRAC_OFFSET + mdrPos * PN + efovIndex);

        return (byte) stream.readUnsignedByte();
    }

    byte get_GEUMAvhrr1BQual(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);
        final int efovIndex = getEFOVIndex(x, line);

        stream.seek(GEUM_AVHRR_QUAL_OFFSET + mdrPos * PN + efovIndex);

        return stream.readByte();
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

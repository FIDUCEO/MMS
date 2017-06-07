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
import java.util.HashMap;

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
    private static final int UTC_SIZE = 6;
    private static final int SHORT_SIZE = 2;
    private static final int INT_SIZE = 4;
    private static final int DUAL_INT_SIZE = 8;
    private static final int G1S_SPECT_SIZE = 17400;    // 8700 shorts tb 2015-05-05

    private final byte[] raw_record;

    private ImageInputStream iis;

    MDR_1C() {
        raw_record = new byte[RECORD_SIZE];
    }

    byte[] getRaw_record() {
        return raw_record;
    }

    long get_OBT(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);

        stream.seek(OBT_OFFSET + mdrPos * OBT_SIZE);

        return EpsMetopUtil.readOBT(stream);
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

    private int getMdrPos(int x) {
        return x / 2;
    }

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

    byte readPerEFOV_byte(int x, long position) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);

        stream.seek(position + mdrPos);
        return stream.readByte();
    }

    short readPerEFOV_short(int x, long position) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);

        stream.seek(position + mdrPos * 2);
        return stream.readShort();
    }

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

    byte readPerPixel_byte(int x, int line, long position) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);
        final int efovIndex = getEFOVIndex(x, line);

        stream.seek(position + mdrPos * PN + efovIndex);

        return stream.readByte();
    }

    short readPerPixel_short(int x, int line, long position) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);
        final int efovIndex = getEFOVIndex(x, line);

        stream.seek(position + (mdrPos * PN + efovIndex) * SHORT_SIZE);

        return stream.readShort();
    }

    int readPerPixel_int(int x, int line, long position) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);
        final int efovIndex = getEFOVIndex(x, line);

        stream.seek(position + (mdrPos * PN + efovIndex) * INT_SIZE);

        return stream.readInt();
    }

    private float readPerPixel_scaledAngle(int x, int line, long position, int offset) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);
        final int efovIndex = getEFOVIndex(x, line);

        stream.seek(position + (mdrPos * PN + efovIndex) * DUAL_INT_SIZE + offset);

        final int angleInt = stream.readInt();
        return G_GEO_SOND_LOC_SCALING_FACTOR * angleInt;
    }

    int readPerPixel_angle(int x, int line, long position, int offset) throws IOException {
        final ImageInputStream stream = getStream();
        final int mdrPos = getMdrPos(x);
        final int efovIndex = getEFOVIndex(x, line);

        stream.seek(position + (mdrPos * PN + efovIndex) * DUAL_INT_SIZE + offset);

        return stream.readInt();
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

    static HashMap<String, ReadProxy> getReadProxies() {
        final HashMap<String, ReadProxy> proxies = new HashMap<>();
        proxies.put("DEGRADED_INST_MDR", new ReadProxy.bytePerScan(DEGRADED_INST_MDR_OFFSET));
        proxies.put("DEGRADED_PROC_MDR", new ReadProxy.bytePerScan(DEGRADED_PROC_MDR_OFFSET));
        proxies.put("GEPSIasiMode", new ReadProxy.intPerScan(GEPS_IASI_MODE_OFFSET));
        proxies.put("GEPSOPSProcessingMode", new ReadProxy.intPerScan(GEPS_OPS_PROC_MODE_OFFSET));
        // skipping GEPSIdConf tb 2017-06-07
        // skipping GEPSLocIasiAvhrr_IASI tb 2017-06-07
        // skipping GEPSLocIasiAvhrr_IIS tb 2017-06-07
        proxies.put("OBT", new ReadProxy.obtPerEVOF(OBT_OFFSET));
        proxies.put("OnboardUTC", new ReadProxy.utcPerEVOF(ONBOARD_UTC_OFFSET));
        proxies.put("GEPSDatIasi", new ReadProxy.utcPerEVOF(GEPS_DAT_IASI_OFFSET));
        // skipping GIsfLinOrigin tb 2017-06-07
        // skipping GIsfColOrigin tb 2017-06-07
        // skipping GIsfPds1 tb 2017-06-07
        // skipping GIsfPds2 tb 2017-06-07
        // skipping GIsfPds3 tb 2017-06-07
        // skipping GIsfPds4 tb 2017-06-07
        proxies.put("GEPS_CCD", new ReadProxy.bytePerEVOF(GEPS_CCD_OFFSET));
        proxies.put("GEPS_SP", new ReadProxy.intPerEVOF(GEPS_SP_OFFSET));
        // skipping GIrcImage tb 2017-06-07
        // @todo 3 tb/tb GQisFlagQual - one variable per channel 2017-05-04
        proxies.put("GQisFlagQualDetailed", new ReadProxy.shortPerPixel(GQIS_FLAG_QUAL_DET_OFFSET));
        // @todo 1 tb/tb GQisQualIndex 2017-05-04
        // @todo 1 tb/tb GQisQualIndexIIS 2017-05-04
        // @todo 1 tb/tb GQisQualIndexLoc 2017-05-04
        // @todo 1 tb/tb GQisQualIndexRad 2017-05-04
        // @todo 1 tb/tb GQisQualIndexSpect 2017-05-04
        proxies.put("GQisSysTecIISQual", new ReadProxy.intPerScan(GQIS_SYS_TEC_IIS_QUAL_OFFSET));
        proxies.put("GQisSysTecSondQual", new ReadProxy.intPerScan(GQIS_SYS_TEC_SOND_QUAL_OFFSET));
        proxies.put("GGeoSondLoc_Lon", new ReadProxy.dualIntPerPixel(GGEO_SOND_LOC_OFFSET, 0, 1e-6));
        proxies.put("GGeoSondLoc_Lat", new ReadProxy.dualIntPerPixel(GGEO_SOND_LOC_OFFSET, 4, 1e-6));
        proxies.put("GGeoSondAnglesMETOP_Zenith", new ReadProxy.dualIntPerPixel(GGEO_SOND_ANGLES_METOP_OFFSET, 0, 1e-6));
        proxies.put("GGeoSondAnglesMETOP_Azimuth", new ReadProxy.dualIntPerPixel(GGEO_SOND_ANGLES_METOP_OFFSET, 4, 1e-6));
        proxies.put("GGeoSondAnglesSUN_Zenith", new ReadProxy.dualIntPerPixel(GGEO_SOND_ANGLES_SUN_OFFSET, 0, 1e-6));
        proxies.put("GGeoSondAnglesSUN_Azimuth", new ReadProxy.dualIntPerPixel(GGEO_SOND_ANGLES_SUN_OFFSET, 4, 1e-6));
        // skipping GGeoIISLoc tb 2017-06-07
        proxies.put("EARTH_SATELLITE_DISTANCE", new ReadProxy.intPerScan(EARTH_SATELLITE_DISTANCE_OFFSET));
        // l1c specific --------------------------------------------
        // @todo 1 tb/tb IDefSpectDWn1b 2017-05-04
        proxies.put("IDefNsfirst1b", new ReadProxy.intPerScan(IDEF_NS_FIRST_1B_OFFSET));
        proxies.put("IDefNslast1b", new ReadProxy.intPerScan(IDEF_NS_LAST_1B_OFFSET));
        // @todo 1 tb/tb add spectrum here 2017-05-17
        // skipping IDefCovarMatEigenVal1c tb 2017-06-07
        // skipping IDefCcsChannelId tb 2017-06-07
        proxies.put("GCcsRadAnalNbClass", new ReadProxy.intPerPixel(GCS_RAD_ANAL_NB_OFFSET));
        // skipping GCcsRadAnalWgt tb 2017-06-07
        // skipping GCcsRadAnalY tb 2017-06-07
        // skipping GCcsRadAnalZ tb 2017-06-07
        // skipping GCcsRadAnalMean tb 2017-06-07
        // skipping GCcsRadAnalStd tb 2017-06-07
        // skipping GCcsImageClassified tb 2017-06-07
        proxies.put("IDefCcsMode", new ReadProxy.intPerScan(IDEF_CS_MODE_OFFSET));
        proxies.put("GCcsImageClassifiedNbLin", new ReadProxy.shortPerEVOF(GCS_IMG_CLASS_LIN_OFFSET));
        proxies.put("GCcsImageClassifiedNbCol", new ReadProxy.shortPerEVOF(GCS_IMG_CLASS_COL_OFFSET));
        // @todo 1 tb/tb GCcsImageClassifiedFirstLin 2017-05-04
        // @todo 1 tb/tb GCcsImageClassifiedFirstCol 2017-05-04
        // skipping GCcsRadAnalType tb 2017-06-07
        // @todo 1 tb/tb GIacVarImagIIS 2017-05-05
        // @todo 1 tb/tb GIacAvgImagIIS 2017-05-05
        proxies.put("GEUMAvhrr1BCldFrac", new ReadProxy.bytePerPixel(GEUM_AVHRR_CLOUD_FRAC_OFFSET));
        proxies.put("GEUMAvhrr1BLandFrac", new ReadProxy.bytePerPixel(GEUM_AVHRR_LAND_FRAC_OFFSET));
        proxies.put("GEUMAvhrr1BQual", new ReadProxy.bytePerPixel(GEUM_AVHRR_QUAL_OFFSET));
        return proxies;
    }
}

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

import java.io.IOException;
import java.util.HashMap;

class MDR_1C_v4 extends MDR_1C {

    private static final int RECORD_SIZE = 2727768;

    MDR_1C_v4() {
        super(new byte[RECORD_SIZE]);
    }

    @Override
    int getMdrSize() {
        return RECORD_SIZE;
    }

    @Override
    float readPerScan_vInt4(long position) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    short readPerEFOV_short(int x, long position) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    float readPerEFOV_vInt4(int x, long position) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    byte readPerPixel_byte(int x, int line, long position) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    short readPerPixel_short(int x, int line, long position) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    int readPerPixel_int(int x, int line, long position) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    int readPerPixel_oneOfDualInt(int x, int line, long position, int offset) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    short[] get_GS1cSpect(int x, int line) throws IOException {
        throw new RuntimeException("not implemented");
    }

    static HashMap<String, ReadProxy> getReadProxies() {
        final HashMap<String, ReadProxy> proxies = new HashMap<>();
        proxies.put("DEGRADED_INST_MDR", new ReadProxy.bytePerScan(DEGRADED_INST_MDR_OFFSET));
        proxies.put("DEGRADED_PROC_MDR", new ReadProxy.bytePerScan(DEGRADED_PROC_MDR_OFFSET));
        proxies.put("GEPSIasiMode", new ReadProxy.intPerScan(GEPS_IASI_MODE_OFFSET));
        proxies.put("GEPSOPSProcessingMode", new ReadProxy.intPerScan(GEPS_OPS_PROC_MODE_OFFSET));
        // skipping GEPSIdConf tb 2017-06-14
        // skipping GEPSLocIasiAvhrr_IASI tb 2017-06-14
        // skipping GEPSLocIasiAvhrr_IIS tb 2017-06-14
        proxies.put("OBT", new ReadProxy.obtPerEVOF(OBT_OFFSET));
        proxies.put("OnboardUTC", new ReadProxy.utcPerEVOF(ONBOARD_UTC_OFFSET));
        proxies.put("GEPSDatIasi", new ReadProxy.utcPerEVOF(GEPS_DAT_IASI_OFFSET));
        // skipping GIsfLinOrigin tb 2017-06-14
        // skipping GIsfColOrigin tb 2017-06-14
        // skipping GIsfPds1 tb 2017-06-14
        // skipping GIsfPds2 tb 2017-06-14
        // skipping GIsfPds3 tb 2017-06-14
        // skipping GIsfPds4 tb 2017-06-14
        proxies.put("GEPS_CCD", new ReadProxy.bytePerEVOF(GEPS_CCD_OFFSET));
        proxies.put("GEPS_SP", new ReadProxy.intPerEVOF(GEPS_SP_OFFSET));
        // skipping GIrcImage tb 2017-06-14
        // @todo 3 tb/tb GQisFlagQual - one variable per channel 2017-05-04

        return proxies;
    }
}

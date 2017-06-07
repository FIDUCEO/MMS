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
import java.io.IOException;
import java.util.Arrays;

class GiadrScaleFactors {
    private static final int PN = 4; //number of sounder pixel
    private static final int SS = 8700;

    private short defScaleSondNbScale;
    private short[] defScaleSondNsfirst = new short[10];
    private short[] defScaleSondNslast = new short[10];
    private short[] defScaleSondScaleFactor = new short[10];
    private short defScaleIISScaleFactor;

    void readRecord(ImageInputStream inputStream) throws IOException {
        GenericRecordHeader grh = GenericRecordHeader.readGenericRecordHeader(inputStream);
        if (grh.recordClass != RecordClass.GIADR
                || grh.instrumentGroup != InstrumentGroup.IASI
                || grh.recordSubclass != 1) {
            throw new IllegalArgumentException("Bad GRH.");
        }

        defScaleSondNbScale = inputStream.readShort();
        for (int i = 0; i < 10; i++) {
            defScaleSondNsfirst[i] = inputStream.readShort();
        }
        for (int i = 0; i < 10; i++) {
            defScaleSondNslast[i] = inputStream.readShort();
        }
        for (int i = 0; i < 10; i++) {
            defScaleSondScaleFactor[i] = inputStream.readShort();
        }
        defScaleIISScaleFactor = inputStream.readShort();
    }
}


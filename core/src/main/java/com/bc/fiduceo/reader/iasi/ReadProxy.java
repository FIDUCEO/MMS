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

abstract class ReadProxy {

    final long offset;
    final double scaleFactor;

    ReadProxy(long offset) {
        this(offset,  Double.NaN);
    }

    ReadProxy(long offset, double scaleFactor) {
        this.offset = offset;
        this.scaleFactor = scaleFactor;
    }

    abstract Class getDataType();

    abstract Object read(int x, int line, MDR_1C mdr_1C) throws IOException;

    double getScaleFactor() {
        return scaleFactor;
    }

    static class bytePerScan extends ReadProxy {
        bytePerScan(long offset) {
            super(offset);
        }

        Class getDataType() {
            return byte.class;
        }

        Object read(int x, int line, MDR_1C mdr_1C) throws IOException {
            return mdr_1C.readPerScan_byte(offset);
        }
    }

    static class intPerScan extends ReadProxy {
        intPerScan(long offset) {
            super(offset);
        }

        Class getDataType() {
            return int.class;
        }

        Object read(int x, int line, MDR_1C mdr_1C) throws IOException {
            return mdr_1C.readPerScan_int(offset);
        }
    }

    static class obtPerEVOF extends ReadProxy {
        obtPerEVOF(long offset) {
            super(offset);
        }

        Class getDataType() {
            return long.class;
        }

        Object read(int x, int line, MDR_1C mdr_1C) throws IOException {
            return mdr_1C.get_OBT(x, line);
        }
    }

    static class utcPerEVOF extends ReadProxy {
        utcPerEVOF(long offset) {
            super(offset);
        }

        Class getDataType() {
            return long.class;
        }

        Object read(int x, int line, MDR_1C mdr_1C) throws IOException {
            return mdr_1C.readPerEFOV_utc(x, offset);
        }
    }

    static class bytePerEVOF extends ReadProxy {
        bytePerEVOF(long offset) {
            super(offset);
        }

        Class getDataType() {
            return byte.class;
        }

        Object read(int x, int line, MDR_1C mdr_1C) throws IOException {
            return mdr_1C.readPerEFOV_byte(x, offset);
        }
    }

    static class shortPerEVOF extends ReadProxy {
        shortPerEVOF(long offset) {
            super(offset);
        }

        Class getDataType() {
            return short.class;
        }

        Object read(int x, int line, MDR_1C mdr_1C) throws IOException {
            return mdr_1C.readPerEFOV_short(x, offset);
        }
    }

    static class intPerEVOF extends ReadProxy {
        intPerEVOF(long offset) {
            super(offset);
        }

        Class getDataType() {
            return int.class;
        }

        Object read(int x, int line, MDR_1C mdr_1C) throws IOException {
            return mdr_1C.readPerEFOV_int(x, offset);
        }
    }

    static class bytePerPixel extends ReadProxy {
        bytePerPixel(long offset) {
            super(offset);
        }

        Class getDataType() {
            return byte.class;
        }

        Object read(int x, int line, MDR_1C mdr_1C) throws IOException {
            return mdr_1C.readPerPixel_byte(x, line, offset);
        }
    }

    static class shortPerPixel extends ReadProxy {
        shortPerPixel(long offset) {
            super(offset);
        }

        Class getDataType() {
            return short.class;
        }

        Object read(int x, int line, MDR_1C mdr_1C) throws IOException {
            return mdr_1C.readPerPixel_short(x, line, offset);
        }
    }

    static class intPerPixel extends ReadProxy {
        intPerPixel(long offset) {
            super(offset);
        }

        Class getDataType() {
            return int.class;
        }

        Object read(int x, int line, MDR_1C mdr_1C) throws IOException {
            return mdr_1C.readPerPixel_int(x, line, offset);
        }
    }

    static class dualIntPerPixel extends ReadProxy {
        private final int fieldOffsetInBytes;

        dualIntPerPixel(long offset, int fieldOffsetInBytes, double scaleFactor) {
            super(offset, scaleFactor);
            this.fieldOffsetInBytes = fieldOffsetInBytes;
        }

        Class getDataType() {
            return int.class;
        }

        Object read(int x, int line, MDR_1C mdr_1C) throws IOException {
            return mdr_1C.readPerPixel_angle(x, line, offset, fieldOffsetInBytes);
        }
    }
}

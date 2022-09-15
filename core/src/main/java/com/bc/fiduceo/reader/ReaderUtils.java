/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader;

import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.math.TimeInterval;
import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.io.FileUtils;
import ucar.ma2.DataType;

import java.io.*;
import java.util.Date;
import java.util.zip.GZIPInputStream;


public class ReaderUtils {

    public static Number getDefaultFillValue(int productDataType) {
        switch (productDataType) {
            case ProductData.TYPE_FLOAT64:
                return NetCDFUtils.getDefaultFillValue(double.class);

            case ProductData.TYPE_FLOAT32:
                return NetCDFUtils.getDefaultFillValue(float.class);

            case ProductData.TYPE_INT32:
                return NetCDFUtils.getDefaultFillValue(int.class);

            case ProductData.TYPE_INT16:
                return NetCDFUtils.getDefaultFillValue(short.class);

            case ProductData.TYPE_UINT16:
                return NetCDFUtils.getDefaultFillValue(DataType.SHORT, true);

            case ProductData.TYPE_INT8:
                return NetCDFUtils.getDefaultFillValue(byte.class);

            case ProductData.TYPE_UINT8:
                return NetCDFUtils.getDefaultFillValue(DataType.BYTE, true);
        }
        throw new RuntimeException("getDefaultFillValue not implemented for type: " + productDataType);
    }

    public static boolean mustScale(double scaleFactor, double offset) {
        return scaleFactor != 1.0 || offset != 0.0;
    }

    public static void setTimeAxes(AcquisitionInfo acquisitionInfo, final Geometry timeAxesGeometry, GeometryFactory geometryFactory) {
        final Date sensingStart = acquisitionInfo.getSensingStart();
        final Date sensingStop = acquisitionInfo.getSensingStop();
        if (timeAxesGeometry instanceof GeometryCollection) {
            final GeometryCollection axesCollection = (GeometryCollection) timeAxesGeometry;
            final Geometry[] axesGeometries = axesCollection.getGeometries();
            final TimeAxis[] timeAxes = new TimeAxis[axesGeometries.length];
            final TimeInterval timeInterval = new TimeInterval(sensingStart, sensingStop);
            final TimeInterval[] timeSplits = timeInterval.split(axesGeometries.length);
            for (int i = 0; i < axesGeometries.length; i++) {
                final LineString axisGeometry = (LineString) axesGeometries[i];
                final TimeInterval currentTimeInterval = timeSplits[i];
                timeAxes[i] = geometryFactory.createTimeAxis(axisGeometry, currentTimeInterval.getStartTime(), currentTimeInterval.getStopTime());
            }
            acquisitionInfo.setTimeAxes(timeAxes);
        } else {
            final TimeAxis timeAxis = geometryFactory.createTimeAxis((LineString) timeAxesGeometry, sensingStart, sensingStop);
            acquisitionInfo.setTimeAxes(new TimeAxis[]{timeAxis});
        }
    }

    public static String stripChannelSuffix(String fullVariableName) {
        final int splitIndex = fullVariableName.lastIndexOf("_ch");
        if (splitIndex > 0) {
            return fullVariableName.substring(0, splitIndex);
        }
        return fullVariableName;
    }

    public static int getChannelIndex(String variableName) {
        final int splitIndex = variableName.lastIndexOf("_ch");
        if (splitIndex < 0) {
            return 0;
        }

        final String channelNumber = variableName.substring(splitIndex + 3);
        int channelIndex;
        try {
            channelIndex = Integer.parseInt(channelNumber) - 1;
        } catch (NumberFormatException e) {
            channelIndex = 0;   // fallback for names that contain an "_ch" but are not assembled like "bla_ch08" tb 2016-08-03
        }
        return channelIndex;
    }

    public static boolean isCompressed(File file) {
        final String extension = FileUtils.getExtension(file);
        return extension.equalsIgnoreCase(".gz") || extension.equalsIgnoreCase(".tgz")|| extension.equalsIgnoreCase(".zip");
    }

    /**
     * Uncompresses a file in gzip format to a tmp file.
     *
     * @param gzipFile existing file in gzip format
     * @param tmpFile  new file for the uncompressed content
     * @throws IOException if reading the input, decompression, or writing the output fails
     */
    public static void decompress(File gzipFile, File tmpFile) throws IOException {
        final byte[] buffer = new byte[32768];

        try (InputStream in = new GZIPInputStream(new FileInputStream(gzipFile), 32768);
             OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
            int noOfBytesRead;
            while ((noOfBytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, noOfBytesRead);
            }
        }
    }
}

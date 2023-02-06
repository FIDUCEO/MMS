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
 */

package com.bc.fiduceo.post.plugin.point_distance;

import static com.bc.fiduceo.util.NetCDFUtils.getCenterPosArrayFromMMDFile;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.math.Distance;
import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;

class SphericalDistance extends PostProcessing {

    final String targetVarName;
    final String targetDataType;
    final String primLatVar;
    final String primLatScaleAttrName;
    final String primLatOffsetAttrName;
    final String primLonVar;
    final String primLonScaleAttrName;
    final String primLonOffsetAttrName;
    final String secoLatVar;
    final String secoLatScaleAttrName;
    final String secoLatOffsetAttrName;
    final String secoLonVar;
    final String secoLonScaleAttrName;
    final String secoLonOffsetAttrName;

    SphericalDistance(String targetVarName, String targetDataType,
                      String primLatVar, String primLatScaleAttrName, String primLatOffsetAttrName,
                      String primLonVar, String primLonScaleAttrName, String primLonOffsetAttrName,
                      String secoLatVar, String secoLatScaleAttrName, String secoLatOffsetAttrName,
                      String secoLonVar, String secoLonScaleAttrName, String secoLonOffsetAttrName) {
        this.targetVarName = targetVarName;
        this.targetDataType = targetDataType;
        this.primLatVar = primLatVar;
        this.primLatScaleAttrName = primLatScaleAttrName;
        this.primLatOffsetAttrName = primLatOffsetAttrName;
        this.primLonVar = primLonVar;
        this.primLonScaleAttrName = primLonScaleAttrName;
        this.primLonOffsetAttrName = primLonOffsetAttrName;
        this.secoLatVar = secoLatVar;
        this.secoLatScaleAttrName = secoLatScaleAttrName;
        this.secoLatOffsetAttrName = secoLatOffsetAttrName;
        this.secoLonVar = secoLonVar;
        this.secoLonScaleAttrName = secoLonScaleAttrName;
        this.secoLonOffsetAttrName = secoLonOffsetAttrName;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) {
        final String matchupDimensionName = getMatchupDimensionName();
        writer.addVariable(null, targetVarName, DataType.getType(targetDataType), matchupDimensionName);
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final NetcdfFile netcdfFile = writer.getNetcdfFile();
        final String matchupDimensionName = getMatchupDimensionName();
        final int count = NetCDFUtils.getDimensionLength(matchupDimensionName, netcdfFile);

        final Variable targetVar = netcdfFile.findVariable(targetVarName);

        final Array p_lon = getCenterPosArrayFromMMDFile(netcdfFile, primLonVar, primLonScaleAttrName, primLonOffsetAttrName, matchupDimensionName);
        final Array p_lat = getCenterPosArrayFromMMDFile(netcdfFile, primLatVar, primLatScaleAttrName, primLatOffsetAttrName, matchupDimensionName);
        final Array s_lon = getCenterPosArrayFromMMDFile(netcdfFile, secoLonVar, secoLonScaleAttrName, secoLonOffsetAttrName, matchupDimensionName);
        final Array s_lat = getCenterPosArrayFromMMDFile(netcdfFile, secoLatVar, secoLatScaleAttrName, secoLatOffsetAttrName, matchupDimensionName);

        Array target = Array.factory(DataType.getType(targetDataType), new int[]{count});
        for (int i = 0; i < count; i++) {
            final double pLon = p_lon.getDouble(i);
            final double pLat = p_lat.getDouble(i);
            final double sLon = s_lon.getDouble(i);
            final double sLat = s_lat.getDouble(i);
            final double distanceKm = Distance.computeSphericalDistanceKm(pLon, pLat, sLon, sLat);
            target.setDouble(i, distanceKm);
        }
        writer.write(targetVar, target);
    }
}

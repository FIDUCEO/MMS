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
package com.bc.fiduceo.post.plugin;

import com.bc.fiduceo.math.Distance;
import com.bc.fiduceo.post.PostProcessing;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.List;

public class SphericalDistance extends PostProcessing {

    public final String targetVarName;
    public final String targetDataType;
    public final String targetDimName;
    public final String primLatVar;
    public final String primLatScaleAttrName;
    public final String primLatOffsetAttrName;
    public final String primLonVar;
    public final String primLonScaleAttrName;
    public final String primLonOffsetAttrName;
    public final String secoLatVar;
    public final String secoLatScaleAttrName;
    public final String secoLatOffsetAttrName;
    public final String secoLonVar;
    public final String secoLonScaleAttrName;
    public final String secoLonOffsetAttrName;

    public SphericalDistance(String targetVarName, String targetDataType, String targetDimName,
                             String primLatVar, String primLatScaleAttrName, String primLatOffsetAttrName,
                             String primLonVar, String primLonScaleAttrName, String primLonOffsetAttrName,
                             String secoLatVar, String secoLatScaleAttrName, String secoLatOffsetAttrName,
                             String secoLonVar, String secoLonScaleAttrName, String secoLonOffsetAttrName) {
        this.targetVarName = targetVarName;
        this.targetDataType = targetDataType;
        this.targetDimName = targetDimName;
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
    protected void prepareImpl(NetcdfFile reader, NetcdfFileWriter writer) {
        writer.addVariable(null, targetVarName, DataType.getType(targetDataType), targetDimName);
    }

    @Override
    protected void computeImpl(NetcdfFile reader, NetcdfFileWriter writer, List<Variable> outstandingTransfer) throws IOException, InvalidRangeException {
        final NetcdfFile netcdfFile = writer.getNetcdfFile();
        final Dimension countDimension = getCountDimension(netcdfFile);
        int count = countDimension.getLength();

        final Variable targetVar = netcdfFile.findVariable(targetVarName);
        final Variable primLons = netcdfFile.findVariable(null, primLonVar);
        final Variable primLats = netcdfFile.findVariable(null, primLatVar);
        final Variable secoLons = netcdfFile.findVariable(null, secoLonVar);
        final Variable secoLats = netcdfFile.findVariable(null, secoLatVar);

        final Array p_lon = getCenterPosArray(primLons, primLonScaleAttrName, primLonOffsetAttrName);
        final Array p_lat = getCenterPosArray(primLats, primLatScaleAttrName, primLatOffsetAttrName);
        final Array s_lon = getCenterPosArray(secoLons, secoLonScaleAttrName, secoLonOffsetAttrName);
        final Array s_lat = getCenterPosArray(secoLats, secoLatScaleAttrName, secoLatOffsetAttrName);

        Array target = Array.factory(DataType.getType(targetDataType), new int[]{count});
        for (int i = 0; i < count; i++) {
            final double pLon = p_lon.getDouble(i);
            final double pLat = p_lat.getDouble(i);
            final double sLon = s_lon.getDouble(i);
            final double sLat = s_lat.getDouble(i);
            final double distanceKm = Distance.computeSpericalDistanceKm(pLon, pLat, sLon, sLat);
            target.setDouble(i, distanceKm);
        }
        writer.write(targetVar, target);
    }

    static double getValueFromAttribute(Variable variable, String attrName, final int defaultValue) {
        if (attrName != null) {
            final Attribute attribute = variable.findAttribute(attrName);
            if (attribute == null) {
                throw new RuntimeException("No attribute with name '" + attrName + "'.");
            }
            final Number number = attribute.getNumericValue();
            if (number == null) {
                throw new RuntimeException("Attribute '" + attrName + "' does not own a number value.");
            }
            return number.doubleValue();
        }
        return defaultValue;
    }

    private Array getCenterPosArray(Variable variable, String scaleAttrName, String offsetAttrName) throws IOException, InvalidRangeException {
        final int countIdx = variable.findDimensionIndex(targetDimName);
        final int[] shape = variable.getShape();
        final int[] index = new int[shape.length];
        for (int i = 0; i < shape.length; i++) {
            int dimWith = shape[i];
            if (i != countIdx) {
                index[i] = dimWith / 2;
                shape[i] = 1;
            }
        }

        final Array array = variable.read(index, shape).reduce();

        double scaleFactor = getValueFromAttribute(variable, scaleAttrName, 1);
        double offset = getValueFromAttribute(variable, offsetAttrName, 0);
        if (scaleFactor != 1d || offset != 0d) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, offset);
            return MAMath.convert2Unpacked(array, scaleOffset);
        }
        return array;
    }

    Dimension getCountDimension(NetcdfFile netcdfFile) {
        final Dimension dimension = netcdfFile.findDimension(targetDimName);
        if (dimension == null) {
            throw new RuntimeException("Dimension '" + targetDimName + "' expected");
        }
        return dimension;
    }
}

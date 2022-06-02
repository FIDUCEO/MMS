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

package com.bc.fiduceo.reader.snap;


import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariablePrototype;
import org.esa.snap.core.datamodel.RasterDataNode;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;

import java.util.ArrayList;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_ADD_OFFSET_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_SCALE_FACTOR_NAME;

// This class should be used when driving special purpose classes from the NetCDF Variable class. Overwriting
// all methods with a throws implementation ensures that methods that should be overridden are really overridden;
// calls into the not completely initialized base class are not possible this was tb 2016-09-26
public class VariableProxy extends VariablePrototype {

    private final RasterDataNode rasterDataNode;

    public VariableProxy(RasterDataNode rasterDataNode) {
        this.rasterDataNode = rasterDataNode;
    }

    @Override
    public String getFullName() {
        return rasterDataNode.getName();
    }

    @Override
    public String getShortName() {
        return rasterDataNode.getName();
    }

    @Override
    public DataType getDataType() {
        final int dataType = rasterDataNode.getDataType();
        return NetCDFUtils.getNetcdfDataType(dataType);
    }

    @Override
    public List<Attribute> getAttributes() {
        final ArrayList<Attribute> attributes = new ArrayList<>();

        if (rasterDataNode.isScalingApplied()) {
            final double scalingFactor = rasterDataNode.getScalingFactor();
            if (scalingFactor != 1.0) {
                attributes.add(new Attribute(CF_SCALE_FACTOR_NAME, scalingFactor));
            }

            final double scalingOffset = rasterDataNode.getScalingOffset();
            if (scalingOffset != 0.0) {
                attributes.add(new Attribute(CF_ADD_OFFSET_NAME, scalingOffset));
            }
        }

        if (rasterDataNode.isNoDataValueUsed()) {
            final Number fillValue = rasterDataNode.getNoDataValue();
            attributes.add(new Attribute(CF_FILL_VALUE_NAME, fillValue));
        } else if (getDataType().isNumeric()){
            final Class classType = getDataType().getPrimitiveClassType();
            final Number fillValue = NetCDFUtils.getDefaultFillValue(classType);
            attributes.add(new Attribute(CF_FILL_VALUE_NAME, fillValue));
        }
        return attributes;
    }
}

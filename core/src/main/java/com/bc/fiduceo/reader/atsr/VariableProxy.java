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

package com.bc.fiduceo.reader.atsr;


import com.bc.fiduceo.reader.ReaderUtils;
import org.esa.snap.core.datamodel.RasterDataNode;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;

import java.util.List;

public class VariableProxy extends Variable {

    private final RasterDataNode rasterDataNode;

    public VariableProxy(RasterDataNode rasterDataNode) {
        this.rasterDataNode = rasterDataNode;
    }

    @Override
    public String getFullName() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getShortName() {
        return rasterDataNode.getName();
    }

    @Override
    public String getDescription() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getUnitsString() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int getRank() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int[] getShape() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<Dimension> getDimensions() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public DataType getDataType() {
        final int dataType = rasterDataNode.getDataType();
        return ReaderUtils.getNetcdfDataType(dataType);
    }

    @Override
    public List<Attribute> getAttributes() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Attribute findAttributeIgnoreCase(String s) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int compareTo(VariableSimpleIF o) {
        throw new RuntimeException("not implemented");
    }
}

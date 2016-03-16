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

package com.bc.fiduceo.matchup.writer;

import ucar.nc2.Attribute;

import java.util.List;

public class VariablePrototype {

    private String targetVariableName;
    private String dimensionNames;
    private String dataType;
    private List<Attribute> attributes;

    public String getTargetVariableName() {
        return targetVariableName;
    }

    public void setTargetVariableName(String name) {
        this.targetVariableName = name;
    }

    public String getDimensionNames() {
        return dimensionNames;
    }

    /**
     * Sets the dimension names for this variable. The String must be composed of a blank separated list of dimensions names.
     * The order of the dimensions is "z y x".
     *
     * @param dimensionNames the zero separated dimension names
     */
    public void setDimensionNames(String dimensionNames) {
        this.dimensionNames = dimensionNames;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }
}

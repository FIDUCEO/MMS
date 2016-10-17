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

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.RawDataSource;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class VariablePrototype implements RawDataSource {

    private final RawDataSourceContainer rawDataSourceContainer;
    private List<Attribute> attributes;
    private String targetVariableName;
    private String sourceVariableName;
    private String dimensionNames;
    private String dataType;

    VariablePrototype() {
        this(null);
    }

    VariablePrototype(RawDataSourceContainer rawDataSourceContainer) {
        attributes = new ArrayList<>();
        this.rawDataSourceContainer = rawDataSourceContainer;
    }

    String getTargetVariableName() {
        return targetVariableName;
    }

    void setTargetVariableName(String name) {
        this.targetVariableName = name;
    }

    String getDimensionNames() {
        return dimensionNames;
    }

    /**
     * Sets the dimension names for this variable. The String must be composed of a blank separated list of dimensions names.
     * The order of the dimensions is "z y x".
     *
     * @param dimensionNames the zero separated dimension names
     */
    void setDimensionNames(String dimensionNames) {
        this.dimensionNames = dimensionNames;
    }

    String getDataType() {
        return dataType;
    }

    void setDataType(String dataType) {
        this.dataType = dataType;
    }

    List<Attribute> getAttributes() {
        return attributes;
    }

    void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    String getSourceVariableName() {
        return sourceVariableName;
    }

    void setSourceVariableName(String sourceVariableName) {
        this.sourceVariableName = sourceVariableName;
    }

    @Override
    public void close() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void open(File file) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        return rawDataSourceContainer.getSource().readRaw(centerX, centerY, interval, variableName);
    }
}

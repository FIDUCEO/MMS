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

package com.bc.fiduceo.util;

import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.AttributeContainerMutable;

import java.util.List;

public class VariableProxy extends VariablePrototype {

    private final String name;
    private final DataType dataType;
    private final List<Attribute> attributes;

    public VariableProxy(String name, DataType dataType, List<Attribute> attributes) {
        this.name = name;
        this.dataType = dataType;
        this.attributes = attributes;
        super.attributes = new AttributeContainerMutable(name+ "_atts");
        if (attributes != null) {
            super.attributes.addAll(attributes);
        }
    }

    @Override
    public String getFullName() {
        return name;
    }

    @Override
    public String getShortName() {
        return name;
    }

    @Override
    public DataType getDataType() {
        return dataType;
    }

    @Override
    public List<Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public Attribute findAttribute(String name) {
        for(final Attribute attribute : attributes) {
            if (name.equalsIgnoreCase(attribute.getShortName())) {
                return attribute;
            }
        }

        return null;
    }

    public void setShape(int[] shape) {
        this.shape = shape;
    }

    @Override
    public int[] getShape() {
        return this.shape;
    }
}

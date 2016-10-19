/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.bc.fiduceo.matchup.writer;

import com.bc.fiduceo.core.Interval;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractIOVariable implements IOVariable {

    protected final RawDataSourceContainer rawDataSourceContainer;

    protected String targetVariableName;
    protected String sourceVariableName;
    protected Target target;

    private List<Attribute> attributes;
    private String dimensionNames;
    private String dataType;

    public AbstractIOVariable(RawDataSourceContainer rawDataSourceContainer) {
        attributes = new ArrayList<>();
        this.rawDataSourceContainer = rawDataSourceContainer;
    }

    @Override
    public void setTarget(Target target) {
        this.target = target;
    }

    @Override
    public String getSourceVariableName() {
        return sourceVariableName;
    }

    public void setSourceVariableName(String sourceVariableName) {
        this.sourceVariableName = sourceVariableName;
    }

    @Override
    public List<Attribute> getAttributes() {
        return attributes;
    }

    void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getDataType() {
        return dataType;
    }

    void setDataType(String dataType) {
        this.dataType = dataType;
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
    void setDimensionNames(String dimensionNames) {
        this.dimensionNames = dimensionNames;
    }

    @Override
    public String getTargetVariableName() {
        return targetVariableName;
    }

    public void setTargetVariableName(String name) {
        this.targetVariableName = name;
    }
}

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
package com.bc.fiduceo.post;

import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class PostProcessing {

    private PostProcessingContext context;

    public void setContext(PostProcessingContext context) {
        this.context = context;
    }

    public PostProcessingContext getContext() {
        return context;
    }

    protected List<String> getVariableNamesToRemove() {
        return new ArrayList<>();
    }

    protected abstract void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException;

    protected abstract void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException;
}

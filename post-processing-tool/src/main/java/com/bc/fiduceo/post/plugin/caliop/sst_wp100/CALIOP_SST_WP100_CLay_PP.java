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

package com.bc.fiduceo.post.plugin.caliop.sst_wp100;

import com.bc.fiduceo.post.PostProcessing;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import java.io.IOException;

public class CALIOP_SST_WP100_CLay_PP extends PostProcessing {

    final String variableName_caliopVFM_fileName;
    final String variableName_caliopVFM_y;
    final String processingVersion;

    public CALIOP_SST_WP100_CLay_PP(String variableName_caliopVFM_fileName,
                                    String variableName_caliopVFM_y,
                                    String processingVersion) {
        this.variableName_caliopVFM_fileName = variableName_caliopVFM_fileName;
        this.variableName_caliopVFM_y = variableName_caliopVFM_y;
        this.processingVersion = processingVersion;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {

    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {

    }
}

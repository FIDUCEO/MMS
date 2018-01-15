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

package com.bc.fiduceo.reader.amsr;

import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;

public class AmsrUtils {

    public static void setNodeType(AcquisitionInfo acquisitionInfo, NetcdfFile netcdfFile) {
        final Attribute orbitDirectionAttribute = NetCDFUtils.getGlobalAttributeSafe("OrbitDirection", netcdfFile);
        final String orbitDirection = orbitDirectionAttribute.getStringValue();
        assignNodeType(acquisitionInfo, orbitDirection);
    }

    // package access for testing only tb 2018-01-15
    static void assignNodeType(AcquisitionInfo acquisitionInfo, String orbitDirection) {
        if ("Ascending".equalsIgnoreCase(orbitDirection)) {
            acquisitionInfo.setNodeType(NodeType.ASCENDING);
        } else if ("Descending".equalsIgnoreCase(orbitDirection)) {
            acquisitionInfo.setNodeType(NodeType.DESCENDING);
        } else {
            acquisitionInfo.setNodeType(NodeType.UNDEFINED);
        }
    }
}

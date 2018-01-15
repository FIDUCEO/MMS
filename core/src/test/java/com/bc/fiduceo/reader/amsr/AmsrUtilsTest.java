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
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AmsrUtilsTest {

    @Test
    public void testAssignNodeType() {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        AmsrUtils.assignNodeType(acquisitionInfo, "Ascending");
        assertEquals(NodeType.ASCENDING, acquisitionInfo.getNodeType());

        AmsrUtils.assignNodeType(acquisitionInfo, "Descending");
        assertEquals(NodeType.DESCENDING, acquisitionInfo.getNodeType());

        AmsrUtils.assignNodeType(acquisitionInfo, "quer");
        assertEquals(NodeType.UNDEFINED, acquisitionInfo.getNodeType());
    }
}

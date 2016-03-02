/*
 * Copyright (C) 2015 Brockmann Consult GmbH
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

package com.bc.fiduceo.reader;


import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;

import java.util.Date;
import java.util.List;

public class AcquisitionInfo {

    private List<Point> coordinates;
    private List<Polygon> multiPolygons;
    private int[] timeAxisStartIndices;
    private int[] timeAxisEndIndices;
    private Date sensingStart;
    private Date sensingStop;
    private NodeType nodeType;

    // @todo tb/** remove this and use GeometryCollection instead 2016-03-02
    @Deprecated
    public List<Polygon> getMultiPolygons() {
        return multiPolygons;
    }

    // @todo tb/** remove this and use GeometryCollection instead 2016-03-02
    @Deprecated
    public void setMultiPolygons(List<Polygon> multiPolygons) {
        this.multiPolygons = multiPolygons;
    }

    public List<Point> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Point> coordinates) {
        this.coordinates = coordinates;
    }

    public int[] getTimeAxisStartIndices() {
        return timeAxisStartIndices;
    }

    public void setTimeAxisStartIndices(int[] timeAxisStartIndices) {
        this.timeAxisStartIndices = timeAxisStartIndices;
    }

    public int[] getTimeAxisEndIndices() {
        return timeAxisEndIndices;
    }

    public void setTimeAxisEndIndices(int[] timeAxisEndIndices) {
        this.timeAxisEndIndices = timeAxisEndIndices;
    }

    public Date getSensingStart() {
        return sensingStart;
    }

    public void setSensingStart(Date sensingStart) {
        this.sensingStart = sensingStart;
    }

    public Date getSensingStop() {
        return sensingStop;
    }

    public void setSensingStop(Date sensingStop) {
        this.sensingStop = sensingStop;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }
}

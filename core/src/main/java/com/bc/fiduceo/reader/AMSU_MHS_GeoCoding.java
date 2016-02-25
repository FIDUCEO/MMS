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

package com.bc.fiduceo.reader;


import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.dataop.maptransf.Datum;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

class AMSU_MHS_GeoCoding implements GeoCoding{

    @Override
    public boolean isCrossingMeridianAt180() {
        throw new RuntimeException("not Implemented");
    }

    @Override
    public boolean canGetPixelPos() {
        throw new RuntimeException("not Implemented");
    }

    @Override
    public boolean canGetGeoPos() {
        throw new RuntimeException("not Implemented");
    }

    @Override
    public PixelPos getPixelPos(GeoPos geoPos, PixelPos pixelPos) {
        throw new RuntimeException("not Implemented");
    }

    @Override
    public GeoPos getGeoPos(PixelPos pixelPos, GeoPos geoPos) {
        throw new RuntimeException("not Implemented");
    }

    @Override
    public Datum getDatum() {
        throw new RuntimeException("not Implemented");
    }

    @Override
    public void dispose() {
        throw new RuntimeException("not Implemented");
    }

    @Override
    public CoordinateReferenceSystem getImageCRS() {
        throw new RuntimeException("not Implemented");
    }

    @Override
    public CoordinateReferenceSystem getMapCRS() {
        throw new RuntimeException("not Implemented");
    }

    @Override
    public CoordinateReferenceSystem getGeoCRS() {
        throw new RuntimeException("not Implemented");
    }

    @Override
    public MathTransform getImageToMapTransform() {
        throw new RuntimeException("not Implemented");
    }
}

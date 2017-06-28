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
package com.bc.fiduceo.post.util;

import com.bc.fiduceo.log.FiduceoLogger;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.dataio.netcdf.metadata.profiles.beam.BeamNetCdfReaderPlugIn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DistanceToLandMap {

    private final Path distancePath;
    private Band distanceToLand;
    private GeoCoding geoCoding;
    private boolean mustBeInitialized;
    private Product product;

    public DistanceToLandMap(Path path) {
        distancePath = path;
        mustBeInitialized = true;
        validatePath();
    }

    public double getDistance(double longitude, double latitude) throws IOException {
        if (mustBeInitialized) {
            init();
            mustBeInitialized = false;
        }
        final PixelPos pixelPos = geoCoding.getPixelPos(new GeoPos(latitude, longitude), null);
        final int x = (int) pixelPos.getX();
        final int y = (int) pixelPos.getY();
        return distanceToLand.getPixelDouble(x, y);
    }

    public void close() {
        if (product != null) {
            product.dispose();
        }
    }

    private void init() {
        final String absolutePathString = distancePath.toAbsolutePath().toString();
        final ProductReaderPlugIn readerPlugIn = new BeamNetCdfReaderPlugIn();
        final ProductReader reader = readerPlugIn.createReaderInstance();
        try {
            product = reader.readProductNodes(absolutePathString, null);
            distanceToLand = product.getBand("distance_to_land");
            distanceToLand.readRasterDataFully();
            geoCoding = product.getSceneGeoCoding();
        } catch (IOException e) {
            throw new RuntimeException("Error while reading. '" + absolutePathString +"'", e);
        }
    }

    private void validatePath() {
        final String absolutePathString = distancePath.toAbsolutePath().toString();
        if (!Files.isRegularFile(distancePath)) {
            throw new RuntimeException("Missing file: '" + absolutePathString + "'");
        }
        if (!Files.isReadable(distancePath)) {
            throw new RuntimeException("No read access to: '" + absolutePathString + "'");
        }
    }
}

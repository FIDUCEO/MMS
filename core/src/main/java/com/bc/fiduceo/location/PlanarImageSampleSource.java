package com.bc.fiduceo.location;

import javax.media.jai.PlanarImage;
import java.awt.image.Raster;

public class PlanarImageSampleSource implements SampleSource {

        private final PlanarImage image;

        public PlanarImageSampleSource(PlanarImage image) {
            this.image = image;
        }

        @Override
        public int getSample(int x, int y) {
            return getSample(x, y, image);
        }

        @Override
        public double getSampleDouble(int x, int y) {
            return getSampleDouble(x, y, image);
        }

        private static int getSample(int pixelX, int pixelY, PlanarImage image) {
            final int x = image.getMinX() + pixelX;
            final int y = image.getMinY() + pixelY;
            final int tileX = image.XToTileX(x);
            final int tileY = image.YToTileY(y);
            final Raster data = image.getTile(tileX, tileY);

            return data.getSample(x, y, 0);
        }

        private static double getSampleDouble(int pixelX, int pixelY, PlanarImage image) {
            final int x = image.getMinX() + pixelX;
            final int y = image.getMinY() + pixelY;
            final int tileX = image.XToTileX(x);
            final int tileY = image.YToTileY(y);
            final Raster data = image.getTile(tileX, tileY);

            return data.getSampleDouble(x, y, 0);
        }
    }

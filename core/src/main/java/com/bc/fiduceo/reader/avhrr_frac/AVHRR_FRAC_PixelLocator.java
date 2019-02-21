package com.bc.fiduceo.reader.avhrr_frac;

import com.bc.fiduceo.location.PixelLocator;
import org.esa.snap.core.datamodel.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class AVHRR_FRAC_PixelLocator implements PixelLocator {

    private static final double THRESH = 89.0;

    private final PixelPos pixelPos;
    private final GeoPos geoPos;

    private GeoCoding geoCoding;


    AVHRR_FRAC_PixelLocator(TiePointGrid longitude, TiePointGrid latitude) {
        this.pixelPos = new PixelPos();
        this.geoPos = new GeoPos();

        final List<Section> sections = extractSections(latitude);

        final int gridWidth = latitude.getGridWidth();
        final float[] lonTpRaster = longitude.getTiePoints();
        final float[] latTpRaster = latitude.getTiePoints();

        int yOffset = 0;
        for (final Section section : sections) {
            final int sectionHeight = section.stopLine - section.startLine + 1;
            final int sectionSize = gridWidth * sectionHeight;
            final float[] lonBuffer = new float[sectionSize];
            final float[] latBuffer = new float[sectionSize];

            System.arraycopy(lonTpRaster, yOffset * gridWidth, lonBuffer, 0, sectionSize);
            System.arraycopy(latTpRaster, yOffset * gridWidth, latBuffer, 0, sectionSize);
            yOffset = section.stopLine;

            final TiePointGrid lonTp = new TiePointGrid("longitude",
                    gridWidth,
                    sectionHeight,
                    0.5,
                    0.5,
                    40,
                    40,
                    lonBuffer,
                    TiePointGrid.DISCONT_AT_180);

            final TiePointGrid latTp = new TiePointGrid("latitude",
                    gridWidth,
                    sectionHeight,
                    0.5,
                    0.5,
                    40,
                    40,
                    latBuffer,
                    TiePointGrid.DISCONT_NONE);

            geoCoding = new TiePointGeoCoding(latTp, lonTp);
            break;
        }
    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D point) {
        pixelPos.setLocation(x, y);
        final GeoPos geoCodingGeoPos = geoCoding.getGeoPos(pixelPos, geoPos);

        if (point == null) {
            point = new Point2D.Double();
        }

        point.setLocation(geoCodingGeoPos.getLon(), geoCodingGeoPos.getLat());
        return point;
    }

    @Override
    public Point2D[] getPixelLocation(double lon, double lat) {
        geoPos.setLocation(lat, lon);
        final PixelPos newPos = geoCoding.getPixelPos(geoPos, pixelPos);
        if (Double.isNaN(newPos.getX()) || Double.isNaN(newPos.getY())) {
            return new Point2D[0];
        }

        final Point2D.Double pxPos = new Point2D.Double(newPos.getX(), newPos.getY());
        return new Point2D[]{pxPos};
    }

    static List<Section> extractSections(TiePointGrid latitude) {
        final ArrayList<Section> sections = new ArrayList<>();

        final int gridHeight = latitude.getGridHeight();
        final int gridWidth = latitude.getGridWidth();

        final float[] latitudeTiePoints = latitude.getTiePoints();
        final float[] buffer = new float[gridWidth];
        System.arraycopy(latitudeTiePoints, 0, buffer, 0, gridWidth);

        boolean polarLine = isPolarLine(buffer);

        Section section = new Section();
        section.startLine = 0;

        for (int y = 1; y < gridHeight; y++) {
            System.arraycopy(latitudeTiePoints, y * gridWidth, buffer, 0, gridWidth);
            final boolean lineStatus = isPolarLine(buffer);
            if (lineStatus != polarLine) {
                section.stopLine = y;
                sections.add(section);

                section = new Section();
                section.startLine = y;
                polarLine = lineStatus;
            }
        }

        section.stopLine = gridHeight - 1;
        sections.add(section);

        return sections;
    }

    static boolean isPolarLine(float[] buffer) {
        boolean isPolar = false;
        for (float latValue : buffer) {
            if (latValue > THRESH) {
                isPolar = true;
                break;
            } else if (latValue < -THRESH) {
                isPolar = true;
                break;
            }
        }

        return isPolar;
    }

    static class Section {
        int startLine;
        int stopLine;
    }
}

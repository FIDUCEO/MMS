package com.bc.fiduceo.reader.avhrr_frac;

import com.bc.fiduceo.location.PixelLocator;
import org.esa.snap.core.datamodel.TiePointGrid;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class AVHRR_FRAC_PixelLocator implements PixelLocator {

    private static final double THRESH = 89.0;

    AVHRR_FRAC_PixelLocator(TiePointGrid longitude, TiePointGrid latitude) {
        extractSections(latitude);
    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D g) {
        return null;
    }

    @Override
    public Point2D[] getPixelLocation(double lon, double lat) {
        return new Point2D[0];
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

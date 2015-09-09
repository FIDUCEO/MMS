package com.bc.fiduceo.reader;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;

class LonShifter implements CoordinateFilter {

    private final double displacement;

    public LonShifter(double displacement) {
        this.displacement = displacement;
    }

    @Override
    public void filter(Coordinate coordinate) {
        coordinate.x += displacement;
    }
}


package com.bc.fiduceo.geometry;

import com.bc.fiduceo.math.TimeInterval;

import java.util.Date;

public interface TimeAxis {

    TimeInterval getIntersectionTime(Polygon polygon);

    TimeInterval getProjectionTime(LineString polygonSide);

    Date getTime(Point coordinate);
}

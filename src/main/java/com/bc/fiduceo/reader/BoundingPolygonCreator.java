package com.bc.fiduceo.reader;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import ucar.ma2.ArrayDouble;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @muhammad.bc on 8/12/2015.
 */
public class BoundingPolygonCreator {

    private int intervalX = 1;
    private int intervalY = 1;


    public BoundingPolygonCreator(int intervalX, int intervalY) {
        this.intervalX = intervalX;
        this.intervalY = intervalY;
    }


    public List<ArrayDouble.D2> createCoordinate(NetcdfFile netcdfFile) throws IOException {

        List<Group> groups = netcdfFile.getRootGroup().getGroups().get(0).getGroups();
        List<ArrayDouble.D2> d2List = new ArrayList<>(2);
        ArrayDouble.D2 d2Coordinate = null;
        for (Group group : groups) {
            if (group.getShortName().equals("Geolocation_Fields")) {
                List<Variable> variables = group.getVariables();
                for (Variable variable : variables) {
                    if (variable.getShortName().startsWith("Latitude")) {
                        d2Coordinate = (ArrayDouble.D2) variable.read();
                        if (d2Coordinate == null) {
                            throw new NullPointerException("The array is empty !!!");
                        }
                        d2List.add(d2Coordinate);
                    }

                    if (variable.getShortName().startsWith("Longitude")) {
                        d2Coordinate = (ArrayDouble.D2) variable.read();
                        if (d2Coordinate == null) {
                            throw new NullPointerException("The array is empty !!!");
                        }
                        d2List.add(d2Coordinate);
                    }
                }
            }
        }

        return d2List;
    }

    public Geometry createPolygon(ArrayDouble.D2 arrayLatitude, ArrayDouble.D2 arrayLongitude)
            throws com.vividsolutions.jts.io.ParseException {

        int geoXTrack = arrayLatitude.getShape()[1]-1;
        int geoTrack = arrayLatitude.getShape()[0]-1;

        List<Coordinate> coordinates = new ArrayList<>();
        for (int x = 1; x < geoXTrack; x += intervalX) {
            coordinates.add(new Coordinate(arrayLongitude.get(0, x), arrayLatitude.get(0, x)));
        }
        for (int y = 0; y <= geoTrack; y += intervalY) {
            coordinates.add(new Coordinate(arrayLongitude.get(y, geoXTrack), arrayLatitude.get(y, geoXTrack)));
        }
        for (int x = geoXTrack - 1; x > 0; x -= intervalX) {
            coordinates.add(new Coordinate(arrayLongitude.get(geoTrack, x), arrayLatitude.get(geoTrack, x)));
        }
        for (int y = geoTrack; y >= 0; y -= intervalY) {
            coordinates.add(new Coordinate(arrayLongitude.get(y, 0), arrayLatitude.get(y, 0)));
        }
        coordinates.add(coordinates.get(0));
        return new GeometryFactory().createPolygon(coordinates.toArray(new Coordinate[coordinates.size()]));
    }

}

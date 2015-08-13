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
public abstract class AbstractCreatePolygon {

    private int intervalX = 1;
    private int intervalY = 1;

    public AbstractCreatePolygon(int intervalX, int intervalY) {
        this.intervalX = intervalX;
        this.intervalY = intervalY;
    }



    public Geometry getCreatePolygon(NetcdfFile netcdfFile) throws IOException, com.vividsolutions.jts.io.ParseException {
        List<Group> groups = netcdfFile.getRootGroup().getGroups().get(0).getGroups();
        int geoXTrack = netcdfFile.findDimension("L1B_AMSU_GeoXTrack").getLength() - 1;
        int geoTrack = netcdfFile.findDimension("L1B_AMSU_GeoTrack").getLength() - 1;

        ArrayDouble.D2 arrayLatitude = null;
        ArrayDouble.D2 arrayLongitude = null;

        for (Group group : groups) {
            if (group.getShortName().equals("Geolocation_Fields")) {
                List<Variable> variables = group.getVariables();
                for (Variable variable : variables) {
                    if (variable.getShortName().startsWith("Latitude")) {
                        arrayLatitude = (ArrayDouble.D2) variable.read();
                    }


                    if (variable.getShortName().startsWith("Longitude")) {
                        arrayLongitude = (ArrayDouble.D2) variable.read();
                    }
                }
            }
        }

        assert arrayLongitude != null;
        assert arrayLatitude != null;
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

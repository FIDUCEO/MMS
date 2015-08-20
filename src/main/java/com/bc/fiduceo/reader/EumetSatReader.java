package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.SatelliteObservation;
import com.vividsolutions.jts.geom.Geometry;
import org.esa.snap.framework.datamodel.ProductData;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
 * @muhammad.bc on 8/17/2015.
 */
public class EumetSatReader implements Reader {
    private NetcdfFile netcdfFile;
    private BoundingPolygonCreator boundingPolygonCreator;

    public EumetSatReader(int intervalX, int intervalY) {
        boundingPolygonCreator = new BoundingPolygonCreator(intervalX, intervalY);
    }

    @Override
    public void open(File file) {
        try {
            netcdfFile = NetcdfFile.open(file.getPath());
        } catch (IOException e) {
            System.out.println("The NetCDF did not open the product,Please check the product path.");
        }
    }

    @Override
    public void close() throws IOException {
        try {
            netcdfFile.close();
        } catch (IOException e) {
            throw new IOException("The product is not close.");
        }
    }

    @Override
    public SatelliteObservation read() {
        final SatelliteObservation satelliteObservation = new SatelliteObservation();

        satelliteObservation.setStartTime(getParseDate("time_converage_start"));
        satelliteObservation.setStopTime(getParseDate("time_converage_end"));

        final Geometry polygonForEumetSat = boundingPolygonCreator.createPolygonForEumetSat(netcdfFile);
        satelliteObservation.setGeoBounds(polygonForEumetSat);
        return satelliteObservation;
    }

    Date getParseDate(String timeCoverage) {
        try {
            return ProductData.UTC.parse(netcdfFile.findGlobalAttribute(timeCoverage).getStringValue(), "yyyy-MM-dd'T'HH:mm:ss").getAsDate();
        } catch (ParseException e) {
            System.out.println("The string is not parse with the PATTERN yyyy-MM-dd'T'HH:mm:ss");
        }
        return null;
    }
}

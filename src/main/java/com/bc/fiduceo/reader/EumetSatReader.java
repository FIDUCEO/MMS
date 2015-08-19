package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.SatelliteObservation;
import org.esa.snap.framework.datamodel.ProductData;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
 * @muhammad.bc on 8/17/2015.
 */
public class EumetSatReader extends BoundingPolygonCreator implements Reader {
    private NetcdfFile netcdfFile;

    public EumetSatReader(int intervalX, int intervalY) {
        super(intervalX, intervalY);
    }

    @Override
    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
    }

    @Override
    public void close() throws IOException {
        netcdfFile.close();
    }

    @Override
    public SatelliteObservation read() throws IOException, ParseException, com.vividsolutions.jts.io.ParseException {
        SatelliteObservation satelliteObservation = new SatelliteObservation();

        satelliteObservation.setStartTime(getParseDate("time_converage_start"));
        satelliteObservation.setStopTime(getParseDate("time_converage_end"));

        satelliteObservation.setGeoBounds(createPolygonForEumetSat(netcdfFile));
        return satelliteObservation;
    }

    Date getParseDate(String timeCoverage) throws ParseException {
        return ProductData.UTC.parse(netcdfFile.findGlobalAttribute(timeCoverage).getStringValue(), "yyyy-MM-dd'T'HH:mm:ss").getAsDate();
    }
}

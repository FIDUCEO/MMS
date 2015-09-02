package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.SatelliteObservation;
import com.vividsolutions.jts.geom.Geometry;
import org.esa.snap.framework.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;


public class EumetsatIASIReader implements Reader {

    private static final int GEO_INTERVAL_X = 6;
    private static final int GEO_INTERVAL_Y = 6;

    private NetcdfFile netcdfFile;
    private BoundingPolygonCreator boundingPolygonCreator;

    public EumetsatIASIReader() {
        boundingPolygonCreator = new BoundingPolygonCreator(GEO_INTERVAL_X, GEO_INTERVAL_Y);
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
    public SatelliteObservation read() throws IOException {
        final SatelliteObservation satelliteObservation = new SatelliteObservation();

        satelliteObservation.setStartTime(getGlobalAttributeAsDate("time_converage_start", netcdfFile));
        satelliteObservation.setStopTime(getGlobalAttributeAsDate("time_converage_end", netcdfFile));

        final Variable latVariable = netcdfFile.findVariable("lat");
        final Variable lonVariable = netcdfFile.findVariable("lon");
        final Array latArray = latVariable.read();
        final Array lonArray = lonVariable.read();

        final Geometry polygon = boundingPolygonCreator.createIASIBoundingPolygon((ArrayFloat.D2) latArray, (ArrayFloat.D2) lonArray);
        satelliteObservation.setGeoBounds(polygon);
        return satelliteObservation;
    }

    static Date getGlobalAttributeAsDate(String timeCoverage, NetcdfFile netcdfFile) throws IOException {
        try {
            final Attribute globalAttribute = netcdfFile.findGlobalAttribute(timeCoverage);
            if (globalAttribute == null) {
                throw new IOException("Requested attribute '" + timeCoverage + "' not found");
            }

            final String attributeValue = globalAttribute.getStringValue();
            return ProductData.UTC.parse(attributeValue, "yyyy-MM-dd'T'HH:mm:ss").getAsDate();
        } catch (ParseException e) {
            throw new IOException(e.getMessage());
        }
    }
}

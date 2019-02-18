package com.bc.fiduceo.reader.fiduceo_fcdr;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import com.bc.fiduceo.reader.Geometries;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

abstract class FCDR_Reader extends NetCDFReader {

    private static final int NUM_SPLITS = 2;

    private BoundingPolygonCreator boundingPolygonCreator;
    
    protected final GeometryFactory geometryFactory;
    protected File file;

    FCDR_Reader(ReaderContext readerContext) {
        this.geometryFactory = readerContext.getGeometryFactory();
    }

    @Override
    public void open(File file) throws IOException {
        super.open(file);
        this.file = file;
    }

    @Override
    public void close() throws IOException {
        boundingPolygonCreator = null;
        file = null;
        
        super.close();
    }

    Geometries calculateGeometries(boolean clockwise, Interval interval) throws IOException {
        final BoundingPolygonCreator boundingPolygonCreator = getBoundingPolygonCreator(interval);
        final Geometries geometries = new Geometries();

        final Array longitudes = arrayCache.getScaled("longitude", "scale_factor", "add_offset");
        final Array latitudes = arrayCache.getScaled("latitude", "scale_factor", "add_offset");
        Geometry timeAxisGeometry;
        Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(longitudes, latitudes);
        if (!boundingGeometry.isValid()) {
            boundingGeometry = boundingPolygonCreator.createBoundingGeometrySplitted(longitudes, latitudes, NUM_SPLITS, clockwise);
            if (!boundingGeometry.isValid()) {
                throw new RuntimeException("Invalid bounding geometry detected");
            }
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometrySplitted(longitudes, latitudes, NUM_SPLITS);
        } else {
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(longitudes, latitudes);
        }

        geometries.setBoundingGeometry(boundingGeometry);
        geometries.setTimeAxesGeometry(timeAxisGeometry);
        return geometries;
    }

    BoundingPolygonCreator getBoundingPolygonCreator(Interval interval) {
        if (boundingPolygonCreator == null) {
            boundingPolygonCreator = new BoundingPolygonCreator(interval, geometryFactory);
        }

        return boundingPolygonCreator;
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final Date date = FCDRUtils.parseStartDate(fileName);
        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.setTime(date);
        final int[] ymd = new int[3];
        ymd[0] = utcCalendar.get(Calendar.YEAR);
        ymd[1] = utcCalendar.get(Calendar.MONTH) + 1;
        ymd[2] = utcCalendar.get(Calendar.DAY_OF_MONTH);
        return ymd;
    }
}

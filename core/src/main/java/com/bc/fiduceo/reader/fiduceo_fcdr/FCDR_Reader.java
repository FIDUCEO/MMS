package com.bc.fiduceo.reader.fiduceo_fcdr;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.location.PixelLocatorFactory;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import com.bc.fiduceo.reader.Geometries;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.*;

abstract class FCDR_Reader extends NetCDFReader {

    private static final int NUM_SPLITS = 2;

    private static final String LONGITUDE_VAR_NAME = "longitude";
    private static final String LATITUDE_VAR_NAME = "latitude";
    protected final GeometryFactory geometryFactory;
    protected File file;
    protected PixelLocator pixelLocator;
    private BoundingPolygonCreator boundingPolygonCreator;

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
        pixelLocator = null;
        file = null;

        super.close();
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

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final ArrayDouble lonStorage = (ArrayDouble) arrayCache.getScaled(LONGITUDE_VAR_NAME, CF_SCALE_FACTOR_NAME, CF_OFFSET_NAME);
            final ArrayDouble latStorage = (ArrayDouble) arrayCache.getScaled(LATITUDE_VAR_NAME, CF_SCALE_FACTOR_NAME, CF_OFFSET_NAME);
            final int[] shape = lonStorage.getShape();
            final int width = shape[1];
            final int height = shape[0];
            pixelLocator = PixelLocatorFactory.getSwathPixelLocator(lonStorage, latStorage, width, height);
        }
        return pixelLocator;
    }

    @Override
    public String getLongitudeVariableName() {
        return LONGITUDE_VAR_NAME;
    }

    @Override
    public String getLatitudeVariableName() {
        return LATITUDE_VAR_NAME;
    }

    Geometries calculateGeometries(boolean clockwise, BoundingPolygonCreator boundingPolygonCreator) throws IOException {
        final Array longitudes = arrayCache.getScaled(LONGITUDE_VAR_NAME, CF_SCALE_FACTOR_NAME, CF_OFFSET_NAME);
        final Array latitudes = arrayCache.getScaled(LATITUDE_VAR_NAME, CF_SCALE_FACTOR_NAME, CF_OFFSET_NAME);

        final double fillValue = arrayCache.getNumberAttributeValue(CF_FILL_VALUE_NAME, LONGITUDE_VAR_NAME).doubleValue();

        final Geometries geometries;
        final Interval[] intervals = boundingPolygonCreator.extractValidIntervals(longitudes, fillValue);
        try {
            if (1 == intervals.length) {
                geometries = calculateGeometriesSmooth(clockwise, boundingPolygonCreator, longitudes, latitudes);
            } else {
                geometries = calculateGeometriesGapped(clockwise, boundingPolygonCreator, longitudes, latitudes, intervals);
            }
            geometries.setIntervals(intervals);
            return geometries;
        } catch (InvalidRangeException e) {
            throw new IOException(e.getMessage());
        }
    }

    private Geometries calculateGeometriesSmooth(boolean clockwise, BoundingPolygonCreator boundingPolygonCreator, Array longitudes, Array latitudes) {
        final Geometries geometries = new Geometries();
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

    private Geometries calculateGeometriesGapped(boolean clockwise, BoundingPolygonCreator boundingPolygonCreator, Array longitudes, Array latitudes, Interval[] intervals) throws InvalidRangeException {
        final Geometries geometries = new Geometries();
        final List<Polygon> geometryList = new ArrayList<>();
        final List<LineString> timeAxesList = new ArrayList<>();

        final int[] shape = longitudes.getShape();

        final int[] offset = new int[2];
        final int[] subsetShape = new int[2];
        subsetShape[1] = shape[1];
        for (final Interval interval : intervals) {
            offset[0] = interval.getX();
            subsetShape[0] = interval.getY() - interval.getX() + 1;

            final Array lonSection = longitudes.section(offset, subsetShape);
            final Array latSection = latitudes.section(offset, subsetShape);

            final Polygon boundingGeometry;
            if (clockwise) {
                boundingGeometry = boundingPolygonCreator.createBoundingGeometryClockwise(lonSection, latSection);
            } else {
                boundingGeometry = boundingPolygonCreator.createBoundingGeometry(lonSection, latSection);
            }

            geometryList.add(boundingGeometry);
            final LineString timeAxis = boundingPolygonCreator.createTimeAxisGeometry(lonSection, latSection);
            timeAxesList.add(timeAxis);
        }

        final MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(geometryList);
        geometries.setBoundingGeometry(multiPolygon);
        final GeometryCollection timeAxes = geometryFactory.createGeometryCollection(timeAxesList.toArray(new Geometry[]{}));
        geometries.setTimeAxesGeometry(timeAxes);

        return geometries;
    }

    PixelLocator getSubScenePixelLocator(Polygon sceneGeometry, Interval stepInterval) throws IOException {
        final Array longitudes = arrayCache.get(LONGITUDE_VAR_NAME);
        final int[] shape = longitudes.getShape();
        final int height = shape[0];
        final int width = shape[1];
        final int subsetHeight = getBoundingPolygonCreator(stepInterval).getSubsetHeight(height, NUM_SPLITS);
        final PixelLocator pixelLocator = getPixelLocator();

        return PixelLocatorFactory.getSubScenePixelLocator(sceneGeometry, width, height, subsetHeight, pixelLocator);
    }

    protected ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval, String timeVariableName) throws IOException, InvalidRangeException {
        final Array rawTimeArray = readRaw(x, y, interval, timeVariableName);

        final Number fillValue = getFillValue(timeVariableName);
        final int[] shape = rawTimeArray.getShape();
        int height = shape[0];
        int width = shape[1];
        final ArrayInt.D2 integerTimeArray = new ArrayInt.D2(height, width);
        final int targetFillValue = (int) NetCDFUtils.getDefaultFillValue(DataType.INT, false);
        final Index index = rawTimeArray.getIndex();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                index.set(i, j);
                final double rawTime = rawTimeArray.getDouble(index);
                if (!fillValue.equals(rawTime)) {
                    integerTimeArray.set(i, j, (int) Math.round(rawTime));
                } else {
                    integerTimeArray.set(i, j, targetFillValue);
                }
            }
        }

        return integerTimeArray;
    }

    BoundingPolygonCreator getBoundingPolygonCreator(Interval interval) {
        if (boundingPolygonCreator == null) {
            boundingPolygonCreator = new BoundingPolygonCreator(interval, geometryFactory);
        }

        return boundingPolygonCreator;
    }
}

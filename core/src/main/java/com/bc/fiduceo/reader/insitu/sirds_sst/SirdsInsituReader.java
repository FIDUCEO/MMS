package com.bc.fiduceo.reader.insitu.sirds_sst;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class SirdsInsituReader extends NetCDFReader {

    private static final String REGEX = "SSTCCI2_refdata_[a-z]+(_[a-z]+)?_\\d{6}.nc";
    private int[] timeMinMax;

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo info = new AcquisitionInfo();

        ensureSensingTimesAvailable();

        info.setSensingStart(TimeUtils.create(timeMinMax[0] * 1000L));
        info.setSensingStop(TimeUtils.create(timeMinMax[1] * 1000L));

        info.setNodeType(NodeType.UNDEFINED);
        return info;
    }

    private void ensureSensingTimesAvailable() throws IOException {
        if (timeMinMax == null) {
            final Calendar calendar = TimeUtils.getUTCCalendar();
            final Array year = arrayCache.get("YEAR");
            final Array month = arrayCache.get("MONTH");
            final Array day = arrayCache.get("DAY");
            final Array hour = arrayCache.get("HOUR");
            final Array minute = arrayCache.get("MINUTE");
            final Array second = arrayCache.get("SECOND");
            final int numMeasures = (int) year.getSize();
            final int[] timeStamps = new int[numMeasures];
            for (int i = 0; i < numMeasures; i++) {
                final short yearVal = year.getShort(i);
                final byte monthVal = (byte) (month.getByte(i) - 1);
                final byte dayVal = day.getByte(i);
                final byte hourVal = hour.getByte(i);
                final byte minuteVal = minute.getByte(i);
                final byte secondVal = second.getByte(i);

                //noinspection MagicConstant
                calendar.set(yearVal, monthVal, dayVal, hourVal, minuteVal, secondVal);
                timeStamps[i] = (int) (calendar.getTime().getTime() / 1000);
            }

            timeMinMax = extractMinMax(timeStamps);
        }
    }

    @Override
    public String getRegEx() {
        return REGEX;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Dimension getProductSize() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getLongitudeVariableName() {
        return "LONGITUDE";
    }

    @Override
    public String getLatitudeVariableName() {
        return "LATITUDE";
    }

    // package access for testing only tb 2021-07-21
    static int[] extractMinMax(int[] timeStamps) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int value : timeStamps) {
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
        }

        return new int[]{min, max};
    }

    @Override
    public void close() throws IOException {
        timeMinMax = null;
        super.close();
    }
}

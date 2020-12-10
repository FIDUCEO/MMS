package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

class VariableUtils {

    // package access for testing purpose only tb 2020-12-02
    static void addAttributes(TemplateVariable template, Variable variable) {
        variable.addAttribute(new Attribute("units", template.getUnits()));
        variable.addAttribute(new Attribute("long_name", template.getLongName()));
        final String standardName = template.getStandardName();
        if (StringUtils.isNotNullAndNotEmpty(standardName)) {
            variable.addAttribute(new Attribute("standard_name", standardName));
        }
        variable.addAttribute(new Attribute("_FillValue", template.getFillValue()));
    }

    static Array readTimeArray(String timeVariableName, NetcdfFile reader) throws IOException, InvalidRangeException {
        final Variable timeVariable = NetCDFUtils.getVariable(reader, timeVariableName);

        final Array timeArray;
        final int rank = timeVariable.getRank();

        // @todo 2 tb/tb this block might be of general interest, extract and test 2020-11-17
        if (rank == 1) {
            timeArray = timeVariable.read();
        } else if (rank == 2) {
            final int[] shape = timeVariable.getShape();
            final int shapeOffset = shape[1] / 2;
            final int[] offset = {0, shapeOffset};
            timeArray = timeVariable.read(offset, new int[]{shape[0], 1});
        } else if (rank == 3) {
            final int[] shape = timeVariable.getShape();
            final int yOffset = shape[1] / 2;
            final int xOffset = shape[2] / 2;
            final int[] offset = {0, yOffset, xOffset};
            timeArray = timeVariable.read(offset, new int[]{shape[0], 1, 1});
        } else {
            throw new IllegalArgumentException("Rank of time-variable not supported");
        }
        return timeArray.reduce();
    }

    static Array convertToEra5TimeStamp(Array timeArray) {
        final Array era5TimeArray = Array.factory(timeArray.getDataType(), timeArray.getShape());
        final IndexIterator era5Iterator = era5TimeArray.getIndexIterator();
        final IndexIterator indexIterator = timeArray.getIndexIterator();
        while (indexIterator.hasNext() && era5Iterator.hasNext()) {
            final int satelliteTime = indexIterator.getIntNext();
            final int era5Time = toEra5TimeStamp(satelliteTime);
            era5Iterator.setIntNext(era5Time);
        }
        return era5TimeArray;
    }

    static int toEra5TimeStamp(int utc1970Seconds) {
        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.setTime(new Date(utc1970Seconds * 1000L));

        final int minutes = utcCalendar.get(Calendar.MINUTE);
        if (minutes >= 30) {
            utcCalendar.add(Calendar.HOUR_OF_DAY, 1);
        }
        utcCalendar.set(Calendar.MINUTE, 0);
        utcCalendar.set(Calendar.SECOND, 0);
        utcCalendar.set(Calendar.MILLISECOND, 0);

        return (int) (utcCalendar.getTimeInMillis() / 1000L);
    }

    static int[] getNwpShape(com.bc.fiduceo.core.Dimension dimension, int[] shape) {
        int xExtract = dimension.getNx();
        int yExtract = dimension.getNy();
        if (yExtract >= shape[1]) {
            yExtract = shape[1];
        }
        if (xExtract >= shape[2]) {
            xExtract = shape[2];
        }
        return new int[]{shape[0], yExtract, xExtract};
    }

    static int[] getNwpOffset(int[] shape, int[] nwpShape) {
        final int yOffset = shape[1] / 2 - nwpShape[1] / 2;
        final int xOffset = shape[2] / 2 - nwpShape[2] / 2;
        return new int[]{0, yOffset, xOffset};
    }

    static Array readGeolocationVariable(com.bc.fiduceo.core.Dimension dimension, NetcdfFile reader, String lonVarName) throws IOException, InvalidRangeException {
        final Variable geoVariable = NetCDFUtils.getVariable(reader, lonVarName);

        final int[] shape = geoVariable.getShape();

        final int[] nwpShape = getNwpShape(dimension, shape);
        final int[] offset = getNwpOffset(shape, nwpShape);

        Array rawData = geoVariable.read(offset, nwpShape);

        final double scaleFactor = NetCDFUtils.getScaleFactor(geoVariable);
        final double addOffset = NetCDFUtils.getOffset(geoVariable);
        if (ReaderUtils.mustScale(scaleFactor, addOffset)) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, addOffset);
            rawData = MAMath.convert2Unpacked(rawData, scaleOffset);
        }
        return rawData.reduce();
    }
}

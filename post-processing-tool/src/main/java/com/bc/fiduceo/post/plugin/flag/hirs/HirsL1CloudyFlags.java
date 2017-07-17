/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.bc.fiduceo.post.plugin.flag.hirs;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.post.Constants;
import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingContext;
import com.bc.fiduceo.post.ReaderCache;
import com.bc.fiduceo.post.util.DistanceToLandMap;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_FLAG_MASKS_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_FLAG_MEANINGS_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.getCenterPosArrayFromMMDFile;
import static com.bc.fiduceo.util.NetCDFUtils.getFloatValueFromAttribute;
import static com.bc.fiduceo.util.NetCDFUtils.getVariable;

class HirsL1CloudyFlags extends PostProcessing {

    private static final float DELTA_1_LAND_OR_ICE_COVERED = 6.5f;
    private static final float DELTA_1_WATER = 3.5f;
    static final byte SPACE_CONTRAST_TEST_ALL_PIXELS_USABLE = 0x1;
    static final byte SPACE_CONTRAST_TEST_WARNING = 0x2;
    static final byte SPACE_CONTRAST_TEST_CLOUDY = 0x4;
    static final byte INTERCHANNEL_TEST_CLOUDY = 0x8;
    private static final int DOMAIN_LAND_OR_ICE_NOT_USEABLE = 1;
    private static final int DOMAIN_WATER_NOT_USEABLE = 20;

    private final static DataType FLAG_VAR_DATA_TYPE = DataType.BYTE;
    final String sensorName;
    final String sourceFileVarName;
    final String sourceXVarName;
    final String sourceYVarName;
    final String processingVersionVarName;
    final String sourceBt_11_1_um_VarName;

    final String flagVarName;
    final String latVarName;
    final String lonVarName;
    final String bt_11_1_um_VarName;
    final String bt_6_5_um_VarName;
    final DistanceToLandMap distanceToLandMap;

    private float fillValue_11_1;
    private float fillValue_6_5;
    private Variable varFlags;
    private Array data11_1;
    private Array data6_5;
    private Array flags;
    private ArrayChar sourcFileNames;
    private ArrayChar processingVersions;
    private Array lats;
    private Array lons;
    private int[] shape;
    private int[] xValues;
    private int[] yValues;

    HirsL1CloudyFlags(String sensorName, String sourceFileVarName,
                      String sourceXVarName, String sourceYVarName,
                      String processingVersionVarName, String sourceBt11_1umVarName,
                      String flagVarName,
                      String latVarName, String lonVarName,
                      String btVarName_11_1_um, String btVarName_6_5_um,
                      DistanceToLandMap distanceToLandMap) {
        this.sensorName = sensorName;
        this.sourceFileVarName = sourceFileVarName;
        this.sourceXVarName = sourceXVarName;
        this.sourceYVarName = sourceYVarName;
        this.processingVersionVarName = processingVersionVarName;
        sourceBt_11_1_um_VarName = sourceBt11_1umVarName;

        this.flagVarName = flagVarName;
        this.latVarName = latVarName;
        this.lonVarName = lonVarName;
        this.bt_11_1_um_VarName = btVarName_11_1_um;
        this.bt_6_5_um_VarName = btVarName_6_5_um;
        this.distanceToLandMap = distanceToLandMap;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Variable variable = getVariable(reader, bt_11_1_um_VarName);
        final String dimensions = variable.getDimensionsString();
        final Variable flagVar = writer.addVariable(null, flagVarName, FLAG_VAR_DATA_TYPE, dimensions);

        // add flag meanings and other Attributes to flagVar
        Array masks = new ArrayByte(new int[]{4});
        masks.setByte(0, SPACE_CONTRAST_TEST_ALL_PIXELS_USABLE);
        masks.setByte(1, SPACE_CONTRAST_TEST_WARNING);
        masks.setByte(2, SPACE_CONTRAST_TEST_CLOUDY);
        masks.setByte(3, INTERCHANNEL_TEST_CLOUDY);
        final String Separator = "\t";

        flagVar.addAttribute(new Attribute(CF_FLAG_MEANINGS_NAME, Arrays.asList("sc_all", "sc_warning", "sc_cloudy", "ic_cloudy")));
        flagVar.addAttribute(new Attribute(CF_FLAG_MASKS_NAME, masks));
        flagVar.addAttribute(new Attribute("flag_coding_name", "hirs_cloudy_flags"));
        flagVar.addAttribute(new Attribute("flag_descriptions", "space contrast test, all pixels are usable"
                + Separator +
                "space contrast test, warning, less than 99 percent are usable"
                + Separator +
                "space contrast test, cloudy"
                + Separator +
                "interchannel test, cloudy"));
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        initDataForComputing(reader, writer);

        final int[] levelShape = data11_1.getShape();
        levelShape[0] = 1;
        final DomainDataProvider landIceDDP = new DomainDataProvider(DOMAIN_LAND_OR_ICE_NOT_USEABLE, DELTA_1_LAND_OR_ICE_COVERED) {
            final int[] origin3D = {0, 0, 0};

            @Override
            public Array getDomainData_11_1(int z) throws InvalidRangeException {
                origin3D[0] = z;
                return data11_1.section(origin3D, levelShape);
            }
        };
        final Domain domainLandIce = new Domain(data11_1, data6_5, flags, fillValue_11_1, fillValue_6_5, landIceDDP);
        final DomainDataProvider waterDDP = new DomainDataProvider(DOMAIN_WATER_NOT_USEABLE, DELTA_1_WATER) {
            final int[] origin2D = {0, 0};
            private ReaderCache readerCache = new CloudRC(getContext());

            @Override
            Array getDomainData_11_1(int z) throws InvalidRangeException, IOException {
                origin2D[0] = z;
                final String fileName = sourcFileNames.getString(z);
                final String version = processingVersions.getString(z);
                Reader srcReader = readerCache.getFileOpened(fileName, sensorName, version);
                return srcReader.readScaled(xValues[z], yValues[z], new Interval(45, 45), sourceBt_11_1_um_VarName);
            }
        };

        final Domain domainWater = new Domain(data11_1, data6_5, flags, fillValue_11_1, fillValue_6_5, waterDDP);

        try {
            for (int z = 0; z < shape[0]; z++) {
                final boolean land = isLand(distanceToLandMap, lons.getDouble(z), lats.getDouble(z));
                final boolean iceCoveredWater = !land && isIceCoveredWater();
                final boolean water = !land && !iceCoveredWater;
                if (land || iceCoveredWater) {
                    domainLandIce.computeFlags(z);
                } else {
                    domainWater.computeFlags(z);
                }
            }
            writer.write(varFlags, flags);
        } finally {
            distanceToLandMap.close();
        }
    }

    static MaximumAndFlags getMaximumAndFlags(Array domainData, float fillValue, int maxNumInvalidPixels) {
        final IndexIterator iterator = domainData.getIndexIterator();
        int invalidCount = 0;
        float max = fillValue;
        while (iterator.hasNext()) {
            final float v = iterator.getFloatNext();
            if (v == fillValue) {
                invalidCount++;
            } else {
                max = Math.max(max, v);
            }
        }
        byte flags = 0;
        if (invalidCount == 0) { // all pixels are valid
            flags = SPACE_CONTRAST_TEST_ALL_PIXELS_USABLE;
        } else if (invalidCount > maxNumInvalidPixels) { // warning flag
            flags = SPACE_CONTRAST_TEST_WARNING;
        }
        return new MaximumAndFlags(max, flags);
    }

    static boolean isLand(DistanceToLandMap distanceToLandMap, double lon, double lat) throws IOException {
        final double distanceToLand = distanceToLandMap.getDistance(lon, lat);
        return distanceToLand < 0.3;
    }

    static boolean isIceCoveredWater() {
        //@todo 1 se/** - the "ice covered" check must be implemented. Ask UHH (Imke Hans or Martin Burgdorf)
        return false;
    }

    static byte getCloudy_SpaceContrastTest(double spaceContrastThreshold, final float value_11_1, float fillValue_11_1) {
        // A pixel is classified cloudy if the pixel is useable (!= fill value) and value < threshold
        // see chapter 2.2.2.1 - FIDUCEO Multi-sensor Match up System - Implementation Plan
        boolean usable = value_11_1 != fillValue_11_1;
        if (usable && spaceContrastThreshold > value_11_1) {
            return SPACE_CONTRAST_TEST_CLOUDY;
        }
        return 0;
    }

    static byte getCloudy_InterChannelTest(final float value_11_1, float fillValue_11_1,
                                           final float value_6_5, float fillValue_6_5) {
        // A pixel is classified cloudy if both values are useable (!= fill value)
        // and Math.abs(value_11_1 - value_6_5) < 25
        // see chapter 2.2.2.2 - FIDUCEO Multi-sensor Match up System - Implementation Plan
        boolean usable = value_11_1 != fillValue_11_1 && value_6_5 != fillValue_6_5;
        if (usable && Math.abs(value_11_1 - value_6_5) < 25) {
            return INTERCHANNEL_TEST_CLOUDY;
        }
        return 0;
    }

    private void initDataForComputing(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        Variable var11_1um = getVariable(writer, bt_11_1_um_VarName);
        fillValue_11_1 = getFloatValueFromAttribute(var11_1um, CF_FILL_VALUE_NAME, 0);

        Variable var6_5um = getVariable(writer, bt_6_5_um_VarName);
        fillValue_6_5 = getFloatValueFromAttribute(var6_5um, CF_FILL_VALUE_NAME, 0);

        varFlags = getVariable(writer, flagVarName);

        final Variable sourceFileVar = getVariable(writer, sourceFileVarName);
        sourcFileNames = (ArrayChar) sourceFileVar.read();

        final Variable versionVar = getVariable(writer, processingVersionVarName);
        processingVersions = (ArrayChar) versionVar.read();

        xValues = (int[]) getVariable(writer, sourceXVarName).read().getStorage();
        yValues = (int[]) getVariable(writer, sourceYVarName).read().getStorage();

        shape = var11_1um.getShape();

        data11_1 = var11_1um.read();
        data6_5 = var6_5um.read();
        flags = varFlags.read();

        lats = getCenterPosArrayFromMMDFile(reader, latVarName, null, null, Constants.MATCHUP_COUNT);
        lons = getCenterPosArrayFromMMDFile(reader, lonVarName, null, null, Constants.MATCHUP_COUNT);
    }

    static class MaximumAndFlags {

        final double maximum;
        final byte flags;

        MaximumAndFlags(double maximum, byte flags) {
            this.maximum = maximum;
            this.flags = flags;
        }
    }

    static class CloudRC extends ReaderCache {

        CloudRC(final PostProcessingContext context) {
            super(context);
        }

        @Override
        protected int[] extractYearMonthDayFromFilename(String fileName) {
            final String[] strings = fileName.split("\\.");
            final String datePart = strings[4].substring(1);
            final Date yyDDD = TimeUtils.parse(datePart, "yyDDD");
            final Calendar utcCalendar = TimeUtils.getUTCCalendar();
            utcCalendar.setTime(yyDDD);
            return new int[]{
                    utcCalendar.get(Calendar.YEAR),
                    utcCalendar.get(Calendar.MONTH) + 1,
                    utcCalendar.get(Calendar.DAY_OF_MONTH),
            };
        }
    }

    static class Domain {

        private final float fillValue_11_1;
        private final float fillValue_6_5;
        private final Array data11_1um;
        private final Array data6_5um;
        private final Array flags;
        private final int[] shape;
        private final Index index;
        private final DomainDataProvider ddp;

        Domain(Array data11_1, Array data6_5, Array flags, float fillValue_11_1, float fillValue_6_5, DomainDataProvider ddp) {
            this.data11_1um = data11_1;
            this.data6_5um = data6_5;
            this.flags = flags;
            this.fillValue_11_1 = fillValue_11_1;
            this.fillValue_6_5 = fillValue_6_5;
            shape = flags.getShape();
            index = flags.getIndex();
            this.ddp = ddp;
        }

        void computeFlags(int z) throws InvalidRangeException, IOException {
            Array domainData_11_1 = ddp.getDomainData_11_1(z);
            final MaximumAndFlags mf = getMaximumAndFlags(domainData_11_1, fillValue_11_1, ddp.maxNumUnuseable);
            final double spaceContrastThreshold = mf.maximum - ddp.delta1;
            index.set0(z);
            for (int y = 0; y < shape[1]; y++) {
                index.set1(y);
                for (int x = 0; x < shape[2]; x++) {
                    index.set2(x);
                    byte flagsByte = mf.flags;
                    flagsByte += getCloudy_SpaceContrastTest(spaceContrastThreshold, data11_1um.getFloat(index), fillValue_11_1);
                    flagsByte += getCloudy_InterChannelTest(data11_1um.getFloat(index), fillValue_11_1, data6_5um.getFloat(index), fillValue_6_5);
                    flags.setByte(index, flagsByte);
                }
            }
        }
    }

    static abstract class DomainDataProvider {

        final int maxNumUnuseable;
        final float delta1;

        DomainDataProvider(int maxNumUnuseable, float delta1) {
            this.maxNumUnuseable = maxNumUnuseable;
            this.delta1 = delta1;
        }

        abstract Array getDomainData_11_1(int z) throws InvalidRangeException, IOException;
    }
}

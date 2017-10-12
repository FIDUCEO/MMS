/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */
package com.bc.fiduceo.reader.caliop;

import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaliopUtils {

    public int[] extractYearMonthDayFromFilename(String fileName) {
        final Pattern compile = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        final Matcher matcher = compile.matcher(fileName);
        //noinspection ResultOfMethodCallIgnored
        matcher.find();
        final String[] split = matcher.group().split("-");
        final int[] ymd = new int[3];
        for (int i = 0; i < ymd.length; i++) {
            ymd[i] = Integer.parseInt(split[i]);
        }
        return ymd;
    }

    public Number getFillValue(Variable ncVariable) {
        final Attribute fillvalueAttr = ncVariable.findAttribute("fillvalue");
        if (fillvalueAttr == null) {
            final DataType dataType = ncVariable.getDataType();
            final Attribute unsignedAttr = ncVariable.findAttribute(NetCDFUtils.CF_UNSIGNED);
            final boolean unsigned = unsignedAttr != null && Boolean.parseBoolean(unsignedAttr.getStringValue());
            return NetCDFUtils.getDefaultFillValue(dataType, unsigned);
        } else {
            return fillvalueAttr.getNumericValue();
        }
    }

    public Date getDate(Array charArray) throws ParseException {
        final String pattern = "yyyy-MM-dd'T'HH:mm:ss";
        final String str = charArray.toString().trim();
        final String analysableUtcStr = stripTrailingZ(str);
        final ProductData.UTC utc = ProductData.UTC.parse(analysableUtcStr, pattern);
        return utc.getAsDate();
    }

    private static String stripTrailingZ(String str) {
        if (str.endsWith("Z")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }
}

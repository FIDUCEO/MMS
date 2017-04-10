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

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.Arrays;

class HirsL1CloudyFlags extends PostProcessing {

    public final static byte SPACE_CONTRAST_TEST_ALL_PIXELS_USABLE = 1;
    public final static byte SPACE_CONTRAST_TEST_WARNING = 2;
    public final static byte SPACE_CONTRAST_TEST_CLOUDY = 4;
    public final static byte INTERCHANNEL_TEST_CLOUDY = 8;

    private final static DataType FLAG_VAR_DATA_TYPE = DataType.BYTE;

    final String flagVarName;
    final String btVarName_11_1_µm;
    final String btVarName_6_5_µm;

    public HirsL1CloudyFlags(final String btVarName_11_1_µm, final String btVarName_6_5_µm, final String flagVarName) {
        this.flagVarName = flagVarName;
        this.btVarName_11_1_µm = btVarName_11_1_µm;
        this.btVarName_6_5_µm = btVarName_6_5_µm;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Variable variable = NetCDFUtils.getVariable(reader, btVarName_11_1_µm);
        final String dimensions = variable.getDimensionsString();
        final Variable flagVar = writer.addVariable(null, flagVarName, FLAG_VAR_DATA_TYPE, dimensions);
        // todo add flag meanings and other Attributes to flagVar
        flagVar.addAttribute(new Attribute("flag_meanings", Arrays.asList("sc_all",
                                                                          "sc_warning",
                                                                          "sc_cloudy",
                                                                          "ic_cloudy")));
        Array masks = new ArrayByte(new int[]{4});
        masks.setByte(0, SPACE_CONTRAST_TEST_ALL_PIXELS_USABLE);
        masks.setByte(1, SPACE_CONTRAST_TEST_WARNING);
        masks.setByte(2, SPACE_CONTRAST_TEST_CLOUDY);
        masks.setByte(3, INTERCHANNEL_TEST_CLOUDY);
        flagVar.addAttribute(new Attribute("flag_masks", masks));
        flagVar.addAttribute(new Attribute("flag_coding_name", "hirs_cloudy_flags"));
        final String Separator = "\t";
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
        throw new RuntimeException("not implemented");
    }
}

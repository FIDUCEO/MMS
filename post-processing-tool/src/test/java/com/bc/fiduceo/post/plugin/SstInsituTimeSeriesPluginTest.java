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
 */

package com.bc.fiduceo.post.plugin;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import org.jdom.Element;
import org.junit.*;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;

/**
 * Created by Sabine on 08.12.2016.
 */
public class SstInsituTimeSeriesPluginTest {

    private SstInsituTimeSeriesPlugin plugin;

    public static void main(String[] args) throws IOException, InvalidRangeException {

        rename_a_variable_and_add_a_new_one_with_the_old_name_and_time_series_dimension();

        check_if_the_values_are_equal();
    }

    @Before
    public void setUp() throws Exception {
        plugin = new SstInsituTimeSeriesPlugin();
    }

    @Test
    public void testThatPluginImplementsInterfacePostProcessingPlugin() throws Exception {
        assertThat(plugin, instanceOf(PostProcessingPlugin.class));
    }

    @Test
    public void testThatPluginReturnsASstInsituTimeSeriesPostProcessing() throws Exception {
        final Element jDomElement = new Element("name");
        final PostProcessing postProcessing = plugin.createPostProcessing(jDomElement);

        assertThat(postProcessing, is(not(nullValue())));
        assertThat(postProcessing, instanceOf(PostProcessing.class));
        assertThat(postProcessing, instanceOf(SstInsituTimeSeries.class));
    }

    private static void rename_a_variable_and_add_a_new_one_with_the_old_name_and_time_series_dimension() throws IOException, InvalidRangeException {
        NetcdfFileWriter writer;
        final String varName = "avhrr-n10_x";
        final String tempVarName = varName + "_pp";
        final String dimName = "insitu_ts";
        final int stLength = 7;

        writer = NetcdfFileWriter.openExisting("F:/Development Temp Dirs/Fiduceo/Postprocessing/mmd02_avhrr-n10_avhrr-n11_1989-122_1989-123.nc");
        writer.setRedefineMode(true);

        writer.renameVariable(varName, tempVarName);
        writer.close();

        writer = NetcdfFileWriter.openExisting("F:/Development Temp Dirs/Fiduceo/Postprocessing/mmd02_avhrr-n10_avhrr-n11_1989-122_1989-123.nc");
        writer.setRedefineMode(true);

        writer.addDimension(null, dimName, stLength);
        final Variable tempVar = writer.findVariable(tempVarName);
        final String dims = tempVar.getDimensionsString();
        final DataType dataType = tempVar.getDataType();
        writer.addVariable(null, varName, dataType, dims + " " + dimName);

        writer.setRedefineMode(false);

        final Array src = writer.findVariable(tempVarName).read();
        final Variable target = writer.findVariable(varName);
        final int[] origin = new int[target.getRank()];
        origin[origin.length - 1] = stLength / 2;
        final Array data = Array.factory(dataType, target.getShape());
        final Index index = data.getIndex();
        index.set1(stLength / 2);
        for (int i = 0; i < src.getShape()[0]; i++) {
            index.set0(i);
            data.setDouble(index, src.getDouble(i));
        }
        writer.write(target, data);

        writer.close();
    }

    private static void check_if_the_values_are_equal() throws IOException, InvalidRangeException {
        final String varName = "avhrr-n10_x";
        final String tempVarName = varName + "_pp";

        final NetcdfFile ncFile = NetcdfFile.open("F:/Development Temp Dirs/Fiduceo/Postprocessing/mmd02_avhrr-n10_avhrr-n11_1989-122_1989-123.nc");
        Array oldArray = ncFile.findVariable(tempVarName).read();
        Array newArray = ncFile.findVariable(varName).read(new int[]{0, 3}, new int[]{ncFile.findDimension("matchup_count").getLength(), 1}).reduce();

        final double[] javaOld = (double[]) oldArray.get1DJavaArray(double.class);
        final double[] javaNew = (double[]) newArray.get1DJavaArray(double.class);

        for (int i = 0; i < javaOld.length; i++) {
            final double oldV = javaOld[i];
            final double newV = javaNew[i];
            if (oldV != newV) {
                System.out.println("Differences at pos: " + i + "    old " + oldV + " != " + newV);
            }
        }
        ncFile.close();
    }
}
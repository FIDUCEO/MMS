/*
 * Copyright (C) 2017 Brockmann Consult GmbH
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

package com.bc.fiduceo.post.plugin.nwp;


import org.esa.snap.core.util.math.FracIndex;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class NwpUtils {

    // @todo 2 tb/tb write tests for this method 2017-01-10
    static String composeFilesString(final String dirPath, final List<String> subDirPaths, final String pattern, int skip) {
        final StringBuilder sb = new StringBuilder();
        final List<File> allFiles = new ArrayList<>();
        final FilenameFilter filter = (dir, name) -> name.matches(pattern);
        for (final String subDirPath : subDirPaths) {
            final File subDir = new File(dirPath, subDirPath);
            final File[] files = subDir.listFiles(filter);
            if (files == null) {
                throw new RuntimeException(String.format("%s directory does not exist", subDir.getPath()));
            }
            Arrays.sort(files);
            Collections.addAll(allFiles, files);
        }
        final int m;
        final int n;
        if (skip >= 0) {
            m = skip;
            n = allFiles.size();
        } else {
            m = 0;
            n = allFiles.size() + skip;
        }
        for (int i = m; i < n; i++) {
            final File file = allFiles.get(i);
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(file.getPath());
        }
        return sb.toString();
    }

    // package public for testing
    static int computeFutureTimeStepCount(int timeStepCount) {
        return ((timeStepCount - 1) / 8) * 3;
    }

    // package public for testing
    static int computePastTimeStepCount(int timeStepCount) {
        return ((timeStepCount - 1) / 8) * 5;
    }

    // package access for testing only tb 2017-02-17
    static int nearestTimeStep(Array sourceTimes, int targetTime) {
        int timeStep = 0;
        int minTimeDelta = Math.abs(targetTime - sourceTimes.getInt(0));

        for (int i = 1; i < sourceTimes.getSize(); i++) {
            final int sourceTime = sourceTimes.getInt(i);
            final int actTimeDelta = Math.abs(targetTime - sourceTime);
            if (actTimeDelta < minTimeDelta) {
                minTimeDelta = actTimeDelta;
                timeStep = i;
            }
        }

        return timeStep;
    }

    // package access for testing only tb 2017-04-04
    static FracIndex getInterpolationIndex(Array sourceTimes, int targetTime) {
        for (int i = 1; i < sourceTimes.getSize(); i++) {
            final double maxTime = sourceTimes.getDouble(i);
            final double minTime = sourceTimes.getDouble(i - 1);
            if (targetTime >= minTime && targetTime <= maxTime) {
                final FracIndex index = new FracIndex();
                index.i = i - 1;
                index.f = (targetTime - minTime) / (maxTime - minTime);
                return index;
            }
        }
        throw new RuntimeException("Not enough time steps in NWP time series.");
    }

    static void copyValues(Map<Variable, Variable> map,
                           NetcdfFileWriter targetFile,
                           int targetMatchup,
                           int[] sourceStart,
                           int[] sourceShape) throws IOException, InvalidRangeException {
        for (final Variable targetVariable : map.keySet()) {
            final Variable sourceVariable = map.get(targetVariable);
            final Array sourceData = sourceVariable.read(sourceStart, sourceShape);
            final int[] targetShape = targetVariable.getShape();
            targetShape[0] = 1;
            final int[] targetStart = new int[targetShape.length];
            targetStart[0] = targetMatchup;
            targetFile.write(targetVariable, targetStart, sourceData.reshape(targetShape));
        }
    }
}

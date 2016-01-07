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

package com.bc.fiduceo.util;

import com.bc.fiduceo.core.SamplingPoint;

import java.util.ArrayList;
import java.util.List;

public class SobolSamplingPointGenerator {

    public List<SamplingPoint> createSamples(int sampleCount, int sampleSkip, long startTime, long stopTime) {
        final SobolSequenceGenerator sequenceGenerator = new SobolSequenceGenerator(4);
        sequenceGenerator.skip(sampleSkip);
        final List<SamplingPoint> sampleList = new ArrayList<>(sampleCount);

        for (int i = 0; i < sampleCount; i++) {
            final int index = sequenceGenerator.getNextIndex();
            final double[] sample = sequenceGenerator.nextVector();
            final double x = sample[0];
            final double y = sample[1];
            final double t = sample[2];
            final double random = sample[3];

            final double lon = x * 360.0 - 180.0;
            final double lat = 90.0 - y * 180.0;
            final long time = (long) (t * (stopTime - startTime)) + startTime;

            final SamplingPoint samplingPoint = new SamplingPoint(lon, lat, time);
            // @todo 3 tb/tb maybe extend the functionality to the commented out stuff below. If we really need this. 2016-01-07
//            final SamplingPoint p = new SamplingPoint(lon, lat, time, random);
//            p.setIndex(index);
//            p.setInsituDatasetId(InsituDatasetId.dummy_bc);
//            p.setDatasetName(String.valueOf(index));

            sampleList.add(samplingPoint);
        }

        return sampleList;
    }
}

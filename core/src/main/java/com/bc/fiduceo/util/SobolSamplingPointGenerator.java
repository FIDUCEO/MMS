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

    private final Distribution distribution;

    public SobolSamplingPointGenerator(Distribution distribution) {
        this.distribution = distribution;
    }

    static long createTime(double t, long startTime, long stopTime) {
        return (long) (t * (stopTime - startTime)) + startTime;
    }

    static double createLat(double y) {
        return 90.0 - y * 180.0;
    }

    static double createLon(double x) {
        return x * 360.0 - 180.0;
    }

    public static int createRandomSkip() {
        final long nanoTime = System.nanoTime();

        // use the lower 31 bits of time as random tb 2017-07-20
        return (int) (nanoTime & 0x000000007FFFFFFFL);
    }

    public List<SamplingPoint> createSamples(int sampleCount, int sampleSkip, long startTime, long stopTime) {
        final SobolSequenceGenerator sequenceGenerator;
        final DistributionFunction distributionFunction;
        if (distribution != Distribution.FLAT) {
            sequenceGenerator = new SobolSequenceGenerator(4);
            distributionFunction = getDistributionFunction(distribution);
        } else {
            sequenceGenerator = new SobolSequenceGenerator(3);
            distributionFunction = null;
        }
        sequenceGenerator.skip(sampleSkip);
        final List<SamplingPoint> sampleList = new ArrayList<>(sampleCount);

        int count = 0;
        while (count < sampleCount) {
            final double[] sample = sequenceGenerator.nextVector();
            final double x = sample[0];
            final double y = sample[1];
            final double t = sample[2];
            final double lat = createLat(y);
            if (distributionFunction != null) {
                if (distributionFunction.keepSample(lat, sample[3])) {
                    continue;
                }
            }
            final double lon = createLon(x);
            final long time = createTime(t, startTime, stopTime);
            sampleList.add(new SamplingPoint(lon, lat, time));
            count++;
        }

        return sampleList;
    }

    public enum Distribution {
        FLAT,
        COSINE_LAT,
        INV_TRUNC_COSINE_LAT;

        public static Distribution fromString(String string) {
            return valueOf(string);
        }
    }

    static DistributionFunction getDistributionFunction(Distribution distribution) {
        if (distribution == Distribution.COSINE_LAT)   {
            return new CosineLatDistribution();
        } else  if(distribution == Distribution.INV_TRUNC_COSINE_LAT) {
            return new InverseTruncatedCosineLatDistribution();
        }
        return null;
    }

    interface DistributionFunction {
        boolean keepSample(double lat, double sample);
    }

    static class CosineLatDistribution implements DistributionFunction {

        @Override
        public boolean keepSample(double lat, double sample) {
            final double cos = Math.cos(Math.toRadians(lat));
            return sample > cos;
        }
    }

    static class InverseTruncatedCosineLatDistribution implements DistributionFunction {

        private static final double ONE_THIRD = 1.0 / 3.0;

        @Override
        public boolean keepSample(double lat, double sample) {
            final double cos = Math.cos(Math.toRadians(lat));
            final double value = ONE_THIRD * (Math.min(1.0 / cos, 3.0));
            return sample > value;
        }
    }
}

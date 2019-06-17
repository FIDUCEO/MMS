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
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class SobolSamplingPointGeneratorTest {

    private SobolSamplingPointGenerator generator;

    @Before
    public void setUp() {
        generator = new SobolSamplingPointGenerator(SobolSamplingPointGenerator.Distribution.FLAT);
    }

    @Test
    public void testEquidistantDistribution_flat() {
        generator = new SobolSamplingPointGenerator(SobolSamplingPointGenerator.Distribution.FLAT);

        final int numPoints = 200000;
        final List<SamplingPoint> samples = generator.createSamples(numPoints, 0, 0, 100);
        int count90n60n = 0;
        int count60n30n = 0;
        int count30n0n = 0;
        int count0n30s = 0;
        int count30s60s = 0;
        int count60s90s = 0;
        for (SamplingPoint sample : samples) {
            final double lat = sample.getLat();
            if (lat >= 60) {
                count90n60n++;
            } else if (lat >= 30) {
                count60n30n++;
            } else if (lat >= 0) {
                count30n0n++;
            } else if (lat >= -30) {
                count0n30s++;
            } else if (lat >= -60) {
                count30s60s++;
            } else {
                count60s90s++;
            }
        }
        assertEquals(33333, count90n60n);
        assertEquals(33334, count60n30n);
        assertEquals(33334, count30n0n);
        assertEquals(33332, count0n30s);
        assertEquals(33334, count30s60s);
        assertEquals(33333, count60s90s);
    }

    @Test
    public void testEquidistantDistribution_cosineWeight() {
        generator = new SobolSamplingPointGenerator(SobolSamplingPointGenerator.Distribution.COSINE_LAT);

        final int numPoints = 200000;
        final List<SamplingPoint> samples = generator.createSamples(numPoints, 0, 0, 100);
        int count90n60n = 0;
        int count60n30n = 0;
        int count30n0n = 0;
        int count0n30s = 0;
        int count30s60s = 0;
        int count60s90s = 0;
        for (SamplingPoint sample : samples) {
            final double lat = sample.getLat();
            if (lat >= 60) {
                count90n60n++;
            } else if (lat >= 30) {
                count60n30n++;
            } else if (lat >= 0) {
                count30n0n++;
            } else if (lat >= -30) {
                count0n30s++;
            } else if (lat >= -60) {
                count30s60s++;
            } else {
                count60s90s++;
            }
        }
        assertEquals(13395, count90n60n);
        assertEquals(36593, count60n30n);
        assertEquals(50004, count30n0n);
        assertEquals(50000, count0n30s);
        assertEquals(36620, count30s60s);
        assertEquals(13388, count60s90s);
    }

    @Test
    public void testEquidistantDistribution_inverseCosineLat() {
        generator = new SobolSamplingPointGenerator(SobolSamplingPointGenerator.Distribution.INV_TRUNC_COSINE_LAT);

        final int numPoints = 200000;
        final List<SamplingPoint> samples = generator.createSamples(numPoints, 0, 0, 100);
        int count90n60n = 0;
        int count60n30n = 0;
        int count30n0n = 0;
        int count0n30s = 0;
        int count30s60s = 0;
        int count60s90s = 0;
        for (SamplingPoint sample : samples) {
            final double lat = sample.getLat();
            if (lat >= 60) {
                count90n60n++;
            } else if (lat >= 30) {
                count60n30n++;
            } else if (lat >= 0) {
                count30n0n++;
            } else if (lat >= -30) {
                count0n30s++;
            } else if (lat >= -60) {
                count30s60s++;
            } else {
                count60s90s++;
            }
        }
        assertEquals(52673, count90n60n);
        assertEquals(27594, count60n30n);
        assertEquals(19739, count30n0n);
        assertEquals(19745, count0n30s);
        assertEquals(27578, count30s60s);
        assertEquals(52671, count60s90s);
    }

    @Test
    public void testGenerateSample() {
        final List<SamplingPoint> samples = generator.createSamples(1, 0, 100000000L, 110000000L);
        assertEquals(1, samples.size());

        final SamplingPoint samplingPoint = samples.get(0);
        assertEquals(-180.0, samplingPoint.getLon(), 1e-8);
        assertEquals(90.0, samplingPoint.getLat(), 1e-8);
        assertEquals(100000000L, samplingPoint.getTime());
        // NOTE: these assertions only work because it is a pseudo-random sequence generator, returning always the
        // same sequence for the same sample-skip value tb 2ß16-01-07
    }

    @Test
    public void testGenerateList() {
        final long startTime = 100000000L;
        final long stopTime = 110000000L;
        final List<SamplingPoint> samples = generator.createSamples(100, 69845, startTime, stopTime);

        assertEquals(100, samples.size());
        for (final SamplingPoint samplingPoint : samples) {
            final double lon = samplingPoint.getLon();
            assertTrue(lon >= -180.0 && lon <= 180.0);

            final double lat = samplingPoint.getLat();
            assertTrue(lat >= -90.0 && lat <= 90.0);

            final long time = samplingPoint.getTime();
            assertTrue(time >= startTime && time <= stopTime);
        }
    }

    @Test
    public void testCreateTime() {
        assertEquals(100, SobolSamplingPointGenerator.createTime(0, 100, 200));
        assertEquals(133, SobolSamplingPointGenerator.createTime(0.33, 100, 200));
        assertEquals(150, SobolSamplingPointGenerator.createTime(0.5, 100, 200));
        assertEquals(167, SobolSamplingPointGenerator.createTime(0.67, 100, 200));
        assertEquals(200, SobolSamplingPointGenerator.createTime(1.0, 100, 200));
    }

    @Test
    public void testCreateLat() {
        assertEquals(90.0, SobolSamplingPointGenerator.createLat(0.0), 1e-8);
        assertEquals(54.0, SobolSamplingPointGenerator.createLat(0.2), 1e-8);
        assertEquals(0.0, SobolSamplingPointGenerator.createLat(0.5), 1e-8);
        assertEquals(-90.0, SobolSamplingPointGenerator.createLat(1.0), 1e-8);
    }

    @Test
    public void testCreateLon() {
        assertEquals(-180.0, SobolSamplingPointGenerator.createLon(0.0), 1e-8);
        assertEquals(-72.0, SobolSamplingPointGenerator.createLon(0.3), 1e-8);
        assertEquals(0.0, SobolSamplingPointGenerator.createLon(0.5), 1e-8);
        assertEquals(180.0, SobolSamplingPointGenerator.createLon(1.0), 1e-8);
    }

    @Test
    public void testCosineLatDistribution() {
        final SobolSamplingPointGenerator.DistributionFunction dist = new SobolSamplingPointGenerator.CosineLatDistribution();

        assertFalse(dist.keepSample(-45.3, 0.65));
        assertFalse(dist.keepSample(62.55, 0.416));

        assertTrue(dist.keepSample(-61.889, 265112));
        assertTrue(dist.keepSample(76.556, 0.67312));
    }

    @Test
    public void testInverseTruncatedCosineLatDistribution() {
        final SobolSamplingPointGenerator.DistributionFunction dist = new SobolSamplingPointGenerator.InverseTruncatedCosineLatDistribution();

        assertFalse(dist.keepSample(-45.3, 0.42112));
        assertFalse(dist.keepSample(62.55, 0.416));

        assertTrue(dist.keepSample(-61.889, 265112));
        assertTrue(dist.keepSample(34.556, 0.67312));
    }

    @Test
    public void testGetDistributionFunction() {
        SobolSamplingPointGenerator.DistributionFunction distributionFunction = SobolSamplingPointGenerator.getDistributionFunction(SobolSamplingPointGenerator.Distribution.FLAT);
        assertNull(distributionFunction);

        distributionFunction = SobolSamplingPointGenerator.getDistributionFunction(SobolSamplingPointGenerator.Distribution.COSINE_LAT);
        assertTrue(distributionFunction instanceof SobolSamplingPointGenerator.CosineLatDistribution);

        distributionFunction = SobolSamplingPointGenerator.getDistributionFunction(SobolSamplingPointGenerator.Distribution.INV_TRUNC_COSINE_LAT);
        assertTrue(distributionFunction instanceof SobolSamplingPointGenerator.InverseTruncatedCosineLatDistribution);
    }

    @Test
    public void testDistributionEnumFromString() {
        assertEquals(SobolSamplingPointGenerator.Distribution.FLAT, SobolSamplingPointGenerator.Distribution.fromString("FLAT"));
        assertEquals(SobolSamplingPointGenerator.Distribution.COSINE_LAT, SobolSamplingPointGenerator.Distribution.fromString("COSINE_LAT"));
        assertEquals(SobolSamplingPointGenerator.Distribution.INV_TRUNC_COSINE_LAT, SobolSamplingPointGenerator.Distribution.fromString("INV_TRUNC_COSINE_LAT"));
    }
}

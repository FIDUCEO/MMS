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

package com.bc.fiduceo.matchup.cloud;

public class AMSUBCloudScreening {

    // @todo 2 tb/tb think about creating a cloud flag coding to be able to distinguish more tan just a true/false decision 2016-02-04

    /**
     * Implements AMSU-B/ATMS/SSMT2 cloud screening algorithm as described by Marting Burgdorf (UniHH).
     * Ref: MMS-Implementation Plan
     *
     * @param ch1       183.31 GHz channel, 1 GHz bandwidth brightness temperatures
     * @param ch2       183.31 GHz channel, 7 GHz bandwidth brightness temperatures
     * @param satZenith satellite zenith angle. Nadir = 0
     */
    public boolean[] run(double[] ch1, double[] ch2, double[] satZenith) {
        final boolean[] cloudFlag = new boolean[ch1.length];
        for (int i = 0; i < ch1.length; i++) {
            if (ch1[i] > ch2[i]) {
                cloudFlag[i] = true;
            } else {
                final double threshold = getThreshold(satZenith[i]);
                if (ch1[i] < threshold) {
                    cloudFlag[i] = true;
                }
            }
        }
        return cloudFlag;
    }

    static double getThreshold(double satZenith) {
        if (satZenith == 0.0) {
            return 240.0;
        } else if (satZenith <= 7.15) {
            return 240.1;
        } else if (satZenith <= 9.35) {
            return 239.9;
        } else if (satZenith <= 11.55) {
            return 239.8;
        } else if (satZenith <= 13.75) {
            return 239.7;
        } else if (satZenith <= 15.95) {
            return 239.6;
        } else if (satZenith <= 17.05) {
            return 239.5;
        } else if (satZenith <= 18.15) {
            return 239.4;
        } else if (satZenith <= 19.25) {
            return 239.3;
        } else if (satZenith <= 21.45) {
            return 239.2;
        } else if (satZenith <= 22.55) {
            return 239.1;
        } else if (satZenith <= 23.65) {
            return 239.0;
        } else if (satZenith <= 24.75) {
            return 238.8;
        } else if (satZenith <= 25.85) {
            return 238.7;
        } else if (satZenith <= 26.95) {
            return 238.6;
        } else if (satZenith <= 28.05) {
            return 238.5;
        } else if (satZenith <= 29.15) {
            return 238.3;
        } else if (satZenith <= 30.25) {
            return 238.2;
        } else if (satZenith <= 31.35) {
            return 238.0;
        } else if (satZenith <= 32.45) {
            return 237.8;
        } else if (satZenith <= 33.55) {
            return 237.6;
        } else if (satZenith <= 34.65) {
            return 237.4;
        } else if (satZenith <= 35.75) {
            return 237.2;
        } else if (satZenith <= 36.85) {
            return 237.0;
        } else if (satZenith <= 37.95) {
            return 236.7;
        } else if (satZenith <= 39.05) {
            return 236.6;
        } else if (satZenith <= 40.15) {
            return 236.4;
        } else if (satZenith <= 41.25) {
            return 236.1;
        } else if (satZenith <= 42.35) {
            return 235.8;
        } else if (satZenith <= 43.45) {
            return 235.5;
        } else if (satZenith <= 44.55) {
            return 235.2;
        } else if (satZenith <= 45.65) {
            return 234.9;
        } else if (satZenith <= 46.75) {
            return 234.4;
        } else if (satZenith <= 47.85) {
            return 233.9;
        } else if (satZenith <= 48.95) {
            return 233.3;
        }

        throw new IllegalArgumentException("zenith angle out of range");
    }
}

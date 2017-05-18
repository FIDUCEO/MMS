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

package com.bc.fiduceo.matchup;

import java.util.HashMap;
import java.util.Map;

// todo se multisensor ... done
public class SampleSet {

    // todo se multisensor
    // if the multi sensor refactoring is done, this constant should never be used
    public static final String ONLY_ONE_SECONDARY = "0";

    private Sample primary;
    private Map<String, Sample> secondary;

    public SampleSet() {
        secondary = new HashMap<>();
    }

    public Sample getPrimary() {
        return primary;
    }

    public void setPrimary(Sample primary) {
        this.primary = primary;
    }

    // todo se multisensor ... done
    public Sample getSecondary(String sensorName) {
        return secondary.get(sensorName);
    }

    // todo se multisensor ... done
    public void setSecondary(String sensorName, Sample secondary) {
        this.secondary.put(sensorName, secondary);
    }
}

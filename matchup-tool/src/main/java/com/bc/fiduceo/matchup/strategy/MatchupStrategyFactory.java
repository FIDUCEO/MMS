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

package com.bc.fiduceo.matchup.strategy;

import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.ReaderFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class MatchupStrategyFactory {

    public static AbstractMatchupStrategy get(UseCaseConfig useCaseConfig, Logger logger) {
        if (useCaseConfig.getRandomPointsPerDay()>0) {
            return new SeedPointMatchupStrategy(logger);
        }
        final Sensor primarySensor = useCaseConfig.getPrimarySensor();
        final List<Sensor> secondarySensors = useCaseConfig.getSecondarySensors();

        // @todo 3 tb/** this is a piece of dirty code: we assume that the ReaderFactory singleton has already been created
        // therefore we can use the null argument here. Improve this! 2016-11-04
        final ReaderFactory readerFactory = ReaderFactory.get(null);
        final DataType primaryType = readerFactory.getDataType(primarySensor.getName());

        final Set<DataType> secondaryDataTypes = new HashSet<>();
        for (Sensor secondarySensor : secondarySensors) {
            final DataType secondaryType = readerFactory.getDataType(secondarySensor.getName());
            secondaryDataTypes.add(secondaryType);
        }

        if (secondaryDataTypes.size() != 1) {
            throw new RuntimeException("No matchup strategy registerd for a combination with different secondary data types");
        }

        final DataType secondaryType = secondaryDataTypes.iterator().next();
        if (primaryType == DataType.POLAR_ORBITING_SATELLITE && secondaryType == DataType.POLAR_ORBITING_SATELLITE) {
            return new PolarOrbitingMatchupStrategy(logger);
        } else if (primaryType == DataType.INSITU && secondaryType == DataType.POLAR_ORBITING_SATELLITE) {
            return new InsituPolarOrbitingMatchupStrategy(logger);
        }

        throw new RuntimeException("No matchup strategy registerd for combination primary: " + primaryType + " secondary: " + secondaryType);
    }
}

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

import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.db.QueryParameter;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.tool.ToolContext;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractMatchupStrategy {

    final Logger logger;

    AbstractMatchupStrategy(Logger logger) {
        this.logger = logger;
    }

    abstract public MatchupCollection createMatchupCollection(ToolContext context) throws SQLException, IOException, InvalidRangeException;

    List<SatelliteObservation> getPrimaryObservations(ToolContext context) throws SQLException {
        final QueryParameter parameter = PolarOrbitingMatchupStrategy.getPrimarySensorParameter(context);
        logger.info("Requesting primary data ... (" + parameter.getSensorName() + ", " + parameter.getStartTime() + ", " + parameter.getStopTime());

        final Storage storage = context.getStorage();
        final List<SatelliteObservation> primaryObservations = storage.get(parameter);

        logger.info("Received " + primaryObservations.size() + " primary satellite observations");

        return primaryObservations;
    }
}

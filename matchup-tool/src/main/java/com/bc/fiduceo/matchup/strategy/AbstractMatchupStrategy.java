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

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.QueryParameter;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.MultiPolygon;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.ObservationsSet;
import com.bc.fiduceo.matchup.condition.ConditionEngine;
import com.bc.fiduceo.matchup.condition.ConditionEngineContext;
import com.bc.fiduceo.matchup.screening.ScreeningEngine;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.tool.ToolContext;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public abstract class AbstractMatchupStrategy {

    final Logger logger;

    AbstractMatchupStrategy(Logger logger) {
        this.logger = logger;
    }

    abstract public MatchupCollection createMatchupCollection(ToolContext context) throws SQLException, IOException, InvalidRangeException;

    void applyConditionsAndScreenings(MatchupSet matchupSet,
                                      ConditionEngine conditionEngine, ConditionEngineContext conditionEngineContext,
                                      ScreeningEngine screeningEngine,
                                      Reader primaryReader, Map<String, Reader> secondaryReader) throws IOException, InvalidRangeException {
        final Dimension primarySize = primaryReader.getProductSize();
        conditionEngineContext.setPrimarySize(primarySize);
        for (Map.Entry<String, Reader> stringReaderEntry : secondaryReader.entrySet()) {
            final Reader reader = stringReaderEntry.getValue();
            final String sensorName = stringReaderEntry.getKey();
            final Dimension secondarySize = reader.getProductSize();
            conditionEngineContext.setSecondarySize(secondarySize, sensorName);
        }

        logger.info("Found " + matchupSet.getNumObservations() + " matchup pixels");
        conditionEngine.process(matchupSet, conditionEngineContext);
        logger.info("Remaining " + matchupSet.getNumObservations() + " after condition processing");

        screeningEngine.process(matchupSet, primaryReader, secondaryReader);
        logger.info("Remaining " + matchupSet.getNumObservations() + " after matchup screening");
    }

    // package access for testing only tb 2016-11-04
    // @todo 1 tb/tb update tests 2018-02-01
    static boolean isSegmented(Geometry primaryGeoBounds) {
        if (primaryGeoBounds instanceof GeometryCollection) {
            return ((GeometryCollection) primaryGeoBounds).getGeometries().length > 1;
        } else if (primaryGeoBounds instanceof MultiPolygon) {
            return ((MultiPolygon) primaryGeoBounds).getPolygons().size() > 1;
        }
        return false;
    }

    static List<QueryParameter> getSecondarySensorParameter(UseCaseConfig useCaseConfig, Date searchTimeStart, Date searchTimeEnd) {
        final ArrayList<QueryParameter> queryParameters = new ArrayList<>();

        final List<Sensor> secondarySensors = useCaseConfig.getSecondarySensors();
        for (Sensor secondarySensor : secondarySensors) {
            final QueryParameter parameter = new QueryParameter();
            assignSensor(parameter, secondarySensor);
            parameter.setStartTime(searchTimeStart);
            parameter.setStopTime(searchTimeEnd);
            queryParameters.add(parameter);
        }
        return queryParameters;
    }

    // package access for testing only tb 2016-11-04
    static PixelLocator getPixelLocator(Reader reader, boolean isSegmented, Polygon polygon) throws IOException {
        final PixelLocator pixelLocator;
        if (isSegmented) {
            pixelLocator = reader.getSubScenePixelLocator(polygon);
        } else {
            pixelLocator = reader.getPixelLocator();
        }
        return pixelLocator;
    }

    // package access for testing only tb 2016-02-23
    static QueryParameter getPrimarySensorParameter(ToolContext context) {
        final QueryParameter parameter = new QueryParameter();
        final Sensor primarySensor = context.getUseCaseConfig().getPrimarySensor();
        if (primarySensor == null) {
            throw new RuntimeException("primary sensor not present in configuration file");
        }

        AbstractMatchupStrategy.assignSensor(parameter, primarySensor);
        parameter.setStartTime(context.getStartDate());
        parameter.setStopTime(context.getEndDate());
        return parameter;
    }

    // package access for testing only tb 2016-11-04
    static void assignSensor(QueryParameter parameter, Sensor sensor) {
        parameter.setSensorName(sensor.getName());
        final String dataVersion = sensor.getDataVersion();
        if (StringUtils.isNotNullAndNotEmpty(dataVersion)) {
            parameter.setVersion(dataVersion);
        }
    }

    List<SatelliteObservation> getPrimaryObservations(ToolContext context) throws SQLException {
        final QueryParameter parameter = getPrimarySensorParameter(context);
        logger.info("Requesting primary data ... (" + parameter.getSensorName() + ", " + parameter.getStartTime() + ", " + parameter.getStopTime());

        final Storage storage = context.getStorage();
        final List<SatelliteObservation> primaryObservations = storage.get(parameter);

        logger.info("Received " + primaryObservations.size() + " primary satellite observations");

        return primaryObservations;
    }

    ObservationsSet getSecondaryObservations(ToolContext context, Date searchTimeStart, Date searchTimeEnd) throws SQLException {
        final ObservationsSet observationsSet = new ObservationsSet();

        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();
        final Storage storage = context.getStorage();
        final List<QueryParameter> parameters = getSecondarySensorParameter(useCaseConfig, searchTimeStart, searchTimeEnd);
        for (QueryParameter parameter : parameters) {
            final String sensorName = parameter.getSensorName();
            logger.info("Requesting secondary data ... (" + sensorName + ", " + parameter.getStartTime() + ", " + parameter.getStopTime());
            final List<SatelliteObservation> secondaryObservations = storage.get(parameter);
            logger.info("Received " + secondaryObservations.size() + " secondary satellite observations of sensor type " + sensorName);
            observationsSet.add(sensorName, secondaryObservations);
        }
        return observationsSet;
    }
}

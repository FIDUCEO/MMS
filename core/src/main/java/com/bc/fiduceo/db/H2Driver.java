/*
 * Copyright (C) 2015 Brockmann Consult GmbH
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

package com.bc.fiduceo.db;

import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.util.TimeUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class H2Driver extends AbstractDriver {

    private GeometryFactory geometryFactory;

    @Override
    public String getUrlPattern() {
        return "jdbc:h2";
    }

    @Override
    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    @Override
    public boolean isInitialized() throws SQLException {
        final ResultSet tables = connection.getMetaData().getTables(null, null, "SATELLITE_OBSERVATION", null);
        return tables.next();
    }

    @Override
    public void insert(SatelliteObservation observation) throws SQLException {
        final Sensor sensor = observation.getSensor();
        Integer sensorId = getSensorId(sensor.getName());
        if (sensorId == null) {
            sensorId = insert(sensor);
        }

        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO SATELLITE_OBSERVATION VALUES(default, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setTimestamp(1, TimeUtils.toTimestamp(observation.getStartTime()));
        preparedStatement.setTimestamp(2, TimeUtils.toTimestamp(observation.getStopTime()));
        preparedStatement.setByte(3, (byte) observation.getNodeType().toId());
        final com.bc.fiduceo.geometry.Geometry geoBounds = observation.getGeoBounds();
        if (geoBounds != null) {
            final String wkt = geometryFactory.format(geoBounds);
            preparedStatement.setString(4, wkt);
        } else {
            preparedStatement.setNull(4, Types.VARCHAR);
        }
        preparedStatement.setInt(5, sensorId);
        preparedStatement.setString(6, observation.getVersion());
        preparedStatement.setString(7, observation.getDataFilePath().toString());
        preparedStatement.executeUpdate();

        final ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        final int observationId;
        if (generatedKeys.next()) {
            observationId = generatedKeys.getInt(1);
        } else {
            throw new SQLException("Internal driver error: no ID generated for SATELLITE_OBSERVATION");
        }

        final TimeAxis[] timeAxes = observation.getTimeAxes();
        if (timeAxes != null) {
            for (final TimeAxis timeAxis : timeAxes) {
                preparedStatement = connection.prepareStatement("INSERT INTO TIMEAXIS VALUES(default, ?, ?, ?, ?)");
                preparedStatement.setInt(1, observationId);
                final String wkt = geometryFactory.format(timeAxis.getGeometry());
                preparedStatement.setString(2, wkt);
                preparedStatement.setTimestamp(3, TimeUtils.toTimestamp(timeAxis.getStartTime()));
                preparedStatement.setTimestamp(4, TimeUtils.toTimestamp(timeAxis.getEndTime()));
                preparedStatement.executeUpdate();
            }
        } else {
            preparedStatement = connection.prepareStatement("INSERT INTO TIMEAXIS VALUES(default, ?, ?, ?, ?)");
            preparedStatement.setInt(1, observationId);
            preparedStatement.setNull(2, Types.VARCHAR);
            preparedStatement.setTimestamp(3, TimeUtils.toTimestamp(observation.getStartTime()));
            preparedStatement.setTimestamp(4, TimeUtils.toTimestamp(observation.getStopTime()));
            preparedStatement.executeUpdate();
        }

        connection.commit();
    }

    @Override
    public AbstractBatch updatePathBatch(SatelliteObservation satelliteObservation, String newPath, AbstractBatch batch) throws SQLException {
        if (batch == null) {
            final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE SATELLITE_OBSERVATION SET DataFile = ?  WHERE ID = ? ");
            batch = new JdbcBatch(preparedStatement);
        }

        final PreparedStatement preparedStatement = (PreparedStatement) batch.getStatement();
        preparedStatement.setString(1, newPath);
        preparedStatement.setInt(2, satelliteObservation.getId());
        preparedStatement.addBatch();

        return batch;
    }

    @Override
    public void commitBatch(AbstractBatch batch) throws SQLException {
        final PreparedStatement preparedStatement = (PreparedStatement) batch.getStatement();
        preparedStatement.executeBatch();

        connection.commit();
    }

    @Override
    public List<SatelliteObservation> get() throws SQLException {
        return get(null);
    }

    @Override
    public List<SatelliteObservation> get(QueryParameter parameter) throws SQLException {
        final Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        final String sql = createSql(parameter);
        final ResultSet resultSet = statement.executeQuery(sql);
        resultSet.last();
        final int numValues = resultSet.getRow();
        resultSet.beforeFirst();

        final List<SatelliteObservation> resultList = new ArrayList<>(numValues);
        while (resultSet.next()) {
            final SatelliteObservation observation = new SatelliteObservation();

            final int observationId = resultSet.getInt("id");
            observation.setId(observationId);

            final Timestamp startDate = resultSet.getTimestamp("StartDate");
            observation.setStartTime(TimeUtils.toDate(startDate));

            final Timestamp stopDate = resultSet.getTimestamp("StopDate");
            observation.setStopTime(TimeUtils.toDate(stopDate));

            final int nodeTypeId = resultSet.getInt("NodeType");
            observation.setNodeType(NodeType.fromId(nodeTypeId));

            final Object geoBounds = resultSet.getObject("GeoBounds");
            if (geoBounds != null) {
                final String geoBoundsWkt = geoBounds.toString();
                final com.bc.fiduceo.geometry.Geometry geometry = geometryFactory.fromStorageFormat(geoBoundsWkt.getBytes());
                observation.setGeoBounds(geometry);
            }

            final int sensorId = resultSet.getInt("SensorId");
            final Sensor sensor = getSensor(sensorId);
            observation.setSensor(sensor);

            final String version = resultSet.getString("Version");
            observation.setVersion(version);

            final String dataFile = resultSet.getString("DataFile");
            observation.setDataFilePath(dataFile);

            final List<TimeAxis> timeAxesList = new ArrayList<>();
            while (observationId == resultSet.getInt("id")) {
                final TimeAxis timeAxis = getTimeAxis(resultSet);
                if (timeAxis != null) {
                    timeAxesList.add(timeAxis);
                }

                if (!resultSet.next()) {
                    break;
                }
            }
            resultSet.previous();   // need to rewind one result because the while loop runs one result too far tb 2016-09-23

            observation.setTimeAxes(timeAxesList.toArray(new TimeAxis[0]));

            resultList.add(observation);
        }

        return resultList;
    }

    private TimeAxis getTimeAxis(ResultSet resultSet) throws SQLException {
        final Object axis = resultSet.getObject("Axis");
        if (axis == null) {
            return null;
        }

        final Timestamp startTime = resultSet.getTimestamp("StartTime");
        final Date axisStartTime = TimeUtils.toDate(startTime);
        final Timestamp endTime = resultSet.getTimestamp("StopTime");
        final Date axisEndTime = TimeUtils.toDate(endTime);

        final String axisWkt = axis.toString();
        final Geometry geometry = geometryFactory.fromStorageFormat(axisWkt.getBytes());
        if (geometry instanceof MultiLineString) {
            return new L3TimeAxis(axisStartTime, axisEndTime, geometry);
        } else {
            final LineString axisGeometry = (LineString) geometry;
            return geometryFactory.createTimeAxis(axisGeometry, axisStartTime, axisEndTime);
        }
    }
}

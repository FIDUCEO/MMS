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
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.util.TimeUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;
import org.esa.snap.core.util.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public class H2Driver extends AbstractDriver {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.S";

    private GeometryFactory geometryFactory;
    private WKTWriter wktWriter;

    @Override
    public String getUrlPattern() {
        return "jdbc:h2";
    }

    @Override
    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
        wktWriter = new WKTWriter();
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

        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO SATELLITE_OBSERVATION VALUES(default, ?, ?, ?, ?, ?, ?, ?)");
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
    }

    @Override
    public List<SatelliteObservation> get() throws SQLException {
        return get(null);
    }

    @Override
    public List<SatelliteObservation> get(QueryParameter parameter) throws SQLException {

        //org.h2.tools.Server.startWebServer(connection);

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

            final Timestamp startDate = resultSet.getTimestamp("StartDate");
            observation.setStartTime(TimeUtils.toDate(startDate));

            final Timestamp stopDate = resultSet.getTimestamp("StopDate");
            observation.setStopTime(TimeUtils.toDate(stopDate));

            final int nodeTypeId = resultSet.getInt("NodeType");
            observation.setNodeType(NodeType.fromId(nodeTypeId));

            final Geometry geoBounds = (Geometry) resultSet.getObject("GeoBounds");
            if (geoBounds != null) {
                final String geoBoundsWkt = wktWriter.write(geoBounds);
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

            observation.setTimeAxes(timeAxesList.toArray(new TimeAxis[timeAxesList.size()]));

            resultList.add(observation);
        }

        //org.h2.tools.Server.startWebServer(connection);

        return resultList;
    }

    // package access for testing only tb 2016-09-21
    String createSql(QueryParameter parameter) {
        // @todo 2 tb/** refactor this method 2016-09-22
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM SATELLITE_OBSERVATION obs INNER JOIN SENSOR sen ON obs.SensorId = sen.ID INNER JOIN TIMEAXIS axis ON obs.ID = axis.ObservationId");
        if (parameter == null) {
            return sql.toString();
        }

        sql.append(" WHERE ");

        boolean appendAnd = false;

        final Date startTime = parameter.getStartTime();
        final Date stopTime = parameter.getStopTime();
        if (startTime != null) {
            sql.append("obs.stopDate >= '");
            sql.append(TimeUtils.format(startTime, DATE_PATTERN));
            sql.append("'");

            appendAnd = true;
        }

        if (stopTime != null) {
            if (appendAnd) {
                sql.append(" AND ");
            }
            sql.append("obs.startDate <= '");
            sql.append(TimeUtils.format(stopTime, DATE_PATTERN));
            sql.append("'");
            appendAnd = true;
        }

        final String sensorName = parameter.getSensorName();
        if (StringUtils.isNotNullAndNotEmpty(sensorName)) {
            if (appendAnd) {
                sql.append(" AND ");
            }

            sql.append("sen.Name = '");
            sql.append(sensorName);
            sql.append("'");
            appendAnd = true;
        }

        final String path = parameter.getPath();
        if (StringUtils.isNotNullAndNotEmpty(path)) {
            if (appendAnd) {
                sql.append(" AND ");
            }

            sql.append("obs.DataFile = '");
            sql.append(path);
            sql.append("'");
            appendAnd = true;
        }

        final String version = parameter.getVersion();
        if (StringUtils.isNotNullAndNotEmpty(version)) {
            if (appendAnd) {
                sql.append(" AND ");
            }

            sql.append("obs.Version = '");
            sql.append(version);
            sql.append("'");
        }

        return sql.toString();
    }

    private TimeAxis getTimeAxis(ResultSet resultSet) throws SQLException {
        final Geometry axis = (Geometry) resultSet.getObject("Axis");
        if (axis == null) {
            return null;
        }
        final String axisWkt = wktWriter.write(axis);
        final LineString axisGeometry = (LineString) geometryFactory.fromStorageFormat(axisWkt.getBytes());

        final Timestamp startTime = resultSet.getTimestamp("StartTime");
        final Date axisStartTime = TimeUtils.toDate(startTime);
        final Timestamp endTime = resultSet.getTimestamp("StopTime");
        final Date axisEndTime = TimeUtils.toDate(endTime);
        return geometryFactory.createTimeAxis(axisGeometry, axisStartTime, axisEndTime);
    }
}

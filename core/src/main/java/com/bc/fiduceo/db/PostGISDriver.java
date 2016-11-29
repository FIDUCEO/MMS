
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
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.util.TimeUtils;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import org.esa.snap.core.util.StringUtils;
import org.postgis.PGgeometry;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class PostGISDriver extends AbstractDriver {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.S";

    private WKBWriter wkbWriter;
    private WKBReader wkbReader;

    private GeometryFactory geometryFactory;

    public PostGISDriver() {
        wkbWriter = new WKBWriter();
        wkbReader = new WKBReader();
    }

    @Override
    public String getUrlPattern() {
        return "jdbc:postgresql";
    }


    @Override
    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    @Override
    public boolean isInitialized() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void initialize() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE SATELLITE_OBSERVATION (ID SERIAL PRIMARY KEY, " +
                "StartDate TIMESTAMP," +
                "StopDate TIMESTAMP," +
                "NodeType SMALLINT," +
                "GeoBounds GEOMETRY, " +
                "SensorId INT," +
                "Version VARCHAR(16)," +
                "DataFile VARCHAR(256))");

        statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE SENSOR (ID SERIAL PRIMARY KEY, " +
                "Name VARCHAR(64))");

        statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE TIMEAXIS (ID SERIAL PRIMARY KEY, " +
                "ObservationId INT," +
                "Axis GEOMETRY," +
                "StartTime TIMESTAMP, " +
                "StopTime TIMESTAMP, " +
                "FOREIGN KEY (ObservationId) REFERENCES SATELLITE_OBSERVATION(ID))");
    }

    @Override
    public void insert(SatelliteObservation observation) throws SQLException {
        final Sensor sensor = observation.getSensor();
        Integer sensorId = getSensorId(sensor.getName());
        if (sensorId == null) {
            sensorId = insert(sensor);
        }

        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO SATELLITE_OBSERVATION VALUES(default, ?, ?, ?, ST_GeomFromText(?), ?, ?, ?)");
        preparedStatement.setTimestamp(1, TimeUtils.toTimestamp(observation.getStartTime()));
        preparedStatement.setTimestamp(2, TimeUtils.toTimestamp(observation.getStopTime()));
        preparedStatement.setByte(3, (byte) observation.getNodeType().toId());
        final Geometry geoBounds = observation.getGeoBounds();
        if (geoBounds != null) {
            preparedStatement.setString(4, new String(geometryFactory.toStorageFormat(geoBounds)));
        }else {
           preparedStatement.setNull(4, Types.OTHER);
        }
        preparedStatement.setInt(5, sensorId);
        preparedStatement.setString(6, observation.getVersion());
        preparedStatement.setString(7, observation.getDataFilePath().toString());

        final int observationId = preparedStatement.executeUpdate();
        final TimeAxis[] timeAxes = observation.getTimeAxes();
        if (timeAxes != null) {
            for (final TimeAxis timeAxis : timeAxes) {
                preparedStatement = connection.prepareStatement("INSERT INTO TIMEAXIS VALUES(default, ?, ST_GeomFromText(?), ?, ?)");
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
            preparedStatement.setNull(2, Types.OTHER);
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

            final PGgeometry geoBounds = (PGgeometry) resultSet.getObject("GeoBounds");
            if (geoBounds != null) {
                final Geometry geometry = geometryFactory.fromStorageFormat(geoBounds.getValue().getBytes());
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
            resultSet.previous();   // need to rewind one result because the while loop runs one result too far tb 2016-11-29

            observation.setTimeAxes(timeAxesList.toArray(new TimeAxis[timeAxesList.size()]));

            resultList.add(observation);
        }

        return resultList;
    }

    // package access for testing only tb 2016-11-29
    String createSql(QueryParameter parameter) {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM SATELLITE_OBSERVATION obs INNER JOIN SENSOR sen ON obs.SensorId = sen.ID INNER JOIN TIMEAXIS axis ON obs.ID = axis.ObservationId");
        if (parameter == null) {
            return sql.toString();
        }

        sql.append(" WHERE ");

        boolean appendAnd = false;

        final java.util.Date startTime = parameter.getStartTime();

        if (startTime != null) {
            sql.append("obs.stopDate >= '");
            sql.append(TimeUtils.format(startTime, DATE_PATTERN));
            sql.append("'");

            appendAnd = true;
        }

        final java.util.Date stopTime = parameter.getStopTime();
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
        final PGgeometry axis = (PGgeometry) resultSet.getObject("Axis");
        if (axis == null) {
            return null;
        }
        final String axisWkt = axis.getValue();
        final LineString axisGeometry = (LineString) geometryFactory.fromStorageFormat(axisWkt.getBytes());

        final Timestamp startTime = resultSet.getTimestamp("StartTime");
        final java.util.Date axisStartTime = TimeUtils.toDate(startTime);
        final Timestamp endTime = resultSet.getTimestamp("StopTime");
        final java.util.Date axisEndTime = TimeUtils.toDate(endTime);
        return geometryFactory.createTimeAxis(axisGeometry, axisStartTime, axisEndTime);
    }
}

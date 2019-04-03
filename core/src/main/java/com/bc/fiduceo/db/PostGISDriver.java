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
import org.esa.snap.core.util.StringUtils;
import org.postgis.PGgeometry;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PostGISDriver extends AbstractDriver {

    private GeometryFactory geometryFactory;

    @Override
    public String getUrlPattern() {
        return "jdbc:postgresql";
    }

    @Override
    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    @Override
    public boolean isInitialized() throws SQLException {
        final ResultSet tables = connection.getMetaData().getTables(null, null, "satellite_observation", null);
        return tables.next();
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
                "FOREIGN KEY (ObservationId) REFERENCES SATELLITE_OBSERVATION(ID) ON DELETE CASCADE )");

        statement = connection.createStatement();
        statement.execute("CREATE INDEX START_TIME ON SATELLITE_OBSERVATION(StartDate)");

        statement = connection.createStatement();
        statement.execute("CREATE INDEX STOP_TIME ON SATELLITE_OBSERVATION(StopDate)");

        statement = connection.createStatement();
        statement.execute("CREATE INDEX OBSERVATION_ID ON TIMEAXIS(ObservationId)");
    }

    @Override
    public void insert(SatelliteObservation observation) throws SQLException {
        final Sensor sensor = observation.getSensor();
        Integer sensorId = getSensorId(sensor.getName());
        if (sensorId == null) {
            sensorId = insert(sensor);
        }

        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO SATELLITE_OBSERVATION VALUES(default, ?, ?, ?, ST_GeomFromText(?), ?, ?, ?) RETURNING ID");
        preparedStatement.setTimestamp(1, TimeUtils.toTimestamp(observation.getStartTime()));
        preparedStatement.setTimestamp(2, TimeUtils.toTimestamp(observation.getStopTime()));
        preparedStatement.setByte(3, (byte) observation.getNodeType().toId());
        final Geometry geoBounds = observation.getGeoBounds();
        if (geoBounds != null) {
            preparedStatement.setString(4, new String(geometryFactory.toStorageFormat(geoBounds)));
        } else {
            preparedStatement.setNull(4, Types.OTHER);
        }
        preparedStatement.setInt(5, sensorId);
        preparedStatement.setString(6, observation.getVersion());
        preparedStatement.setString(7, observation.getDataFilePath().toString());

        final ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        final int observationId = resultSet.getInt(1);
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
        }
//        else {
//            preparedStatement = connection.prepareStatement("INSERT INTO TIMEAXIS VALUES(default, ?, ?, ?, ?)");
//            preparedStatement.setInt(1, observationId);
//            preparedStatement.setNull(2, Types.OTHER);
//            preparedStatement.setTimestamp(3, TimeUtils.toTimestamp(observation.getStartTime()));
//            preparedStatement.setTimestamp(4, TimeUtils.toTimestamp(observation.getStopTime()));
//            preparedStatement.executeUpdate();
//        }
    }

    @Override
    public void updatePath(SatelliteObservation satelliteObservation, String newPath) throws SQLException {
        final QueryParameter queryParameter = new QueryParameter();
        queryParameter.setStartTime(satelliteObservation.getStartTime());
        queryParameter.setStopTime(satelliteObservation.getStopTime());
        queryParameter.setSensorName(satelliteObservation.getSensor().getName());
        queryParameter.setVersion(satelliteObservation.getVersion());
        queryParameter.setPath(satelliteObservation.getDataFilePath().toString());

        final StringBuilder sql = new StringBuilder();
        sql.append("UPDATE SATELLITE_OBSERVATION AS obs SET DataFile = '");
        sql.append(newPath);
        sql.append("' ");
        sql.append("FROM SENSOR AS sen"); //ON obs.SensorId = sen.ID

        appendWhereClause(queryParameter, sql);
        sql.append(" AND obs.SensorId = sen.ID");

        final PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
        preparedStatement.executeUpdate();
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

            //@todo 2 tb/tb writ test for this condition
            if (!timeAxesList.isEmpty()) {
                observation.setTimeAxes(timeAxesList.toArray(new TimeAxis[timeAxesList.size()]));
            }

            resultList.add(observation);
        }

        return resultList;
    }

    public boolean isAlreadyRegistered(QueryParameter queryParameter) throws SQLException {
        final StringBuilder sql = new StringBuilder("SELECT count(*) FROM satellite_observation WHERE ");
        final String sensorName = queryParameter.getSensorName();
        if (StringUtils.isNotNullAndNotEmpty(sensorName)) {
            sql.append("sensorid = (SELECT id FROM sensor WHERE name = '")
                    .append(sensorName)
                    .append("') AND ");
        }
        final String path = queryParameter.getPath();
        sql.append(" datafile = '")
                .append(path)
                .append("';");
        final Statement statement = connection.createStatement();
        final ResultSet resultSet = statement.executeQuery(sql.toString());
        resultSet.next();
        final int numValues = resultSet.getInt(1);
        return numValues > 0;
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

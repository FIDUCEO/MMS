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
import org.apache.commons.dbcp2.BasicDataSource;
import org.esa.snap.core.util.StringUtils;
import org.postgis.PGgeometry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
    public void open(DatabaseConfig databaseConfig) throws SQLException {
        final BasicDataSource dataSource = databaseConfig.getDataSource();
        try {
            final java.sql.Driver driverClass = (java.sql.Driver) Class.forName(dataSource.getDriverClassName()).newInstance();
            DriverManager.registerDriver(driverClass);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new SQLException(e.getMessage());
        }

        final String url = dataSource.getUrl();
        final Properties properties = new Properties();
        properties.put("user", dataSource.getUsername());
        properties.put("password", dataSource.getPassword());
        properties.put("loginTimeout", databaseConfig.getTimeoutInSeconds());
        properties.put("connectTimeout", databaseConfig.getTimeoutInSeconds());

        connection = DriverManager.getConnection(url, properties);
        connection.setAutoCommit(false);
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

        connection.commit();
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

        connection.commit();
    }

    @Override
    public AbstractBatch updatePathBatch(SatelliteObservation satelliteObservation, String newPath, AbstractBatch batch) throws SQLException {
        if (batch == null) {
            final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE SATELLITE_OBSERVATION SET DataFile = ? WHERE ID = ?");
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
        final Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        String sql = createSql(parameter);
        final ResultSet resultSet = statement.executeQuery(sql);

        final List<SatelliteObservation> resultList = new ArrayList<>();
        final List<TimeAxis> timeAxesList = new ArrayList<>();
        int currentId = -1;
        SatelliteObservation currentObservation = null;
        while (resultSet.next()) {
            final int observationId = resultSet.getInt("id");
            if (observationId != currentId) {
                if (currentObservation != null) {
                    currentObservation.setTimeAxes(timeAxesList.toArray(new TimeAxis[0]));
                    resultList.add(currentObservation);
                    timeAxesList.clear();
                }

                currentId = observationId;
                currentObservation = new SatelliteObservation();
                currentObservation.setId(currentId);

                final Timestamp startDate = resultSet.getTimestamp("StartDate");
                currentObservation.setStartTime(TimeUtils.toDate(startDate));

                final Timestamp stopDate = resultSet.getTimestamp("StopDate");
                currentObservation.setStopTime(TimeUtils.toDate(stopDate));

                final int nodeTypeId = resultSet.getInt("NodeType");
                currentObservation.setNodeType(NodeType.fromId(nodeTypeId));

                final PGgeometry geoBounds = (PGgeometry) resultSet.getObject("GeoBounds");
                if (geoBounds != null) {
                    final Geometry geometry = geometryFactory.fromStorageFormat(geoBounds.getValue().getBytes());
                    currentObservation.setGeoBounds(geometry);
                }

                final int sensorId = resultSet.getInt("SensorId");
                final Sensor sensor = getSensor(sensorId);
                currentObservation.setSensor(sensor);

                final String version = resultSet.getString("Version");
                currentObservation.setVersion(version);

                final String dataFile = resultSet.getString("DataFile");
                currentObservation.setDataFilePath(dataFile);

                final TimeAxis timeAxis = getTimeAxis(resultSet);
                if (timeAxis != null) {
                    timeAxesList.add(timeAxis);
                }
            } else {
                // update current observation with TimeAxis
                final TimeAxis timeAxis = getTimeAxis(resultSet);
                if (timeAxis != null) {
                    timeAxesList.add(timeAxis);
                }
            }
        }

        if (currentObservation != null) {
            currentObservation.setTimeAxes(timeAxesList.toArray(new TimeAxis[0]));
            resultList.add(currentObservation);
            timeAxesList.clear();
        }

        connection.commit();

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

        connection.commit();

        return numValues > 0;
    }

    private TimeAxis getTimeAxis(ResultSet resultSet) throws SQLException {
        final PGgeometry axis = (PGgeometry) resultSet.getObject("Axis");
        if (axis == null) {
            return null;
        }

        final Timestamp startTime = resultSet.getTimestamp("StartTime");
        final java.util.Date axisStartTime = TimeUtils.toDate(startTime);
        final Timestamp endTime = resultSet.getTimestamp("StopTime");
        final java.util.Date axisEndTime = TimeUtils.toDate(endTime);

        final String axisWkt = axis.getValue();
        final Geometry geometry = geometryFactory.fromStorageFormat(axisWkt.getBytes());
        if (geometry instanceof MultiLineString) {
            return new L3TimeAxis(axisStartTime, axisEndTime, geometry);
        } else {
            final LineString axisGeometry = (LineString) geometry;
            return geometryFactory.createTimeAxis(axisGeometry, axisStartTime, axisEndTime);
        }
    }
}

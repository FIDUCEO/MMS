
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


import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.esa.snap.core.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

abstract class AbstractDriver implements Driver {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.S";

    Connection connection;

    @Override
    public void open(BasicDataSource dataSource) throws SQLException {
        try {
            final java.sql.Driver driverClass = (java.sql.Driver) Class.forName(dataSource.getDriverClassName()).newInstance();
            DriverManager.registerDriver(driverClass);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new SQLException(e.getMessage());
        }
        connection = DriverManager.getConnection(dataSource.getUrl(),
                dataSource.getUsername(),
                dataSource.getPassword());
    }

    @Override
    public void initialize() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE SATELLITE_OBSERVATION (ID INT AUTO_INCREMENT PRIMARY KEY, " +
                "StartDate TIMESTAMP," +
                "StopDate TIMESTAMP," +
                "NodeType TINYINT," +
                "GeoBounds GEOMETRY, " +
                "SensorId INT," +
                "Version VARCHAR(16)," +
                "DataFile VARCHAR(256))");

        statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE SENSOR (ID INT AUTO_INCREMENT PRIMARY KEY, " +
                "Name VARCHAR(64))");

        statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE TIMEAXIS (ID INT AUTO_INCREMENT PRIMARY KEY, " +
                "ObservationId INT," +
                "Axis GEOMETRY," +
                "StartTime TIMESTAMP, " +
                "StopTime TIMESTAMP, " +
                "FOREIGN KEY (ObservationId) REFERENCES SATELLITE_OBSERVATION(ID) ON DELETE CASCADE)");
    }

    @Override
    public void clear() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("DROP TABLE IF EXISTS TIMEAXIS");

        statement = connection.createStatement();
        statement.execute("DROP TABLE IF EXISTS SATELLITE_OBSERVATION");

        connection.createStatement();
        statement.execute("DROP TABLE IF EXISTS SENSOR");
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }

    @Override
    public int insert(Sensor sensor) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO SENSOR VALUES(default, ?)", Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setString(1, sensor.getName());
        preparedStatement.executeUpdate();

        final ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1);
        }
        return -1;
    }

    @Override
    public boolean isAlreadyRegistered(QueryParameter queryParameter) throws SQLException {
        final List<SatelliteObservation> observations = get(queryParameter);
        return observations.size() > 0;
    }

    Sensor getSensor(int id) throws SQLException {
        final Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        final ResultSet resultSet = statement.executeQuery("SELECT * FROM SENSOR where ID = " + id);
        if (resultSet.next()) {
            final Sensor sensor = new Sensor();
            sensor.setName(resultSet.getString("Name"));
            return sensor;
        } else {
            throw new SQLException("No Sensor available for ID '" + id + "'");
        }
    }

    Integer getSensorId(String sensorName) throws SQLException {
        final Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        final ResultSet resultSet = statement.executeQuery("SELECT ID FROM SENSOR WHERE NAME = '" + sensorName + "'");

        if (resultSet.first()) {
            return resultSet.getInt("ID");
        } else {
            return null;
        }
    }

    // package access for testing only tb 2016-11-29
    static String createSql(QueryParameter parameter) {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM SATELLITE_OBSERVATION obs INNER JOIN SENSOR sen ON obs.SensorId = sen.ID LEFT OUTER JOIN TIMEAXIS axis ON obs.ID = axis.ObservationId");

        boolean hasWhereClause = hasWhereClause(parameter);
        if (!hasWhereClause) {
            appendLimitAndOffset(parameter, sql);
            return sql.toString();
        }

        appendWhereClause(parameter, sql);

        appendLimitAndOffset(parameter, sql);
        return sql.toString();
    }

    static void appendWhereClause(QueryParameter parameter, StringBuilder sql) {
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
    }

    // package access for testing only tb 2019-04-01
    static void appendLimitAndOffset(QueryParameter parameter, StringBuilder sql) {
        if (parameter != null) {
            if (parameter.getPageSize() >= 0) {
                sql.append(" LIMIT ");
                sql.append(parameter.getPageSize());
            }
            if (parameter.getOffset() >= 0) {
                sql.append(" OFFSET ");
                sql.append(parameter.getOffset());
            }
        }
    }

    // package access for testing only tb 2019-04-01
    static boolean hasWhereClause(QueryParameter parameter) {
        boolean hasWhereClause = true;
        if (parameter == null) {
            return false;
        }

        if (parameter.getStartTime() == null &&
                parameter.getStopTime() == null &&
                parameter.getSensorName() == null &&
                parameter.getVersion() == null &&
                parameter.getPath() == null) {
            hasWhereClause = false;
        }
        return hasWhereClause;
    }
}

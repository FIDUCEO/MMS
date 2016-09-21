
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


import com.bc.fiduceo.core.Sensor;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

abstract class AbstractDriver implements Driver {

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
                "FOREIGN KEY (ObservationId) REFERENCES SATELLITE_OBSERVATION(ID))");
    }

    @Override
    public void clear() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("DROP TABLE IF EXISTS SATELLITE_OBSERVATION");

        connection.createStatement();
        statement.execute("DROP TABLE IF EXISTS SENSOR");

        connection.createStatement();
        statement.execute("DROP TABLE IF EXISTS TIMEAXIS");
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

    protected Sensor getSensor(int id) throws SQLException {
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

    protected Integer getSensorId(String sensorName) throws SQLException {
        final Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        final ResultSet resultSet = statement.executeQuery("SELECT ID FROM SENSOR WHERE NAME = '" + sensorName + "'");

        if (resultSet.first()) {
            return resultSet.getInt("ID");
        } else {
            return null;
        }
    }
}

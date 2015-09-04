package com.bc.fiduceo.db;


import com.bc.fiduceo.core.Sensor;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.*;

public abstract class AbstractDriver implements Driver {

    protected Connection connection;

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
                "DataFile VARCHAR(256), " +
                "TimeAxisStartIndex INT, " +
                "TimeAxisEndIndex INT)");

        statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE SENSOR (ID INT AUTO_INCREMENT PRIMARY KEY, " +
                "Name VARCHAR(64))");
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

    protected static Timestamp toTimeStamp(java.util.Date date) {
        final long time = date.getTime();
        return new Timestamp(time);
    }

    protected static java.util.Date toDate(Timestamp timestamp) {
        final long time = timestamp.getTime();
        return new Date(time);
    }
}

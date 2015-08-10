package com.bc.fiduceo.db;

import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public class H2Driver implements Driver {

    private Connection connection;

    @Override
    public String getUrlPattern() {
        return "jdbc:h2";
    }

    @Override
    public void open(BasicDataSource dataSource) throws SQLException {
        try {
            final java.sql.Driver driverClass = (java.sql.Driver) Class.forName(dataSource.getDriverClassName()).newInstance();
            DriverManager.registerDriver(driverClass);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new SQLException(e.getMessage());
        }
        connection = DriverManager.getConnection(dataSource.getUrl());
    }

    @Override
    public void initialize() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE SATELLITE_OBSERVATION (ID INT AUTO_INCREMENT PRIMARY KEY, " +
                "StartDate TIMESTAMP," +
                "StopDate TIMESTAMP," +
                "NodeType TINYINT," +
                "GeoBounds GEOMETRY, " +
                "SensorId INT)");

        statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE SENSOR (ID INT AUTO_INCREMENT PRIMARY KEY, " +
                "Name VARCHAR)");

        connection.commit();
    }

    @Override
    public void clear() throws SQLException {
        // nothing to do here tb 2015-08-06
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }

    @Override
    public void insert(SatelliteObservation observation) throws SQLException {
        final Sensor sensor = observation.getSensor();
        Integer sensorId = getSensorId(sensor.getName());
        if (sensorId == null) {
            sensorId = insert(sensor);
        }

        final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO SATELLITE_OBSERVATION VALUES(default, ?, ?, ?, ?, ?)");
        preparedStatement.setTimestamp(1, toTimeStamp(observation.getStartTime()));
        preparedStatement.setTimestamp(2, toTimeStamp(observation.getStopTime()));
        preparedStatement.setByte(3, (byte) observation.getNodeType().toId());
        preparedStatement.setObject(4, observation.getGeoBounds());
        preparedStatement.setInt(5, sensorId);

        preparedStatement.executeUpdate();
    }

    @Override
    public List<SatelliteObservation> get() throws SQLException {
        final Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        final ResultSet resultSet = statement.executeQuery("SELECT * FROM SATELLITE_OBSERVATION");
        resultSet.last();
        final int numValues = resultSet.getRow();
        resultSet.beforeFirst();

        final List<SatelliteObservation> resultList = new ArrayList<>(numValues);
        while (resultSet.next()) {
            final SatelliteObservation observation = new SatelliteObservation();

            final Timestamp startDate = resultSet.getTimestamp("StartDate");
            observation.setStartTime(toDate(startDate));

            final Timestamp stopDate = resultSet.getTimestamp("StopDate");
            observation.setStopTime(toDate(stopDate));

            final int nodeTypeId = resultSet.getInt("NodeType");
            observation.setNodeType(NodeType.fromId(nodeTypeId));

            final Geometry geoBounds = (Geometry) resultSet.getObject("GeoBounds");
            observation.setGeoBounds(geoBounds);

            final int sensorId = resultSet.getInt("SensorId");
            final Sensor sensor = getSensor(sensorId);
            observation.setSensor(sensor);

            resultList.add(observation);
        }

        return resultList;
    }

    public int insert(Sensor sensor) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Sensor VALUES(default, ?)", Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setString(1, sensor.getName());
        preparedStatement.executeUpdate();

        final ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1);
        }
        return -1;
    }

    private static Timestamp toTimeStamp(java.util.Date date) {
        final long time = date.getTime();
        return new Timestamp(time);
    }

    private static java.util.Date toDate(Timestamp timestamp) {
        final long time = timestamp.getTime();
        return new Date(time);
    }

    private Integer getSensorId(String sensorName) throws SQLException {
        final Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        final ResultSet resultSet = statement.executeQuery("SELECT ID FROM SENSOR WHERE NAME = '" + sensorName + "'");

        if (resultSet.first()) {
            return resultSet.getInt("ID");
        } else {
            return null;
        }
    }

    private Sensor getSensor(int id) throws SQLException {
        final Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        final ResultSet resultSet = statement.executeQuery("SELECT * FROM SENSOR");
        if (resultSet.next()) {
            final Sensor sensor = new Sensor();
            sensor.setName(resultSet.getString("Name"));
            return sensor;
        } else {
            throw new SQLException("No Sensor available for ID '" + id + "'");
        }
    }
}

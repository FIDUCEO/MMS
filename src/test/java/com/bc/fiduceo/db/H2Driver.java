package com.bc.fiduceo.db;

import com.bc.fiduceo.core.SatelliteObservation;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.*;
import java.sql.Date;
import java.util.*;


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
        final Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE SATELLITE_OBSERVATION (ID INT AUTO_INCREMENT PRIMARY KEY, " +
                "StartDate TIMESTAMP," +
                "StopDate TIMESTAMP)");

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
        final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO SATELLITE_OBSERVATION VALUES(default, ?, ?)");
        preparedStatement.setTimestamp(1, toTimeStamp(observation.getStartTime()));
        preparedStatement.setTimestamp(2, toTimeStamp(observation.getStopTime()));

        preparedStatement.executeUpdate();
    }

    @Override
    public List<SatelliteObservation> get() throws SQLException {
        final Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        final ResultSet resultSet = statement.executeQuery("SELECT * FROM SATELLITE_OBSERVATION");
        resultSet.last();
        final int numValues = resultSet.getRow();
        resultSet.beforeFirst();

        final List<SatelliteObservation> resultList =  new ArrayList<>(numValues);
        while (resultSet.next()) {
            final SatelliteObservation observation = new SatelliteObservation();

            final Timestamp startDate = resultSet.getTimestamp("StartDate");
            observation.setStartTime(toDate(startDate));

            final Timestamp stopDate = resultSet.getTimestamp("StopDate");
            observation.setStopTime(toDate(stopDate));

            resultList.add(observation);
        }

        return resultList;
    }

    private static Timestamp toTimeStamp(java.util.Date date) {
        final long time = date.getTime();
        return new Timestamp(time);
    }

    private static java.util.Date toDate(Timestamp timestamp) {
        final long time = timestamp.getTime();
        return new Date(time);
    }
}

package com.bc.fiduceo.db;

import com.bc.fiduceo.core.SatelliteObservation;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


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
        statement.executeUpdate("CREATE TABLE SATELLITE_OBSERVATION (ID INT AUTO_INCREMENT PRIMARY KEY, StartDate TIMESTAMP )");

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
    public void insert(SatelliteObservation satelliteObservation) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO SATELLITE_OBSERVATION VALUES(default, ?)");
        preparedStatement.setDate(1, new java.sql.Date(satelliteObservation.getStartTime().getTime()));

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

            resultList.add(observation);
        }

        return resultList;
    }
}

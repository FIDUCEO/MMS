package com.bc.fiduceo.db;


import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DerbyDriver implements Driver {

    private Connection connection;
    private String url;

    public String getUrlPattern() {
        return "jdbc:derby";
    }

    public void open(BasicDataSource dataSource) throws SQLException {
        try {
            final java.sql.Driver driverClass = (java.sql.Driver) Class.forName(dataSource.getDriverClassName()).newInstance();
            DriverManager.registerDriver(driverClass);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new SQLException(e.getMessage());
        }

        url = dataSource.getUrl();
        final String createUrl = url.concat(";create=true");
        connection = DriverManager.getConnection(createUrl);
    }

    public void initialize() throws SQLException {
        final Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE SATELLITE_OBSERVATION (ID INT PRIMARY KEY, NAME VARCHAR(12))");

        connection.commit();
    }

    public void clear() throws SQLException {
        try {
            final String dropUrl = url.concat(";drop=true");
            DriverManager.getConnection(dropUrl);
        } catch (SQLException e) {
            if (!isShutdownException(e)) {
                throw e;
            }
        }
    }

    public void close() throws SQLException {
        try {
            final String shutdownUrl = url.concat(";shutdown=true");
            DriverManager.getConnection(shutdownUrl);
        } catch (SQLException e) {
            if (!isShutdownException(e)) {
                throw e;
            }
        }
    }

    private boolean isShutdownException(SQLException e) {
        return e.getSQLState().equals("08006");
    }
}

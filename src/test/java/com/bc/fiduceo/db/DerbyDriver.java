package com.bc.fiduceo.db;


import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DerbyDriver implements Driver {

    private Connection connection;

    public String getUrlPattern() {
        return "jdbc:derby";
    }

    public void open(BasicDataSource dataSource) throws SQLException {
        try {
            final java.sql.Driver driverClass = (java.sql.Driver) Class.forName(dataSource.getDriverClassName()).newInstance();
            DriverManager.registerDriver(driverClass);
        } catch (ClassNotFoundException e) {
            throw new SQLException(e.getMessage());
        } catch (InstantiationException e) {
            throw new SQLException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new SQLException(e.getMessage());
        }

        final String url = dataSource.getUrl();
        final String ulrWithParameters = url.concat(";create=true");
        connection = DriverManager.getConnection(ulrWithParameters);
    }

    public void initialize() throws SQLException {
        final Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE SATELLITE_OBSERVATION (ID INT PRIMARY KEY, NAME VARCHAR(12))");

        connection.commit();
    }

    public void clear() throws SQLException {
        try {
            DriverManager.getConnection("jdbc:derby:memory:fiduceo;drop=true");
        } catch (SQLException e) {
            if (!e.getSQLState().equals("08006")) {
                throw e;
            }
        }
    }

    public void close() throws SQLException {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Derby system shutdown")) {
                throw e;
            }
        }
    }
}

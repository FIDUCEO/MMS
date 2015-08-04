package com.bc.fiduceo.db;


import org.apache.commons.dbcp.BasicDataSource;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DerbyDriver implements Driver {

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
        DriverManager.getConnection(ulrWithParameters);
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

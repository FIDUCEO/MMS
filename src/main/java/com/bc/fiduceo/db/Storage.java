package com.bc.fiduceo.db;


import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Storage {

    private static Storage storage;

    public static Storage create(BasicDataSource dataSource) throws SQLException {
        if (storage == null) {
            storage = new Storage(dataSource);
        }
        return storage;
    }

    public void close() throws SQLException {
        if (storage == null) {
            return;
        }

        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Derby system shutdown")) {
                throw e;
            }
        }

        storage = null;
    }

    Storage(BasicDataSource dataSource) throws SQLException {
        // @todo 2 tb/tb move this code to an Apache Derby support class 2015-08-04
        try {
            final Driver driverClass = (Driver) Class.forName(dataSource.getDriverClassName()).newInstance();
            DriverManager.registerDriver(driverClass);
        } catch (ClassNotFoundException e) {
            throw new SQLException(e.getMessage());
        } catch (InstantiationException e) {
            throw new SQLException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new SQLException(e.getMessage());
        }

        DriverManager.getConnection("jdbc:derby:bc/fiduceo;create=true");
    }
}

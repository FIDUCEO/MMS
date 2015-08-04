package com.bc.fiduceo.db;


import java.sql.DriverManager;
import java.sql.SQLException;

public class Storage {

    public static Storage create() throws SQLException {
        DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
        return new Storage();
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

    Storage() throws SQLException {
        DriverManager.getConnection("jdbc:derby:bc/fiduceo;create=true");
    }
}

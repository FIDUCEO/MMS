package com.bc.fiduceo.db;


import org.apache.commons.dbcp.BasicDataSource;

import java.sql.SQLException;

interface Driver {

    /**
     * Used for the identification of the database-drivers. Must return the beginning of the driver-specific JDBC-Url,
     * e.g. "jdbc:mysql" for a MySQL driver.
     *
     * @return the driver URL pattern
     */
    String getUrlPattern();

    void open(BasicDataSource dataSource) throws SQLException;

    void close() throws SQLException;
}

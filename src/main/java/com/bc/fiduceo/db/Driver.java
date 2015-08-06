package com.bc.fiduceo.db;


import com.bc.fiduceo.core.SatelliteObservation;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.SQLException;
import java.util.List;

interface Driver {

    /**
     * Used for the identification of the database-drivers. Must return the beginning of the driver-specific JDBC-Url,
     * e.g. "jdbc:mysql" for a MySQL driver.
     *
     * @return the driver URL pattern
     */
    String getUrlPattern();

    void open(BasicDataSource dataSource) throws SQLException;

    void initialize() throws SQLException;

    void clear() throws SQLException;

    void close() throws SQLException;

    void insert(SatelliteObservation satelliteObservation) throws SQLException;

    List<SatelliteObservation> get() throws SQLException;
}

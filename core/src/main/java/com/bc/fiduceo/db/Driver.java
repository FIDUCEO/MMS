
/*
 * Copyright (C) 2015 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.db;


import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.geometry.GeometryFactory;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.SQLException;
import java.util.List;

public interface Driver {

    /**
     * Used for the identification of the database-drivers. Must return the beginning of the driver-specific JDBC-Url,
     * e.g. "jdbc:mysql" for a MySQL driver.
     *
     * @return the driver URL pattern
     */
    String getUrlPattern();

    void setGeometryFactory(GeometryFactory geometryFactory);

    void open(BasicDataSource dataSource) throws SQLException;

    boolean isInitialized() throws SQLException;

    void initialize() throws SQLException;

    void clear() throws SQLException;

    void close() throws SQLException;

    void insert(SatelliteObservation satelliteObservation) throws SQLException;

    List<SatelliteObservation> get() throws SQLException;

    List<SatelliteObservation> get(QueryParameter parameter) throws SQLException;

    int insert(Sensor sensor) throws SQLException;

    boolean isAlreadyRegistered(QueryParameter queryParameter) throws SQLException;
}

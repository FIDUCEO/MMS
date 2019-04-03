
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


import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.util.TimeUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MySQLDriver extends AbstractDriver {

    private GeometryFactory geometryFactory;

    @Override
    public String getUrlPattern() {
        return "jdbc:mysql";
    }

    @Override
    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    @Override
    public boolean isInitialized() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void insert(SatelliteObservation observation) throws SQLException {
        final Sensor sensor = observation.getSensor();
        Integer sensorId = getSensorId(sensor.getName());
        if (sensorId == null) {
            sensorId = insert(sensor);
        }

        final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO SATELLITE_OBSERVATION VALUES(default, ?, ?, ?, GeomFromWKB(?), ?, ?, ?, ?)");
        preparedStatement.setTimestamp(1, TimeUtils.toTimestamp(observation.getStartTime()));
        preparedStatement.setTimestamp(2, TimeUtils.toTimestamp(observation.getStopTime()));
        preparedStatement.setByte(3, (byte) observation.getNodeType().toId());
        preparedStatement.setObject(4, geometryFactory.toStorageFormat(observation.getGeoBounds()));
        preparedStatement.setInt(5, sensorId);
        preparedStatement.setString(6, observation.getDataFilePath().toString());
        // @todo 2 tb/tb insert TimeAxes here 2013-03-07

        preparedStatement.executeUpdate();
    }

    @Override
    public void updatePath(SatelliteObservation satelliteObservation, String newPath) throws SQLException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<SatelliteObservation> get() throws SQLException {
        final Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        final ResultSet resultSet = statement.executeQuery("SELECT StartDate, StopDate,NodeType, AsWKB(GeoBounds), SensorId, DataFile, TimeAxisStartIndex, TimeAxisEndIndex FROM SATELLITE_OBSERVATION");
        resultSet.last();
        final int numValues = resultSet.getRow();
        resultSet.beforeFirst();

        final List<SatelliteObservation> resultList = new ArrayList<>(numValues);
        while (resultSet.next()) {
            final SatelliteObservation observation = new SatelliteObservation();

            final Timestamp startDate = resultSet.getTimestamp("StartDate");
            observation.setStartTime(TimeUtils.toDate(startDate));

            final Timestamp stopDate = resultSet.getTimestamp("StopDate");
            observation.setStopTime(TimeUtils.toDate(stopDate));

            final int nodeTypeId = resultSet.getInt("NodeType");
            observation.setNodeType(NodeType.fromId(nodeTypeId));

            final byte[] geoBoundsBytes = resultSet.getBytes("AsWKB(GeoBounds)");
            final Geometry geometry = geometryFactory.fromStorageFormat(geoBoundsBytes);
            observation.setGeoBounds(geometry);

            final int sensorId = resultSet.getInt("SensorId");
            final Sensor sensor = getSensor(sensorId);
            observation.setSensor(sensor);

            final String dataFile = resultSet.getString("DataFile");
            observation.setDataFilePath(dataFile);

            // @todo 2 tb/tb insert TimeAxes here 2013-03-07

            resultList.add(observation);
        }

        return resultList;
    }

    @Override
    public List<SatelliteObservation> get(QueryParameter parameter) throws SQLException {
        throw new RuntimeException("not implemented");
    }
}

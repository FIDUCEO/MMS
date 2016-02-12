
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
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.util.TimeUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBWriter;
import org.esa.snap.core.util.StringUtils;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public class H2Driver extends AbstractDriver {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.S";

    private GeometryFactory geometryFactory;
    private WKBWriter wkbWriter;

    @Override
    public String getUrlPattern() {
        return "jdbc:h2";
    }

    @Override
    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
        wkbWriter = new WKBWriter();
    }

    @Override
    public void insert(SatelliteObservation observation) throws SQLException {
        final Sensor sensor = observation.getSensor();
        Integer sensorId = getSensorId(sensor.getName());
        if (sensorId == null) {
            sensorId = insert(sensor);
        }

        final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO SATELLITE_OBSERVATION VALUES(default, ?, ?, ?, ?, ?, ?, ?, ?)");
        preparedStatement.setTimestamp(1, TimeUtils.toTimestamp(observation.getStartTime()));
        preparedStatement.setTimestamp(2, TimeUtils.toTimestamp(observation.getStopTime()));
        preparedStatement.setByte(3, (byte) observation.getNodeType().toId());
        preparedStatement.setObject(4, observation.getGeoBounds().getInner());
        preparedStatement.setInt(5, sensorId);
        preparedStatement.setString(6, observation.getDataFile().getAbsolutePath());
        preparedStatement.setInt(7, observation.getTimeAxisStartIndex());
        preparedStatement.setInt(8, observation.getTimeAxisEndIndex());

        preparedStatement.executeUpdate();
    }

    @Override
    public List<SatelliteObservation> get() throws SQLException {
        return get(null);
    }

    @Override
    public List<SatelliteObservation> get(QueryParameter parameter) throws SQLException {

        //org.h2.tools.Server.startWebServer(connection);

        final Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        final String sql = createSql(parameter);
        final ResultSet resultSet = statement.executeQuery(sql);
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

            // @todo 2 tb/tb remove this when H2GIS is working properly 2015-12-22
            final Geometry geoBounds = (Geometry) resultSet.getObject("GeoBounds");
            final byte[] geoBoundsWkb = wkbWriter.write(geoBounds);
            observation.setGeoBounds(geometryFactory.fromStorageFormat(geoBoundsWkb));

            final int sensorId = resultSet.getInt("SensorId");
            final Sensor sensor = getSensor(sensorId);
            observation.setSensor(sensor);

            final String dataFile = resultSet.getString("DataFile");
            observation.setDataFile(new File(dataFile));

            final int timeAxisStartIndex = resultSet.getInt("TimeAxisStartIndex");
            observation.setTimeAxisStartIndex(timeAxisStartIndex);

            final int timeAxisEndIndex = resultSet.getInt("TimeAxisEndIndex");
            observation.setTimeAxisEndIndex(timeAxisEndIndex);

            resultList.add(observation);
        }

        //org.h2.tools.Server.startWebServer(connection);

        return resultList;
    }

    String createSql(QueryParameter parameter) {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM SATELLITE_OBSERVATION obs JOIN SENSOR sen ON obs.SensorId = sen.ID");
        if (parameter == null) {
            return sql.toString();
        }

        sql.append(" WHERE ");

        boolean appendAnd = false;

        final Date startTime = parameter.getStartTime();
        final Date stopTime = parameter.getStopTime();
        if (startTime != null) {
            sql.append("obs.stopDate >= '");
            sql.append(TimeUtils.format(startTime, DATE_PATTERN));
            sql.append("'");

            appendAnd = true;
        }

        if (stopTime != null) {
            if (appendAnd) {
                sql.append(" AND ");
            }
            sql.append("obs.startDate <= '");
            sql.append(TimeUtils.format(stopTime, DATE_PATTERN));
            sql.append("'");
            appendAnd = true;
        }

        final String sensorName = parameter.getSensorName();
        if (StringUtils.isNotNullAndNotEmpty(sensorName)) {
            if (appendAnd) {
                sql.append(" AND ");
            }

            sql.append("sen.Name = '");
            sql.append(sensorName);
            sql.append("'");
            appendAnd = true;
        }

        final com.bc.fiduceo.geometry.Geometry geometry = parameter.getGeometry();
        if (geometry != null) {
            if (appendAnd) {
                sql.append(" AND ");
            }

//            sql.append("ST_Intersects(obs.GeoBounds, ");
//            sql.append(geometryFactory.)

        }


        return sql.toString();
    }
}

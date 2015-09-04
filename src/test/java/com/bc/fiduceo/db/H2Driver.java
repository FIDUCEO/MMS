package com.bc.fiduceo.db;

import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.vividsolutions.jts.geom.Geometry;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public class H2Driver extends AbstractDriver {

    @Override
    public String getUrlPattern() {
        return "jdbc:h2";
    }

    @Override
    public void clear() throws SQLException {
        // nothing to do here tb 2015-08-06
    }

    @Override
    public void insert(SatelliteObservation observation) throws SQLException {
        final Sensor sensor = observation.getSensor();
        Integer sensorId = getSensorId(sensor.getName());
        if (sensorId == null) {
            sensorId = insert(sensor);
        }

        final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO SATELLITE_OBSERVATION VALUES(default, ?, ?, ?, ?, ?, ?, ?, ?)");
        preparedStatement.setTimestamp(1, toTimeStamp(observation.getStartTime()));
        preparedStatement.setTimestamp(2, toTimeStamp(observation.getStopTime()));
        preparedStatement.setByte(3, (byte) observation.getNodeType().toId());
        preparedStatement.setObject(4, observation.getGeoBounds());
        preparedStatement.setInt(5, sensorId);
        preparedStatement.setString(6, observation.getDataFile().getAbsolutePath());
        preparedStatement.setInt(7, observation.getTimeAxisStartIndex());
        preparedStatement.setInt(8, observation.getTimeAxisEndIndex());

        preparedStatement.executeUpdate();
    }

    @Override
    public List<SatelliteObservation> get() throws SQLException {
        final Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        final ResultSet resultSet = statement.executeQuery("SELECT * FROM SATELLITE_OBSERVATION");
        resultSet.last();
        final int numValues = resultSet.getRow();
        resultSet.beforeFirst();

        final List<SatelliteObservation> resultList = new ArrayList<>(numValues);
        while (resultSet.next()) {
            final SatelliteObservation observation = new SatelliteObservation();

            final Timestamp startDate = resultSet.getTimestamp("StartDate");
            observation.setStartTime(toDate(startDate));

            final Timestamp stopDate = resultSet.getTimestamp("StopDate");
            observation.setStopTime(toDate(stopDate));

            final int nodeTypeId = resultSet.getInt("NodeType");
            observation.setNodeType(NodeType.fromId(nodeTypeId));

            final Geometry geoBounds = (Geometry) resultSet.getObject("GeoBounds");
            observation.setGeoBounds(geoBounds);

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

        return resultList;
    }
}

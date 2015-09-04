package com.bc.fiduceo.db;

import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PostGISDriver extends AbstractDriver {

    private WKBWriter wkbWriter;
    private WKBReader wkbReader;

    public PostGISDriver() {
        wkbWriter = new WKBWriter();
        wkbReader = new WKBReader();
    }

    @Override
    public String getUrlPattern() {
        return "jdbc:postgresql";
    }

    @Override
    public void initialize() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE SATELLITE_OBSERVATION (ID SERIAL PRIMARY KEY, " +
                "StartDate TIMESTAMP," +
                "StopDate TIMESTAMP," +
                "NodeType SMALLINT," +
                "GeoBounds GEOMETRY, " +
                "SensorId INT," +
                "DataFile VARCHAR(256)," +
                "TimeAxisStartIndex INT, " +
                "TimeAxisEndIndex INT)");

        statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE SENSOR (ID SERIAL PRIMARY KEY, " +
                "Name VARCHAR(64))");
    }

    @Override
    public void clear() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("DROP TABLE SATELLITE_OBSERVATION");

        connection.createStatement();
        statement.execute("DROP TABLE SENSOR");
    }

    @Override
    public void insert(SatelliteObservation observation) throws SQLException {
        final Sensor sensor = observation.getSensor();
        Integer sensorId = getSensorId(sensor.getName());
        if (sensorId == null) {
            sensorId = insert(sensor);
        }

        final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO SATELLITE_OBSERVATION VALUES(default, ?, ?, ?, ST_GeomFromWKB(?), ?, ?, ?, ?)");
        preparedStatement.setTimestamp(1, toTimeStamp(observation.getStartTime()));
        preparedStatement.setTimestamp(2, toTimeStamp(observation.getStopTime()));
        preparedStatement.setByte(3, (byte) observation.getNodeType().toId());
        preparedStatement.setObject(4, wkbWriter.write(observation.getGeoBounds()));
        preparedStatement.setInt(5, sensorId);
        preparedStatement.setString(6, observation.getDataFile().getAbsolutePath());
        preparedStatement.setInt(7, observation.getTimeAxisStartIndex());
        preparedStatement.setInt(8, observation.getTimeAxisEndIndex());

        preparedStatement.executeUpdate();
    }

    @Override
    public List<SatelliteObservation> get() throws SQLException {
        final Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        final ResultSet resultSet = statement.executeQuery("SELECT StartDate, StopDate,NodeType, ST_AsBinary(GeoBounds), SensorId, DataFile, TimeAxisStartIndex, TimeAxisEndIndex FROM SATELLITE_OBSERVATION");
        resultSet.last();
        final int numValues = resultSet.getRow();
        resultSet.beforeFirst();

        final List<SatelliteObservation> resultList = new ArrayList<>(numValues);
        try {
            while (resultSet.next()) {
                final SatelliteObservation observation = new SatelliteObservation();

                final Timestamp startDate = resultSet.getTimestamp("StartDate");
                observation.setStartTime(toDate(startDate));

                final Timestamp stopDate = resultSet.getTimestamp("StopDate");
                observation.setStopTime(toDate(stopDate));

                final int nodeTypeId = resultSet.getInt("NodeType");
                observation.setNodeType(NodeType.fromId(nodeTypeId));

                final byte[] geoBoundsBytes = resultSet.getBytes("ST_AsBinary");
                observation.setGeoBounds(wkbReader.read(geoBoundsBytes));

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
        } catch (ParseException e) {
            throw new SQLException(e.getMessage());
        }

        return resultList;
    }
}

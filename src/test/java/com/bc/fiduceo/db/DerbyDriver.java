package com.bc.fiduceo.db;


import org.apache.commons.dbcp.BasicDataSource;

import java.sql.*;

public class DerbyDriver implements Driver {

    private Connection connection;

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
        connection = DriverManager.getConnection(ulrWithParameters);
    }

    public void initialize() throws SQLException {
        final Statement statement = connection.createStatement();
        int i = statement.executeUpdate("CREATE TABLE NAMES (ID INT PRIMARY KEY, NAME VARCHAR(12))");
//        final PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE Satellite_Observation (ID INTEGER PRIMARY KEY , StartDate TIMESTAMP, StoptDate TIMESTAMP)");
//        final boolean execute = preparedStatement.execute();

//        final DatabaseMetaData metaData = connection.getMetaData();
//        final ResultSet tables = metaData.getTables(null, null, "%", null);
//        while (tables.next()) {
//            System.out.println(tables.getString("TABLE_NAME"));
//        }
        connection.commit();
    }

    public void clear() throws SQLException {
        // @todo 1 tb/tb continue here 2015-08-04
//        final PreparedStatement preparedStatement = connection.prepareStatement("DROP TABLE NAMES");
//        preparedStatement.execute();

        connection.commit();
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

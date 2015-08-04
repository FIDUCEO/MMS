package com.bc.fiduceo.db;


import org.apache.commons.dbcp.BasicDataSource;

import java.sql.SQLException;

interface Driver {

    String getUrlPattern();

    void open(BasicDataSource dataSource) throws SQLException;

    void close() throws SQLException;
}

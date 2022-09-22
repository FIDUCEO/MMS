package com.bc.fiduceo.db;

import java.sql.PreparedStatement;

class JdbcBatch extends AbstractBatch {

    final PreparedStatement preparedStatement;

    JdbcBatch(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    @Override
    Object getStatement() {
        return preparedStatement;
    }
}

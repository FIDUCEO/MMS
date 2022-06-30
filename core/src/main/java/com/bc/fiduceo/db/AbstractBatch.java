package com.bc.fiduceo.db;

import java.sql.PreparedStatement;

public abstract class AbstractBatch {

    abstract Object getStatement();
}

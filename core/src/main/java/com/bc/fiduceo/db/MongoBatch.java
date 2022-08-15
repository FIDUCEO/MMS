package com.bc.fiduceo.db;

import com.mongodb.client.model.WriteModel;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

class MongoBatch extends AbstractBatch {

    private final List<WriteModel<Document>> writeOperations;

    public MongoBatch() {
        writeOperations = new ArrayList<>();
    }

    @Override
    Object getStatement() {
        return writeOperations;
    }
}

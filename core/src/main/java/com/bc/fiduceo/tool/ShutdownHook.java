package com.bc.fiduceo.tool;

import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.util.TempFileUtils;

import java.sql.SQLException;

public class ShutdownHook extends Thread {

    private final ToolContext context;

    public ShutdownHook(ToolContext context) {
        this.context = context;
    }

    @Override
    public void run() {
        final TempFileUtils tempFileUtils = context.getTempFileUtils();
        if (tempFileUtils != null) {
            tempFileUtils.cleanup();
        }

        final Storage storage = context.getStorage();
        if (storage != null) {
            try {
                storage.close();
            } catch (SQLException e) {
                FiduceoLogger.getLogger().severe(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

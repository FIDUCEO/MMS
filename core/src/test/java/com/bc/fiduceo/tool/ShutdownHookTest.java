package com.bc.fiduceo.tool;

import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.util.TempFileUtils;
import org.junit.Test;

import java.sql.SQLException;

import static org.mockito.Mockito.*;

@SuppressWarnings("CallToThreadRun")
public class ShutdownHookTest {

    @Test
    public void testRun() throws SQLException {
        final TempFileUtils tempFileUtils = mock(TempFileUtils.class);
        final Storage storage = mock(Storage.class);

        final ToolContext toolContext = new ToolContext();
        toolContext.setTempFileUtils(tempFileUtils);
        toolContext.setStorage(storage);

        final ShutdownHook shutdownHook = new ShutdownHook(toolContext);
        shutdownHook.run();

        verify(tempFileUtils, times(1)).cleanup();
        verifyNoMoreInteractions(tempFileUtils);

        verify(storage, times(1)).close();
        verifyNoMoreInteractions(storage);
    }

    @Test
    public void testRun_missingTempFileUtils() throws SQLException {
        final Storage storage = mock(Storage.class);

        final ToolContext toolContext = new ToolContext();
        toolContext.setStorage(storage);

        final ShutdownHook shutdownHook = new ShutdownHook(toolContext);
        shutdownHook.run();

        verify(storage, times(1)).close();
        verifyNoMoreInteractions(storage);
    }

    @Test
    public void testRun_missingStorage() {
        final TempFileUtils tempFileUtils = mock(TempFileUtils.class);

        final ToolContext toolContext = new ToolContext();
        toolContext.setTempFileUtils(tempFileUtils);

        final ShutdownHook shutdownHook = new ShutdownHook(toolContext);
        shutdownHook.run();

        verify(tempFileUtils, times(1)).cleanup();
        verifyNoMoreInteractions(tempFileUtils);
    }
}

package com.bc.fiduceo.post.plugin.nwp;


import org.junit.Before;
import org.junit.Test;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class ContextTest {

    private Context context;

    @Before
    public void setUp() {
        context = new Context();
    }

    @Test
    public void testSetGetReader() {
        final NetcdfFile reader = mock(NetcdfFile.class);

        context.setReader(reader);
        assertSame(reader, context.getReader());
    }

    @Test
    public void testSetGetWriter() {
        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);

        context.setWriter(writer);
        assertSame(writer, context.getWriter());
    }

    @Test
    public void testSetGetConfiguration() {
        final Configuration configuration = new Configuration();

        context.setConfiguration(configuration);
        assertSame(configuration, context.getConfiguration());
    }

    @Test
    public void testSetGetTemplateVariables() {
        final TemplateVariables templateVariables = mock(TemplateVariables.class);

        context.setTemplateVariables(templateVariables);
        assertSame(templateVariables, context.getTemplateVariables());
    }

    @Test
    public void testSetGetTempFileManager(){
        final TempFileManager tempFileManager = new TempFileManager();

        context.setTempFileManager(tempFileManager);
        assertSame(tempFileManager, context.getTempFileManager());
    }
}

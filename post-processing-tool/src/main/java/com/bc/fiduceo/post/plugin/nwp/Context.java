package com.bc.fiduceo.post.plugin.nwp;

import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

class Context {
    private NetcdfFile reader;
    private NetcdfFileWriter writer;
    private Configuration configuration;
    private TemplateVariables templateVariables;
    private TempFileManager tempFileManager;

    void setReader(NetcdfFile reader) {
        this.reader = reader;
    }

    NetcdfFile getReader() {
        return reader;
    }

    void setWriter(NetcdfFileWriter writer) {
        this.writer = writer;
    }

    NetcdfFileWriter getWriter() {
        return writer;
    }

    void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    Configuration getConfiguration() {
        return configuration;
    }

    void setTemplateVariables(TemplateVariables templateVariables) {
        this.templateVariables = templateVariables;
    }

    TemplateVariables getTemplateVariables() {
        return templateVariables;
    }

    void setTempFileManager(TempFileManager tempFileManager) {
        this.tempFileManager = tempFileManager;
    }

    TempFileManager getTempFileManager() {
        return tempFileManager;
    }
}

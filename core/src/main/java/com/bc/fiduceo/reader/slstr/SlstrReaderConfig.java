package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.util.JDomUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class SlstrReaderConfig {

    private boolean usePixelGeoCoding;

    public static SlstrReaderConfig loadFrom(File configDirectory) throws IOException {
        final File systemPropertiesFile = new File(configDirectory, "slstr-reader-config.xml");
        if (!systemPropertiesFile.isFile()) {
            FiduceoLogger.getLogger().info("No slstr reader configuration found, using default values.");
            return new SlstrReaderConfig();
        }

        try (FileInputStream inputStream = new FileInputStream(systemPropertiesFile)) {
            return load(inputStream);
        }
    }

    SlstrReaderConfig(Document document) {
        this();

        final Element rootElement = JDomUtils.getMandatoryRootElement("slstr-reader-config", document);
        final Element usePixelGeoCodingElement = rootElement.getChild("use-pixel-geocoding");
        if (usePixelGeoCodingElement != null) {
            usePixelGeoCoding = Boolean.parseBoolean(usePixelGeoCodingElement.getTextTrim());
        }
    }

    SlstrReaderConfig() {
        usePixelGeoCoding = false;
    }

    static SlstrReaderConfig load(InputStream inputStream) {
        final SAXBuilder saxBuilder = new SAXBuilder();
        try {
            final Document document = saxBuilder.build(inputStream);
            return new SlstrReaderConfig(document);
        } catch (JDOMException | IOException | RuntimeException e) {
            throw new RuntimeException("Unable to initialize reader configuration: " + e.getMessage(), e);
        }
    }

    boolean usePixelGeoCoding() {
        return usePixelGeoCoding;
    }
}

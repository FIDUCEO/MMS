/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package com.bc.fiduceo.post;

import com.bc.fiduceo.util.JDomUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostProcessingConfig {

    public static final String TAG_NAME_ROOT = "post-processing-config";
    public static final String TAG_NAME_POST_PROCESSINGS = "post-processings";
    private final ArrayList<PostProcessing> processings;
    transient private Document document;

    private PostProcessingConfig(Document document) {
        this.document = document;
        processings = new ArrayList<>();
        init();
    }

    public static PostProcessingConfig load(InputStream inputStream) {
        final SAXBuilder saxBuilder = new SAXBuilder();
        try {
            final Document document = saxBuilder.build(inputStream);
            return new PostProcessingConfig(document);
        } catch (JDOMException | IOException | RuntimeException e) {
            throw new RuntimeException("Unable to initialize post processing configuration: " + e.getMessage(), e);
        }
    }

    public void store(OutputStream outputStream) throws IOException {
        new XMLOutputter(Format.getPrettyFormat()).output(document, outputStream);
    }

    public List<PostProcessing> getProcessings() {
        return Collections.unmodifiableList(processings);
    }

    @SuppressWarnings("unchecked")
    private void init() {
        final Element rootElement = JDomUtils.getMandatoryRootElement(TAG_NAME_ROOT, document);
        final Element processingsElem = JDomUtils.getMandatoryChild(rootElement, TAG_NAME_POST_PROCESSINGS);

        final PostProcessingFactory factory = PostProcessingFactory.get();

        final List<Element> processingList = processingsElem.getChildren();

        for (Element processing : processingList) {
            processings.add(factory.getPostProcessing(processing));
        }
    }
}

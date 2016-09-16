/*
 * SnapLogic - Data Integration
 *
 * Copyright (C) 2016, SnapLogic, Inc.  All rights reserved.
 *
 * This program is licensed under the terms of
 * the SnapLogic Commercial Subscription agreement.
 *
 * "SnapLogic" is a trademark of SnapLogic, Inc.
 */
package com.snaplogic.snaps;

import com.google.inject.Inject;
import com.snaplogic.api.ConfigurationException;
import com.snaplogic.api.ExecutionException;
import com.snaplogic.api.Snap;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.snap.api.DocumentUtility;
import com.snaplogic.snap.api.OutputViews;
import com.snaplogic.snap.api.PropertyValues;
import com.snaplogic.snap.api.SnapCategory;
import com.snaplogic.snap.api.capabilities.Category;
import com.snaplogic.snap.api.capabilities.Errors;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Inputs;
import com.snaplogic.snap.api.capabilities.Outputs;
import com.snaplogic.snap.api.capabilities.Version;
import com.snaplogic.snap.api.capabilities.ViewType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A Snap that generates a single document.
 */
// Set the Snap title, description etc
// Also, use the "docLink" parameter to set a link to the documentation
@General(title = "Single Doc Generator", purpose = "Generates one document and completes",
        author = "Your Company Name", docLink = "http://yourdocslinkhere.com")
// This Snap does not permit input views for demonstration purposes, however, in practice, there
// should almost always be an input view
@Inputs(min = 0, max = 1, accepts = {ViewType.DOCUMENT})
// This snap has exactly one document output view (min = 1, max = 1).
// Snaps can also have binary output i.e., offers={ViewType.BINARY}
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
// This snap has an optional document error view(min = 1, max = 1).
// Snaps can also have binary error view i.e., offers={ViewType.BINARY}
@Errors(min = 1, max = 1, offers = {ViewType.DOCUMENT})
// Version number of the snap
@Version(snap = 1)
// This snap belongs to SnapCategory.READ as it is idempotent.
@Category(snap = SnapCategory.READ)
public class SingleDocGenerator implements Snap {

    private static final Logger log = LoggerFactory.getLogger(SingleDocGenerator.class);
    int counter = 0;
    @Inject
    private DocumentUtility documentUtility;
    @Inject
    private OutputViews outputViews;

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
    }

    @Override
    public void configure(PropertyValues propertyValues)
            throws ConfigurationException {
    }

    @Override
    public void execute() throws ExecutionException {
        counter++;
        log.debug("counter=" + counter);

        /*
         * Write a single document to outputView. The next snap in the pipe will
         * only ever see a single document from this snap.
         */
        Map<String, String> data = new LinkedHashMap<String, String>() {{
            put("key", "value");
        }};
        outputViews.write(documentUtility.newDocument(data));
    }

    @Override
    public void cleanup() throws ExecutionException {
    }
}
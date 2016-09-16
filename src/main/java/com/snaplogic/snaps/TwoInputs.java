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
import com.snaplogic.snap.api.Document;
import com.snaplogic.snap.api.OutputViews;
import com.snaplogic.snap.api.PropertyValues;
import com.snaplogic.snap.api.SimpleSnap;
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

import java.util.Map;

/**
 * This snap accepts two inputs and outputs to a single output. To use it, feed
 * it two JSON documents. It will output a single stream which consists
 * of the combination of the two inputs, plus an additional "Processed" field in each.
 *
 * <p>This Snap extends {@link SimpleSnap} (instead of implementing {@link Snap}).</p>
 * <p>This means that instead of having a method called 'execute' which is called once,
 * it has a method called 'process' which is called for every document the snap receives.
 * This means you do not have to iterate over incoming documents as 'process' does that
 * for you.
 */
@General(title = "Two Inputs", purpose = "Accepts two inputs (expected phone books, merges them)",
        author = "Your Company Name", docLink = "http://yourdocslinkhere.com")
@Inputs(min = 2, max = 2, accepts = {ViewType.DOCUMENT})
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Errors(min = 0, max = 1, offers = {ViewType.DOCUMENT})
@Version(snap = 1)
@Category(snap = SnapCategory.READ)
public class TwoInputs extends SimpleSnap {

    private static final Logger log = LoggerFactory.getLogger(TwoInputs.class);
    private int count = 0;
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
    public void process(Document document, String inputViewName) {
        // Demonstrates how to create a new copy of the document
        Document newdoc = document.copy();

        // get a map representation of the document
        // NOTE: we are *not* duplicating the data.
        // We will *not* have to convert the map back to a doc.
        // It's just a pointer representation.
        @SuppressWarnings("unchecked")
        Map<String, Object> data = newdoc.get(Map.class);

        // assign a value to a field of the map
        // In this case, we are just assigning 'processed=True'
        // to signal that the document has been processed.
        data.put("processed", "True");

        count++;
        // log current document number
        log.debug("count=" + count);

        // log current document
        log.debug("document: " + newdoc.toString());

        // send new document to next snap
        outputViews.write(newdoc, document);
    }

    @Override
    public void cleanup() throws ExecutionException {
        // Log final number of documents processed
        log.debug("Final count=" + count);
    }
}
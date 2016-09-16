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
import com.snaplogic.api.ViewProvider;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.common.properties.builders.ViewBuilder;
import com.snaplogic.snap.api.Document;
import com.snaplogic.snap.api.ErrorViews;
import com.snaplogic.snap.api.OutputViews;
import com.snaplogic.snap.api.PropertyValues;
import com.snaplogic.snap.api.SimpleSnap;
import com.snaplogic.snap.api.SnapCategory;
import com.snaplogic.snap.api.ViewCategory;
import com.snaplogic.snap.api.capabilities.Category;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Inputs;
import com.snaplogic.snap.api.capabilities.Outputs;
import com.snaplogic.snap.api.capabilities.Version;
import com.snaplogic.snap.api.capabilities.ViewType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This Snap demonstrates two inputs and two outputs.
 *
 * <p>To use it, feed it parents.json and children.json. It will output two streams,
 * one for only males and another for females. Unknowns will get sent to both.</p>
 */
@General(title = "Two Ins/Outs", purpose = "Accepts two inputs, sends to two outputs.",
        author = "Your Company Name", docLink = "http://yourdocslinkhere.com")
@Inputs(min = 2, max = 2, accepts = {ViewType.DOCUMENT})
@Outputs(min = 2, max = 2, offers = {ViewType.DOCUMENT})
@Version(snap = 1)
@Category(snap = SnapCategory.READ)
public class TwoInputsTwoOutputs extends SimpleSnap implements ViewProvider {
    private static final Logger log = LoggerFactory.getLogger(TwoInputsTwoOutputs.class);
    private final static String INPUT_0 = "input0";
    private final static String INPUT_1 = "input1";
    private final static String MALE_VIEW = "output_male";
    private final static String FEMALE_VIEW = "output_female";
    private int count_gender_male = 0;
    private int count_gender_female = 0;
    private int count_gender_unk = 0;
    @Inject
    private OutputViews outputViews;
    @Inject
    private ErrorViews errorViews;

    @Override
    public void defineViews(final ViewBuilder viewBuilder) {
        viewBuilder.describe(INPUT_0)
                .type(ViewType.DOCUMENT)
                .add(ViewCategory.INPUT);
        viewBuilder.describe(INPUT_1)
                .type(ViewType.DOCUMENT)
                .add(ViewCategory.INPUT);

        viewBuilder.describe(MALE_VIEW)
                .type(ViewType.DOCUMENT)
                .add(ViewCategory.OUTPUT);
        viewBuilder.describe(FEMALE_VIEW)
                .type(ViewType.DOCUMENT)
                .add(ViewCategory.OUTPUT);
    }

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
    }

    @Override
    public void configure(PropertyValues propertyValues)
            throws ConfigurationException {
    }

    /*
     * process gets called for every document
     */
    @Override
    public void process(Document document, String inputViewName) {
        // Demonstrates how to create a new copy of the document
        Document newdoc = document.copy();

        // get a map representation of the document
        // NOTE: we are *not* duplicating the data.
        // We will *not* have to convert the map back to a doc.
        // It's just a pointer representation
        Map<String, Object> data = newdoc.get(Map.class);

        // Add a new field "processed" that is set to value - TRUE
        data.put("processed", "True");

        log.debug("document:<< " + document.toString());

        log.debug("gender: " + data.get("gender"));
        // send males to male output view
        if (data.get("gender").equals("male")) {
            count_gender_male++;
            outputViews.getDocumentViewFor(MALE_VIEW).write(newdoc);
        } else if (data.get("gender").equals("female")) {
            // send females to female output view
            count_gender_female++;
            outputViews.getDocumentViewFor(FEMALE_VIEW).write(newdoc);
        } else {
            // send unknowns to error views
            count_gender_unk++;
            errorViews.write(newdoc, document);
        }
    }

    @Override
    public void cleanup() throws ExecutionException {
        log.debug("Number of males found: " + count_gender_male);
        log.debug("Number of females found: " + count_gender_female);
        log.debug("Number of unknowns found: " + count_gender_unk);
    }
}
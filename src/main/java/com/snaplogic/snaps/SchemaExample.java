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

import com.snaplogic.api.ConfigurationException;
import com.snaplogic.api.InputSchemaProvider;
import com.snaplogic.api.OutputSchemaProvider;
import com.snaplogic.common.SnapType;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.snap.api.Document;
import com.snaplogic.snap.api.PropertyValues;
import com.snaplogic.snap.api.SimpleSnap;
import com.snaplogic.snap.api.SnapCategory;
import com.snaplogic.snap.api.SnapDataException;
import com.snaplogic.snap.api.capabilities.Category;
import com.snaplogic.snap.api.capabilities.Errors;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Inputs;
import com.snaplogic.snap.api.capabilities.Outputs;
import com.snaplogic.snap.api.capabilities.Version;
import com.snaplogic.snap.api.capabilities.ViewType;
import com.snaplogic.snap.schema.api.Schema;
import com.snaplogic.snap.schema.api.SchemaProvider;

import java.util.Map;

/**
 * A Pass-through Snap to provide schema on its input and output view.
 *
 * <p>If the incoming document matches the expected schema, document is written
 * to the output view. If the document does not match the schema, it is written
 * to the error view.
 */
@General(title = "View Schema", purpose = "Demonstrates view schema definition",
        author = "Your Company Name", docLink = "http://yourdocslinkhere.com")
@Inputs(min = 1, max = 1, accepts = {ViewType.DOCUMENT})
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Errors(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Version(snap = 1)
@Category(snap = SnapCategory.TRANSFORM)
public class SchemaExample extends SimpleSnap implements InputSchemaProvider,
        OutputSchemaProvider {
    private static final String COL_A = "colA";
    private static final String COL_B = "colB";
    private static final String COL_C = "colC";
    private static final String INPUT_VIEW_NAME = "input0";
    private static final String OUTPUT_VIEW_NAME = "output0";

    @Override
    public void defineInputSchema(final SchemaProvider provider) {
        Schema colA = provider.createSchema(SnapType.STRING, COL_A);
        Schema colB = provider.createSchema(SnapType.STRING, COL_B);
        Schema colC = provider.createSchema(SnapType.STRING, COL_C);
        provider.getSchemaBuilder(INPUT_VIEW_NAME)
                .withChildSchema(colA)
                .withChildSchema(colB)
                .withChildSchema(colC)
                .build();
    }

    @Override
    public void defineOutputSchema(SchemaProvider provider) {
        Schema colA = provider.createSchema(SnapType.STRING, COL_A);
        Schema colB = provider.createSchema(SnapType.STRING, COL_B);
        Schema colC = provider.createSchema(SnapType.STRING, COL_C);
        provider.getSchemaBuilder(OUTPUT_VIEW_NAME)
                .withChildSchema(colA)
                .withChildSchema(colB)
                .withChildSchema(colC)
                .build();
    }

    @Override
    public void defineProperties(final PropertyBuilder propertyBuilder) {
    }

    @Override
    public void configure(PropertyValues propertyValues)
            throws ConfigurationException {
    }

    @Override
    protected void process(Document document, final String inputViewName) {
        // Validate incoming data against the schema
        validate(document);
        // Pass the document to the downstream snap in the pipeline.
        outputViews.write(document);
    }

    private void validate(Document document) {
        @SuppressWarnings("unchecked")
        Map<String, Object> content = document.get(Map.class);
        for (String key : new String[]{COL_A, COL_B, COL_C}) {
            if (!content.containsKey(key)) {
                // Throwing a SnapDataException instructs the SnapLogic platform
                // to forward this document to the error view with the
                // provided error message.
                throw new SnapDataException(document,
                        String.format("Data map does not contain key:%s", key));
            }
        }
    }
}
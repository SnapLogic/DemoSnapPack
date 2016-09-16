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
import com.snaplogic.account.api.capabilities.Accounts;
import com.snaplogic.api.ConfigurationException;
import com.snaplogic.common.properties.SnapProperty;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.snap.api.Document;
import com.snaplogic.snap.api.PropertyValues;
import com.snaplogic.snap.api.SimpleSnap;
import com.snaplogic.snap.api.SnapCategory;
import com.snaplogic.snap.api.capabilities.Category;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Inputs;
import com.snaplogic.snap.api.capabilities.Outputs;
import com.snaplogic.snap.api.capabilities.Version;
import com.snaplogic.snap.api.capabilities.ViewType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This Snap requires configuration of an {@link ExampleAccount}.
 *
 * <p>After the account is configured, the {@code account.userId} property can be used within the
 * Snap's settings.</p>
 */
@General(title = "Account Example", purpose = "Authenticates with an Account",
        author = "Your Company Name", docLink = "http://yourdocslinkhere.com")
@Inputs(min = 0, max = 1, accepts = {ViewType.DOCUMENT})
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Version(snap = 1)
@Category(snap = SnapCategory.READ)
@Accounts(provides = {ExampleAccount.class}, optional = false)
public class SnapWithAccount extends SimpleSnap {

    private static final String USER_ID_COPY = "userIdCopy";

    private String userIdCopy;

    @Inject
    private ExampleAccount snapAccount;

    // Once the Account has been configured, the userId can be used in the Snap
    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
        propertyBuilder.describe(USER_ID_COPY, "User ID Copy",
                "Demonstrates account.userId expression variable usage")
                .required()
                .expression(SnapProperty.DecoratorType.ENABLED_EXPRESSION)
                .defaultValue("account.userId")
                .add();
    }

    @Override
    public void configure(PropertyValues propertyValues) throws ConfigurationException {
        userIdCopy = propertyValues.getAsExpression(USER_ID_COPY).eval(null);
    }

    @Override
    protected void process(Document document, String s) {
        String token = snapAccount.connect();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("userId", userIdCopy);

        // do something with the token

        outputViews.write(documentUtility.newDocumentFor(document, data));
    }
}
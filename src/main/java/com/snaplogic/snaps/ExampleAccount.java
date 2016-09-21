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

import com.google.common.collect.ImmutableSet;
import com.snaplogic.account.api.Account;
import com.snaplogic.account.api.AccountType;
import com.snaplogic.account.api.AccountVariableProvider;
import com.snaplogic.account.api.ValidatableAccount;
import com.snaplogic.account.api.capabilities.AccountCategory;
import com.snaplogic.api.ExecutionException;
import com.snaplogic.common.properties.SnapProperty;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.common.utilities.ExpressionVariableAdapter;
import com.snaplogic.snap.api.PropertyValues;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Version;

import org.joda.time.DateTime;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Demonstrates Snap Accounts. Two security-sensitive properties, User ID and Passphrase, are used
 * to build a simple hash-token.
 *
 * <p>The User ID property is also made available to the Snap (through
 * the {@code account.userId} expression variable).</p>
 */
@General(title = "Example Snap Account")
@Version(snap = 1)
@AccountCategory(type = AccountType.CUSTOM)
public class ExampleAccount implements Account<String>, ValidatableAccount<String>,
        AccountVariableProvider {

    protected static final String USER_ID = "userId";
    protected static final String PASSPHRASE = "passphrase";

    private String userId;
    private String passphrase;

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
        propertyBuilder.describe(USER_ID, "User ID", "ID of the user")
                .required()
                // for Enhanced Account Encryption; indicate to the SnapLogic Platform
                // that Medium/High Sensitivity-configured Organizations should encrypt
                // this data
                .sensitivity(SnapProperty.SensitivityLevel.MEDIUM)
                .add();

        propertyBuilder.describe(PASSPHRASE, "Passphrase", "The user's passphrase")
                .required()
                .obfuscate() // masks user's input and sets SensitivityLevel to HIGH
                .add();
    }

    @Override
    public void configure(PropertyValues propertyValues) {
        // Exercise: sanitize and validate
        userId = propertyValues.get(USER_ID);
        passphrase = propertyValues.get(PASSPHRASE);
    }

    @Override
    public String connect() throws ExecutionException {
        /*
        Return a String that conforms to this simple hash-token scheme:

        base64(userId + ":" + expirationTimestamp + ":" +
             md5(userId + ":" + expirationTimestamp + ":" passphrase))
         */
        long expiration = getExpirationTimestamp();
        byte[] md5;

        try {
            MessageDigest md5MessageDigest = MessageDigest.getInstance("MD5");
            md5 = md5MessageDigest.digest(
                    (getUserId() + ":" + expiration + ":" + getPassphrase()).getBytes(UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new ExecutionException(e, "Unable to get MD5 MessageDigest instance")
                    .withResolution("Contact the Snap Developer");
        }

        return encodeBase64String(
                (userId + ":" + expiration + ":" + new String(md5, UTF_8)).getBytes(UTF_8));
    }

    @Override
    public void disconnect() throws ExecutionException {
        // no-op
    }

    /*
    Make account property values available to a Snap
     */
    @Override
    public Map<String, Object> getAccountVariableValue() {
        return new ExpressionVariableAdapter() {
            @Override
            public Set<Entry<String, Object>> entrySet() {
                return new ImmutableSet.Builder<Entry<String, Object>>()
                        .add(entry(USER_ID, getUserId()))
                        .build();
            }
        };
    }

    protected long getExpirationTimestamp() {
        return DateTime.now().plusDays(1).getMillis();
    }

    public String getUserId() {
        return userId;
    }

    public String getPassphrase() {
        return passphrase;
    }
}
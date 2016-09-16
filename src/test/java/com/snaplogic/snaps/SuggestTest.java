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

import com.snaplogic.snap.test.harness.SnapTestRunner;
import com.snaplogic.snap.test.harness.TestFixture;
import com.snaplogic.snap.test.harness.TestSetup;

import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests that the {@link Suggest} Snap's "echo" property matches the value of the "name" property
 * when the suggest (lookup) functionality is enabled.
 */
@RunWith(SnapTestRunner.class)
public class SuggestTest {
    private static final String FOO = "foo";

    // asserting using TestSetup.suggest()
    @TestFixture(snap = Suggest.class)
    public void suggest_WithTestSetupAndSuggest_ReturnsCorrectValue(TestSetup testSetup)
            throws Exception {
        // Set the value for the property "name"
        testSetup.setPropertyValue(Suggest.PROP_NAME, FOO);
        // Call suggest for the property "echo"
        Map<String, Object> values = testSetup.suggest(Suggest.PROP_ECHO);
        // This should return the value that was set for the property "name"
        assertEquals(Arrays.asList(FOO), values.get(Suggest.PROP_ECHO));
    }

    // "suggestProperty" and "expectedOutputPath" can be used together to declare the expected
    // output of a Suggest action on the specified property
    @TestFixture(snap = Suggest.class,
            suggestProperty = Suggest.PROP_ECHO,
            expectedOutputPath = "data/suggest",
            properties = "data/suggest/suggest_properties.json")
    public void suggest_WithSuggestProperty_WritesResultsToSpecialOutputView() throws Exception {
    }
}
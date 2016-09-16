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

import org.junit.runner.RunWith;

@RunWith(SnapTestRunner.class)
public class PropertyTypesTest {

    @TestFixture(snap = PropertyTypes.class,
            outputs = "output0",
            expectedOutputPath = "data/property_types",
            properties = "data/property_types/property_types_properties.json")
    public void propertyTypes_WithFieldsCompositesAndTables_OutputsEvaluatedValuesDocument() throws Exception {

    }
}

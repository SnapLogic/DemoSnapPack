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

import com.snaplogic.snap.test.harness.OutputRecorder;
import com.snaplogic.snap.test.harness.SnapTestRunner;
import com.snaplogic.snap.test.harness.TestFixture;
import com.snaplogic.snap.test.harness.TestResult;

import org.junit.Ignore;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests that the {@link DocGenerator} Snap creates the number of Documents specified by the "count"
 * property.
 */
@RunWith(SnapTestRunner.class)
public class DocGeneratorTest {

    // "properties" is the path to the JSON file representing the Snap's settings
    @TestFixture(snap = DocGenerator.class,
            outputs = "output0",
            properties = "data/doc_generator/doc_generator_properties.json")
    public void docGenerator_WithProperties_OutputsCorrectNumberOfDocuments(TestResult testResult)
            throws Exception {
        assertNull(testResult.getException());
        OutputRecorder outputRecorder = testResult.getOutputViewByName("output0");
        assertEquals(5, outputRecorder.getRecordedData().size());
    }

    // "propertyOverrides" allows overriding specific values defined in the "properties" file
    @TestFixture(snap = DocGenerator.class,
            outputs = "output0",
            properties = "data/doc_generator/doc_generator_properties.json",
            propertyOverrides = {"$.settings.count.value", "20"})
    public void docGenerator_WithPropertyOverrides_OutputsCorrectNumDocuments(TestResult testResult)
            throws Exception {
        assertNull(testResult.getException());
        OutputRecorder outputRecorder = testResult.getOutputViewByName("output0");
        assertEquals(20, outputRecorder.getRecordedData().size());
    }

    /**
     * Tests that the {@link DocGenerator} Snap's output generated for the given value of "count"
     * property matches with what is provided in a separate file referred to by "expectedOutputPath"
     * attribute of the TestFixture annotation.
     */
    @TestFixture(snap = DocGenerator.class,
            outputs = "output0",
            expectedOutputPath = "data/doc_generator",
            properties = "data/doc_generator/doc_generator_properties.json")
    public void docGenerator_WithExpectedOutputPath_OutputsDocumentsCorrectly()
            throws Exception {
    }

    // "expectedErrorPath" specifies the folder that contains the
    // "docGenerator_WithExpectedErrorPath_OutputsErrorDocumentCorrectly-err.json" file
    @TestFixture(snap = DocGenerator.class,
            errors = "error0",
            expectedErrorPath = "data/doc_generator",
            properties = "data/doc_generator/doc_generator_properties.json",
            propertyOverrides = {"$.settings.count.value", "-2"})
    public void docGenerator_WithExpectedErrorPath_OutputsErrorDocumentCorrectly()
            throws Exception {
    }
}

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

import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Tests that the {@link SchemaExample} Snap routes to the output and error views when valid and
 * invalid data, respectively, is passed in as input.
 */
@RunWith(SnapTestRunner.class)
public class SchemaExampleTest {

    @TestFixture(snap = SchemaExample.class,
            input = "data/schema_example/schema_valid_input.data",
            outputs = "output0")
    public void schemaExample_WithValidData_OutputsDocument(TestResult testResult)
            throws Exception {
        // Input document should appear on the output
        OutputRecorder outputRecorder = testResult.getOutputViewByName("output0");
        assertEquals(1, outputRecorder.getDocumentCount());
    }

    @TestFixture(snap = SchemaExample.class,
            input = "data/schema_example/schema_invalid_input.data",
            outputs = "output0",
            errors = "error0")
    public void schemaExample_WithInvalidData_OutputsErrorDocumentOnly(TestResult testResult)
            throws Exception {
        // Since the input document does not conform to the expected schema,
        // there should be no output.
        OutputRecorder outputRecorder = testResult.getOutputViewByName("output0");
        assertEquals(0, outputRecorder.getDocumentCount());

        // Input document should be forwarded to the error view as it does not
        // match the expected schema.
        OutputRecorder errorRecorder = testResult.getErrorViewByName("error0");
        assertEquals(1, errorRecorder.getDocumentCount());
    }
}

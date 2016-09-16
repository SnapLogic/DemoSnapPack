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
import static org.junit.Assert.assertNull;

/**
 * Tests that the {@link SingleDocGenerator} Snap sent one Document to the output view.
 */
@RunWith(SnapTestRunner.class)
public class SingleDocGeneratorTest {

    // "snap" attribute targets the Snap being executed
    // "outputs" defines the name of the output view to write to
    @TestFixture(snap = SingleDocGenerator.class,
            outputs = "output0")
    public void singleDocGenerator_WithOutputView_OutputsCorrectNumDocuments(TestResult testResult)
            throws Exception {
        assertNull(testResult.getException());
        OutputRecorder outputRecorder = testResult.getOutputViewByName("output0");
        assertEquals(1, outputRecorder.getRecordedData().size());
    }
}
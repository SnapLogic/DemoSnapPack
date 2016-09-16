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
public class TwoInputsTwoOutputsTest {

    @TestFixture(snap = TwoInputsTwoOutputs.class,
            input = "data/two_inputs_two_outputs/input_files.json",
            outputs = {"output_male", "output_female"},
            expectedOutputPath = "data/two_inputs_two_outputs")
    public void twoInputsTwoOutputs_WithParentsAndChildren_GroupsByGender() throws Exception {
    }
}

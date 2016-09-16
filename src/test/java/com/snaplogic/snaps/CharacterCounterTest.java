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

/**
 * Tests that the {@link CharacterCounter} Snap correctly counts the number of occurrences of each
 * letter in the input document.
 */
@RunWith(SnapTestRunner.class)
public class CharacterCounterTest {

    @TestFixture(snap = CharacterCounter.class,
            input = "data/character_counter/character_counter_input_pointer.json",
            outputs = "output0",
            expectedOutputPath = "data/character_counter")
    public void characterCounter_WithSnapLogicAsInput_CountsOccurrences() throws Exception {
    }

}

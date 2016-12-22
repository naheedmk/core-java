/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.spine3.change;

import com.google.protobuf.FloatValue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.spine3.protobuf.AnyPacker.unpack;
import static org.spine3.test.Tests.hasPrivateUtilityConstructor;

public class FloatMismatchShould {

    private static final int VERSION = 100;
    private static final double DELTA = 0.01;

    @Test
    public void have_private_constructor() {
        assertTrue(hasPrivateUtilityConstructor(FloatMismatch.class));
    }

    @Test
    public void return_mismatch_object_with_float_values() {
        final float expected = 0.0F;
        final float actual = 1.0F;
        final float newValue = 19.0F;
        final ValueMismatch mismatch = FloatMismatch.of(expected, actual, newValue, VERSION);
        final FloatValue expectedWrapped = unpack(mismatch.getExpectedPreviousValue());
        final FloatValue actualWrapped = unpack(mismatch.getActualPreviousValue());

        assertEquals(expected, expectedWrapped.getValue(), DELTA);
        assertEquals(actual, actualWrapped.getValue(), DELTA);
    }

}

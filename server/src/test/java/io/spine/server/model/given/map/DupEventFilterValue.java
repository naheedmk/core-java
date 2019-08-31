/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.server.model.given.map;

import io.spine.core.ByField;
import io.spine.core.Subscribe;
import io.spine.server.projection.Projection;
import io.spine.server.projection.given.SavedString;
import io.spine.test.projection.event.Int32Imported;

import static io.spine.testing.Tests.halt;

/**
 * This projection class is not valid because values used in the filtering subscribes
 * evaluate to the same field value (even though that the string values are different).
 */
public final class DupEventFilterValue
        extends Projection<String, SavedString, SavedString.Builder> {

    private static final String VALUE_FIELD_PATH = "value";

    private DupEventFilterValue(String id) {
        super(id);
    }

    @Subscribe(filter = @ByField(path = VALUE_FIELD_PATH, value = "1"))
    void onString1(Int32Imported event) {
        halt();
    }

    @Subscribe(filter = @ByField(path = VALUE_FIELD_PATH, value = "+1"))
    void onStringOne(Int32Imported event) {
        halt();
    }
}

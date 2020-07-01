/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.server.event.store;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Timestamp;
import io.spine.core.Event;
import io.spine.query.RecordColumn;

/**
 * Columns stored along with {@link Event}.
 */
final class EventColumn {

    /**
     * The name of the column storing the Protobuf type name of the event.
     *
     * <p>For example, an Event of type {@code io.spine.test.TaskAdded} whose definition
     * is enclosed in the {@code spine.test} Protobuf package would have this column
     * equal to {@code "spine.test.TaskAdded"}.
     */
    static final RecordColumn<Event, String> type =
            new RecordColumn<>("type", String.class, (m) -> m.enclosedTypeUrl()
                                                             .toTypeName()
                                                             .value());

    /**
     * The name of the column storing the time, when the event was fired.
     */
    @SuppressWarnings("DuplicateStringLiteralInspection")   // popular column name.
    static final RecordColumn<Event, Timestamp> created =
            new RecordColumn<>("created", Timestamp.class, (m) -> m.getContext()
                                                                   .getTimestamp());

    private EventColumn() {
    }

    static ImmutableList<RecordColumn<Event, ?>> definitions() {
        return ImmutableList.of(type, created);
    }
}
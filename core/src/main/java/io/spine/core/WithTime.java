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

package io.spine.core;

import com.google.protobuf.Timestamp;
import io.spine.time.InstantConverter;
import io.spine.time.TimestampTemporal;

import java.time.Instant;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An object with associated point in time.
 */
public interface WithTime {

    /**
     * Obtains the point in time associated with the object.
     */
    Timestamp timestamp();

    /**
     * Verifies if associated time is after the passed point in time.
     */
    default boolean isAfter(Timestamp bound) {
        checkNotNull(bound);
        TimestampTemporal timeTemporal = TimestampTemporal.from(timestamp());
        TimestampTemporal boundTemporal = TimestampTemporal.from(bound);
        return timeTemporal.isLaterThan(boundTemporal);
    }

    /**
     * Verifies if the associated time is before the passed point in time.
     */
    default boolean isBefore(Timestamp bound) {
        checkNotNull(bound);
        TimestampTemporal timeTemporal = TimestampTemporal.from(timestamp());
        TimestampTemporal boundTemporal = TimestampTemporal.from(bound);
        return timeTemporal.isEarlierThan(boundTemporal);
    }

    /**
     * Verifies if the associated time point is within the passed period of time.
     *
     * @param periodStart
     *         lower bound, exclusive
     * @param periodEnd
     *         higher bound, inclusive
     * @return {@code true} if the time point of the command creation lies in between the given two
     */
    default boolean isBetween(Timestamp periodStart, Timestamp periodEnd) {
        checkNotNull(periodStart);
        checkNotNull(periodEnd);
        TimestampTemporal timeTemporal = TimestampTemporal.from(timestamp());
        TimestampTemporal start = TimestampTemporal.from(periodStart);
        TimestampTemporal end = TimestampTemporal.from(periodEnd);
        return timeTemporal.isBetween(start, end);
    }

    /**
     * Obtains the associated time point as {@link Instant}.
     *
     * @see #timestamp()
     */
    default Instant instant() {
        Instant result = InstantConverter.reversed()
                                         .convert(timestamp());
        return checkNotNull(result);
    }
}

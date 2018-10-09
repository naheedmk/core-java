/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.server.stand.given;

import com.google.protobuf.Any;
import io.spine.server.stand.AggregateStateId;
import io.spine.test.projection.ProjectId;
import io.spine.testdata.Sample;
import io.spine.type.TypeUrl;

import static io.spine.server.stand.AggregateStateId.of;

/**
 * @author Dmytro Dashenkov
 * @author Dmytro Kuzmin
 */
public class AggregateStateIdStringifierTestEnv {

    /** Prevents instantiation of this utility class. */
    private AggregateStateIdStringifierTestEnv() {
    }

    public static AggregateStateId newStringId() {
        return of("some-aggregate-ID", TypeUrl.of(Any.class));
    }

    public static AggregateStateId newIntId() {
        return of(42, TypeUrl.of(Any.class));
    }

    public static AggregateStateId newLongId() {
        return of(42L, TypeUrl.of(Any.class));
    }

    public static AggregateStateId newMessageId() {
        return of(Sample.messageOfType(ProjectId.class), TypeUrl.of(Any.class));
    }
}
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

package io.spine.server.storage.memory.given;

import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import io.spine.core.Version;
import io.spine.core.Versions;
import io.spine.protobuf.AnyPacker;
import io.spine.server.entity.storage.EntityRecordSpec;
import io.spine.server.entity.storage.OldColumnName;
import io.spine.server.projection.Projection;
import io.spine.server.storage.CustomColumn;
import io.spine.server.storage.OldColumn;
import io.spine.test.storage.StgProject;
import io.spine.test.storage.StgProjectId;
import io.spine.test.storage.StgProjectWithColumns;
import io.spine.testdata.Sample;

/**
 * The test environment for {@link io.spine.server.storage.memory.RecordQueryMatcher} tests.
 *
 * <p>Provides various types of {@linkplain CustomColumn record columns}
 * that can be used to emulate a client-side query.
 */
public final class RecordQueryMatcherTestEnv {

    /** Prevents instantiation of this test env class. */
    private RecordQueryMatcherTestEnv() {
    }

    /**
     * A {@code Column} which holds an {@link Any} instance.
     */
    public static OldColumn anyColumn() {
        return column("wrapped_state");
    }

    /**
     * The {@link Any} value held by the corresponding {@linkplain #anyColumn() entity column}.
     */
    public static Any anyValue() {
        StgProject someMessage = Sample.messageOfType(StgProject.class);
        Any value = AnyPacker.pack(someMessage);
        return value;
    }

    /**
     * A {@code Column} which holds a {@code boolean} value.
     */
    public static OldColumn booleanColumn() {
        return column("internal");
    }

    /**
     * The {@code boolean} value held by the corresponding {@linkplain #booleanColumn() entity
     * column}.
     */
    @SuppressWarnings("MethodOnlyUsedFromInnerClass")   // for the sake of consistency.
    private static boolean booleanValue() {
        return true;
    }

    private static OldColumn column(String name) {
        EntityRecordSpec spec = EntityRecordSpec.of(ProjectView.class);
        OldColumnName columnName = OldColumnName.of(name);
        OldColumn column = spec.get(columnName);
        return column;
    }

    private static class ProjectView
            extends Projection<StgProjectId, StgProject, StgProject.Builder>
            implements StgProjectWithColumns {

        @Override
        public String getIdString() {
            return idAsString();
        }

        @Override
        public boolean getInternal() {
            return booleanValue();
        }

        @Override
        public Any getWrappedState() {
            return anyValue();
        }

        @Override
        public int getProjectStatusValue() {
            return 0;
        }

        @Override
        public Version getProjectVersion() {
            return Versions.zero();
        }

        @Override
        public Timestamp getDueDate() {
            return Timestamp.newBuilder()
                            .setSeconds(4250)
                            .setNanos(212)
                            .build();
        }
    }
}
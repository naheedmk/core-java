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

package io.spine.testing.server.model.given;

import com.google.common.collect.ImmutableList;
import io.spine.base.EventMessage;
import io.spine.server.command.AbstractCommandHandler;
import io.spine.server.command.Assign;
import io.spine.testing.server.given.entity.command.TuRemoveProject;

import java.util.List;

public class ModelTestsTestEnv {

    /** Prevents instantiation of this utility class. */
    private ModelTestsTestEnv() {
    }

    public static class TestCommandHandler extends AbstractCommandHandler {
        private TestCommandHandler() {
            super();
        }

        @Assign
        List<EventMessage> handle(TuRemoveProject cmd) {
            return ImmutableList.of(nothing());
        }
    }

    public static class DuplicatedCommandHandler extends AbstractCommandHandler {
        private DuplicatedCommandHandler() {
            super();
        }

        /**
         * Handles the same command as {@link TestCommandHandler#handle(TuRemoveProject)}.
         */
        @Assign
        List<EventMessage> handle(TuRemoveProject cmd) {
            return ImmutableList.of(nothing());
        }
    }
}

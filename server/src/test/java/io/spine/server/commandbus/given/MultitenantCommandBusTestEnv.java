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

package io.spine.server.commandbus.given;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.spine.server.commandbus.CommandDispatcher;
import io.spine.server.type.CommandClass;
import io.spine.server.type.CommandEnvelope;
import io.spine.server.type.MessageEnvelope;
import io.spine.test.command.CmdAddTask;

import java.util.Set;

public class MultitenantCommandBusTestEnv {

    /** Prevents instantiation of this utility class. */
    private MultitenantCommandBusTestEnv() {
    }

    /**
     * The dispatcher that remembers that
     * {@link CommandDispatcher#dispatch(MessageEnvelope) dispatch()}
     * was called.
     */
    public static class AddTaskDispatcher implements CommandDispatcher<Message> {

        private boolean dispatcherInvoked = false;

        @Override
        public Set<CommandClass> messageClasses() {
            return CommandClass.setOf(CmdAddTask.class);
        }

        @Override
        public Message dispatch(CommandEnvelope envelope) {
            dispatcherInvoked = true;
            return Empty.getDefaultInstance();
        }

        @Override
        public void onError(CommandEnvelope envelope, RuntimeException exception) {
            // Do nothing.
        }

        public boolean wasDispatcherInvoked() {
            return dispatcherInvoked;
        }
    }
}

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

package io.spine.server.event.given;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.spine.base.EventMessage;
import io.spine.core.Command;
import io.spine.core.CommandContext;
import io.spine.server.command.AbstractCommandHandler;
import io.spine.server.command.Assign;
import io.spine.server.command.CommandHistory;
import io.spine.server.event.EventBus;
import io.spine.server.event.EventDispatcher;
import io.spine.server.integration.ExternalMessageDispatcher;
import io.spine.server.tuple.Pair;
import io.spine.server.type.CommandEnvelope;
import io.spine.server.type.EventClass;
import io.spine.server.type.EventEnvelope;
import io.spine.test.command.CmdAddTask;
import io.spine.test.command.CmdCreateProject;
import io.spine.test.command.CmdCreateTask;
import io.spine.test.command.CmdStartProject;
import io.spine.test.command.ProjectId;
import io.spine.test.command.TaskId;
import io.spine.test.command.event.CmdProjectCreated;
import io.spine.test.command.event.CmdProjectStarted;
import io.spine.test.command.event.CmdTaskAdded;
import io.spine.test.command.event.CmdTaskAssigned;
import io.spine.test.command.event.CmdTaskStarted;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.util.Exceptions.unsupported;

public class CommandHandlerTestEnv {

    /** Prevents instantiation of this utility class. */
    private CommandHandlerTestEnv() {
    }

    public static final class EventCatcher implements EventDispatcher<String> {

        private final List<EventEnvelope> dispatched = newLinkedList();

        @Override
        public Set<EventClass> messageClasses() {
            return EventClass.setOf(
                    CmdProjectStarted.class,
                    CmdTaskAssigned.class,
                    CmdTaskStarted.class
            );
        }

        @Override
        public Set<EventClass> externalEventClasses() {
            return ImmutableSet.of();
        }

        @Override
        public Optional<ExternalMessageDispatcher<String>> createExternalDispatcher() {
            throw unsupported();
        }

        @Override
        public Set<String> dispatch(EventEnvelope event) {
            dispatched.add(event);
            return identity();
        }

        @Override
        public void onError(EventEnvelope event, RuntimeException exception) {
            // Do nothing.
        }

        @SuppressWarnings("ReturnOfCollectionOrArrayField") // OK for tests.
        public List<EventEnvelope> getDispatched() {
            return dispatched;
        }
    }

    public static class TestCommandHandler extends AbstractCommandHandler {

        private final ImmutableList<EventMessage> eventsOnStartProjectCmd =
                createEventsOnStartProjectCmd();

        private final CommandHistory commandsHandled = new CommandHistory();

        private @Nullable CommandEnvelope lastErrorEnvelope;
        private @Nullable RuntimeException lastException;

        public TestCommandHandler(EventBus eventBus) {
            super(eventBus);
        }

        public void assertHandled(Command expected) {
            commandsHandled.assertHandled(expected);
        }

        @SuppressWarnings("CheckReturnValue")
        // Can ignore the returned ID of the command handler in these tests.
        public void handle(Command cmd) {
            CommandEnvelope commandEnvelope = CommandEnvelope.of(cmd);
            dispatch(commandEnvelope);
        }

        public ImmutableList<EventMessage> getEventsOnStartProjectCmd() {
            return eventsOnStartProjectCmd;
        }

        @Override
        @VisibleForTesting
        public Logger log() {
            return super.log();
        }

        @Assign
        CmdProjectCreated handle(CmdCreateProject msg, CommandContext context) {
            commandsHandled.add(msg, context);
            return CmdProjectCreated.getDefaultInstance();
        }

        @Assign
        CmdTaskAdded handle(CmdAddTask msg, CommandContext context) {
            commandsHandled.add(msg, context);
            return CmdTaskAdded.getDefaultInstance();
        }

        @Assign
        List<EventMessage> handle(CmdStartProject msg, CommandContext context) {
            commandsHandled.add(msg, context);
            return eventsOnStartProjectCmd;
        }

        @Assign
        Pair<CmdTaskAssigned, Optional<CmdTaskStarted>>
        handle(CmdCreateTask msg, CommandContext context) {
            commandsHandled.add(msg, context);
            return createEventsOnCreateTaskCmd(msg);
        }

        @Override
        public void onError(CommandEnvelope envelope, RuntimeException exception) {
            super.onError(envelope, exception);
            lastErrorEnvelope = envelope;
            lastException = exception;
        }

        public @Nullable CommandEnvelope getLastErrorEnvelope() {
            return lastErrorEnvelope;
        }

        public @Nullable RuntimeException getLastException() {
            return lastException;
        }

        private ImmutableList<EventMessage> createEventsOnStartProjectCmd() {
            ProjectId id = ProjectId
                    .newBuilder()
                    .setId(getId())
                    .build();
            CmdProjectStarted startedEvent = CmdProjectStarted
                    .newBuilder()
                    .setProjectId(id)
                    .build();
            CmdProjectStarted defaultEvent = CmdProjectStarted.getDefaultInstance();
            return ImmutableList.of(startedEvent, defaultEvent);
        }

        private static Pair<CmdTaskAssigned, Optional<CmdTaskStarted>>
        createEventsOnCreateTaskCmd(CmdCreateTask msg) {
            TaskId taskId = msg.getTaskId();
            CmdTaskAssigned cmdTaskAssigned = CmdTaskAssigned
                    .newBuilder()
                    .setTaskId(taskId)
                    .build();
            CmdTaskStarted cmdTaskStarted = msg.getStart()
                                            ? CmdTaskStarted
                                                    .newBuilder()
                                                    .setTaskId(taskId)
                                                    .build()
                                            : null;
            return Pair.withNullable(cmdTaskAssigned, cmdTaskStarted);
        }
    }
}

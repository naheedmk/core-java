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

package io.spine.server.commandbus;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Any;
import io.grpc.stub.StreamObserver;
import io.spine.base.EventMessage;
import io.spine.base.Identifier;
import io.spine.core.Ack;
import io.spine.core.Command;
import io.spine.core.CommandId;
import io.spine.core.Origin;
import io.spine.core.Status;
import io.spine.core.TenantId;
import io.spine.grpc.DelegatingObserver;
import io.spine.server.type.CommandEnvelope;
import io.spine.system.server.SystemWriteSide;
import io.spine.system.server.event.CommandAcknowledged;
import io.spine.system.server.event.CommandErrored;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static io.spine.system.server.WriteSideFunction.delegatingTo;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * A {@link StreamObserver} for {@link io.spine.core.Command Command}
 * {@linkplain Ack acknowledgement}.
 *
 * <p>Posts a system command whenever a command is acknowledged or errored.
 *
 * <p>{@code CommandAckMonitor} is designed to wrap instances of {@link StreamObserver}.
 * All the calls to {@link StreamObserver} methods on an instance of {@code CommandAckMonitor}
 * invoke respective methods on a {@code delegate} instance.
 */
final class CommandAckMonitor extends DelegatingObserver<Ack> {

    private final SystemWriteSide writeSide;
    private final ImmutableMap<CommandId, Command> commands;

    private CommandAckMonitor(Builder builder) {
        super(builder.delegate);
        this.writeSide = delegatingTo(builder.systemWriteSide).get(builder.tenantId);
        this.commands = builder
                .commands
                .stream()
                .collect(toImmutableMap(Command::id, c -> c));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Posts either {@link CommandAcknowledged} or {@link CommandErrored} system
     * event depending on the value of the given {@code Ack}.
     *
     * @param value
     */
    @Override
    public void onNext(Ack value) {
        super.onNext(value);
        postSystemEvent(value);
    }

    private void postSystemEvent(Ack ack) {
        Status status = ack.getStatus();
        CommandId commandId = commandIdFrom(ack);
        EventMessage systemEvent = systemEventFor(status, commandId);
        Command command = commands.get(commandId);
        checkState(command != null, "Unknown command ID encountered: `%s`.", commandId.value());
        Origin systemEventOrigin = CommandEnvelope.of(command)
                                                  .asMessageOrigin();
        writeSide.postEvent(systemEvent, systemEventOrigin);
    }

    private static CommandId commandIdFrom(Ack ack) {
        Any messageId = ack.getMessageId();
        return Identifier.unpack(messageId, CommandId.class);
    }

    @SuppressWarnings("EnumSwitchStatementWhichMissesCases") // Default values.
    private static EventMessage systemEventFor(Status status, CommandId commandId) {
        switch (status.getStatusCase()) {
            case OK:
                return CommandAcknowledged.newBuilder()
                                          .setId(commandId)
                                          .build();
            case ERROR:
                return CommandErrored.newBuilder()
                                     .setId(commandId)
                                     .setError(status.getError())
                                     .build();
            case REJECTION:
            default:
                throw newIllegalArgumentException(
                        "Command `%s` has invalid status `%s`.",
                        commandId.getUuid(),
                        status.getStatusCase()
                );
        }
    }

    /**
     * Creates a new instance of {@code Builder} for {@code CommandAckMonitor} instances.
     *
     * @return new instance of {@code Builder}
     */
    static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder for the {@code CommandAckMonitor} instances.
     */
    static final class Builder {

        private StreamObserver<Ack> delegate;
        private TenantId tenantId;
        private SystemWriteSide systemWriteSide;
        private ImmutableSet<Command> commands;

        /**
         * Prevents direct instantiation.
         */
        private Builder() {
        }

        /**
         * Sets the {@link StreamObserver} to delegate calls to.
         */
        Builder setDelegate(StreamObserver<Ack> delegate) {
            this.delegate = checkNotNull(delegate);
            return this;
        }

        /**
         * Sets the ID of a tenant who owns the observed commands.
         */
        Builder setTenantId(TenantId tenantId) {
            this.tenantId = checkNotNull(tenantId);
            return this;
        }

        /**
         * Sets the {@link SystemWriteSide} to post system commands into.
         */
        Builder setSystemWriteSide(SystemWriteSide systemWriteSide) {
            this.systemWriteSide = checkNotNull(systemWriteSide);
            return this;
        }

        /**
         * Sets the commands being posted into the command bus.
         *
         * <p>The resulting monitor is able to observe only outcomes of these commands.
         */
        Builder setPostedCommands(ImmutableSet<Command> commands) {
            this.commands = checkNotNull(commands);
            return this;
        }

        /**
         * Creates a new instance of {@code CommandAckMonitor}.
         *
         * @return new instance of {@code CommandAckMonitor}
         */
        CommandAckMonitor build() {
            checkNotNull(delegate);
            checkNotNull(tenantId);
            checkNotNull(systemWriteSide);
            checkNotNull(commands);

            return new CommandAckMonitor(this);
        }
    }
}

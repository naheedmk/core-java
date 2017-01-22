/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package org.spine3.test;

import com.google.common.base.Optional;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import org.spine3.base.Command;
import org.spine3.base.CommandContext;
import org.spine3.client.CommandFactory;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spine3.test.Tests.adjustTimestamp;

/**
 * An abstract base for testing entities handling commands.
 *
 * @param <C> the type of the command message to test
 * @author Alexander Yevsyukov
 */
public abstract class CommandTest<C extends Message> {

    private final CommandFactory commandFactory;

    @Nullable
    private C commandMessage;

    @Nullable
    private Command command;

    /**
     * Creates instance with the passed {@code CommandFactory}.
     */
    protected CommandTest(CommandFactory commandFactory) {
        this.commandFactory = checkNotNull(commandFactory);
    }

    /**
     * Creates new instance with {@link TestCommandFactory}.
     */
    protected CommandTest() {
        this.commandFactory = TestCommandFactory.newInstance(getClass());
    }

    /**
     * Creates a new command with the passed message and remembers it.
     *
     * <p>The created command and its content can be obtained via {@link #command()},
     * {@link #commandMessage()}, {@link #commandContext()}.
     *
     * <p>Subsequent call to this method will create another command and
     * overwrite the previously stored one.
     *
     * @return created command instance
     */
    protected Command createCommand(C commandMessage) {
        this.commandMessage = checkNotNull(commandMessage);
        this.command = commandFactory.create(commandMessage);
        return this.command;
    }

    protected Command createAnotherCommand(Message commandMessage) {
        return commandFactory.create(checkNotNull(commandMessage));
    }

    /**
     * Creates a command with the passed message and timestamp and remembers it.
     *
     * <p>The created command and its content can be obtained via {@link #command()},
     * {@link #commandMessage()}, {@link #commandContext()}.
     *
     * <p>Subsequent call to this method will create another command and
     * overwrite the previously stored one.
     *
     * @return created command instance
     */
    protected Command createCommand(C commandMessage, Timestamp timestamp) {
        this.commandMessage = checkNotNull(commandMessage);
        this.command = adjustTimestamp(commandFactory.create(commandMessage), checkNotNull(timestamp));
        return this.command;
    }

    /**
     * Obtains remembered command message or empty {@code Optional} if
     * none of the {@code createCommand()} was called before.
     */
    protected Optional<C> commandMessage() {
        return Optional.fromNullable(commandMessage);
    }

    /**
     * Obtains remembered command context or empty {@code Optional} if
     * none of the {@code createCommand()} was called before.
     */
    protected Optional<CommandContext> commandContext() {
        if (command().isPresent()) {
            return Optional.of(command().get().getContext());
        }
        return Optional.absent();
    }

    /**
     * Obtains remembered command or empty {@code Optional} if
     * none of the {@code createCommand()} was called before.
     */
    protected Optional<Command> command() {
        return Optional.fromNullable(command);
    }
}

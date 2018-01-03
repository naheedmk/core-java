/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.server.command;

import com.google.common.base.Optional;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.spine.client.ActorRequestFactory;
import io.spine.client.TestActorRequestFactory;
import io.spine.core.ActorContext;
import io.spine.core.Command;
import io.spine.core.CommandContext;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An abstract base for testing entities handling commands.
 *
 * <p>It is expected that a test suite derived from this class tests that:
 * <ol>
 *     <li>correct events are generated by the handling method
 *     <li>state of the handling object is modified as the result of handling the command
 *     <li>rejections (if declared) are generated and are correctly populated
 * </ol>
 *
 * @param <C> the type of the command message to test
 * @author Alexander Yevsyukov
 */
public abstract class CommandTest<C extends Message> {

    private final ActorRequestFactory requestFactory;

    @Nullable
    private C commandMessage;

    @Nullable
    private Command command;

    /**
     * Creates instance with the passed {@code ActorRequestFactory}.
     */
    protected CommandTest(ActorRequestFactory requestFactory) {
        this.requestFactory = checkNotNull(requestFactory);
    }

    /**
     * Creates new instance with {@link TestActorRequestFactory}.
     */
    protected CommandTest() {
        this.requestFactory = TestActorRequestFactory.newInstance(getClass());
    }

    /**
     * Adjusts a timestamp in the context of the passed command.
     *
     * @return new command instance with the modified timestamp
     */
    private static Command adjustTimestamp(Command command, Timestamp timestamp) {
        final CommandContext context = command.getContext();
        final ActorContext.Builder withTime = context.getActorContext()
                                                     .toBuilder()
                                                     .setTimestamp(timestamp);
        final Command.Builder commandBuilder =
                command.toBuilder()
                       .setContext(context.toBuilder()
                                          .setActorContext(withTime));
        return commandBuilder.build();
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
        this.command = requestFactory.command().create(commandMessage);
        return this.command;
    }

    /**
     * Creates a command with the passed message.
     *
     * <p>Use this method for creating commands of types different than
     * one which is the subject of the test suite (defined by the generic type {@code <C>}.
     *
     * @param commandMessage the message of the command to create
     * @return new command instance
     */
    protected Command createDifferentCommand(Message commandMessage) {
        return requestFactory.command().create(checkNotNull(commandMessage));
    }

    /**
     * Creates a command with the passed message with the given timestamp.
     *
     * <p>Use this method for creating commands of types different than
     * one which is the subject of the test suite (defined by the generic type {@code <C>}.
     *
     * @param commandMessage the message of the command to create
     * @param timestamp the moment in time at which the command was created
     * @return new command instance
     */
    protected Command createDifferentCommand(Message commandMessage, Timestamp timestamp) {
        return adjustTimestamp(createDifferentCommand(commandMessage), timestamp);
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
        this.command = adjustTimestamp(requestFactory.command().create(commandMessage),
                                       checkNotNull(timestamp));
        return this.command;
    }

    /**
     * Obtains remembered command message or empty {@code Optional} if
     * none of the {@code Command()} was called before.
     */
    protected Optional<C> commandMessage() {
        return Optional.fromNullable(commandMessage);
    }

    /**
     * Obtains remembered command context or empty {@code Optional} if
     * none of the {@code Command()} was called before.
     */
    protected Optional<CommandContext> commandContext() {
        if (command().isPresent()) {
            return Optional.of(command().get().getContext());
        }
        return Optional.absent();
    }

    /**
     * Obtains remembered command or empty {@code Optional} if
     * none of the {@code Command()} was called before.
     */
    protected Optional<Command> command() {
        return Optional.fromNullable(command);
    }

    /**
     * Implement this method to create and store the reference to the object
     * which handles the command we test.
     *
     * <p>This method must be called in derived test suites in methods
     * annotated with {@code @Before} (JUnit 4) or {@code @BeforeEach} (JUnit 5).
     */
    protected abstract void setUp();
}

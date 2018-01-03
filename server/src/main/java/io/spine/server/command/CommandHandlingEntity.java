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

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import io.spine.Identifier;
import io.spine.base.ThrowableMessage;
import io.spine.change.MessageMismatch;
import io.spine.change.StringMismatch;
import io.spine.change.ValueMismatch;
import io.spine.core.CommandContext;
import io.spine.core.CommandEnvelope;
import io.spine.core.Rejections;
import io.spine.server.entity.EventPlayingEntity;
import io.spine.validate.ValidatingBuilder;

import java.util.List;

/**
 * An entity that can handle commands.
 *
 * <h2>Command handling methods</h2>
 *
 * <p>A command handling method is a {@code public} method that accepts two parameters.
 * The first parameter is a command message of an <strong>exact</strong> type
 * derived from {@code Message}
 * The second (optional) parameter is {@link CommandContext}.
 *
 * <p>The method returns an event message of the specific type, or {@code List} of messages
 * if it produces more than one event.
 *
 * <p>The method may throw one or more throwables derived from
 * {@link io.spine.base.ThrowableMessage ThrowableMessage}.
 * Throwing a {@code ThrowableMessage} indicates that the passed command cannot be handled
 * because of a {@linkplain Rejections#toRejection(ThrowableMessage, io.spine.core.Command)
 * rejection}.
 *
 * @author Alexander Yevsyukov
 */
public abstract
class CommandHandlingEntity<I,
                            S extends Message,
                            B extends ValidatingBuilder<S, ? extends Message.Builder>>
      extends EventPlayingEntity<I, S, B> {

    /** Cached value of the ID in the form of {@code Any} instance. */
    private final Any idAsAny;

    /**
     * Creates a new entity with the passed ID.
     */
    protected CommandHandlingEntity(I id) {
        super(id);
        this.idAsAny = Identifier.pack(id);
    }

    protected Any getProducerId() {
        return idAsAny;
    }

    /**
     * Dispatches the passed command to appropriate handler.
     *
     * @param  cmd the envelope with the command to handle
     * @return event messages generated by the handler
     */
    protected abstract List<? extends Message> dispatchCommand(CommandEnvelope cmd);

    /*
     * Helper methods for producing `ValueMismatch`es in command handling methods
     ******************************************************************************/

    /**
     * Creates {@code ValueMismatch} for the case of discovering a non-default value,
     * when the default value was expected by a command.
     *
     * @param  actual   the value discovered instead of the default value
     * @param  newValue the new value requested in the command
     * @return new {@code ValueMismatch} instance
     */
    protected ValueMismatch expectedDefault(Message actual, Message newValue) {
        return MessageMismatch.expectedDefault(actual, newValue, versionNumber());
    }

    /**
     * Creates a {@code ValueMismatch} for a command that wanted to <em>clear</em> a value,
     * but discovered that the field already has the default value.
     *
     * @param expected the value of the field that the command wanted to clear
     * @return new {@code ValueMismatch} instance
     */
    protected ValueMismatch expectedNotDefault(Message expected) {
        return MessageMismatch.expectedNotDefault(expected, versionNumber());
    }

    /**
     * Creates a {@code ValueMismatch} for a command that wanted to <em>change</em> a field value,
     * but discovered that the field has the default value.
     *
     * @param expected the value expected by the command
     * @param newValue the value the command wanted to set
     * @return new {@code ValueMismatch} instance
     */
    protected ValueMismatch expectedNotDefault(Message expected, Message newValue) {
        return MessageMismatch.expectedNotDefault(expected, newValue, versionNumber());
    }

    /**
     * Creates {@code ValueMismatch} for the case of discovering a value different
     * than by a command.
     *
     * @param expected the value expected by the command
     * @param actual   the value discovered instead of the expected value
     * @param newValue the new value requested in the command
     * @return new {@code ValueMismatch} instance
     */
    protected ValueMismatch unexpectedValue(Message expected, Message actual, Message newValue) {
        return MessageMismatch.unexpectedValue(expected, actual, newValue, versionNumber());
    }

    /**
     * Creates {@code ValueMismatch} for the case of discovering a non-empty value,
     * when an empty string was expected by a command.
     *
     * @param actual   the value discovered instead of the empty string
     * @param newValue the new value requested in the command
     * @return new {@code ValueMismatch} instance
     */
    protected ValueMismatch expectedEmpty(String actual, String newValue) {
        return StringMismatch.expectedEmpty(actual, newValue, versionNumber());
    }

    /**
     * Creates a {@code ValueMismatch} for a command that wanted to clear a string value,
     * but discovered that the field is already empty.
     *
     * @param expected the value of the field that the command wanted to clear
     * @return new ValueMismatch instance
     */
    protected ValueMismatch expectedNotEmpty(String expected) {
        return StringMismatch.expectedNotEmpty(expected, versionNumber());
    }

    /**
     * Creates {@code ValueMismatch} for the case of discovering a value
     * different than expected by a command.
     *
     * @param expected the value expected by the command
     * @param actual   the value discovered instead of the expected string
     * @param newValue the new value requested in the command
     * @return new {@code ValueMismatch} instance
     */
    protected ValueMismatch unexpectedValue(String expected, String actual, String newValue) {
        return StringMismatch.unexpectedValue(expected, actual, newValue, versionNumber());
    }
}

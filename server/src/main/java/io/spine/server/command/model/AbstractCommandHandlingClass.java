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

package io.spine.server.command.model;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.Immutable;
import io.spine.server.model.HandlerMap;
import io.spine.server.model.ModelClass;
import io.spine.server.type.CommandClass;
import io.spine.server.type.EventClass;
import io.spine.type.MessageClass;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

/**
 * Abstract base for classes providing message handling information of classes that handle commands.
 *
 * @param <C>
 *         the type of a command handling class
 * @param <R>
 *         the type of the class of produced messages
 * @param <H>
 *         the type of methods performing the command handle
 */
@Immutable(containerOf = "H")
public abstract class AbstractCommandHandlingClass<C,
                                                   R extends MessageClass<?>,
                                                   H extends CommandAcceptingMethod<?, R>>
        extends ModelClass<C>
        implements CommandHandlingClass {

    private static final long serialVersionUID = 0L;

    private final HandlerMap<CommandClass, R, H> commands;

    AbstractCommandHandlingClass(Class<? extends C> cls,
                                 CommandAcceptingSignature<H> signature) {
        super(cls);
        this.commands = HandlerMap.create(cls, signature);
    }

    @Override
    public ImmutableSet<CommandClass> commands() {
        return commands.messageClasses();
    }

    @Override
    public ImmutableSet<R> commandOutput() {
        return commands.producedTypes();
    }

    @Override
    public ImmutableSet<EventClass> rejections() {
        ImmutableSet<EventClass> result =
                commands.methods()
                        .stream()
                        .map(CommandAcceptingMethod::rejections)
                        .flatMap(ImmutableSet::stream)
                        .collect(toImmutableSet());
        return result;
    }

    /** Obtains the handler method for the passed command class. */
    @Override
    public H handlerOf(CommandClass commandClass) {
        return commands.handlerOf(commandClass);
    }

    boolean contains(CommandClass commandClass) {
        return commands.containsClass(commandClass);
    }
}

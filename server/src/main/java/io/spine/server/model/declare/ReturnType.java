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

package io.spine.server.model.declare;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Message;
import io.spine.base.CommandMessage;
import io.spine.base.EventMessage;
import io.spine.server.model.Nothing;
import io.spine.server.tuple.Pair;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;

import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * A wrapper around method's return type which knows how to get emitted messages (events and
 * commands) of the method.
 */
public enum ReturnType {

    VOID(void.class) {
        @Override
        protected ImmutableSet<Class<? extends Message>> gatherEmittedMessages(Method method) {
            return ImmutableSet.of();
        }
    },

    NOTHING(Nothing.class) {
        @Override
        protected ImmutableSet<Class<? extends Message>> gatherEmittedMessages(Method method) {
            return ImmutableSet.of();
        }
    },

    COMMAND_MESSAGE(CommandMessage.class) {
        @Override
        protected ImmutableSet<Class<? extends Message>> gatherEmittedMessages(Method method) {
            return ImmutableSet.of();
        }
    },

    EVENT_MESSAGE(EventMessage.class, NOTHING) {
        @Override
        protected ImmutableSet<Class<? extends Message>> gatherEmittedMessages(Method method) {
            return ImmutableSet.of();
        }
    },

    OPTIONAL(Optional.class) {
        @Override
        protected ImmutableSet<Class<? extends Message>> gatherEmittedMessages(Method method) {
            return ImmutableSet.of();
        }
    },

    PAIR(Pair.class) {
        @Override
        protected ImmutableSet<Class<? extends Message>> gatherEmittedMessages(Method method) {
            return ImmutableSet.of();
        }
    },

    ITERABLE(Iterable.class, PAIR) {
        @Override
        protected ImmutableSet<Class<? extends Message>> gatherEmittedMessages(Method method) {
            return ImmutableSet.of();
        }
    };

    private final Class<?> returnType;
    private final ImmutableSet<ReturnType> specialCases;

    ReturnType(Class<?> type, ReturnType... specialCases) {
        returnType = type;
        this.specialCases = ImmutableSet.copyOf(specialCases);
    }

    Optional<ReturnType> getMatching(Class<?> methodReturnType) {
        Optional<ReturnType> specialCase = specialCases
                .stream()
                .filter(type -> type.matches(methodReturnType))
                .findFirst();
        if (specialCase.isPresent()) {
            return specialCase;
        }
        Optional<ReturnType> result = matches(methodReturnType)
                                      ? Optional.of(this)
                                      : Optional.empty();
        return result;
    }

    private boolean matches(Class<?> methodReturnType) {
        return returnType.isAssignableFrom(methodReturnType);
    }

    ImmutableSet<Class<? extends Message>> emittedMessages(Method method) {
        Class<?> returnType = method.getReturnType();
        ImmutableSet<Class<? extends Message>> result = getMatching(returnType)
                .orElseThrow(() -> signatureMismatchError(method))
                .gatherEmittedMessages(method);
        return result;
    }

    private IllegalArgumentException signatureMismatchError(Method method) {
        return newIllegalArgumentException(
                "Trying to gather emitted messages for method %s on mismatching return type %s",
                method.getName(),
                this.name()
        );
    }

    protected abstract ImmutableSet<Class<? extends Message>> gatherEmittedMessages(Method method);

    static Optional<ReturnType> findMatching(Method method, Collection<ReturnType> types) {
        Class<?> returnType = method.getReturnType();
        Optional<ReturnType> result = types
                .stream()
                .map(type -> type.getMatching(returnType))
                .filter(Optional::isPresent)
                .findFirst()
                .orElseGet(Optional::empty);
        return result;
    }
}

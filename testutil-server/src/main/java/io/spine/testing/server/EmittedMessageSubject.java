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

package io.spine.testing.server;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.Subject;
import io.spine.base.SerializableMessage;
import io.spine.core.MessageWithContext;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import static com.google.common.truth.Truth.assertThat;

/**
 * Abstract base for subjects checking messages, such as events or commands, generated by
 * a Bounded Context under the test.
 *
 * @param <S>
 *          the self-type for return type covariance
 * @param <T>
 *          the type of the outer objects of the checked messages,
 *          such as {@link io.spine.core.Command Command} or {@link io.spine.core.Event Event}
 * @param <M>
 *          the type of emitted messages, such as {@link io.spine.base.CommandMessage CommandMessage}
 *          or {@link io.spine.base.EventMessage EventMessage}.
 */
public abstract class EmittedMessageSubject<S extends EmittedMessageSubject<S, T, M>,
                                            T extends MessageWithContext,
                                            M extends SerializableMessage>
        extends Subject<S, Iterable<T>> {

    protected EmittedMessageSubject(FailureMetadata metadata, @NullableDecl Iterable<T> actual) {
        super(metadata, actual);
    }

    /** Fails if the subject does not have the given size. */
    public final void hasSize(int expectedSize) {
        assertActual().hasSize(expectedSize);
    }

    /** Fails if the subject is not empty. */
    public final void isEmpty() {
        assertActual().isNotEmpty();
    }

    /** Fails if the subject is empty. */
    public final void isNotEmpty() {
        assertActual().isNotEmpty();
    }

    private IterableSubject assertActual() {
        return assertThat(actual());
    }
}

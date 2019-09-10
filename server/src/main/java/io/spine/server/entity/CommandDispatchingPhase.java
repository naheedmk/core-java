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

package io.spine.server.entity;

import io.spine.annotation.Internal;
import io.spine.core.Signal;
import io.spine.core.SignalId;
import io.spine.server.command.DispatchCommand;
import io.spine.server.dispatch.DispatchOutcome;

/**
 * A phase that dispatched a command to the entity in transaction.
 *
 * @param <I>
 *         the type of entity ID
 */
@Internal
public final class CommandDispatchingPhase<I> extends Phase<I> {

    private final DispatchCommand<I> dispatch;

    public CommandDispatchingPhase(Transaction<I, ?, ?, ?> transaction,
                                   DispatchCommand<I> dispatch,
                                   VersionIncrement versionIncrement) {
        super(transaction, versionIncrement);
        this.dispatch = dispatch;
    }

    @Override
    protected DispatchOutcome performDispatch() {
        return dispatch.perform();
    }

    @Override
    public I entityId() {
        return dispatch.entity()
                       .id();
    }

    @Override
    public SignalId messageId() {
        return dispatch.command()
                       .id();
    }

    @Override
    protected Signal<?, ?, ?> signal() {
        return dispatch.command()
                       .outerObject();
    }
}
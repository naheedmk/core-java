/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.system.server;

import io.spine.base.CommandMessage;
import io.spine.base.EventMessage;
import io.spine.core.TenantId;
import io.spine.server.tenant.TenantFunction;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A {@link SystemWriteSide} which memoizes the posted system commands.
 *
 * <p>This class is a test-only facility, used in order to avoid mocking {@link SystemWriteSide}
 * instances.
 */
public final class MemoizingWriteSide implements SystemWriteSide {

    private @MonotonicNonNull MemoizedSystemMessage lastSeenCommand;
    private @MonotonicNonNull MemoizedSystemMessage lastSeenEvent;

    private final boolean multitenant;

    private MemoizingWriteSide(boolean multitenant) {
        this.multitenant = multitenant;
    }

    /**
     * Creates a new instance of {@code MemoizingWriteSide} for a single-tenant execution environment.
     *
     * @return new {@code MemoizingWriteSide}
     */
    public static MemoizingWriteSide singleTenant() {
        return new MemoizingWriteSide(false);
    }

    /**
     * Creates a new instance of {@code MemoizingWriteSide} for a multitenant execution environment.
     *
     * @return new {@code MemoizingWriteSide}
     */
    public static MemoizingWriteSide multitenant() {
        return new MemoizingWriteSide(true);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Memoizes the given command message and the {@link TenantId} which it was posted for.
     *
     * @see #lastSeenCommand()
     */
    @Override
    public void postCommand(CommandMessage systemCommand) {
        TenantId tenantId = currentTenant();
        lastSeenCommand = new MemoizedSystemMessage(systemCommand, tenantId);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Memoizes the given event message and the {@link TenantId} which it was posted for.
     *
     * @see #lastSeenEvent()
     */
    @Override
    public void postEvent(EventMessage systemEvent) {
        TenantId tenantId = currentTenant();
        lastSeenEvent = new MemoizedSystemMessage(systemEvent, tenantId);
    }

    /** Obtains the ID of the current tenant. */
    private TenantId currentTenant() {
        TenantId result = new TenantFunction<TenantId>(multitenant) {
            @Override
            public TenantId apply(TenantId id) {
                return id;
            }
        }.execute();
        return checkNotNull(result);
    }

    /**
     * Obtains the last command message posted to {@link SystemWriteSide}.
     *
     * <p>Fails if no commands were posted yet.
     */
    public MemoizedSystemMessage lastSeenCommand() {
        assertNotNull(lastSeenCommand);
        return lastSeenCommand;
    }

    /**
     * Obtains the last event message posted to {@link SystemWriteSide}.
     *
     * <p>Fails if no events were posted yet.
     */
    public MemoizedSystemMessage lastSeenEvent() {
        assertNotNull(lastSeenEvent);
        return lastSeenEvent;
    }
}
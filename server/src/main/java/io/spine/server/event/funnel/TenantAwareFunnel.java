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

package io.spine.server.event.funnel;

import io.grpc.stub.StreamObserver;
import io.spine.base.EventMessage;
import io.spine.core.Ack;
import io.spine.core.TenantId;
import io.spine.server.tenant.TenantAwareRunner;

import static com.google.common.base.Preconditions.checkNotNull;

final class TenantAwareFunnel implements EventFunnel {

    private final EventFunnel delegate;
    private final TenantId tenantId;

    TenantAwareFunnel(EventFunnel delegate, TenantId tenantId) {
        this.delegate = checkNotNull(delegate);
        this.tenantId = checkNotNull(tenantId);
    }

    @Override
    public EventFunnel with(StreamObserver<Ack> resultObserver) {
        return new TenantAwareFunnel(delegate.with(resultObserver), tenantId);
    }

    @Override
    public EventFunnel forTenant(TenantId tenantId) {
        checkNotNull(tenantId);
        return new TenantAwareFunnel(delegate, tenantId);
    }

    @Override
    public void post(EventMessage... events) {
        TenantAwareRunner.with(tenantId)
                         .run(() -> delegate.post(events));
    }
}

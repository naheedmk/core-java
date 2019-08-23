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

package io.spine.server.delivery.given;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.spine.server.projection.ProjectionRepository;
import io.spine.server.route.EventRouting;
import io.spine.test.delivery.NumberAdded;
import io.spine.test.delivery.NumberImported;
import io.spine.test.delivery.StatFunnel;

/**
 * A repository of {@link StatFunnelProjection}.
 */
public class StatFunnelRepository
        extends ProjectionRepository<String, StatFunnelProjection, StatFunnel> {

    public static final String THE_ONLY_INSTANCE = "SINGLETON_FUNNEL";
    private static final ImmutableSet<String> FUNNEL = ImmutableSet.of(THE_ONLY_INSTANCE);

    @OverridingMethodsMustInvokeSuper
    @Override
    protected void setupEventRouting(EventRouting<String> routing) {
        super.setupEventRouting(routing);
        routing.route(NumberImported.class, (e, ctx) -> FUNNEL);
        routing.route(NumberAdded.class, (e, ctx) -> FUNNEL);
    }
}

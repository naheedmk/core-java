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

import io.spine.core.Subscribe;
import io.spine.server.projection.Projection;
import io.spine.string.Stringifiers;
import io.spine.test.delivery.NumberAdded;
import io.spine.test.delivery.NumberImported;
import io.spine.test.delivery.StatFunnel;

/**
 * A projection which gathers all the events in {@link io.spine.server.delivery.DeliveryTest
 * Delivery tests}.
 */
public class StatFunnelProjection extends Projection<String, StatFunnel, StatFunnel.Builder> {

    @Subscribe
    void on(NumberImported e) {
        if (builder().getImportedList()
                     .contains(e)) {
            throw new RuntimeException("A duplicate `NumberImported` event delivered: " +
                                               Stringifiers.toString(e));
        }
        builder().addImported(e);
    }

    @Subscribe
    void on(NumberAdded e) {
        if (builder().getAddedList()
                     .contains(e)) {
            throw new RuntimeException("A duplicate `NumberAdded` event delivered: " +
                                               Stringifiers.toString(e));
        }

        builder().addAdded(e);
    }
}

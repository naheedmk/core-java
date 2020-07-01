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

package io.spine.server.delivery;

import io.spine.server.storage.Storage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests on the default values set in the created {@code Delivery} instance.
 */
@DisplayName("`Delivery` configured by default should")
class DeliveryDefaultsTest {

    @Test
    @DisplayName("initialize the `InboxStorage` in a single-tenant mode")
    void createSingleTenantInboxStorage() {
        InboxStorage storage = defaultDelivery().inboxStorage();
        assertSingleTenant(storage);
    }

    @Test
    @DisplayName("initialize the `CatchUp` in a single-tenant mode")
    void createSingleTenantCatchUpStorage() {
        CatchUpStorage storage = defaultDelivery().catchUpStorage();
        assertSingleTenant(storage);
    }

    private static Delivery defaultDelivery() {
        return Delivery.newBuilder()
                       .build();
    }

    private static void assertSingleTenant(Storage<?, ?> storage) {
        assertThat(storage).isNotNull();
        assertThat(storage.isMultitenant()).isFalse();
    }
}
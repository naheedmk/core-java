/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package org.spine3.testdata;

import org.spine3.server.BoundedContext;
import org.spine3.server.commandbus.CommandBus;
import org.spine3.server.event.EventBus;
import org.spine3.server.event.enrich.EventEnricher;
import org.spine3.server.stand.Stand;
import org.spine3.server.storage.StorageFactorySwitch;

/**
 * Creates stubs with instances of {@link BoundedContext} for testing purposes.
 *
 * @author Alexander Yevsyukov
 */
@SuppressWarnings("UtilityClass")
public class TestBoundedContextFactory {

    private TestBoundedContextFactory() {
    }

    public static class SingleTenant {

        private SingleTenant() {
            // Prevent instantiation of this utility class.
        }

        public static BoundedContext newBoundedContext(Stand stand) {
            return BoundedContext.newBuilder()
                                 .setStand(stand)
                                 .build();
        }
    }

    public static class MultiTenant {

        private MultiTenant() {
            // Prevent instantiation of this utility class.
        }

        private static BoundedContext.Builder newBuilder() {
            return BoundedContext.newBuilder()
                                 .setMultitenant(true);
        }

        public static BoundedContext newBoundedContext() {
            return newBuilder().build();
        }

        public static BoundedContext newBoundedContext(EventBus eventBus) {
            return newBuilder()
                    .setEventBus(eventBus)
                    .build();
        }

        public static BoundedContext newBoundedContext(CommandBus commandBus) {
            return newBuilder()
                    .setCommandBus(commandBus)
                    .build();
        }

        public static BoundedContext newBoundedContext(String name, Stand stand) {
            return newBuilder()
                    .setStand(stand)
                    .setName(name)
                    .build();
        }

        public static BoundedContext newBoundedContext(EventEnricher enricher) {
            final EventBus eventBus = EventBus.newBuilder()
                                              .setEnricher(enricher)
                                              .setStorageFactory(
                                                      StorageFactorySwitch.getInstance(true)
                                                                          .get())
                                              .build();
            return newBoundedContext(eventBus);
        }

        public static BoundedContext newBoundedContext(CommandBus commandBus, EventBus eventBus) {
            return newBuilder()
                    .setCommandBus(commandBus)
                    .setEventBus(eventBus)
                    .build();
        }
    }
}

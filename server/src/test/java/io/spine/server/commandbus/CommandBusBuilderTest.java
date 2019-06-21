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

package io.spine.server.commandbus;

import io.spine.core.Command;
import io.spine.server.bus.BusBuilderTest;
import io.spine.server.event.EventBus;
import io.spine.server.storage.memory.InMemoryStorageFactory;
import io.spine.server.tenant.TenantIndex;
import io.spine.server.type.CommandEnvelope;
import io.spine.system.server.NoOpSystemWriteSide;
import io.spine.system.server.SystemWriteSide;
import io.spine.testing.Tests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.spine.core.BoundedContextNames.newName;
import static io.spine.testing.server.tenant.TenantAwareTest.createTenantIndex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@SuppressWarnings("DuplicateStringLiteralInspection") // Common test display names.
@DisplayName("CommandBus Builder should")
class CommandBusBuilderTest
        extends BusBuilderTest<CommandBus.Builder, CommandEnvelope, Command> {

    private static final SystemWriteSide SYSTEM_WRITE_SIDE = NoOpSystemWriteSide.INSTANCE;

    private TenantIndex tenantIndex;
    private EventBus eventBus;

    @Override
    protected CommandBus.Builder builder() {
        return CommandBus.newBuilder()
                         .injectSystem(SYSTEM_WRITE_SIDE)
                         .injectTenantIndex(tenantIndex)
                         .injectEventBus(eventBus);
    }

    @BeforeEach
    void setUp() {
        boolean multitenant = true;
        InMemoryStorageFactory storageFactory =
                InMemoryStorageFactory.newInstance(newName(getClass().getSimpleName()),
                                                   multitenant);
        tenantIndex = createTenantIndex(multitenant, storageFactory);
        eventBus = EventBus
                .newBuilder()
                .setStorageFactory(storageFactory)
                .build();
    }

    @Test
    @DisplayName("create new CommandBus instance")
    void createNewInstance() {
        CommandBus commandBus = CommandBus.newBuilder()
                                          .injectTenantIndex(tenantIndex)
                                          .injectSystem(SYSTEM_WRITE_SIDE)
                                          .injectEventBus(eventBus)
                                          .build();
        assertNotNull(commandBus);
    }

    @Test
    @DisplayName("not accept null CommandStore")
    void notAcceptNullCommandStore() {
        assertThrows(NullPointerException.class,
                     () -> builder().injectTenantIndex(Tests.nullRef()));
    }

    @Test
    @DisplayName("not allow to omit setting CommandStore")
    void neverOmitCommandStore() {
        assertThrows(IllegalStateException.class,
                     () -> CommandBus.newBuilder()
                                     .injectSystem(SYSTEM_WRITE_SIDE)
                                     .build());
    }

    @Test
    @DisplayName("not allow to omit setting SystemWriteSide")
    void neverOmitSystem() {
        assertThrows(IllegalStateException.class,
                     () -> CommandBus.newBuilder()
                                     .injectTenantIndex(tenantIndex)
                                     .build());
    }

    @Nested
    @DisplayName("allow to specify")
    class AllowToSpecify {

        @Test
        @DisplayName("CommandScheduler")
        void commandScheduler() {
            CommandScheduler expectedScheduler = mock(CommandScheduler.class);

            CommandBus.Builder builder = builder().setCommandScheduler(expectedScheduler);

            assertTrue(builder.getCommandScheduler()
                              .isPresent());
            assertEquals(expectedScheduler, builder.getCommandScheduler()
                                                   .get());

            CommandBus commandBus = builder.build();
            assertNotNull(commandBus);

            CommandScheduler actualScheduler = commandBus.scheduler();
            assertEquals(expectedScheduler, actualScheduler);
        }

        @Test
        @DisplayName("EventBus")
        void eventBus() {
            EventBus expectedEventBus = mock(EventBus.class);

            CommandBus.Builder builder = builder().injectEventBus(expectedEventBus);
            assertTrue(builder.getEventBus()
                              .isPresent());
            assertEquals(expectedEventBus, builder.getEventBus()
                                                  .get());
        }

        @Test
        @DisplayName("if CommandBus is multitenant")
        void ifIsMultitenant() {
            assertTrue(builder().setMultitenant(true)
                                .isMultitenant());
            assertFalse(builder().setMultitenant(false)
                                 .isMultitenant());
        }

        @Test
        @DisplayName("system write side")
        void system() {
            SystemWriteSide systemWriteSide = mock(SystemWriteSide.class);
            CommandBus.Builder builder = builder().injectSystem(systemWriteSide);
            Optional<SystemWriteSide> actual = builder.system();
            assertTrue(actual.isPresent());
            assertSame(systemWriteSide, actual.get());
        }

        @Test
        @DisplayName("tenant index")
        void tenantIndex() {
            TenantIndex index = mock(TenantIndex.class);
            CommandBus.Builder builder = builder().injectTenantIndex(index);
            Optional<TenantIndex> actual = builder.tenantIndex();
            assertTrue(actual.isPresent());
            assertSame(index, actual.get());
        }
    }
}
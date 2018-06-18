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
package io.spine.server.delivery;

import io.spine.core.BoundedContextName;
import io.spine.server.BoundedContext;
import io.spine.server.delivery.given.ShardedStreamTestEnv.TaskAggregateRepository;
import io.spine.server.model.ModelTests;
import io.spine.test.Tests;
import io.spine.test.aggregate.ProjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.server.delivery.given.ShardedStreamTestEnv.builder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * @author Alex Tymchenko
 */
@SuppressWarnings("unchecked")  // the numerous generic parameters are omitted to simplify tests.
@DisplayName("ShardedStream Builder should")
class ShardedStreamBuilderTest {

    @BeforeEach
    void setUp() {
        // as long as we refer to the Model in delivery tag initialization.
        ModelTests.clearModel();
    }

    @Test
    @DisplayName("not accept null boundedContextName")
    void notAcceptNullContextName() {
        assertThrows(NullPointerException.class,
                     () -> builder().setBoundedContextName(Tests.nullRef()));
    }

    @Test
    @DisplayName("return set boundedContextName")
    void returnSetContextName() {
        BoundedContextName value = BoundedContext.newName("ShardedStreams");
        assertEquals(value, builder().setBoundedContextName(value)
                                     .getBoundedContextName());
    }

    @Test
    @DisplayName("not accept null key")
    void notAcceptNullKey() {
        assertThrows(NullPointerException.class, () -> builder().setKey(Tests.nullRef()));
    }

    @Test
    @DisplayName("return set key")
    void returnSetKey() {
        ShardingKey value = mock(ShardingKey.class);
        assertEquals(value, builder().setKey(value)
                                     .getKey());
    }

    @Test
    @DisplayName("not accept null tag")
    void notAcceptNullTag() {
        assertThrows(NullPointerException.class, () -> builder().setTag(Tests.nullRef()));
    }

    @Test
    @DisplayName("return set tag")
    void returnSetTag() {
        DeliveryTag value = DeliveryTag.forCommandsOf(new TaskAggregateRepository());
        assertEquals(value, builder().setTag(value)
                                     .getTag());
    }

    @Test
    @DisplayName("not accept null targetIdClass")
    void notAcceptNullTargetIdClass() {
        assertThrows(NullPointerException.class,
                     () -> builder().setTargetIdClass(Tests.nullRef()));
    }

    @Test
    @DisplayName("return set targetIdClass")
    void returnSetTargetIdClass() {
        Class value = ProjectId.class;
        assertEquals(value, builder().setTargetIdClass(value)
                                     .getTargetIdClass());
    }

    @Test
    @DisplayName("not accept null consumer")
    void notAcceptNullConsumer() {
        assertThrows(NullPointerException.class,
                     () -> builder().setConsumer(Tests.<Consumer>nullRef()));
    }

    @Test
    @DisplayName("return set consumer")
    void returnSetConsumer() {
        Consumer value = mock(Consumer.class);
        assertEquals(value, builder().setConsumer(value)
                                     .getConsumer());
    }

    @Test
    @DisplayName("not accept null transportFactory")
    void notAcceptNullTransportFactory() {
        assertThrows(NullPointerException.class, () -> builder().build(Tests.nullRef()));
    }
}

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

package io.spine.server.transport.memory;

import com.google.protobuf.Any;
import io.grpc.stub.StreamObserver;
import io.spine.base.Identifier;
import io.spine.server.integration.ExternalMessage;
import io.spine.server.transport.ChannelId;
import io.spine.server.transport.memory.given.SingleThreadInMemSubscriberTestEnv.ThrowingObserver;
import io.spine.testing.logging.MuteLogging;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.base.Identifier.newUuid;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@DisplayName("SingleThreadInMemSubscriber should")
class SingleThreadInMemSubscriberTest {

    /**
     * Wait time to make sure the observer is called.
     */
    private static final int SYNC_TIMEOUT_MILLS = 500;

    @SuppressWarnings("unchecked") // OK for testing mocks.
    @Test
    @MuteLogging
    @DisplayName("not halt after observer throws an error")
    void recoverFromObserverError() {
        SingleThreadInMemSubscriber subscriber =
                new SingleThreadInMemSubscriber(ChannelId.getDefaultInstance());

        StreamObserver<ExternalMessage> throwingObserver = spy(new ThrowingObserver());
        subscriber.addObserver(throwingObserver);

        Any id = Identifier.pack(newUuid());
        ExternalMessage externalMessage = ExternalMessage
                .newBuilder()
                .setId(id)
                .build();
        subscriber.onMessage(externalMessage);
        verify(throwingObserver, timeout(SYNC_TIMEOUT_MILLS)).onNext(externalMessage);

        subscriber.removeObserver(throwingObserver);

        StreamObserver observerMock = mock(StreamObserver.class);
        subscriber.addObserver(observerMock);
        subscriber.onMessage(externalMessage);
        verify(observerMock, timeout(SYNC_TIMEOUT_MILLS)).onNext(externalMessage);
    }
}
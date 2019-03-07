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

package io.spine.server.bc.given;

import com.google.protobuf.Message;
import io.spine.core.EventContext;
import io.spine.core.Subscribe;
import io.spine.server.event.AbstractEventSubscriber;
import io.spine.test.bc.event.BcProjectCreated;
import io.spine.test.bc.event.BcProjectStarted;
import io.spine.test.bc.event.BcTaskAdded;

public class TestEventSubscriber extends AbstractEventSubscriber {

    private Message handledEvent;

    @Subscribe
    void on(BcProjectCreated event, EventContext context) {
        this.handledEvent = event;
    }

    @Subscribe
    void on(BcTaskAdded event, EventContext context) {
    }

    @Subscribe
    void on(BcProjectStarted event, EventContext context) {
    }

    public Message getHandledEvent() {
        return handledEvent;
    }
}
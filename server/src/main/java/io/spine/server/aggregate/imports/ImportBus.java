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

package io.spine.server.aggregate.imports;

import io.spine.core.EventClass;
import io.spine.server.aggregate.ImportEvent;
import io.spine.server.bus.DeadMessageHandler;
import io.spine.server.bus.DispatcherRegistry;
import io.spine.server.bus.EnvelopeValidator;
import io.spine.server.bus.MessageUnhandled;
import io.spine.server.bus.MulticastBus;

import static com.google.common.base.Preconditions.checkState;

/**
 * Dispatches import events to aggregates that import these events.
 *
 * @author Alexander Yevsyukov
 */
public class ImportBus
        extends MulticastBus<ImportEvent, ImportEnvelope, EventClass, ImportDispatcher<?>> {

    private final ImportValidator validator = new ImportValidator();
    private final DeadImportEventHandler deadImportEventHandler = new DeadImportEventHandler();

    protected ImportBus(AbstractBuilder<ImportEnvelope, ImportEvent, ?> builder) {
        super(builder);
    }

    @Override
    protected DeadMessageHandler<ImportEnvelope> getDeadMessageHandler() {
        return deadImportEventHandler;
    }

    @Override
    protected EnvelopeValidator<ImportEnvelope> getValidator() {
        return validator;
    }

    @Override
    protected DispatcherRegistry<EventClass, ImportDispatcher<?>> createRegistry() {
        return new Registry();
    }

    @Override
    protected ImportEnvelope toEnvelope(ImportEvent wrapper) {
        return new ImportEnvelope(wrapper);
    }

    @Override
    protected void dispatch(ImportEnvelope envelope) {
        int dispatchersCalled = callDispatchers(envelope);
        checkState(dispatchersCalled != 0,
                   "The event with class: `%s` has no import dispatchers.",
                   envelope.getMessageClass());
    }

    /**
     * Does nothing because instances of {@link ImportEvent} are transient.
     */
    @Override
    protected void store(Iterable<ImportEvent> messages) {
    }

    /**
     * Creates {@link UnsupportedImportEventException} in response to an event message
     * of unsupported type.
     */
    private static class DeadImportEventHandler implements DeadMessageHandler<ImportEnvelope> {

        @Override
        public MessageUnhandled handle(ImportEnvelope envelope) {
            return new UnsupportedImportEventException(envelope);
        }
    }
}

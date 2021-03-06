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

package io.spine.testing.server.blackbox;

import com.google.common.flogger.FluentLogger.Api;
import com.google.common.flogger.StackSize;
import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import io.spine.base.EventMessage;
import io.spine.base.Identifier;
import io.spine.core.MessageId;
import io.spine.core.Subscribe;
import io.spine.logging.Logging;
import io.spine.server.event.AbstractEventSubscriber;
import io.spine.system.server.AggregateHistoryCorrupted;
import io.spine.system.server.CannotDispatchDuplicateCommand;
import io.spine.system.server.CannotDispatchDuplicateEvent;
import io.spine.system.server.ConstraintViolated;
import io.spine.system.server.HandlerFailedUnexpectedly;
import io.spine.system.server.RoutingFailed;

import java.io.PrintStream;

import static io.spine.json.Json.toJson;
import static java.lang.String.format;

/**
 * A subscriber for all the diagnostic events.
 *
 * <p>Logs all the received diagnostic events with a meaningful message.
 */
final class DiagnosticLog
        extends AbstractEventSubscriber
        implements Logging {

    private static final DiagnosticLog instance = new DiagnosticLog();

    /**
     * Obtains the only instance of {@code DiagnosticLog}.
     */
    static DiagnosticLog instance() {
        return instance;
    }

    /**
     * Prevents direct instantiation.
     */
    private DiagnosticLog() {
        super();
    }

    @Subscribe
    void on(ConstraintViolated event) {
        MessageId entity = event.getEntity();
        String typeUrl = entity.getTypeUrl();
        String idAsString = Identifier.toString(entity.getId());
        log(event, "The state (type: `%s`) of the entity (ID: `%s`) is invalid.",
            typeUrl, idAsString);
    }

    @Subscribe
    void on(CannotDispatchDuplicateCommand event) {
        MessageId command = event.getDuplicateCommand();
        log(event, "The command `%s` (ID: `%s`) should not be dispatched twice.",
            command.getTypeUrl(),
            command.asCommandId()
                   .getUuid());
    }

    @Subscribe
    void on(CannotDispatchDuplicateEvent event) {
        MessageId duplicateEvent = event.getDuplicateEvent();
        log(event, "Event `%s` (ID: `%s`) should not be dispatched twice.",
            duplicateEvent.getTypeUrl(),
            duplicateEvent.asEventId()
                          .getValue());
    }

    @Subscribe
    void on(HandlerFailedUnexpectedly event) {
        log(event, "The entity (state type `%s`) could not handle the signal `%s`:%n%s%n",
            event.getEntity()
                 .getTypeUrl(),
            event.getHandledSignal()
                 .getTypeUrl(),
            event.getError()
                 .getMessage());
    }

    @Subscribe
    void on(RoutingFailed event) {
        log(event, "The signal `%s` could not be routed to `%s`:%n%s%n",
            event.getHandledSignal()
                 .getTypeUrl(),
            event.getEntityType()
                 .getJavaClassName(),
            event.getError()
                 .getMessage());
    }

    @Subscribe
    void on(AggregateHistoryCorrupted event) {
        MessageId aggregate = event.getEntity();
        String idAsString = Identifier.toString(aggregate.getId());
        log(event,
            "Unable to load the history of the aggregate (state type: `%s`, ID: `%s`):%n%s%n",
            aggregate.getTypeUrl(),
            idAsString,
            event.getError()
                 .getMessage());
    }

    @FormatMethod
    private void log(EventMessage event, @FormatString String errorMessage, Object... formatArgs) {
        String msg = format(errorMessage, formatArgs);
        Api severeLogger = logger()
                .atSevere()
                .withStackTrace(StackSize.NONE);
        boolean loggingEnabled = severeLogger.isEnabled();
        if (loggingEnabled) {
            severeLogger.log(msg);
            severeLogger.log(toJson(event));
        } else {
            @SuppressWarnings("UseOfSystemOutOrSystemErr")
                // Edge case for disabled/misconfigured logging .
            PrintStream stderr = System.err;
            stderr.println(msg);
            stderr.println(toJson(event));
        }
    }
}

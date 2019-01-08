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

package io.spine.server.command;

import io.spine.core.CommandClass;
import io.spine.core.CommandEnvelope;
import io.spine.core.Event;
import io.spine.core.Version;
import io.spine.server.command.model.CommandHandlerClass;
import io.spine.server.command.model.CommandHandlerMethod;
import io.spine.server.command.model.CommandHandlerMethod.Result;
import io.spine.server.commandbus.CommandDispatcher;
import io.spine.server.event.EventBus;

import java.util.List;
import java.util.Set;

import static io.spine.server.command.model.CommandHandlerClass.asCommandHandlerClass;

/**
 * The abstract base for non-aggregate classes that expose command handling methods
 * and post their results to {@link EventBus}.
 *
 * <p>A command handler is responsible for:
 * <ol>
 *     <li>Changing the state of the business model in response to a command.
 *     This is done by one of the command handling methods to which the handler dispatches
 *     the command.
 *     <li>Producing corresponding events.
 *     <li>Posting events to {@code EventBus}.
 * </ol>
 *
 * <p>Event messages are returned as values of command handling methods.
 *
 * <p>A command handler does not have own state. So the state of the business
 * model it changes is external to it. Even though such a behaviour may be needed in
 * some rare cases, using {@linkplain io.spine.server.aggregate.Aggregate aggregates}
 * is a preferred way of handling commands.
 *
 * <p>This class implements {@code CommandDispatcher} dispatching messages
 * to methods declared in the derived classes.
 *
 * @author Alexander Yevsyukov
 * @see io.spine.server.aggregate.Aggregate Aggregate
 * @see CommandDispatcher
 */
public abstract class AbstractCommandHandler
        extends AbstractCommandDispatcher
        implements CommandHandler {

    private final CommandHandlerClass<?> thisClass = asCommandHandlerClass(getClass());

    /**
     * Creates a new instance of the command handler.
     *
     * @param eventBus the {@code EventBus} to post events generated by this handler
     */
    protected AbstractCommandHandler(EventBus eventBus) {
        super(eventBus);
    }

    /**
     * Dispatches the command to the handler method and
     * posts resulting events to the {@link EventBus}.
     *
     * @param envelope the command to dispatch
     * @return the handler identity as the result of {@link #toString()}
     * @throws IllegalStateException
     *         if an exception occurred during command dispatching with this exception as the cause
     */
    @Override
    public String dispatch(CommandEnvelope envelope) {
        CommandHandlerMethod method = thisClass.getHandler(envelope.getMessageClass());
        Result result = method.invoke(this, envelope);
        List<Event> events = result.produceEvents(envelope);
        postEvents(events);
        return getId();
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField") // OK as we return immutable impl.
    @Override
    public Set<CommandClass> getMessageClasses() {
        return thisClass.getCommands();
    }

    /**
     * Always returns {@linkplain Version#getDefaultInstance() empty} version.
     */
    @Override
    public Version getVersion() {
        return Version.getDefaultInstance();
    }
}

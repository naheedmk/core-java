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

package io.spine.server.reflect;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import io.spine.client.TestActorRequestFactory;
import io.spine.core.CommandContext;
import io.spine.core.CommandEnvelope;
import io.spine.server.reflect.given.CommandHandlerMethodTestEnv.InvalidHandlerNoAnnotation;
import io.spine.server.reflect.given.CommandHandlerMethodTestEnv.InvalidHandlerNoParams;
import io.spine.server.reflect.given.CommandHandlerMethodTestEnv.InvalidHandlerOneNotMsgParam;
import io.spine.server.reflect.given.CommandHandlerMethodTestEnv.InvalidHandlerReturnsVoid;
import io.spine.server.reflect.given.CommandHandlerMethodTestEnv.InvalidHandlerTooManyParams;
import io.spine.server.reflect.given.CommandHandlerMethodTestEnv.InvalidHandlerTwoParamsFirstInvalid;
import io.spine.server.reflect.given.CommandHandlerMethodTestEnv.InvalidHandlerTwoParamsSecondInvalid;
import io.spine.server.reflect.given.CommandHandlerMethodTestEnv.ValidHandlerButPrivate;
import io.spine.server.reflect.given.CommandHandlerMethodTestEnv.ValidHandlerOneParam;
import io.spine.server.reflect.given.CommandHandlerMethodTestEnv.ValidHandlerOneParamReturnsList;
import io.spine.server.reflect.given.CommandHandlerMethodTestEnv.ValidHandlerTwoParams;
import io.spine.server.reflect.given.CommandHandlerMethodTestEnv.ValidHandlerTwoParamsReturnsList;
import io.spine.test.reflect.command.RefCreateProject;
import io.spine.test.reflect.event.RefProjectCreated;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

import static io.spine.server.reflect.CommandHandlerMethod.from;
import static io.spine.server.reflect.CommandHandlerMethod.predicate;
import static io.spine.server.reflect.given.Given.CommandMessage.createProject;
import static io.spine.server.reflect.given.Given.CommandMessage.startProject;
import static io.spine.test.TestValues.newUuidValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Alexander Litus
 */
public class CommandHandlerMethodShould {

    private static final TestActorRequestFactory requestFactory =
            TestActorRequestFactory.newInstance(CommandHandlerMethodShould.class);

    private static final CommandContext defCmdCtx = CommandContext.getDefaultInstance();

    @Test
    public void pass_null_tolerance_check() {
        new NullPointerTester()
                .setDefault(CommandEnvelope.class,
                            CommandEnvelope.of(requestFactory.command()
                                                             .create(newUuidValue())))
                .setDefault(CommandContext.class, defCmdCtx)
                .setDefault(Any.class, Any.getDefaultInstance())
                .testAllPublicStaticMethods(CommandHandlerMethod.class);
    }

    @Test
    public void invoke_handler_method_which_returns_one_message() {
        final ValidHandlerTwoParams handlerObject = spy(new ValidHandlerTwoParams());
        final CommandHandlerMethod handler = from(handlerObject.getHandler());
        final RefCreateProject cmd = createProject();

        final List<? extends Message> events = handler.invoke(handlerObject, cmd, defCmdCtx);

        verify(handlerObject, times(1))
                .handleTest(cmd, defCmdCtx);
        assertEquals(1, events.size());
        final RefProjectCreated event = (RefProjectCreated) events.get(0);
        assertEquals(cmd.getProjectId(), event.getProjectId());
    }

    @Test
    public void invoke_handler_method_and_return_message_list() {
        final ValidHandlerOneParamReturnsList handlerObject =
                spy(new ValidHandlerOneParamReturnsList());
        final CommandHandlerMethod handler = from(handlerObject.getHandler());
        final RefCreateProject cmd = createProject();

        final List<? extends Message> events = handler.invoke(handlerObject, cmd, defCmdCtx);

        verify(handlerObject, times(1)).handleTest(cmd);
        assertEquals(1, events.size());
        final RefProjectCreated event = (RefProjectCreated) events.get(0);
        assertEquals(cmd.getProjectId(), event.getProjectId());
    }

    @Test
    public void consider_handler_with_one_msg_param_valid() {
        final Method handler = new ValidHandlerOneParam().getHandler();

        assertIsCommandHandler(handler, true);
    }

    @Test
    public void consider_handler_with_one_msg_param_which_returns_list_valid() {
        final Method handler = new ValidHandlerOneParamReturnsList().getHandler();

        assertIsCommandHandler(handler, true);
    }

    @Test
    public void consider_handler_with_msg_and_context_params_valid() {
        final Method handler = new ValidHandlerTwoParams().getHandler();

        assertIsCommandHandler(handler, true);
    }

    @Test
    public void consider_handler_with_msg_and_context_params_which_returns_list_valid() {
        final Method handler = new ValidHandlerTwoParamsReturnsList().getHandler();

        assertIsCommandHandler(handler, true);
    }

    @Test
    public void consider_not_public_handler_valid() {
        final Method method = new ValidHandlerButPrivate().getHandler();

        assertIsCommandHandler(method, true);
    }

    @Test
    public void consider_not_annotated_handler_invalid() {
        final Method handler = new InvalidHandlerNoAnnotation().getHandler();

        assertIsCommandHandler(handler, false);
    }

    @Test
    public void consider_handler_without_params_invalid() {
        final Method handler = new InvalidHandlerNoParams().getHandler();

        assertIsCommandHandler(handler, false);
    }

    @Test
    public void consider_handler_with_too_many_params_invalid() {
        final Method handler = new InvalidHandlerTooManyParams().getHandler();

        assertIsCommandHandler(handler, false);
    }

    @Test
    public void consider_handler_with_one_invalid_param_invalid() {
        final Method handler = new InvalidHandlerOneNotMsgParam().getHandler();

        assertIsCommandHandler(handler, false);
    }

    @Test
    public void consider_handler_with_first_not_message_param_invalid() {
        final Method handler = new InvalidHandlerTwoParamsFirstInvalid().getHandler();

        assertIsCommandHandler(handler, false);
    }

    @Test
    public void consider_handler_with_second_not_context_param_invalid() {
        final Method handler = new InvalidHandlerTwoParamsSecondInvalid().getHandler();

        assertIsCommandHandler(handler, false);
    }

    @Test
    public void consider_void_handler_invalid() {
        final Method handler = new InvalidHandlerReturnsVoid().getHandler();

        assertIsCommandHandler(handler, false);
    }

    private static void assertIsCommandHandler(Method handler, boolean isHandler) {
        assertEquals(isHandler, predicate().apply(handler));
    }

    @Test(expected = IllegalStateException.class)
    public void throw_ISE_for_not_handled_command_type() {
        final Object handler = new ValidHandlerOneParam();
        CommandHandlerMethod.invokeFor(handler,
                                       startProject(),
                                       CommandContext.getDefaultInstance());
    }
}

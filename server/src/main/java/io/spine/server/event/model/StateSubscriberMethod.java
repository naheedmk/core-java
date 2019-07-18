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

package io.spine.server.event.model;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import io.spine.base.Environment;
import io.spine.base.EventMessage;
import io.spine.base.FieldPath;
import io.spine.base.FieldPaths;
import io.spine.core.BoundedContextName;
import io.spine.core.ByField;
import io.spine.core.Subscribe;
import io.spine.logging.Logging;
import io.spine.server.annotation.BoundedContext;
import io.spine.server.model.MessageFilter;
import io.spine.server.model.Model;
import io.spine.server.model.declare.ParameterSpec;
import io.spine.server.type.EventEnvelope;
import io.spine.system.server.event.EntityStateChanged;
import io.spine.type.TypeUrl;

import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkState;
import static io.spine.core.BoundedContextNames.assumingTests;
import static io.spine.protobuf.TypeConverter.toAny;

/**
 * A handler method which receives an entity state and produces no output.
 */
public final class StateSubscriberMethod extends SubscriberMethod implements Logging {

    private static final FieldPath TYPE_URL_PATH = FieldPaths.parse("entity.type_url");

    private final BoundedContextName contextOfSubscriber;
    private final Class<? extends Message> stateType;
    private final Any typeUrlAsAny;

    StateSubscriberMethod(Method method, ParameterSpec<EventEnvelope> parameterSpec) {
        super(checkNotFiltered(method), parameterSpec);
        this.contextOfSubscriber = contextOf(method.getDeclaringClass());
        this.stateType = firstParamType(rawMethod());
        checkExternal();
        TypeUrl targetType = TypeUrl.of(this.stateType);
        this.typeUrlAsAny = toAny(targetType.value());
    }

    private static Method checkNotFiltered(Method method) {
        Subscribe subscribe = method.getAnnotation(Subscribe.class);
        ByField filter = subscribe.filter();
        checkState(filter.path().isEmpty() && filter.value().isEmpty(),
                   "A state subscriber method cannot declare filters but the method `%s` does.",
                   method);
        return method;
    }

    private void checkExternal() {
        BoundedContextName originContext = contextOf(stateType());
        boolean external = !originContext.equals(contextOfSubscriber);
        ensureExternalMatch(external);
    }

    /**
     * Obtains the type of the entity state to which the method is subscribed.
     */
    public Class<? extends Message> stateType() {
        return stateType;
    }

    @Override
    public MessageFilter filter() {
        return MessageFilter
                .newBuilder()
                .setField(TYPE_URL_PATH)
                .setValue(typeUrlAsAny)
                .build();
    }

    /**
     * Always returns {@link EntityStateChanged}{@code .class}.
     */
    @Override
    protected Class<? extends EventMessage> rawMessageClass() {
        return EntityStateChanged.class;
    }

    @SuppressWarnings("TestOnlyProblems")
        // Checks that the resulting context is not `AssumingTests` in production environment.
    private BoundedContextName contextOf(Class<?> cls) {
        Model model = Model.inContextOf(cls);
        BoundedContextName name = model.contextName();
        if (Environment.instance().isProduction() && name.equals(assumingTests())) {
            _warn().log(
                    "The class `%s` belongs to the Bounded Context named `%s`," +
                    " which is used for testing. As such, it should not be used in production." +
                    " Please see the description of `%s` for instructions on" +
                    " annotating packages with names of Bounded Contexts of your application.",
                    cls.getName(), assumingTests().getValue(), BoundedContext.class.getName()
            );
        }
        return name;
    }
}
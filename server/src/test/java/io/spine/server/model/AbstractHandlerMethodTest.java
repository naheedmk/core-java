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

package io.spine.server.model;

import io.spine.core.Event;
import io.spine.core.EventContext;
import io.spine.server.model.given.method.OneParamMethod;
import io.spine.server.model.given.method.OneParamSignature;
import io.spine.server.model.given.method.OneParamSpec;
import io.spine.server.model.given.method.StubHandler;
import io.spine.server.model.given.method.TwoParamMethod;
import io.spine.server.model.given.method.TwoParamSpec;
import io.spine.server.type.EventEnvelope;
import io.spine.test.model.ModProjectCreated;
import io.spine.test.model.ModProjectStarted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.spine.base.Identifier.newUuid;
import static io.spine.protobuf.AnyPacker.pack;
import static io.spine.server.model.AbstractHandlerMethod.firstParamType;
import static io.spine.server.model.given.method.StubHandler.getMethodWithCheckedException;
import static io.spine.server.model.given.method.StubHandler.getMethodWithRuntimeException;
import static io.spine.testing.Tests.nullRef;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("DuplicateStringLiteralInspection") // Common test display names.
@DisplayName("AbstractHandlerMethod should")
class AbstractHandlerMethodTest {

    private final OneParamSignature signature = new OneParamSignature();

    private TwoParamMethod twoParamMethod;
    private OneParamMethod oneParamMethod;

    private Object target;

    @BeforeEach
    void setUp() {
        target = new StubHandler();
        twoParamMethod = new TwoParamMethod(StubHandler.getTwoParameterMethod(),
                                            TwoParamSpec.INSTANCE);
        oneParamMethod = new OneParamMethod(StubHandler.getOneParameterMethod(),
                                            OneParamSpec.INSTANCE);
    }

    @Test
    @DisplayName("not accept null method")
    void notAcceptNullMethod() {
        assertThrows(NullPointerException.class, () -> new TwoParamMethod(nullRef(),
                                                                          TwoParamSpec.INSTANCE));
    }

    @Test
    @DisplayName("return method")
    void returnMethod() {
        assertEquals(StubHandler.getTwoParameterMethod(), twoParamMethod.rawMethod());
    }

    @Nested
    @DisplayName("check if method access is")
    class CheckAccess {

        @Test
        @DisplayName(" public")
        void isPublic() {
            assertTrue(twoParamMethod.isPublic());
        }

        @Test
        @DisplayName(" private")
        void isPrivate() {
            assertTrue(oneParamMethod.isPrivate());
        }
    }

    @Test
    @DisplayName("obtain first parameter type of method")
    void returnFirstParamType() {
        assertEquals(ModProjectStarted.class, firstParamType(oneParamMethod.rawMethod()));
    }

    @Nested
    @DisplayName("invoke method")
    class InvokeMethod {

        @SuppressWarnings("CheckReturnValue") // can ignore the result in this test
        @Test
        @DisplayName("with one parameter")
        void withOneParam() {
            ModProjectStarted eventMessage = ModProjectStarted
                    .newBuilder()
                    .setId(newUuid())
                    .build();
            Event event = Event
                    .newBuilder()
                    .setMessage(pack(eventMessage))
                    .build();
            EventEnvelope envelope = EventEnvelope.of(event);
            oneParamMethod.invoke(target, envelope);

            assertTrue(((StubHandler) target).wasHandleInvoked());
        }

        @SuppressWarnings("CheckReturnValue") // can ignore the result in this test
        @Test
        @DisplayName("with two parameters")
        void withTwoParams() {
            ModProjectCreated eventMessage = ModProjectCreated
                    .newBuilder()
                    .setId(newUuid())
                    .build();
            Event event = Event
                    .newBuilder()
                    .setMessage(pack(eventMessage))
                    .setContext(EventContext.getDefaultInstance())
                    .build();
            EventEnvelope envelope = EventEnvelope.of(event);
            twoParamMethod.invoke(target, envelope);

            assertTrue(((StubHandler) target).wasOnInvoked());
        }
    }

    @Test
    @DisplayName("return full name in `toString`")
    void provideToString() {
        assertEquals(twoParamMethod.getFullName(), twoParamMethod.toString());
    }

    @Nested
    @DisplayName("provide `equals` method such that")
    class ProvideEqualsSuchThat {

        @Test
        @DisplayName("instance equals to itself")
        void equalsToItself() {
            assertEquals(twoParamMethod, twoParamMethod);
        }

        @Test
        @DisplayName("instance is not equal to null")
        void notEqualsToNull() {
            assertNotEquals(null, oneParamMethod);
        }

        @Test
        @DisplayName("instance is not equal to another class")
        void notEqualsToOtherClass() {
            assertNotEquals(twoParamMethod, oneParamMethod);
        }

        @Test
        @DisplayName("all fields are compared")
        void allFieldsAreCompared() {
            AbstractHandlerMethod<?, ?, ?, ?, ?> anotherMethod =
                    new TwoParamMethod(StubHandler.getTwoParameterMethod(),
                                       TwoParamSpec.INSTANCE);
            assertEquals(twoParamMethod, anotherMethod);
        }
    }

    @Test
    @DisplayName("have `hashCode`")
    void haveHashCode() {
        assertNotEquals(System.identityHashCode(twoParamMethod), twoParamMethod.hashCode());
    }

    @Nested
    @DisplayName("not be created from method throwing")
    class RejectMethodThrowing {

        @Test
        @DisplayName("checked exception")
        void checkedException() {
            Optional<OneParamMethod> method = signature.classify(getMethodWithCheckedException());
            assertFalse(method.isPresent());
        }

        @Test
        @DisplayName("runtime exception")
        void runtimeException() {
            Optional<OneParamMethod> method = signature.classify(getMethodWithRuntimeException());
            assertFalse(method.isPresent());
        }
    }
}

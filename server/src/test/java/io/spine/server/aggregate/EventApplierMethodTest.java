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

package io.spine.server.aggregate;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Any;
import com.google.protobuf.StringValue;
import io.spine.core.CommandContext;
import io.spine.server.model.HandlerMethod;
import io.spine.test.reflect.event.RefProjectCreated;
import io.spine.testdata.Sample;
import io.spine.validate.StringValueVBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static io.spine.test.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"InnerClassMayBeStatic", "ClassCanBeStatic"
        /* JUnit 5 Nested classes cannot to be static. */})
@DisplayName("EventApplierMethod should")
class EventApplierMethodTest {

    private final HandlerMethod.Factory<EventApplierMethod> factory = EventApplierMethod.factory();

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .setDefault(CommandContext.class, CommandContext.getDefaultInstance())
                .setDefault(Any.class, Any.getDefaultInstance())
                .testAllPublicStaticMethods(EventApplierMethod.class);
    }

    @Nested
    @DisplayName("provide")
    class Provide {

        @Test
        @DisplayName("factory instance")
        void factoryInstance() {
            assertNotNull(factory);
        }

        @Test
        @DisplayName("method class")
        void methodClass() {
            assertEquals(EventApplierMethod.class, factory.getMethodClass());
        }

        @Test
        @DisplayName("method predicate")
        void methodPredicate() {
            assertEquals(EventApplierMethod.predicate(), factory.getPredicate());
        }
    }

    @Test
    @DisplayName("be properly created from factory")
    void beCreatedFromFactory() {
        final Method method = new ValidApplier().getMethod();

        final EventApplierMethod actual = factory.create(method);

        assertEquals(EventApplierMethod.from(method), actual);
    }

    @Test
    @DisplayName("allow invocation")
    void invokeApplierMethod() throws InvocationTargetException {
        final ValidApplier applierObject = new ValidApplier();
        final EventApplierMethod applier = EventApplierMethod.from(applierObject.getMethod());
        final RefProjectCreated event = Sample.messageOfType(RefProjectCreated.class);

        applier.invoke(applierObject, event);

        assertEquals(event, applierObject.eventApplied);
    }

    @Test
    @DisplayName("check method access modifier")
    void checkMethodAccessModifier() {
        final Method method = new ValidApplierButNotPackagePrivate().getMethod();

        factory.checkAccessModifier(method);
    }

    @Nested
    @DisplayName("consider applier valid when")
    class ConsiderApplierValidWhen {

        @Test
        @DisplayName("it has one message parameter")
        void hasOneMessageParameter() {
            final Method applier = new ValidApplier().getMethod();

            assertIsEventApplier(applier);
        }

        @Test
        @DisplayName("it's not private")
        void isNotPrivate() {
            final Method method = new ValidApplierButNotPackagePrivate().getMethod();

            assertIsEventApplier(method);
        }

        private void assertIsEventApplier(Method applier) {
            assertTrue(EventApplierMethod.predicate()
                                         .apply(applier));
        }
    }

    @Nested
    @DisplayName("consider applier invalid when")
    class ConsiderApplierInvalidWhen {

        @Test
        @DisplayName("it's not annotated")
        void isNotAnnotated() {
            final Method applier = new InvalidApplierNoAnnotation().getMethod();

            assertIsNotEventApplier(applier);
        }

        @Test
        @DisplayName("it has no params")
        void hasNoParams() {
            final Method applier = new InvalidApplierNoParams().getMethod();

            assertIsNotEventApplier(applier);
        }

        @Test
        @DisplayName("it has too many params")
        void hasTooManyParams() {
            final Method applier = new InvalidApplierTooManyParams().getMethod();

            assertIsNotEventApplier(applier);
        }

        @Test
        @DisplayName("it has one param of invalid type")
        void hasOneInvalidParam() {
            final Method applier = new InvalidApplierOneNotMsgParam().getMethod();

            assertIsNotEventApplier(applier);
        }

        @Test
        @DisplayName("it has non-void return type")
        void returnsNonVoidType() {
            final Method applier = new InvalidApplierNotVoid().getMethod();

            assertIsNotEventApplier(applier);
        }

        private void assertIsNotEventApplier(Method applier) {
            assertFalse(EventApplierMethod.predicate()
                                          .apply(applier));
        }
    }

    /*
     * Valid appliers
     ****************/

    private static class ValidApplier extends TestEventApplier {

        private RefProjectCreated eventApplied;

        @Apply
        void apply(RefProjectCreated event) {
            this.eventApplied = event;
        }
    }

    private static class ValidApplierButNotPackagePrivate extends TestEventApplier {
        @Apply
        public void apply(RefProjectCreated event) {
        }
    }

    /*
     * Invalid appliers
     *******************/

    private static class InvalidApplierNoAnnotation extends TestEventApplier {
        @SuppressWarnings("unused")
        public void apply(RefProjectCreated event) {
        }
    }

    private static class InvalidApplierNoParams extends TestEventApplier {
        @Apply
        public void apply() {
        }
    }

    private static class InvalidApplierTooManyParams extends TestEventApplier {
        @Apply
        public void apply(RefProjectCreated event, Object redundant) {
        }
    }

    private static class InvalidApplierOneNotMsgParam extends TestEventApplier {
        @Apply
        public void apply(Exception invalid) {
        }
    }

    private static class InvalidApplierNotVoid extends TestEventApplier {
        @Apply
        public Object apply(RefProjectCreated event) {
            return event;
        }
    }

    private abstract static class TestEventApplier extends Aggregate<Long,
                                                                     StringValue,
                                                                     StringValueVBuilder> {

        protected TestEventApplier() {
            super(0L);
        }

        private static final String APPLIER_METHOD_NAME = "apply";

        public Method getMethod() {
            final Method[] methods = getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(APPLIER_METHOD_NAME)) {
                    method.setAccessible(true);
                    return method;
                }
            }
            throw new RuntimeException("No applier method found: " + APPLIER_METHOD_NAME);
        }
    }
}
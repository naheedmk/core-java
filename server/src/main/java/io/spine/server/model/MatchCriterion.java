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

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;
import io.spine.annotation.Internal;
import io.spine.string.Diags;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static io.spine.server.model.MethodExceptionCheck.check;
import static io.spine.server.model.MethodParams.findMatching;
import static io.spine.server.model.ModelError.MessageFormatter.toStringEnumeration;
import static io.spine.server.model.SignatureMismatch.Severity.ERROR;
import static io.spine.server.model.SignatureMismatch.Severity.WARN;
import static io.spine.server.model.SignatureMismatch.create;
import static java.lang.String.format;

/**
 * The criteria of {@linkplain Method method} matching to a certain {@linkplain MethodSignature
 * set of requirements}, applied to the model message handlers.
 *
 * <p>Each criterion defines the {@linkplain #severity() severity} of its violation.
 * Depending on it, the callees may refuse working with the tested methods.
 *
 * <p>Additionally, upon testing the criteria provide a number of {@linkplain SignatureMismatch
 * signature mismatches}, that may later be used for diagnostic purposes.
 */
@Internal
public enum MatchCriterion {

    /**
     * The criterion, which checks that the method return type is among the
     * {@linkplain MethodSignature#returnTypes() expected}.
     */
    RETURN_TYPE(ERROR,
                "The return type of `%s` method does not match the constraints " +
                        "set for `%s`-annotated method.") {
        @SuppressWarnings("UnstableApiUsage")   // Using Guava's `TypeToken`.
        @Override
        Optional<SignatureMismatch> test(Method method, MethodSignature<?, ?> signature) {
            TypeToken<?> returnType = Invokable.from(method)
                                               .getReturnType();
            boolean conforms = signature
                    .returnTypes()
                    .stream()
                    .anyMatch(type -> TypeMatcher.matches(type, returnType) &&
                            (signature.mayReturnIgnored() ||
                                    TypeMatcher.messagesFitting(returnType)
                                               .stream()
                                               .noneMatch(MethodResult::isIgnored))
                    );
            if (!conforms) {
                SignatureMismatch mismatch =
                        create(this,
                               methodAsString(method),
                               signature.annotation()
                                        .getSimpleName());
                return Optional.of(mismatch);
            }
            return Optional.empty();
        }
    },

    /**
     * The criterion, which ensures that the method access modifier is among the
     * {@linkplain MethodSignature#modifiers() expected}.
     */
    ACCESS_MODIFIER(WARN,
                    "The access modifier of `%s` method is `%s`. We recommend it to be `%s`. " +
                            "Refer to the `%s` annotation docs for details.") {
        @Override
        Optional<SignatureMismatch> test(Method method, MethodSignature<?, ?> signature) {
            ImmutableSet<AccessModifier> allowedModifiers = signature.modifiers();
            boolean hasMatch = allowedModifiers
                    .stream()
                    .anyMatch(m -> m.test(method));
            if (!hasMatch) {
                SignatureMismatch mismatch =
                        create(this,
                               methodAsString(method),
                               AccessModifier.fromMethod(method),
                               AccessModifier.asString(allowedModifiers),
                               signature.annotation()
                                        .getSimpleName());
                return Optional.of(mismatch);

            }
            return Optional.empty();
        }
    },

    /**
     * The criterion checking that the tested method throws only
     * {@linkplain MethodSignature#allowedThrowable() allowed exceptions}.
     */
    PROHIBITED_EXCEPTION(ERROR, "%s") {

        @Override
        Optional<SignatureMismatch> test(Method method, MethodSignature<?, ?> signature) {
            @Nullable
            Class<? extends Throwable> allowedThrowable =
                    signature.allowedThrowable().orElse(null);
            MethodExceptionCheck checker = check(method, allowedThrowable);
            List<Class<? extends Throwable>> prohibited = checker.findProhibited();
            if (prohibited.isEmpty()) {
                return Optional.empty();
            }
            String errorMessage = toMessage(method, prohibited, allowedThrowable);
            SignatureMismatch mismatch = create(this, errorMessage);
            return Optional.of(mismatch);
        }

        private String toMessage(Method method,
                                 List<Class<? extends Throwable>> exceptionsThrown,
                                 @Nullable Class<? extends Throwable> allowedThrowable) {
            if (allowedThrowable == null) {
                return format(
                        "The method `%s.%s` throws %s. But throwing is not allowed" +
                                " for this kind of methods.",
                        method.getDeclaringClass().getCanonicalName(),
                        method.getName(),
                        enumerate(exceptionsThrown)
                );
            }
            return format(
                    "The method `%s.%s` throws %s. But only `%s` is allowed for" +
                            " this kind of methods.",
                    method.getDeclaringClass().getCanonicalName(),
                    method.getName(),
                    enumerate(exceptionsThrown),
                    allowedThrowable.getName()
            );
        }

        /**
         * Prints {@link Iterable} to {@link String}, separating elements with comma.
         */
        private String enumerate(List<Class<? extends Throwable>> throwables) {
            return throwables.stream()
                             .map(Diags::backtick)
                             .collect(toStringEnumeration());
        }
    },

    /**
     * The criterion for the method parameters to conform the
     * {@linkplain MethodSignature#paramSpecs() requirements}.
     *
     * @see MethodParams#findMatching(Method, Collection)
     */
    PARAMETERS(ERROR,
               "The method `%s` has invalid parameters. " +
               "Please refer to `%s` annotation documentation for allowed parameter types.") {
        @Override
        Optional<SignatureMismatch> test(Method method, MethodSignature<?, ?> signature) {
            Optional<? extends ParameterSpec<?>> matching =
                    findMatching(method, signature.paramSpecs());
            if (!matching.isPresent()) {
                SignatureMismatch mismatch =
                        create(this,
                               methodAsString(method),
                               signature.annotation()
                                        .getSimpleName());
                return Optional.of(mismatch);
            }
            return Optional.empty();
        }
    };

    private final SignatureMismatch.Severity severity;
    private final String format;

    /**
     * Creates an instance with the given severity and the template of the signature
     * mismatch message.
     */
    MatchCriterion(SignatureMismatch.Severity severity, String format) {
        this.severity = severity;
        this.format = format;
    }

    SignatureMismatch.Severity severity() {
        return severity;
    }

    String formatMsg(Object... args) {
        String message = format(Locale.ROOT, format, args);
        return message;
    }

    /**
     * Tests the method against the rules defined by the signature.
     *
     * @param method
     *         the method to test
     * @param signature
     *         the signature to use in testing
     * @return {@link Optional#empty() Optional.empty()} if there was no mismatch,
     *         or the {@code Optional<SignatureMismatch>} of the mismatch detected.
     */
    abstract Optional<SignatureMismatch> test(Method method, MethodSignature<?, ?> signature);

    private static String methodAsString(Method method) {
        String declaringClassName = method.getDeclaringClass()
                                          .getCanonicalName();
        String parameterTypes = Diags.join(method.getParameterTypes());
        String result = format("%s.%s(%s)", declaringClassName, method.getName(), parameterTypes);
        return result;
    }
}

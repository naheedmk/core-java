/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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

package org.spine3.server.event.enrich;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMultimap;
import com.google.protobuf.Internal;
import com.google.protobuf.Message;
import org.spine3.protobuf.Messages;
import org.spine3.server.event.enrich.EventEnricher.SupportsFieldConversion;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.protobuf.Descriptors.FieldDescriptor;

/**
 * The default mechanism for enriching messages based on {@code FieldOptions} of Protobuf message definitions.
 *
 * @param <S> a type of the source event message to enrich
 * @param <T> a type of the target enrichment message
 *
 * @author Alexander Yevsyukov
 */
/* package */ class EventMessageEnricher<S extends Message, T extends Message> extends EnrichmentFunction<S, T> {

    /**
     * A parent instance holding this instance and its siblings.
     */
    private final EventEnricher enricher;

    /**
     * A map from source event field class to enrichment functions.
     */
    @Nullable
    private ImmutableMultimap<Class<?>, EnrichmentFunction> fieldFunctions;

    /**
     * A map from source event field to target enrichment field descriptors.
     */
    @Nullable
    private ImmutableMultimap<FieldDescriptor, FieldDescriptor> fieldMap;

    /* package */ EventMessageEnricher(EventEnricher enricher, Class<S> sourceClass, Class<T> targetClass) {
        super(sourceClass, targetClass);
        this.enricher = enricher;
    }

    /**
     * Creates a new instance for enriching events.
     *
     * @param source a class of the event message to enrich
     * @param target a class of the target enrichment
     * @param <M> the type of the event message
     * @param <E> the type of the enrichment message
     * @return new enrichment function with {@link EventMessageEnricher}
     */
    /* package */ static <M extends Message, E extends Message>
    EnrichmentFunction<M, E> unboundInstance(Class<M> source, Class<E> target) {
        final EnrichmentFunction<M, E> result = new Unbound<>(source, target);
        return result;
    }

    @Override
    public Function<S, T> getFunction() {
        return this;
    }

    @Override
    /* package */ void validate() {
        final ReferenceValidator referenceValidator = new ReferenceValidator(enricher,
                                                                             getSourceClass(),
                                                                             getTargetClass());
        final ImmutableMultimap.Builder<Class<?>, EnrichmentFunction> map = ImmutableMultimap.builder();
        final List<EnrichmentFunction<?, ?>> fieldFunctions = referenceValidator.validate();
        for (EnrichmentFunction<?, ?> fieldFunction : fieldFunctions) {
            map.put(fieldFunction.getSourceClass(), fieldFunction);
        }
        this.fieldFunctions = map.build();
        this.fieldMap = referenceValidator.fieldMap();
    }

    @SuppressWarnings("unchecked") // We control the type safety during initialization and validation.
    @Nullable
    @Override
    public T apply(@Nullable S message) {
        if (message == null) {
            return null;
        }
        checkNotNull(this.fieldMap, "fieldMap");
        checkNotNull(this.fieldFunctions, "fieldFunctions");

        checkState(!fieldMap.isEmpty(), "fieldMap is empty");
        checkState(!fieldFunctions.isEmpty(), "fieldFunctions is empty");

        final T defaultTarget = Internal.getDefaultInstance(getTargetClass());
        final Message.Builder builder = defaultTarget.toBuilder();

        for (FieldDescriptor srcField : fieldMap.keySet()) {
            final Object srcFieldValue = message.getField(srcField);
            final Class<?> sourceFieldClass = srcFieldValue.getClass();
            final Collection<EnrichmentFunction> functions = fieldFunctions.get(sourceFieldClass);
            final Collection<FieldDescriptor> targetFields = fieldMap.get(srcField);

            // TODO:2016-06-23:alexander.litus: refactor
            for (FieldDescriptor targetField : targetFields) {
                final Optional<EnrichmentFunction> function = FluentIterable.from(functions)
                        .firstMatch(SupportsFieldConversion.of(sourceFieldClass, Messages.getFieldClass(targetField)));
                final Object targetValue = function.get().apply(srcFieldValue);
                if (targetValue != null) {
                    builder.setField(targetField, targetValue);
                }
            }
        }
        return (T) builder.build();
    }
}

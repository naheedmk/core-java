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

package io.spine.server.aggregate;

import io.spine.annotation.Internal;
import io.spine.server.DefaultRepository;
import io.spine.server.aggregate.model.AggregateClass;

import static io.spine.server.aggregate.model.AggregateClass.asAggregateClass;

/**
 * Default implementation of {@code AggregateRepository}.
 *
 * @see io.spine.server.DefaultRepository
 */
@Internal
public final class DefaultAggregateRepository<I, A extends Aggregate<I, ?, ?>>
        extends AggregateRepository<I, A>
        implements DefaultRepository {

    private final AggregateClass<A> modelClass;

    /**
     * Creates a new repository for managing aggregates of the passed class.
     */
    public DefaultAggregateRepository(Class<A> cls) {
        super();
        this.modelClass = asAggregateClass(cls);
    }

    /**
     * Obtains the class of aggregates managed by this repository.
     */
    @Override
    public AggregateClass<A> entityModelClass() {
        return modelClass;
    }

    @Override
    public String toString() {
        return logName();
    }
}

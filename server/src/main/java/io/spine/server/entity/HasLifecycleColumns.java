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

package io.spine.server.entity;

import io.spine.annotation.Internal;
import io.spine.base.EntityState;
import io.spine.server.entity.storage.SystemColumn;

/**
 * Marks an {@link Entity} that declares columns for lifecycle flags.
 *
 * @see SystemColumn
 */
@SuppressWarnings("DuplicateStringLiteralInspection") // Can only use string literals in annotation.
@Internal
public interface HasLifecycleColumns<I, S extends EntityState> extends Entity<I, S> {

    /**
     * Obtains the value of {@code archived} flag.
     */
    @SystemColumn(name = "archived")
    default boolean getArchived() {
        return isArchived();
    }

    /**
     * Obtains the value of {@code deleted} flag.
     */
    @SystemColumn(name = "deleted")
    default boolean getDeleted() {
        return isDeleted();
    }
}

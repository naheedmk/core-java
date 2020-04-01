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

package io.spine.server.delivery;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Timestamp;
import io.spine.server.entity.storage.ColumnName;
import io.spine.server.storage.MessageColumn;

/**
 * The columns stored for {@link CatchUp} statuses.
 */
public enum CatchUpColumn {

    status("status",
           CatchUpStatus.class,
           CatchUp::getStatus),

    whenLastRead("when_last_read",
                 Timestamp.class,
                 CatchUp::getWhenLastRead),

    projectionType("projection_type",
                   String.class,
                   (m) -> m.getId()
                           .getProjectionType());

    @SuppressWarnings("NonSerializableFieldInSerializableClass")
    private final MessageColumn<?, CatchUp> column;

    <T> CatchUpColumn(String columnName, Class<T> type, MessageColumn.Getter<CatchUp, T> getter) {
        ColumnName name = ColumnName.of(columnName);
        this.column = new MessageColumn<>(name, type, getter);
    }

    static ImmutableList<MessageColumn<?, CatchUp>> definitions() {
        ImmutableList.Builder<MessageColumn<?, CatchUp>> list = ImmutableList.builder();
        for (CatchUpColumn value : CatchUpColumn.values()) {
            list.add(value.column);
        }
        return list.build();
    }

    public MessageColumn<?, CatchUp> column() {
        return column;
    }
}
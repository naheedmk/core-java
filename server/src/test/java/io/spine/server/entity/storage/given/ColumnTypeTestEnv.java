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

package io.spine.server.entity.storage.given;

import io.spine.server.entity.storage.ColumnType;

public class ColumnTypeTestEnv {

    /** Prevents instantiation of this utility class. */
    private ColumnTypeTestEnv() {
    }

    public static class TestColumnType<T> implements ColumnType<T, String, StringBuilder, String> {

        @Override
        public String convertColumnValue(T fieldValue) {
            return String.valueOf(fieldValue);
        }

        @Override
        public void setColumnValue(StringBuilder storageRecord,
                                   String value,
                                   String columnIdentifier) {
            storageRecord.append(columnIdentifier)
                         .append(": ")
                         .append(value);
        }

        @Override
        public void setNull(StringBuilder storageRecord, String columnIdentifier) {
            setColumnValue(storageRecord, "null", columnIdentifier);
        }
    }
}
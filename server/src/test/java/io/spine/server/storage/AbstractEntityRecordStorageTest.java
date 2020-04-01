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

package io.spine.server.storage;

import com.google.protobuf.Any;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Message;
import io.spine.base.EntityState;
import io.spine.base.Identifier;
import io.spine.client.ResponseFormat;
import io.spine.server.entity.EntityRecord;
import io.spine.server.entity.storage.EntityRecordStorage;
import io.spine.server.entity.storage.EntityRecordWithColumns;
import io.spine.server.storage.given.RecordStorageTestEnv;
import io.spine.testing.core.given.GivenVersion;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.truth.Truth.assertThat;
import static com.google.protobuf.util.FieldMaskUtil.fromFieldNumbers;
import static io.spine.protobuf.AnyPacker.pack;
import static io.spine.protobuf.AnyPacker.unpack;
import static io.spine.protobuf.Messages.isDefault;
import static io.spine.server.storage.given.RecordStorageTestEnv.withLifecycleColumns;
import static io.spine.testing.Tests.assertMatchesMask;
import static io.spine.testing.Tests.nullRef;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract base for tests of message storage implementations.
 *
 * <p>This abstract test should not contain {@linkplain org.junit.jupiter.api.Nested nested tests}
 * because they are not under control of {@code AbstractMessageStorageTest} inheritors.
 * Such a control is required for overriding or disabling tests due to a lag between read and
 * write on remote storage implementations, etc.
 *
 * @param <I>
 *         the type of identifiers a storage uses
 * @param <S>
 *         the type of storage under the test
 */
public abstract class AbstractEntityRecordStorageTest<I, S extends EntityRecordStorage<I>>
        extends AbstractStorageTest<I, EntityRecord, S> {

    private static <I> EntityRecord newStorageRecord(I id, EntityState state) {
        Any wrappedState = pack(state);
        EntityRecord record = EntityRecord
                .newBuilder()
                .setEntityId(Identifier.pack(id))
                .setState(wrappedState)
                .setVersion(GivenVersion.withNumber(0))
                .build();
        return record;
    }

    /**
     * Creates an unique {@code Message} with the specified ID.
     *
     * <p>Two calls for the same ID should return messages, which are not equal.
     *
     * @param id
     *         the ID for the message
     * @return the unique {@code Message}
     */
    protected abstract EntityState newState(I id);

    @Override
    protected EntityRecord newStorageRecord(I id) {
        return newStorageRecord(id, newState(id));
    }

    @Test
    @DisplayName("write and read record by Message ID")
    void writeAndReadByMessageId() {
        EntityRecordStorage<I> storage = storage();
        I id = newId();
        EntityRecord expected = newStorageRecord(id);
        storage.write(id, expected);

        Optional<EntityRecord> optional = storage.read(id);
        assertTrue(optional.isPresent());
        EntityRecord actual = optional.get();

        assertEquals(expected, actual);
        close(storage);
    }

    @Test
    @DisplayName("retrieve empty iterator if storage is empty")
    void retrieveEmptyIterator() {
        FieldMask nonEmptyFieldMask = FieldMask
                .newBuilder()
                .addPaths("invalid-path")
                .build();
        ResponseFormat format = ResponseFormat
                .newBuilder()
                .setFieldMask(nonEmptyFieldMask)
                .vBuild();
        EntityRecordStorage storage = storage();
        Iterator empty = storage.readAll(format);

        assertNotNull(empty);
        assertFalse(empty.hasNext(), "Iterator is not empty!");
    }

    @Test
    @DisplayName("delete record")
    void deleteRecord() {
        EntityRecordStorage<I> storage = storage();
        I id = newId();
        EntityRecord record = newStorageRecord(id);

        // Write the record.
        storage.write(id, record);

        // Delete the record.
        assertTrue(storage.delete(id));

        // There's no record with such ID.
        assertFalse(storage.read(id)
                           .isPresent());
    }

    @Test
    @DisplayName("given field mask, read single record")
    void singleRecord() {
        I id = newId();
        EntityRecord record = newStorageRecord(id);
        EntityRecordStorage<I> storage = storage();
        storage.write(id, record);

        EntityState state = newState(id);
        FieldMask idMask = fromFieldNumbers(state.getClass(), 1);

        Optional<EntityRecord> optional = storage.read(id, idMask);
        assertTrue(optional.isPresent());
        EntityRecord entityRecord = optional.get();

        Message unpacked = unpack(entityRecord.getState());
        assertFalse(isDefault(unpacked));
    }

    @SuppressWarnings("MethodWithMultipleLoops")
    @Test
    @DisplayName("given field mask, read multiple records")
    void multipleRecords() {
        EntityRecordStorage<I> storage = storage();
        int count = 10;
        List<I> ids = new ArrayList<>();
        Class<? extends EntityState> stateClass = null;

        for (int i = 0; i < count; i++) {
            I id = newId();
            EntityState state = newState(id);
            if (stateClass == null) {
                stateClass = state.getClass();
            }
            EntityRecord record = newStorageRecord(id, state);
            storage.write(id, record);
            ids.add(id);
        }

        int bulkCount = count / 2;
        FieldMask fieldMask = fromFieldNumbers(stateClass, 2);
        Iterator<EntityRecord> readRecords = storage.readAll(ids.subList(0, bulkCount), fieldMask);
        List<EntityRecord> readList = newArrayList(readRecords);
        assertThat(readList).hasSize(bulkCount);
        for (EntityRecord record : readList) {
            Message state = unpack(record.getState());
            assertMatchesMask(state, fieldMask);
        }
    }

    @Test
    @DisplayName("given bulk of records, write them for the first time")
    void forTheFirstTime() {
        EntityRecordStorage<I> storage = storage();
        int bulkSize = 5;

        Map<I, EntityRecordWithColumns<I>> initial = new HashMap<>(bulkSize);

        for (int i = 0; i < bulkSize; i++) {
            I id = newId();
            EntityRecord record = newStorageRecord(id);
            initial.put(id, EntityRecordWithColumns.create(id, record));
        }
        storage.writeAll(initial.values());

        Iterator<@Nullable EntityRecord> records =
                storage.readAll(initial.keySet());
        Collection<@Nullable EntityRecord> actual = newArrayList(records);

        Collection<@Nullable EntityRecord> expected =
                initial.values()
                       .stream()
                       .map(recordWithColumns -> recordWithColumns != null
                                                 ? recordWithColumns.record()
                                                 : nullRef())
                       .collect(toList());
        assertThat(actual).containsExactlyElementsIn(expected);

        close(storage);
    }

    @Test
    @DisplayName("given bulk of records, write them re-writing existing ones")
    void rewritingExisting() {
        int recordCount = 3;
        EntityRecordStorage<I> storage = storage();

        Map<I, EntityRecord> v1Records = new HashMap<>(recordCount);
        Map<I, EntityRecord> v2Records = new HashMap<>(recordCount);

        for (int i = 0; i < recordCount; i++) {
            I id = newId();
            EntityRecord record = newStorageRecord(id);

            // Some records are changed and some are not.
            EntityRecord alternateRecord = (i % 2 == 0)
                                           ? record
                                           : newStorageRecord(id);
            v1Records.put(id, record);
            v2Records.put(id, alternateRecord);
        }

        storage.writeAll(recordsWithColumnsFrom(v1Records));
        Iterator<EntityRecord> firstRevision = storage.readAll();
        RecordStorageTestEnv.assertIteratorsEqual(v1Records.values()
                                                           .iterator(), firstRevision);

        storage.writeAll(recordsWithColumnsFrom(v2Records));
        Iterator<EntityRecord> secondRevision =
                storage.readAll(ResponseFormat.getDefaultInstance());
        RecordStorageTestEnv.assertIteratorsEqual(v2Records.values()
                                                           .iterator(), secondRevision);
    }

    private List<EntityRecordWithColumns<I>>
    recordsWithColumnsFrom(Map<I, EntityRecord> recordMap) {
        return recordMap.entrySet()
                        .stream()
                        .map(entry -> withLifecycleColumns(entry.getKey(), entry.getValue()))
                        .collect(toList());
    }
}
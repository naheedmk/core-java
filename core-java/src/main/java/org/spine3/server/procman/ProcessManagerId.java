/*
 * Copyright 2015, TeamDev Ltd. All rights reserved.
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

package org.spine3.server.procman;

import com.google.protobuf.Message;
import org.spine3.protobuf.MessageFields;
import org.spine3.server.EntityId;
import org.spine3.server.procman.error.MissingProcessManagerIdException;

import static org.spine3.util.Identifiers.ID_PROPERTY_SUFFIX;

/**
 * A value object for process manager IDs.
 *
 * @param <I> the type of process manager IDs
 * @author Alexander Litus
 */
public class ProcessManagerId<I> extends EntityId<I> {

    /**
     * The standard name for properties holding an ID of a process manager.
     */
    public static final String PROPERTY_NAME = "processManagerId";

    /**
     * The standard name for a parameter containing a process manager ID.
     */
    public static final String PARAM_NAME = PROPERTY_NAME;

    /**
     * The process manager ID must be the first field in events/commands.
     */
    public static final int PROCESS_MANAGER_ID_FIELD_INDEX = 0;

    private ProcessManagerId(I value) {
        super(value);
    }

    /**
     * Creates a new non-null ID of a process manager.
     *
     * @param value id value
     * @return new manager instance
     */
    public static <I> ProcessManagerId<I> of(I value) {
        return new ProcessManagerId<>(value);
    }

    /**
     * Obtains a process manager ID from the passed command/event instance.
     *
     * <p>The ID value must be the first field of the proto file. Its name must end with the "id" suffix.
     *
     * @param message the command/event to get id from
     * @return value of the id
     */
    public static ProcessManagerId from(Message message) {
        final String fieldName = MessageFields.getFieldName(message, PROCESS_MANAGER_ID_FIELD_INDEX);
        if (!fieldName.endsWith(ID_PROPERTY_SUFFIX)) {
            throw new MissingProcessManagerIdException(message.getClass().getName(), fieldName);
        }
        try {
            final Message value = (Message) MessageFields.getFieldValue(message, PROCESS_MANAGER_ID_FIELD_INDEX);
            return new ProcessManagerId<>(value);
        } catch (RuntimeException e) {
            throw new MissingProcessManagerIdException(message, MessageFields.toAccessorMethodName(fieldName), e);
        }
    }
}

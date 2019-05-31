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
package io.spine.server.procman;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Message;
import io.spine.base.EventMessage;
import io.spine.core.Event;
import io.spine.core.Version;
import io.spine.server.entity.Transaction;
import io.spine.server.entity.TransactionListener;
import io.spine.server.entity.TransactionTest;
import io.spine.server.entity.given.tx.Project;
import io.spine.server.entity.given.tx.ProjectId;
import io.spine.server.entity.given.tx.TxTestProcessManager;
import io.spine.server.entity.given.tx.event.TxProjectCreated;
import io.spine.server.entity.given.tx.event.TxTaskAdded;
import io.spine.server.type.EventEnvelope;
import io.spine.validate.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;

import static io.spine.protobuf.AnyPacker.unpack;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PmTransaction should")
class PmTransactionTest
        extends TransactionTest<ProjectId,
                                ProcessManager<ProjectId, Project, Project.Builder>,
                                Project,
                                Project.Builder> {

    private static final ProjectId ID = ProjectId.newBuilder()
                                                 .setId("procman-transaction-should-project")
                                                 .build();

    @Override
    protected Transaction<
            ProjectId,
            ProcessManager<ProjectId, Project, Project.Builder>,
            Project,
            Project.Builder
            >
    createTx(ProcessManager<ProjectId, Project, Project.Builder> entity) {
        return new PmTransaction<>(entity);
    }

    @Override
    protected Transaction<
            ProjectId,
            ProcessManager<ProjectId, Project, Project.Builder>,
            Project,
            Project.Builder
            >
    createTxWithState(ProcessManager<ProjectId, Project, Project.Builder> entity,
                      Project state,
                      Version version) {
        return new PmTransaction<>(entity, state, version);
    }

    @Override
    protected Transaction<
            ProjectId,
            ProcessManager<ProjectId, Project, Project.Builder>,
            Project,
            Project.Builder
            >
    createTxWithListener(
            ProcessManager<ProjectId, Project, Project.Builder> entity,
            TransactionListener<
                    ProjectId,
                    ProcessManager<ProjectId, Project, Project.Builder>,
                    Project,
                    Project.Builder
                    > listener) {
        PmTransaction<ProjectId, Project, Project.Builder> transaction =
                new PmTransaction<>(entity);
        transaction.setListener(listener);
        return transaction;
    }

    @Override
    protected ProcessManager<ProjectId, Project, Project.Builder> createEntity() {
        return new TxTestProcessManager(ID);
    }

    @Override
    protected
    ProcessManager<ProjectId, Project, Project.Builder>
    createEntity(ImmutableList<ConstraintViolation> violations) {
        return new TxTestProcessManager(ID, violations);
    }

    @Override
    protected Project createNewState() {
        return Project.newBuilder()
                      .setId(ID)
                      .setName("The new project name for procman tx tests")
                      .build();
    }

    @Override
    protected void
    checkEventReceived(ProcessManager<ProjectId, Project, Project.Builder> entity, Event event) {
        TxTestProcessManager aggregate = (TxTestProcessManager) entity;
        Message actualMessage = unpack(event.getMessage());
        assertTrue(aggregate.receivedEvents()
                            .contains(actualMessage));
    }

    @Override
    protected EventMessage createEventMessage() {
        return TxProjectCreated.newBuilder()
                               .setProjectId(ID)
                               .build();
    }

    @Override
    protected EventMessage createEventThatFailsInHandler() {
        return TxTaskAdded.newBuilder()
                          .setProjectId(ID)
                          .build();
    }

    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
    // Method called to dispatch event.
    @Override
    protected void applyEvent(Transaction tx, Event event) {
        PmTransaction cast = (PmTransaction) tx;
        EventEnvelope envelope = EventEnvelope.of(event);
        cast.dispatchEvent(envelope);
    }
}

/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.server.integration;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Message;
import io.spine.base.Error;
import io.spine.core.RejectionClass;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.Character.LINE_SEPARATOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Mykhailo Drachuk
 */
@VisibleForTesting
public abstract class CommandAcksVerifier {

    abstract void verify(CommandAcks acks);

    public static CommandAcksVerifier acked(int expectedCount) {
        checkArgument(expectedCount >= 0, "0 or more acknowledgements must be expected.");

        return new CommandAcksVerifier() {
            @Override
            void verify(CommandAcks acks) {
                int actualCount = acks.count();
                String moreOrLess = compare(actualCount, expectedCount);
                assertEquals(
                        "Bounded Context acknowledged " + moreOrLess + " commands than expected",
                        expectedCount, actualCount);
            }
        };
    }

    private static String compare(int actualCount, int expectedCount) {
        return (expectedCount < actualCount) ? "more" : "less";
    }

    /*
     * Factory methods for verifying acks with errors.
     ******************************************************************************/

    public static CommandAcksVerifier ackedWithoutErrors() {
        return new CommandAcksVerifier() {
            @Override
            void verify(CommandAcks acks) {
                assertTrue("Bounded Context unexpectedly erred", acks.containNoErrors());
            }
        };
    }

    public static CommandAcksVerifier ackedWithError() {
        return new CommandAcksVerifier() {
            @Override
            void verify(CommandAcks acks) {
                assertTrue("Bounded Context unexpectedly did not err", acks.containErrors());
            }
        };
    }

    public static CommandAcksVerifier ackedWithError(Error error) {
        return new CommandAcksVerifier() {
            @Override
            void verify(CommandAcks acks) {
                assertTrue("Bounded Context did not contain an expected error"
                                   + error.getMessage(),
                           acks.containError(error));
            }
        };
    }

    public static CommandAcksVerifier ackedWithError(ErrorQualifier qualifier) {
        return new CommandAcksVerifier() {
            @Override
            void verify(CommandAcks acks) {
                assertTrue("Bounded Context did not contain an expected error. "
                                   + qualifier.description(),
                           acks.containError(qualifier));
            }
        };
    }

    /*
     * Factory methods for verifying acks with rejections.
     ******************************************************************************/

    public static CommandAcksVerifier ackedWithoutRejections() {
        return new CommandAcksVerifier() {
            @Override
            void verify(CommandAcks acks) {
                assertTrue("Bounded Context unexpectedly rejected a message",
                           acks.containNoRejections());
            }
        };
    }

    public static CommandAcksVerifier ackedWithRejections() {
        return new CommandAcksVerifier() {
            @Override
            void verify(CommandAcks acks) {
                assertTrue("Bounded Context did not reject any messages",
                           acks.containRejections());
            }
        };
    }

    public static CommandAcksVerifier ackedWithRejections(Class<? extends Message> type) {
        RejectionClass rejectionClass = RejectionClass.of(type);
        return ackedWithRejections(rejectionClass);
    }

    public static CommandAcksVerifier ackedWithRejections(RejectionClass type) {
        Class<? extends Message> domainRejection = type.value();
        return new CommandAcksVerifier() {
            @Override
            void verify(CommandAcks acks) {
                assertTrue("Bounded Context did not reject a message of type:"
                                   + domainRejection.getName(),
                           acks.containRejections(type));
            }
        };
    }

    public static CommandAcksVerifier ackedWithRejection(Message domainRejection) {
        return new CommandAcksVerifier() {
            @Override
            void verify(CommandAcks acks) {
                assertTrue("Bounded Context did not reject a message:"
                                   + LINE_SEPARATOR + domainRejection,
                           acks.containRejection(domainRejection));
            }
        };
    }

    public static CommandAcksVerifier
    ackedWithRejections(Message rejection1, Message rejection2, Message... otherRejections) {
        return new CommandAcksVerifier() {
            @Override
            void verify(CommandAcks acks) {
                ackedWithRejection(rejection1).verify(acks);
                ackedWithRejection(rejection2).verify(acks);
                for (Message rejection : otherRejections) {
                    ackedWithRejection(rejection).verify(acks);
                }
            }
        };
    }

    /*
     * Methods incorporating verifiers.
     ******************************************************************************/

    public CommandAcksVerifier and(CommandAcksVerifier otherVerifier) {
        return new CommandsAcksListVerifier(this, otherVerifier);
    }

    private static class CommandsAcksListVerifier extends CommandAcksVerifier {

        private final List<CommandAcksVerifier> verifiers = newLinkedList();

        private CommandsAcksListVerifier(CommandAcksVerifier first, CommandAcksVerifier second) {
            verifiers.add(first);
            if (second instanceof CommandsAcksListVerifier) {
                verifiers.addAll(((CommandsAcksListVerifier) second).verifiers);
            } else {
                verifiers.add(second);
            }
        }

        @Override
        void verify(CommandAcks acks) {
            for (CommandAcksVerifier verifier : verifiers) {
                verifier.verify(acks);
            }
        }

        @Override
        public CommandAcksVerifier and(CommandAcksVerifier verifier) {
            verifiers.add(verifier);
            return this;
        }
    }

    public CommandAcksVerifier withoutErrors() {
        CommandAcksVerifier noErrors = ackedWithoutErrors();
        return this.and(noErrors);
    }

    public CommandAcksVerifier withError() {
        CommandAcksVerifier withError = ackedWithError();
        return this.and(withError);
    }

    public CommandAcksVerifier withError(Error error) {
        CommandAcksVerifier withError = ackedWithError(error);
        return this.and(withError);
    }

    public CommandAcksVerifier withError(ErrorQualifier qualifier) {
        CommandAcksVerifier withError = ackedWithError(qualifier);
        return this.and(withError);
    }

    public CommandAcksVerifier withoutErrorsOrRejections() {
        CommandAcksVerifier noRejections = ackedWithoutRejections();
        CommandAcksVerifier noErrors = ackedWithoutErrors();
        return this.and(noRejections.and(noErrors));
    }

    public CommandAcksVerifier withoutRejections() {
        CommandAcksVerifier noRejections = ackedWithoutRejections();
        return this.and(noRejections);
    }

    public CommandAcksVerifier withRejections() {
        CommandAcksVerifier someRejection = ackedWithRejections();
        return this.and(someRejection);
    }

    public CommandAcksVerifier withRejections(Class<? extends Message> type) {
        CommandAcksVerifier rejectedType = ackedWithRejections(type);
        return this.and(rejectedType);
    }

    public CommandAcksVerifier withRejections(RejectionClass type) {
        CommandAcksVerifier rejectedType = ackedWithRejections(type);
        return this.and(rejectedType);
    }

    public CommandAcksVerifier withRejection(Message domainRejection) {
        CommandAcksVerifier oneRejection = ackedWithRejection(domainRejection);
        return this.and(oneRejection);
    }

    public CommandAcksVerifier
    withRejections(Message rejection1, Message rejection2, Message... otherRejections) {
        CommandAcksVerifier multipleRejections =
                ackedWithRejections(rejection1, rejection2, otherRejections);
        return this.and(multipleRejections);
    }
}

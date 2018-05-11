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

package io.spine.server.procman.given;

import com.google.protobuf.Empty;
import io.spine.core.React;
import io.spine.server.command.Assign;
import io.spine.server.procman.ProcessManager;
import io.spine.server.tuple.EitherOfThree;
import io.spine.test.procman.quiz.PmQuiz;
import io.spine.test.procman.quiz.PmQuizId;
import io.spine.test.procman.quiz.PmQuizVBuilder;
import io.spine.test.procman.quiz.PmAnswer;
import io.spine.test.procman.quiz.PmQuestionId;
import io.spine.test.procman.quiz.command.PmAnswerQuestion;
import io.spine.test.procman.quiz.command.PmStartQuiz;
import io.spine.test.procman.quiz.event.PmQuestionAnswered;
import io.spine.test.procman.quiz.event.PmQuestionFailed;
import io.spine.test.procman.quiz.event.PmQuestionSolved;
import io.spine.test.procman.quiz.event.PmQuizStarted;

import java.util.List;

/**
 * A quiz is started using {@link PmStartQuiz Start Quiz command} which defines a question set, and 
 * the question are answered using {@link PmAnswerQuestion Answer Question commands}.
 */
class QuizProcman extends ProcessManager<PmQuizId, PmQuiz, PmQuizVBuilder> {

    protected QuizProcman(PmQuizId id) {
        super(id);
    }

    @Assign
    PmQuizStarted handle(PmStartQuiz command) {
        return PmQuizStarted.newBuilder()
                            .setQuizId(command.getQuizId())
                            .addAllQuestion(command.getQuestionList())
                            .build();
    }

    @Assign
    PmQuestionAnswered handle(PmAnswerQuestion command) {
        final PmQuestionAnswered event =
                PmQuestionAnswered.newBuilder()
                                  .setQuizId(command.getQuizId())
                                  .setAnswer(command.getAnswer())
                                  .build();
        return event;
    }

    @React
    void on(PmQuizStarted event) {
        getBuilder().setId(event.getQuizId());
    }

    @React
    EitherOfThree<PmQuestionSolved, PmQuestionFailed, Empty> on(PmQuestionAnswered event) {
        final PmAnswer answer = event.getAnswer();
        final PmQuizId examId = event.getQuizId();
        final PmQuestionId questionId = answer.getQuestionId();

        if (questionIsClosed(questionId)) {
            return EitherOfThree.withC(Empty.getDefaultInstance());
        }

        final boolean answerIsCorrect = answer.getCorrect();
        if (answerIsCorrect) {
            final PmQuestionSolved reaction =
                    PmQuestionSolved.newBuilder()
                                    .setQuizId(examId)
                                    .setQuestionId(questionId)
                                    .build();
            return EitherOfThree.withA(reaction);
        } else {
            final PmQuestionFailed reaction =
                    PmQuestionFailed.newBuilder()
                                    .setQuizId(examId)
                                    .setQuestionId(questionId)
                                    .build();
            return EitherOfThree.withB(reaction);
        }
    }

    private boolean questionIsClosed(final PmQuestionId questionId) {
        final List<PmQuestionId> openQuestions = getBuilder().getOpenQuestion();
        final boolean containedInOpenQuestions = openQuestions.contains(questionId);
        return !containedInOpenQuestions;
    }

    @React
    void on(PmQuestionSolved event) {
        final PmQuestionId questionId = event.getQuestionId();
        removeOpenQuestion(questionId);
        getBuilder().addSolvedQuestion(questionId);
    }

    @React
    void on(PmQuestionFailed event) {
        final PmQuestionId questionId = event.getQuestionId();
        removeOpenQuestion(questionId);
        getBuilder().addFailedQuestion(questionId);
    }

    private void removeOpenQuestion(PmQuestionId questionId) {
        final List<PmQuestionId> openQuestions = getBuilder().getOpenQuestion();
        final int index = openQuestions.indexOf(questionId);
        getBuilder().removeOpenQuestion(index);
    }
}

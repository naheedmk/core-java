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

package io.spine.server.delivery.given;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.spine.core.Event;
import io.spine.protobuf.AnyPacker;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;
import io.spine.server.event.React;
import io.spine.test.delivery.AddNumber;
import io.spine.test.delivery.Calc;
import io.spine.test.delivery.NumberAdded;
import io.spine.test.delivery.NumberImported;
import io.spine.test.delivery.NumberReacted;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A calculator that is only capable of adding integer numbers.
 */
public class CalcAggregate extends Aggregate<String, Calc, Calc.Builder> {

    private static final Multimap<String, CalculatorSignal> signals = ArrayListMultimap.create();

    private static final Map<String, NumberImported> firstImportEvents = new HashMap<>();

    @Assign
    NumberAdded handle(AddNumber command) {
        verifyFirstImport();
        int value = command.getValue();
        NumberAdded numberAdded = NumberAdded
                .newBuilder()
                .setValue(value)
                .setCalculatorId(id())
                .vBuild();
        signals.put(id(), numberAdded);
        return numberAdded;
    }

    @React
    NumberAdded on(NumberReacted event) {
        verifyFirstImport();
        NumberAdded numberAdded = NumberAdded
                .newBuilder()
                .setCalculatorId(event.getCalculatorId())
                .setValue(event.getValue())
                .vBuild();
        signals.put(id(), numberAdded);
        return numberAdded;
    }

    private void verifyFirstImport() {
        NumberImported firstImportEvent = firstImportEvents.get(id());
        if(firstImportEvent != null) {
            int value = firstImportEvent.getValue();
            Optional<Event> first = recentHistory().stream()
                                                   .filter(e -> ((CalculatorSignal) AnyPacker.unpack(
                                                           e.getMessage())).getValue() == value)
                                                   .findFirst();
            if(!first.isPresent()) {
                System.out.println("The first import event of " + id() + " is absent in the history.");
            }
        }
    }

    @Apply
    private void on(NumberAdded event) {
        int currentSum = builder().getSum();
        builder().setSum(currentSum + event.getValue());
    }

    @Apply(allowImport = true)
    private void on(NumberImported event) {
        String id = id();
        if(signals.get(id).isEmpty()) {
            if (!firstImportEvents.containsKey(id)) {
                firstImportEvents.put(id, event);
                System.out.println("The candidate for the first imported event is " + id);
            }
        }
        int currentSum = builder().getSum();
        builder().setSum(currentSum + event.getValue());
    }
}

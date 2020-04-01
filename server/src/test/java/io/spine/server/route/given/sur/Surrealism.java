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

package io.spine.server.route.given.sur;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;
import java.util.Set;

/**
 * Provides utilities and some constants behind
 * the <a href="https://en.wikipedia.org/wiki/Surrealist_Manifesto">Surrealism Manifesto</a> story.
 */
public final class Surrealism {

    public static final ArtistName BRETON = name("André Breton");
    public static final ArtistName GOLL = name("Yvan Goll");

    private static final ImmutableMap<ArtistName, ArtistName> opponents = ImmutableMap.of(
            GOLL, BRETON
    );

    /** Prevents instantiation of this utility class. */
    private Surrealism() {
    }

    public static ArtistName name(String value) {
        return ArtistName
                .newBuilder()
                .setValue(value)
                .build();
    }

    static Optional<ArtistName> opponentOf(ArtistName name) {
        ArtistName opponent = opponents.get(name);
        return Optional.ofNullable(opponent);
    }

    static Set<ArtistName> allTheseGentlemen() {
        return ImmutableSet.<ArtistName>builder()
                .addAll(opponents.keySet())
                .addAll(opponents.values())
                .build();
    }
}
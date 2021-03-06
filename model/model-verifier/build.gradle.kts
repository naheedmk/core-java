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

import io.spine.gradle.internal.Deps

buildscript {
    apply(from = "$rootDir/version.gradle.kts")
}

group = "io.spine.tools"

val spineBaseVersion: String by extra

dependencies {
    implementation(gradleApi())
    implementation("io.spine.tools:spine-plugin-base:$spineBaseVersion")
    implementation("io.spine.tools:spine-model-compiler:$spineBaseVersion")
    implementation(project(":server"))
    implementation(project(":model-assembler"))

    testImplementation(gradleTestKit())
    testImplementation("io.spine:spine-testlib:$spineBaseVersion")
    testImplementation("io.spine.tools:spine-plugin-testlib:$spineBaseVersion")
    testImplementation(Deps.test.junitPioneer)
    testImplementation(project(":testutil-server"))
}

tasks.test {
    dependsOn("publishToMavenLocal",
              ":core:publishToMavenLocal",
              ":client:publishToMavenLocal",
              ":server:publishToMavenLocal",
              ":model-assembler:publishToMavenLocal")
}

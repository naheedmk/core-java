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

package io.spine.server;

import com.google.common.annotations.VisibleForTesting;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.Optional;

import static com.google.common.base.Strings.emptyToNull;
import static io.spine.server.DeploymentType.APPENGINE_CLOUD;
import static io.spine.server.DeploymentType.APPENGINE_EMULATOR;
import static io.spine.server.DeploymentType.STANDALONE;
import static java.util.Optional.ofNullable;

/**
 * The Default implementation of {@linkplain io.spine.server.ServerEnvironment.Provider
 * server environment provider}.
 */
class SystemEnvironmentProvider implements ServerEnvironment.Provider {

    @VisibleForTesting
    static final String APP_ENGINE_ENVIRONMENT_PATH =
            "com.google.appengine.runtime.environment";
    @VisibleForTesting
    static final String APP_ENGINE_ENVIRONMENT_PRODUCTION_VALUE = "Production";
    @VisibleForTesting
    static final String APP_ENGINE_ENVIRONMENT_DEVELOPMENT_VALUE = "Development";

    private @MonotonicNonNull DeploymentType deploymentType = null;

    /** Prevent instantiation from outside. */
    private SystemEnvironmentProvider() {
    }

    public static ServerEnvironment.Provider newInstance() {
        return new SystemEnvironmentProvider();
    }

    @Override
    public DeploymentType getDeploymentType() {
        if (deploymentType == null) {
            deploymentType = readDeploymentType();
        }
        return deploymentType;
    }

    private static DeploymentType readDeploymentType() {
        Optional<String> gaeEnvironment = getProperty(APP_ENGINE_ENVIRONMENT_PATH);
        if (gaeEnvironment.isPresent()) {
            if (APP_ENGINE_ENVIRONMENT_DEVELOPMENT_VALUE.equals(gaeEnvironment.get())) {
                return APPENGINE_EMULATOR;
            }
            if (APP_ENGINE_ENVIRONMENT_PRODUCTION_VALUE.equals(gaeEnvironment.get())) {
                return APPENGINE_CLOUD;
            }
        }
        return STANDALONE;
    }

    @SuppressWarnings("AccessOfSystemProperties") /*  Based on system property. */
    private static Optional<String> getProperty(String path) {
        return ofNullable(emptyToNull(System.getProperty(path)));
    }
}

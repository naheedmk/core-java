/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.system.server;

import io.spine.annotation.Internal;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The entry point of a system context API exposed to its domain counterpart.
 */
@Internal
public interface SystemMonitor {

    /**
     * Obtains the system context write side.
     */
    SystemWriteSide writeSide();

    /**
     * Obtains the system context read side.
     */
    SystemReadSide readSide();

    /**
     * Closes the underlying system context.
     *
     * @throws Exception
     *         if the context thrown an exception when closing
     */
    void closeSystemContext() throws Exception;

    /**
     * Creates a new instance of {@code SystemMonitor} for the given system context.
     *
     * @param context
     *         the system context to monitor
     * @return new system monitor
     */
    static SystemMonitor newInstance(SystemContext context) {
        checkNotNull(context);
        return new DefaultSystemMonitor(context);
    }
}

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

package io.spine.server.entity;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.spine.annotation.Internal;
import io.spine.server.tenant.IdInTenant;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The cache of {@code Entity} states for a certain {@code Repository} and
 * {@linkplain #startCaching(Object) selected} identifiers.
 *
 * <p>The users of this class should keep the number of the simultaneously cached entities
 * reasonable due to a potentially huge significant memory footprint.
 *
 * @param <I>
 *         the type of {@code Entity} identifiers
 * @param <E>
 *         the type of entity
 */
@Internal
public final class RepositoryCache<I, E extends Entity<I, ?>> {

    private final Map<IdInTenant<I>, E> cache = Maps.newConcurrentMap();
    private final Set<IdInTenant<I>> idsToCache = Sets.newConcurrentHashSet();

    private final boolean multitenant;
    private final Load<I, E> loadFn;
    private final Store<E> storeFn;

    public RepositoryCache(boolean multitenant, Load<I, E> loadFn, Store<E> storeFn) {
        this.multitenant = multitenant;
        this.loadFn = loadFn;
        this.storeFn = storeFn;
    }

    public E load(I id) {
        IdInTenant<I> idInTenant = idInTenant(id);
        if (!idsToCache.contains(idInTenant)) {
            return loadFn.apply(idInTenant.value());
        }

        if (!cache.containsKey(idInTenant)) {
            E entity = loadFn.apply(idInTenant.value());
            cache.put(idInTenant, entity);
            return entity;
        }
        return cache.get(idInTenant);
    }

    /**
     * Starts caching the {@code load} and {@code store} operation results in memory
     * for the given {@code Entity} identifier.
     *
     * @param id
     *         an identifier of the entity to cache
     */
    public void startCaching(I id) {
        idsToCache.add(idInTenant(id));
    }

    /**
     * Stops caching the {@code load} and {@code store} operations for the
     * {@code Entity} with the passed identifier.
     *
     * <p>Stores the cached entity state to the entity repository via
     * {@linkplain RepositoryCache#RepositoryCache(boolean, Load, Store) pre-configured}
     * {@code Store} function.
     *
     * @param id
     *         an identifier of the entity to cache
     */
    public void stopCaching(I id) {
        IdInTenant<I> idInTenant = idInTenant(id);
        E entity = checkNotNull(cache.get(idInTenant));
        storeFn.accept(entity);
        cache.remove(idInTenant);
        idsToCache.remove(idInTenant);
    }

    private IdInTenant<I> idInTenant(I id) {
        return IdInTenant.of(id, multitenant);
    }

    public void store(E entity) {
        I id = entity.id();
        IdInTenant<I> idInTenant = idInTenant(id);
        if (idsToCache.contains(idInTenant)) {
            cache.put(idInTenant, entity);
        } else {
            storeFn.accept(entity);
        }
    }

    /**
     * A function which loads an {@code Entity} state by ID from its real repository.
     *
     * @param <I>
     *         the type of {@code Entity} identifiers
     * @param <E>
     *         the type of entity
     */
    @FunctionalInterface
    public interface Load<I, E extends Entity<I, ?>> extends Function<I, E> {
    }

    /**
     * A function which stores the {@code Entity} state to its real repository.
     *
     * @param <E>
     *         the type of entity
     */
    @FunctionalInterface
    public interface Store<E extends Entity> extends Consumer<E> {
    }
}
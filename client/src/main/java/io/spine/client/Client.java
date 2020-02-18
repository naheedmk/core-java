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

package io.spine.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.Message;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.spine.client.grpc.CommandServiceGrpc;
import io.spine.client.grpc.CommandServiceGrpc.CommandServiceBlockingStub;
import io.spine.client.grpc.QueryServiceGrpc;
import io.spine.client.grpc.QueryServiceGrpc.QueryServiceBlockingStub;
import io.spine.client.grpc.SubscriptionServiceGrpc;
import io.spine.client.grpc.SubscriptionServiceGrpc.SubscriptionServiceBlockingStub;
import io.spine.client.grpc.SubscriptionServiceGrpc.SubscriptionServiceStub;
import io.spine.core.Command;
import io.spine.core.TenantId;
import io.spine.core.UserId;
import io.spine.protobuf.AnyPacker;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Preconditions2.checkNotDefaultArg;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * The gRPC-based gateway for backend services such as {@code CommandService},
 * {@code QueryService}, or {@code SubscriptionService}.
 *
 * <p>A connection can be established via {@linkplain #connectTo(String, int) host/port}
 * combination, or via already available {@link #usingChannel(ManagedChannel) ManagedChannel}.
 *
 * <p>Multitenant applications need to specify {@link Builder#forTenant(TenantId) TenantId}
 * for a new client connection. Single-tenant applications do nothing about it.
 *
 * <p>Client requests can be created on behalf of a {@linkplain #asGuest() guest user},
 * if the user is not yet authenticated, and on behalf of the
 * {@linkplain #onBehalfOf(UserId) current user} after the user is authenticated.
 *
 * <p>Please note that Spine client-side library does not define the authentication process.
 * The {@code Client} class simply relies on the fact that {@link UserId} passed to the method
 * {@link #onBehalfOf(UserId)} represents a valid logged-in user, ID of whom the client application
 * got (presumably as field of a {@code UserLoggedIn} event) following due authentication process.
 * The server-side code also needs to make sure that the {@link UserId} matches security
 * constraints of the backend services. Security arrangements is not a part of the Spine client-side
 * library either.
 *
 * <p>Subscriptions to {@linkplain SubscriptionRequest entity states} or
 * {@linkplain EventSubscriptionRequest events} must be {@linkplain #cancel(Subscription)
 * cancelled} when no longer needed to preserve both client-side and backend resources.
 *
 * <p>The client connection must be {@link #close() closed} when the application finishes its work.
 */
public class Client implements AutoCloseable {

    /** The number of seconds to wait when {@linkplain #close() closing} the client. */
    public static final Timeout DEFAULT_SHUTDOWN_TIMEOUT = Timeout.of(5, SECONDS);

    /** Default ID for a guest user. */
    public static final UserId DEFAULT_GUEST_ID = user("guest");

    private final @Nullable TenantId tenant;
    private final UserId guestUser;
    private final ManagedChannel channel;
    private final Timeout shutdownTimeout;
    private final QueryServiceBlockingStub queryService;
    private final CommandServiceBlockingStub commandService;
    private final SubscriptionServiceStub subscriptionService;
    private final SubscriptionServiceBlockingStub blockingSubscriptionService;

    /** Subscriptions created by the client which are not cancelled yet. */
    private final ActiveSubscriptions subscriptions;

    /**
     * Creates a builder for a client connected to the specified address.
     *
     * <p>The returned builder will create {@code ManagedChannel} with the default configuration.
     * For a channel with custom configuration please use {@link #usingChannel(ManagedChannel)}.
     *
     * @see #usingChannel(ManagedChannel)
     * @see #inProcess(String)
     */
    public static Builder connectTo(String host, int port) {
        checkNotEmptyOrBlank(host);
        return new Builder(host, port);
    }

    /**
     * Creates a builder for a client which will use the passed channel for the communication
     * with the backend services.
     *
     * <p>Use this method when a channel with custom configuration is needed for your client
     * application.
     *
     * @see #connectTo(String, int)
     * @see #inProcess(String)
     * @see ManagedChannel
     */
    public static Builder usingChannel(ManagedChannel channel) {
        checkNotNull(channel);
        return new Builder(channel);
    }

    /**
     * Creates a client which will be connected to the in-process server with the passed name.
     *
     * <p>The client is fully-featured, high performance, and is useful in testing.
     *
     * @see #connectTo(String, int)
     * @see #usingChannel(ManagedChannel)
     */
    public static Builder inProcess(String serverName) {
        checkNotEmptyOrBlank(serverName);
        return new Builder(serverName);
    }

    /**
     * Creates a new client which uses the passed channel for communications
     * with the backend services.
     */
    private Client(Builder builder) {
        this.tenant = builder.tenant;
        this.guestUser = builder.guestUser;
        this.channel = checkNotNull(builder.channel);
        this.shutdownTimeout = checkNotNull(builder.shutdownTimeout);
        this.commandService = CommandServiceGrpc.newBlockingStub(channel);
        this.queryService = QueryServiceGrpc.newBlockingStub(channel);
        this.subscriptionService = SubscriptionServiceGrpc.newStub(channel);
        this.blockingSubscriptionService = SubscriptionServiceGrpc.newBlockingStub(channel);
        this.subscriptions = new ActiveSubscriptions();
    }

    /**
     * Obtains the tenant of this client connection in a multitenant application,
     * and empty {@code Optional} in a single-tenant one.
     *
     * @see Builder#forTenant(TenantId)
     */
    public Optional<TenantId> tenant() {
        return Optional.ofNullable(tenant);
    }

    /**
     * Closes the client by shutting down the gRPC connection.
     *
     * <p>Subscriptions created by this client which were not cancelled
     * {@linkplain #cancel(Subscription) directly} will be cancelled.
     *
     * @see #isOpen()
     */
    @Override
    public void close() {
        if (!isOpen()) {
            return;
        }
        subscriptions.cancelAll(this);
        try {
            channel.shutdown()
                   .awaitTermination(shutdownTimeout.value(), shutdownTimeout.unit());
        } catch (InterruptedException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * Same as {@link #close()}.
     */
    public void shutdown() {
        close();
    }

    /**
     * Verifies if the client connection is open.
     *
     * @see #close()
     */
    public boolean isOpen() {
        return !channel.isTerminated();
    }

    /**
     * Creates a builder for requests on behalf of the passed user.
     *
     * @see #asGuest()
     */
    public ClientRequest onBehalfOf(UserId user) {
        checkNotDefaultArg(user);
        return new ClientRequest(user, this);
    }

    /**
     * Creates a builder for posting guest requests.
     *
     * @see #onBehalfOf(UserId)
     */
    public ClientRequest asGuest() {
        return onBehalfOf(guestUser);
    }

    /**
     * Requests cancellation of the passed subscription.
     *
     * @see ClientRequest#subscribeTo(Class)
     * @see ClientRequest#subscribeToEvent(Class)
     */
    public void cancel(Subscription s) {
        blockingSubscriptionService.cancel(s);
        subscriptions.forget(s);
    }

    @VisibleForTesting
    ManagedChannel channel() {
        return channel;
    }

    @VisibleForTesting
    Timeout shutdownTimeout() {
        return shutdownTimeout;
    }

    @VisibleForTesting
    ActiveSubscriptions subscriptions() {
        return subscriptions;
    }

    /**
     * Creates a new request factory for the requests to be sent on behalf of the passed user.
     */
    ActorRequestFactory requestOf(UserId user) {
        return ActorRequestFactory
                .newBuilder()
                .setTenantId(tenant)
                .setActor(user)
                .build();
    }

    /**
     * Posts the command to the {@code CommandService}.
     */
    void post(Command c) {
        commandService.post(c);
    }

    /**
     * Queries the read-side with the specified query.
     */
    ImmutableList<Message> read(Query query) {
        ImmutableList<Message> result = queryService
                .read(query)
                .getMessageList()
                .stream()
                .map(EntityStateWithVersion::getState)
                .map(AnyPacker::unpack)
                .collect(toImmutableList());
        return result;
    }

    /**
     * Subscribes the given {@link StreamObserver} to the given topic and activates
     * the subscription.
     *
     * @param topic
     *         the topic to subscribe to
     * @param observer
     *         the observer to subscribe
     * @param <M>
     *         the type of the result messages
     * @return the activated subscription
     * @see #cancel(Subscription)
     */
    <M extends Message> Subscription subscribeTo(Topic topic, StreamObserver<M> observer) {
        Subscription subscription = blockingSubscriptionService.subscribe(topic);
        subscriptionService.activate(subscription, new SubscriptionObserver<>(observer));
        subscriptions.remember(subscription);
        return subscription;
    }

    private static UserId user(String value) {
        checkNotEmptyOrBlank(value);
        return UserId.newBuilder()
                     .setValue(value)
                     .build();
    }

    /**
     * The builder for the client.
     */
    public static final class Builder {

        /**
         * The channel to be used in the client.
         *
         * <p>If not set directly, the channel will be created using the assigned
         * host and port values.
         */
        private ManagedChannel channel;

        /**
         * The address of the host which will be used for creating an instance
         * of {@code ManagedChannel}.
         *
         * <p>This field is {@code null} if the builder is created using already made
         * {@code ManagedChannel}.
         */
        private @MonotonicNonNull String host;
        private int port;
        private @MonotonicNonNull Timeout shutdownTimeout;

        /**
         * The ID of the tenant in a multi-tenant application.
         *
         * <p>Is {@code null} in single-tenant applications.
         */
        private @Nullable TenantId tenant;

        /** The ID of the user for performing requests on behalf of a non-logged in user. */
        private UserId guestUser = DEFAULT_GUEST_ID;

        private Builder(ManagedChannel channel) {
            this.channel = checkNotNull(channel);
        }

        private Builder(String host, int port) {
            this.host = host;
            this.port = port;
        }

        private Builder(String processServerName) {
            this(channelForTesting(processServerName));
        }

        private static ManagedChannel channelForTesting(String serverName) {
            ManagedChannel result = InProcessChannelBuilder
                    .forName(serverName)
                    .directExecutor()
                    .build();
            return result;
        }

        private static ManagedChannel createChannel(String host, int port) {
            ManagedChannel result = ManagedChannelBuilder
                    .forAddress(host, port)
                    .build();
            return result;
        }

        /**
         * Assigns the tenant for the client connection to be built.
         *
         * <p>This method should be called only in multitenant applications.
         *
         * @param tenant
         *          a non-null and non-default ID of the tenant
         */
        public Builder forTenant(TenantId tenant) {
            this.tenant = checkNotDefaultArg(tenant);
            return this;
        }

        /**
         * Assigns the ID of the user for performing requests on behalf of non-logged in user.
         *
         * <p>If the not set directly, the value {@code "guest"} will be used.
         *
         * @param guestUser
         *         non-null and non-default value
         */
        public Builder withGuestId(UserId guestUser) {
           checkNotNull(guestUser);
           this.guestUser = checkNotDefaultArg(
                    guestUser, "Guest user ID cannot be a default value.");
           return this;
        }

        /**
         * Assigns the ID of the user for performing requests on behalf of non-logged in user.
         *
         * <p>If the not set directly, the value {@code "guest"} will be used.
         *
         * @param guestUser
         *         non-null and not empty or a blank value
         */
        public Builder withGuestId(String guestUser) {
            checkNotEmptyOrBlank(guestUser, "Guest user ID cannot be empty or blank.");
            return withGuestId(user(guestUser));
        }

        /**
         * Sets the timeout for the {@linkplain Client#close() shutdown operation} of the client.
         *
         * <p>If not specified directly, {@link Client#DEFAULT_SHUTDOWN_TIMEOUT} will be used.
         */
        public Builder shutdownTimout(long timeout, TimeUnit timeUnit) {
            checkNotNull(timeUnit);
            this.shutdownTimeout = Timeout.of(timeout, timeUnit);
            return this;
        }

        /**
         * Creates a new instance of the client.
         */
        public Client build() {
            if (channel == null) {
                checkNotNull(host, "Either channel or host/port must be specified.");
                channel = createChannel(host, port);
            }
            if (shutdownTimeout == null) {
                shutdownTimeout = DEFAULT_SHUTDOWN_TIMEOUT;
            }
            return new Client(this);
        }

        @VisibleForTesting
        @Nullable String host() {
            return host;
        }

        @VisibleForTesting
        int port() {
            return port;
        }

        @VisibleForTesting
        @Nullable ManagedChannel channel() {
            return channel;
        }
    }
}
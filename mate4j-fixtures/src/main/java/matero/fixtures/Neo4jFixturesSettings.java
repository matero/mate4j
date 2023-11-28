package matero.fixtures;

/*-
 * #%L
 * Mate4j/Fixtures
 * %%
 * Copyright (C) 2023 matero
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import matero.fixtures.Neo4jFixturesSettings.ExternalDb.ByProperties;
import matero.fixtures.Neo4jFixturesSettings.ExternalDb.ByUri;
import matero.fixtures.Neo4jFixturesSettings.InternalDb.InstantiationModel;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.HashSet.newHashSet;

public abstract class Neo4jFixturesSettings {
    protected static final boolean MUTABLE = false;
    private final boolean isImmutable;
    private @Nullable Loading loading;
    private @Nullable Rollback rollback;
    private @Nullable ConnectionDetails connectionDetails;

    private boolean showFixture;

    protected Neo4jFixturesSettings() {
        this(true);
    }

    protected Neo4jFixturesSettings(final boolean isImmutable) {
        this.isImmutable = isImmutable;
    }

    public abstract void configure();

    public final @NonNull Loading loading() {
        if (this.loading == null) {
            return Loading.UNIQUE;
        }
        return this.loading;
    }

    protected final void loading(final @NonNull Loading strategy) {
        if (this.loading != null && this.isImmutable) {
            throw new IllegalStateException("loading already configured to: " + this.loading);
        }
        this.loading = strategy;
    }

    public final @NonNull Rollback rollback() {
        if (this.rollback == null) {
            return Rollback.AFTER_TEST;
        }
        return this.rollback;
    }

    public final void rollback(final @NonNull Rollback strategy) {
        if (this.rollback != null && this.isImmutable) {
            throw new IllegalStateException("rollback already configured to: " + this.rollback);
        }
        this.rollback = strategy;
    }

    public boolean shouldShowFixture() {
        return this.showFixture;
    }

    public void showFixture() {
        showFixture(true);
    }

    public void dontShowFixture() {
        showFixture(false);
    }

    public void showFixture(final boolean flag) {
        this.showFixture = showFixture;
    }

    public final @NonNull ConnectionDetails connectionDetails() {
        if (this.connectionDetails == null) {
            throw new IllegalStateException("connection details not configured");
        }
        return this.connectionDetails;
    }

    /**
     * Strategy to be used when loading a fixture.
     */
    protected enum Loading {
        UNIQUE("Load the most specific fixture found for the test method."),
        INCREMENTAL("TBD: Load from most generic upto most specific fixture found for the test method.");

        /**
         * Describes the fixture loading.
         */
        final @NonNull Description description;

        /**
         * Creates the fixture loading strategy describing its intention.
         *
         * @param description The text describing the intention of the {@link Loading}.
         */
        Loading(final @NonNull String description) {
            this.description = Description.of(description);
        }
    }

    /**
     * When should a rollback should be applied.
     */
    protected enum Rollback {
        NEVER("Don't rollback under any circumstance."),
        AFTER_CLASS("Rollback after all tests in test class are executed."),
        AFTER_TEST("Rollback after test method is executed.");

        /**
         * Describes the rollback strategy.
         */
        final @NonNull Description description;

        /**
         * Creates the rollback strategy describing its intention.
         *
         * @param description The text describing the intention of the {@link Rollback}.
         */
        Rollback(final @NonNull String description) {
            this.description = Description.of(description);
        }
    }

    sealed interface ConnectionDetails
            permits ExternalDb,
            InternalDb {
    }

    protected final @NonNull ByProperties external() {
        if (this.connectionDetails != null && this.isImmutable) {
            throw new IllegalStateException("connectionDetails already configured to: " + this.connectionDetails);
        }
        final var externalDb = new ExternalDb.ByProperties();
        this.connectionDetails = externalDb;
        return externalDb;
    }

    protected final @NonNull ByUri external(final @NonNull String uri) {
        if (this.connectionDetails != null && this.isImmutable) {
            throw new IllegalStateException("connectionDetails already configured to: " + this.connectionDetails);
        }
        final var externalDb = new ExternalDb.ByUri(uri);
        this.connectionDetails = externalDb;
        return externalDb;
    }

    protected final @NonNull InternalDb internal() {
        return internal(InstantiationModel.SINGLETON);
    }

    protected final @NonNull InternalDb internal(final @NonNull InstantiationModel model) {
        if (this.connectionDetails != null && this.isImmutable) {
            throw new IllegalStateException("connectionDetails already configured to: " + this.connectionDetails);
        }
        final var internalDb = new InternalDb().instantiation(model);
        this.connectionDetails = internalDb;
        return internalDb;
    }

    /**
     * External neo4j database, requires connection details.
     * <p/>
     * Please node, framework is not able to configure instantiation.
     */
    public static abstract sealed class ExternalDb<SELF extends ExternalDb<SELF>>
            implements ConnectionDetails
            permits ExternalDb.ByProperties,
            ExternalDb.ByUri {
        private @NonNull AuthToken authToken = AuthTokens.none();

        @SuppressWarnings("unchecked")
        private SELF self() {
            return (SELF) this;
        }

        abstract @NonNull String uri();

        final @NonNull AuthToken authToken() {
            return this.authToken;
        }

        public final @NonNull SELF noAuth() {
            this.authToken = AuthTokens.none();
            return self();
        }

        public final @NonNull SELF basicAuth(
                final @NonNull String user,
                final @NonNull String password) {
            this.authToken = AuthTokens.basic(user, password);
            return self();
        }

        public final @NonNull SELF bearerAuth(final @NonNull String token) {
            this.authToken = AuthTokens.bearer(token);
            return self();
        }

        public final @NonNull SELF kerberosAuth(final @NonNull String base64EncodedTicket) {
            this.authToken = AuthTokens.kerberos(base64EncodedTicket);
            return self();
        }

        public final @NonNull SELF bearerAuth(
                final @NonNull String principal,
                final @NonNull String credentials,
                final @NonNull String realm,
                final @NonNull String scheme) {
            this.authToken = AuthTokens.custom(principal, credentials, realm, scheme);
            return self();
        }

        public final @NonNull SELF bearerAuth(
                final @NonNull String principal,
                final @NonNull String credentials,
                final @NonNull String realm,
                final @NonNull String scheme,
                final @NonNull Map<@NonNull String, @NonNull Object> parameters) {
            this.authToken = AuthTokens.custom(principal, credentials, realm, scheme, parameters);
            return self();
        }

        /**
         * Required connection details:
         * <ul>
         * <li>host</li>
         * <li>port</li>
         * <li>protocol</li>
         * <li>dbname</li>
         * <li>user</li>
         * <li>password</li>
         * </ul>
         */
        public static final class ByProperties
                extends ExternalDb<ByProperties> {
            private @NonNull String host = "localhost";
            private @Positive int port = 7474;
            private @NonNull Neo4JProtocol protocol = Neo4JProtocol.bolt;
            private @NonNull String dbname = "neo4j";

            ByProperties() {
            }

            @NonNull String host() {
                return this.host;
            }

            public @NonNull ByProperties host(final @NonNull String value) {
                this.host = value;
                return this;
            }

            @Positive int port() {
                return this.port;
            }

            public @NonNull ByProperties port(final @Positive int value) {
                this.port = value;
                return this;
            }

            @NonNull Neo4JProtocol protocol() {
                return this.protocol;
            }

            public @NonNull ByProperties protocol(final @NonNull Neo4JProtocol value) {
                this.protocol = value;
                return this;
            }

            @NonNull String dbname() {
                return this.dbname;
            }

            public @NonNull ByProperties dbname(final @NonNull String value) {
                this.dbname = value;
                return this;
            }

            @Override
            @NonNull String uri() {
                return this.protocol + "://" + this.host + ':' + this.port;
            }

            enum Neo4JProtocol {
                bolt
            }
        }

        /**
         * Required connection details:
         * <ul>
         * <li>connection url</li>
         * <li>user</li>
         * <li>password</li>
         * </ul>
         */
        public static final class ByUri
                extends ExternalDb<ByUri> {
            private final @NonNull String uri;

            ByUri(@NonNull String uri) {
                this.uri = uri;
            }

            @Override
            @NonNull String uri() {
                return uri;
            }
        }
    }

    /**
     * Database configured by the framework. Instantiation model is configurable to one of these options:
     * <ul>
     * <li>Singleton: 1 db for all tests</li>
     * <li>Per class: create one db per test class, drop it after all its test methods are executed</li>
     * <li>Per test: create one db per test method, drop it after test is executed</li>
     * </ul>
     * <p>
     * By default, Singleton instantiation is used.
     */
    public static final class InternalDb
            implements ConnectionDetails {
        private @NonNull InstantiationModel instantiationModel = InstantiationModel.SINGLETON;
        private final @NonNull Set<@NonNull Class<?>> procedureClasses = newHashSet(1);
        private final @NonNull Set<@NonNull Class<?>> functionClasses = newHashSet(1);
        private final @NonNull Set<@NonNull Class<?>> aggregationFunctionClasses = newHashSet(1);

        @NonNull InstantiationModel instantiation() {
            return this.instantiationModel;
        }

        public @NonNull InternalDb instantiation(final @NonNull InstantiationModel model) {
            this.instantiationModel = model;
            return this;
        }

        public @NonNull InternalDb singleton() {
            return instantiation(InstantiationModel.SINGLETON);
        }

        public @NonNull InternalDb perClass() {
            return instantiation(InstantiationModel.PER_CLASS);
        }

        public @NonNull InternalDb perTest() {
            return instantiation(InstantiationModel.PER_TEST);
        }

        @NonNull Set<@NonNull Class<?>> procedureClasses() {
            return Collections.unmodifiableSet(this.procedureClasses);
        }

        public @NonNull InternalDb procedureClasses(
                final @NonNull Class<?> procedureClass,
                final @NonNull Class<?> @NonNull ... otherProcedureClasses) {
            this.procedureClasses.add(procedureClass);
            Collections.addAll(this.procedureClasses, otherProcedureClasses);
            return this;
        }

        public @NonNull InternalDb procedureClasses(final @NonNull Collection<@NonNull Class<?>> classes) {
            this.procedureClasses.addAll(classes);
            return this;
        }

        @NonNull Set<@NonNull Class<?>> functionClasses() {
            return Collections.unmodifiableSet(this.functionClasses);
        }

        public @NonNull InternalDb functionClasses(
                final @NonNull Class<?> functionClass,
                final @NonNull Class<?> @NonNull ... otherProcedureClasses) {
            this.functionClasses.add(functionClass);
            Collections.addAll(this.functionClasses, otherProcedureClasses);
            return this;
        }

        public @NonNull InternalDb functionClasses(final @NonNull Collection<@NonNull Class<?>> classes) {
            this.functionClasses.addAll(classes);
            return this;
        }

        @NonNull Set<@NonNull Class<?>> aggregationFunctionClasses() {
            return Collections.unmodifiableSet(this.aggregationFunctionClasses);
        }

        public @NonNull InternalDb aggregationFunctionClasses(
                final @NonNull Class<?> aggregationFunctionClass,
                final @NonNull Class<?> @NonNull ... otherAggregationFunctionClasses) {
            this.aggregationFunctionClasses.add(aggregationFunctionClass);
            Collections.addAll(this.aggregationFunctionClasses, otherAggregationFunctionClasses);
            return this;
        }

        public @NonNull InternalDb aggregationFunctionClasses(final @NonNull Collection<@NonNull Class<?>> classes) {
            this.aggregationFunctionClasses.addAll(classes);
            return this;
        }

        @NonNull Neo4j buildNeo4J() {
            final var home = makeNeo4JHome();
            var builder = Neo4jBuilders.newInProcessBuilder(home)
                    .withDisabledServer() // we don't need the http server
                    .withConfig(GraphDatabaseSettings.forbid_shortestpath_common_nodes, false);

            for (final var procedureClass : procedureClasses()) {
                builder = builder.withProcedure(procedureClass);
            }
            for (final var functionClass : functionClasses()) {
                builder = builder.withFunction(functionClass);
            }
            for (final var aggregationFunctionClass : aggregationFunctionClasses()) {
                builder = builder.withAggregationFunction(aggregationFunctionClass);
            }

            return builder.build();
        }

        private Path makeNeo4JHome() {
            final Path neo4jHome;
            try {
                neo4jHome = Files.createTempDirectory("_neo4jdb_");
            } catch (final IOException e) {
                throw new IllegalStateException("unable to create neo4j home", e);
            }
            LoggerFactory.getLogger(InternalDb.class).debug("neo4j home dir='{}'", neo4jHome);
            return neo4jHome;
        }

        /**
         * Indicates which strategy should be used to instantiate a test db.
         */
        protected enum InstantiationModel {
            SINGLETON("One db for all tests."),
            PER_CLASS("One db per test class, the instance will be shared by all test methods in the class."),
            PER_TEST("One db per test method, the instance will not be shared by any test method.");

            /**
             * Describes the instantiation model.
             */
            final @NonNull Description description;

            /**
             * Creates the instantiation model describing its intention.
             *
             * @param description The text describing the intention of the {@link InstantiationModel}.
             */
            InstantiationModel(final @NonNull String description) {
                this.description = Description.of(description);
            }
        }
    }
}

final class DefaultNeo4jFixturesConfigurationSettings
        extends Neo4jFixturesSettings {
    @Override
    public void configure() {
        internal();
    }
}

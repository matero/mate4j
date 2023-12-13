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

import matero.fixtures.Neo4jFixturesSettings.ConnectionDetails;
import matero.fixtures.Neo4jFixturesSettings.ExternalDb;
import matero.fixtures.Neo4jFixturesSettings.InternalDb;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.util.NullnessUtil;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

abstract sealed class Neo4jDatabase
    implements AutoCloseable {
  private @Nullable Session currentSession;

  private Neo4jDatabase() {
    // nothing to do
  }

  abstract void testMethodCompleted();

  abstract void testClassCompleted();

  final @NonNull Session session() {
    if (this.currentSession == null) {
      this.currentSession = makeSession();
    }
    return this.currentSession;
  }

  private @NonNull Session makeSession() {
    final var session = openSession();
    return new SessionWrapper(session);
  }

  abstract @NonNull Session openSession();

  final void dropCurrentSession() {
    if (this.currentSession != null) {
      this.currentSession.close();
    }
  }

  static @NonNull Neo4jDatabase connect(final @NonNull ConnectionDetails connectionDetails) {
    if (connectionDetails instanceof ExternalDb db) {
      return Neo4jDatabase.External.with(db);
    }
    if (connectionDetails instanceof InternalDb db) {
      return Neo4jDatabase.Internal.with(db);
    }
    throw new IllegalArgumentException("unknown neo4j database connection details " + connectionDetails.getClass());
  }

  static final class External
      extends Neo4jDatabase {
    private final @NonNull Driver driver;

    External(final @NonNull Driver driver) {
      this.driver = driver;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
      return (this == o) || (o instanceof External other && this.driver.equals(other.driver));
    }

    @Override
    public int hashCode() {
      return this.driver.hashCode();
    }

    @Override
    void testMethodCompleted() {
      // nothing to do
    }

    @Override
    void testClassCompleted() {
      // nothing to do
    }

    @Override
    @NonNull Session openSession() {
      return this.driver.session();
    }

    @Override
    public void close() {
      this.driver.close();
    }

    static @NonNull External with(final @NonNull ExternalDb db) {
      return new External(GraphDatabase.driver(db.uri(), db.authToken()));
    }
  }

  static final class Internal
      extends Neo4jDatabase {
    private final @NonNull Supplier<Neo4j> neo4jSupplier;
    private final @NonNull Consumer<@NonNull Internal> onTestMethodCompleted;
    private final @NonNull Consumer<@NonNull Internal> onTestClassCompleted;
    private @Nullable Neo4j neo4j;
    private @Nullable Driver currentDriver;


    Internal(final @NonNull Neo4j neo4j) {
      this(() -> neo4j, Internal::nothing, Internal::nothing);
    }

    Internal(
        final @NonNull Supplier<Neo4j> neo4jSupplier,
        final @NonNull Consumer<@NonNull Internal> onTestMethodCompleted,
        final @NonNull Consumer<@NonNull Internal> onTestClassCompleted) {
      this.neo4jSupplier = neo4jSupplier;
      this.onTestMethodCompleted = onTestMethodCompleted;
      this.onTestClassCompleted = onTestClassCompleted;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
      return (this == o) || (o instanceof Internal other && Objects.equals(this.currentDriver,
          other.currentDriver));
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(this.currentDriver);
    }

    @Override
    @NonNull Session openSession() {
      if (this.neo4j == null) {
        this.neo4j = this.neo4jSupplier.get();
      }
      if (this.currentDriver == null) {
        this.currentDriver = makeDriver(this.neo4j);
      }
      return this.currentDriver.session();
    }

    private Driver makeDriver(final @NonNull Neo4j neo4j) {
      return GraphDatabase.driver(neo4j.boltURI(), AuthTokens.none());
    }

    @Override
    public void close() {
      if (this.currentDriver != null) {
        this.currentDriver.close();
      }
      if (this.neo4j != null) {
        this.neo4j.close();
      }
    }

    @Override
    void testMethodCompleted() {
      this.onTestMethodCompleted.accept(this);
    }

    @Override
    void testClassCompleted() {
      this.onTestClassCompleted.accept(this);
    }

    static @NonNull Internal with(final @NonNull InternalDb db) {
      return switch (db.instantiation()) {
        case SINGLETON -> new Internal(db.buildNeo4J());
        case PER_CLASS -> new Internal(db::buildNeo4J, Internal::nothing, Internal::doDropNeo4j);
        case PER_TEST -> new Internal(db::buildNeo4J, Internal::doDropNeo4j, Internal::nothing);
      };
    }

    private static void nothing(final @NonNull Internal db) {
      // nothing to do
    }

    private static void doDropNeo4j(final @NonNull Internal db) {
      db.dropNeo4j();
    }

    private void dropNeo4j() {
      if (this.neo4j != null) {
        dropCurrentDriver();
        NullnessUtil.castNonNull(this.neo4j).close();
        this.neo4j = null;
      }
    }

    private void dropCurrentDriver() {
      if (this.currentDriver != null) {
        dropCurrentSession();
        NullnessUtil.castNonNull(this.currentDriver).close();
        this.currentDriver = null;
      }
    }

  }

  final class SessionWrapper
      implements Session {
    private final @NonNull Session target;

    public SessionWrapper(final @NonNull Session session) {
      this.target = session;
    }

    @Override
    public Transaction beginTransaction() {
      return this.target.beginTransaction();
    }

    @Override
    public Transaction beginTransaction(final @NonNull TransactionConfig config) {
      return this.target.beginTransaction(config);
    }

    @Override
    @Deprecated
    public <T> T readTransaction(final @NonNull TransactionWork<T> work) {
      return this.target.readTransaction(work);
    }

    @Override
    public <T> T executeRead(final @NonNull TransactionCallback<T> callback) {
      return this.target.executeRead(callback);
    }

    @Override
    @Deprecated
    public <T> T readTransaction(
        final @NonNull TransactionWork<T> work,
        final @NonNull TransactionConfig config) {
      return this.target.readTransaction(work, config);
    }

    @Override
    public <T> T executeRead(
        final @NonNull TransactionCallback<T> callback,
        final @NonNull TransactionConfig config) {
      return this.target.executeRead(callback, config);
    }

    @Override
    @Deprecated
    public <T> T writeTransaction(final @NonNull TransactionWork<T> work) {
      return this.target.writeTransaction(work);
    }

    @Override
    public <T> T executeWrite(final @NonNull TransactionCallback<T> callback) {
      return this.target.executeWrite(callback);
    }

    @Override
    public void executeWriteWithoutResult(final @NonNull Consumer<TransactionContext> contextConsumer) {
      this.target.executeWriteWithoutResult(contextConsumer);
    }

    @Override
    @Deprecated
    public <T> T writeTransaction(
        final @NonNull TransactionWork<T> work,
        final @NonNull TransactionConfig config) {
      return this.target.writeTransaction(work, config);
    }

    @Override
    public <T> T executeWrite(
        final @NonNull TransactionCallback<T> callback,
        final @NonNull TransactionConfig config) {
      return this.target.executeWrite(callback, config);
    }

    @Override
    public void executeWriteWithoutResult(
        final @NonNull Consumer<TransactionContext> contextConsumer,
        final @NonNull TransactionConfig config) {
      this.target.executeWriteWithoutResult(contextConsumer, config);
    }

    @Override
    public Result run(
        final @NonNull String query,
        final @NonNull TransactionConfig config) {
      return this.target.run(query, config);
    }

    @Override
    public Result run(
        final @NonNull String query,
        final @NonNull Map<String, Object> parameters,
        final @NonNull TransactionConfig config) {
      return this.target.run(query, parameters, config);
    }

    @Override
    public Result run(
        final @NonNull Query query,
        final @NonNull TransactionConfig config) {
      return this.target.run(query, config);
    }

    @Override
    @Deprecated
    public Bookmark lastBookmark() {
      return this.target.lastBookmark();
    }

    @Override
    public Set<Bookmark> lastBookmarks() {
      return this.target.lastBookmarks();
    }

    @Override
    public void close() {
      this.target.close();
      Neo4jDatabase.this.currentSession = null;
    }

    @Override
    public boolean isOpen() {
      return this.target.isOpen();
    }

    @Override
    public Result run(
        final @NonNull String query,
        final @NonNull Value parameters) {
      return this.target.run(query, parameters);
    }

    @Override
    public Result run(
        final @NonNull String query,
        final @NonNull Map<String, Object> parameters) {
      return this.target.run(query, parameters);
    }

    @Override
    public Result run(
        final @NonNull String query,
        final @NonNull Record parameters) {
      return this.target.run(query, parameters);
    }

    @Override
    public Result run(final @NonNull String query) {
      return this.target.run(query);
    }

    @Override
    public Result run(final @NonNull Query query) {
      return this.target.run(query);
    }
  }
}

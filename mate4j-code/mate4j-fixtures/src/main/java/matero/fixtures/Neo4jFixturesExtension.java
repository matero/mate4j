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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.neo4j.driver.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Neo4jFixturesExtension
    implements InvocationInterceptor,
    BeforeAllCallback,
    ExtensionContext.Store.CloseableResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jFixturesExtension.class);
  private static final AtomicBoolean SHOULD_PREPARE = new AtomicBoolean(true);
  private @Nullable Neo4jFixturesSettings settings;
  private @Nullable Neo4jDatabase neo4jdb;

  final @NonNull Neo4jFixturesSettings settings() {
    if (this.settings == null) {
      throw new IllegalStateException("neo4j settings not configured... WEIRD!");
    }
    return this.settings;
  }

  final @NonNull Neo4jDatabase neo4jdb() {
    if (this.neo4jdb == null) {
      throw new IllegalStateException("neo4j database not configured... WEIRD");
    }
    return this.neo4jdb;
  }

  @Override
  public void beforeAll(final @NonNull ExtensionContext extensionContext) {
    if (shouldPrepare()) {
      LOGGER.info("beforeAll on {} required the startup of neo4j container", extensionContext.getDisplayName());

      registerExtensionForShutdownAt(extensionContext);
      prepareExtension(extensionContext.getRequiredTestClass());
      settings().configure();
      this.neo4jdb = Neo4jDatabase.connect(settings().connectionDetails());
    }
  }

  private boolean shouldPrepare() {
    return SHOULD_PREPARE.compareAndSet(true, false);
  }

  private void registerExtensionForShutdownAt(final ExtensionContext extensionContext) {
    LOGGER.debug("registering extension for callback hook for when the root test context is shut down");
    extensionContext
        .getRoot()
        .getStore(ExtensionContext.Namespace.GLOBAL)
        .put("Neo4JFixtures", this);
  }

  void prepareExtension(final Class<?> testClass) {
    LOGGER.debug("configuring fixtures settings");

    final var neo4JFixtures = getNeo4jFixturesFrom(testClass);
    this.settings = loadSettings(testClass, neo4JFixtures.settings());

    LOGGER.debug("fixtures settings configured");
  }

  Neo4jFixtures getNeo4jFixturesFrom(final @NonNull Class<?> testClass) {
    final var neo4JFixtures = testClass.getAnnotation(Neo4jFixtures.class);
    if (neo4JFixtures == null) {
      throw new IllegalStateException("no @Neo4jFixtures found in test " + testClass + " hierarchy!");
    }
    return neo4JFixtures;
  }

  Neo4jFixturesSettings loadSettings(
      final @NonNull Class<?> testClass,
      final @Nullable Class<? extends Neo4jFixturesSettings> settings) {
    if (settings == null) {
      //FIXME: report this problem with an annotation processor, not at runtime
      throw new NullPointerException("@Neo4jFixtures.settings() @ " + testClass);
    }
    final var defaultConstructor = getDefaultConstructorOf(settings);

    try {
      return defaultConstructor.newInstance();
    } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException("could not create a settings object for configured class " + settings, e);
    }
  }

  Constructor<? extends Neo4jFixturesSettings> getDefaultConstructorOf(Class<? extends Neo4jFixturesSettings> settings) {
    final Constructor<? extends Neo4jFixturesSettings> defaultConstructor;
    try {
      defaultConstructor = settings.getDeclaredConstructor();
    } catch (final NoSuchMethodException e) {
      //FIXME: report this problem with an annotation processor, not at runtime
      throw new IllegalStateException("configured settings class " + settings + " does not have a default constructor");
    }
    return defaultConstructor;
  }

  @Override
  public void interceptTestMethod(
      final @NonNull Invocation<Void> invocation,
      final @NonNull ReflectiveInvocationContext<Method> invocationCtx,
      final @NonNull ExtensionContext extensionContext)
      throws Throwable {

    try (final var session = neo4jdb().openSession();
         final var tx = session.beginTransaction()) {

      Throwable invocationFailure = null;
      try {
        applyTestFixtures(tx, invocationCtx.getExecutable());
        ScopedValue.where(Neo4j.SESSION, session)
            .where(Neo4j.TX, tx)
            .run(() -> {
              try {
                invocation.proceed();
              } catch (final Throwable e) {
                throw new InvocationFailed(e);
              }
            });
      } catch (final InvocationFailed failure) {
        invocationFailure = failure.getCause();
      }

      if (settings().rollback() == Neo4jFixturesSettings.Rollback.AFTER_TEST) {
        LOGGER.trace("rolling back changes from db!");
        tx.rollback();
      } else {
        LOGGER.trace("committing changes to db!");
        tx.commit();
      }
      neo4jdb().testMethodCompleted();

      if (invocationFailure != null) {
        throw invocationFailure;
      }
    }
  }

  private void applyTestFixtures(
      final @NonNull Transaction tx,
      final @NonNull Method method) {
    final var fixtureContents = getFixtureContentsFor(method);
    for (final var content : fixtureContents) {
      applyFixture(tx, method, content);
    }
  }

  private void applyFixture(
      final @NonNull Transaction tx,
      final @NonNull Method method,
      final @NonNull String content) {
    if (shouldShowFixtureOf(method)) {
      getLogger(method).info("Applying fixture:\n{}", content);
    }
  }

  private static @NonNull Logger getLogger(final @NonNull Method method) {
    return LoggerFactory.getLogger(method.getDeclaringClass());
  }

  private boolean shouldShowFixtureOf(final @NonNull Method method) {
    final var showFixture = ShowFixtureContents.of(method);
    if (showFixture == null) {
      return settings().shouldShowFixture();
    } else {
      return showFixture;
    }
  }

  private @NonNull Iterable<@NonNull String> getFixtureContentsFor(final @NonNull Method method) {
    final var fixtures = GetFixtures.at(method);
    return fixtures.stream()
        .map(fixture -> {
          try (final var lines = Files.lines(fixture)) {
            return lines.collect(Collectors.joining("\n"));
          } catch (final IOException e) {
            throw new IllegalStateException("could not read lines of " + fixture, e);
          }
        })
        .collect(Collectors.toList());
  }

  @Override
  public void close() throws Throwable {
    LOGGER.info("closing");
    if (this.neo4jdb != null) {
      this.neo4jdb.close();
    }
  }

  private static final class InvocationFailed extends RuntimeException {
    InvocationFailed(final Throwable cause) {
      super(cause);
    }
  }
}

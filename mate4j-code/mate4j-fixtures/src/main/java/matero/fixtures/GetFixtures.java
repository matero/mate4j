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

import matero.fixtures.Neo4j.Fixture;
import matero.support.ClassNotInstantiable;
import matero.support.ClasspathResource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

final class GetFixtures {
  private GetFixtures() {
    throw new ClassNotInstantiable(GetFixtures.class);
  }

  static @NonNull Collection<@NonNull Path> at(final @NonNull Method method) {
    var loadPathname = getFilenamesToLoadFrom(method.getAnnotation(Fixture.class));

    if (isNotConfigured(loadPathname)) {
      final var testFixtures = getDefaultMethodFixtures(method);
      if (testFixtures.isEmpty()) {
        loadPathname = getFilenamesToLoadFrom(method.getDeclaringClass().getAnnotation(Fixture.class));
        if (isNotConfigured(loadPathname)) {
          return getDefaultClassFixtures(method);
        }
      } else {
        return testFixtures;
      }
    }

    return asPaths(method.getDeclaringClass(), loadPathname);
  }

  private static @NonNull String @NonNull [] getFilenamesToLoadFrom(final @Nullable Fixture fixture) {
    if (fixture == null) {
      return Fixture.UNDEFINED;
    }
    if (fixture.value().length > 0 && fixture.load().length > 0) {
      throw new IllegalStateException("you can define 1 of `Neo4j.fixture.value()` or `Neo4j.Fixture.load()`, but not both of them.");
    }
    if (fixture.value().length > 0) {
      return fixture.value();
    } else {
      return fixture.load();
    }
  }

  private static boolean isNotConfigured(final @NonNull String @NonNull [] filenames) {
    return filenames.length == 0;
  }

  private static SortedSet<Path> getDefaultMethodFixtures(final @NonNull Method method) {
    return getTestFixtures(method.getDeclaringClass(), method.getName());
  }

  private static SortedSet<Path> getDefaultClassFixtures(final @NonNull Method ctx) {
    return getTestFixtures(ctx.getDeclaringClass(), "fixture");
  }

  private static SortedSet<Path> getTestFixtures(
      final @NonNull Class<?> aClass,
      final @NonNull String fixture) {
    final var dir = getFixtureDirFor(aClass);

    if (dir == null) {
      return Collections.emptySortedSet();
    }
    final var possibleFixtures = dir.listFiles();
    if (possibleFixtures == null || possibleFixtures.length == 0) {
      return Collections.emptySortedSet();
    }

    final var fixturePattern = Pattern.compile(fixture + "(#\\d+)?\\.cypher");
    boolean generalFixtureFound = false;
    final var fixtures = new java.util.TreeSet<Path>();
    for (final File f : possibleFixtures) {
      final var fixtureMatcher = fixturePattern.matcher(f.getName());
      if (fixtureMatcher.matches()) {
        if (fixtureMatcher.group(1) == null) {
          if (!fixtures.isEmpty()) {
            throw new IllegalStateException(
                "Define ONE general fixture, OR MANY sorted fixtures. But not both cases together.");
          }
          generalFixtureFound = true;
          fixtures.add(f.toPath());
        } else {
          if (generalFixtureFound) {
            throw new IllegalStateException(
                "Define ONE general fixture, OR MANY sorted fixtures. But not both cases together.");
          }
          fixtures.add(f.toPath());
        }
      }
    }
    return fixtures;
  }


  private static @Nullable File getFixtureDirFor(final @NonNull Class<?> aClass) {
    final var classDirs = getClassDirs(aClass);
    final var pkgDirs = getPackageDirs(aClass);
    final var path = new File(pkgDirs, classDirs).getPath();

    return ClasspathResource.file(path);
  }

  private static @NonNull String getPackageDirs(final @NonNull Class<?> aClass) {
    return aClass.getPackageName().replaceAll("\\.", "/");
  }

  private static @NonNull String getClassDirs(final @NonNull Class<?> aClass) {
    if (aClass.getEnclosingClass() == null) {
      return aClass.getSimpleName();
    } else {
      final var elements = new ArrayList<String>(4);
      var enclosingClass = aClass.getEnclosingClass();
      do {
        elements.addFirst(enclosingClass.getSimpleName());
        enclosingClass = enclosingClass.getEnclosingClass();
      } while (enclosingClass != null);

      final var classnameDirs = new StringBuilder();
      for (final var dir : elements) {
        classnameDirs.append(dir).append('/');
      }
      classnameDirs.append(aClass.getSimpleName());
      return classnameDirs.toString();
    }
  }

  private static @NonNull Collection<@NonNull Path> asPaths(
      final @NonNull Class<?> declaringClass,
      final @NonNull String @NonNull [] filenames) {
    if (filenames.length == 0) {
      return List.of();
    }
    final var fixturePaths = new ArrayList<@NonNull Path>(filenames.length);
    for (final var f : filenames) {
      fixturePaths.add(asPath(declaringClass, f));
    }
    return fixturePaths;
  }

  private static @NonNull Path asPath(
      final @NonNull Class<?> declaringClass,
      final @NonNull String f) {
    var fixtureFile = ClasspathResource.file(f);
    if (fixtureFile == null) {
      if (f.contains(".")) { // in this case it has an extension
        final var fixtureDir = getFixtureDirFor(declaringClass);
        if (fixtureDir != null) {
          fixtureFile = new File(fixtureDir, f);
        }
      } else {
        fixtureFile = ClasspathResource.file(f + ".cypher");
        if (fixtureFile == null) {
          final var fixtureDir = getFixtureDirFor(declaringClass);
          if (fixtureDir != null) {
            fixtureFile = new File(fixtureDir, f + ".cypher");
          }
        }
      }
    }
    if (fixtureFile == null) {
      throw new IllegalStateException("fixture " + f + " not found!");
    }
    if (!fixtureFile.exists()) {
      throw new IllegalStateException("fixture " + f + " not found!");
    }
    if (fixtureFile.isDirectory()) {
      throw new IllegalStateException("fixture " + f + " is a directory!");
    }
    if (!fixtureFile.canRead()) {
      throw new IllegalStateException("fixture " + f + " can not be read!");
    }
    return fixtureFile.toPath();
  }
}


package matero.queries.processor;

/*-
 * #%L
 * mate4j-queries-processor
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

final class ReturnType {
  static sealed abstract class Mapper permits StaticMethod, InstanceMethod {
    final @NonNull String target;
    final @NonNull String method;

    @NonNull String argument;

    private Mapper(
        final @NonNull String target,
        final @NonNull String method,
        final @NonNull String argument) {
      this.target = target;
      this.method = method;
      this.argument = argument;
    }

    @NonNull Mapper withArgument(final @NonNull String a) {
      if (!a.isEmpty()) {
        if (this.argument.isEmpty()) {
          this.argument = a;
        } else {
          this.argument = this.argument + ", " + a;
        }
      }
      return this;
    }

    abstract @NonNull String str(@NonNull String target);

    @Override
    public final @NonNull String toString() {
      return str(this.target);
    }

    static @NonNull Mapper staticMethod(
        final @NonNull String target,
        final @NonNull String method) {
      return staticMethod(target, method, "");
    }

    static @NonNull Mapper staticMethod(
        final @NonNull String target,
        final @NonNull String method,
        final @NonNull String argument) {
      return new StaticMethod(target, method, argument);
    }

    static @NonNull Mapper instanceMethod(
        final @NonNull String target,
        final @NonNull String method) {
      return instanceMethod(target, method, "");
    }

    static @NonNull Mapper instanceMethod(
        final @NonNull String target,
        final @NonNull String method,
        final @NonNull String argument) {
      return new InstanceMethod(target, method, argument);
    }
  }

  private static final class StaticMethod extends Mapper {
    private StaticMethod(
        final @NonNull String target,
        final @NonNull String method,
        final @NonNull String argument) {
      super(target, method, argument);
    }

    @Override
    @NonNull String str(final @NonNull String target) {
      if (this.argument.isEmpty()) {
        return this.method + '(' + target + ')';
      } else {
        return this.method + '(' + target + ", " + this.argument + ')';
      }
    }
  }

  private static final class InstanceMethod extends Mapper {

    public InstanceMethod(
        final @NonNull String target,
        final @NonNull String method,
        final @NonNull String arguments) {
      super(target, method, arguments);
    }


    @Override
    @NonNull String str(final @NonNull String target) {
      if (this.argument.isEmpty()) {
        return target + '.' + this.method + "()";
      } else {
        return target + '.' + this.method + '(' + this.argument + ')';
      }
    }
  }

  public final @NonNull String annotations;
  public final @NonNull String javaSpec;
  public final @NonNull String executionTemplate;
  public final @Nullable Mapper mapper;

  ReturnType(
      final @NonNull String annotations,
      final @NonNull String javaSpec,
      final @NonNull String executionTemplate,
      final @Nullable Mapper mapper) {
    this.annotations = annotations;
    this.javaSpec = javaSpec;
    this.executionTemplate = executionTemplate;
    this.mapper = mapper;
  }

  @Override
  public String toString() {
    if (this.annotations.isEmpty()) {
      return this.javaSpec;
    } else {
      return this.annotations + ' ' + this.javaSpec;
    }
  }

  static @NonNull ReturnTypeBuilder builder() {
    return new DefaultReturnTypeBuilder();
  }
}

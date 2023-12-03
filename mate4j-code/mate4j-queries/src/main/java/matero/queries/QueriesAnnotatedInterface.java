package matero.queries;

/*-
 * #%L
 * Mate4j/Code/Queries
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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedList;
import java.util.List;

class QueriesAnnotatedInterface {
  private final @NonNull TypeElement target;
  private final LinkedList<MatchMethod> matchMethods = new LinkedList<>();

  QueriesAnnotatedInterface(final TypeElement target) {
    this.target = target;
  }

  @Override
  public boolean equals(final @Nullable Object o) {
    return (this == o) || (o instanceof QueriesAnnotatedInterface other && this.target.equals(other.target));
  }

  @Override
  public int hashCode() {
    return this.target.hashCode();
  }

  @Override
  public String toString() {
    return "QueriesAnnotatedInterface(target=" + this.target + ", MATCH=" + this.matchMethods + ')';
  }


  void registerMatchMethod(final @NonNull ExecutableElement method) {
    if (method.isDefault()) {
      throw new IllegalQueriesDefinition(method, "methods with default implementation are not allowed");
    }

    final var match = method.getAnnotation(MATCH.class);
    if (match == null) {
      throw new IllegalQueriesDefinition(method, "only cypher query methods can be defined as instance methods");
    }

    if (!method.getTypeParameters().isEmpty()) {
      throw new IllegalQueriesDefinition(method, "generic methods are not allowed");
    }

    this.matchMethods.add(new MatchMethod(
        method.getSimpleName(),
        method.getReturnType(),
        method.getParameters(),
        method.getThrownTypes(),
        match.value())
    );
  }
}

record MatchMethod(
    @NonNull Name name,
    @NonNull TypeMirror returnType,
    @NonNull List<@NonNull ? extends VariableElement> parameters,
    @NonNull List<@NonNull ? extends TypeMirror> thrownTypes,
    @NonNull String cypher) {

  String code() {
    return returnType.toString() + ' ' + name + '(';
  }
}
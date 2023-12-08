package matero.queries.processor;

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

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class QueriesAnnotatedInterface {
  private final @NonNull TypeElement target;
  private final @NonNull List<QueryMethod> methods;

  private final @NonNull List<@NonNull String> imports;

  QueriesAnnotatedInterface(
      final @NonNull TypeElement target,
      final @NonNull Collection<QueryMethod> methods,
      final @NonNull Collection<@NonNull String> imports) {
    this.target = target;
    this.methods = List.copyOf(methods);
    this.imports = List.copyOf(imports);
  }

  @NonNull
  String packageName() {
    return getPackage().getQualifiedName().toString();
  }

  private PackageElement getPackage() {
    return (PackageElement) this.target.getEnclosingElement();
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
    return "QueriesAnnotatedInterface(\n  target=" + this.target + ",\n  imports=" + this.imports.stream().collect(Collectors.joining("\n", "import ", ";")) + ",\n  queries=" + this.methods + ')';
  }

  public @NonNull List<@NonNull String> imports() {
    return this.imports;
  }

  String interfaceName() {
    return this.target.getSimpleName().toString();
  }

  final @NonNull List<QueryMethod> methods() {
    return this.methods;
  }
}

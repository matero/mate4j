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

import matero.queries.neo4j.CurrentSession;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.util.List;
import java.util.Set;

final class ImportsParser implements TypeVisitor<@Nullable Void, @Nullable Void>, AnnotationValueVisitor<@Nullable Void, @Nullable Void> {

  private final @NonNull Set<@NonNull String> knownImports;

  ImportsParser() {
    this(new java.util.TreeSet<>());
  }

  ImportsParser(final @NonNull Set<@NonNull String> knownImports) {
    this.knownImports = knownImports;
  }

  void prepare() {
    this.knownImports.clear();
    this.knownImports.add(CurrentSession.class.getCanonicalName());
    this.knownImports.add(Nullable.class.getCanonicalName());
    this.knownImports.add(NonNull.class.getCanonicalName());
  }

  @NonNull Set<@NonNull String> getImports() {
    return java.util.Collections.unmodifiableSet(this.knownImports);
  }

  @Override
  public @Nullable Void visit(final @NonNull TypeMirror t) {
    return visit(t, null);
  }


  @Override
  public @Nullable Void visit(
      final @NonNull TypeMirror t,
      final @Nullable Void unused) {
    return t.accept(this, null);
  }

  @Override
  public @Nullable Void visitIntersection(
      final @NonNull IntersectionType t,
      final @Nullable Void unused) {
    for (final var bound : t.getBounds()) {
      bound.accept(this, null);
    }
    return null;
  }

  @Override
  public @Nullable Void visitPrimitive(
      final @NonNull PrimitiveType t,
      final @Nullable Void unused) {
    visitAnnotations(t.getAnnotationMirrors(), null);
    return null; // nothing to do
  }

  @Override
  public @Nullable Void visitNull(
      final @NonNull NullType t,
      final @Nullable Void unused) {
    return null; // nothing to do
  }

  @Override
  public @Nullable Void visitArray(
      final @NonNull ArrayType t,
      final @Nullable Void unused) {
    visitAnnotations(t.getAnnotationMirrors(), null);
    t.getComponentType().accept(this, null);
    return null;
  }

  @Override
  public @Nullable Void visitDeclared(
      final @NonNull DeclaredType t,
      final @Nullable Void unused) {
    final var type = t.asElement();

    if (type.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
      throw new IllegalQueriesDefinition(type, "enclosed types not supported");
    }
    visitAnnotations(t.getAnnotationMirrors(), null);
    final var pkg = (PackageElement) type.getEnclosingElement();
    final var packageName = pkg.getQualifiedName().toString();
    if (!"java.lang".equals(packageName)) { // only non-lang abstractions are imported
      this.knownImports.add(packageName + '.' + t.asElement().getSimpleName().toString());
      for (final var typeArgument : t.getTypeArguments()) {
        typeArgument.accept(this, null);
      }
    }
    return null;
  }

  @Override
  public @Nullable Void visitError(
      final @NonNull ErrorType t,
      final @Nullable Void unused) {
    return null; // nothing to do
  }

  @Override
  public @Nullable Void visitTypeVariable(
      final @NonNull TypeVariable t,
      final @Nullable Void unused) {
    t.getLowerBound().accept(this, null);
    t.getUpperBound().accept(this, null);
    return null;
  }

  @Override
  public @Nullable Void visitWildcard(
      final @NonNull WildcardType t,
      final @Nullable Void unused) {
    visitAnnotations(t.getAnnotationMirrors(), null);
    return null; // nothing to do
  }

  @Override
  public @Nullable Void visitExecutable(
      final @NonNull ExecutableType t,
      final @Nullable Void unused) {
    visitAnnotations(t.getAnnotationMirrors(), null);
    return null; // nothing to do
  }

  @Override
  public @Nullable Void visitNoType(
      final @NonNull NoType t,
      final @Nullable Void unused) {
    return null; // nothing to do
  }

  @Override
  public @Nullable Void visitUnknown(
      final @NonNull TypeMirror t,
      final @Nullable Void unused) {
    throw new IllegalArgumentException("unknown type: " + t);
  }

  @Override
  public @Nullable Void visitUnion(
      final @NonNull UnionType t,
      final @Nullable Void unused) {
    for (final var alternative : t.getAlternatives()) {
      alternative.accept(this, null);
    }
    return null;
  }

  void parse(final @NonNull ExecutableElement method) {
    method.getReturnType().accept(this, null);
    for (final var parameter : method.getParameters()) {
      parameter.asType().accept(this, null);
    }
    for (final var thrownType : method.getThrownTypes()) {
      thrownType.accept(this, null);
    }
  }

  @Override
  public @Nullable Void visit(
      final @NonNull AnnotationValue av,
      final @Nullable Void unused) {
    return av.accept(this, null);
  }

  @Override
  public @Nullable Void visitBoolean(
      final boolean b,
      final @Nullable Void unused) {
    return null;
  }

  @Override
  public @Nullable Void visitByte(
      final byte b,
      final @Nullable Void unused) {
    return null;
  }

  @Override
  public @Nullable Void visitChar(
      final char c,
      final @Nullable Void unused) {
    return null;
  }

  @Override
  public @Nullable Void visitDouble(
      final double d,
      final @Nullable Void unused) {
    return null;
  }

  @Override
  public @Nullable Void visitFloat(
      final float f,
      final @Nullable Void unused) {
    return null;
  }

  @Override
  public @Nullable Void visitInt(
      final int i,
      final @Nullable Void unused) {
    return null;
  }

  @Override
  public @Nullable Void visitLong(
      final long i,
      final @Nullable Void unused) {
    return null;
  }

  @Override
  public @Nullable Void visitShort(
      final short s,
      final @Nullable Void unused) {
    return null;
  }

  @Override
  public @Nullable Void visitString(
      final String s,
      final @Nullable Void unused) {
    return null;
  }

  @Override
  public @Nullable Void visitType(
      final @NonNull TypeMirror t,
      final @Nullable Void unused) {
    return t.accept(this, null);
  }

  @Override
  public @Nullable Void visitEnumConstant(
      final @NonNull VariableElement c,
      final @Nullable Void unused) {
    return c.asType().accept(this, null);
  }

  private void visitAnnotations(
      final @NonNull List<@NonNull ? extends AnnotationMirror> annotationMirrors,
      final @Nullable Void unused) {
    for (final var a : annotationMirrors) {
      visitAnnotation(a, null);
    }
  }

  @Override
  public @Nullable Void visitAnnotation(
      final @NonNull AnnotationMirror a,
      final @Nullable Void unused) {
    a.getAnnotationType().accept(this, null);
    for (final var av : a.getElementValues().values()) {
      av.accept(this, null);
    }
    return null;
  }

  @Override
  public @Nullable Void visitArray(
      final @NonNull List<? extends AnnotationValue> vals,
      final @Nullable Void unused) {
    for (final var av : vals) {
      av.accept(this, null);
    }
    return null;
  }

  @Override
  public @Nullable Void visitUnknown(
      final @NonNull AnnotationValue av,
      final @Nullable Void unused) {
    return null;
  }
}

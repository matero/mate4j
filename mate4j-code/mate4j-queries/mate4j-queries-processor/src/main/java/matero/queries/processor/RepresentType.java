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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.*;
import javax.lang.model.util.Types;
import java.util.List;

final class RepresentType implements TypeVisitor<@NonNull StringBuilder, @NonNull StringBuilder> {
  private final @NonNull Types types;

  RepresentType(final @NonNull Types types) {
    this.types = types;
  }

  @Override
  public @NonNull StringBuilder visit(final @NonNull TypeMirror t) {
    return visit(t, new StringBuilder());
  }

  @Override
  public @NonNull StringBuilder visit(
      final @NonNull TypeMirror t,
      final @NonNull StringBuilder rep) {
    return t.accept(this, rep);
  }

  @Override
  public @NonNull StringBuilder visitPrimitive(final @NonNull PrimitiveType t, final @NonNull StringBuilder rep) {
    visitAnnotations(t.getAnnotationMirrors(), rep);
    return rep.append(t.getKind().name().toLowerCase());
  }

  @Override
  public @NonNull StringBuilder visitNull(final @NonNull NullType t, final @NonNull StringBuilder rep) {
    return rep;
  }

  @Override
  public @NonNull StringBuilder visitArray(final @NonNull ArrayType t, final @NonNull StringBuilder rep) {
    visitAnnotations(t.getAnnotationMirrors(), rep);
    t.getComponentType().accept(this, rep);
    return rep.append("[]");
  }

  @Override
  public @NonNull StringBuilder visitDeclared(final @NonNull DeclaredType t, final @NonNull StringBuilder rep) {
    visitAnnotations(t.getAnnotationMirrors(), rep);
    rep.append(t.asElement().getSimpleName());

    final var typeArguments = t.getTypeArguments();
    if (!typeArguments.isEmpty()) {
      rep.append('<');
      final var args = typeArguments.iterator();
      args.next().accept(this, rep);
      while (args.hasNext()) {
        args.next().accept(this, rep.append(", "));
      }
      rep.append('>');
    }
    return rep;
  }

  @Override
  public @NonNull StringBuilder visitError(final @NonNull ErrorType t, final @NonNull StringBuilder rep) {
    return rep;
  }

  @Override
  public @NonNull StringBuilder visitTypeVariable(final @NonNull TypeVariable t, final @NonNull StringBuilder rep) {
    visitAnnotations(t.getAnnotationMirrors(), rep);
    rep.append(t.asElement().getSimpleName());
    return rep;
  }

  @Override
  public @NonNull StringBuilder visitWildcard(final @NonNull WildcardType t, final @NonNull StringBuilder rep) {
    return rep.append(t);
  }

  @Override
  public @NonNull StringBuilder visitExecutable(final @NonNull ExecutableType t, final @NonNull StringBuilder rep) {
    return rep;
  }

  @Override
  public @NonNull StringBuilder visitNoType(final @NonNull NoType t, final @NonNull StringBuilder rep) {
    return rep;
  }

  @Override
  public @NonNull StringBuilder visitUnknown(final @NonNull TypeMirror t, final @NonNull StringBuilder rep) {
    return rep;
  }

  @Override
  public @NonNull StringBuilder visitUnion(final @NonNull UnionType t, final @NonNull StringBuilder rep) {
    final var types = t.getAlternatives().iterator();
    types.next().accept(this, rep);
    while (types.hasNext()) {
      types.next().accept(this, rep.append(" | "));
    }
    return rep;
  }

  @Override
  public @NonNull StringBuilder visitIntersection(final @NonNull IntersectionType t, final @NonNull StringBuilder rep) {
    final var types = t.getBounds().iterator();
    types.next().accept(this, rep);
    while (types.hasNext()) {
      types.next().accept(this, rep.append(" & "));
    }
    return rep;
  }

  void visitAnnotations(
      final @NonNull List<? extends AnnotationMirror> annotations,
      final @NonNull StringBuilder rep) {
    for (final var annotation : annotations) {
      visitAnnotation(annotation, rep);
      rep.append(' ');
    }
  }

  void visitAnnotation(final @NonNull AnnotationMirror annotation, final @NonNull StringBuilder rep) {
    annotation.getAnnotationType().accept(this, rep.append('@'));
  }
}

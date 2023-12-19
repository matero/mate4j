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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.*;
import java.util.List;

enum RepresentParameter implements TypeVisitor<@Nullable Void, @NonNull StringBuilder> {
  VISITOR;

  @Override
  public @Nullable Void visit(final @NonNull TypeMirror t) {
    return visit(t, new StringBuilder());
  }

  @Override
  public @Nullable Void visit(
      final @NonNull TypeMirror t,
      final @NonNull StringBuilder rep) {
    return t.accept(this, rep);
  }

  @Override
  public @Nullable Void visitPrimitive(final @NonNull PrimitiveType t, final @NonNull StringBuilder rep) {
    visitAnnotations(t.getAnnotationMirrors(), rep);
    rep.append(t.getKind().name().toLowerCase());
    return null;
  }

  @Override
  public @Nullable Void visitNull(final @NonNull NullType t, final @NonNull StringBuilder rep) {
    return null;
  }

  @Override
  public @Nullable Void visitArray(final @NonNull ArrayType t, final @NonNull StringBuilder rep) {
    visitAnnotations(t.getAnnotationMirrors(), rep);
    t.getComponentType().accept(this, rep);
    rep.append("[]");
    return null;
  }

  @Override
  public @Nullable Void visitDeclared(final @NonNull DeclaredType t, final @NonNull StringBuilder rep) {
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
    return null;
  }

  @Override
  public @Nullable Void visitError(final @NonNull ErrorType t, final @NonNull StringBuilder rep) {
    return null;
  }

  @Override
  public @Nullable Void visitTypeVariable(final @NonNull TypeVariable t, final @NonNull StringBuilder rep) {
    visitAnnotations(t.getAnnotationMirrors(), rep);
    rep.append(t.asElement().getSimpleName());
    return null;
  }

  @Override
  public @Nullable Void visitWildcard(final @NonNull WildcardType t, final @NonNull StringBuilder rep) {
    rep.append(t);
    return null;
  }

  @Override
  public @Nullable Void visitExecutable(final @NonNull ExecutableType t, final @NonNull StringBuilder rep) {
    return null;
  }

  @Override
  public @Nullable Void visitNoType(final @NonNull NoType t, final @NonNull StringBuilder rep) {
    return null;
  }

  @Override
  public @Nullable Void visitUnknown(final @NonNull TypeMirror t, final @NonNull StringBuilder rep) {
    return null;
  }

  @Override
  public @Nullable Void visitUnion(final @NonNull UnionType t, final @NonNull StringBuilder rep) {
    final var types = t.getAlternatives().iterator();
    types.next().accept(this, rep);
    while (types.hasNext()) {
      types.next().accept(this, rep.append(" | "));
    }
    return null;
  }

  @Override
  public @Nullable Void visitIntersection(final @NonNull IntersectionType t, final @NonNull StringBuilder rep) {
    final var types = t.getBounds().iterator();
    types.next().accept(this, rep);
    while (types.hasNext()) {
      types.next().accept(this, rep.append(" & "));
    }
    return null;
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

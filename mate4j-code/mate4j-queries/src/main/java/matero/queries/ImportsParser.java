package matero.queries;

import org.checkerframework.checker.nullness.qual.NonNull;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.util.List;
import java.util.Set;

enum ImportsParser implements
    TypeVisitor<Void, @NonNull Set<@NonNull String>>,
    AnnotationValueVisitor<Void, @NonNull Set<@NonNull String>> {
  INSTANCE;

  @Override
  public Void visit(final @NonNull TypeMirror t) {
    return visit(t, new java.util.TreeSet<>());
  }


  @Override
  public Void visit(
      final @NonNull TypeMirror t,
      final @NonNull Set<@NonNull String> knownImports) {
    t.accept(this, knownImports);
    return null;
  }

  @Override
  public Void visitIntersection(
      final @NonNull IntersectionType t,
      final @NonNull Set<@NonNull String> knownImports) {
    for (final var bound : t.getBounds()) {
      bound.accept(this, knownImports);
    }
    return null;
  }

  @Override
  public Void visitPrimitive(
      final @NonNull PrimitiveType t,
      final @NonNull Set<@NonNull String> knownImports) {
    visitAnnotations(t.getAnnotationMirrors(), knownImports);
    return null; // nothing to do
  }

  @Override
  public Void visitNull(
      final @NonNull NullType t,
      final @NonNull Set<@NonNull String> knownImports) {
    return null; // nothing to do
  }

  @Override
  public Void visitArray(
      final @NonNull ArrayType t,
      final @NonNull Set<@NonNull String> knownImports) {
    visitAnnotations(t.getAnnotationMirrors(), knownImports);
    t.getComponentType().accept(this, knownImports);
    return null;
  }

  @Override
  public Void visitDeclared(
      final @NonNull DeclaredType t,
      final @NonNull Set<@NonNull String> knownImports) {
    final var type = t.asElement();

    if (type.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
      throw new IllegalQueriesDefinition(type, "enclosed types not supported");
    }
    visitAnnotations(t.getAnnotationMirrors(), knownImports);
    final var pkg = (PackageElement) type.getEnclosingElement();
    final var packageName = pkg.getQualifiedName().toString();
    if (!"java.lang".equals(packageName)) { // only non-lang abstractions are imported
      knownImports.add(packageName + '.' + t.asElement().getSimpleName().toString());
      for (final var typeArgument : t.getTypeArguments()) {
        typeArgument.accept(this, knownImports);
      }
    }
    return null;
  }

  @Override
  public Void visitError(
      final @NonNull ErrorType t,
      final @NonNull Set<@NonNull String> knownImports) {
    return null; // nothing to do
  }

  @Override
  public Void visitTypeVariable(
      final @NonNull TypeVariable t,
      final @NonNull Set<@NonNull String> knownImports) {
    t.getLowerBound().accept(this, knownImports);
    t.getUpperBound().accept(this, knownImports);
    return null;
  }

  @Override
  public Void visitWildcard(
      final @NonNull WildcardType t,
      final @NonNull Set<@NonNull String> knownImports) {
    visitAnnotations(t.getAnnotationMirrors(), knownImports);
    return null; // nothing to do
  }

  @Override
  public Void visitExecutable(
      final @NonNull ExecutableType t,
      final @NonNull Set<@NonNull String> knownImports) {
    visitAnnotations(t.getAnnotationMirrors(), knownImports);
    return null; // nothing to do
  }

  @Override
  public Void visitNoType(
      final @NonNull NoType t,
      final @NonNull Set<@NonNull String> knownImports) {
    return null; // nothing to do
  }

  @Override
  public Void visitUnknown(TypeMirror t, @NonNull Set<@NonNull String> knownImports) {
    throw new IllegalArgumentException("unknown type: " + t);
  }

  @Override
  public Void visitUnion(
      final @NonNull UnionType t,
      final @NonNull Set<@NonNull String> knownImports) {
    for (final var alternative : t.getAlternatives()) {
      alternative.accept(this, knownImports);
    }
    return null;
  }

  public void visitMethod(
      final @NonNull ExecutableElement method,
      final @NonNull Set<@NonNull String> knownImports) {
    method.getReturnType().accept(this, knownImports);
    for (final var parameter : method.getParameters()) {
      parameter.asType().accept(this, knownImports);
    }
    for (final var thrownType : method.getThrownTypes()) {
      thrownType.accept(this, knownImports);
    }
  }

  @Override
  public Void visit(
      final @NonNull AnnotationValue av,
      final @NonNull Set<@NonNull String> knownImports) {
    return av.accept(this, knownImports);
  }

  @Override
  public Void visitBoolean(
      final boolean b,
      final @NonNull Set<@NonNull String> knownImports) {
    return null;
  }

  @Override
  public Void visitByte(
      final byte b,
      final @NonNull Set<@NonNull String> knownImports) {
    return null;
  }

  @Override
  public Void visitChar(
      final char c,
      final @NonNull Set<@NonNull String> knownImports) {
    return null;
  }

  @Override
  public Void visitDouble(
      final double d,
      final @NonNull Set<@NonNull String> knownImports) {
    return null;
  }

  @Override
  public Void visitFloat(
      final float f,
      final @NonNull Set<@NonNull String> knownImports) {
    return null;
  }

  @Override
  public Void visitInt(
      final int i,
      final @NonNull Set<@NonNull String> knownImports) {
    return null;
  }

  @Override
  public Void visitLong(
      final long i,
      final @NonNull Set<@NonNull String> knownImports) {
    return null;
  }

  @Override
  public Void visitShort(
      final short s,
      final @NonNull Set<@NonNull String> knownImports) {
    return null;
  }

  @Override
  public Void visitString(
      final String s,
      final @NonNull Set<@NonNull String> knownImports) {
    return null;
  }

  @Override
  public Void visitType(
      final @NonNull TypeMirror t,
      final @NonNull Set<@NonNull String> knownImports) {
    return t.accept(this, knownImports);
  }

  @Override
  public Void visitEnumConstant(
      final @NonNull VariableElement c,
      final @NonNull Set<@NonNull String> knownImports) {
    return c.asType().accept(this, knownImports);
  }

  private void visitAnnotations(
      final @NonNull List<@NonNull ? extends AnnotationMirror> annotationMirrors,
      final @NonNull Set<@NonNull String> knownImports) {
    for (final var a : annotationMirrors) {
      visitAnnotation(a, knownImports);
    }
  }

  @Override
  public Void visitAnnotation(
      final @NonNull AnnotationMirror a,
      final @NonNull Set<@NonNull String> knownImports) {
    a.getAnnotationType().accept(this, knownImports);
    for (final var av: a.getElementValues().values()) {
      av.accept(this, knownImports);
    }
    return null;
  }

  @Override
  public Void visitArray(
      final @NonNull List<? extends AnnotationValue> vals,
      final @NonNull Set<@NonNull String> knownImports) {
    for (final var av: vals) {
      av.accept(this, knownImports);
    }
    return null;
  }

  @Override
  public Void visitUnknown(
      final @NonNull AnnotationValue av,
      final @NonNull Set<@NonNull String> knownImports) {
    return null;
  }
}

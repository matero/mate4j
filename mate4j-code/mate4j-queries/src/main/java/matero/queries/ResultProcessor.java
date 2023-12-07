package matero.queries;

import org.checkerframework.checker.nullness.qual.NonNull;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.util.List;

class ResultProcessor
    implements TypeVisitor<@NonNull StringBuilder, @NonNull StringBuilder>,
    AnnotationValueVisitor<@NonNull StringBuilder, @NonNull StringBuilder> {

  @Override
  public @NonNull StringBuilder visit(final @NonNull TypeMirror t) {
    return visit(t, new StringBuilder());
  }


  @Override
  public @NonNull StringBuilder visit(
      final @NonNull TypeMirror t,
      final @NonNull StringBuilder stmts) {
    t.accept(this, stmts);
    return stmts;
  }

  @Override
  public @NonNull StringBuilder visitIntersection(
      final @NonNull IntersectionType t,
      final @NonNull StringBuilder stmts) {
    for (final var bound : t.getBounds()) {
      bound.accept(this, stmts);
    }
    return stmts;
  }

  @Override
  public @NonNull StringBuilder visitPrimitive(
      final @NonNull PrimitiveType t,
      final @NonNull StringBuilder stmts) {

    stmts.append("""
        final var row = result.next();
        final var value = row.get(0);
        return value.""");
    return stmts; // nothing to do
  }

  @Override
  public @NonNull StringBuilder visitNull(
      final @NonNull NullType t,
      final @NonNull StringBuilder stmts) {
    return stmts; // nothing to do
  }

  @Override
  public @NonNull StringBuilder visitArray(
      final @NonNull ArrayType t,
      final @NonNull StringBuilder stmts) {
    visitAnnotations(t.getAnnotationMirrors(), stmts);
    t.getComponentType().accept(this, stmts);
    return stmts;
  }

  @Override
  public @NonNull StringBuilder visitDeclared(
      final @NonNull DeclaredType t,
      final @NonNull StringBuilder stmts) {
    final var type = t.asElement();

    if (type.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
      throw new IllegalQueriesDefinition(type, "enclosed types not supported");
    }
    visitAnnotations(t.getAnnotationMirrors(), stmts);
    final var pkg = (PackageElement) type.getEnclosingElement();
    final var packageName = pkg.getQualifiedName().toString();
    if (!"java.lang".equals(packageName)) { // only non-lang abstractions are imported
      //stmts.add(packageName + '.' + t.asElement().getSimpleName().toString());
      for (final var typeArgument : t.getTypeArguments()) {
        typeArgument.accept(this, stmts);
      }
    }
    return stmts;
  }

  @Override
  public @NonNull StringBuilder visitError(
      final @NonNull ErrorType t,
      final @NonNull StringBuilder stmts) {
    return stmts; // nothing to do
  }

  @Override
  public @NonNull StringBuilder visitTypeVariable(
      final @NonNull TypeVariable t,
      final @NonNull StringBuilder stmts) {
    t.getLowerBound().accept(this, stmts);
    t.getUpperBound().accept(this, stmts);
    return stmts;
  }

  @Override
  public @NonNull StringBuilder visitWildcard(
      final @NonNull WildcardType t,
      final @NonNull StringBuilder stmts) {
    visitAnnotations(t.getAnnotationMirrors(), stmts);
    return stmts; // nothing to do
  }

  @Override
  public @NonNull StringBuilder visitExecutable(
      final @NonNull ExecutableType t,
      final @NonNull StringBuilder stmts) {
    visitAnnotations(t.getAnnotationMirrors(), stmts);
    return stmts; // nothing to do
  }

  @Override
  public @NonNull StringBuilder visitNoType(
      final @NonNull NoType t,
      final @NonNull StringBuilder stmts) {
    return stmts; // nothing to do
  }

  @Override
  public @NonNull StringBuilder visitUnknown(TypeMirror t, @NonNull StringBuilder stmts) {
    throw new IllegalArgumentException("unknown type: " + t);
  }

  @Override
  public @NonNull StringBuilder visitUnion(
      final @NonNull UnionType t,
      final @NonNull StringBuilder stmts) {
    for (final var alternative : t.getAlternatives()) {
      alternative.accept(this, stmts);
    }
    return stmts;
  }

  public void visitMethod(
      final @NonNull ExecutableElement method,
      final @NonNull StringBuilder stmts) {
    method.getReturnType().accept(this, stmts);
    for (final var parameter : method.getParameters()) {
      parameter.asType().accept(this, stmts);
    }
    for (final var thrownType : method.getThrownTypes()) {
      thrownType.accept(this, stmts);
    }
  }

  @Override
  public @NonNull StringBuilder visit(
      final @NonNull AnnotationValue av,
      final @NonNull StringBuilder stmts) {
    return av.accept(this, stmts);
  }

  @Override
  public @NonNull StringBuilder visitBoolean(
      final boolean b,
      final @NonNull StringBuilder stmts) {
    return stmts;
  }

  @Override
  public @NonNull StringBuilder visitByte(
      final byte b,
      final @NonNull StringBuilder stmts) {
    return stmts;
  }

  @Override
  public @NonNull StringBuilder visitChar(
      final char c,
      final @NonNull StringBuilder stmts) {
    return stmts;
  }

  @Override
  public @NonNull StringBuilder visitDouble(
      final double d,
      final @NonNull StringBuilder stmts) {
    return stmts;
  }

  @Override
  public @NonNull StringBuilder visitFloat(
      final float f,
      final @NonNull StringBuilder stmts) {
    return stmts;
  }

  @Override
  public @NonNull StringBuilder visitInt(
      final int i,
      final @NonNull StringBuilder stmts) {
    return stmts;
  }

  @Override
  public @NonNull StringBuilder visitLong(
      final long i,
      final @NonNull StringBuilder stmts) {
    return stmts;
  }

  @Override
  public @NonNull StringBuilder visitShort(
      final short s,
      final @NonNull StringBuilder stmts) {
    return stmts;
  }

  @Override
  public @NonNull StringBuilder visitString(
      final String s,
      final @NonNull StringBuilder stmts) {
    return stmts;
  }

  @Override
  public @NonNull StringBuilder visitType(
      final @NonNull TypeMirror t,
      final @NonNull StringBuilder stmts) {
    return t.accept(this, stmts);
  }

  @Override
  public @NonNull StringBuilder visitEnumConstant(
      final @NonNull VariableElement c,
      final @NonNull StringBuilder stmts) {
    return c.asType().accept(this, stmts);
  }

  private void visitAnnotations(
      final @NonNull List<@NonNull ? extends AnnotationMirror> annotationMirrors,
      final @NonNull StringBuilder stmts) {
    for (final var a : annotationMirrors) {
      visitAnnotation(a, stmts);
    }
  }

  @Override
  public @NonNull StringBuilder visitAnnotation(
      final @NonNull AnnotationMirror a,
      final @NonNull StringBuilder stmts) {
    a.getAnnotationType().accept(this, stmts);
    for (final var av : a.getElementValues().values()) {
      av.accept(this, stmts);
    }
    return stmts;
  }

  @Override
  public @NonNull StringBuilder visitArray(
      final @NonNull List<? extends AnnotationValue> vals,
      final @NonNull StringBuilder stmts) {
    for (final var av : vals) {
      av.accept(this, stmts);
    }
    return stmts;
  }

  @Override
  public @NonNull StringBuilder visitUnknown(
      final @NonNull AnnotationValue av,
      final @NonNull StringBuilder stmts) {
    return stmts;
  }
}

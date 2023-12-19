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

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.util.List;

final class ReturnType {
  public final @NonNull String annotations;
  public final @NonNull String javaSpec;
  public final @NonNull String executionTemplate;

  ReturnType(
      final @NonNull String annotations,
      final @NonNull String javaSpec,
      final @NonNull String executionTemplate) {
    this.annotations = annotations;
    this.javaSpec = javaSpec;
    this.executionTemplate = executionTemplate;
  }

  @Override
  public String toString() {
    if (this.annotations.isEmpty()) {
      return this.javaSpec;
    } else {
      return this.annotations + ' ' + this.javaSpec;
    }
  }

  public @NonNull String getAnnotations() {
    return this.annotations;
  }

  public @NonNull String getJavaSpec() {
    return this.javaSpec;
  }

  public @NonNull String getExecutionTemplate() {
    return this.executionTemplate;
  }

  static @NonNull ReturnTypeBuilder builder() {
    return new DefaultReturnTypeBuilder();
  }
}

interface ReturnTypeBuilder {

  @NonNull ReturnType build();

  @NonNull String getAnnotations();

  @NonNull String getJavaSpec();

  @NonNull String getExecutionTemplate();

  @NonNull ReturnTypeBuilder javaSpec(@NonNull String value);

  @NonNull ReturnTypeBuilder executionTemplate(@NonNull String value);

  boolean hasAnnotations();

  default @NonNull ReturnTypeBuilder primitive(final @NonNull String name) {
    return executionTemplate(name).javaSpec(name);
  }

  default @NonNull ReturnTypeBuilder primitiveWrapper(final @NonNull String name) {
    return executionTemplate(name).javaSpec(name);
  }

  default @NonNull ReturnTypeBuilder supportedReference(final @NonNull String name) {
    return executionTemplate(name).javaSpec(name);
  }

  @NonNull ReturnTypeBuilder appendAnnotation(@NonNull String annotation);

  @NonNull ReturnTypeBuilder startAnnotationValues();

  @NonNull ReturnTypeBuilder appendAnnotationValueName(@NonNull String name);

  @NonNull ReturnTypeBuilder appendAnnotationValue(@NonNull String value);

  @NonNull ReturnTypeBuilder appendAnnotationValue(boolean value);

  @NonNull ReturnTypeBuilder appendAnnotationValue(byte value);

  @NonNull ReturnTypeBuilder appendAnnotationValue(short value);

  @NonNull ReturnTypeBuilder appendAnnotationValue(int value);

  @NonNull ReturnTypeBuilder appendAnnotationValue(long value);

  @NonNull ReturnTypeBuilder appendAnnotationValue(float value);

  @NonNull ReturnTypeBuilder appendAnnotationValue(double value);

  @NonNull ReturnTypeBuilder appendAnnotationValue(char value);

  @NonNull ReturnTypeBuilder appendAnnotationQuotedValue(@NonNull String s);

  @NonNull ReturnTypeBuilder endAnnotationValues();
}

enum PredefinedReturnType
    implements ReturnTypeBuilder {
  VOID("", "void", "return/void"),
  VOID_WRAPPER("", "Void", "return/Void");

  private final ReturnType returnType;

  PredefinedReturnType(
      final @NonNull String annotations,
      final @NonNull String javaSpec,
      final @NonNull String executionTemplate) {
    this.returnType = new ReturnType(annotations, javaSpec, executionTemplate);
  }

  @Override
  public @NonNull ReturnType build() {
    return this.returnType;
  }

  @Override
  public @NonNull String getAnnotations() {
    return this.returnType.annotations;
  }

  @Override
  public @NonNull String getJavaSpec() {
    return this.returnType.javaSpec;
  }

  @Override
  public @NonNull String getExecutionTemplate() {
    return this.returnType.executionTemplate;
  }

  @Override
  public @NonNull ReturnTypeBuilder javaSpec(final @NonNull String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder executionTemplate(final @NonNull String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasAnnotations() {
    return false;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotation(final @NonNull String annotation) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder startAnnotationValues() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValueName(final @NonNull String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final @NonNull String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final boolean value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final byte value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final short value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final int value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final long value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final float value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final double value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final char value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationQuotedValue(final @NonNull String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder endAnnotationValues() {
    throw new UnsupportedOperationException();
  }
}

final class DefaultReturnTypeBuilder implements ReturnTypeBuilder {
  private @Nullable StringBuilder annotations;
  private @Nullable String javaSpec;
  private @Nullable String executionTemplate;

  @Override
  public @NonNull ReturnType build() {
    return new ReturnType(getAnnotations(), getJavaSpec(), getExecutionTemplate());
  }

  @Override
  public @NonNull String getAnnotations() {
    return this.annotations == null ? "" : this.annotations.toString();
  }

  @Override
  public @NonNull String getJavaSpec() {
    if (this.javaSpec == null) {
      throw new NullPointerException("this.javaSpec");
    }
    return this.javaSpec;
  }

  @Override
  public @NonNull String getExecutionTemplate() {
    if (this.executionTemplate == null) {
      throw new NullPointerException("this.executionTemplate");
    }
    return "return/" + this.executionTemplate;
  }

  @Override
  public @NonNull ReturnTypeBuilder javaSpec(final @NonNull String value) {
    this.javaSpec = value;
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder executionTemplate(final @NonNull String value) {
    this.executionTemplate = value;
    return this;
  }


  @Override
  public boolean hasAnnotations() {
    return this.annotations != null && !this.annotations.isEmpty();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotation(final @NonNull String annotation) {
    annotations().append('@').append(annotation);
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder startAnnotationValues() {
    annotations().append('(');
    return this;
  }

  private @NonNull StringBuilder annotations() {
    if (this.annotations == null) {
      this.annotations = new StringBuilder();
    }
    return this.annotations;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValueName(final @NonNull String name) {
    final var ann = annotations();
    if (ann.charAt(ann.length() - 1) != '(') {
      ann.ensureCapacity(ann.length() + 3 + name.length());
      ann.append(", ");
    } else {
      ann.ensureCapacity(ann.length() + 1 + name.length());
    }
    ann.append(name).append('=');
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final @NonNull String value) {
    annotations().append('"').append(value).append('"');
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(boolean value) {
    annotations().append(value);
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(byte value) {
    annotations().append(value);
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(short value) {
    annotations().append(value);
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(int value) {
    annotations().append(value);
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(long value) {
    annotations().append(value);
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(float value) {
    annotations().append(value);
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(double value) {
    annotations().append(value);
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(char value) {
    annotations().append(value);
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationQuotedValue(@NonNull String value) {
    annotations().append('"').append(value).append('"');
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder endAnnotationValues() {
    annotations().append(')');
    return this;
  }
}

enum DescribeJavaReturnType
    implements TypeVisitor<@NonNull ReturnTypeBuilder, @NonNull ReturnTypeBuilder>,
    AnnotationValueVisitor<@NonNull ReturnTypeBuilder, @NonNull ReturnTypeBuilder> {

  VISITOR;

  @Override
  public @NonNull ReturnTypeBuilder visit(final @NonNull TypeMirror t) {
    return visit(t, ReturnType.builder());
  }

  @Override
  public @NonNull ReturnTypeBuilder visit(
      final @NonNull TypeMirror t,
      final @NonNull ReturnTypeBuilder builder) {
    return t.accept(this, builder);
  }

  @Override
  public @NonNull ReturnTypeBuilder visitPrimitive(
      final @NonNull PrimitiveType t,
      final @NonNull ReturnTypeBuilder builder) {
    visitAnnotations(t.getAnnotationMirrors(), builder);
    return switch (t.getKind()) {
      case BOOLEAN -> builder.primitive("boolean");
      case CHAR -> builder.primitive("char");
      case BYTE -> builder.primitive("byte");
      case SHORT -> builder.primitive("short");
      case INT -> builder.primitive("int");
      case LONG -> builder.primitive("long");
      case FLOAT -> builder.primitive("float");
      case DOUBLE -> builder.primitive("double");
      default -> throw new IllegalArgumentException("unknown primitive type");
    };
  }

  void visitAnnotations(
      final @NonNull List<? extends AnnotationMirror> annotationMirrors,
      final @NonNull ReturnTypeBuilder builder) {
    for (final var annotation : annotationMirrors) {
      visitAnnotation(annotation, builder);
    }
  }

  @Override
  public @NonNull ReturnTypeBuilder visitNull(
      final @NonNull NullType t,
      final @NonNull ReturnTypeBuilder builder) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder visitArray(
      final @NonNull ArrayType t,
      final @NonNull ReturnTypeBuilder builder) {
    final var componentType = t.getComponentType();
    if (componentType.getKind() != TypeKind.BYTE) {
      throw new IllegalArgumentException("only byte[] are allowed");
    }
    visitAnnotations(t.getAnnotationMirrors(), builder);
    final var component = componentType.accept(this, ReturnType.builder());

    if (component.hasAnnotations()) {
      builder.javaSpec("byte " + component.getAnnotations() + " []");
    } else {
      builder.javaSpec("byte[]");
    }
    return builder.executionTemplate("byteArray");
  }

  @Override
  public @NonNull ReturnTypeBuilder visitDeclared(
      final @NonNull DeclaredType t,
      final @NonNull ReturnTypeBuilder builder) {
    visitAnnotations(t.getAnnotationMirrors(), builder);
    final var type = (TypeElement) t.asElement();
    final var name = type.getSimpleName().toString();
    final var qname = type.getQualifiedName().toString();

    switch (qname) {
      case "java.lang.Character":
        builder.javaSpec("Character").executionTemplate("Character");
        break;
      case "java.lang.Void":
        return PredefinedReturnType.VOID_WRAPPER;
      case "java.lang.Boolean",
          "java.lang.Byte",
          "java.lang.Short",
          "java.lang.Integer",
          "java.lang.Long",
          "java.lang.Float",
          "java.lang.Double":
        builder.primitiveWrapper(name);
        break;
      case "java.lang.Object",
          "java.lang.String",
          "org.neo4j.driver.types.Entity",
          "org.neo4j.driver.types.Node",
          "org.neo4j.driver.types.Relationship",
          "org.neo4j.driver.types.Path",
          "java.time.LocalDate",
          "java.time.LocalDateTime",
          "java.time.OffsetTime",
          "java.time.LocalTime",
          "java.time.OffsetDateTime",
          "java.time.ZonedDateTime",
          "org.neo4j.driver.types.IsoDuration",
          "org.neo4j.driver.types.Point":
        builder.supportedReference(name);
        break;
      default:
        throw new IllegalQueriesDefinition(t.asElement(), "unsupported type "+t);
    }
/*
      final var typeArguments = t.getTypeArguments();
      if (!typeArguments.isEmpty()) {
        builder.append('<');
        final var args = typeArguments.iterator();
        args.next().accept(this, builder);
        while (args.hasNext()) {
          args.next().accept(this, builder.append(", "));
        }
        builder.append('>');
      }
*/
    return builder;
  }

  @Override
  public @NonNull ReturnTypeBuilder visitError(
      final @NonNull ErrorType t,
      final @NonNull ReturnTypeBuilder builder) {
    throw new IllegalQueriesDefinition(t.asElement(), "unsupported error type");
  }

  @Override
  public @NonNull ReturnTypeBuilder visitTypeVariable(
      final @NonNull TypeVariable t,
      final @NonNull ReturnTypeBuilder builder) {
    throw new IllegalQueriesDefinition(t.asElement(), "unsupported variable type");
  }

  @Override
  public @NonNull ReturnTypeBuilder visitWildcard(
      final @NonNull WildcardType t,
      final @Nullable ReturnTypeBuilder builder) {
    throw new IllegalArgumentException("unsupported wilcard");
  }

  @Override
  public @NonNull ReturnTypeBuilder visitExecutable(
      final @NonNull ExecutableType t,
      final @NonNull ReturnTypeBuilder builder) {
    throw new IllegalArgumentException("unsupported executable type");
  }

  @Override
  public @NonNull ReturnTypeBuilder visitNoType(
      final @NonNull NoType t,
      final @NonNull ReturnTypeBuilder builder) {
    return builder.javaSpec("void").executionTemplate("void");

  }

  @Override
  public @NonNull ReturnTypeBuilder visitUnknown(
      final @NonNull TypeMirror t,
      final @NonNull ReturnTypeBuilder builder) {
    throw new IllegalArgumentException("unsupported variable type");
  }

  @Override
  public @NonNull ReturnTypeBuilder visitUnion(
      final @NonNull UnionType t,
      final @NonNull ReturnTypeBuilder builder) {
    throw new IllegalArgumentException("unsupported variable type");
  }

  @Override
  public @NonNull ReturnTypeBuilder visitIntersection(
      final @NonNull IntersectionType t,
      final @NonNull ReturnTypeBuilder builder) {
    throw new IllegalArgumentException("unsupported variable type");
  }

  @Override
  public @NonNull ReturnTypeBuilder visit(
      final @NonNull AnnotationValue av,
      final @NonNull ReturnTypeBuilder builder) {
    return av.accept(this, builder);
  }

  @Override
  public @NonNull ReturnTypeBuilder visit(final @NonNull AnnotationValue av) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder visitBoolean(
      final boolean b,
      final @NonNull ReturnTypeBuilder builder) {
    builder.appendAnnotationValue(b);
    return builder;
  }

  @Override
  public @NonNull ReturnTypeBuilder visitByte(
      final byte b,
      final @NonNull ReturnTypeBuilder builder) {
    builder.appendAnnotationValue(b);
    return builder;
  }

  @Override
  public @NonNull ReturnTypeBuilder visitChar(
      final char c,
      final @NonNull ReturnTypeBuilder builder) {
    builder.appendAnnotationValue(c);
    return builder;
  }

  @Override
  public @NonNull ReturnTypeBuilder visitDouble(
      final double d,
      final @NonNull ReturnTypeBuilder builder) {
    builder.appendAnnotationValue(d);
    return builder;
  }

  @Override
  public @NonNull ReturnTypeBuilder visitFloat(
      final float f,
      @NonNull ReturnTypeBuilder builder) {
    builder.appendAnnotationValue(f);
    return builder;
  }

  @Override
  public @NonNull ReturnTypeBuilder visitInt(
      final int i,
      final @NonNull ReturnTypeBuilder builder) {
    builder.appendAnnotationValue(i);
    return builder;
  }

  @Override
  public @NonNull ReturnTypeBuilder visitLong(
      final long i,
      final @NonNull ReturnTypeBuilder builder) {
    builder.appendAnnotationValue(i);
    return builder;
  }

  @Override
  public @NonNull ReturnTypeBuilder visitShort(
      final short s,
      final @NonNull ReturnTypeBuilder builder) {
    builder.appendAnnotationValue(s);
    return builder;
  }

  @Override
  public @NonNull ReturnTypeBuilder visitString(
      final @NonNull String s,
      final @NonNull ReturnTypeBuilder builder) {
    builder.appendAnnotationQuotedValue(s);
    return builder;
  }

  @Override
  public @NonNull ReturnTypeBuilder visitType(
      final @NonNull TypeMirror t,
      final @NonNull ReturnTypeBuilder builder) {
    builder.appendAnnotationValue(t.toString());
    return builder;
  }

  @Override
  public @NonNull ReturnTypeBuilder visitEnumConstant(
      final @NonNull VariableElement c,
      final @NonNull ReturnTypeBuilder builder) {
    builder.appendAnnotationValue(c.getSimpleName().toString());
    return builder;
  }

  @Override
  public @NonNull ReturnTypeBuilder visitAnnotation(
      final @NonNull AnnotationMirror a,
      final @NonNull ReturnTypeBuilder builder) {
    builder.appendAnnotation(a.getAnnotationType().asElement().getSimpleName().toString());
    final var elementValues = a.getElementValues();
    if (!elementValues.isEmpty()) {
      builder.startAnnotationValues();
      for (final var value : elementValues.entrySet()) {
        builder.appendAnnotationValueName(value.getKey().getSimpleName().toString());
        value.getValue().accept(this, builder);
      }
      builder.endAnnotationValues();
    }
    return builder;
  }

  @Override
  public @NonNull ReturnTypeBuilder visitArray(
      final @NonNull List<? extends AnnotationValue> vals,
      final @NonNull ReturnTypeBuilder builder) {
    if (vals.isEmpty()) {
      builder.appendAnnotationValue("[]");
    } else {
      builder.appendAnnotationValue("[");
      final var e = vals.iterator();
      e.next().accept(this, builder);
      while (e.hasNext()) {
        builder.appendAnnotationValue(", ");
        e.next().accept(this, builder);
      }
      builder.appendAnnotationValue("]");
    }
    return builder;
  }

  @Override
  public @NonNull ReturnTypeBuilder visitUnknown(
      final @NonNull AnnotationValue av,
      final @NonNull ReturnTypeBuilder builder) {
    throw new UnsupportedOperationException();
  }
}


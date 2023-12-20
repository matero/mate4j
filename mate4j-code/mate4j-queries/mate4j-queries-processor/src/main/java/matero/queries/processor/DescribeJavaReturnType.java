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

import matero.queries.processor.ReturnType.Mapper;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.util.List;

import static matero.queries.processor.ReturnType.Mapper.instanceMethod;
import static matero.queries.processor.ReturnType.Mapper.staticMethod;

enum DescribeJavaReturnType
    implements TypeVisitor<@NonNull ReturnTypeBuilder, @NonNull ReturnTypeBuilder>,
    AnnotationValueVisitor<@NonNull ReturnTypeBuilder, @NonNull ReturnTypeBuilder> {

  VISITOR;

  private static final @NonNull Mapper asByteArray = instanceMethod("value", "asByteArray");
  private static final @NonNull Mapper asObject = instanceMethod("value", "asObject");
  private static final @NonNull Mapper asString = instanceMethod("value", "asString");
  private static final @NonNull Mapper asEntity = instanceMethod("value", "asEntity");
  private static final @NonNull Mapper asNode = instanceMethod("value", "asNode");
  private static final @NonNull Mapper asRelationship = instanceMethod("value", "asRelationship");
  private static final @NonNull Mapper asPath = instanceMethod("value", "asPath");
  private static final @NonNull Mapper asLocalDate = instanceMethod("value", "asLocalDate");
  private static final @NonNull Mapper asLocalDateTime = instanceMethod("value", "asLocalDateTime");
  private static final @NonNull Mapper asOffsetTime = instanceMethod("value", "asOffsetTime");
  private static final @NonNull Mapper asLocalTime = instanceMethod("value", "asLocalTime");
  private static final @NonNull Mapper asOffsetDateTime = instanceMethod("value", "asOffsetDateTime");
  private static final @NonNull Mapper asZonedDateTime = instanceMethod("value", "asZonedDateTime");
  private static final @NonNull Mapper asIsoDuration = instanceMethod("value", "asIsoDuration");
  private static final @NonNull Mapper asPoint = instanceMethod("value", "asPoint");
  private static final @NonNull Mapper asList = instanceMethod("value", "asList");
  private static final @NonNull Mapper toPrimitiveBoolean = staticMethod("value", "matero.queries.neo4j.MapValue.toPrimitiveBoolean");
  private static final @NonNull Mapper toPrimitiveChar = staticMethod("value", "matero.queries.neo4j.MapValue.toPrimitiveChar");
  private static final @NonNull Mapper toPrimitiveByte = staticMethod("value", "matero.queries.neo4j.MapValue.toPrimitiveByte");
  private static final @NonNull Mapper toPrimitiveShort = staticMethod("value", "matero.queries.neo4j.MapValue.toPrimitiveShort");
  private static final @NonNull Mapper toPrimitiveInt = staticMethod("value", "matero.queries.neo4j.MapValue.toPrimitiveInt");
  private static final @NonNull Mapper toPrimitiveLong = staticMethod("value", "matero.queries.neo4j.MapValue.toPrimitiveLong");
  private static final @NonNull Mapper toPrimitiveFloat = staticMethod("value", "matero.queries.neo4j.MapValue.toPrimitiveFloat");
  private static final @NonNull Mapper toPrimitiveDouble = staticMethod("value", "matero.queries.neo4j.MapValue.toPrimitiveDouble");
  private static final @NonNull Mapper toNullableBoolean = staticMethod("value", "matero.queries.neo4j.MapValue.toNullableBoolean");
  private static final @NonNull Mapper toNullableCharacter = staticMethod("value", "matero.queries.neo4j.MapValue.toNullableCharacter");
  private static final @NonNull Mapper toNullableByte = staticMethod("value", "matero.queries.neo4j.MapValue.toNullableByte");
  private static final @NonNull Mapper toNullableShort = staticMethod("value", "matero.queries.neo4j.MapValue.toNullableShort");
  private static final @NonNull Mapper toNullableInteger = staticMethod("value", "matero.queries.neo4j.MapValue.toNullableInteger");
  private static final @NonNull Mapper toNullableLong = staticMethod("value", "matero.queries.neo4j.MapValue.toNullableLong");
  private static final @NonNull Mapper toNullableFloat = staticMethod("value", "matero.queries.neo4j.MapValue.toNullableFloat");
  private static final @NonNull Mapper toNullableDouble = staticMethod("value", "matero.queries.neo4j.MapValue.toNullableDouble");

  private int level = 0;

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
    switch (t.getKind()) {
      case BOOLEAN -> builder.javaSpec("boolean").mapper(toPrimitiveBoolean);
      case CHAR -> builder.javaSpec("char").mapper(toPrimitiveChar);
      case BYTE -> builder.javaSpec("byte").mapper(toPrimitiveByte);
      case SHORT -> builder.javaSpec("short").mapper(toPrimitiveShort);
      case INT -> builder.javaSpec("int").mapper(toPrimitiveInt);
      case LONG -> builder.javaSpec("long").mapper(toPrimitiveLong);
      case FLOAT -> builder.javaSpec("float").mapper(toPrimitiveFloat);
      case DOUBLE -> builder.javaSpec("double").mapper(toPrimitiveDouble);
      default -> throw new IllegalArgumentException("unknown primitive type");
    }
    return builder.executionTemplate("return/single");
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
    return builder
        .executionTemplate("return/single")
        .mapper(asByteArray);
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
      case "java.lang.Void":
        return PredefinedReturnType.VOID_WRAPPER;
      case "java.lang.Character":
        builder.mapper(toNullableCharacter);
        break;
      case "java.lang.Boolean":
        builder.mapper(toNullableBoolean);
        break;
      case "java.lang.Byte":
        builder.mapper(toNullableByte);
        break;
      case "java.lang.Short":
        builder.mapper(toNullableShort);
        break;
      case "java.lang.Integer":
        builder.mapper(toNullableInteger);
        break;
      case "java.lang.Long":
        builder.mapper(toNullableLong);
        break;
      case "java.lang.Float":
        builder.mapper(toNullableFloat);
        break;
      case "java.lang.Double":
        builder.mapper(toNullableDouble);
        break;
      case "java.lang.Object":
        builder.mapper(asObject);
        break;
      case "java.lang.String":
        builder.mapper(asString);
        break;
      case "org.neo4j.driver.types.Entity":
        builder.mapper(asEntity);
        break;
      case "org.neo4j.driver.types.Node":
        builder.mapper(asNode);
        break;
      case "org.neo4j.driver.types.Relationship":
        builder.mapper(asRelationship);
        break;
      case "org.neo4j.driver.types.Path":
        builder.mapper(asPath);
        break;
      case "java.time.LocalDate":
        builder.mapper(asLocalDate);
        break;
      case "java.time.LocalDateTime":
        builder.mapper(asLocalDateTime);
        break;
      case "java.time.OffsetTime":
        builder.mapper(asOffsetTime);
        break;
      case "java.time.LocalTime":
        builder.mapper(asLocalTime);
        break;
      case "java.time.OffsetDateTime":
        builder.mapper(asOffsetDateTime);
        break;
      case "java.time.ZonedDateTime":
        builder.mapper(asZonedDateTime);
        break;
      case "org.neo4j.driver.types.IsoDuration":
        builder.mapper(asIsoDuration);
        break;
      case "org.neo4j.driver.types.Point":
        builder.mapper(asPoint);
        break;
      case "java.util.List":
        if (this.level != 0) {
          this.level = 0; // avoid reporting false positives in Lists to be visited after this error
          throw new IllegalQueriesDefinition(t.asElement(), "List of List are not supported");
        }
        visitList(t, builder);
        return builder;
      default:
        throw new IllegalQueriesDefinition(t.asElement(), "unsupported type " + t);
    }
    return builder.javaSpec(name).executionTemplate("return/single");
  }

  private void visitList(
      final @NonNull DeclaredType t,
      final @NonNull ReturnTypeBuilder builder) {
    final var typeArguments = t.getTypeArguments();
    if (typeArguments.isEmpty()) {
      builder.mapper(null).javaSpec("List");
    } else {
      this.level++;
      final var componentBuilder = visit(typeArguments.getFirst());
      if (asObject.equals(componentBuilder.getMapper())) {
        builder.mapper(null);
      } else {
        builder.mapper(componentBuilder.getMapper());
      }
      if (componentBuilder.hasAnnotations()) {
        builder.javaSpec("List<" + componentBuilder.getAnnotations() + ' ' + componentBuilder.getJavaSpec() + '>');
      } else {
        builder.javaSpec("List<" + componentBuilder.getJavaSpec() + '>');
      }
      this.level--;
    }
    builder.executionTemplate("return/list");
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
      final @NonNull ReturnTypeBuilder builder) {
    return builder
        .javaSpec(t.toString())
        .mapper(asObject);
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
    return PredefinedReturnType.VOID;
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

  void visitAnnotations(
      final @NonNull List<? extends AnnotationMirror> annotationMirrors,
      final @NonNull ReturnTypeBuilder builder) {
    for (final var annotation : annotationMirrors) {
      visitAnnotation(annotation, builder);
    }
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


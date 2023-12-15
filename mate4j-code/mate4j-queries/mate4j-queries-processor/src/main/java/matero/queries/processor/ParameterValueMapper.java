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

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.*;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

class ParameterValueMapper implements TypeVisitor<@NonNull String, @NonNull ValueMappingContext> {
  private static final Set<@NonNull String> ATOMIC_VALUE_CLASSES = Set.of(
      AtomicBoolean.class.getCanonicalName(),
      AtomicInteger.class.getCanonicalName(),
      AtomicLong.class.getCanonicalName());

  private static final @NonNull Set<@NonNull String> NATIVELY_SUPPORTED_TYPES;

  static {
    final var supportedTypes = new java.util.HashSet<@NonNull String>();
    supportedTypes.add(String.class.getCanonicalName());
    supportedTypes.add(Boolean.class.getCanonicalName());
    supportedTypes.add(Byte.class.getCanonicalName());
    supportedTypes.add(Short.class.getCanonicalName());
    supportedTypes.add(Integer.class.getCanonicalName());
    supportedTypes.add(Long.class.getCanonicalName());
    supportedTypes.add(Float.class.getCanonicalName());
    supportedTypes.add(Double.class.getCanonicalName());
    supportedTypes.add(org.neo4j.graphdb.spatial.Point.class.getCanonicalName());
    supportedTypes.add(java.time.LocalDate.class.getCanonicalName());
    supportedTypes.add(java.time.OffsetTime.class.getCanonicalName());
    supportedTypes.add(java.time.LocalTime.class.getCanonicalName());
    supportedTypes.add(java.time.ZonedDateTime.class.getCanonicalName());
    supportedTypes.add(java.time.LocalDateTime.class.getCanonicalName());
    supportedTypes.add(java.time.temporal.TemporalAmount.class.getCanonicalName());
    NATIVELY_SUPPORTED_TYPES = Set.copyOf(supportedTypes);
  }

  private final @NonNull Types types;
  private final @NonNull TypeMirror collectionType;
  private final @NonNull TypeMirror listType;
  private final @NonNull TypeMirror mapType;
  boolean fromNativelySupported;

  ParameterValueMapper(
      final @NonNull Types types,
      final @NonNull TypeElement collectionType,
      final @NonNull TypeElement listType,
      final @NonNull TypeElement mapType) {
    this.collectionType = types.erasure(collectionType.asType());
    this.listType = types.erasure(listType.asType());
    this.mapType = types.erasure(mapType.asType());
    this.types = types;
  }

  @Override
  public @NonNull String visit(
      final @NonNull TypeMirror t,
      final @NonNull ValueMappingContext ctx) {
    this.fromNativelySupported = false;
    return t.accept(this, ctx);
  }

  @Override
  public @NonNull String visitPrimitive(
      final @NonNull PrimitiveType t,
      final @NonNull ValueMappingContext ctx) {
    return switch (t.getKind()) {
      case BOOLEAN, DOUBLE, LONG -> {
        this.fromNativelySupported = true;
        yield ctx.name;
      }
      case FLOAT -> {
        this.fromNativelySupported = false;
        yield "Double.valueOf(" + ctx.name + ')';
      }
      case BYTE, SHORT, INT -> {
        this.fromNativelySupported = false;
        yield "Long.valueOf(" + ctx.name + ')';
      }
      case CHAR -> {
        this.fromNativelySupported = false;
        yield "String.valueOf(" + ctx.name + ')';
      }
      default -> throw new IllegalQueriesDefinition(ctx.root, "unknown primitive type");
    };
  }

  @Override
  public @NonNull String visitNull(
      final @NonNull NullType t,
      final @NonNull ValueMappingContext ctx) {
    throw new IllegalQueriesDefinition(ctx.root, "unserializable null type");
  }

  @Override
  public @NonNull String visitArray(
      final @NonNull ArrayType t,
      final @NonNull ValueMappingContext ctx) {
    this.fromNativelySupported = false;
    final var componentCtx = ctx.ofComponent(t.getComponentType());
    final var componentSerialization = t.getComponentType().accept(this, componentCtx);
    if (this.fromNativelySupported) {
      return "java.util.List.of(" + ctx.name + ")";
    } else {
      return "java.util.Arrays.stream(" + ctx.name + ").map(" + componentCtx.name + " -> " + componentSerialization + ").collect(java.util.Collectors.toList())";
    }
  }

  @Override
  public @NonNull String visitDeclared(
      final @NonNull DeclaredType t,
      final @NonNull ValueMappingContext ctx) {
    this.fromNativelySupported = isNativelySupported(t);
    if (this.fromNativelySupported) {
      return ctx.name;
    } else if (isAtomicValue(t)) {
      return ctx.name + ".get()";
    } else if (isAtomicReference(t)) {
      final var componentType = t.getTypeArguments().get(0);
      final var componentCtx = ctx.ofComponent(componentType, ctx.name + ".get()");
      final var componentSerialization = t.accept(this, componentCtx);
      this.fromNativelySupported = false;
      return componentSerialization;
    } else if (isList(t)) {
      final var componentType = t.getTypeArguments().get(0);
      final var componentCtx = ctx.ofComponent(componentType);
      final var componentSerialization = componentType.accept(this, componentCtx);
      return ctx.name + ".stream().map(" + componentCtx.name + " -> " + componentSerialization + ").collect(java.util.Collectors.toList())";
    } else if (isCollection(t)) {
      final var componentType = t.getTypeArguments().getFirst();
      final var componentCtx = ctx.ofComponent(componentType);
      final var componentSerialization = componentType.accept(this, componentCtx);
      if (this.fromNativelySupported) {
        return "java.util.List.copyOf(" + ctx.name + ")";
      } else {
        return ctx.name + ".stream().map(" + componentCtx.name + " -> " + componentSerialization + ").collect(java.util.Collectors.toList())";
      }
    } else if (isMap(t)) {
      final var typeArguments = t.getTypeArguments();
      final var keyType = typeArguments.getFirst();
      final var keyName = "_k" + ctx.level;
      final var keyCtx = ctx.ofComponent(keyType, keyName);
      final var keySerialization = t.accept(this, keyCtx);
      final var nativeKey = this.fromNativelySupported;
      final var valueName = "_v" + ctx.level;
      final var valueType = typeArguments.get(1);
      final var valueCtx = ctx.ofComponent(valueType, valueName);
      final var valueSerialization = t.accept(this, valueCtx);
      final var nativeValue = this.fromNativelySupported;

      if (nativeKey && nativeValue) {
        return ctx.name;
      } else {
        return ctx.name + ".entrySet().stream().map((" + keyName + ", " + valueName + ") -> " +
            "new java.util.AbstractMap.SimpleEntry<>(" + keySerialization + ", " + valueSerialization + ")" +
            ".collect(java.util.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue())";
      }
    }

    throw new IllegalQueriesDefinition(ctx.root, "unserializable declared type");
  }

  private boolean isNativelySupported(final @NonNull DeclaredType t) {
    if (NATIVELY_SUPPORTED_TYPES.contains(typeNameOf(t))) {
      return true;
    } else if (isList(t)) {
      final var typeArguments = t.getTypeArguments();
      return isNativelySupported((DeclaredType) typeArguments.getFirst());
    } else if (isMap(t)) {
      final var typeArguments = t.getTypeArguments();
      return isNativelySupported((DeclaredType) typeArguments.get(0)) && isNativelySupported((DeclaredType) typeArguments.get(1));
    }
    return false;
  }

  @NonNull String typeNameOf(final @NonNull DeclaredType t) {
    final var type = (TypeElement) t.asElement();
    return type.getQualifiedName().toString();
  }

  boolean isAtomicValue(final @NonNull DeclaredType t) {
    return ATOMIC_VALUE_CLASSES.contains(typeNameOf(t));
  }

  boolean isAtomicReference(final @NonNull DeclaredType t) {
    return AtomicReference.class.getCanonicalName().equals(typeNameOf(t));
  }

  boolean isList(final @NonNull DeclaredType t) {
    return List.class.getCanonicalName().equals(typeNameOf(t));
  }

  boolean isCollection(final @NonNull DeclaredType t) {
    return this.types.isAssignable(t, this.collectionType);
  }

  boolean isMap(final @NonNull DeclaredType t) {
    return this.types.isAssignable(t, this.mapType);
  }

  @Override
  public @NonNull String visitError(
      final @NonNull ErrorType t,
      final @NonNull ValueMappingContext ctx) {
    throw new IllegalQueriesDefinition(ctx.root, "unserializable type");
  }

  @Override
  public @NonNull String visitTypeVariable(
      final @NonNull TypeVariable t,
      final @NonNull ValueMappingContext ctx) {
    throw new IllegalQueriesDefinition(ctx.root, "unserializable type");
  }

  @Override
  public @NonNull String visitWildcard(
      final @NonNull WildcardType t,
      final @NonNull ValueMappingContext ctx) {
    throw new IllegalQueriesDefinition(ctx.root, "unserializable type");
  }

  @Override
  public @NonNull String visitExecutable(
      final @NonNull ExecutableType t,
      final @NonNull ValueMappingContext ctx) {
    throw new IllegalQueriesDefinition(ctx.root, "unserializable type");
  }

  @Override
  public @NonNull String visitNoType(
      final @NonNull NoType t,
      final @NonNull ValueMappingContext ctx) {
    throw new IllegalQueriesDefinition(ctx.root, "unserializable type");
  }

  @Override
  public @NonNull String visitUnknown(
      final @NonNull TypeMirror t,
      final @NonNull ValueMappingContext ctx) {
    throw new IllegalQueriesDefinition(ctx.root, "unserializable type");
  }

  @Override
  public @NonNull String visitUnion(
      final @NonNull UnionType t,
      final @NonNull ValueMappingContext ctx) {
    throw new IllegalQueriesDefinition(ctx.root, "unserializable type");
  }

  @Override
  public @NonNull String visitIntersection(
      final @NonNull IntersectionType t,
      final @NonNull ValueMappingContext ctx) {
    throw new IllegalQueriesDefinition(ctx.root, "unserializable type");
  }

  @NonNull String getMapperOf(final @NonNull VariableElement parameter) {
    return parameter.asType().accept(this, new ValueMappingContext(parameter));
  }
}

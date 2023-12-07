package matero.queries;

import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.*;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ValueTypes {
  private final @NonNull Set<@NonNull TypeMirror> nativelySupportedTypes;
  private final @NonNull Types types;

  private final @NonNull TypeMirror collectionType;
  private final @NonNull TypeMirror listType;
  private final @NonNull TypeMirror mapType;
  private final @NonNull TypeMirror atomicBooleanType;
  private final @NonNull TypeMirror atomicIntegerType;
  private final @NonNull TypeMirror atomicLongType;
  private final @NonNull TypeMirror atomicReferenceType;

  private final @NonNull TypeSerializer serializer;

  ValueTypes(final @NonNull ProcessingEnvironment processingEnv) {
    this.types = processingEnv.getTypeUtils();

    final var elements = processingEnv.getElementUtils();

    final var supportedTypes = new java.util.HashSet<@NonNull TypeMirror>();

    supportedTypes.add(elements.getTypeElement(String.class.getCanonicalName()).asType());
    supportedTypes.add(elements.getTypeElement(Boolean.class.getCanonicalName()).asType());
    supportedTypes.add(elements.getTypeElement(Byte.class.getCanonicalName()).asType());
    supportedTypes.add(elements.getTypeElement(Short.class.getCanonicalName()).asType());
    supportedTypes.add(elements.getTypeElement(Integer.class.getCanonicalName()).asType());
    supportedTypes.add(elements.getTypeElement(Long.class.getCanonicalName()).asType());
    supportedTypes.add(elements.getTypeElement(Float.class.getCanonicalName()).asType());
    supportedTypes.add(elements.getTypeElement(Double.class.getCanonicalName()).asType());
    supportedTypes.add(elements.getTypeElement(org.neo4j.graphdb.spatial.Point.class.getCanonicalName()).asType());
    supportedTypes.add(elements.getTypeElement(java.time.LocalDate.class.getCanonicalName()).asType());
    supportedTypes.add(elements.getTypeElement(java.time.OffsetTime.class.getCanonicalName()).asType());
    supportedTypes.add(elements.getTypeElement(java.time.LocalTime.class.getCanonicalName()).asType());
    supportedTypes.add(elements.getTypeElement(java.time.ZonedDateTime.class.getCanonicalName()).asType());
    supportedTypes.add(elements.getTypeElement(java.time.LocalDateTime.class.getCanonicalName()).asType());
    supportedTypes.add(elements.getTypeElement(java.time.temporal.TemporalAmount.class.getCanonicalName()).asType());
    this.nativelySupportedTypes = Set.copyOf(supportedTypes);

    this.collectionType = types.erasure(elements.getTypeElement(java.util.Collection.class.getCanonicalName()).asType());
    this.listType = types.erasure(elements.getTypeElement(java.util.List.class.getCanonicalName()).asType());
    this.mapType = types.erasure(elements.getTypeElement(Map.class.getCanonicalName()).asType());

    this.atomicBooleanType = elements.getTypeElement(java.util.concurrent.atomic.AtomicBoolean.class.getCanonicalName()).asType();
    this.atomicIntegerType = elements.getTypeElement(java.util.concurrent.atomic.AtomicInteger.class.getCanonicalName()).asType();
    this.atomicLongType = elements.getTypeElement(java.util.concurrent.atomic.AtomicLong.class.getCanonicalName()).asType();
    this.atomicReferenceType = types.erasure(elements.getTypeElement(java.util.concurrent.atomic.AtomicReference.class.getCanonicalName()).asType());

    this.serializer = new TypeSerializer();
  }

  public String getSerializerFor(final @NonNull VariableElement parameter) {
    return parameter.asType().accept(this.serializer, new ValueSerializationContext(parameter));
  }

  static final class ValueSerializationContext {
    final @NonNull Element root;
    final @NonNull String name;
    final @NonNull List<@NonNull ? extends AnnotationMirror> annotations;

    private final int level;


    ValueSerializationContext(final @NonNull VariableElement parameter) {
      this(parameter, parameter.getSimpleName().toString(), parameter.getAnnotationMirrors());
    }

    ValueSerializationContext(
        final @NonNull Element root,
        final @NonNull String name,
        final @NonNull List<@NonNull ? extends AnnotationMirror> annotations) {
      this(root, name, annotations, 0);
    }

    private ValueSerializationContext(
        final @NonNull Element root,
        final @NonNull String name,
        final @NonNull List<@NonNull ? extends AnnotationMirror> annotations,
        final int level) {
      this.root = root;
      this.name = name;
      this.annotations = annotations;
      this.level = level;
    }

    @NonNull ValueSerializationContext ofComponent(final @NonNull TypeMirror componentType) {
      return ofComponent(componentType, componentName());
    }

    @NonNull String componentName() {
      return "_it" + this.level;
    }

    @NonNull ValueSerializationContext ofComponent(
        final @NonNull TypeMirror componentType,
        final @NonNull String componentName) {
      return new ValueSerializationContext(this.root, componentName, componentType.getAnnotationMirrors(), this.level + 1);
    }
  }

  class TypeSerializer implements TypeVisitor<@NonNull String, @NonNull ValueSerializationContext> {
    boolean fromNativelySupported;

    @Override
    public @NonNull String visit(
        final @NonNull TypeMirror t,
        final @NonNull ValueSerializationContext ctx) {
      this.fromNativelySupported = false;
      return t.accept(this, ctx);
    }

    @Override
    public @NonNull String visitPrimitive(
        final @NonNull PrimitiveType t,
        final @NonNull ValueSerializationContext ctx) {
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
        final @NonNull ValueSerializationContext ctx) {
      throw new IllegalQueriesDefinition(ctx.root, "unserializable null type");
    }

    @Override
    public @NonNull String visitArray(
        final @NonNull ArrayType t,
        final @NonNull ValueSerializationContext ctx) {
      this.fromNativelySupported = false;
      final var componentCtx = ctx.ofComponent(t.getComponentType());
      final var componentSerialization = t.getComponentType().accept(this, componentCtx);
      if (this.fromNativelySupported) {
        return "List.of(" + ctx.name + ")";
      } else {
        return "java.util.Arrays.stream(" + ctx.name + ").map(" + componentCtx.name + " -> " + componentSerialization + ").collect(java.util.Collectors.toList())";
      }
    }

    @Override
    public @NonNull String visitDeclared(
        final @NonNull DeclaredType t,
        final @NonNull ValueSerializationContext ctx) {
      this.fromNativelySupported = ValueTypes.this.nativelySupportedTypes.contains(t);
      if (this.fromNativelySupported) {
        return ctx.name;
      } else {
        if (isAtomicValue(t)) {
          return ctx.name + ".get()";
        } else if (isAtomicReference(t)) {
          if (!t.getTypeArguments().isEmpty()) { //raw type
            final var componentType = t.getTypeArguments().get(0);
            final var componentCtx = ctx.ofComponent(componentType, ctx.name + ".get()");
            final var componentSerialization = t.accept(this, componentCtx);
            this.fromNativelySupported = false;
            return componentSerialization;
          }
        } else if (isList(t)) {
          if (!t.getTypeArguments().isEmpty()) { //raw type
            final var componentType = t.getTypeArguments().get(0);
            final var componentCtx = ctx.ofComponent(componentType);
            final var componentSerialization = componentType.accept(this, componentCtx);
            if (this.fromNativelySupported) {
              return ctx.name;
            } else {
              return ctx.name + ".stream().map(" + componentCtx.name + " -> " + componentSerialization + ").collect(java.util.Collectors.toList())";
            }
          } else if (isCollection(t)) {
            if (!t.getTypeArguments().isEmpty()) { //raw type
              final var componentType = t.getTypeArguments().get(0);
              final var componentCtx = ctx.ofComponent(componentType);
              final var componentSerialization = componentType.accept(this, componentCtx);
              if (this.fromNativelySupported) {
                return "List.copyOf(" + ctx.name + ")";
              } else {
                return ctx.name + ".stream().map(" + componentCtx.name + " -> " + componentSerialization + ").collect(java.util.Collectors.toList())";
              }
            }
          } else if (isMap(t)) {
            if (!t.getTypeArguments().isEmpty()) { //raw type
              final var typeArguments = t.getTypeArguments();
              final var keyType = typeArguments.get(0);
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
          }
        }
        throw new IllegalQueriesDefinition(ctx.root, "unserializable declared type");
      }
    }

    boolean isAtomicValue(final @NonNull TypeMirror t) {
      return isAtomicBoolean(t) || isAtomicInteger(t) || isAtomicLong(t);
    }

    boolean isAtomicBoolean(final @NonNull TypeMirror t) {
      return ValueTypes.this.types.isAssignable(t, ValueTypes.this.atomicBooleanType);
    }

    boolean isAtomicInteger(final @NonNull TypeMirror t) {
      return ValueTypes.this.types.isAssignable(t, ValueTypes.this.atomicIntegerType);
    }

    boolean isAtomicLong(final @NonNull TypeMirror t) {
      return ValueTypes.this.types.isAssignable(t, ValueTypes.this.atomicLongType);
    }

    boolean isAtomicReference(final @NonNull DeclaredType t) {
      return ValueTypes.this.types.isAssignable(t, ValueTypes.this.atomicReferenceType);
    }

    boolean isList(final @NonNull DeclaredType t) {
      return ValueTypes.this.types.isAssignable(t, ValueTypes.this.listType);
    }

    boolean isCollection(final @NonNull DeclaredType t) {
      return ValueTypes.this.types.isAssignable(t, ValueTypes.this.collectionType);
    }

    boolean isMap(final @NonNull DeclaredType t) {
      return ValueTypes.this.types.isAssignable(t, ValueTypes.this.mapType);
    }

    @Override
    public @NonNull String visitError(
        final @NonNull ErrorType t,
        final @NonNull ValueSerializationContext ctx) {
      throw new IllegalQueriesDefinition(ctx.root, "unserializable type");
    }

    @Override
    public @NonNull String visitTypeVariable(
        final @NonNull TypeVariable t,
        final @NonNull ValueSerializationContext ctx) {
      throw new IllegalQueriesDefinition(ctx.root, "unserializable type");
    }

    @Override
    public @NonNull String visitWildcard(
        final @NonNull WildcardType t,
        final @NonNull ValueSerializationContext ctx) {
      throw new IllegalQueriesDefinition(ctx.root, "unserializable type");
    }

    @Override
    public @NonNull String visitExecutable(
        final @NonNull ExecutableType t,
        final @NonNull ValueSerializationContext ctx) {
      throw new IllegalQueriesDefinition(ctx.root, "unserializable type");
    }

    @Override
    public @NonNull String visitNoType(
        final @NonNull NoType t,
        final @NonNull ValueSerializationContext ctx) {
      throw new IllegalQueriesDefinition(ctx.root, "unserializable type");
    }

    @Override
    public @NonNull String visitUnknown(
        final @NonNull TypeMirror t,
        final @NonNull ValueSerializationContext ctx) {
      throw new IllegalQueriesDefinition(ctx.root, "unserializable type");
    }

    @Override
    public @NonNull String visitUnion(
        final @NonNull UnionType t,
        final @NonNull ValueSerializationContext ctx) {
      throw new IllegalQueriesDefinition(ctx.root, "unserializable type");
    }

    @Override
    public @NonNull String visitIntersection(
        final @NonNull IntersectionType t,
        final @NonNull ValueSerializationContext ctx) {
      throw new IllegalQueriesDefinition(ctx.root, "unserializable type");
    }
  }
}
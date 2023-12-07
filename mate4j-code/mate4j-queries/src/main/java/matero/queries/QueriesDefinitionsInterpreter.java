package matero.queries;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import java.util.List;
import java.util.TreeSet;

final class QueriesDefinitionsInterpreter {
  private final @NonNull ProcessingEnvironment processingEnv;
  private final @NonNull List<@NonNull QueriesAnnotatedInterface> queries;
  private final @NonNull List<@NonNull QueryMethod> methods;
  private final @NonNull TreeSet<@NonNull String> imports;
  public QueriesDefinitionsInterpreter(final @NonNull ProcessingEnvironment processingEnv) {
    this(processingEnv, new java.util.ArrayList<>(), new java.util.ArrayList<>(), new TreeSet<>());
  }

  QueriesDefinitionsInterpreter(
      final @NonNull ProcessingEnvironment processingEnv,
      final @NonNull List<@NonNull QueriesAnnotatedInterface> queries,
      final @NonNull List<@NonNull QueryMethod> methods,
      final @NonNull TreeSet<@NonNull String> imports) {
    this.processingEnv = processingEnv;
    this.queries = queries;
    this.methods = methods;
    this.imports = imports;
  }

  void interpretQueriesAt(final @NonNull Element spec) {
    if (spec.getKind() != ElementKind.INTERFACE) {
      throw new IllegalQueriesDefinition(spec, "only root interfaces allowed to be annotated with @" + Queries.class.getCanonicalName());
    }
    if (spec.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
      throw new IllegalQueriesDefinition(spec, "only root interfaces allowed to be annotated with @" + Queries.class.getCanonicalName());
    }

    final var specType = (TypeElement) spec;

    init();

    for (final var enclosed : spec.getEnclosedElements()) {
      if (enclosed.getKind() == ElementKind.METHOD) {
        final var method = (ExecutableElement) enclosed;
        if (isInstanceMethod(method)) {
          this.methods.add(parseMethod(method));
        }
      }
    }

    this.queries.add(new QueriesAnnotatedInterface(specType, this.methods, this.imports));
  }

  private void init() {
    initImports();
    initMethods();
  }

  private void initImports() {
    this.imports.clear();
    this.imports.add(CurrentSession.class.getCanonicalName());
    this.imports.add(Nullable.class.getCanonicalName());
    this.imports.add(NonNull.class.getCanonicalName());
  }

  private void initMethods() {
    this.methods.clear();
  }

  boolean isInstanceMethod(final @NonNull ExecutableElement method) {
    return !method.getModifiers().contains(Modifier.STATIC);
  }

  QueryMethod parseMethod(final @NonNull ExecutableElement method) {
    if (method.isDefault()) {
      throw new IllegalQueriesDefinition(method, "methods with default implementation are not allowed");
    }

    if (!method.getTypeParameters().isEmpty()) {
      throw new IllegalQueriesDefinition(method, "generic methods are not allowed");
    }

    final var match = method.getAnnotation(MATCH.class);
    if (match != null) {
      ImportsParser.INSTANCE.visitMethod(method, this.imports);
      return new QueryMethod(
          method.getSimpleName(),
          method.getReturnType(),
          method.getParameters(),
          method.getThrownTypes(),
          match.value(),
          true);
    } else {
      throw new IllegalQueriesDefinition(method, "only cypher query methods can be defined as instance methods");
    }
  }

  public @NonNull List<@NonNull QueriesAnnotatedInterface> queries() {
    return java.util.Collections.unmodifiableList(this.queries);
  }
}

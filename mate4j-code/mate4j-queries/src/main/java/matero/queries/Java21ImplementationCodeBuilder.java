package matero.queries;

import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;

final class Java21ImplementationCodeBuilder implements ImplementationCodeBuilder {
  private final @NonNull String date;
  private final @NonNull RepresentType representType;
  private final @NonNull ValueTypes valueTypes;
  private final @NonNull ResultProcessor resultProcessor;

  private final @NonNull Function<@NonNull TypeMirror, @NonNull Boolean> isVoidWrapper;
  Java21ImplementationCodeBuilder(final @NonNull ProcessingEnvironment processingEnv) {
    this(LocalDateTime.now(), processingEnv);
  }

  Java21ImplementationCodeBuilder(
      final @NonNull LocalDateTime date,
      final @NonNull ProcessingEnvironment processingEnv) {
    this(date.format(DateTimeFormatter.ISO_DATE_TIME), processingEnv);
  }

  Java21ImplementationCodeBuilder(
      final @NonNull String date,
      final @NonNull ProcessingEnvironment processingEnv) {
    this.date = date;
    final var types = processingEnv.getTypeUtils();
    this.representType = new RepresentType(types);
    this.valueTypes = new ValueTypes(processingEnv);
    final var voidWrapper = processingEnv.getElementUtils().getTypeElement(Void.class.getCanonicalName()).asType();
    this.isVoidWrapper = (t) -> types.isSameType(t, voidWrapper);
    this.resultProcessor = new ResultProcessor();
  }

  @Override
  public @NonNull String getImplementationCodeFor(final @NonNull QueriesAnnotatedInterface queries) {
    return STR. """
        package \{ queries.packageName() };

        \{ queries.imports().stream().map(it -> "import " + it + ';').collect(joining("\n")) }

        @javax.annotation.processing.Generated(
          value="\{ QueriesProcessor.class.getCanonicalName() }",
          date="\{ this.date }",
          comments="code generated for java 21")
        final class \{ queries.interfaceName() }Java21Impl implements \{ queries.interfaceName() } {

          \{ queries.methods().stream().map(this::buildMethodCodeFor).collect(joining("\n")) }
        }

        """ ;
  }

  @NonNull String buildMethodCodeFor(final @NonNull QueryMethod method) {
    return STR. """
        \{ annotationsOf(method) } public \{ returnTypeOf(method) } \{ method.name() }(\{ parametersOf(method) }) \{ throwsOf(method) } {
          final var __queryParameters = Map.<@NonNull String, @Nullable Object>of(\{ queryParametersOf(method) });

          return CurrentSession.get()
            .\{ executionOf(method) }(tx -> {
              final var result = tx.run(\{ cypherOf(method) }, __queryParameters);
              \{ processResultOf(method) }
            });
        }
        """ ;
  }

  @NonNull String annotationsOf(final @NonNull QueryMethod method) {
    return "@Override ";
  }

  @NonNull String returnTypeOf(final @NonNull QueryMethod method) {
    return method.returnType().toString();
  }

  @NonNull String parametersOf(final @NonNull QueryMethod method) {
    if (method.parameters().isEmpty()) {
      return "";
    } else {
      final var sb = new StringBuilder();
      final var arg = method.parameters().iterator();
      appendArgument(sb, arg.next());
      while (arg.hasNext()) {
        sb.append(", ");
        appendArgument(sb, arg.next());
      }
      return sb.toString();
    }
  }

  void appendArgument(
      final @NonNull StringBuilder sb,
      final @NonNull VariableElement parameter) {
    sb.append("final ");
    parameter.asType().accept(this.representType, sb);
    sb.append(' ').append(parameter.getSimpleName());
  }

  @NonNull String throwsOf(final @NonNull QueryMethod method) {
    if (method.thrownTypes().isEmpty()) {
      return "";
    } else {
      final var sb = new StringBuilder().append("throws ");
      final var exceptionType = method.thrownTypes().iterator();
      appendException(sb, exceptionType.next());
      while (exceptionType.hasNext()) {
        sb.append(", ");
        appendException(sb, exceptionType.next());
      }
      return sb.toString();
    }
  }

  private void appendException(
      final @NonNull StringBuilder sb,
      final @NonNull TypeMirror exceptionType) {
    final var type = (DeclaredType) exceptionType;
    sb.append(type.asElement().getSimpleName());
  }

  @NonNull String queryParametersOf(final @NonNull QueryMethod method) {
    if (method.parameters().isEmpty()) {
      return "";
    } else {
      final var sb = new StringBuilder();
      final var parameter = method.parameters().iterator();
      appendQueryParameter(parameter.next(), sb);
      while (parameter.hasNext()) {
        sb.append(", ");
        appendQueryParameter(parameter.next(), sb);
      }
      return sb.toString();
    }
  }

  void appendQueryParameter(
      final @NonNull VariableElement parameter,
      final @NonNull StringBuilder sb) {
    sb.append('"').append(aliasOf(parameter)).append("\", ").append(this.valueTypes.getSerializerFor(parameter));
  }

  @NonNull String aliasOf(final @NonNull Element parameter) {
    final var alias = parameter.getAnnotation(Alias.class);
    if (alias == null) {
      return parameter.getSimpleName().toString();
    } else {
      return alias.value();
    }
  }

  private Object executionOf(final @NonNull QueryMethod method) {
    if (method.read()) {
      return "executeRead";
    } else {
      return "executeWrite";
    }
  }

  @NonNull String cypherOf(final @NonNull QueryMethod method) {
    final var cypher = method.cypher().trim();

    if (cypher.contains("\n")) {
      return "\"\"\"\n" + cypher + "\"\"\"";
    } else {
      return '"' + cypher + '"';
    }
  }

  @NonNull String processResultOf(final @NonNull QueryMethod method) {
    if (isVoid(method)) {
      return "";
    } else if (this.isVoidWrapper.apply(method.returnType())) {
      return "return null;";
    } else {
      return "return null; // TBD";
    }
  }

  boolean isVoid(final @NonNull QueryMethod method) {
    return method.returnType().getKind() == TypeKind.VOID;
  }
}

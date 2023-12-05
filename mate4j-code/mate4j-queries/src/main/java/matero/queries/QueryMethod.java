package matero.queries;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.neo4j.driver.Query;

import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;

record QueryMethod(
    @NonNull Name name,
    @NonNull TypeMirror returnType,
    @NonNull List<@NonNull ? extends VariableElement> parameters,
    @NonNull List<@NonNull ? extends TypeMirror> thrownTypes,
    @NonNull String cypher,

    boolean read) {

  public @NonNull String toString() {
    return STR. """
        @Override public \{ this.returnType.accept(RepresentType.INSTANCE, new StringBuilder()) } \{ this.name }(\{ parametersDeclaration() }) \{ throwsDeclaration() } {
          final var __query_parameters = Map.<@NonNull String, @Nullable Object>of(\{ queryParameters() });

          return CurrentSession.get()
            .\{ executeMethod() }(tx -> {
              final var result = tx.run(\{ cypherQuery() }, __query_parameters);
              \{ processResult() }
            });
        }
        """ ;
  }

  private String processResult() {

    return """
        final var row = result.next(); // returns a primitive -> assume it has a value
        final var value = row.get(0); // it is a primitive -> get first (supodsely only) column on result
        return value.asBoolean(false); // translate, if null use default primitive value""";
  }

  private String cypherQuery() {
    if (this.cypher.contains("\n")) {
      return "\"\"\"\n" + this.cypher + "\"\"\"";
    } else {
      return '"' + this.cypher + '"';
    }
  }

  private String executeMethod() {
    if (this.read) {
      return "executeRead";
    } else {
      return "executeWrite";
    }
  }

  String parametersDeclaration() {
    if (this.parameters.isEmpty()) {
      return "";
    } else {
      final var sb = new StringBuilder();
      final var arg = this.parameters.iterator();
      appendArgument(sb, arg.next());
      while (arg.hasNext()) {
        sb.append(", ");
        appendArgument(sb, arg.next());
      }
      return sb.toString();
    }
  }

  private void appendArgument(
      final @NonNull StringBuilder sb,
      final @NonNull VariableElement parameter) {
    RepresentType.INSTANCE.visit(parameter.asType(), sb.append("final "));
    sb.append(' ').append(parameter.getSimpleName());
  }

  private String throwsDeclaration() {
    if (this.thrownTypes.isEmpty()) {
      return "";
    } else {
      final var sb = new StringBuilder().append("throws ");
      final var exceptionType = this.thrownTypes.iterator();
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

  private String queryParameters() {
    if (this.parameters.isEmpty()) {
      return "";
    } else {
      final var sb = new StringBuilder();
      final var parameter = this.parameters.iterator();
      appendQueryParameter(parameter.next(), sb);
      while (parameter.hasNext()) {
        appendQueryParameter(parameter.next(), sb.append(", "));
      }
      return sb.toString();
    }
  }

  private void appendQueryParameter(
      final @NonNull VariableElement parameter,
      final @NonNull StringBuilder sb) {
    final var parameterName = getAlias(parameter);
    sb.append('"').append(parameterName).append("\", ").append(parameterName);
  }

  private @NonNull String getAlias(final @NonNull VariableElement parameter) {
    final var alias = parameter.getAnnotation(Alias.class);
    if (alias == null) {
      return parameter.getSimpleName().toString();
    } else {
      return alias.value();
    }
  }
}

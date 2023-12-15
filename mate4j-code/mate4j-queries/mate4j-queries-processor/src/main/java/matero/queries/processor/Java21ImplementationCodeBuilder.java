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

import matero.queries.Alias;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

final class Java21ImplementationCodeBuilder implements ImplementationCodeBuilder {
  private final @NonNull String date;
  private final @NonNull ValueTypes valueTypes;
  private final @NonNull ResultProcessor resultProcessor;

  private final @NonNull Function<@NonNull TypeMirror, @NonNull Boolean> isVoidWrapper;
  private final @NonNull STGroup templates;
  private final @NonNull StringBuilder sb;

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
    this.valueTypes = new ValueTypes(processingEnv);
    final var voidWrapper = processingEnv.getElementUtils().getTypeElement(Void.class.getCanonicalName()).asType();
    this.isVoidWrapper = (t) -> types.isSameType(t, voidWrapper);
    this.resultProcessor = new ResultProcessor();

    this.templates = new STGroupDir("templates/java21");
    this.sb = new StringBuilder();
  }

  @Override
  public @NonNull String getImplementationCodeFor(final @NonNull QueriesAnnotatedInterface queries) {
    final var impl = templates.getInstanceOf("impl");
    impl.add("spec", asJava21Spec(queries));
    impl.add("date", this.date);
    return impl.render();
  }

  @NonNull
  String buildMethodCodeFor(final @NonNull QueryMethod method) {
    return ""; /*STR. """
        \{ annotationsOf(method) } public \{ returnTypeOf(method) } \{ method.name() }(\{ parametersOf(method) }) \{ throwsOf(method) } {
          final var __queryParameters = Map.<@NonNull String, @Nullable Object>of(\{ queryParametersOf(method) });

          return CurrentSession.get()
            .\{ method.txType().executorMethod }(tx -> {
              final var result = tx.run(\{ cypherOf(method) }, __queryParameters);
              \{ processResultOf(method) }
            });
        }
        """ ;*/
  }

  @NonNull
  String annotationsOf(final @NonNull QueryMethod method) {
    return "@Override ";
  }

  @NonNull
  String returnTypeOf(final @NonNull QueryMethod method) {
    return method.method.getReturnType().toString();
  }

  @NonNull
  String throwsOf(final @NonNull QueryMethod method) {
    if (method.method.getThrownTypes().isEmpty()) {
      return "";
    } else {
      final var sb = new StringBuilder().append("throws ");
      final var exceptionType = method.method.getThrownTypes().iterator();
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

  @NonNull
  String queryParametersOf(final @NonNull QueryMethod method) {
    if (method.method.getParameters().isEmpty()) {
      return "";
    } else {
      final var sb = new StringBuilder();
      final var parameter = method.method.getParameters().iterator();
      //appendQueryParameter(parameter.next(), sb);
      while (parameter.hasNext()) {
        sb.append(", ");
        //appendQueryParameter(parameter.next(), sb);
      }
      return sb.toString();
    }
  }

  void appendQueryParameter(
      final @NonNull VariableElement parameter,
      final @NonNull StringBuilder sb) {
    sb.append('"').append(aliasOf(parameter)).append("\", ").append(this.valueTypes.getSerializerFor(parameter));
  }

  @NonNull
  String aliasOf(final @NonNull Element parameter) {
    final var alias = parameter.getAnnotation(Alias.class);
    if (alias == null) {
      return parameter.getSimpleName().toString();
    } else {
      return alias.value();
    }
  }

  @NonNull
  String cypherOf(final @NonNull QueryMethod method) {
    final var cypher = method.cypher.trim();

    if (cypher.contains("\n")) {
      return "\"\"\"\n" + cypher + "\"\"\"";
    } else {
      return '"' + cypher + '"';
    }
  }

  @NonNull
  String processResultOf(final @NonNull QueryMethod method) {
    if (isVoid(method)) {
      return "";
    } else if (this.isVoidWrapper.apply(method.method.getReturnType())) {
      return "return null;";
    } else {
      return "return null; // TBD";
    }
  }

  boolean isVoid(final @NonNull QueryMethod method) {
    return method.method.getReturnType().getKind() == TypeKind.VOID;
  }

  @NonNull ImplSpec asJava21Spec(final @NonNull QueriesAnnotatedInterface queries) {
    return new ImplSpec(
        queries.getPackage().getQualifiedName().toString(),
        queries.imports,
        queries.target.getSimpleName().toString(),
        asMethodSpecs(queries.methods)
    );
  }

  final static class ImplSpec {
    public final @NonNull String packageName;
    public final @NonNull List<@NonNull String> imports;

    public final @NonNull String interfaceClassName;

    public final @NonNull List<@NonNull MethodSpec> queryMethods;

    ImplSpec(
        final @NonNull String packageName,
        final @NonNull List<@NonNull String> imports,
        final @NonNull String interfaceClassName,
        final @NonNull List<@NonNull MethodSpec> queryMethods) {
      this.packageName = packageName;
      this.imports = imports;
      this.interfaceClassName = interfaceClassName;
      this.queryMethods = queryMethods;
    }

    public boolean isInRootPackage() {
      return this.packageName.isEmpty();
    }

    public @NonNull String getProcessorClassName() {
      return QueriesProcessor.class.getCanonicalName();
    }

    public @NonNull String getImplClassName() {
      return this.interfaceClassName + "Java21Impl";
    }
  }


  @NonNull List<@NonNull MethodSpec> asMethodSpecs(final List<@NonNull QueryMethod> methods) {
    if (methods.isEmpty()) {
      return List.of();
    } else {
      return methods.stream()
          .map(this::asMethodSpec)
          .collect(Collectors.toList());
    }
  }

  @NonNull MethodSpec asMethodSpec(final @NonNull QueryMethod m) {
    this.sb.setLength(0);
    m.method.getReturnType().accept(RepresentType.VISITOR, sb);
    return new MethodSpec(
        sb.toString(),
        m.method.getSimpleName().toString(),
        m.method.getParameters().stream()
            .map(this::asParameterSpec)
            .collect(Collectors.toList()),
        m.method.getThrownTypes().stream()
            .map(it -> ((DeclaredType) it).asElement().getSimpleName().toString())
            .collect(Collectors.toList())
    );
  }

  final static class MethodSpec {
    public final @NonNull String returnType;
    public final @NonNull String name;

    public final @NonNull List<@NonNull ParameterSpec> parameters;

    public final @NonNull List<@NonNull String> exceptions;

    MethodSpec(
        final @NonNull String returnType,
        final @NonNull String name,
        final @NonNull List<@NonNull ParameterSpec> parameters,
        final @NonNull List<@NonNull String> exceptions) {
      this.returnType = returnType;
      this.name = name;
      this.parameters = parameters;
      this.exceptions = exceptions;
    }

    public boolean isDeclareThrows() {return !this.exceptions.isEmpty();}
  }

  @NonNull ParameterSpec asParameterSpec(final @NonNull VariableElement parameter) {
    this.sb.setLength(0);
    parameter.asType().accept(RepresentType.VISITOR, this.sb);
    return new ParameterSpec(
        this.sb.toString(),
        parameter.getSimpleName().toString()
    );
  }

  final static class ParameterSpec {
    public final @NonNull String type;
    public final @NonNull String name;

    ParameterSpec(
        final @NonNull String type,
        final @NonNull String name) {
      this.type = type;
      this.name = name;
    }
  }
}

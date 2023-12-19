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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

final class Java21ImplementationCodeBuilder implements ImplementationCodeBuilder {
  private final @NonNull String date;
  private final @NonNull ResultProcessor resultProcessor;

  private final @NonNull Function<@NonNull TypeMirror, @NonNull Boolean> isVoidWrapper;

  private final @NonNull ParameterValueMapper parameterValueMapper;
  private final @NonNull STGroup templates;

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
    final var voidWrapper = processingEnv.getElementUtils().getTypeElement(Void.class.getCanonicalName()).asType();
    this.isVoidWrapper = (t) -> types.isSameType(t, voidWrapper);
    this.resultProcessor = new ResultProcessor();

    this.templates = new STGroupDir("templates/java21");

    final var elements = processingEnv.getElementUtils();

    this.parameterValueMapper = new ParameterValueMapper(
        types,
        elements.getTypeElement(java.util.Collection.class.getCanonicalName()),
        elements.getTypeElement(java.util.List.class.getCanonicalName()),
        elements.getTypeElement(Map.class.getCanonicalName())
    );
  }

  @Override
  public @NonNull String getImplementationCodeFor(final @NonNull QueriesAnnotatedInterface queries) {
    final var impl = templates.getInstanceOf("impl");
    impl.add("spec", asJava21Spec(queries));
    impl.add("date", this.date);
    return impl.render();
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
    final var returnType = DescribeJavaReturnType.VISITOR.visit(m.method.getReturnType()).build();
    return new MethodSpec(
        returnType,
        m.method.getSimpleName().toString(),
        m.method.getParameters().stream()
            .map(this::asParameterSpec)
            .collect(Collectors.toList()),
        m.method.getThrownTypes().stream()
            .map(it -> ((DeclaredType) it).asElement().getSimpleName().toString())
            .collect(Collectors.toList()),
        m.txType.executorMethod,
        m.cypher
    );
  }

  final static class MethodSpec {
    public final @NonNull ReturnType returnType;
    public final @NonNull String name;

    public final @NonNull List<@NonNull ParameterSpec> parameters;

    public final @NonNull List<@NonNull String> exceptions;

    public final @NonNull String executor;
    private final @NonNull String cypher;

    MethodSpec(
        final @NonNull ReturnType returnType,
        final @NonNull String name,
        final @NonNull List<@NonNull ParameterSpec> parameters,
        final @NonNull List<@NonNull String> exceptions,
        final @NonNull String executor,
        final @NonNull String cypher) {
      this.returnType = returnType;
      this.name = name;
      this.parameters = parameters;
      this.exceptions = exceptions;
      this.executor = executor;
      this.cypher = cypher;
    }

    public boolean isDeclareThrows() {
      return !this.exceptions.isEmpty();
    }

    public @NonNull String getCypher() {
      if (this.cypher.contains("\n")) {
        return "\"\"\"\n" + this.cypher + "\n\"\"\"";
      } else {
        return '"' + this.cypher + '"';
      }
    }
  }

  @NonNull ParameterSpec asParameterSpec(final @NonNull VariableElement parameter) {
    // FIXME
    final var sb = new StringBuilder();
    parameter.asType().accept(RepresentParameter.VISITOR, sb);
    return new ParameterSpec(
        sb.toString(),
        parameter.getSimpleName().toString(),
        aliasOf(parameter),
        this.parameterValueMapper.getMapperOf(parameter)
    );
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

  final static class ParameterSpec {
    public final @NonNull String type;
    public final @NonNull String name;

    public final @NonNull String alias;

    public final @NonNull String value;

    ParameterSpec(
        final @NonNull String type,
        final @NonNull String name,
        final @NonNull String alias,
        final @NonNull String value) {
      this.type = type;
      this.name = name;
      this.alias = alias;
      this.value = value;
    }
  }
}

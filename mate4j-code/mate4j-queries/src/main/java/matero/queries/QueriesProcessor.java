package matero.queries;

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

import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class QueriesProcessor extends AbstractProcessor {

  @Override
  public @NonNull Set<@NonNull String> getSupportedAnnotationTypes() {
    return Set.of(Queries.class.getCanonicalName(), MATCH.class.getCanonicalName());
  }

  @Override
  public boolean process(
      final @NonNull Set<@NonNull ? extends TypeElement> annotations,
      final @NonNull RoundEnvironment roundEnv) {
    final var specifications = roundEnv.getElementsAnnotatedWith(Queries.class);
    final var contracts = new java.util.ArrayList<@NonNull QueriesAnnotatedInterface>();
    for (final var spec : specifications) {
      try {
        contracts.add(buildContractFrom(spec));
      } catch (final IllegalQueriesDefinition failure) {
        error(failure.element, failure.getMessage());
      }
    }

    if (!contracts.isEmpty()) {
      System.out.println(contracts);
    }

    return true;
  }

  @NonNull QueriesAnnotatedInterface buildContractFrom(final @NonNull Element spec) {
    if (spec.getKind() != ElementKind.INTERFACE) {
      throw new IllegalQueriesDefinition(spec, "only root interfaces allowed to be annotated with @" + Queries.class.getCanonicalName());
    }
    if (spec.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
      throw new IllegalQueriesDefinition(spec, "only root interfaces allowed to be annotated with @" + Queries.class.getCanonicalName());
    }

    final var specType = (TypeElement) spec;
    final var q = new QueriesAnnotatedInterface(specType);

    for (final var enclosed : spec.getEnclosedElements()) {
      if (enclosed.getKind() == ElementKind.METHOD) {
        final var method = (ExecutableElement) enclosed;

        if (!method.getModifiers().contains(Modifier.STATIC)) {
          q.registerMatchMethod(method);
        }
      }
    }
    return q;
  }

  void error(
      final @NonNull Element e,
      final @Nullable CharSequence message) {
    final var errorMessage = message == null ? "" : message;
    messager().printMessage(Diagnostic.Kind.ERROR, errorMessage, e);
  }

  @NonNull Messager messager() {
    return this.processingEnv.getMessager();
  }

  public class MethodPrintScanner extends TreePathScanner<Void, Trees> {
    @Override
    public Void visitMethod(final MethodTree methodTree, Trees o) {
      System.out.println(methodTree);
      return null;
    }
  }
}

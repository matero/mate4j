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

import matero.queries.MATCH;
import matero.queries.Queries;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.List;
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
    processSpecifications(roundEnv.getElementsAnnotatedWith(Queries.class));
    return false;
  }

  private void processSpecifications(final @NonNull Set<@NonNull ? extends Element> specifications) {
    if (!specifications.isEmpty()) {
      generateCodeFor(queriesAt(specifications));
    }
  }

  private @NonNull List<@NonNull QueriesAnnotatedInterface> queriesAt(final @NonNull Set<@NonNull ? extends Element> specifications) {
    final var queriesParser = new QueriesDefinitionsParser(this.processingEnv);

    for (final var spec : specifications) {
      try {
        queriesParser.parseQueriesAt(spec);
      } catch (final IllegalQueriesDefinition failure) {
        error(failure.element, failure.getMessage());
      }
    }

    return queriesParser.queries();
  }

  private void generateCodeFor(final @NonNull List<@NonNull QueriesAnnotatedInterface> queries) {
    if (!queries.isEmpty()) {
      final var codeBuilder = new Java21ImplementationCodeBuilder(this.processingEnv);
      for (final var q : queries) {
        System.out.println(codeBuilder.getImplementationCodeFor(q));
      }
    }
  }

  void error(
      final @NonNull Element e,
      final @Nullable CharSequence message) {
    final var errorMessage = message == null ? "error found" : message;
    messager().printMessage(Diagnostic.Kind.ERROR, errorMessage, e);
  }

  @NonNull
  Messager messager() {
    return this.processingEnv.getMessager();
  }
}

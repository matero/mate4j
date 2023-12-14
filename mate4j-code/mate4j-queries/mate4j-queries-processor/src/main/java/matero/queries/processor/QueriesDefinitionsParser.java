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

import matero.queries.Queries;
import matero.queries.Query;
import matero.queries.QueryType;
import matero.queries.TransactionType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import java.util.List;

final class QueriesDefinitionsParser {
  private final @NonNull List<@NonNull QueriesAnnotatedInterface> queries;
  private final @NonNull List<@NonNull QueryMethod> methods;
  private final @NonNull ImportsParser importsParser;

  private final @NonNull StringBuilder sb = new StringBuilder();

  QueriesDefinitionsParser() {
    this(new java.util.ArrayList<>(), new java.util.ArrayList<>(), new ImportsParser());
  }

  QueriesDefinitionsParser(
      final @NonNull List<@NonNull QueriesAnnotatedInterface> queries,
      final @NonNull List<@NonNull QueryMethod> methods,
      final @NonNull ImportsParser importsParser) {
    this.queries = queries;
    this.methods = methods;
    this.importsParser = importsParser;
  }

  void parseQueriesAt(final @NonNull Element spec) {
    ensureThatIsInterface(spec);
    ensureThatIsRootElement(spec);

    prepareParsing();

    parseRootInterfaceAt((@NonNull TypeElement) spec);
  }

  void ensureThatIsInterface(final @NonNull Element spec) {
    if (spec.getKind() != ElementKind.INTERFACE) {
      throw new IllegalQueriesDefinition(spec, "only root interfaces allowed to be annotated with @" + Queries.class.getCanonicalName());
    }
  }

  void ensureThatIsRootElement(final @NonNull Element spec) {
    if (spec.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
      throw new IllegalQueriesDefinition(spec, "only root interfaces allowed to be annotated with @" + Queries.class.getCanonicalName());
    }
  }

  private void prepareParsing() {
    prepareImports();
    prepareMethods();
  }

  private void prepareImports() {
    this.importsParser.prepare();
  }

  private void prepareMethods() {
    this.methods.clear();
  }

  void parseRootInterfaceAt(final @NonNull TypeElement spec) {
    for (final var enclosed : spec.getEnclosedElements()) {
      parseInterfaceElement(enclosed);
    }
    this.queries.add(new QueriesAnnotatedInterface(spec, this.methods, this.importsParser.getImports()));
  }

  void parseInterfaceElement(final @NonNull Element e) {
    if (e.getKind() == ElementKind.METHOD) {
      parseInterfaceMethod((@NonNull ExecutableElement) e);
    }
  }

  void parseInterfaceMethod(final @NonNull ExecutableElement method) {
    final var queryMethod = parseMethod(method);
    if (queryMethod != null) {
      this.methods.add(queryMethod);
    }
  }

  @Nullable QueryMethod parseMethod(final @NonNull ExecutableElement method) {
    final var query = getQuery(method);
    if (query != null) {
      ensureThat(method)
          .isNotStatic()
          .isNotGeneric()
          .doesNotHaveDefaultImplementation();

      final var cypher = getQueryCypher(method, query);
      final var queryType = getQueryType(method, query, cypher);
      final var txType = getTransactionType(method, query, queryType);

      this.importsParser.parse(method);

      return new QueryMethod(
          method,
          cypher,
          queryType,
          txType);
    }

    return null; // method is not annotated -> it does not require to be registered
  }

  @NonNull TransactionType getTransactionType(
      final @NonNull ExecutableElement method,
      final @NonNull Query query,
      final @NonNull QueryType queryType) {
    final var txType = query.txType();
    if (txType == TransactionType.UNKNOWN) {
      return queryType.defaultTransactionType;
    } else {
      return txType;
    }
  }

  private static @Nullable Query getQuery(final @NonNull ExecutableElement method) {
    final var query = method.getAnnotation(Query.class);
    if (query == null) {
      return null;
    }
    final var undefinedValue = Query.is.undefined(query.value());
    final var undefinedCypher = Query.is.undefined(query.cypher());
    if (undefinedValue && undefinedCypher) {
      throw new IllegalQueriesDefinition(method, "@" + Query.class.getCanonicalName() + " must have one of value() or cypher() configured, but none of them are");
    }
    if (!undefinedValue && !undefinedCypher) {
      throw new IllegalQueriesDefinition(method, "@" + Query.class.getCanonicalName() + " must have one of value() or cypher() configured, but both of them are");
    }
    return query;
  }

  @NonNull String getQueryCypher(
      final @NonNull ExecutableElement method,
      final @NonNull Query query) {
    if (Query.is.undefined(query.value())) {
      return validateCypher(method, query.cypher(), "cypher");
    } else {
      return validateCypher(method, query.value(), "value");
    }
  }

  @NonNull String validateCypher(
      final @NonNull ExecutableElement method,
      final @NonNull String cypher,
      final @NonNull String property) {

    if (cypher.isEmpty()) {
      throw new IllegalQueriesDefinition(method, "@" + Query.class.getCanonicalName() + " can not have empty " + property);
    }
    if (cypher.isBlank()) {
      throw new IllegalQueriesDefinition(method, "@" + Query.class.getCanonicalName() + " can not have blank " + property);
    }
    return cypher.trim();
  }

  @NonNull QueryType getQueryType(
      final @NonNull ExecutableElement method,
      final @NonNull Query query,
      final @NonNull String cypher) {
    final var type = query.queryType();
    if (type == QueryType.UNKNOWN) {
      return switch (cypher.charAt(0)) {
        case 'C', 'c' -> switch (cypher.charAt(1)) {
          case 'A', 'a' -> QueryType.CALL;
          case 'R', 'r' -> QueryType.CREATE;
          default -> throw new IllegalQueriesDefinition(method, "can not deduct type of statemente for '" + cypher + "'.");
        };
        case 'D', 'd' -> QueryType.DELETE;
        case 'M', 'm' -> switch (cypher.charAt(1)) {
          case 'A', 'a' -> QueryType.MATCH;
          case 'E', 'e' -> QueryType.MERGE;
          default -> throw new IllegalQueriesDefinition(method, "can not deduct type of statemente for '" + cypher + "'.");
        };
        case 'O', 'o' -> QueryType.OPTIONAL_MATCH;
        default -> throw new IllegalQueriesDefinition(method, "can not deduct type of statemente for '" + cypher + "'.");
      };
    } else {
      return type;
    }
  }

  public @NonNull List<@NonNull QueriesAnnotatedInterface> queries() {
    return java.util.Collections.unmodifiableList(this.queries);
  }

  @NonNull QueryMethodPreconditions ensureThat(final @NonNull ExecutableElement method) {
    return new QueryMethodPreconditions(method);
  }

  static final class QueryMethodPreconditions {
    private final @NonNull ExecutableElement method;

    QueryMethodPreconditions(final @NonNull ExecutableElement method) {
      this.method = method;
    }


    @NonNull QueryMethodPreconditions isNotStatic() {
      if (this.method.getModifiers().contains(Modifier.STATIC)) {
        throw invalidQueryMethod("static methods are not allowed");
      }
      return this;
    }

    @NonNull QueryMethodPreconditions isNotGeneric() {
      if (!this.method.getTypeParameters().isEmpty()) {
        throw invalidQueryMethod("generic methods are not allowed");
      }
      return this;
    }

    @NonNull QueryMethodPreconditions doesNotHaveDefaultImplementation() {
      if (this.method.isDefault()) {
        throw invalidQueryMethod("methods with default implementation are not allowed");
      }
      return this;
    }

    @NonNull IllegalQueriesDefinition invalidQueryMethod(final @NonNull String message) {
      return new IllegalQueriesDefinition(this.method, message);
    }
  }
}

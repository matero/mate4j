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

import com.google.testing.compile.JavaFileObjects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class QueriesProcessorTest {
  @Test
  void non_annotated_class_should_be_compilable() {
    final var compilation = javac()
        .withProcessors(new QueriesProcessor())
        .compile(JavaFileObjects.forSourceString("HelloWorld", "final class HelloWorld {}"));
    assertThat(compilation)
        .succeeded();
  }

  @Test
  void annotated_class_should_not_be_compilable() {
    final var compilation = javac()
        .withProcessors(new QueriesProcessor())
        .compile(JavaFileObjects.forSourceString("HelloWorld", "@matero.queries.Queries class HelloWorld {}"));
    assertThat(compilation)
        .hadErrorCount(1);
    assertThat(compilation)
        .hadErrorContaining("only root interfaces allowed to be annotated with @matero.queries.Queries");
  }

  @Test
  void annotated_interface_should_be_compilable() {
    final var compilation = javac()
        .withProcessors(new QueriesProcessor())
        .compile(JavaFileObjects.forSourceString("HelloWorld", "@matero.queries.Queries interface HelloWorld {}"));
    assertThat(compilation)
        .succeeded();
  }

  @Test
  void annotated_interface_with_MATCH_method_over_default_types_should_be_compilable() {
    final var compilation = javac()
        .withProcessors(new QueriesProcessor())
        .compile(JavaFileObjects.forSourceString("sample.queries.Players", """
            package sample.queries;
                                               
            import org.checkerframework.checker.nullness.qual.NonNull;
            import org.checkerframework.checker.nullness.qual.Nullable;
            import org.checkerframework.checker.index.qual.Positive;

            import matero.queries.Queries;
            import matero.queries.Query;
            import matero.queries.Alias;
                        
            import java.util.List;
            import java.util.Set;
            import java.util.concurrent.atomic.AtomicBoolean;
                        
            import org.neo4j.exceptions.EntityNotFoundException;
                                                             
            @Queries
            public interface Players {
              @Query("MATCH (p:Player {id: ${playerId}) RETURN p IS NOT NULL")
              boolean existsPlayerWithId(long id)
                  throws EntityNotFoundException;
            }"""));
    assertThat(compilation)
        .succeeded();
  }
}

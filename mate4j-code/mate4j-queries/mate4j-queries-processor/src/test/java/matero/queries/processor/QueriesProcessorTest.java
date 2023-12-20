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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.neo4j.driver.types.*;

import java.time.*;
import java.util.List;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class QueriesProcessorTest {

  private static final LocalDateTime PROCESSING_DATETIME = LocalDateTime.of(2008, Month.MARCH, 20, 8, 30, 0);

  @Test
  void non_annotated_class_should_be_compilable() {
    final var compilation = javac()
        .withProcessors(new QueriesProcessor(PROCESSING_DATETIME))
        .compile(JavaFileObjects.forSourceString("HelloWorld", "final class HelloWorld {}"));
    assertThat(compilation)
        .succeeded();
  }

  @Test
  void annotated_class_should_not_be_compilable() {
    final var compilation = javac()
        .withProcessors(new QueriesProcessor(PROCESSING_DATETIME))
        .compile(JavaFileObjects.forSourceString("HelloWorld", "@matero.queries.Queries class HelloWorld {}"));
    assertThat(compilation)
        .hadErrorCount(1);
    assertThat(compilation)
        .hadErrorContaining("only root interfaces allowed to be annotated with @matero.queries.Queries");
  }

  @Test
  void annotated_interface_should_be_compilable() {
    final var compilation = javac()
        .withProcessors(new QueriesProcessor(PROCESSING_DATETIME))
        .compile(JavaFileObjects.forSourceString("HelloWorld", "@matero.queries.Queries interface HelloWorld {}"));
    assertThat(compilation)
        .succeeded();
  }

  @ParameterizedTest
  @ValueSource(classes = {boolean.class, char.class, byte.class, short.class, int.class, long.class, float.class, double.class})
  void query_method_returning_primitive_should_be_compilable(final @NonNull Class<?> primitiveType) {
    final var compiler = javac()
        .withProcessors(new QueriesProcessor(PROCESSING_DATETIME));
    final var compilation = compiler
        .compile(JavaFileObjects.forSourceString("sample.queries.Players", queryWithTypeResult(primitiveType.getTypeName())));
    assertThat(compilation)
        .succeeded();
  }

  @ParameterizedTest
  @ValueSource(classes = {Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class})
  void query_method_returning_primitive_wrapper_should_be_compilable(final @NonNull Class<?> primitiveWrapperClass) {
    final var compiler = javac()
        .withProcessors(new QueriesProcessor(PROCESSING_DATETIME));
    final var compilation = compiler
        .compile(JavaFileObjects.forSourceString("sample.queries.Players", queryWithTypeResult(primitiveWrapperClass.getSimpleName())));
    assertThat(compilation)
        .succeeded();
  }

  @NonNull String queryWithTypeResult(final @NonNull String typeName) {
    return """
        package sample.queries;

        import matero.queries.*;

        @Queries
        public interface Players {
          @Query("MATCH (n:Player {id:$id}) RETURN n.""".strip() + typeName + "Prop\")" + typeName + " get_" + typeName + "(long id);\n}";
  }

  @ParameterizedTest
  @ValueSource(classes = {void.class, Void.class})
  void void_query_method_should_be_compilable(final @NonNull Class<?> voidClass) {
    final var compiler = javac()
        .withProcessors(new QueriesProcessor(PROCESSING_DATETIME));
    final var compilation = compiler
        .compile(JavaFileObjects.forSourceString("sample.queries.Players", queryWithVoidResult(voidClass.getSimpleName())));
    assertThat(compilation)
        .succeeded();
  }

  @NonNull String queryWithVoidResult(final @NonNull String typeName) {
    return """
        package sample.queries;

        import matero.queries.*;

        @Queries
        public interface Players {
          @Query("MATCH (n:Player {id:$id}) DETACH DELETE n\") """ + typeName + " delete(long id);\n}";
  }


  @ParameterizedTest
  @ValueSource(classes = {byte[].class, Object.class, String.class, Entity.class, Node.class, Relationship.class, Path.class, IsoDuration.class,
      Point.class, LocalDate.class, LocalDateTime.class, OffsetTime.class, OffsetDateTime.class, LocalTime.class, ZonedDateTime.class})
  void query_method_returning_supported_class_reference_should_be_compilable(final @NonNull Class<?> supportedClass) {
    final var qname = supportedClass.getCanonicalName();

    final var compilation = javac()
        .withProcessors(new QueriesProcessor())
        .compile(JavaFileObjects.forSourceString("sample.queries.Players", """
            package sample.queries;
                        
            import matero.queries.*;
                
            @Queries
            public interface Players {
              @Query("MATCH (n {id:$id}) RETURN n.property") """ + qname + """
              get(long id);
            }"""));
    assertThat(compilation)
        .succeeded();
  }

  @ParameterizedTest
  @ValueSource(strings = {/*raw list*/"", "Object", /*list of any*/"?"})
  void query_method_returning_list_of_Objects_should_be_compilable(final @NonNull String componentType) {
    final var component = componentType.isEmpty() ? "" : ('<' + componentType + '>');
    final var listType = List.class.getCanonicalName() + component;
    final var compilation = javac()
        .withProcessors(new QueriesProcessor())
        .compile(JavaFileObjects.forSourceString("sample.queries.Players", """
            package sample.queries;
                                               
            import matero.queries.*;
                                                             
            @Queries
            public interface Players {
              @Query("MATCH (n) RETURN n.prop") """ + listType + " get();\n}"));
    assertThat(compilation)
        .succeeded();
  }

  @ParameterizedTest
  @ValueSource(classes = {
      Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, String.class, Entity.class,
      Node.class, Relationship.class, Path.class, IsoDuration.class, Point.class, LocalDate.class, LocalDateTime.class, OffsetTime.class,
      OffsetDateTime.class, LocalTime.class, ZonedDateTime.class})
  void query_method_returning_list_of_supported_scalar_types_should_be_compilable(final @NonNull Class<?> componentType) {
    final var listType = List.class.getCanonicalName() + '<' + componentType.getCanonicalName() + '>';
    final var compilation = javac()
        .withProcessors(new QueriesProcessor())
        .compile(JavaFileObjects.forSourceString("sample.queries.Players", """
            package sample.queries;
                                               
            import matero.queries.*;
                                                             
            @Queries
            public interface Players {
              @Query("MATCH (n) RETURN n.prop") """ + listType + " get();\n}"));
    assertThat(compilation)
        .succeeded();
  }

  @Test
  void query_method_returning_list_of_byte_array_should_be_compilable() {
    final var listType = List.class.getCanonicalName() + "<byte[]>";
    final var compilation = javac()
        .withProcessors(new QueriesProcessor())
        .compile(JavaFileObjects.forSourceString("sample.queries.Players", """
            package sample.queries;
                                               
            import matero.queries.*;
                                                             
            @Queries
            public interface Players {
              @Query("MATCH (n) RETURN n.prop") """ + listType + " get();\n}"));
    assertThat(compilation)
        .succeeded();
  }

  @Test
  void query_method_returning_list_of_lists_should_NOT_be_compilable() {
    final var listType = List.class.getCanonicalName() + "<" + List.class.getCanonicalName() + "<Boolean>>";
    final var compilation = javac()
        .withProcessors(new QueriesProcessor())
        .compile(JavaFileObjects.forSourceString("sample.queries.Players", """
            package sample.queries;
                                               
            import matero.queries.*;
                                                             
            @Queries
            public interface Players {
              @Query("MATCH (n) RETURN n.prop") """ + listType + " get();\n}"));
    assertThat(compilation)
        .hadErrorCount(1);
    assertThat(compilation)
        .hadErrorContaining("List of List are not supported");
  }
}

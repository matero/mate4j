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
import matero.queries.Query;
import matero.queries.TransactionType;
import matero.queries.neo4j.CurrentSession;
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
                                               
            import org.checkerframework.checker.nullness.qual.*;
            import matero.queries.*;
            import java.util.*;
            import java.time.*;
            import java.util.concurrent.atomic.AtomicBoolean;
    
            import org.neo4j.exceptions.EntityNotFoundException;
            import org.neo4j.driver.types.*;
                                                             
            @Queries
            public interface Players {
              @Query("MATCH (n {id:$id}) RETURN n.booleanProp") boolean get_boolean(long id);
              @Query("MATCH (n {id:$id}) RETURN n.booleanProp") Boolean get_Boolean(long id);
              @Query("MATCH (n {id:$id}) RETURN n.byteProp") byte get_byte(long id);
              @Query("MATCH (n {id:$id}) RETURN n.byteProp") Byte get_Byte(long id);
              @Query("MATCH (n {id:$id}) RETURN n.byteArrayProp") byte[] get_byteArray(long id);
              @Query("MATCH (n {id:$id}) RETURN n.charProp") char get_char(long id);
              @Query("MATCH (n {id:$id}) RETURN n.charProp") Character get_Character(long id);
              @Query("MATCH (n {id:$id}) RETURN n.doubleProp") double get_double(long id);
              @Query("MATCH (n {id:$id}) RETURN n.doubleProp") Double get_Double(long id);
              @Query("MATCH (n {id:$id}) RETURN n.floatProp") float get_float(long id);
              @Query("MATCH (n {id:$id}) RETURN n.floatProp") Float get_Float(long id);
              @Query("MATCH (n {id:$id}) RETURN n.intProp") int get_int(long id);
              @Query("MATCH (n {id:$id}) RETURN n.intProp") Integer get_Integer(long id);
              @Query("MATCH (n {id:$id}) RETURN n.longProp") long get_long(long id);
              @Query("MATCH (n {id:$id}) RETURN n.longProp") Long get_Long(long id);
              @Query("MATCH (n {id:$id}) RETURN n.shortProp") short get_short(long id);
              @Query("MATCH (n {id:$id}) RETURN n.shortProp") Short get_Short(long id);
              @Query("MATCH (n {id:$id}) RETURN n.voidProp") void get_void(long id);
              @Query("MATCH (n {id:$id}) RETURN n.voidProp") Void get_Void(long id);
              @Query("MATCH (n {id:$id}) RETURN n.prop") Object get_Object(long id);
              @Query("MATCH (n {id:$id}) RETURN n.strProp") String get_String(long id);
              @Query("MATCH (n {id:$id}) RETURN n") Entity get_Entity(long id);
              @Query("MATCH (n {id:$id}) RETURN n") Node get_Node(long id);
              @Query("MATCH (n {id:$id}) RETURN n") Relationship get_Relationship(long id);
              @Query("MATCH (n {id:$id}) RETURN n") Path get_Path(long id);
              @Query("MATCH (n {id:$id}) RETURN n") IsoDuration get_IsoDuration(long id);
              @Query("MATCH (n {id:$id}) RETURN n") Point get_Point(long id);
              @Query("MATCH (n {id:$id}) RETURN n") LocalDate get_LocalDate(long id);
              @Query("MATCH (n {id:$id}) RETURN n") LocalDateTime get_LocalDateTime(long id);
              @Query("MATCH (n {id:$id}) RETURN n") OffsetTime get_OffsetTime(long id);
              @Query("MATCH (n {id:$id}) RETURN n") LocalTime get_LocalTime(long id);
              @Query("MATCH (n {id:$id}) RETURN n") OffsetDateTime get_OffsetDateTime(long id);
              @Query("MATCH (n {id:$id}) RETURN n") ZonedDateTime get_ZonedDateTime(long id);
            }"""));
    assertThat(compilation)
        .succeeded();
  }
}

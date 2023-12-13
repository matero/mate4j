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

import matero.support.ClassNotInstantiable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
@Documented
@Inherited
public @interface Query {
  @NonNull String value() default is.UNDEFINED;

  @NonNull String cypher() default is.UNDEFINED;

  @NonNull QueryType queryType() default QueryType.UNKNOWN;

  @NonNull TransactionType txType() default TransactionType.UNKNOWN;

  final class is {
    private static final String UNDEFINED = "<__UNDEFINED__>";

    private is() {throw new ClassNotInstantiable(is.class);
    }
    public static boolean undefined(final @NonNull String s) {
      return UNDEFINED.equals(s);
    }
  }
}
package matero.fixtures;

/*-
 * #%L
 * Mate4j/Fixtures
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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

import java.lang.annotation.*;

public final class Neo4j {
    final static ScopedValue<Session> SESSION = ScopedValue.newInstance();
    final static ScopedValue<Transaction> TX = ScopedValue.newInstance();

    private Neo4j() {
        throw new ClassNotInstantiable(Neo4j.class);
    }

    public static @Nullable Session session() {
        return SESSION.get();
    }

    public static @Nullable Transaction tx() {
        return TX.get();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Documented
    @Inherited
    public @interface Fixture {
        String @NonNull [] UNDEFINED = {};

        @NonNull String @NonNull [] value() default {};
        @NonNull String @NonNull [] load() default {};

        boolean show() default false;
    }
}

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

import matero.fixtures.Neo4j.Fixture;
import matero.support.ClassNotInstantiable;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

final class ShowFixtureContents {
    private ShowFixtureContents() {
        throw new ClassNotInstantiable(ShowFixtureContents.class);
    }

    static @Nullable Boolean of(final @Nullable AnnotatedElement e) {
        if (e == null) {
            return null;
        }
        var fixtures = e.getAnnotation(Fixture.class);
        if (fixtures == null) {
            if (e instanceof Method m) {
                return of(m.getDeclaringClass());
            } else if (e instanceof Class<?> c) {
                return of(c.getDeclaringClass());
            }
            throw new IllegalArgumentException("only methods and classes are accepted, but received " + e);
        } else {
            return fixtures.show();
        }
    }
}


package matero.fixtures.configuration;

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

import matero.fixtures.Neo4jFixtures;
import matero.fixtures.Neo4j;
import matero.fixtures.Neo4jFixturesExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Neo4jFixtures(settings = SingletonInternalDbWithRollbackPerTestMethod.class)
@ExtendWith(Neo4jFixturesExtension.class)
public class SimpleTest {
    @Test
    void classFixture() {
        assert Neo4j.session() != null;
    }

    @Test
    void defaultTestFixture() {
        assert Neo4j.session() != null;
    }

    @Test
    @Neo4j.Fixture("configuredFixture")
    void configuredTestFixtureWithValue() {
        assert Neo4j.session() != null;
    }

    @Test
    @Neo4j.Fixture(load = "configuredFixture", show = true)
    void configuredTestFixtureWithLoad() {
        assert Neo4j.session() != null;
    }
}

package matero.queries.model;

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

import matero.queries.ParameterSerializer;
import matero.queries.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;

import java.util.function.Function;


public final class PlayerId {
  @ParameterSerializer
  public static final Function<@NonNull PlayerId, @NonNull Object> AS_PARAMETER = (final @NonNull PlayerId it) -> it.value;

  @Parser
  public static @NonNull PlayerId parse(final @NonNull Node node) {
    final var value = node.get("id");
    if (value == null) {
      throw new NullPointerException("Player.id");
    }
    return new PlayerId(value.asLong());
  }

  @Parser
  public static @Nullable PlayerId parse(final @NonNull Record r) {
    final var value = r.get("id");
    if (value == null) {
      return null;
    } else {
      return new PlayerId(value.asLong());
    }
  }

  final long value;

  PlayerId(final long value) {
    this.value = value;
  }


  @Override
  public boolean equals(final @Nullable Object o) {
    return (this == o) || (o instanceof PlayerId other && this.value == other.value);
  }

  @Override
  public int hashCode() {
    return Long.hashCode(this.value);
  }

  @Override
  public String toString() {
    return "PlayerId(" + this.value + ')';
  }
}

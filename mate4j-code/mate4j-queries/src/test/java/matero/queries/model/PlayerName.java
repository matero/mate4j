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


public final class PlayerName {
  @ParameterSerializer
  public static final Function<@NonNull PlayerName, @NonNull Object> AS_PARAMETER = (PlayerName it) -> it.value;

  @Parser
  public static @NonNull PlayerName parse(final @NonNull Node node) {
    final var value = node.get("name");
    if (value == null) {
      throw new NullPointerException("Player.name");
    }
    return new PlayerName(value.asString());
  }

  @Parser
  public static @Nullable PlayerName parse(final @NonNull Record r) {
    final var value = r.get("name");
    if (value == null) {
      return null;
    } else {
      return new PlayerName(value.asString());
    }
  }

  final @NonNull String value;

  PlayerName(final @NonNull String value) {
    this.value = value;
  }


  @Override
  public boolean equals(final @Nullable Object o) {
    return (this == o) || (o instanceof PlayerName other && this.value.equals(other.value));
  }

  @Override
  public int hashCode() {
    return this.value.hashCode();
  }

  @Override
  public String toString() {
    return "PlayerName('" + this.value + "')";
  }
}

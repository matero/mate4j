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

import matero.queries.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;

public class Player {

  @Parser
  public static @Nullable Player parse(final Value player) {
    if (player.isNull()) {
      return null;
    } else {
      final var playerNode = player.asNode();
      return new Player(PlayerId.parse(playerNode), PlayerName.parse(playerNode));
    }
  }

  public static @NonNull Player parseProperties(final @NonNull Record r)  {
    final var playerId = PlayerId.parse(r);
    if (playerId == null) {
      throw new NullPointerException("Player.id");
    }
    final var playerName = PlayerName.parse(r);
    if (playerName == null) {
      throw new NullPointerException("Player.name");
    }
    return new Player(playerId, playerName);
  }

  final @NonNull PlayerId id;
  final @NonNull PlayerName name;

  Player(
      final @NonNull PlayerId id,
      final @NonNull PlayerName name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public boolean equals(final @Nullable Object o) {
    return (this == o) || (o instanceof Player other && this.id.equals(other.id));
  }

  @Override
  public int hashCode() {
    return this.id.hashCode();
  }

  @Override
  public String toString() {
    return "Player(" + this.id + ", " + this.name + ')';
  }
}

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

/*
@Queries
public interface Players {

  @MATCH
  boolean existsPlayerWithId(long playerId);

  @MATCH
  boolean exists(@NonNull PlayerId playerId);

  @MATCH
  @Nullable Node findPlayerNodeById(long playerId);

  @MATCH
  @Nullable Map<@NonNull String, @Nullable Object> findPlayerPropertiesById(long playerId);

  @MATCH
  @Nullable Player findPlayerBy(@NonNull PlayerId playerId);

  @MATCH
  @Alias("p")
  Player getPlayerBy(@NonNull PlayerId playerId);

  @MATCH(parserClass = Player.class, parserMethod = "parseProperties")
  Optional<@NonNull Player> playerBy(@NonNull PlayerId playerId);
}

@Generated("mate4j-queries")
final class PlayersNeo4jRepository implements Players {

  @Override
  public boolean existsPlayerWithId(final long playerId) {
    final var queryParameter = Map.<@NonNull String, @NonNull Object>of("playerId", playerId);

    return CurrentSession.get()
        .executeRead(tx -> { // it is a MATCH -> executeRead
          final var result = tx.run("MATCH (p:Player {id: ${playerId}) RETURN p IS NOT NULL", queryParameter);
          final var row = result.next(); // returns a primitive -> assume it has a value
          final var value = row.get(0); // it is a primitive -> get first (supodsely only) column on result
          return value.asBoolean(false); // translate, if null use default primitive value
        });
  }

  @Override
  public boolean exists(final @NonNull PlayerId playerId) {
    final var queryParameter = Map.<@NonNull String, @NonNull Object>of(
        "playerId", // by default, use parameter name
        PlayerId.AS_PARAMETER.apply(playerId));

    return CurrentSession.get()
        .executeRead(tx -> { // it is a MATCH -> executeRead
          final var result = tx.run("MATCH (p:Player {id: ${playerId}) RETURN p IS NOT NULL", queryParameter);
          final var row = result.next(); // returns a primitive -> assume it has a value
          final var value = row.get(0); // it is a primitive -> get first (supodsely only) column on result
          return value.asBoolean(false); // translate, if null use default primitive value
        });
  }

  @Override
  public @Nullable Node findPlayerNodeById(final long playerId) {
    final var queryParameter = Map.<@NonNull String, @NonNull Object>of("playerId", playerId);

    final var node = CurrentSession.get().executeRead(tx -> { // it is a MATCH -> executeRead
          final var result = tx.run("MATCH (player:Player {id: ${playerId}) RETURN player LIMIT 1", queryParameter);
          if (result.hasNext()) { // non-primitive, non-sequence -> can be null
            final var row = result.next(); // returns a primitive -> assume it has a value
            if (result.hasNext()) {
              throw new IllegalStateException("too many rows while solving Players#findPlayerBy(PlayerId)");
            }
            return row.get(0).asNode();
          } else {
            return null;
          }
        });
    return node;
  }

  @Override
  public @Nullable Map<@NonNull String, @Nullable Object> findPlayerPropertiesById(final long playerId) {
    final var queryParameter = Map.<@NonNull String, @NonNull Object>of("playerId", playerId);

    final var map = CurrentSession.get().executeRead(tx -> { // it is a MATCH -> executeRead
      final var result = tx.run("MATCH (player:Player {id: ${playerId}) RETURN player LIMIT 1", queryParameter);
      if (result.hasNext()) { // non-primitive, non-sequence -> can be null
        final var row = result.next(); // returns a primitive -> assume it has a value
        if (result.hasNext()) {
          throw new IllegalStateException("too many rows while solving Players#findPlayerBy(PlayerId)");
        }
        return row.get(0).asMap();
      } else {
        return null;
      }
    });
    return map;
  }

  @Override
  public @Nullable Player findPlayerBy(final @NonNull PlayerId playerId) {
    final var queryParameter = Map.<@NonNull String, @NonNull Object>of(
        "playerId", // by default, use parameter name
        PlayerId.AS_PARAMETER.apply(playerId)); // this allow

    final var player = CurrentSession.get().executeRead(tx -> { // it is a MATCH -> executeRead
      final var result = tx.run("MATCH (player:Player {id: ${playerId}) RETURN player LIMIT 1", queryParameter);
      if (result.hasNext()) { // non-primitive, non-sequence -> can be null
        final var row = result.next(); // returns a primitive -> assume it has a value
        if (result.hasNext()) {
          throw new IllegalStateException("too many rows while solving Players#findPlayerBy(PlayerId)");
        }
        return Player.parse(row.get(0));
      } else {
        return null;
      }
    });
    return player;
  }

  @Override
  public @NonNull Player getPlayerBy(final @NonNull PlayerId playerId) {
    final var queryParameter = Map.<@NonNull String, @NonNull Object>of(
        "playerId", // by default, use parameter name
        PlayerId.AS_PARAMETER.apply(playerId)); // this allow

    final var player = CurrentSession.get().executeRead(tx -> { // it is a MATCH -> executeRead
      final var result = tx.run("MATCH (p:Player {id: ${playerId}) RETURN p LIMIT 1", queryParameter);
      if (result.hasNext()) { // non-primitive, non-sequence -> can be null
        final var row = result.next(); // returns a primitive -> assume it has a value
        if (result.hasNext()) {
          throw new IllegalStateException("too many rows while solving Players#findPlayerBy(PlayerId)");
        }
        return Player.parse(row.get("p"));
      } else {
        throw new IllegalStateException("empty result while solving Players#findPlayerBy(PlayerId)");
      }
    });
    return player;
  }

  @Override
  public @NonNull Optional<@NonNull Player> playerBy(final @NonNull PlayerId playerId) {
    final var queryParameter = Map.<@NonNull String, @NonNull Object>of(
        "playerId", // by default, use parameter name
        PlayerId.AS_PARAMETER.apply(playerId)); // this allow

    final Optional<@NonNull Player> player = CurrentSession.get().executeRead(tx -> { // it is a MATCH -> executeRead
      final var result = tx.run("MATCH (player:Player {id: ${playerId}) RETURN player LIMIT 1", queryParameter);
      if (result.hasNext()) { // non-primitive, non-sequence -> can be null
        final var row = result.next(); // returns a primitive -> assume it has a value
        if (result.hasNext()) {
          throw new IllegalStateException("too many rows while solving Players#findPlayerBy(PlayerId)");
        }
        return Optional.ofNullable(Player.parseProperties(row));
      } else {
        return Optional.empty();
      }
    });
    return player;
  }
}
*/
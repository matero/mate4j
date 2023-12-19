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
import org.neo4j.driver.Values;
import org.neo4j.exceptions.EntityNotFoundException;

@Queries
public interface Players {
  @Query("MATCH (p:Player {id: $id}) RETURN p IS NOT NULL")
  boolean existsPlayerWithId(long id)
      throws EntityNotFoundException;

  @Query(cypher = """
      MATCH (n:Player)
      WHERE n.id = $playerId
      DETACH DELETE n""", txType = TransactionType.WRITE)
  void delete(@Alias("playerId") int id) throws NullPointerException, EntityNotFoundException;

  @Query("MATCH (n:Player) WHERE n.id = $playerId RETURN n")
  Object get(int i);
}

final class queries {
  private queries() {
    throw new ClassNotInstantiable(queries.class);
  }

  static final Players Players = PlayersJ21Impl.INSTANCE;
}

enum PlayersJ21Impl implements Players {
  INSTANCE;

  @Override
  public boolean existsPlayerWithId(long id) throws EntityNotFoundException {
    final var __query = new org.neo4j.driver.Query(
        "MATCH (p:Player {id: $id}) RETURN p IS NOT NULL",
        java.util.Map.of("id", Values.value(id))
    );

    return matero.queries.neo4j.CurrentSession.get().executeRead(tx -> {
      final var result = tx.run(__query);
      if (!result.hasNext()) {
        throw new matero.queries.EmptyResult();
      }
      final var row = result.next();
      if (result.hasNext()) {
        throw matero.queries.TooManyRows.moreThanOne();
      }
      final var value = row.get(0);
      return value.asBoolean();
    });
  }

  @Override
  public void delete(int id) throws NullPointerException, EntityNotFoundException {
    final var __query = new org.neo4j.driver.Query(
        """
            MATCH (n:Player)
            WHERE n.id = $playerId
            DETACH DELETE n""",
        java.util.Map.of("id", Values.value(id))
    );
    matero.queries.neo4j.CurrentSession.get().executeWrite(tx -> tx.run(__query));
  }

  @Override
  public Object get(int i) {
    return Long.MAX_VALUE;
  }
}
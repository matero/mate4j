computed(executor, mapper, defaultValue="null") ::= <<
return matero.queries.neo4j.CurrentSession.get().<executor>(tx -> {
  final var result = tx.run(__query);
  if (!result.hasNext()) {
    throw new matero.queries.EmptyResult();
  }
  final var row = result.next();
  if (result.hasNext()) {
    throw matero.queries.TooManyRows.moreThanOne();
  }
  final var value = row.get(0);
  return value.computeOrDefault(<mapper>, <defaultValue>);
});
>>
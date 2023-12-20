single(executor, mapper) ::= <<
return matero.queries.neo4j.CurrentSession.get().<executor>(tx -> {
  final var result = tx.run(__query);
  final var record = result.single();
  final var value = record.get(0);
  return <mapper>;
});
>>
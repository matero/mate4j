reference(executor, cast) ::= <<
return matero.queries.neo4j.CurrentSession.get().<executor>(tx -> {
  final var value = tx.run(__query).single().get(0);
  if (value.isNull()) {
    return null;
  } else {
    return value.<cast>;
  }
});
>>
char(executor) ::= <<
return matero.queries.neo4j.CurrentSession.get().<executor>(tx -> {
  final var value = tx.run(__query).single().get(0);
  if (value.isNull()) {
    throw new NullPointerException();
  }
  final var s = value.asString();
  if (s.isEmpty()) {
    throw new org.neo4j.driver.exceptions.value.ValueException("received string is empty");
  }
  if (s.length() > 1) {
    throw new org.neo4j.driver.exceptions.value.ValueException("received string has more than 1 char");
  }
  return s.charAt(0);
});
>>
Character(executor) ::= <<
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
  if (value.isNull()) {
    return null;
  } else {
    final var s = value.asString();
    if (s.isEmpty()) {
      throw new org.neo4j.driver.exceptions.value.ValueException("received string is empty");
    }
    if (s.length() > 1) {
      throw new org.neo4j.driver.exceptions.value.ValueException("received string has more than 1 char");
    }
    return s.charAt(0);
  }
});
>>
list(executor, mapper) ::= <<
return matero.queries.neo4j.CurrentSession.get().<executor>(tx -> {
    final var result = tx.run(__query);
    <if (mapper)>
    return result.list(record -> {
      final var value = record.get(0);
      <mapper>;
    });
    <else>
    return result.list();
    <endif>
  });
>>
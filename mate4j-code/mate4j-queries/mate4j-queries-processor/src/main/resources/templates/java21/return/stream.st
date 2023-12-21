stream(executor, mapper) ::= <<
return matero.queries.neo4j.CurrentSession.get().<executor>(tx -> {
    final var result = tx.run(__query);
    <if (mapper)>
    return result.stream().map(record -> {
      final var value = record.get(0);
      return <mapper>;
    });
    <else>
    return result.stream();
    <endif>
  });
>>
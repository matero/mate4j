queryMethod(m) ::= <<
@Override public <m.returnType> <m.name>(
  <m.parameters:{prm|<declareMethodParameter(prm)>}; separator=",\n">)<declareThrows(m)>
{
  final var __cypher = <m.cypher>;
  final var __parameters = java.util.Map.of(
    <m.parameters:{p | "<p.alias>", <p.value>}; separator=",\n">
  );
  <if (m.returnsValue)>
  return matero.queries.neo4j.CurrentSession.get().<m.executor>(tx -> {
      final var result = tx.run(__cypher, __parameters);
      \{ processResultOf(method) }
    });
  <else>
  matero.queries.neo4j.CurrentSession.get().<m.executor>(tx -> tx.run(__cypher, __parameters));
  <endif>
  <if (m.returnsNull)>return null;<endif>
}
>>
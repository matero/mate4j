queryMethod(m) ::= <<
@Override public <m.returnType> <m.name>(
  <m.parameters:{prm|<declareMethodParameter(prm)>}; separator=",\n">)<declareThrows(m)>
{
  final var __query = new org.neo4j.driver.Query(
      <m.cypher>,
      java.util.Map.of(
        <m.parameters:{p | "<p.alias>", <p.value>}; separator=",\n">
      );
    );
  <(m.returnType.executionTemplate)(m.executor)>
}
>>
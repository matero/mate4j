queryMethod(m) ::= <<
@Override public <m.returnType> <m.name>(<m.parameters:{prm|<declareMethodParameter(prm)>}; separator=", ">)<declareThrows(m)> {
  final var __queryParameters = java.util.Map.of(
    <m.parameters:{p | "<p.alias>", <p.value>}; separator=",\n">
  );
}
>>
queryMethod(m) ::= <<
@Override public <m.returnType> <m.name>(<m.parameters:{prm|<declareMethodParameter(prm)>}; separator=", ">)<declareThrows(m)> {
}
>>
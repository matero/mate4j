declareThrows(m) ::= <<
<if (m.declareThrows)>
throws <m.exceptions; separator=",\n       "><endif>
>>
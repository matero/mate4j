impl(spec, date) ::= <<
<if (!spec.inRootPackage)>package <spec.packageName>;

<endif>
<spec.imports:{decl | import <decl>;}; separator="\n">

@javax.annotation.processing.Generated(
  value="<spec.processorClassName>",
  date="<date>",
  comments="code generated for java 21")
final class <spec.implClassName>
    implements <spec.interfaceClassName> {

  <spec.queryMethods:{m|<queryMethod(m)>}; separator="\n\n">

}

>>
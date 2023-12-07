package matero.queries;

import org.checkerframework.checker.nullness.qual.NonNull;

import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

record QueryMethod(
    @NonNull Name name,
    @NonNull TypeMirror returnType,
    @NonNull List<@NonNull ? extends VariableElement> parameters,
    @NonNull List<@NonNull ? extends TypeMirror> thrownTypes,
    @NonNull String cypher,

    boolean read) {
}

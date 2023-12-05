package matero.queries;

import org.checkerframework.checker.nullness.qual.NonNull;

interface ImplementationCodeBuilder {

  @NonNull String getImplementationCodeFor(@NonNull QueriesAnnotatedInterface queries);
}

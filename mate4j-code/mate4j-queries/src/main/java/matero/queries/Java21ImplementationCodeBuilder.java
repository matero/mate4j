package matero.queries;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

final class Java21ImplementationCodeBuilder implements ImplementationCodeBuilder {
  private final @NonNull String date;

  Java21ImplementationCodeBuilder() {
    this(LocalDateTime.now());
  }

  Java21ImplementationCodeBuilder(final @NonNull LocalDateTime date) {
    this(date.format(DateTimeFormatter.ISO_DATE_TIME));
  }

  Java21ImplementationCodeBuilder(final @NonNull String date) {
    this.date = date;
  }

  @Override
  public @NonNull String getImplementationCodeFor(final @NonNull QueriesAnnotatedInterface queries) {
    return STR. """
        package \{ queries.packageName() };

        \{ queries.imports().stream().map(it -> "import " + it + ';').collect(Collectors.joining("\n")) }

        @javax.annotation.processing.Generated(
          value="\{ QueriesProcessor.class.getCanonicalName() }",
          date="\{ this.date }",
          comments="code generated for java 21")
        final class \{ queries.interfaceName() }Java21Impl implements \{ queries.interfaceName() } {

          \{ queries.methods() }
        }

        """ ;
  }
}

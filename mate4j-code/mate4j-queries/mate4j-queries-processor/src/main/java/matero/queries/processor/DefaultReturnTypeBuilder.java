package matero.queries.processor;

/*-
 * #%L
 * mate4j-queries-processor
 * %%
 * Copyright (C) 2023 matero
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import matero.queries.processor.ReturnType.Mapper;

final class DefaultReturnTypeBuilder implements ReturnTypeBuilder {
  private @Nullable StringBuilder annotations;
  private @NonNull String javaSpec = "";
  private @NonNull String executionTemplate = "";
  private @Nullable Mapper mapper;

  @Override
  public @NonNull ReturnType build() {
    return new ReturnType(getAnnotations(), this.javaSpec, this.executionTemplate, this.mapper);
  }

  @Override
  public @NonNull String getAnnotations() {
    return this.annotations == null ? "" : this.annotations.toString();
  }

  @Override
  public @Nullable Mapper getMapper() {
    return this.mapper;
  }

  @Override
  public @NonNull String getJavaSpec() {
    return this.javaSpec;
  }

  @Override
  public @NonNull ReturnTypeBuilder javaSpec(final @NonNull String value) {
    this.javaSpec = value;
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder executionTemplate(final @NonNull String value) {
    this.executionTemplate = value;
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder mapper(final @Nullable Mapper value) {
    this.mapper = value;
    return this;
  }


  @Override
  public boolean hasAnnotations() {
    return this.annotations != null && !this.annotations.isEmpty();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotation(final @NonNull String annotation) {
    annotations().append('@').append(annotation);
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder startAnnotationValues() {
    annotations().append('(');
    return this;
  }

  private @NonNull StringBuilder annotations() {
    if (this.annotations == null) {
      this.annotations = new StringBuilder();
    }
    return this.annotations;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValueName(final @NonNull String name) {
    final var ann = annotations();
    if (ann.charAt(ann.length() - 1) != '(') {
      ann.ensureCapacity(ann.length() + 3 + name.length());
      ann.append(", ");
    } else {
      ann.ensureCapacity(ann.length() + 1 + name.length());
    }
    ann.append(name).append('=');
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final @NonNull String value) {
    annotations().append('"').append(value).append('"');
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(boolean value) {
    annotations().append(value);
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(byte value) {
    annotations().append(value);
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(short value) {
    annotations().append(value);
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(int value) {
    annotations().append(value);
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(long value) {
    annotations().append(value);
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(float value) {
    annotations().append(value);
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(double value) {
    annotations().append(value);
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(char value) {
    annotations().append(value);
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationQuotedValue(@NonNull String value) {
    annotations().append('"').append(value).append('"');
    return this;
  }

  @Override
  public @NonNull ReturnTypeBuilder endAnnotationValues() {
    annotations().append(')');
    return this;
  }
}

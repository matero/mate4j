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

import matero.queries.processor.ReturnType.Mapper;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

enum PredefinedReturnType
    implements ReturnTypeBuilder {
  VOID("", "void", "return/void"),
  VOID_WRAPPER("", "Void", "return/Void");

  private final ReturnType returnType;

  PredefinedReturnType(
      final @NonNull String annotations,
      final @NonNull String javaSpec,
      final @NonNull String executionTemplate) {
    this.returnType = new ReturnType(annotations, javaSpec, executionTemplate, null);
  }

  @Override
  public @NonNull ReturnType build() {
    return this.returnType;
  }

  @Override
  public @NonNull ReturnTypeBuilder javaSpec(final @NonNull String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder executionTemplate(final @NonNull String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder mapper(final @Nullable Mapper value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasAnnotations() {
    return false;
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotation(final @NonNull String annotation) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder startAnnotationValues() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValueName(final @NonNull String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final @NonNull String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final boolean value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final byte value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final short value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final int value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final long value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final float value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final double value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationValue(final char value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder appendAnnotationQuotedValue(final @NonNull String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull ReturnTypeBuilder endAnnotationValues() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NonNull String getAnnotations() {
    return this.returnType.annotations;
  }

  @Override
  public @Nullable Mapper getMapper() {
    return this.returnType.mapper;
  }

  @Override
  public @NonNull String getJavaSpec() {
    return this.returnType.javaSpec;
  }
}

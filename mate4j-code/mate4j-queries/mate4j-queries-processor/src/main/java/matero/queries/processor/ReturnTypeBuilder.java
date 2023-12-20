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

sealed interface ReturnTypeBuilder permits PredefinedReturnType, DefaultReturnTypeBuilder {
  @Nullable String NO_MAPPER = null;

  @NonNull ReturnType build();

  @NonNull ReturnTypeBuilder javaSpec(@NonNull String value);

  @NonNull ReturnTypeBuilder executionTemplate(@NonNull String value);

  @NonNull ReturnTypeBuilder mapper(@Nullable Mapper value);

  boolean hasAnnotations();

  @NonNull ReturnTypeBuilder appendAnnotation(@NonNull String annotation);

  @NonNull ReturnTypeBuilder startAnnotationValues();

  @NonNull ReturnTypeBuilder appendAnnotationValueName(@NonNull String name);

  @NonNull ReturnTypeBuilder appendAnnotationValue(@NonNull String value);

  @NonNull ReturnTypeBuilder appendAnnotationValue(boolean value);

  @NonNull ReturnTypeBuilder appendAnnotationValue(byte value);

  @NonNull ReturnTypeBuilder appendAnnotationValue(short value);

  @NonNull ReturnTypeBuilder appendAnnotationValue(int value);

  @NonNull ReturnTypeBuilder appendAnnotationValue(long value);

  @NonNull ReturnTypeBuilder appendAnnotationValue(float value);

  @NonNull ReturnTypeBuilder appendAnnotationValue(double value);

  @NonNull ReturnTypeBuilder appendAnnotationValue(char value);

  @NonNull ReturnTypeBuilder appendAnnotationQuotedValue(@NonNull String s);

  @NonNull ReturnTypeBuilder endAnnotationValues();

  @NonNull String getAnnotations();

  @Nullable Mapper getMapper();

  @NonNull String getJavaSpec();
}

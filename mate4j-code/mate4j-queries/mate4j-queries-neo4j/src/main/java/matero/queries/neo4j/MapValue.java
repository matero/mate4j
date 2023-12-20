package matero.queries.neo4j;

/*-
 * #%L
 * mate4j-queries-neo4j
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

import matero.support.ClassNotInstantiable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.neo4j.driver.Value;
import org.neo4j.driver.exceptions.value.ValueException;

public final class MapValue {
  private MapValue() {
    throw new ClassNotInstantiable(MapValue.class);
  }

  public static boolean toPrimitiveBoolean(final @NonNull Value value) {
    if (value.isNull()) {
      throw new ValueException("value is null");
    }
    return value.asBoolean();
  }

  public static char toPrimitiveChar(final @NonNull Value value) {
    if (value.isNull()) {
      throw new ValueException("value is null");
    }
    final var s = value.asString();
    if (s.isEmpty()) {
      throw new ValueException("received string is empty");
    }
    if (s.length() > 1) {
      throw new ValueException("received string has more than 1 char");
    }
    return s.charAt(0);
  }

  public static byte toPrimitiveByte(final @NonNull Value value) {
    if (value.isNull()) {
      throw new ValueException("value is null");
    }
    return value.asNumber().byteValue();
  }

  public static short toPrimitiveShort(final @NonNull Value value) {
    if (value.isNull()) {
      throw new ValueException("value is null");
    }
    return value.asNumber().shortValue();
  }

  public static int toPrimitiveInteger(final @NonNull Value value) {
    if (value.isNull()) {
      throw new ValueException("value is null");
    }
    return value.asInt();
  }

  public static long toPrimitiveLong(final @NonNull Value value) {
    if (value.isNull()) {
      throw new ValueException("value is null");
    }
    return value.asLong();
  }

  public static float toPrimitiveFloat(final @NonNull Value value) {
    if (value.isNull()) {
      throw new ValueException("value is null");
    }
    return value.asFloat();
  }

  public static double toPrimitiveDouble(final @NonNull Value value) {
    if (value.isNull()) {
      throw new ValueException("value is null");
    }
    return value.asDouble();
  }

  public static @Nullable Boolean toNullableBoolean(final @NonNull Value value) {
    if (value.isNull()) {
      return null;
    } else {
      return value.asBoolean();
    }
  }

  public static @Nullable Character toNullableCharacter(final @NonNull Value value) {
    if (value.isNull()) {
      return null;
    } else {
      final var s = value.asString();
      if (s.isEmpty()) {
        throw new ValueException("received string is empty");
      }
      if (s.length() > 1) {
        throw new ValueException("received string has more than 1 char");
      }
      return s.charAt(0);
    }
  }

  public static @Nullable Byte toNullableByte(final @NonNull Value value) {
    if (value.isNull()) {
      return null;
    } else {
      return value.asNumber().byteValue();
    }
  }

  public static @Nullable Short toNullableShort(final @NonNull Value value) {
    if (value.isNull()) {
      return null;
    } else {
      return value.asNumber().shortValue();
    }
  }

  public static @Nullable Integer toNullableInteger(final @NonNull Value value) {
    if (value.isNull()) {
      return null;
    } else {
      return value.asInt();
    }
  }

  public static @Nullable Long toNullableLong(final @NonNull Value value) {
    if (value.isNull()) {
      return null;
    } else {
      return value.asLong();
    }
  }

  public static @Nullable Float toNullableFloat(final @NonNull Value value) {
    if (value.isNull()) {
      return null;
    } else {
      return value.asFloat();
    }
  }

  public static @Nullable Double toNullableDouble(final @NonNull Value value) {
    if (value.isNull()) {
      return null;
    } else {
      return value.asDouble();
    }
  }
}

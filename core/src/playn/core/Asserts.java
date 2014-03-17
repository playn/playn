/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.core;

/** @deprecated Use stock java assert statement. These methods cannot be properly optimized away. */
@Deprecated
public class Asserts
{
  private Asserts() {}

  private static final boolean assertsEnabled = Asserts.class.desiredAssertionStatus();

  /** @deprecated Use stock java assert statement. This method cannot be properly optimized away. */
  @Deprecated
  public static void check(boolean expression) {
    if (assertsEnabled && !expression) {
      throw new AssertionError();
    }
  }

  /** @deprecated Use stock java assert statement. This method cannot be properly optimized away. */
  @Deprecated
  public static void check(boolean expression, Object errorMessage) {
    if (assertsEnabled && !expression) {
      throw new AssertionError(String.valueOf(errorMessage));
    }
  }

  /** @deprecated Use stock java assert statement. This method cannot be properly optimized away. */
  @Deprecated
  public static void check(boolean expression, String errorMessageTemplate,
                           Object... errorMessageArgs) {
    if (assertsEnabled && !expression) {
      throw new AssertionError(format(errorMessageTemplate, errorMessageArgs));
    }
  }

  /** @deprecated Use stock java assert statement. This method cannot be properly optimized away. */
  @Deprecated
  public static void checkArgument(boolean expression) {
    if (assertsEnabled && !expression) {
      throw new IllegalArgumentException();
    }
  }

  /** @deprecated Use stock java assert statement. This method cannot be properly optimized away. */
  @Deprecated
  public static void checkArgument(boolean expression, Object errorMessage) {
    if (assertsEnabled && !expression) {
      throw new IllegalArgumentException(String.valueOf(errorMessage));
    }
  }

  /** @deprecated Use stock java assert statement. This method cannot be properly optimized away. */
  @Deprecated
  public static void checkArgument(boolean expression, String errorMessageTemplate,
                                   Object... errorMessageArgs) {
    if (assertsEnabled && !expression) {
      throw new IllegalArgumentException(format(errorMessageTemplate, errorMessageArgs));
    }
  }

  /** @deprecated Use stock java assert statement. This method cannot be properly optimized away. */
  @Deprecated
  public static void checkState(boolean expression) {
    if (assertsEnabled && !expression) {
      throw new IllegalStateException();
    }
  }

  /** @deprecated Use stock java assert statement. This method cannot be properly optimized away. */
  @Deprecated
  public static void checkState(boolean expression, Object errorMessage) {
    if (assertsEnabled && !expression) {
      throw new IllegalStateException(String.valueOf(errorMessage));
    }
  }

  /** @deprecated Use stock java assert statement. This method cannot be properly optimized away. */
  @Deprecated
  public static void checkState(boolean expression, String errorMessageTemplate,
                                Object... errorMessageArgs) {
    if (assertsEnabled && !expression) {
      throw new IllegalStateException(format(errorMessageTemplate, errorMessageArgs));
    }
  }

  /** @deprecated Use stock java assert statement. This method cannot be properly optimized away. */
  @Deprecated
  public static <T> T checkNotNull(T reference) {
    if (assertsEnabled && reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  /** @deprecated Use stock java assert statement. This method cannot be properly optimized away. */
  @Deprecated
  public static <T> T checkNotNull(T reference, Object errorMessage) {
    if (assertsEnabled && reference == null) {
      throw new NullPointerException(String.valueOf(errorMessage));
    }
    return reference;
  }

  /** @deprecated Use stock java assert statement. This method cannot be properly optimized away. */
  @Deprecated
  public static <T> T checkNotNull(T reference, String errorMessageTemplate,
                                   Object... errorMessageArgs) {
    if (assertsEnabled && reference == null) {
      // If either of these parameters is null, the right thing happens anyway
      throw new NullPointerException(format(errorMessageTemplate, errorMessageArgs));
    }
    return reference;
  }

  /** @deprecated Use stock java assert statement. This method cannot be properly optimized away. */
  @Deprecated
  public static int checkElementIndex(int index, int size) {
    return checkElementIndex(index, size, "index");
  }

  /** @deprecated Use stock java assert statement. This method cannot be properly optimized away. */
  @Deprecated
  public static int checkElementIndex(int index, int size, String desc) {
    // Carefully optimized for execution by hotspot (explanatory comment above)
    if (assertsEnabled && (index < 0 || index >= size)) {
      throw new IndexOutOfBoundsException(badElementIndex(index, size, desc));
    }
    return index;
  }

  private static String badElementIndex(int index, int size, String desc) {
    if (index < 0) {
      return format("%s (%s) must not be negative", desc, index);
    } else if (size < 0) {
      throw new IllegalArgumentException("negative size: " + size);
    } else { // index >= size
      return format("%s (%s) must be less than size (%s)", desc, index, size);
    }
  }

  /** @deprecated Use stock java assert statement. This method cannot be properly optimized away. */
  @Deprecated
  public static int checkPositionIndex(int index, int size) {
    return checkPositionIndex(index, size, "index");
  }

  /** @deprecated Use stock java assert statement. This method cannot be properly optimized away. */
  @Deprecated
  public static int checkPositionIndex(int index, int size, String desc) {
    // Carefully optimized for execution by hotspot (explanatory comment above)
    if (assertsEnabled && (index < 0 || index > size)) {
      throw new IndexOutOfBoundsException(badPositionIndex(index, size, desc));
    }
    return index;
  }

  private static String badPositionIndex(int index, int size, String desc) {
    if (index < 0) {
      return format("%s (%s) must not be negative", desc, index);
    } else if (size < 0) {
      throw new IllegalArgumentException("negative size: " + size);
    } else { // index > size
      return format("%s (%s) must not be greater than size (%s)", desc, index, size);
    }
  }

  /** @deprecated Use stock java assert statement. This method cannot be properly optimized away. */
  @Deprecated
  public static void checkPositionIndexes(int start, int end, int size) {
    // Carefully optimized for execution by hotspot (explanatory comment above)
    if (assertsEnabled && (start < 0 || end < start || end > size)) {
      throw new IndexOutOfBoundsException(badPositionIndexes(start, end, size));
    }
  }

  private static String badPositionIndexes(int start, int end, int size) {
    if (start < 0 || start > size) {
      return badPositionIndex(start, size, "start index");
    }
    if (end < 0 || end > size) {
      return badPositionIndex(end, size, "end index");
    }
    // end < start
    return format("end index (%s) must not be less than start index (%s)", end, start);
  }

  /**
   * Substitutes each {@code %s} in {@code template} with an argument. These are matched by
   * position - the first {@code %s} gets {@code args[0]}, etc. If there are more arguments than
   * placeholders, the unmatched arguments will be appended to the end of the formatted message in
   * square braces.
   *
   * @param template a non-null string containing 0 or more {@code %s} placeholders.
   * @param args the arguments to be substituted into the message template. Arguments are converted
   *     to strings using {@link String#valueOf(Object)}. Arguments can be null.
   */
  static String format(String template, Object... args) {
    template = String.valueOf(template); // null -> "null"

    // start substituting the arguments into the '%s' placeholders
    StringBuilder builder = new StringBuilder(
        template.length() + 16 * args.length);
    int templateStart = 0;
    int i = 0;
    while (i < args.length) {
      int placeholderStart = template.indexOf("%s", templateStart);
      if (placeholderStart == -1) {
        break;
      }
      builder.append(template.substring(templateStart, placeholderStart));
      builder.append(args[i++]);
      templateStart = placeholderStart + 2;
    }
    builder.append(template.substring(templateStart));

    // if we run out of placeholders, append the extra args in square braces
    if (i < args.length) {
      builder.append(" [");
      builder.append(args[i++]);
      while (i < args.length) {
        builder.append(", ");
        builder.append(args[i++]);
      }
      builder.append(']');
    }

    return builder.toString();
  }
}

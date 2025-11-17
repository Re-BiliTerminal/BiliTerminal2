package com.google.gson.internal;

import java.util.Objects;

/**
 * A simple utility class used to check method Preconditions.
 *
 * <pre>
 * public long divideBy(long value) {
 *   Preconditions.checkArgument(value != 0);
 *   return this.value / value;
 * }
 * </pre>
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
@SuppressWarnings("MemberName") // legacy class name
public final class GsonPreconditions {
  private GsonPreconditions() {
    throw new UnsupportedOperationException();
  }

  /**
   * @deprecated This is an internal Gson method. Use {@link Objects#requireNonNull(Object)}
   *     instead.
   */
  // Only deprecated for now because external projects might be using this by accident
  @Deprecated
  public static <T> T checkNotNull(T obj) {
    if (obj == null) {
      throw new NullPointerException();
    }
    return obj;
  }

  public static void checkArgument(boolean condition) {
    if (!condition) {
      throw new IllegalArgumentException();
    }
  }
}
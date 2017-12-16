package org.robolectric.util;

import java.util.Objects;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * Included in Robolectric since Android doesn't support streams yet (as of O).
 *
 * @param <T> the type of the input to the operation
 */
public interface Consumer<T> {

  /**
   * Performs this operation on the given argument.
   *
   * @param t the input argument
   */
  void accept(T t);

  /**
   * Returns a composed {@code Consumer} that performs, in sequence, this operation followed by the
   * {@code after} operation. If performing either operation throws an exception, it is relayed to
   * the caller of the composed operation.  If performing this operation throws an exception, the
   * {@code after} operation will not be performed.
   *
   * @param after the operation to perform after this operation
   * @return a composed {@code Consumer} that performs in sequence this operation followed by the
   * {@code after} operation
   * @throws NullPointerException if {@code after} is null
   */
  default Consumer<T> andThen(Consumer<? super T> after) {
    Objects.requireNonNull(after);
    return (T t) -> {
      accept(t);
      after.accept(t);
    };
  }
}

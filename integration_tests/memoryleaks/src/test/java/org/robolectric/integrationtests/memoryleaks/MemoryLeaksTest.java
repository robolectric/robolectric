package org.robolectric.integrationtests.memoryleaks;

import com.google.common.testing.GcFinalization;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.concurrent.Callable;

/** Gradle-specific implementation of {@link BaseMemoryLeaksTest}. */
public final class MemoryLeaksTest extends BaseMemoryLeaksTest {

  // Allow assigning null to potentiallyLeakingCallable to clear the stack's reference on the
  // callable.
  @SuppressWarnings("assignment.type.incompatible")
  @Override
  public <T> void assertNotLeaking(Callable<T> potentiallyLeakingCallable) {
    WeakReference<T> wr;
    try {
      wr = new WeakReference<>(potentiallyLeakingCallable.call());
      // Make it explicit that the callable isn't reachable from this method's stack, in case it
      // holds a strong reference on the supplied instance.
      potentiallyLeakingCallable = null;
    } catch (Exception e) {
      throw new IllegalStateException("encountered an error in the callable", e);
    }
    assertReferentWeaklyReachable(wr);
  }

  private static <T> void assertReferentWeaklyReachable(WeakReference<T> wr) {
    try {
      GcFinalization.awaitClear(wr);
    } catch (RuntimeException e) {
      T notWeaklyReachable = wr.get();

      if (notWeaklyReachable == null) {
        // Looks like it is weakly reachable after all.
        return;
      }

      // GcFinalization throws a RuntimeException instead of a TimeoutException when we timeout to
      // clear the weak reference, so we catch any exception and consider that the assertion failed
      // in that case.
      throw new AssertionError(
          String.format(
              Locale.ROOT,
              "Not true that <%s> is not leaking, encountered an error while attempting to GC it.",
              notWeaklyReachable),
          e);
    }
  }
}

package org.robolectric.android.internal;

import static java.util.Objects.requireNonNull;

import androidx.test.internal.platform.util.TestOutputEmitter;
import com.google.common.annotations.Beta;
import java.util.List;
import java.util.Locale;

/**
 * Timeout exception thrown when idling resources are not idle for longer than the configured
 * timeout.
 *
 * <p>See {@link androidx.test.espresso.IdlingResourceTimeoutException}.
 *
 * <p>Note: This API may be removed in the future in favor of using espresso's exception directly.
 */
@Beta
@SuppressWarnings("RestrictTo")
public final class IdlingResourceTimeoutException extends RuntimeException {
  public IdlingResourceTimeoutException(List<String> resourceNames) {
    super(
        String.format(
            Locale.ROOT, "Wait for %s to become idle timed out", requireNonNull(resourceNames)));
    TestOutputEmitter.dumpThreadStates("ThreadState-IdlingResTimeoutExcep.txt");
  }
}

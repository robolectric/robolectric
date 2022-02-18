package org.robolectric.android.internal;

import static com.google.common.base.Preconditions.checkNotNull;

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
public final class IdlingResourceTimeoutException extends RuntimeException {
  public IdlingResourceTimeoutException(List<String> resourceNames) {
    super(
        String.format(
            Locale.ROOT, "Wait for %s to become idle timed out", checkNotNull(resourceNames)));
  }
}

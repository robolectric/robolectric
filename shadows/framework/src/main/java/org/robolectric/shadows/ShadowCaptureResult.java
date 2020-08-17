package org.robolectric.shadows;

import android.annotation.Nullable;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.CaptureResult.Key;
import android.os.Build.VERSION_CODES;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

/** Shadow of {@link CaptureResult}. */
@Implements(value = CaptureResult.class, minSdk = VERSION_CODES.LOLLIPOP)
public class ShadowCaptureResult {

  private final Map<Key<?>, Object> resultsKeyToValue = new HashMap<>();

  /** Convenience method which returns a new instance of {@link CaptureResult}. */
  public static CaptureResult newCaptureResult() {
    return ReflectionHelpers.callConstructor(CaptureResult.class);
  }

  /**
   * Obtain a property of the CaptureResult.
   */
  @Implementation
  @Nullable
  @SuppressWarnings("unchecked")
  protected <T> T get(Key<T> key) {
    return (T) resultsKeyToValue.get(key);
  }

  /**
   * Sets the value for a given key.
   *
   * @throws IllegalArgumentException if there's an existing value for the key.
   */
  public <T> void set(Key<T> key, T value) {
    Preconditions.checkArgument(!resultsKeyToValue.containsKey(key));
    resultsKeyToValue.put(key, value);
  }
}

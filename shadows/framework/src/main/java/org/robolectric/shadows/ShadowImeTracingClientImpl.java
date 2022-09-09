package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.S_V2;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(
    className = "android.util.imetracing.ImeTracingClientImpl",
    isInAndroidSdk = false,
    minSdk = S,
    maxSdk = S_V2)
public class ShadowImeTracingClientImpl {

  // no-op the constructor to avoid deadlocks
  @Implementation
  protected void __constructor__() {}
}

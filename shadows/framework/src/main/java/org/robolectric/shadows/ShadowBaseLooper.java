package org.robolectric.shadows;

/**
 * Temporary adapter to deal with API migration
 *
 * @deprecated use ShadowLooper
 */
@Deprecated
public final class ShadowBaseLooper {

  public static ShadowLooper shadowMainLooper() {
    return ShadowLooper.shadowMainLooper();
  }
}

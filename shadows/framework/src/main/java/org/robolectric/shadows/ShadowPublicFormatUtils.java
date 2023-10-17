package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for private class PublicFormatUtils.
 *
 * <p>It converts between the "legacy" Image "public format" (S) and the newer "hal format" (T).
 *
 * <p>Reference:
 * https://cs.android.com/android/platform/superproject/+/android-13.0.0_r1:frameworks/base/media/java/android/media/PublicFormatUtils.java
 * https://cs.android.com/android/platform/superproject/+/android-13.0.0_r1:frameworks/base/libs/hostgraphics/PublicFormat.cpp
 */
@Implements(className = "android.media.PublicFormatUtils", minSdk = TIRAMISU)
public class ShadowPublicFormatUtils {

  @Implementation
  protected static int getHalFormat(int imageFormat) {
    return imageFormat;
  }

  @Implementation
  protected static int getHalDataspace(int imageFormat) {
    return 0;
  }

  @Implementation
  protected static int getPublicFormat(int imageFormat, int dataspace) {
    return imageFormat;
  }
}

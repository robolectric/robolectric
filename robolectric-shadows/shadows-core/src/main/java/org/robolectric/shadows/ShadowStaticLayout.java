package org.robolectric.shadows;

import android.text.StaticLayout;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static android.os.Build.VERSION_CODES;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;

/**
 * Shadow for {@link android.text.StaticLayout}.
 */
@Implements(value = StaticLayout.class, looseSignatures = true)
public class ShadowStaticLayout {

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  public static int[] nLineBreakOpportunities(String locale, char[] text, int length, int[] recycle) {
    return new int[] {-1};
  }

  @Implementation(minSdk = M)
  @HiddenApi
  public static int nComputeLineBreaks(Object nativePtr, Object recycle,
            Object recycleBreaks, Object recycleWidths, Object recycleFlags, Object recycleLength) {
    return 1;
  }
}
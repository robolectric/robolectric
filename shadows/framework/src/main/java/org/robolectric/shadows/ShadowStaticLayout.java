package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.text.DynamicLayout;
import android.text.StaticLayout;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for android.text.StaticLayout */
@Implements(value = StaticLayout.class, looseSignatures = true)
public class ShadowStaticLayout {

  @ForType(className = "android.text.StaticLayout$LineBreaks")
  interface LineBreaksReflector {
    @Accessor("breaks")
    void setBreaks(int[] breaks);
  }

  @Resetter
  public static void reset() {
    if (RuntimeEnvironment.getApiLevel() >= M) {
      ReflectionHelpers.setStaticField(DynamicLayout.class, "sStaticLayout", null);
      ReflectionHelpers.setStaticField(DynamicLayout.class, "sBuilder", null);
    }
  }

  @HiddenApi
  @Implementation(minSdk = LOLLIPOP, maxSdk = LOLLIPOP_MR1)
  public static int[] nLineBreakOpportunities(
      String locale, char[] text, int length, int[] recycle) {
    return new int[] {-1};
  }

  @HiddenApi
  @Implementation(minSdk = M, maxSdk = O_MR1)
  public static int nComputeLineBreaks(
      Object nativePtr,
      Object recycle,
      Object recycleBreaks,
      Object recycleWidths,
      Object recycleFlags,
      Object recycleLength) {
    return 1;
  }

  @HiddenApi
  @Implementation(minSdk = P, maxSdk = P)
  protected static int nComputeLineBreaks(
      Object nativePtr,
      Object text,
      Object measuredTextPtr,
      Object length,
      Object firstWidth,
      Object firstWidthLineCount,
      Object restWidth,
      Object variableTabStops,
      Object defaultTabStop,
      Object indentsOffset,
      Object recycle,
      Object recycleLength,
      Object recycleBreaks,
      Object recycleWidths,
      Object recycleAscents,
      Object recycleDescents,
      Object recycleFlags,
      Object charWidths) {
    reflector(LineBreaksReflector.class, recycle).setBreaks(new int[] {((char[]) text).length});
    return 1;
  }
}

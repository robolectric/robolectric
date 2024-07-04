package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.text.DynamicLayout;
import android.text.StaticLayout;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for android.text.StaticLayout */
@Implements(value = StaticLayout.class)
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
  @Implementation(maxSdk = LOLLIPOP_MR1)
  public static int[] nLineBreakOpportunities(
      String locale, char[] text, int length, int[] recycle) {
    return new int[] {-1};
  }

  @HiddenApi
  @Implementation(minSdk = M, maxSdk = O_MR1)
  public static int nComputeLineBreaks(
      long nativePtr,
      @ClassName("android.text.StaticLayout$LineBreaks") Object recycle,
      int[] recycleBreaks,
      float[] recycleWidths,
      int[] recycleFlags,
      int recycleLength) {
    return 1;
  }

  @HiddenApi
  @Implementation(minSdk = P, maxSdk = P)
  protected static int nComputeLineBreaks(
      long nativePtr,
      char[] text,
      long measuredTextPtr,
      int length,
      float firstWidth,
      int firstWidthLineCount,
      float restWidth,
      int[] variableTabStops,
      int defaultTabStop,
      int indentsOffset,
      @ClassName("android.text.StaticLayout$LineBreaks") Object recycle,
      int recycleLength,
      int[] recycleBreaks,
      float[] recycleWidths,
      float[] recycleAscents,
      float[] recycleDescents,
      int[] recycleFlags,
      float[] charWidths) {
    reflector(LineBreaksReflector.class, recycle).setBreaks(new int[] {((char[]) text).length});
    return 1;
  }
}

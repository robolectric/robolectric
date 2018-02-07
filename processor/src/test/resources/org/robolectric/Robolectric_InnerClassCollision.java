package org.robolectric;

import com.example.objects.Dummy;
import com.example.objects.OuterDummy;
import com.example.objects.UniqueDummy;
import com.example.objects.UniqueDummy.UniqueInnerDummy;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.robolectric.annotation.processing.shadows.ShadowDummy;
import org.robolectric.annotation.processing.shadows.ShadowOuterDummy;
import org.robolectric.annotation.processing.shadows.ShadowUniqueDummy;
import org.robolectric.annotation.processing.shadows.ShadowUniqueDummy.ShadowUniqueInnerDummy;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.shadow.api.Shadow;

@Generated("org.robolectric.annotation.processing.RobolectricProcessor")
@SuppressWarnings({"unchecked","deprecation"})
public class Shadows implements ShadowProvider {
  private static final Map<String, String> SHADOW_MAP = new HashMap<>(6);

  static {
    SHADOW_MAP.put("com.example.objects.Dummy", "org.robolectric.annotation.processing.shadows.ShadowDummy");
    SHADOW_MAP.put("com.example.objects.OuterDummy", "org.robolectric.annotation.processing.shadows.ShadowOuterDummy");
    SHADOW_MAP.put("com.example.objects.OuterDummy.InnerDummy", "org.robolectric.annotation.processing.shadows.ShadowOuterDummy$ShadowInnerDummy");
    SHADOW_MAP.put("com.example.objects.UniqueDummy", "org.robolectric.annotation.processing.shadows.ShadowUniqueDummy");
    SHADOW_MAP.put("com.example.objects.UniqueDummy.InnerDummy", "org.robolectric.annotation.processing.shadows.ShadowUniqueDummy$ShadowInnerDummy");
    SHADOW_MAP.put("com.example.objects.UniqueDummy.UniqueInnerDummy", "org.robolectric.annotation.processing.shadows.ShadowUniqueDummy$ShadowUniqueInnerDummy");
  }

  public static ShadowDummy shadowOf(Dummy actual) {
    return (ShadowDummy) Shadow.extract(actual);
  }
  
  public static ShadowOuterDummy shadowOf(OuterDummy actual) {
    return (ShadowOuterDummy) Shadow.extract(actual);
  }
  
  public static ShadowOuterDummy.ShadowInnerDummy shadowOf(OuterDummy.InnerDummy actual) {
    return (ShadowOuterDummy.ShadowInnerDummy) Shadow.extract(actual);
  }
  
  public static ShadowUniqueDummy shadowOf(UniqueDummy actual) {
    return (ShadowUniqueDummy) Shadow.extract(actual);
  }
  
  public static ShadowUniqueDummy.ShadowInnerDummy shadowOf(UniqueDummy.InnerDummy actual) {
    return (ShadowUniqueDummy.ShadowInnerDummy) Shadow.extract(actual);
  }
  
  public static ShadowUniqueInnerDummy shadowOf(UniqueInnerDummy actual) {
    return (ShadowUniqueInnerDummy) Shadow.extract(actual);
  }

  @Override
  public void reset() {
    ShadowDummy.resetter_method();
  }

  @Override
  public Map<String, String> getShadowMap() {
    return SHADOW_MAP;
  }

  @Override
  public String[] getProvidedPackageNames() {
    return new String[] {"com.example.objects"};
  }
}

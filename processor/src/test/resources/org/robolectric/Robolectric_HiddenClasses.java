package org.robolectric;

import com.example.objects.Dummy;
import com.example.objects.OuterDummy2;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.robolectric.annotation.processing.shadows.ShadowDummy;
import org.robolectric.annotation.processing.shadows.ShadowOuterDummy2;
import org.robolectric.annotation.processing.shadows.ShadowOuterDummy2.ShadowInnerPackage;
import org.robolectric.annotation.processing.shadows.ShadowOuterDummy2.ShadowInnerProtected;
import org.robolectric.annotation.processing.shadows.ShadowPrivate;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.shadow.api.Shadow;

@Generated("org.robolectric.annotation.processing.RobolectricProcessor")
@SuppressWarnings({"unchecked","deprecation"})
public class Shadows implements ShadowProvider {
  private static final Map<String, String> SHADOW_MAP = new HashMap<>(6);

  static {
    SHADOW_MAP.put("com.example.objects.Dummy", "org.robolectric.annotation.processing.shadows.ShadowDummy");
    SHADOW_MAP.put("com.example.objects.OuterDummy2", "org.robolectric.annotation.processing.shadows.ShadowOuterDummy2");
    SHADOW_MAP.put("com.example.objects.OuterDummy2.InnerPackage", "org.robolectric.annotation.processing.shadows.ShadowOuterDummy2$ShadowInnerPackage");
    SHADOW_MAP.put("com.example.objects.OuterDummy2.InnerProtected", "org.robolectric.annotation.processing.shadows.ShadowOuterDummy2$ShadowInnerProtected");
    SHADOW_MAP.put("com.example.objects.Private", "org.robolectric.annotation.processing.shadows.ShadowPrivate");
    SHADOW_MAP.put("com.example.objects.OuterDummy2.InnerPrivate", "org.robolectric.annotation.processing.shadows.ShadowOuterDummy2$ShadowInnerPrivate");
  }

  public static ShadowDummy shadowOf(Dummy actual) {
    return (ShadowDummy) Shadow.extract(actual);
  }
  
  public static ShadowOuterDummy2 shadowOf(OuterDummy2 actual) {
    return (ShadowOuterDummy2) Shadow.extract(actual);
  }

  @Override
  public void reset() {
    ShadowDummy.resetter_method();
    ShadowPrivate.resetMethod();
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

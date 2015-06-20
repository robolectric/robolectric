package org.robolectric;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

import org.robolectric.annotation.processing.objects.Dummy;
import org.robolectric.annotation.processing.objects.OuterDummy2;
import org.robolectric.annotation.processing.shadows.ShadowDummy;
import org.robolectric.annotation.processing.shadows.ShadowOuterDummy2;
import org.robolectric.annotation.processing.shadows.ShadowOuterDummy2.ShadowInnerPackage;
import org.robolectric.annotation.processing.shadows.ShadowOuterDummy2.ShadowInnerPrivate;
import org.robolectric.annotation.processing.shadows.ShadowOuterDummy2.ShadowInnerProtected;
import org.robolectric.annotation.processing.shadows.ShadowPrivate;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.internal.ShadowProvider;

@Generated("org.robolectric.annotation.processing.RobolectricProcessor")
@SuppressWarnings({"unchecked","deprecation"})
public class Shadows implements ShadowProvider {
  private static final Map<String, String> SHADOW_MAP = new HashMap<>(6);

  static {
    SHADOW_MAP.put("org.robolectric.annotation.processing.objects.Dummy", "org.robolectric.annotation.processing.shadows.ShadowDummy");
    SHADOW_MAP.put("org.robolectric.annotation.processing.objects.OuterDummy2", "org.robolectric.annotation.processing.shadows.ShadowOuterDummy2");
    SHADOW_MAP.put("org.robolectric.annotation.processing.objects.OuterDummy2.InnerPackage", "org.robolectric.annotation.processing.shadows.ShadowOuterDummy2$ShadowInnerPackage");
    SHADOW_MAP.put("org.robolectric.annotation.processing.objects.OuterDummy2.InnerPrivate", "org.robolectric.annotation.processing.shadows.ShadowOuterDummy2$ShadowInnerPrivate");
    SHADOW_MAP.put("org.robolectric.annotation.processing.objects.OuterDummy2.InnerProtected", "org.robolectric.annotation.processing.shadows.ShadowOuterDummy2$ShadowInnerProtected");
    SHADOW_MAP.put("org.robolectric.annotation.processing.objects.Private", "org.robolectric.annotation.processing.shadows.ShadowPrivate");
  }

  public static ShadowDummy shadowOf(Dummy actual) {
    return (ShadowDummy) ShadowExtractor.extract(actual);
  }
  
  public static ShadowOuterDummy2 shadowOf(OuterDummy2 actual) {
    return (ShadowOuterDummy2) ShadowExtractor.extract(actual);
  }
  
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
    return new String[] {"org.robolectric.annotation.processing.objects"};
  }
}

package org.robolectric;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

import org.robolectric.annotation.processing.objects.AnyObject;
import org.robolectric.annotation.processing.objects.Dummy;
import org.robolectric.annotation.processing.shadows.ShadowAnything;
import org.robolectric.annotation.processing.shadows.ShadowDummy;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.internal.ShadowProvider;

@Generated("org.robolectric.annotation.processing.RobolectricProcessor")
@SuppressWarnings({"unchecked","deprecation"})
public class Shadows implements ShadowProvider {
  private static final Map<String, String> SHADOW_MAP = new HashMap<>(2);

  static {
    SHADOW_MAP.put("org.robolectric.annotation.processing.objects.AnyObject", "org.robolectric.annotation.processing.shadows.ShadowAnything");
    SHADOW_MAP.put("org.robolectric.annotation.processing.objects.Dummy", "org.robolectric.annotation.processing.shadows.ShadowDummy");
  }

  public static ShadowAnything shadowOf(AnyObject actual) {
    return (ShadowAnything) ShadowExtractor.extract(actual);
  }
  
  public static ShadowDummy shadowOf(Dummy actual) {
    return (ShadowDummy) ShadowExtractor.extract(actual);
  }
  
  public void reset() {
    ShadowAnything.anotherResetter();
    ShadowDummy.resetter_method();
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

package org.robolectric;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

import org.robolectric.annotation.processing.objects.Dummy;
import org.robolectric.annotation.processing.objects.ParameterizedDummy;
import org.robolectric.annotation.processing.shadows.ShadowDummy;
import org.robolectric.annotation.processing.shadows.ShadowParameterizedDummy;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.internal.ShadowProvider;

@Generated("org.robolectric.annotation.processing.RobolectricProcessor")
@SuppressWarnings({"unchecked","deprecation"})
public class Shadows implements ShadowProvider {
  private static final Map<String, String> SHADOW_MAP = new HashMap<>(2);

  static {
    SHADOW_MAP.put("org.robolectric.annotation.processing.objects.Dummy", "org.robolectric.annotation.processing.shadows.ShadowDummy");
    SHADOW_MAP.put("org.robolectric.annotation.processing.objects.ParameterizedDummy", "org.robolectric.annotation.processing.shadows.ShadowParameterizedDummy");
  }

  public static ShadowDummy shadowOf(Dummy actual) {
    return (ShadowDummy) ShadowExtractor.extract(actual);
  }
  
  public static <T, N extends Number> ShadowParameterizedDummy<T,N> shadowOf(ParameterizedDummy<T,N> actual) {
    return (ShadowParameterizedDummy<T,N>) ShadowExtractor.extract(actual);
  }

  public void reset() {
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

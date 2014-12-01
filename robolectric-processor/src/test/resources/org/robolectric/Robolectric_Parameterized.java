package org.robolectric;

import javax.annotation.Generated;

import org.robolectric.annotation.processing.objects.Dummy;
import org.robolectric.annotation.processing.objects.ParameterizedDummy;
import org.robolectric.annotation.processing.shadows.ShadowDummy;
import org.robolectric.annotation.processing.shadows.ShadowParameterizedDummy;
import org.robolectric.util.ShadowExtractor;

@Generated("org.robolectric.annotation.processing.RoboProcessor")
public class Shadows {

  public static final Class<?>[] DEFAULT_SHADOW_CLASSES = {
    ShadowDummy.class,
    ShadowParameterizedDummy.class
  };
  
  public static ShadowDummy shadowOf(Dummy actual) {
    return (ShadowDummy) ShadowExtractor.extract(actual);
  }
  
  public static <T, N extends Number> ShadowParameterizedDummy<T,N> shadowOf(ParameterizedDummy<T,N> actual) {
    return (ShadowParameterizedDummy)<T,
    N> ShadowExtractor.extract(actual);
  }

  public static void reset() {
    ShadowDummy.resetter_method();
  }
}

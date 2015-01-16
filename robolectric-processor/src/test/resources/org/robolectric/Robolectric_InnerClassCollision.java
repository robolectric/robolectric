package org.robolectric;

import javax.annotation.Generated;

import org.robolectric.annotation.processing.objects.Dummy;
import org.robolectric.annotation.processing.objects.OuterDummy;
import org.robolectric.annotation.processing.objects.UniqueDummy;
import org.robolectric.annotation.processing.objects.UniqueDummy.UniqueInnerDummy;
import org.robolectric.annotation.processing.shadows.ShadowDummy;
import org.robolectric.annotation.processing.shadows.ShadowOuterDummy;
import org.robolectric.annotation.processing.shadows.ShadowUniqueDummy;
import org.robolectric.annotation.processing.shadows.ShadowUniqueDummy.ShadowUniqueInnerDummy;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.util.ShadowProvider;

@Generated("org.robolectric.annotation.processing.RobolectricProcessor")
@SuppressWarnings({"unchecked","deprecation"})
public class Shadows implements ShadowProvider {

  public static final Class<?>[] DEFAULT_SHADOW_CLASSES = {
    ShadowDummy.class,
    ShadowOuterDummy.class,
    ShadowOuterDummy.ShadowInnerDummy.class,
    ShadowUniqueDummy.class,
    ShadowUniqueDummy.ShadowInnerDummy.class,
    ShadowUniqueInnerDummy.class
  };
  
  public static ShadowDummy shadowOf(Dummy actual) {
    return (ShadowDummy) ShadowExtractor.extract(actual);
  }
  
  public static ShadowOuterDummy shadowOf(OuterDummy actual) {
    return (ShadowOuterDummy) ShadowExtractor.extract(actual);
  }
  
  public static ShadowOuterDummy.ShadowInnerDummy shadowOf(OuterDummy.InnerDummy actual) {
    return (ShadowOuterDummy.ShadowInnerDummy) ShadowExtractor.extract(actual);
  }
  
  public static ShadowUniqueDummy shadowOf(UniqueDummy actual) {
    return (ShadowUniqueDummy) ShadowExtractor.extract(actual);
  }
  
  public static ShadowUniqueDummy.ShadowInnerDummy shadowOf(UniqueDummy.InnerDummy actual) {
    return (ShadowUniqueDummy.ShadowInnerDummy) ShadowExtractor.extract(actual);
  }
  
  public static ShadowUniqueInnerDummy shadowOf(UniqueInnerDummy actual) {
    return (ShadowUniqueInnerDummy) ShadowExtractor.extract(actual);
  }
  
  public void reset() {
    ShadowDummy.resetter_method();
  }
}

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

@Generated("org.robolectric.annotation.processing.RoboProcessor")
public class Shadows {

  public static final Class<?>[] DEFAULT_SHADOW_CLASSES = {
    ShadowDummy.class,
    ShadowOuterDummy.class,
    ShadowOuterDummy.ShadowInnerDummy.class,
    ShadowUniqueDummy.class,
    ShadowUniqueDummy.ShadowInnerDummy.class,
    ShadowUniqueInnerDummy.class
  };
  
  public static ShadowDummy shadowOf(Dummy actual) {
    return (ShadowDummy) shadowOf_(actual);
  }
  
  public static ShadowOuterDummy shadowOf(OuterDummy actual) {
    return (ShadowOuterDummy) shadowOf_(actual);
  }
  
  public static ShadowOuterDummy.ShadowInnerDummy shadowOf(OuterDummy.InnerDummy actual) {
    return (ShadowOuterDummy.ShadowInnerDummy) shadowOf_(actual);
  }
  
  public static ShadowUniqueDummy shadowOf(UniqueDummy actual) {
    return (ShadowUniqueDummy) shadowOf_(actual);
  }
  
  public static ShadowUniqueDummy.ShadowInnerDummy shadowOf(UniqueDummy.InnerDummy actual) {
    return (ShadowUniqueDummy.ShadowInnerDummy) shadowOf_(actual);
  }
  
  public static ShadowUniqueInnerDummy shadowOf(UniqueInnerDummy actual) {
    return (ShadowUniqueInnerDummy) shadowOf_(actual);
  }
  
  public static void reset() {
    ShadowDummy.resetter_method();
  }

  @SuppressWarnings({"unchecked"})
  public static <P, R> P shadowOf_(R instance) {
    return (P) ShadowExtractor.extract(instance);
  }
}

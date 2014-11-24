package org.robolectric;

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

@Generated("org.robolectric.annotation.processing.RoboProcessor")
public class Shadows {

  public static final Class<?>[] DEFAULT_SHADOW_CLASSES = {
    ShadowDummy.class,
    ShadowOuterDummy2.class,
    ShadowInnerPackage.class,
    ShadowInnerPrivate.class,
    ShadowInnerProtected.class,
    ShadowPrivate.class
  };
  
  public static ShadowDummy shadowOf(Dummy actual) {
    return (ShadowDummy) shadowOf_(actual);
  }
  
  public static ShadowOuterDummy2 shadowOf(OuterDummy2 actual) {
    return (ShadowOuterDummy2) shadowOf_(actual);
  }
  
  public static void reset() {
    ShadowDummy.resetter_method();
    ShadowPrivate.resetMethod();
  }

  @SuppressWarnings({"unchecked"})
  public static <P, R> P shadowOf_(R instance) {
    return (P) ShadowExtractor.extract(instance);
  }
}

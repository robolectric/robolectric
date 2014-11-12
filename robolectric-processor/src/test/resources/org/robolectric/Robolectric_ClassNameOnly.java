package org.robolectric;

import javax.annotation.Generated;

import org.robolectric.annotation.processing.objects.AnyObject;
import org.robolectric.annotation.processing.objects.Dummy;
import org.robolectric.annotation.processing.shadows.ShadowClassNameOnly;
import org.robolectric.annotation.processing.shadows.ShadowDummy;
import org.robolectric.util.ShadowExtractor;

@Generated("org.robolectric.annotation.processing.RoboProcessor")
public class Shadows {

  public static final Class<?>[] DEFAULT_SHADOW_CLASSES = {
    ShadowClassNameOnly.class,
    ShadowDummy.class,
  };
  
  public static ShadowClassNameOnly shadowOf(AnyObject actual) {
    return (ShadowClassNameOnly) shadowOf_(actual);
  }
  
  public static ShadowDummy shadowOf(Dummy actual) {
    return (ShadowDummy) shadowOf_(actual);
  }
  
  public static void reset() {
    ShadowClassNameOnly.anotherResetter();
    ShadowDummy.resetter_method();
  }

  @SuppressWarnings({"unchecked"})
  public static <P, R> P shadowOf_(R instance) {
    return (P) ShadowExtractor.extract(instance);
  }
}

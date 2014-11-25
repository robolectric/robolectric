package org.robolectric;

import javax.annotation.Generated;

import org.robolectric.annotation.processing.objects.AnyObject;
import org.robolectric.annotation.processing.objects.Dummy;
import org.robolectric.annotation.processing.shadows.ShadowClassNameOnly;
import org.robolectric.annotation.processing.shadows.ShadowDummy;
import org.robolectric.internal.ShadowExtractor;

@Generated("org.robolectric.annotation.processing.RoboProcessor")
public class Shadows {

  public static final Class<?>[] DEFAULT_SHADOW_CLASSES = {
    ShadowClassNameOnly.class,
    ShadowDummy.class,
  };
  
  public static ShadowClassNameOnly shadowOf(AnyObject actual) {
    return (ShadowClassNameOnly) ShadowExtractor.extract(actual);
  }
  
  public static ShadowDummy shadowOf(Dummy actual) {
    return (ShadowDummy) ShadowExtractor.extract(actual);
  }
  
  public static void reset() {
    ShadowClassNameOnly.anotherResetter();
    ShadowDummy.resetter_method();
  }
}

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
import org.robolectric.internal.ShadowProvider;

@Generated("org.robolectric.annotation.processing.RobolectricProcessor")
@SuppressWarnings({"unchecked","deprecation"})
public class Shadows implements ShadowProvider {

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

  public String[] getProvidedPackageNames() {
    return new String[] {"org.robolectric.annotation.processing.objects"};
  }
}

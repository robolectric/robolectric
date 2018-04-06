package org.robolectric.annotation.processing.shadows;

import com.example.objects.OuterDummy2;
import org.robolectric.annotation.Implements;

/** A Shadow that implements an outer class */
@Implements(OuterDummy2.class)
public class ShadowOuterDummy2 {

  /** A Shadow that implements an protected inner class name */
  @Implements(className = "com.example.objects.OuterDummy2$InnerProtected")
  public static class ShadowInnerProtected {}

  /** A Shadow that implements an inner package-private class */
  @Implements(className = "com.example.objects.OuterDummy2$InnerPackage")
  public static class ShadowInnerPackage {}

  /** A Shadow that implements an inner private class */
  @Implements(className = "com.example.objects.OuterDummy2$InnerPrivate", maxSdk = 1)
  public static class ShadowInnerPrivate {}
}

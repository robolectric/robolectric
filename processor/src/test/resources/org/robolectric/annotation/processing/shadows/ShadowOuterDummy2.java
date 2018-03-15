package org.robolectric.annotation.processing.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;
import com.example.objects.OuterDummy2;

@Implements(OuterDummy2.class)
public class ShadowOuterDummy2 {

  @Implements(value=Robolectric.Anything.class,
              className="com.example.objects.OuterDummy2$InnerProtected")
  public static class ShadowInnerProtected {
  }

  @Implements(className="com.example.objects.OuterDummy2$InnerPackage")
  public static class ShadowInnerPackage {
  }

  @Implements(className="com.example.objects.OuterDummy2$InnerPrivate", maxSdk = 1)
  public static class ShadowInnerPrivate {
  }
}

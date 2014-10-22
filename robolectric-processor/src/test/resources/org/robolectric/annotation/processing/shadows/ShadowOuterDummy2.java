package org.robolectric.annotation.processing.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.annotation.processing.objects.OuterDummy2;

@Implements(OuterDummy2.class)
public class ShadowOuterDummy2 {

  @Implements(value=Robolectric.Anything.class,
              className="org.robolectric.annotation.processing.objects.OuterDummy2$InnerProtected")
  public class ShadowInnerProtected {
  }

  @Implements(value=Robolectric.Anything.class,
              className="org.robolectric.annotation.processing.objects.OuterDummy2$InnerPackage")
  public class ShadowInnerPackage {
  }

  @Implements(value=Robolectric.Anything.class,
              className="org.robolectric.annotation.processing.objects.OuterDummy2$InnerPrivate")
  public class ShadowInnerPrivate {
  }
}

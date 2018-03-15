package org.robolectric.annotation.processing.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;
import com.example.objects.OuterDummy2;

@Implements(OuterDummy2.class)
public class ShadowOuterDummyWithErrs {

  @Implements(className="com.example.objects.OuterDummy2$InnerProtected")
  public class ShadowInnerProtected {
  }
}

package org.robolectric.annotation.processing.shadows;

import com.example.objects.OuterDummy2;
import org.robolectric.annotation.Implements;

@Implements(OuterDummy2.class)
public class ShadowOuterDummyWithErrs {

  @Implements(className="com.example.objects.OuterDummy2$InnerProtected")
  public class ShadowInnerProtected {
  }
}

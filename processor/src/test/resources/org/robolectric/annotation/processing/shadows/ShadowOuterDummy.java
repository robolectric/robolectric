package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import com.example.objects.OuterDummy;

@Implements(OuterDummy.class)
public class ShadowOuterDummy {

  @Implements(OuterDummy.InnerDummy.class)
  public class ShadowInnerDummy {
  }
}

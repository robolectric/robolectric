package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import com.example.objects.UniqueDummy;

@Implements(UniqueDummy.class)
public class ShadowUniqueDummy {

  @Implements(UniqueDummy.InnerDummy.class)
  public class ShadowInnerDummy {
  }
  
  @Implements(UniqueDummy.UniqueInnerDummy.class)
  public class ShadowUniqueInnerDummy {
  }
}

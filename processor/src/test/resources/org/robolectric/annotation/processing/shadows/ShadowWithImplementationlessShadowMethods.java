package org.robolectric.annotation.processing.shadows;

import com.example.objects.Dummy;
import org.robolectric.annotation.Implements;

@Implements(Dummy.class)
public class ShadowWithImplementationlessShadowMethods {
  public void __constructor__() {}

  public void __staticInitializer__() {}
}

package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import com.example.objects.Dummy;

@Implements(Dummy.class)
public class ShadowWithImplementationlessShadowMethods {
  public void __constructor__() {
  }

  public void __staticInitializer__() {
  }
}

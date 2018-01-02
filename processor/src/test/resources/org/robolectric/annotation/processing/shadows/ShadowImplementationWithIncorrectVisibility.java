package org.robolectric.annotation.processing.shadows;

import com.example.objects.Dummy;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(Dummy.class)
public class ShadowImplementationWithIncorrectVisibility {
  @Implementation
  public void __constructor__(int i0) {
  }

  @Implementation
  protected void __constructor__(int i0, int i1) {
  }

  @Implementation
  void __constructor__(int i0, int i1, int i2) {
  }

  @Implementation
  private void __constructor__(int i0, int i1, int i2, int i3) {
  }

  @Implementation
  public static void publicMethod() {}

  @Implementation
  protected static void protectedMethod() {}

  @Implementation
  static void packageMethod() {}

  @Implementation
  private static void privateMethod() {}
}

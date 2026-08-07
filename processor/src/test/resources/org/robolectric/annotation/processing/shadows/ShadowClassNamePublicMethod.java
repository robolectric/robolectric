package org.robolectric.annotation.processing.shadows;

import com.example.objects.DummyClassName;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = DummyClassName.class)
public class ShadowClassNamePublicMethod {
  @Implementation
  public @ClassName("com.example.objects.Dummy") Object methodWithReturnType() {
    return null;
  }

  public void methodWithParameter(@ClassName("com.example.objects.Dummy") Object param) {}
}
